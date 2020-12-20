package de.mytb.liretools;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class TextFormatter implements Formatter {
    @Override
    public String generateHeader() {
        return "";
    }

    @Override
    public String generateFooter() {
        return "";
    }

    @Override
    public String generateFileEntrySeparator() {
        return "";
    }
    public String generateFileResult(double maxDifferenceScore, String searchFileName,
        Map<String, FileMatchingHits> fileFavs) {
        return searchFileName + ": [" + fileFavs.keySet().stream()
            .filter(s -> fileFavs.get(s).getMatchingHits().stream()
                .anyMatch(matchingHit -> matchingHit.getScore() < maxDifferenceScore))
            .sorted(Comparator.comparing(o -> fileFavs.get(o).getMinScore()))
            .map(s -> s + ": " + fileFavs.get(s).getMatchingHits().stream()
                .filter(matchingHit -> matchingHit.getScore() < maxDifferenceScore)
                .sorted(Comparator.comparing(MatchingHit::getScore)).map(MatchingHit::toString)
                .collect(Collectors.joining(";"))).collect(Collectors.joining("|")) + "]";
    }

}
