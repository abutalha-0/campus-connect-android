package com.campusconnect.app.classroom;

import com.campusconnect.app.classroom.model.Resource;
import com.campusconnect.app.classroom.model.ResourceRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface ResourceApiService {

    @GET("api/classroom/subjects/{subjectId}/resources/")
    Call<List<Resource>> getResources(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId
    );

    // ── No attachment: plain JSON ────────────────────────────────────────────
    @POST("api/classroom/subjects/{subjectId}/resources/")
    Call<Resource> addResource(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Body ResourceRequest body
    );

    @PATCH("api/classroom/subjects/{subjectId}/resources/{id}/")
    Call<Resource> updateResource(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Path("id") int id,
            @Body ResourceRequest body
    );

    // ── With a file attachment: multipart ────────────────────────────────────
    @Multipart
    @POST("api/classroom/subjects/{subjectId}/resources/")
    Call<Resource> addResourceMultipart(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part file
    );

    @Multipart
    @PATCH("api/classroom/subjects/{subjectId}/resources/{id}/")
    Call<Resource> updateResourceMultipart(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Path("id") int id,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part file
    );

    @DELETE("api/classroom/subjects/{subjectId}/resources/{id}/")
    Call<Void> deleteResource(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Path("id") int id
    );
}
