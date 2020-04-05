package de.mytb.liretools;

import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.Extractor;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.CommandLineUtils;

import java.io.File;
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
        int numOfThreads = 4; // the number of thread used.
        boolean useBitSampling = false;
        String luceneIndexPath = "index";
        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[]{"-i"});
        boolean passed = false;
        File f = new File(p.getProperty("-i"));
        if (f.exists() && f.isDirectory()) passed = true;

        // if there is a second argument, let's assume it is the number of threads used.
        if (p.getProperty("-n") != null && p.getProperty("-n").matches("\\d+")) {
            numOfThreads = Integer.parseInt(p.getProperty("-n")); // included sanity check.
        }
        if (p.getProperty("-b") != null) {
            useBitSampling = true;
        }
        if (p.getProperty("-l") != null) {
            luceneIndexPath = p.getProperty("-l");
        }
        if (!passed) {
            System.out.println("No directory given as first argument.");
            System.out.println(helpMessage);
            System.exit(1);
        }

        ParallelIndexer indexer = useBitSampling
                ? new ParallelIndexer(numOfThreads, luceneIndexPath, f.getAbsolutePath(), GlobalDocumentBuilder.HashingMode.BitSampling)
                : new ParallelIndexer(numOfThreads, luceneIndexPath, f.getAbsolutePath());
        if (p.get("-f") != null && p.get("-f") != null) {
            String argf = p.getProperty("-f");
            String[] featuresNames = new String[1];
            if (argf.contains(",")) {
                // split features
                featuresNames = argf.split(",");
            } else {
                featuresNames[0] = argf;
            }
            for (String feature: featuresNames) {
                if (!feature.contains(".")) {
                    feature = "net.semanticmetadata.lire.imageanalysis.features.global." + feature;
                }
                try {
                    indexer.addExtractor((Class<Extractor>) Class.forName(feature));
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        } else {
            indexer.addExtractor(CEDD.class);
            indexer.addExtractor(FCTH.class);
            indexer.addExtractor(OpponentHistogram.class);
            //indexer.addExtractor(JointHistogram.class);
            //indexer.addExtractor(AutoColorCorrelogram.class);
            indexer.addExtractor(ColorLayout.class);
            //indexer.addExtractor(EdgeHistogram.class);
            //indexer.addExtractor(Gabor.class);
            indexer.addExtractor(JCD.class);
            //indexer.addExtractor(JpegCoefficientHistogram.class);
            //indexer.addExtractor(ScalableColor.class);
            indexer.addExtractor(SimpleColorHistogram.class);
            //indexer.addExtractor(Tamura.class);
            //indexer.addExtractor(LuminanceLayout.class);
            //indexer.addExtractor(PHOG.class);
            //indexer.addExtractor(LocalBinaryPatterns.class);
        }

        System.out.println("Start indexing:'" + f.getAbsolutePath() + "'"
                + " luceneindexPath:'" + luceneIndexPath + "'"
                + " bitSampling:'" + useBitSampling + "'"
                + " threads:'" + numOfThreads + "'");
        indexer.run();
        System.out.println("Finished indexing.");
    }
}
