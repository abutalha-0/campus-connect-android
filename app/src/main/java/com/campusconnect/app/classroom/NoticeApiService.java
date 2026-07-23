package com.campusconnect.app.classroom;

import com.campusconnect.app.classroom.model.Notice;

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

public interface NoticeApiService {

    @GET("api/classroom/subjects/{subjectId}/notices/")
    Call<List<Notice>> getNotices(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId
    );

    // A pre-built JSON RequestBody (not a typed model) so event_date/event_time
    // can be sent as explicit null to clear them — Gson omits null fields by
    // default when serializing a typed object.
    @POST("api/classroom/subjects/{subjectId}/notices/")
    Call<Notice> addNotice(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Body RequestBody body
    );

    @PATCH("api/classroom/subjects/{subjectId}/notices/{id}/")
    Call<Notice> updateNotice(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Path("id") int id,
            @Body RequestBody body
    );

    @Multipart
    @POST("api/classroom/subjects/{subjectId}/notices/")
    Call<Notice> addNoticeMultipart(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part file
    );

    @Multipart
    @PATCH("api/classroom/subjects/{subjectId}/notices/{id}/")
    Call<Notice> updateNoticeMultipart(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Path("id") int id,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part file
    );

    @DELETE("api/classroom/subjects/{subjectId}/notices/{id}/")
    Call<Void> deleteNotice(
            @Header("Authorization") String token,
            @Path("subjectId") int subjectId,
            @Path("id") int id
    );
}
