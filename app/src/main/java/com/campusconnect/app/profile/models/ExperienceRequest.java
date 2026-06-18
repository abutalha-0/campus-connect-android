package com.campusconnect.app.profile.models;

import com.google.gson.annotations.SerializedName;

/**
 * ExperienceRequest
 * ──────────────────
 * Request body for POST /api/profiles/me/experience/
 * Used by AddExperienceBottomSheet.
 *
 * start_date / end_date must be full dates: "YYYY-MM-DD".
 * end_date may be null for a current/ongoing role.
 */
public class ExperienceRequest {

    @SerializedName("title")
    public String title;

    @SerializedName("organization")
    public String organization;

    @SerializedName("description")
    public String description;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("end_date")
    public String endDate; // nullable

    public ExperienceRequest(String title, String organization, String description,
                             String startDate, String endDate) {
        this.title = title;
        this.organization = organization;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}