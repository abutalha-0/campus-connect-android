package com.campusconnect.app.lostfound.api;

import com.campusconnect.app.lostfound.model.LostFoundItem;
import java.util.List;

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
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LostFoundApiService {

    @GET("api/lost-found/")
    Call<List<LostFoundItem>> getItems(
            @Header("Authorization") String token,
            @Query("item_type") String itemType,
            @Query("status") String status,
            @Query("category") String category,
            @Query("location") String location,
            @Query("date") String date,
            @Query("search") String search
    );

    @GET("api/lost-found/{id}/")
    Call<LostFoundItem> getItemDetail(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @Multipart
    @POST("api/lost-found/")
    Call<LostFoundItem> createItemWithImage(
            @Header("Authorization") String token,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("item_type") RequestBody itemType,
            @Part("category") RequestBody category,
            @Part("location") RequestBody location,
            @Part("date_seen") RequestBody dateSeen,
            @Part("contact_info") RequestBody contactInfo,
            @Part MultipartBody.Part image
    );

    @POST("api/lost-found/")
    Call<LostFoundItem> createItem(
            @Header("Authorization") String token,
            @Body LostFoundItem item
    );

    @PATCH("api/lost-found/{id}/")
    Call<LostFoundItem> updateItem(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body LostFoundItem item
    );

    @Multipart
    @PATCH("api/lost-found/{id}/")
    Call<LostFoundItem> updateItemWithImage(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("item_type") RequestBody itemType,
            @Part("category") RequestBody category,
            @Part("location") RequestBody location,
            @Part("date_seen") RequestBody dateSeen,
            @Part("contact_info") RequestBody contactInfo,
            @Part("status") RequestBody status,
            @Part MultipartBody.Part image
    );

    @DELETE("api/lost-found/{id}/")
    Call<Void> deleteItem(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @GET("api/lost-found/my-posts/")
    Call<List<LostFoundItem>> getMyPosts(
            @Header("Authorization") String token
    );
}
