package com.campusconnect.app.classroom.model;

public class FeedComment {
    private int id;
    private String text;
    private String created_at;
    private FeedPost.Author author;
    private boolean can_edit;

    public int getId() { return id; }
    public String getText() { return text; }
    public String getCreatedAt() { return created_at; }
    public FeedPost.Author getAuthor() { return author; }
    public boolean canEdit() { return can_edit; }
}
