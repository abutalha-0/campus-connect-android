package com.campusconnect.app.classroom.model;

public class FeedPost {
    private int id;
    private String tag;
    private String title;
    private String body;
    private String created_at;
    private Author author;
    private boolean can_edit;
    private int score;
    private int my_vote;
    private int comments_count;

    public int getId() { return id; }
    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getCreatedAt() { return created_at; }
    public Author getAuthor() { return author; }
    public boolean canEdit() { return can_edit; }
    public int getScore() { return score; }
    public int getMyVote() { return my_vote; }
    public int getCommentsCount() { return comments_count; }

    public static class Author {
        private int id;
        private String full_name;
        private String role;

        public int getId() { return id; }
        public String getFullName() { return full_name; }
        public String getRole() { return role; }
    }
}
