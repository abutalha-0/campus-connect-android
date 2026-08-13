package com.campusconnect.app.crew.model;

import com.campusconnect.app.user.User;

/** NEW (Crew feature): an accepted member of a Post. */
public class PostMember {
    private int id;
    private int post;
    private User user;
    private String joined_at;

    public int getId() { return id; }
    public int getPost() { return post; }
    public User getUser() { return user; }
    public String getJoinedAt() { return joined_at; }
}
