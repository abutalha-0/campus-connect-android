package com.campusconnect.app.user;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface UserApiService {

    @GET("api/auth/users/")
    Call<UserListResponse> getUsers(@Header("Authorization") String token);

    @GET("api/auth/users/{id}/")
    Call<User> getUser(@Header("Authorization") String token, @Path("id") int id);
}