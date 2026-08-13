package com.campusconnect.app.routemate.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RouteHistoryResponse {
    @SerializedName("closed_posted_routes")
    private List<Route> closedPostedRoutes;

    @SerializedName("accepted_matches")
    private List<RouteJoinRequest> acceptedMatches;

    public List<Route> getClosedPostedRoutes() { return closedPostedRoutes; }
    public void setClosedPostedRoutes(List<Route> closedPostedRoutes) { this.closedPostedRoutes = closedPostedRoutes; }

    public List<RouteJoinRequest> getAcceptedMatches() { return acceptedMatches; }
    public void setAcceptedMatches(List<RouteJoinRequest> acceptedMatches) { this.acceptedMatches = acceptedMatches; }
}
