package com.example.uithub.api;

import com.example.uithub.models.LoginRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<ResponseBody> login(@Body LoginRequest request);

    @GET("schedule/")
    Call<ResponseBody> getSchedule(@Header("Authorization") String authHeader);
}
