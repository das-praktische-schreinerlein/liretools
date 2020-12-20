package de.mytb.liretools;

import lombok.Getter;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Getter
public class IndexOptions {
    public static final String MODE_JSON = "json";

    private boolean flgDebug = false;
    private boolean flgUseBitSampling = false;
    private String luceneIndexPath = "index";
    private int numOfThreads = 4;
    private List<Class<? extends GlobalFeature>> features = new ArrayList<>();

    public IndexOptions(Properties p) {
        if (p.getProperty("-b") != null) {
            flgUseBitSampling = true;
        }
        if (p.getProperty("-d") != null) {
            flgDebug = true;
        }
        if (p.getProperty("-n") != null) {
            numOfThreads = Integer.parseInt(p.getProperty("-n"));
        }
        if (p.getProperty("-l") != null) {
            luceneIndexPath = p.getProperty("-l");
        }

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
                OpponentHistogram.class,
                //JointHistogram.class,
                //AutoColorCorrelogram.class,
                ColorLayout.class,
                //EdgeHistogram.class,
                //Gabor.class,
                JCD.class,
                //JpegCoefficientHistogram.class,
                //ScalableColor.class,
                SimpleColorHistogram.class
                //Tamura.class,
                //LuminanceLayout.class,
                //PHOG.class,
                //LocalBinaryPatterns.class
            );
        }

    }
}
