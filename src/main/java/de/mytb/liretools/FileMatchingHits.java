package de.mytb.liretools;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FileMatchingHits {
    private final String fileName;
    private Double minScore;
    private final List<MatchingHit> matchingHits;

}
