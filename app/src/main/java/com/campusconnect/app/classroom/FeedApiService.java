package com.campusconnect.app.classroom;

import com.campusconnect.app.classroom.model.FeedComment;
import com.campusconnect.app.classroom.model.FeedCommentRequest;
import com.campusconnect.app.classroom.model.FeedPost;
import com.campusconnect.app.classroom.model.FeedPostRequest;
import com.campusconnect.app.classroom.model.FeedVoteRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FeedApiService {

    @GET("api/classroom/feed/tag-options/")
    Call<List<String>> getTagOptions(@Header("Authorization") String token);

    @GET("api/classroom/feed/")
    Call<List<FeedPost>> getPosts(@Header("Authorization") String token);

    @POST("api/classroom/feed/")
    Call<FeedPost> addPost(
            @Header("Authorization") String token,
            @Body FeedPostRequest body
    );

    @PATCH("api/classroom/feed/{id}/")
    Call<FeedPost> updatePost(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body FeedPostRequest body
    );

    @DELETE("api/classroom/feed/{id}/")
    Call<Void> deletePost(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @POST("api/classroom/feed/{id}/vote/")
    Call<FeedPost> vote(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body FeedVoteRequest body
    );

    @GET("api/classroom/feed/{postId}/comments/")
    Call<List<FeedComment>> getComments(
            @Header("Authorization") String token,
            @Path("postId") int postId
    );

    @POST("api/classroom/feed/{postId}/comments/")
    Call<FeedComment> addComment(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Body FeedCommentRequest body
    );

    @PATCH("api/classroom/feed/{postId}/comments/{id}/")
    Call<FeedComment> updateComment(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Path("id") int id,
            @Body FeedCommentRequest body
    );

    @DELETE("api/classroom/feed/{postId}/comments/{id}/")
    Call<Void> deleteComment(
            @Header("Authorization") String token,
            @Path("postId") int postId,
            @Path("id") int id
    );
}
