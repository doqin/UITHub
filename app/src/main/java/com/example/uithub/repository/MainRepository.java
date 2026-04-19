package com.example.uithub.repository;

import com.example.uithub.api.ApiService;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.AnnouncementResponse;

import retrofit2.Call;


//de xai api call o ngoai activity
public class MainRepository {

    private ApiService apiService;

    public MainRepository() {
        apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);
    }

    public Call<AnnouncementResponse> getAnnouncements(String topic) {
        return apiService.getAnnouncements(topic);
    }
}