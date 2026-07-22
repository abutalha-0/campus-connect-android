package com.campusconnect.app.classroom;

import com.campusconnect.app.classroom.model.Subject;
import com.campusconnect.app.classroom.model.SubjectRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SubjectApiService {

    @GET("api/classroom/subjects/")
    Call<List<Subject>> getMySubjects(@Header("Authorization") String token);

    @POST("api/classroom/subjects/")
    Call<Subject> addSubject(
            @Header("Authorization") String token,
            @Body SubjectRequest body
    );
}
