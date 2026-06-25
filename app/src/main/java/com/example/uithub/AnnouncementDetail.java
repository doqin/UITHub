package com.example.uithub;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uithub.models.Announcement;
import com.example.uithub.models.AnnouncementDetailResponse;
import com.example.uithub.repository.MainRepository;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementDetail extends BaseActivity {

    private TextView titleText;
    private TextView contentText;
    private MaterialButton openArticleButton;
    private ProgressBar spinner;

    private MainRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);

        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.thong_bao));
        }
        if (toolbar.getNavigationIcon() != null) {
            DrawableCompat.setTint(toolbar.getNavigationIcon(), getThemeColor(com.google.android.material.R.attr.colorOnSurface));
        }
        toolbar.setTitleTextColor(getThemeColor(com.google.android.material.R.attr.colorOnSurface));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        View root = findViewById(R.id.detailRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        titleText = findViewById(R.id.detailTitle);
        contentText = findViewById(R.id.detailContent);
        openArticleButton = findViewById(R.id.openArticleButton);
        spinner = findViewById(R.id.loadingSpinner);
        spinner.bringToFront();

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

                        if (response.isSuccessful() && response.body() != null) {
                            Announcement announcement = response.body().getData();

                            Log.d("API", "details: " + announcement.getDetails());
                            Log.d("API", "content: " + (announcement.getDetails() != null
                                    ? announcement.getDetails().getContent() : "null"));

                            titleText.setText(announcement.getTitle());
                            bindArticleLink(announcement.getLink());

                            if (announcement.getDetails() != null) {
                                contentText.setText(announcement.getDetails().getContent());
                            }

                            LinearLayout relatedContainer = findViewById(R.id.relatedContainer);
                            relatedContainer.removeAllViews();

                            if (announcement.getDetails() != null
                                    && announcement.getDetails().getRelated() != null
                                    && !announcement.getDetails().getRelated().isEmpty()) {

                                for (Announcement.Related rel : announcement.getDetails().getRelated()) {
                                    TextView linkView = new TextView(AnnouncementDetail.this);
                                    linkView.setLayoutParams(new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    ));
                                    linkView.setText(rel.getTitle());
                                    linkView.setTextColor(getThemeColor(androidx.appcompat.R.attr.colorPrimary));
                                    linkView.setTextSize(15f);
                                    linkView.setPadding(16, 12, 16, 12);
                                    linkView.setClickable(true);
                                    linkView.setFocusable(true);
                                    linkView.setBackgroundResource(android.R.drawable.list_selector_background);
                                    linkView.setBackgroundTintList(ColorStateList.valueOf(getThemeColor(com.google.android.material.R.attr.colorSurfaceContainerHighest)));

                                    linkView.setOnClickListener(v -> {
                                        if (rel.getLink() != null) {
                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(rel.getLink()));
                                            startActivity(i);
                                        }
                                    });

                                    relatedContainer.addView(linkView);
                                }
                            } else {
                                findViewById(R.id.relatedSection).setVisibility(View.GONE);
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

    private void bindArticleLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            openArticleButton.setVisibility(View.GONE);
            openArticleButton.setOnClickListener(null);
            return;
        }

        openArticleButton.setVisibility(View.VISIBLE);
        openArticleButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(intent);
        });
    }

    private int getThemeColor(int attr) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }
}
