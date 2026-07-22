package com.campusconnect.app.classroom.model;

import java.util.List;

public class Classroom {
    private int id;
    private String code;
    private List<Subject> subjects;
    private boolean is_creator;
    private String creator_name;
    private String created_at;

    public int getId() { return id; }
    public String getCode() { return code; }
    public List<Subject> getSubjects() { return subjects; }
    public boolean isCreator() { return is_creator; }
    public String getCreatorName() { return creator_name; }
    public String getCreatedAt() { return created_at; }
}
