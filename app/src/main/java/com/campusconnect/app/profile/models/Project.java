package com.campusconnect.app.profile.models;

import java.util.List;

public class Project {
    private int id;
    private String name;
    private String description;
    private String associated_with;
    private String created_at;
    private List<ProjectImage> images;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAssociatedWith() { return associated_with; }
    public String getCreatedAt() { return created_at; }
    public List<ProjectImage> getImages() { return images; }

    public static class ProjectImage {
        private int id;
        private String image_url;
        private boolean is_cover;
        private int position;

        public int getId() { return id; }
        public String getImageUrl() { return image_url; }
        public boolean isCover() { return is_cover; }
        public int getPosition() { return position; }
    }
}