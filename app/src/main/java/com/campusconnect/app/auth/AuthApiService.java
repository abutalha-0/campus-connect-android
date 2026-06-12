package com.campusconnect.app.auth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import com.campusconnect.app.auth.login.LoginRequest;
import com.campusconnect.app.auth.register.RegisterRequest;

public interface AuthApiService {

    @POST("api/auth/login/")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register/")
    Call<AuthResponse> register(@Body RegisterRequest request);
}