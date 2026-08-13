package com.campusconnect.app.core.api;

import java.util.List;

/** DRF's PageNumberPagination envelope: {"count", "next", "previous", "results"}. */
public class PageResponse<T> {
    private int count;
    private String next;
    private String previous;
    private List<T> results;

    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<T> getResults() { return results; }
}
