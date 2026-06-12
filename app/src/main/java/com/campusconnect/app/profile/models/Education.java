package com.campusconnect.app.profile.models;

public class Education {
    private int id;
    private String institution_name;
    private String degree;
    private int start_year;
    private Integer end_year;
    private String image_url;

    public int getId() { return id; }
    public String getInstitutionName() { return institution_name; }
    public String getDegree() { return degree; }
    public int getStartYear() { return start_year; }
    public Integer getEndYear() { return end_year; }
    public String getImageUrl() { return image_url; }
}