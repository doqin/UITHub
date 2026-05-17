package com.example.uithub.api;

import com.example.uithub.models.AnnouncementDetailResponse;
import com.example.uithub.models.AnnouncementResponse;
import com.example.uithub.models.LoginRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")
    Call<ResponseBody> login(@Body LoginRequest request);

    @GET("schedule/")
    Call<ResponseBody> getSchedule(@Header("Authorization") String authHeader);

    @GET("announcements")
    Call<AnnouncementResponse> getAnnouncements(
            @Query("topic") String topic,
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @GET("announcements/{node_id}")
    Call<AnnouncementDetailResponse> getAnnouncementDetail(
            @Path("node_id") String nodeId
    );
}
