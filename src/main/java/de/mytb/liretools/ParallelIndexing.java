package de.mytb.liretools;

import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.Extractor;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.CommandLineUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

public class ParallelIndexing {
    private static final String helpMessage = "$> ParallelIndexing -i <directory> [-f <feature>]\n" +
        "\n" +
        "Index a features vector from each file in a directory and add to lucene index.\n" +
        "\n" +
        "Options\n" +
        "=======\n" +
        "-b ... use BitSampling, default is false. \n" +
        "-f ... the feature vector, FCTH is default. Example -g net.semanticmetadata.lire.imageanalysis.features.global.CEDD\n" +
        "-i ... the directory with the images, files with .jpg and .png are read. \n" +
        "-l ... lucene-index-path default: index. \n" +
        "-n ... number of threads default: 4. \n" +
        "\n";

    public static void main(String[] args) {
        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[]{"-i"});
        IndexOptions indexOptions = new IndexOptions(p);
        boolean passed = false;

        File f = new File(p.getProperty("-i"));
        if (f.exists() && f.isDirectory()) passed = true;

        // if there is a second argument, let's assume it is the number of threads used.
        if (!passed) {
            System.out.println("No directory given as first argument.");
            System.out.println(helpMessage);
            System.exit(1);
        }

        ParallelIndexer indexer = indexOptions.isFlgUseBitSampling()
            ? new ParallelIndexer(indexOptions.getNumOfThreads(), indexOptions.getLuceneIndexPath(),
            f.getAbsolutePath(), GlobalDocumentBuilder.HashingMode.BitSampling)
            : new ParallelIndexer(indexOptions.getNumOfThreads(), indexOptions.getLuceneIndexPath(),
            f.getAbsolutePath());

        indexOptions.getFeatures()
            .forEach(indexer::addExtractor);

        System.out.println("Start indexing:'" + f.getAbsolutePath() + "'"
            + " luceneindexPath:'" + indexOptions.getLuceneIndexPath() + "'"
            + " bitSampling:'" + indexOptions.isFlgUseBitSampling() + "'"
            + " threads:'" + indexOptions.getNumOfThreads() + "'"
            + " features: '" + Arrays.toString(indexOptions.getFeatures().toArray()) + "'"
        );
        indexer.run();
        System.out.println("Finished indexing.");
    }
}
