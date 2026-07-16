package com.campusconnect.app.profile.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for POST /api/profiles/me/links/ (not live on the backend
 * yet — field names mirror Link's own fields, adjust once the endpoint ships).
 */
public class LinkRequest {

    @SerializedName("link_name")
    public String linkName;

    @SerializedName("icon")
    public String icon;

    @SerializedName("url")
    public String url;

    public LinkRequest(String linkName, String icon, String url) {
        this.linkName = linkName;
        this.icon = icon;
        this.url = url;
    }
}
