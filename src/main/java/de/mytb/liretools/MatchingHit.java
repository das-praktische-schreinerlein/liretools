package de.mytb.liretools;

import java.text.DecimalFormat;

public class MatchingHit {
    private static final DecimalFormat DF2 = new DecimalFormat("#.##");

    private final Class extractor;
    private final String fileName;
    private final Double score;
    private final Double relScore;

    public MatchingHit(Class extractor, String fileName, Double score, Double relScore) {
        this.extractor = extractor;
        this.fileName = fileName;
        this.score = score;
        this.relScore = relScore;
    }

    public String getFileName() {
        return fileName;
    }

    public Double getScore() {
        return score;
    }

    public Double getRelScore() {
        return relScore;
    }

    public Class getExtractor() {
        return extractor;
    }

    public String toString() {
        return "{" + getExtractor().getSimpleName() + ": " + DF2.format(getScore()) + " / " + DF2.format(getRelScore()) + "}";
    }
}
