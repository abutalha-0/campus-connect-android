package com.campusconnect.app.faculty.model;

import java.util.List;

public class FacultyProfile {
    private User user;
    private String full_name;
    private String employee_id;
    private String department;
    private String designation;
    private boolean is_verified;
    private String profile_photo;
    private String updated_at;
    private List<FacultyLink> links;

    public User getUser() { return user; }
    public String getFullName() { return full_name; }
    public String getEmployeeId() { return employee_id; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public boolean isVerified() { return is_verified; }
    public String getProfilePhoto() { return profile_photo; }
    public String getUpdatedAt() { return updated_at; }
    public List<FacultyLink> getLinks() { return links; }

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
}
