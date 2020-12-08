package de.mytb.liretools;

import java.util.List;

public class FileMatchingHits {
    private final String fileName;
    private Double minScore;
    private final List<MatchingHit> matchingHits;

    public FileMatchingHits(String fileName, Double minScore, List<MatchingHit> matchingHits) {
        this.fileName = fileName;
        this.minScore = minScore;
        this.matchingHits = matchingHits;
    }

    public String getFileName() {
        return fileName;
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public List<MatchingHit> getMatchingHits() {
        return matchingHits;
    }
}
