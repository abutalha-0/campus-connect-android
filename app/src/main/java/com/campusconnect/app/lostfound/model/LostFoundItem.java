package com.campusconnect.app.lostfound.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class LostFoundItem implements Serializable {

    public static final String TYPE_LOST = "LOST";
    public static final String TYPE_FOUND = "FOUND";

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLAIMED = "CLAIMED";
    public static final String STATUS_CLOSED = "CLOSED";

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("item_type")
    private String itemType;

    @SerializedName("category")
    private String category;

    @SerializedName("location")
    private String location;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("contact_info")
    private String contactInfo;

    @SerializedName("event_date")
    private String eventDate;

    @SerializedName("status")
    private String status;

    @SerializedName("reported_by")
    private String reportedBy;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("resolved_at")
    private String resolvedAt;

    @SerializedName("claim_questions")
    private List<ClaimQuestion> claimQuestions;

    @SerializedName("claim_attempts")
    private List<ClaimAttempt> claimAttempts;

    public LostFoundItem() {
    }

    public LostFoundItem(int id, String title, String description, String itemType,
                          String category, String location, String imageUrl,
                          String contactInfo, String eventDate, String status,
                          String reportedBy, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.itemType = itemType;
        this.category = category;
        this.location = location;
        this.imageUrl = imageUrl;
        this.contactInfo = contactInfo;
        this.eventDate = eventDate;
        this.status = status;
        this.reportedBy = reportedBy;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemType() {
        return itemType != null ? itemType : TYPE_LOST;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    // Alias getter for backward compatibility
    public String getDateSeen() {
        return eventDate;
    }

    public void setDateSeen(String dateSeen) {
        this.eventDate = dateSeen;
    }

    public String getStatus() {
        return status != null ? status : STATUS_OPEN;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public List<ClaimQuestion> getClaimQuestions() {
        return claimQuestions;
    }

    public void setClaimQuestions(List<ClaimQuestion> claimQuestions) {
        this.claimQuestions = claimQuestions;
    }

    public List<ClaimAttempt> getClaimAttempts() {
        return claimAttempts;
    }

    public void setClaimAttempts(List<ClaimAttempt> claimAttempts) {
        this.claimAttempts = claimAttempts;
    }

    public boolean isLost() {
        return TYPE_LOST.equalsIgnoreCase(itemType);
    }

    public boolean isClosed() {
        return STATUS_CLOSED.equalsIgnoreCase(status);
    }
}

