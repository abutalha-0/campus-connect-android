package com.campusconnect.app.profile;

import com.campusconnect.app.profile.models.Education;
import com.campusconnect.app.profile.models.Experience;
import com.campusconnect.app.profile.models.ExperienceRequest;
import com.campusconnect.app.profile.models.Profile;
import com.campusconnect.app.profile.models.ProfileUpdateRequest;
import com.campusconnect.app.profile.models.Project;
import com.campusconnect.app.profile.models.ProjectRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;

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

    @POST("api/profiles/me/projects/")
    Call<Project> addProject(
            @Header("Authorization") String token,
            @Body ProjectRequest body
    );

    @POST("api/profiles/me/experience/")
    Call<Experience> addExperience(
            @Header("Authorization") String token,
            @Body ExperienceRequest body
    );

    @Multipart
    @POST("api/profiles/me/education/")
    Call<Education> addEducation(
            @Header("Authorization") String token,
            @Part("institution_name") RequestBody institutionName,
            @Part("degree") RequestBody degree,
            @Part("start_year") RequestBody startYear,
            @Part("end_year") RequestBody endYear
    );

    @PATCH("api/profiles/me/")
    Call<Profile> updateProfile(
            @Header("Authorization") String token,
            @Body ProfileUpdateRequest body
    );


    // ── 2.2 Edit My Profile — photo only (multipart) ────────────────────────
    // Separate from updateProfile() because the API requires
    // multipart/form-data specifically when uploading a file.
    @Multipart
    @PATCH("api/profiles/me/")
    Call<Profile> updateProfilePhoto(
            @Header("Authorization") String token,
            @Part MultipartBody.Part profilePhoto
    );



}