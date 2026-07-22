package com.campusconnect.app.classroom.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateClassRequest {

    @SerializedName("subject_codes")
    public List<String> subjectCodes;

    public CreateClassRequest(List<String> subjectCodes) {
        this.subjectCodes = subjectCodes;
    }
}
