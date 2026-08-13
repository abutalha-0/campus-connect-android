package com.campusconnect.app.user;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface UserApiService {

    @GET("api/auth/users/")
    Call<UserListResponse> getUsers(@Header("Authorization") String token);

    @GET("api/auth/users/")
    Call<UserListResponse> searchUsers(@Header("Authorization") String token,
                                        @Query("search") String search);

    /** Follows the "next" URL from a previous {@link UserListResponse} to page
     *  through the full (possibly search-filtered) result set. */
    @GET
    Call<UserListResponse> getPage(@Header("Authorization") String token, @Url String url);

    @GET("api/auth/users/{id}/")
    Call<User> getUser(@Header("Authorization") String token, @Path("id") int id);
}