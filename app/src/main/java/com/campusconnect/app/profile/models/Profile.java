package com.campusconnect.app.profile.models;

import java.util.List;

public class Profile {
    private ProfileUser user;
    private String bio;
    private String about;
    private String dob;
    private String gender;
    private String user_type;
    private String profile_photo;
    private String updated_at;
    private List<LookingFor> looking_for;
    private List<Link> links;
    private List<Education> education;
    private List<Experience> experience;
    private List<Project> projects;
    private List<UserSkill> skills;

    public ProfileUser getUser() { return user; }
    public String getBio() { return bio; }
    public String getAbout() { return about; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getUserType() { return user_type; }
    public String getProfilePhoto() { return profile_photo; }
    public String getUpdatedAt() { return updated_at; }
    public List<LookingFor> getLookingFor() { return looking_for; }
    public List<Link> getLinks() { return links; }
    public List<Education> getEducation() { return education; }
    public List<Experience> getExperience() { return experience; }
    public List<Project> getProjects() { return projects; }
    public List<UserSkill> getSkills() { return skills; }

    public static class ProfileUser {
        private int id;
        private String username;
        private String full_name;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return full_name; }
    }
}