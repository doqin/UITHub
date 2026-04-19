package com.example.uithub.api;

import com.example.uithub.models.AnnouncementResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface ApiService {

    @GET("announcements")
    Call<AnnouncementResponse> getAnnouncements(@Query("topic") String topic);

}