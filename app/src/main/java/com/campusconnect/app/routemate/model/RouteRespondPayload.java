package com.campusconnect.app.routemate.model;

import com.google.gson.annotations.SerializedName;

public class RouteRespondPayload {
    @SerializedName("action")
    private String action;

    public RouteRespondPayload(String action) {
        this.action = action;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
