package com.example.uithub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uithub.models.Announcement;
import com.example.uithub.models.AnnouncementDetailResponse;
import com.example.uithub.repository.MainRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementDetail extends AppCompatActivity {

    private TextView titleText;
    private TextView contentText;
    private ProgressBar spinner;

    private MainRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);

        titleText = findViewById(R.id.detailTitle);
        contentText = findViewById(R.id.detailContent);
        spinner = findViewById(R.id.loadingSpinner);

        repository = new MainRepository();

        String nodeId = getIntent().getStringExtra("node_id");

        if (nodeId != null) {
            callAPI(nodeId);
        }
    }

    private void callAPI(String nodeId) {

        spinner.setVisibility(View.VISIBLE);

        repository.getAnnouncementDetail(nodeId)
                .enqueue(new Callback<AnnouncementDetailResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<AnnouncementDetailResponse> call,
                            @NonNull Response<AnnouncementDetailResponse> response
                    ) {

                        spinner.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Announcement announcement =
                                    response.body().getData();

                            titleText.setText(
                                    announcement.getTitle()
                            );

                            if (announcement.getDetails() != null) {

                                contentText.setText(
                                        announcement
                                                .getDetails()
                                                .getContent()
                                );
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<AnnouncementDetailResponse> call,
                            @NonNull Throwable t
                    ) {

                        spinner.setVisibility(View.GONE);

                        Log.e("API", t.getMessage());
                    }
                });
    }
}