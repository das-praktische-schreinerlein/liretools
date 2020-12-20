package de.mytb.liretools;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchRequest<R> {
    private final SearchRequestType type;
    private final R request;
}
