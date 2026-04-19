package com.example.uithub.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


//call api nhanh hon
public class RetrofitClient {

    private static final String BASE_URL = "https://uit-tkb-scraper.onrender.com/";
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
