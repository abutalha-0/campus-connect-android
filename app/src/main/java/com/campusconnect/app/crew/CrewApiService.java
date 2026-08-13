package com.campusconnect.app.crew;

import com.campusconnect.app.core.api.PageResponse;
import com.campusconnect.app.crew.model.Category;
import com.campusconnect.app.crew.model.JoinRequestModel;
import com.campusconnect.app.crew.model.JoinRequestRequest;
import com.campusconnect.app.crew.model.Post;
import com.campusconnect.app.crew.model.PostMember;
import com.campusconnect.app.crew.model.PostRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** NEW (Crew feature): Retrofit interface for /api/crew/..., styled like FeedApiService.java. */
public interface CrewApiService {

    @GET("api/crew/categories/")
    Call<PageResponse<Category>> getCategories(@Header("Authorization") String token);

    @GET("api/crew/posts/")
    Call<PageResponse<Post>> getPosts(
            @Header("Authorization") String token,
            @Query("category") String categorySlug,
            @Query("status") String status,
            @Query("search") String search
    );

    @POST("api/crew/posts/")
    Call<Post> createPost(@Header("Authorization") String token, @Body PostRequest body);

    @GET("api/crew/posts/{slug}/")
    Call<Post> getPost(@Header("Authorization") String token, @Path("slug") String slug);

    @POST("api/crew/posts/{slug}/close/")
    Call<Post> closePost(@Header("Authorization") String token, @Path("slug") String slug);

    @GET("api/crew/posts/{slug}/members/")
    Call<PageResponse<PostMember>> getPostMembers(@Header("Authorization") String token, @Path("slug") String slug);

    @GET("api/crew/join-requests/")
    Call<PageResponse<JoinRequestModel>> getJoinRequests(@Header("Authorization") String token, @Query("post") String postSlug);

    @POST("api/crew/join-requests/")
    Call<JoinRequestModel> createJoinRequest(@Header("Authorization") String token, @Body JoinRequestRequest body);

    @POST("api/crew/join-requests/{id}/accept/")
    Call<JoinRequestModel> acceptJoinRequest(@Header("Authorization") String token, @Path("id") int id);

    @POST("api/crew/join-requests/{id}/reject/")
    Call<JoinRequestModel> rejectJoinRequest(@Header("Authorization") String token, @Path("id") int id);
}
