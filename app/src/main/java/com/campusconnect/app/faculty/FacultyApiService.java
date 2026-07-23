package com.campusconnect.app.faculty;

import com.campusconnect.app.auth.AuthResponse;
import com.campusconnect.app.faculty.auth.FacultyRegisterRequest;
import com.campusconnect.app.faculty.model.FacultyLink;
import com.campusconnect.app.faculty.model.FacultyLinkRequest;
import com.campusconnect.app.faculty.model.FacultyProfile;
import com.campusconnect.app.faculty.model.FacultyProfileUpdateRequest;
import com.campusconnect.app.faculty.model.FacultyPublicProfile;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface FacultyApiService {

    @POST("api/faculty/register/")
    Call<AuthResponse> register(@Body FacultyRegisterRequest request);

    @GET("api/faculty/me/")
    Call<FacultyProfile> getMyProfile(@Header("Authorization") String token);

    @PATCH("api/faculty/me/")
    Call<FacultyProfile> updateProfile(
            @Header("Authorization") String token,
            @Body FacultyProfileUpdateRequest body
    );

    @Multipart
    @PATCH("api/faculty/me/")
    Call<FacultyProfile> updateProfilePhoto(
            @Header("Authorization") String token,
            @Part MultipartBody.Part profilePhoto
    );

    @POST("api/faculty/me/links/")
    Call<FacultyLink> addLink(
            @Header("Authorization") String token,
            @Body FacultyLinkRequest body
    );

    @DELETE("api/faculty/me/links/{id}/")
    Call<Void> deleteLink(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @GET("api/faculty/{userId}/")
    Call<FacultyPublicProfile> getPublicProfile(
            @Header("Authorization") String token,
            @Path("userId") int userId
    );
}
