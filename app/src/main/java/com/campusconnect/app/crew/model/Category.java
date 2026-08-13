package com.campusconnect.app.crew.model;

import java.util.List;

/** NEW (Crew feature): a post category (Study Partner / Contest Team / Travel Mate). */
public class Category {
    private int id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private boolean is_active;
    private DetailSchema detail_schema;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public boolean isActive() { return is_active; }
    public DetailSchema getDetailSchema() { return detail_schema; }

    public static class DetailSchema {
        private List<String> required;
        public List<String> getRequired() { return required; }
    }

    @Override
    public String toString() { return name; } // used by Spinner adapters
}
