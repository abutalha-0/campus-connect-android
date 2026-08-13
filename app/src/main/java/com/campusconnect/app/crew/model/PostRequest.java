package com.campusconnect.app.crew.model;

import java.util.Map;

/** NEW (Crew feature): body sent to POST /api/crew/posts/. */
public class PostRequest {
    private int category;
    private String title;
    private String slug;
    private String description;
    private String location;
    private String contact_info;
    private Map<String, String> details;
    private Integer max_members;

    public PostRequest(int category, String title, String slug, String description,
                        String location, String contactInfo,
                        Map<String, String> details, Integer maxMembers) {
        this.category = category;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.location = location;
        this.contact_info = contactInfo;
        this.details = details;
        this.max_members = maxMembers;
    }
}
