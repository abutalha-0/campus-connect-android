package com.campusconnect.app.profile.models;

import com.google.gson.annotations.SerializedName;

/**
 * ProfileUpdateRequest
 * ─────────────────────
 * Request body for PATCH /api/profiles/me/
 * Used by EditBasicInfoBottomSheet to update bio, about, and user_type.
 *
 * The @SerializedName values MUST match the API's JSON keys exactly
 * (see campus_connect_api_docs.md section 2.2).
 */
public class ProfileUpdateRequest {

    @SerializedName("bio")
    public String bio;

    @SerializedName("about")
    public String about;

    @SerializedName("user_type")
    public String userType;

    public ProfileUpdateRequest(String bio, String about, String userType) {
        this.bio = bio;
        this.about = about;
        this.userType = userType;
    }
}