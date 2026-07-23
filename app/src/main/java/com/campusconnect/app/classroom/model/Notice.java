package com.campusconnect.app.classroom.model;

public class Notice {
    private int id;
    private String text;
    private String highlight;
    private String event_date;
    private String event_time;
    private String attachment_url;
    private String created_at;
    private Author author;
    private boolean mine;
    private boolean can_edit;
    private boolean has_highlight;

    public int getId() { return id; }
    public String getText() { return text; }
    public String getHighlight() { return highlight; }
    public String getEventDate() { return event_date; }
    public String getEventTime() { return event_time; }
    public String getAttachmentUrl() { return attachment_url; }
    public String getCreatedAt() { return created_at; }
    public Author getAuthor() { return author; }
    public boolean isMine() { return mine; }
    public boolean canEdit() { return can_edit; }
    public boolean hasHighlight() { return has_highlight; }

    public static class Author {
        private int id;
        private String full_name;
        private String role;

        public int getId() { return id; }
        public String getFullName() { return full_name; }
        public String getRole() { return role; }
    }
}
