package com.campusconnect.app.user;

public class User {
    private int id;
    private String email;
    private String username;
    private String full_name;
    private String role;
    private String bio;
    private String created_at;

    // Only populated by the Discover list endpoint (DiscoverUserSerializer);
    // null everywhere else this model is reused (crew author/requester, etc).
    private String profile_photo;
    private String user_type;
    private String designation;
    private String department;

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFullName() { return full_name; }
    public String getRole() { return role; }
    public String getBio() { return bio; }
    public String getCreatedAt() { return created_at; }
    public String getProfilePhoto() { return profile_photo; }
    public String getUserType() { return user_type; }
    public String getDesignation() { return designation; }
    public String getDepartment() { return department; }
}