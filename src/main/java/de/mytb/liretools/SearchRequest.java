package de.mytb.liretools;

public class SearchRequest<R> {
    private final SearchRequestType type;
    private final R request;

    public SearchRequest(SearchRequestType type, R request) {
        this.type = type;
        this.request = request;
    }

    public SearchRequestType getType() {
        return type;
    }

    public R getRequest() {
        return request;
    }
}
