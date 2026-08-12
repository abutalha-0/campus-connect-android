package com.campusconnect.app.crew.model;

import com.campusconnect.app.user.User;

/** NEW (Crew feature): a request from a student to join a Post. */
public class JoinRequestModel {
    private int id;
    private int post;
    private String post_title;
    private User requester;
    private String message;
    private String status;
    private Integer reviewed_by;
    private String reviewed_at;
    private String created_at;

    public int getId() { return id; }
    public int getPost() { return post; }
    public String getPostTitle() { return post_title; }
    public User getRequester() { return requester; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Integer getReviewedBy() { return reviewed_by; }
    public String getReviewedAt() { return reviewed_at; }
    public String getCreatedAt() { return created_at; }
}
