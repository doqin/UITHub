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

import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.TuitionItem;
import com.example.uithub.models.TuitionResponse;
import com.example.uithub.utils.PreferenceManager;
import com.example.uithub.models.GradesResponse;
import com.example.uithub.models.GradesSummary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.recyclerview.widget.RecyclerView;
import com.example.uithub.adapter.DeadlineAdapter;
import com.example.uithub.models.DeadlineResponse;

public class StudyFragment extends Fragment {

    private PreferenceManager preferenceManager;
    private TextView tvGpaValue, tvCreditProgress;
    private TextView tvTuitionDebt, tvTuitionUpdated, tvTuitionStatus;
    private ProgressBar creditProgressBar;
    private Call<TuitionResponse> tuitionCall;

    private RecyclerView rvDeadlines;
    private TextView tvDeadlineRefresh, tvDeadlineEmpty;
    private ProgressBar deadlineProgressBar;
    private DeadlineAdapter deadlineAdapter;
    private Call<DeadlineResponse> deadlineCall;

    public StudyFragment() {
        super(R.layout.fragment_study);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());

        tvGpaValue = view.findViewById(R.id.tvGpaValue);
        tvCreditProgress = view.findViewById(R.id.tvCreditProgress);
        creditProgressBar = view.findViewById(R.id.creditProgressBar);
        tvTuitionDebt = view.findViewById(R.id.tvTuitionDebt);
        tvTuitionUpdated = view.findViewById(R.id.tvTuitionUpdated);
        tvTuitionStatus = view.findViewById(R.id.tvTuitionStatus);

        rvDeadlines = view.findViewById(R.id.rvDeadlines);
        tvDeadlineRefresh = view.findViewById(R.id.tvDeadlineRefresh);
        tvDeadlineEmpty = view.findViewById(R.id.tvDeadlineEmpty);
        deadlineProgressBar = view.findViewById(R.id.deadlineProgressBar);

        // GPA card click -> open GradeDetailActivity
        view.findViewById(R.id.cardGpa).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), GradeDetailActivity.class));
        });

        // Tuition card click -> open TuitionActivity
        view.findViewById(R.id.cardTuitionSummary).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), TuitionActivity.class));
        });

        tvDeadlineRefresh.setOnClickListener(v -> loadDeadlines(true));

        deadlineAdapter = new DeadlineAdapter(requireContext(), null);
        rvDeadlines.setAdapter(deadlineAdapter);

        // Load data
        loadCachedTuition();
        loadTuitionData();
        loadGpa();
        loadDeadlines(false);
    }

    private void loadDeadlines(boolean refresh) {
        String token = preferenceManager.getToken();
        if (token == null) return;

        deadlineProgressBar.setVisibility(View.VISIBLE);
        tvDeadlineEmpty.setVisibility(View.GONE);
        rvDeadlines.setVisibility(View.GONE);

        deadlineCall = RetrofitClient.getApiService().getDeadlines("Bearer " + token, null, null, refresh);
        deadlineCall.enqueue(new Callback<DeadlineResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeadlineResponse> call, @NonNull Response<DeadlineResponse> response) {
                deadlineProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<com.example.uithub.models.Deadline> deadlines = response.body().getData();
                    if (deadlines != null && !deadlines.isEmpty()) {
                        deadlineAdapter.setDeadlines(deadlines);
                        rvDeadlines.setVisibility(View.VISIBLE);
                    } else {
                        tvDeadlineEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvDeadlineEmpty.setVisibility(View.VISIBLE);
                    tvDeadlineEmpty.setText("Không thể tải dữ liệu bài tập.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeadlineResponse> call, @NonNull Throwable t) {
                deadlineProgressBar.setVisibility(View.GONE);
                tvDeadlineEmpty.setVisibility(View.VISIBLE);
                tvDeadlineEmpty.setText("Lỗi kết nối.");
            }
        });
    }

    private void loadCachedTuition() {
        String cached = preferenceManager.getTuitionJson();
        if (cached != null && !cached.isEmpty()) {
            try {
                // Parse cached tuition data
                updateTuitionUi(cached);
            } catch (Exception ignored) {}
        }
    }

    private void loadTuitionData() {
        String token = preferenceManager.getToken();
        if (token == null) return;

        tuitionCall = RetrofitClient.getApiService().getTuition("Bearer " + token, null, null);
        tuitionCall.enqueue(new Callback<TuitionResponse>() {
            @Override
            public void onResponse(@NonNull Call<TuitionResponse> call, @NonNull Response<TuitionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateTuitionFromResponse(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuitionResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void updateTuitionFromResponse(TuitionResponse response) {
        long totalDebt = 0;
        if (response.getSummary() != null) {
            totalDebt = response.getSummary().getRemaining();
        }

        String debtText;
        if (totalDebt > 0) {
            debtText = String.format("%,d₫", totalDebt);
            tvTuitionDebt.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red));
        } else {
            debtText = "0₫";
            tvTuitionDebt.setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground));
        }
        tvTuitionDebt.setText(debtText);

        List<TuitionItem> items = response.getSemesters();
        tvTuitionStatus.setText((items != null && items.size() > 0) ? "Đã cập nhật" : "Không có dữ liệu");

        String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
        tvTuitionUpdated.setText("Cập nhật: " + time);
    }

    private void updateTuitionUi(String cachedJson) {
        // TODO: Parse cached JSON and update UI
        // For now, just show cached status
        tvTuitionUpdated.setText("Đã lưu");
    }

    private void loadGpa() {
        String token = preferenceManager.getToken();
        RetrofitClient.getApiService().getGrades("Bearer " + token, null, null)
                .enqueue(new Callback<GradesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GradesResponse> call, @NonNull Response<GradesResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getData() != null && response.body().getData().getSummary() != null) {
                                GradesSummary summary = response.body().getData().getSummary();

                                tvGpaValue.setText(String.format(Locale.getDefault(), "%.2f", summary.getGpaTichLuy()));

                                double tinChi = summary.getTinChiTichLuy();
                                tvCreditProgress.setText(String.format(Locale.getDefault(), "%.0f TC", tinChi));

                                creditProgressBar.setProgress((int) tinChi);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GradesResponse> call, @NonNull Throwable t) {
                    }
                });
    }

    @Override
    public void onDestroyView() {
        if (tuitionCall != null) {
            tuitionCall.cancel();
        }
        if (deadlineCall != null) {
            deadlineCall.cancel();
        }
        super.onDestroyView();
    }
}