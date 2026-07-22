package com.campusconnect.app.classroom;

import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.classroom.model.SubjectRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SubjectApiService {

    @GET("api/classroom/subjects/")
    Call<List<Subject>> getMySubjects(@Header("Authorization") String token);

    @POST("api/classroom/subjects/")
    Call<Subject> addSubject(
            @Header("Authorization") String token,
            @Body SubjectRequest body
    );

    @GET("api/classroom/subjects/{id}/")
    Call<Subject> getSubject(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @PATCH("api/classroom/subjects/{id}/")
    Call<Subject> updateSubject(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body SubjectRequest body
    );

    @DELETE("api/classroom/subjects/{id}/")
    Call<Void> deleteSubject(
            @Header("Authorization") String token,
            @Path("id") int id
    );
}
