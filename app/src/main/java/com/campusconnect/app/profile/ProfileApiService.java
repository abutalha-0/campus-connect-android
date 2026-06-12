package com.campusconnect.app.profile;

import com.campusconnect.app.profile.models.Profile;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ProfileApiService {

    @GET("api/profiles/me/")
    Call<Profile> getMyProfile(@Header("Authorization") String token);

    @GET("api/profiles/{user_id}/")
    Call<Profile> getPublicProfile(
            @Header("Authorization") String token,
            @Path("user_id") int userId
    );
}