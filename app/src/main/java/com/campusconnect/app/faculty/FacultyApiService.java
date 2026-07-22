package com.campusconnect.app.faculty;

import com.campusconnect.app.auth.AuthResponse;
import com.campusconnect.app.faculty.auth.FacultyRegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FacultyApiService {

    @POST("api/faculty/register/")
    Call<AuthResponse> register(@Body FacultyRegisterRequest request);
}
