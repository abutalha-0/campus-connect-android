package com.campusconnect.app.faculty.model;

import com.google.gson.annotations.SerializedName;

/** Request body for POST /api/faculty/me/links/. */
public class FacultyLinkRequest {

    @SerializedName("link_name")
    public String linkName;

    @SerializedName("icon")
    public String icon;

    @SerializedName("url")
    public String url;

    public FacultyLinkRequest(String linkName, String icon, String url) {
        this.linkName = linkName;
        this.icon = icon;
        this.url = url;
    }
}
