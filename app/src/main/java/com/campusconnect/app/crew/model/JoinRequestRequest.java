package com.campusconnect.app.crew.model;

/** NEW (Crew feature): body sent to POST /api/crew/join-requests/. */
public class JoinRequestRequest {
    private int post;
    private String message;

    public JoinRequestRequest(int post, String message) {
        this.post = post;
        this.message = message;
    }
}
