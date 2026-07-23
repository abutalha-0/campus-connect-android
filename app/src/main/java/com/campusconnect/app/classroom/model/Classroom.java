package com.campusconnect.app.classroom.model;

import java.util.List;

public class Classroom {
    private int id;
    private String code;
    private List<Subject> subjects;
    private boolean is_creator;
    private int creator_id;
    private String creator_name;
    private String created_at;

    public int getId() { return id; }
    public String getCode() { return code; }
    public List<Subject> getSubjects() { return subjects; }
    public boolean isCreator() { return is_creator; }
    public int getCreatorId() { return creator_id; }
    public String getCreatorName() { return creator_name; }
    public String getCreatedAt() { return created_at; }
}
