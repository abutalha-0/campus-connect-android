package com.campusconnect.app.classroom.model;

public class ScheduleEvent {
    private int notice_id;
    private int subject_id;
    private String subject_name;
    private String title;
    private String event_date;
    private String event_time;
    private String author_role;

    public int getNoticeId() { return notice_id; }
    public int getSubjectId() { return subject_id; }
    public String getSubjectName() { return subject_name; }
    public String getTitle() { return title; }
    public String getEventDate() { return event_date; }
    public String getEventTime() { return event_time; }
    public String getAuthorRole() { return author_role; }
}
