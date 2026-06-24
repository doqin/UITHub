package com.example.uithub.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://uit-tkb-scraper.vercel.app/";
    private static final int CONNECT_TIMEOUT_SECONDS = 30;
    private static final int READ_TIMEOUT_SECONDS = 60;
    private static final int WRITE_TIMEOUT_SECONDS = 30;
    private static final int CALL_TIMEOUT_SECONDS = 75;

    private static Retrofit retrofit = null;

    public interface SessionListener {
        void onSessionExpired();
    }

    private static SessionListener sessionListener;

    public static void setSessionListener(SessionListener listener) {
        sessionListener = listener;
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(chain -> {
                        okhttp3.Response response = chain.proceed(chain.request());
                        if (response.code() == 401) {
                            if (sessionListener != null) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                    if (sessionListener != null) {
                                        sessionListener.onSessionExpired();
                                    }
                                });
                            }
                        }
                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
