package com.campusconnect.app.routemate.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RouteJoinRequest implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("route")
    private int route;

    @SerializedName("route_home_area")
    private String routeHomeArea;

    @SerializedName("route_destination")
    private String routeDestination;

    @SerializedName("route_owner_username")
    private String routeOwnerUsername;

    @SerializedName("requester")
    private int requester;

    @SerializedName("requester_username")
    private String requesterUsername;

    @SerializedName("requester_full_name")
    private String requesterFullName;

    @SerializedName("status")
    private String status;

    @SerializedName("note")
    private String note;

    @SerializedName("requester_contact_info")
    private String requesterContactInfo;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public RouteJoinRequest() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoute() { return route; }
    public void setRoute(int route) { this.route = route; }

    public String getRouteHomeArea() { return routeHomeArea; }
    public void setRouteHomeArea(String routeHomeArea) { this.routeHomeArea = routeHomeArea; }

    public String getRouteDestination() { return routeDestination; }
    public void setRouteDestination(String routeDestination) { this.routeDestination = routeDestination; }

    public String getRouteOwnerUsername() { return routeOwnerUsername; }
    public void setRouteOwnerUsername(String routeOwnerUsername) { this.routeOwnerUsername = routeOwnerUsername; }

    public int getRequester() { return requester; }
    public void setRequester(int requester) { this.requester = requester; }

    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }

    public String getRequesterFullName() { return requesterFullName; }
    public void setRequesterFullName(String requesterFullName) { this.requesterFullName = requesterFullName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getRequesterContactInfo() { return requesterContactInfo; }
    public void setRequesterContactInfo(String requesterContactInfo) { this.requesterContactInfo = requesterContactInfo; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
