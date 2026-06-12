package com.campusconnect.app.user;

import java.util.List;

public class UserListResponse {
    private int count;
    private String next;
    private String previous;
    private List<User> results;

    public int getCount() { return count; }
    public String getNext() { return next; }
    public List<User> getResults() { return results; }
}