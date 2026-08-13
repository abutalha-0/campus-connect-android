package com.campusconnect.app.notifications;

import com.campusconnect.app.notifications.model.Notification;
import com.campusconnect.app.notifications.model.UnreadCountResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** NEW (Notifications feature): Retrofit interface for /api/notifications/... */
public interface NotificationApiService {

    @GET("api/notifications/")
    Call<List<Notification>> getNotifications(@Header("Authorization") String token, @Query("unread") Boolean unreadOnly);

    @GET("api/notifications/unread-count/")
    Call<UnreadCountResponse> getUnreadCount(@Header("Authorization") String token);

    @POST("api/notifications/{id}/read/")
    Call<Notification> markRead(@Header("Authorization") String token, @Path("id") int id);
}
