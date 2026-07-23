package com.campusconnect.app.classroom.model;

public class Resource {
    private int id;
    private String title;
    private String resource_type;
    private String description;
    private String file_url;
    private String created_at;
    private boolean can_edit;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getResourceType() { return resource_type; }
    public String getDescription() { return description; }
    public String getFileUrl() { return file_url; }
    public String getCreatedAt() { return created_at; }
    public boolean canEdit() { return can_edit; }
}
