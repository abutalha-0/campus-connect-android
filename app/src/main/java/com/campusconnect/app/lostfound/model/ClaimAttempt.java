package com.campusconnect.app.lostfound.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ClaimAttempt implements Serializable {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @SerializedName("id")
    private int id;

    @SerializedName("item")
    private int item;

    @SerializedName("claimant")
    private int claimantId;

    @SerializedName("claimant_username")
    private String claimantUsername;

    @SerializedName("status")
    private String status;

    @SerializedName("submitted_at")
    private String submittedAt;

    @SerializedName("reviewed_at")
    private String reviewedAt;

    @SerializedName("answers")
    private List<ClaimAnswer> answers;

    public ClaimAttempt() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public int getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(int claimantId) {
        this.claimantId = claimantId;
    }

    public String getClaimantUsername() {
        return claimantUsername;
    }

    public void setClaimantUsername(String claimantUsername) {
        this.claimantUsername = claimantUsername;
    }

    public String getStatus() {
        return status != null ? status : STATUS_PENDING;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public List<ClaimAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ClaimAnswer> answers) {
        this.answers = answers;
    }
}
