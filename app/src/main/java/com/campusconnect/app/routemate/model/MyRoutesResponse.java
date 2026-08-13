package com.campusconnect.app.routemate.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MyRoutesResponse {
    @SerializedName("posted_routes")
    private List<Route> postedRoutes;

    @SerializedName("joined_routes")
    private List<RouteJoinRequest> joinedRoutes;

    public List<Route> getPostedRoutes() { return postedRoutes; }
    public void setPostedRoutes(List<Route> postedRoutes) { this.postedRoutes = postedRoutes; }

    public List<RouteJoinRequest> getJoinedRoutes() { return joinedRoutes; }
    public void setJoinedRoutes(List<RouteJoinRequest> joinedRoutes) { this.joinedRoutes = joinedRoutes; }
}
