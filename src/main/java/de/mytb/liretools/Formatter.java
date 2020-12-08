package de.mytb.liretools;

import java.util.Map;

public interface Formatter {
    String generateFileResult(double maxDifferenceScore, String searchFileName,
        Map<String, FileMatchingHits> fileFavs);
    String generateHeader();
    String generateFooter();
    String generateFileEntrySeparator();

}
