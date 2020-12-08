package de.mytb.liretools;

import net.semanticmetadata.lire.utils.CommandLineUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static de.mytb.liretools.SearchOptions.MODE_JSON;

public class SimilarIndexedFileSearcher {

    private static final String helpMessage = "$> Searcher -i <directory> [-f <feature>]\n" +
        "\n" +
        "Search for features vector from each file in a directory and in lucene index.\n" +
        "\n" +
        "Options\n" +
        "=======\n" +
        "-b ... use BitSampling, default is false\n" +
        "-d ... debug default: false\n" +
        "-f ... the feature vector, FCTH is default. Example -g net.semanticmetadata.lire.imageanalysis.features.global.CEDD\n" +
        "-l ... lucene-index-path default: index.\n" +
        "-m ... maxDifferenceScore default: 5\n" +
        "-n ... number of threads default: 4. \n" +
        "-s ... showSimilarHits default: 1\n" +
        "\n"
        ;

    public static void main(String[] args) throws IOException {

        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[] {  });
        SearchOptions searchOptions = new SearchOptions(p);
        Formatter formatter;
        if (MODE_JSON.equals(searchOptions.getMode())) {
            formatter = new JsonFormatter();
        } else {
            formatter = new TextFormatter();
        }
        Writer writer = new StdOutWriter();

        CommonSearcher searcher = new CommonSearcher(searchOptions.getLuceneIndexPath(), writer, formatter,
            searchOptions.getMaxDifferenceScore(), searchOptions.isFlgUseBitSampling(),
            searchOptions.getShowSimilarHits(), searchOptions.isFlgDebug());
        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(searchOptions.getLuceneIndexPath())));
        List<SearchRequest<?>> allRequests = new ArrayList<>();
        for (int i=0; i < ir.maxDoc(); i++) {
            Document doc = ir.document(i);
            allRequests.add(new SearchRequest<>(SearchRequestType.DOCUMENT, doc));
        }
        System.err.println("Start simalar docs in index - "
            + " docs: '" + allRequests.size() + "'"
            + " luceneindexPath:'" + searchOptions.getLuceneIndexPath() + "'"
            + " bitSampling:'" + searchOptions.isFlgUseBitSampling() + "'"
            + " minScore:'" + searchOptions.getMaxDifferenceScore() + "'"
            + " numOfThreads:" + searchOptions.getNumOfThreads()
            + " features: '" + Arrays.toString(searchOptions.getFeatures().toArray()) + "'"
        );

        writer.println(formatter.generateHeader());

        Queue<SearchRequest<?>> documentQueue = new LinkedBlockingQueue<>(allRequests);
        try {
            searcher.parallelProcessQueue(searchOptions.getNumOfThreads(), documentQueue, searchOptions.getFeatures(),
                searchOptions.getMaxDifferenceScore(),
                searchOptions.getShowSimilarHits(), searchOptions.isFlgUseBitSampling());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        writer.println(formatter.generateFooter());
    }

}
