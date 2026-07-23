package com.campusconnect.app.classroom.model;

public class FeedPostRequest {
    private String title;
    private String body;
    private String tag;

    public FeedPostRequest(String title, String body, String tag) {
        this.title = title;
        this.body = body;
        this.tag = tag;
    }
}
