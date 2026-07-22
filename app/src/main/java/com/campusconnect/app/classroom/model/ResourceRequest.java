package com.campusconnect.app.classroom.model;

import com.google.gson.annotations.SerializedName;

/** JSON body for creating/updating a resource that has no file upload. */
public class ResourceRequest {

    @SerializedName("title")
    public String title;

    @SerializedName("resource_type")
    public String resourceType;

    @SerializedName("description")
    public String description;

    @SerializedName("file_url")
    public String fileUrl;

    public ResourceRequest(String title, String resourceType,
                           String description, String fileUrl) {
        this.title = title;
        this.resourceType = resourceType;
        this.description = description;
        this.fileUrl = fileUrl;
    }
}
