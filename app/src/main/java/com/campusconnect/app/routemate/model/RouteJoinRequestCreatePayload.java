package com.campusconnect.app.routemate.model;

import com.google.gson.annotations.SerializedName;

public class RouteJoinRequestCreatePayload {
    @SerializedName("note")
    private String note;

    @SerializedName("requester_contact_info")
    private String requesterContactInfo;

    public RouteJoinRequestCreatePayload(String note, String requesterContactInfo) {
        this.note = note;
        this.requesterContactInfo = requesterContactInfo;
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getRequesterContactInfo() { return requesterContactInfo; }
    public void setRequesterContactInfo(String requesterContactInfo) { this.requesterContactInfo = requesterContactInfo; }
}
