package de.mytb.liretools;

import net.semanticmetadata.lire.utils.CommandLineUtils;
import net.semanticmetadata.lire.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static de.mytb.liretools.SearchOptions.MODE_JSON;

public class FileSearcher {

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

    public static void main(String[] args) throws IOException {

        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[] { "-i" });
        SearchOptions searchOptions = new SearchOptions(p);
        boolean passed = false;
        File f = new File(p.getProperty("-i"));
        System.err.println("f" + f + " exists" + f.exists() + " dir" + f.isDirectory());
        if (f.exists() && f.isDirectory()) {
            passed = true;
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
        System.err.println("Start searching files in:'" + f.getAbsolutePath() + "'"
            + " files: '" + allImages.size() + "'"
            + " luceneindexPath:'" + searchOptions.getLuceneIndexPath() + "'"
            + " bitSampling:'" + searchOptions.isFlgUseBitSampling() + "'"
            + " minScore:'" + searchOptions.getMaxDifferenceScore() + "'"
            + " numOfThreads:" + searchOptions.getNumOfThreads()
            + " features: '" + Arrays.toString(searchOptions.getFeatures().toArray()) + "'"
        );

        writer.println(formatter.generateHeader());

        Queue<SearchRequest<?>> fileQueue = allImages.stream()
            .map(s -> new SearchRequest<>(SearchRequestType.FILE, s))
            .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        try {
            searcher.parallelProcessQueue(searchOptions.getNumOfThreads(), fileQueue, searchOptions.getFeatures(),
                searchOptions.getMaxDifferenceScore(),
                searchOptions.getShowSimilarHits(), searchOptions.isFlgUseBitSampling());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        writer.println(formatter.generateFooter());
    }

}
