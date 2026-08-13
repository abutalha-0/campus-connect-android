package com.campusconnect.app.crew.model;

import com.campusconnect.app.user.User;
import java.util.List;
import java.util.Map;

/**
 * NEW (Crew feature): mirrors crew.Post's PostSerializer output. Reuses the
 * existing User model for "author" instead of duplicating it.
 */
public class Post {
    private int id;
    private User author;
    private int category;
    private Category category_detail;
    private String title;
    private String slug;
    private String status;
    private String description;
    private String location;
    private String contact_info;
    private Map<String, String> details; // category-specific fields, see docs
    private List<String> skills;
    private Integer max_members;
    private int accepted_members_count;
    private boolean is_full;
    private String deadline;
    private boolean is_featured;
    private String created_at;
    private String updated_at;

    public int getId() { return id; }
    public User getAuthor() { return author; }
    public int getCategory() { return category; }
    public Category getCategoryDetail() { return category_detail; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getContactInfo() { return contact_info; }
    public Map<String, String> getDetails() { return details; }
    public List<String> getSkills() { return skills; }
    public Integer getMaxMembers() { return max_members; }
    public int getAcceptedMembersCount() { return accepted_members_count; }
    public boolean isFull() { return is_full; }
    public String getDeadline() { return deadline; }
    public boolean isFeatured() { return is_featured; }
    public String getCreatedAt() { return created_at; }
    public String getUpdatedAt() { return updated_at; }
}
