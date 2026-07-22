package com.campusconnect.app.classroom.model;

import com.google.gson.annotations.SerializedName;

/** JSON body for creating/updating a notice with no attachment. */
public class NoticeRequest {

    @SerializedName("text")
    public String text;

    @SerializedName("highlight")
    public String highlight;

    public NoticeRequest(String text, String highlight) {
        this.text = text;
        this.highlight = highlight;
    }
}
