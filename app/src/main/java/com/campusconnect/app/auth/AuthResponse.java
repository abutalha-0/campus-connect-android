package com.campusconnect.app.auth;

public class AuthResponse {
    private User user;
    private Tokens tokens;

    public User getUser() { return user; }
    public Tokens getTokens() { return tokens; }

    public static class User {
        private int id;
        private String email;
        private String username;
        private String full_name;
        private String bio;
        private String created_at;

        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getUsername() { return username; }
        public String getFullName() { return full_name; }
        public String getBio() { return bio; }
        public String getCreatedAt() { return created_at; }
    }

    public static class Tokens {
        private String access;
        private String refresh;

        public String getAccess() { return access; }
        public String getRefresh() { return refresh; }
    }
}