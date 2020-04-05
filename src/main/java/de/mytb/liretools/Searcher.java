package de.mytb.liretools;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.SimpleColorHistogram;
import net.semanticmetadata.lire.searchers.BitSamplingImageSearcher;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.CommandLineUtils;
import net.semanticmetadata.lire.utils.FileUtils;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Searcher {
    private static final DecimalFormat DF2 = new DecimalFormat("#.##");
    private static final String helpMessage = "$> Searcher -i <directory> [-f <feature>]\n" +
            "\n" +
            "Search for features vector from each file in a directory and in lucene index.\n" +
            "\n" +
            "Options\n" +
            "=======\n" +
            "-b ... use BitSampling, default is false\n" +
            "-d ... debug default: false\n" +
            "-f ... the feature vector, FCTH is default. Example -g net.semanticmetadata.lire.imageanalysis.features.global.CEDD\n" +
            "-i ... the directory with the images, files with .jpg and .png are read.\n" +
            "-l ... lucene-index-path default: index.\n" +
            "-m ... maxDifferenceScore default: 5\n" +
            "-n ... number of threads default: 4. \n" +
            "-s ... showSimilarHits default: 1\n" +
            "\n"
            ;

    private final String luceneIndexPath;

    public static void main(String[] args) throws IOException {
        boolean flgDebug = false;
        final double maxDifferenceScore;
        boolean flgUseBitSampling = false;
        String luceneIndexPath = "index";
        int numOfThreads = 4;
        int showSimilarHits;
        String mode = "json";

        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[] { "-i" });
        boolean passed = false;
        File f = new File(p.getProperty("-i"));
        System.err.println("f" + f + " exists" + f.exists() + " dir" + f.isDirectory());
        if (f.exists() && f.isDirectory()) {
            passed = true;
        }

        if (p.getProperty("-b") != null) {
            flgUseBitSampling = true;
        }
        if (p.getProperty("-d") != null) {
            flgDebug = true;
        }
        if (p.getProperty("-m") != null) {
            maxDifferenceScore = Double.valueOf(p.getProperty("-m"));
        } else {
            maxDifferenceScore = 5;
        }
        if (p.getProperty("-n") != null) {
            numOfThreads = Integer.valueOf(p.getProperty("-n"));
        }
        if (p.getProperty("-s") != null) {
            showSimilarHits = Integer.valueOf(p.getProperty("-s"));
        } else {
            showSimilarHits = 1;
        }
        if (p.getProperty("-l") != null) {
            luceneIndexPath = p.getProperty("-l");
        }

        List<Class<? extends GlobalFeature>> features = new ArrayList<>();
        if (p.get("-f") != null && p.get("-f") != null) {
            String argf = p.getProperty("-f");
            String[] featuresNames = new String[1];
            if (argf.contains(",")) {
                // split features
                featuresNames = argf.split(",");
            } else {
                featuresNames[0] = argf;
            }
            for (String feature : featuresNames) {
                if (!feature.contains(".")) {
                    feature = "net.semanticmetadata.lire.imageanalysis.features.global." + feature;
                }
                try {
                    features.add((Class<? extends GlobalFeature>) Class.forName(feature));
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        } else {
            features = Arrays.asList(CEDD.class, FCTH.class,
                    //OpponentHistogram.class,
                    //JointHistogram.class,
                    //AutoColorCorrelogram.class,
                    ColorLayout.class,
                    //EdgeHistogram.class,
                    //Gabor.class,
                    //JCD.class,
                    //JpegCoefficientHistogram.class,
                    //ScalableColor.class,
                    SimpleColorHistogram.class
                    //Tamura.class,
                    //LuminanceLayout.class,
                    //PHOG.class,
                    //LocalBinaryPatterns.class
            );
        }

        List<String> allImages = new ArrayList<>();
        if (f.exists()) {
            if (f.isDirectory()) {
                allImages = FileUtils.readFileLines(f, true);
                passed = true;
            } else {
                allImages = Collections.singletonList(args[0]);
                passed = true;
            }
        }

        if (!passed) {
            System.err.println(helpMessage);
            System.exit(1);
        }

        Searcher searcher = new Searcher(luceneIndexPath);
        System.err.println("Start searching files in:'" + f.getAbsolutePath() + "'"
                + " files: '" + allImages.size() + "'"
                + " luceneindexPath:'" + luceneIndexPath + "'"
                + " bitSampling:'" + flgUseBitSampling + "'"
                + " minScore:'" + maxDifferenceScore + "'"
                + " numOfThreads:" + numOfThreads);

        if ("json".equals(mode)) {
            System.out.println("{"
                    + "  \"files\": [");
        }

        Queue<String> fileQueue = new LinkedBlockingQueue<>(allImages);
        try {
            searcher.parallelProcessQueue(numOfThreads, fileQueue, features, maxDifferenceScore, showSimilarHits,
                    flgUseBitSampling, mode, flgDebug);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        if ("json".equals(mode)) {
            System.out.println(generateJsonRecord(
                    String.join(",\n",
                            generateJsonField("file",
                                    generateJsonRecord(
                                            String.join(", ",
                                                    generateJsonTextField("dir", "dummydir"),
                                                    generateJsonTextField("name", "dummyfile")))),
                            generateJsonField("records",
                                    generateJsonList("")))));
            System.out.println("  ]\n"
                    + "}");
        }
    }

    public Searcher(String luceneIndexPath) {
        this.luceneIndexPath = luceneIndexPath;
    }

    public void parallelProcessQueue(int numOfThreads, Queue<String> fileQueue,
            List<Class<? extends GlobalFeature>> features, double maxDifferenceScore, int showSimilarHits,
            boolean flgUseBitSampling, String mode, boolean flgDebug) throws java.lang.InterruptedException {
        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> callables = IntStream.range(1, numOfThreads + 1)
                .boxed()
                .collect(Collectors.toList())
                .stream()
                .map(o -> createQueueProcessor(fileQueue, features, maxDifferenceScore, showSimilarHits,
                        flgUseBitSampling, mode, flgDebug))
                .collect(Collectors.toList());

        executor.invokeAll(callables)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .forEach(System.err::println);
    }

    public void processQueue(Queue<String> fileQueue, List<Class<? extends GlobalFeature>> features,
            double maxDifferenceScore, int showSimilarHits, boolean flgUseBitSampling, String mode,
            boolean flgDebug) throws IOException {
        Map<Class, ImageSearcher> imageSearcherMap = new HashMap<>();
        for (Class<? extends GlobalFeature> featureClass : features) {
            try {
                imageSearcherMap.put(featureClass, flgUseBitSampling
                        ? new BitSamplingImageSearcher(showSimilarHits, featureClass.newInstance())
                        : new GenericFastImageSearcher(showSimilarHits, featureClass));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(luceneIndexPath)));

        while (!fileQueue.isEmpty()) {
            String searchFileName = fileQueue.poll();
            File searchFile = new File(searchFileName);
            try {
                BufferedImage img = ImageIO.read(searchFile);
                Map<String, FileMatchingHits> fileFavs = new HashMap<>();
                for (Map.Entry<Class, ImageSearcher> searcher : imageSearcherMap.entrySet()) {
                    ImageSearchHits hits = searcher.getValue().search(img, ir);
                    double max = hits.score(hits.length() - 1);
                    for (int i = 0; i < hits.length(); i++) {
                        String fileName = ir.document(hits.documentID(0))
                                .getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                        FileMatchingHits matchings = fileFavs.getOrDefault(fileName,
                                new FileMatchingHits(fileName, 99999999D, new ArrayList<>()));
                        fileFavs.put(fileName, matchings);
                        MatchingHit matchingHit = new MatchingHit(searcher.getKey(), fileName, hits.score(0),
                                100 - hits.score(0) / max * 100);
                        matchings.getMatchingHits().add(matchingHit);
                        matchings.setMinScore(Math.min(matchings.getMinScore(), matchingHit.getScore()));
                        fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                        if (flgDebug) {
                            System.err.println(searcher.getKey().toString() +
                                    "\t" + DF2.format(hits.score(i)) +
                                    "\t" + DF2.format(100 - hits.score(i) / max * 100) +
                                    ": \t" + fileName);
                        }
                    }
                }

                if (flgDebug) {
                    System.err.println("======");
                    fileFavs.forEach((s, strings) -> System.err.println("File: " + s + " by " + strings));
                }

                if ("json".equals(mode)) {
                    String out = generateJsonRecord(
                            String.join(",\n",
                                    generateJsonField("file",
                                            generateJsonRecord(
                                                    String.join(", ",
                                                            generateJsonTextField("dir",
                                                                    escapeJsonValue(getDirName(searchFileName))),
                                                            generateJsonTextField("name",
                                                                    escapeJsonValue(getFileName(searchFileName))))
                                            )
                                    ),
                                    generateJsonField("records",
                                            generateJsonList(
                                                    fileFavs.keySet()
                                                            .stream()
                                                            .filter(s ->
                                                                    fileFavs.get(s).getMatchingHits()
                                                                            .stream()
                                                                            .anyMatch(matchingHit -> matchingHit.getScore() < maxDifferenceScore))
                                                            .sorted((o1, o2) -> fileFavs.get(o1).getMinScore().compareTo(fileFavs.get(o2).getMinScore()))
                                                            .map(s -> fileFavs.get(s)
                                                                    .getMatchingHits()
                                                                    .stream()
                                                                    .filter(matchingHit -> matchingHit.getScore() < maxDifferenceScore)
                                                                    .sorted(Comparator.comparing(MatchingHit::getScore))
                                                                    .map(matchingHit -> generateJsonRecord(
                                                                            String.join(", ",
                                                                                    generateJsonTextField("id",
                                                                                            escapeJsonValue(s)),
                                                                                    generateJsonTextField("dir",
                                                                                            escapeJsonValue(getDirName(s))),
                                                                                    generateJsonTextField("name",
                                                                                            escapeJsonValue(getFileName(s))),
                                                                                    generateJsonTextField("matching",
                                                                                            "SIMILARITY"),
                                                                                    generateJsonTextField("matchingDetails",
                                                                                            matchingHit.getExtractor().getSimpleName()),
                                                                                    generateJsonTextField("matchingScore",
                                                                                            matchingHit.getScore().toString())
                                                                            )
                                                                            )
                                                                    )
                                                                    .collect(Collectors.joining(",\n"))
                                                            )
                                                            .collect(Collectors.joining(",\n"))
                                            )
                                    )
                            )
                    ) + ",";
                    System.out.println(out);
                } else {
                    System.out.println(searchFileName + ": [" + fileFavs.keySet()
                            .stream()
                            .filter(s ->
                                    fileFavs.get(s).getMatchingHits().stream()
                                            .anyMatch(matchingHit -> matchingHit.getScore() < maxDifferenceScore))
                            .sorted((o1, o2) -> fileFavs.get(o1).getMinScore().compareTo(fileFavs.get(o2).getMinScore()))
                            .map(s -> s + ": " + fileFavs.get(s)
                                    .getMatchingHits()
                                    .stream()
                                    .filter(matchingHit -> matchingHit.getScore() < maxDifferenceScore)
                                    .sorted(Comparator.comparing(MatchingHit::getScore))
                                    .map(MatchingHit::toString)
                                    .collect(Collectors.joining(";")))
                            .collect(Collectors.joining("|")) + "]");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Callable<Boolean> createQueueProcessor(Queue<String> fileQueue, List<Class<? extends GlobalFeature>> features,
            double maxDifferenceScore, int showSimilarHits, boolean flgUseBitSampling, String mode, boolean flgDebug) {
        return () -> {
            processQueue(fileQueue, features, maxDifferenceScore, showSimilarHits, flgUseBitSampling, mode, flgDebug);
            return true;
        };
    }

    private static String getFileName(String file) {
        return (new File(file)).getName();
    }

    private static String getDirName(String file) {
        return (new File(file)).getParentFile().getAbsolutePath();
    }

    private static String escapeJsonValue(String value) {
        return value.replaceAll("\\\\", "\\\\\\\\");
    }

    private static String generateJsonField(String field, String value) {
        return "  \"" + field + "\": " + value + "";
    }

    private static String generateJsonTextField(String field, String value) {
        return "  \"" + field + "\": \"" + value + "\"";
    }

    private static String generateJsonRecord(String values) {
        return "    {" + values + "}";
    }

    private static String generateJsonList(String values) {
        return "    [\n" + values + "\n    ]";
    }

}
