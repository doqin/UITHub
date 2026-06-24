package com.example.uithub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.adapter.AnnouncementAdapter;
import com.example.uithub.adapter.ScheduleItemAdapter;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.Announcement;
import com.example.uithub.models.AnnouncementResponse;
import com.example.uithub.models.ScheduleItem;
import com.example.uithub.models.TuitionItem;
import com.example.uithub.models.TuitionResponse;
import com.example.uithub.repository.MainRepository;
import com.example.uithub.utils.JSONParser;
import com.example.uithub.utils.PreferenceManager;
import com.example.uithub.utils.ScheduleStatusUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final int MAX_RECENT_ANNOUNCEMENTS = 5;

    private PreferenceManager preferenceManager;
    private ScheduleItemAdapter todayClassesAdapter;
    private AnnouncementAdapter announcementAdapter;
    private TextView todayClassesEmpty;
    private TextView announcementsEmpty;
    private ProgressBar progressBar;
    private Call<ResponseBody> scheduleCall;
    private Call<AnnouncementResponse> announcementsCall;
    private Call<TuitionResponse> tuitionCall;
    private int pendingLoads;

    // Home widgets
    private TextView tvHomeTuitionDebt, tvHomeTuitionUpdate;
    private TextView tvHomeGpa, tvHomeCredits;
    private View cardDeadline;
    private TextView tvDeadlineText;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());
        progressBar = view.findViewById(R.id.homeProgressBar);
        todayClassesEmpty = view.findViewById(R.id.tvTodayClassesEmpty);
        announcementsEmpty = view.findViewById(R.id.tvRecentAnnouncementsEmpty);

        // Home widgets
        tvHomeTuitionDebt = view.findViewById(R.id.tvHomeTuitionDebt);
        tvHomeTuitionUpdate = view.findViewById(R.id.tvHomeTuitionUpdate);
        tvHomeGpa = view.findViewById(R.id.tvHomeGpa);
        tvHomeCredits = view.findViewById(R.id.tvHomeCredits);
        cardDeadline = view.findViewById(R.id.cardDeadline);
        tvDeadlineText = view.findViewById(R.id.tvDeadlineText);

        // Tuition card click -> open Tuition detail in Study context
        view.findViewById(R.id.cardHomeTuition).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TuitionActivity.class);
            startActivity(intent);
        });

        RecyclerView todayClassesRecyclerView = view.findViewById(R.id.todayClassesRecyclerView);
        todayClassesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        todayClassesAdapter = new ScheduleItemAdapter(new ArrayList<>());
        todayClassesRecyclerView.setAdapter(todayClassesAdapter);

        RecyclerView announcementsRecyclerView = view.findViewById(R.id.recentAnnouncementsRecyclerView);
        announcementsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementAdapter = new AnnouncementAdapter(new ArrayList<>());
        announcementsRecyclerView.setAdapter(announcementAdapter);

        // Cache-first: show cached tuition immediately, then refresh in background
        loadCachedTuition();
        loadTodayClasses();
        loadRecentAnnouncements();
        refreshTuitionInBackground();
    }

    private void loadCachedTuition() {
        String cached = preferenceManager.getTuitionJson();
        if (cached != null && !cached.isEmpty()) {
            long timestamp = preferenceManager.getTuitionTimestamp();
            String timeText = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));
            tvHomeTuitionUpdate.setText("Cập nhật: " + timeText);
            tvHomeTuitionDebt.setText("Đã lưu");
        }
    }

    private void refreshTuitionInBackground() {
        String token = preferenceManager.getToken();
        if (token == null) return;

        beginLoad();
        tuitionCall = RetrofitClient.getApiService().getTuition("Bearer " + token, null, null);
        tuitionCall.enqueue(new Callback<TuitionResponse>() {
            @Override
            public void onResponse(@NonNull Call<TuitionResponse> call, @NonNull Response<TuitionResponse> response) {
                finishLoad();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateTuitionDisplay(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuitionResponse> call, @NonNull Throwable t) {
                finishLoad();
            }
        });
    }

    private void updateTuitionDisplay(TuitionResponse tuitionResponse) {
        long totalDebt = 0;
        if (tuitionResponse.getSummary() != null) {
            totalDebt = tuitionResponse.getSummary().getRemaining();
        }

        View cardHomeTuition = getView().findViewById(R.id.cardHomeTuition);
        String debtText;
        if (totalDebt > 0) {
            debtText = String.format("%,d₫", totalDebt);
            tvHomeTuitionDebt.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red));
            cardHomeTuition.setVisibility(View.VISIBLE);
        } else {
            debtText = "Đã thanh toán";
            cardHomeTuition.setVisibility(View.GONE);
        }
        tvHomeTuitionDebt.setText(debtText);

        String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
        tvHomeTuitionUpdate.setText("Cập nhật: " + time);
    }

    private void loadTodayClasses() {
        if (renderCachedSchedule()) {
            return;
        }

        String token = preferenceManager.getToken();
        if (token == null) {
            showTodayClasses(new ArrayList<>());
            return;
        }

        beginLoad();
        scheduleCall = RetrofitClient.getApiService().getSchedule(token);
        scheduleCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                finishLoad();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        preferenceManager.saveScheduleJson(json);
                        renderSchedule(json);
                    } catch (Exception e) {
                        showTodayClasses(new ArrayList<>());
                    }
                } else {
                    showTodayClasses(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                finishLoad();

                if (!call.isCanceled() && !renderCachedSchedule()) {
                    showTodayClasses(new ArrayList<>());
                }
            }
        });
    }

    private boolean renderCachedSchedule() {
        String cachedJson = preferenceManager.getScheduleJson();
        if (cachedJson == null || cachedJson.isEmpty()) {
            return false;
        }

        try {
            renderSchedule(cachedJson);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void renderSchedule(String json) throws Exception {
        List<ScheduleItem> scheduleItems = JSONParser.parseSchedule(json);
        List<ScheduleItem> openToday = new ArrayList<>();
        for (ScheduleItem item : scheduleItems) {
            if (ScheduleStatusUtils.isOpenToday(item)) {
                openToday.add(item);
            }
        }
        showTodayClasses(openToday);
    }

    private void showTodayClasses(List<ScheduleItem> items) {
        todayClassesAdapter.setData(items);
        todayClassesEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadRecentAnnouncements() {
        beginLoad();
        announcementsCall = new MainRepository().getAnnouncements(null, 0, MAX_RECENT_ANNOUNCEMENTS);
        announcementsCall.enqueue(new Callback<AnnouncementResponse>() {
            @Override
            public void onResponse(@NonNull Call<AnnouncementResponse> call, @NonNull Response<AnnouncementResponse> response) {
                finishLoad();

                if (response.isSuccessful() && response.body() != null) {
                    showRecentAnnouncements(response.body().getData());
                } else {
                    showRecentAnnouncements(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AnnouncementResponse> call, @NonNull Throwable t) {
                finishLoad();

                if (!call.isCanceled()) {
                    showRecentAnnouncements(new ArrayList<>());
                }
            }
        });
    }

    private void showRecentAnnouncements(List<Announcement> announcements) {
        List<Announcement> recent = new ArrayList<>();
        if (announcements != null) {
            int count = Math.min(announcements.size(), MAX_RECENT_ANNOUNCEMENTS);
            recent.addAll(announcements.subList(0, count));
        }

        announcementAdapter.setData(recent);
        announcementsEmpty.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void beginLoad() {
        pendingLoads++;
        progressBar.setVisibility(View.VISIBLE);
    }

    private void finishLoad() {
        pendingLoads = Math.max(0, pendingLoads - 1);
        progressBar.setVisibility(pendingLoads > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (scheduleCall != null) {
            scheduleCall.cancel();
        }
        if (announcementsCall != null) {
            announcementsCall.cancel();
        }
        if (tuitionCall != null) {
            tuitionCall.cancel();
        }
        super.onDestroyView();
    }
}
