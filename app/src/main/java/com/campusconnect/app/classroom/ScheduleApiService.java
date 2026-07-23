package com.campusconnect.app.classroom;

import com.campusconnect.app.classroom.model.ScheduleEvent;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ScheduleApiService {

    @GET("api/classroom/schedule/")
    Call<List<ScheduleEvent>> getSchedule(@Header("Authorization") String token);
}
