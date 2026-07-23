package com.campusconnect.app.user;

public class User {
    private int id;
    private String email;
    private String username;
    private String full_name;
    private String role;
    private String bio;
    private String created_at;

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFullName() { return full_name; }
    public String getRole() { return role; }
    public String getBio() { return bio; }
    public String getCreatedAt() { return created_at; }
}