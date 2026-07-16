package com.campusconnect.app.profile.models;

/** A catalog entry from GET /api/profiles/skills/. */
public class Skill {
    private int id;
    private String name;
    private boolean is_predefined;

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isPredefined() { return is_predefined; }
}
