package com.campusconnect.app.classroom.model;

public class Notice {
    private int id;
    private String text;
    private String highlight;
    private String attachment_url;
    private String created_at;
    private Author author;
    private boolean mine;
    private boolean can_edit;

    public int getId() { return id; }
    public String getText() { return text; }
    public String getHighlight() { return highlight; }
    public String getAttachmentUrl() { return attachment_url; }
    public String getCreatedAt() { return created_at; }
    public Author getAuthor() { return author; }
    public boolean isMine() { return mine; }
    public boolean canEdit() { return can_edit; }

    public static class Author {
        private int id;
        private String full_name;
        private String role;

        public int getId() { return id; }
        public String getFullName() { return full_name; }
        public String getRole() { return role; }
    }
}
