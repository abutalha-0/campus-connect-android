package com.campusconnect.app.notifications.model;

/** NEW (Notifications feature): mirrors notifications.Notification. */
public class Notification {
    private int id;
    private Integer actor;
    private String notification_type;
    private Integer post;
    private String post_title;
    private Integer join_request;
    private String message;
    private String action_url;
    private boolean is_read;
    private String read_at;
    private String created_at;

    public int getId() { return id; }
    public Integer getActor() { return actor; }
    public String getNotificationType() { return notification_type; }
    public Integer getPost() { return post; }
    public String getPostTitle() { return post_title; }
    public Integer getJoinRequest() { return join_request; }
    public String getMessage() { return message; }
    public String getActionUrl() { return action_url; }
    public boolean isRead() { return is_read; }
    public String getReadAt() { return read_at; }
    public String getCreatedAt() { return created_at; }
}
