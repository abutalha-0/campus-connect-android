package com.campusconnect.app.faculty.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for PATCH /api/faculty/me/ (text fields only). Email and
 * employee_id are read-only server-side, so they are not included here.
 */
public class FacultyProfileUpdateRequest {

    @SerializedName("full_name")
    public String fullName;

    @SerializedName("department")
    public String department;

    @SerializedName("designation")
    public String designation;

    public FacultyProfileUpdateRequest(String fullName, String department, String designation) {
        this.fullName = fullName;
        this.department = department;
        this.designation = designation;
    }
}
