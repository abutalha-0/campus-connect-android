package com.campusconnect.app.profile;

import com.campusconnect.app.profile.models.Profile;
import retrofit2.Call;
import retrofit2.http.DELETE;
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

    @DELETE("api/profiles/me/projects/{id}/")
    Call<Void> deleteProject(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @DELETE("api/profiles/me/education/{id}/")
    Call<Void> deleteEducation(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @DELETE("api/profiles/me/experience/{id}/")
    Call<Void> deleteExperience(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @DELETE("api/profiles/me/skills/{id}/")
    Call<Void> deleteSkill(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @DELETE("api/profiles/me/links/{id}/")
    Call<Void> deleteLink(
            @Header("Authorization") String token,
            @Path("id") int id
    );
}