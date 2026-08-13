package com.campusconnect.app.routemate.api;

import com.campusconnect.app.routemate.model.MyRoutesResponse;
import com.campusconnect.app.routemate.model.Route;
import com.campusconnect.app.routemate.model.RouteHistoryResponse;
import com.campusconnect.app.routemate.model.RouteJoinRequest;
import com.campusconnect.app.routemate.model.RouteJoinRequestCreatePayload;
import com.campusconnect.app.routemate.model.RouteRespondPayload;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RouteMateApiService {

    @GET("api/route-mate/routes/")
    Call<List<Route>> getRoutes(
            @Header("Authorization") String token,
            @Query("home_area") String homeArea,
            @Query("destination") String destination,
            @Query("gender_preference") String genderPreference,
            @Query("days") String days,
            @Query("status") String status,
            @Query("search") String search
    );

    @POST("api/route-mate/routes/")
    Call<Route> createRoute(
            @Header("Authorization") String token,
            @Body Route route
    );

    @GET("api/route-mate/routes/{id}/")
    Call<Route> getRouteDetail(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @PATCH("api/route-mate/routes/{id}/")
    Call<Route> updateRoute(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body Route route
    );

    @DELETE("api/route-mate/routes/{id}/")
    Call<Void> deleteRoute(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @POST("api/route-mate/routes/{id}/request/")
    Call<RouteJoinRequest> createJoinRequest(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body RouteJoinRequestCreatePayload payload
    );

    @GET("api/route-mate/routes/{id}/requests/")
    Call<List<RouteJoinRequest>> getRouteJoinRequests(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @POST("api/route-mate/requests/{id}/respond/")
    Call<RouteJoinRequest> respondJoinRequest(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body RouteRespondPayload payload
    );

    @GET("api/route-mate/my-routes/")
    Call<MyRoutesResponse> getMyRoutes(
            @Header("Authorization") String token
    );

    @GET("api/route-mate/history/")
    Call<RouteHistoryResponse> getRouteHistory(
            @Header("Authorization") String token
    );
}
