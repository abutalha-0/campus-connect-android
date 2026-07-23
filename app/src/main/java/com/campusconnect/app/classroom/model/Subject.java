package com.campusconnect.app.classroom.model;

public class Subject {
    private int id;
    private String name;
    private String intake;
    private String section;
    private String room;
    private String code;
    private String faculty_name;
    private int faculty_user_id;
    private boolean is_owner;
    private boolean can_post;
    private String created_at;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getIntake() { return intake; }
    public String getSection() { return section; }
    public String getRoom() { return room; }
    public String getCode() { return code; }
    public String getFacultyName() { return faculty_name; }
    public int getFacultyUserId() { return faculty_user_id; }
    public boolean isOwner() { return is_owner; }
    public boolean canPost() { return can_post; }
    public String getCreatedAt() { return created_at; }
}
