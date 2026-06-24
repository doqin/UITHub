package com.example.uithub.api;

import com.example.uithub.models.AnnouncementDetailResponse;
import com.example.uithub.models.AnnouncementResponse;
import com.example.uithub.models.LoginRequest;
import com.example.uithub.models.ExamModel;
import java.util.List;
import com.example.uithub.models.StudentProfile;
import com.example.uithub.models.TuitionItem;
import com.example.uithub.models.TuitionResponse;
import java.util.List;
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
    @GET("schedule/exam_schedule")
    Call<List<ExamModel>> getExamSchedule(@Header("Authorization") String authHeader);
    @GET("tuition/")
    Call<TuitionResponse> getTuition(
            @Header("Authorization") String authHeader,
            @Query("hocky") Integer hocky,
            @Query("namhoc") Integer namhoc
    );

    @GET("profile/")
    Call<StudentProfile> getProfile(@Header("Authorization") String authHeader);
}
