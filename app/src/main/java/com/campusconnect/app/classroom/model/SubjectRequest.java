package com.campusconnect.app.classroom.model;

public class SubjectRequest {
    private String name;
    private String intake;
    private String section;
    private String room;

    public SubjectRequest(String name, String intake, String section, String room) {
        this.name = name;
        this.intake = intake;
        this.section = section;
        this.room = room;
    }
}
