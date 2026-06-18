package com.campusconnect.app.profile.models;

import com.google.gson.annotations.SerializedName;

/**
 * ProjectRequest
 * ───────────────
 * Request body for POST /api/profiles/me/projects/
 * Used by AddProjectBottomSheet.
 *
 * Note: the API does NOT accept a "url" field for projects.
 * Only name, description, and associated_with are valid.
 */
public class ProjectRequest {

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("associated_with")
    public String associatedWith;

    public ProjectRequest(String name, String associatedWith, String description) {
        this.name = name;
        this.associatedWith = associatedWith;
        this.description = description;
    }
}