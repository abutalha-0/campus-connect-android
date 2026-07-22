package com.campusconnect.app.classroom;

import com.campusconnect.app.classroom.model.AddCourseRequest;
import com.campusconnect.app.classroom.model.Classroom;
import com.campusconnect.app.classroom.model.CreateClassRequest;
import com.campusconnect.app.classroom.model.DeleteClassRequest;
import com.campusconnect.app.classroom.model.Subject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ClassApiService {

    @GET("api/classroom/classes/lookup/")
    Call<Subject> lookup(
            @Header("Authorization") String token,
            @Query("code") String code
    );

    @POST("api/classroom/classes/")
    Call<Classroom> createClass(
            @Header("Authorization") String token,
            @Body CreateClassRequest body
    );

    @GET("api/classroom/classes/me/")
    Call<Classroom> getMyClass(@Header("Authorization") String token);

    @POST("api/classroom/classes/me/subjects/")
    Call<Subject> addCourse(
            @Header("Authorization") String token,
            @Body AddCourseRequest body
    );

    @DELETE("api/classroom/classes/me/subjects/{id}/")
    Call<Void> removeCourse(
            @Header("Authorization") String token,
            @Path("id") int subjectId
    );

    // DELETE with a body (password) requires @HTTP.
    @HTTP(method = "DELETE", path = "api/classroom/classes/me/", hasBody = true)
    Call<Void> deleteClass(
            @Header("Authorization") String token,
            @Body DeleteClassRequest body
    );
}
