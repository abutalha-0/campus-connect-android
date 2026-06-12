package com.campusconnect.app.auth.register;

public class RegisterRequest {
    private String email;
    private String username;
    private String full_name;
    private String password;

    public RegisterRequest(String email, String username, String fullName, String password) {
        this.email = email;
        this.username = username;
        this.full_name = fullName;
        this.password = password;
    }
}