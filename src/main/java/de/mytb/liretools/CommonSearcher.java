package de.mytb.liretools;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.searchers.BitSamplingImageSearcher;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommonSearcher {

    private static final DecimalFormat DF2 = new DecimalFormat("#.##");
    protected final String luceneIndexPath;
    protected final Writer writer;
    protected final Formatter formatter;
    protected final double maxDifferenceScore;
    protected final boolean flgUseBitSampling;
    protected final int showSimilarHits;
    protected final boolean flgDebug;

    public CommonSearcher(String luceneIndexPath, Writer writer, Formatter formatter, double maxDifferenceScore,
        boolean flgUseBitSampling, int showSimilarHits, boolean flgDebug) {
        this.luceneIndexPath = luceneIndexPath;
        this.writer = writer;
        this.formatter = formatter;
        this.maxDifferenceScore = maxDifferenceScore;
        this.flgUseBitSampling = flgUseBitSampling;
        this.showSimilarHits = showSimilarHits;
        this.flgDebug = flgDebug;
    }

    public void parallelProcessQueue(int numOfThreads, Queue<SearchRequest<?>> requestQueue,
        List<Class<? extends GlobalFeature>> features, double maxDifferenceScore, int showSimilarHits,
        boolean flgUseBitSampling) throws InterruptedException {
        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> callables =
            IntStream.range(1, numOfThreads + 1).boxed().collect(Collectors.toList()).stream().map(
                o -> createQueueProcessor(requestQueue, features, maxDifferenceScore, showSimilarHits, flgUseBitSampling))
                .collect(Collectors.toList());

        executor.invokeAll(callables).stream().map(future -> {
            try {
                return future.get();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).forEach(System.err::println);
    }

    public void processQueue(Queue<SearchRequest<?>> requestQueue, List<Class<? extends GlobalFeature>> features,
        double maxDifferenceScore, int showSimilarHits, boolean flgUseBitSampling) throws IOException {
        Map<Class<? extends GlobalFeature>, ImageSearcher> imageSearcherMap =
            new HashMap<>();
        for (Class<? extends GlobalFeature> featureClass : features) {
            try {
                imageSearcherMap.put(featureClass, flgUseBitSampling ?
                    new BitSamplingImageSearcher(showSimilarHits, featureClass.newInstance()) :
                    new GenericFastImageSearcher(showSimilarHits, featureClass));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(luceneIndexPath)));

        while (!requestQueue.isEmpty()) {
            SearchRequest<?> searchRequest = requestQueue.poll();
            searchHits(searchRequest, maxDifferenceScore, imageSearcherMap, ir);
        }
    }

    protected void searchHits(SearchRequest<?> searchrequest, double maxDifferenceScore,
        Map<Class<? extends GlobalFeature>, ImageSearcher> imageSearcherMap, IndexReader ir) {
        try {
            final String searchFileName;
            Document searchDocument = null;
            BufferedImage img = null;

            switch (searchrequest.getType()) {
                case DOCUMENT:
                    searchDocument =  (Document)searchrequest.getRequest();
                    searchFileName = searchDocument.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                    break;
                case FILE:
                    searchFileName = (String)searchrequest.getRequest();
                    File searchFile = new File(searchFileName);
                    img = ImageIO.read(searchFile);
                    break;
                default:
                    throw new IllegalArgumentException("unknown searchType:" + searchrequest.getType());
            }

            Map<String, FileMatchingHits> fileFavs = new HashMap<>();
            for (Map.Entry<Class<? extends GlobalFeature>, ImageSearcher> searcher : imageSearcherMap.entrySet()) {
                final ImageSearchHits hits;
                switch (searchrequest.getType()) {
                    case DOCUMENT:
                        hits = searcher.getValue().search(searchDocument, ir);
                        break;
                    case FILE:
                        hits = searcher.getValue().search(img, ir);
                        break;
                    default:
                        throw new IllegalArgumentException("unknown searchType:" + searchrequest.getType());
                }
                processHits(searcher, ir, fileFavs, hits);
            }

            if (flgDebug) {
                System.err.println("======");
                fileFavs.forEach((s, strings) -> System.err.println("File: " + s + " by " + strings));
            }

            writer.println(formatter.generateFileResult(maxDifferenceScore, searchFileName, fileFavs)
                + formatter.generateFileEntrySeparator());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processHits(Map.Entry<Class<? extends GlobalFeature>, ImageSearcher> searcher, IndexReader ir,
        Map<String, FileMatchingHits> fileFavs, ImageSearchHits hits) throws IOException {
        double max = hits.score(hits.length() - 1);
        for (int i = 0; i < hits.length(); i++) {
            String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            double score = hits.score(i);
            double relScore = score != 0
                ? 100 - score / max * 100
                : 100;
            FileMatchingHits matchings = fileFavs.getOrDefault(fileName,
                new FileMatchingHits(fileName, 99999999D, new ArrayList<>()));
            fileFavs.put(fileName, matchings);
            MatchingHit matchingHit = new MatchingHit(searcher.getKey(), fileName, score, relScore);
            matchings.getMatchingHits().add(matchingHit);
            matchings.setMinScore(Math.min(matchings.getMinScore(), matchingHit.getScore()));
            if (flgDebug) {
                System.err.println(searcher.getKey().toString() + "\t" +
                    DF2.format(score) + "\t" +
                    DF2.format(relScore) + ": \t" +
                    fileName);
            }
        }
    }

    protected Callable<Boolean> createQueueProcessor(Queue<SearchRequest<?>> requestQueue,
        List<Class<? extends GlobalFeature>> features,
        double maxDifferenceScore, int showSimilarHits, boolean flgUseBitSampling) {
        return () -> {
            processQueue(requestQueue, features, maxDifferenceScore, showSimilarHits, flgUseBitSampling);
            return true;
        };
    }
}