package com.campusconnect.app.faculty.model;

import java.util.List;

public class FacultyPublicProfile {
    private User user;
    private String department;
    private String designation;
    private String profile_photo;
    private List<FacultyLink> links;
    private List<SubjectSummary> subjects;

    public User getUser() { return user; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public String getProfilePhoto() { return profile_photo; }
    public List<FacultyLink> getLinks() { return links; }
    public List<SubjectSummary> getSubjects() { return subjects; }

    public static class User {
        private int id;
        private String username;
        private String full_name;
        private String email;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return full_name; }
        public String getEmail() { return email; }
    }

    /** Deliberately minimal — no share code, this is public data. */
    public static class SubjectSummary {
        private int id;
        private String name;
        private String intake;
        private String section;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getIntake() { return intake; }
        public String getSection() { return section; }
    }
}
