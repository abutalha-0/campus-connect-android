package com.campusconnect.app.routemate.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Route implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("owner")
    private int owner;

    @SerializedName("owner_username")
    private String ownerUsername;

    @SerializedName("owner_full_name")
    private String ownerFullName;

    @SerializedName("home_area")
    private String homeArea;

    @SerializedName("destination")
    private String destination;

    @SerializedName("departure_time_start")
    private String departureTimeStart;

    @SerializedName("departure_time_end")
    private String departureTimeEnd;

    @SerializedName("days_active")
    private String daysActive;

    @SerializedName("transport_mode")
    private String transportMode;

    @SerializedName("note")
    private String note;

    @SerializedName("gender_preference")
    private String genderPreference;

    @SerializedName("status")
    private String status;

    @SerializedName("contact_info")
    private String contactInfo;

    @SerializedName("user_request_status")
    private String userRequestStatus;

    @SerializedName("accepted_members_count")
    private int acceptedMembersCount;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public Route() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwner() { return owner; }
    public void setOwner(int owner) { this.owner = owner; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public String getOwnerFullName() { return ownerFullName; }
    public void setOwnerFullName(String ownerFullName) { this.ownerFullName = ownerFullName; }

    public String getHomeArea() { return homeArea; }
    public void setHomeArea(String homeArea) { this.homeArea = homeArea; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDepartureTimeStart() { return departureTimeStart; }
    public void setDepartureTimeStart(String departureTimeStart) { this.departureTimeStart = departureTimeStart; }

    public String getDepartureTimeEnd() { return departureTimeEnd; }
    public void setDepartureTimeEnd(String departureTimeEnd) { this.departureTimeEnd = departureTimeEnd; }

    public String getDaysActive() { return daysActive; }
    public void setDaysActive(String daysActive) { this.daysActive = daysActive; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getGenderPreference() { return genderPreference; }
    public void setGenderPreference(String genderPreference) { this.genderPreference = genderPreference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getUserRequestStatus() { return userRequestStatus; }
    public void setUserRequestStatus(String userRequestStatus) { this.userRequestStatus = userRequestStatus; }

    public int getAcceptedMembersCount() { return acceptedMembersCount; }
    public void setAcceptedMembersCount(int acceptedMembersCount) { this.acceptedMembersCount = acceptedMembersCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
