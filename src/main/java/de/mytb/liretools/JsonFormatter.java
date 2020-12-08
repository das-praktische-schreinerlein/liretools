package de.mytb.liretools;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonFormatter implements Formatter {
    public String generateFileResult(double maxDifferenceScore, String searchFileName,
        Map<String, FileMatchingHits> fileFavs) {
        return generateJsonRecord(
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
                    generateJsonList(generateFileResultRecords(maxDifferenceScore, fileFavs)
                    )
                )
            )
        );
    }

    @Override
    public String generateHeader() {
        return "{"
            + "  \"files\": [";
    }

    @Override
    public String generateFooter() {
        return generateFileResult(0, "dummydir/dummyfile", new HashMap<>())
            + "  ]\n"
            + "}";
    }

    @Override
    public String generateFileEntrySeparator() {
        return ",\n";
    }

    private String generateFileResultRecords(double maxDifferenceScore, Map<String, FileMatchingHits> fileFavs) {
        return fileFavs.keySet().stream().filter(s -> fileFavs.get(s).getMatchingHits().stream()
            .anyMatch(matchingHit -> matchingHit.getScore() < maxDifferenceScore))
            .sorted(Comparator.comparing(o -> fileFavs.get(o).getMinScore())).map(
                s -> fileFavs.get(s).getMatchingHits().stream()
                    .filter(matchingHit -> matchingHit.getScore() < maxDifferenceScore)
                    .sorted(Comparator.comparing(MatchingHit::getScore)).map(matchingHit -> generateJsonRecord(String
                        .join(", ", generateJsonTextField("id", escapeJsonValue(s)),
                            generateJsonTextField("dir", escapeJsonValue(getDirName(s))),
                            generateJsonTextField("name", escapeJsonValue(getFileName(s))),
                            generateJsonTextField("matching", "SIMILARITY"),
                            generateJsonTextField("matchingDetails", matchingHit.getExtractor().getSimpleName()),
                            generateJsonTextField("matchingScore", matchingHit.getScore().toString()))))
                    .collect(Collectors.joining(",\n"))).collect(Collectors.joining(",\n"));
    }

    private static String getFileName(String file) {
        return (new File(file)).getName();
    }

    private static String getDirName(String file) {
        return (new File(file)).getParentFile().getAbsolutePath();
    }

    private String escapeJsonValue(String value) {
        return value.replaceAll("\\\\", "\\\\\\\\");
    }

    private String generateJsonField(String field, String value) {
        return "  \"" + field + "\": " + value + "";
    }

    private String generateJsonTextField(String field, String value) {
        return "  \"" + field + "\": \"" + value + "\"";
    }

    private String generateJsonRecord(String values) {
        return "    {" + values + "}";
    }

    private String generateJsonList(String values) {
        return "    [\n" + values + "\n    ]";
    }


}
