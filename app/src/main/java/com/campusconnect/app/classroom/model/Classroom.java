package com.campusconnect.app.classroom.model;

import java.util.List;

public class Classroom {
    private int id;
    private String code;
    private List<Subject> subjects;
    private String created_at;

    public int getId() { return id; }
    public String getCode() { return code; }
    public List<Subject> getSubjects() { return subjects; }
    public String getCreatedAt() { return created_at; }
}
