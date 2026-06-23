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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudyFragment extends Fragment {

    private PreferenceManager preferenceManager;
    private TextView tvGpaValue, tvCreditProgress;
    private TextView tvTuitionDebt, tvTuitionUpdated, tvTuitionStatus;
    private ProgressBar creditProgressBar, progressBar;
    private Call<TuitionResponse> tuitionCall;

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
        progressBar = view.findViewById(R.id.studyProgressBar);

        // Tuition card click -> open TuitionActivity
        view.findViewById(R.id.cardTuitionSummary).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), TuitionActivity.class));
        });

        // Load data
        loadCachedTuition();
        loadTuitionData();
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

        progressBar.setVisibility(View.VISIBLE);
        tuitionCall = RetrofitClient.getApiService().getTuition("Bearer " + token, null, null);
        tuitionCall.enqueue(new Callback<TuitionResponse>() {
            @Override
            public void onResponse(@NonNull Call<TuitionResponse> call, @NonNull Response<TuitionResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateTuitionFromItems(response.body().getSemesters());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuitionResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateTuitionFromItems(List<TuitionItem> items) {
        double totalDebt = 0;
        for (TuitionItem item : items) {
            totalDebt += item.getSoTien();
        }

        String debtText;
        if (totalDebt > 0) {
            debtText = String.format("%,.0f₫", totalDebt);
            tvTuitionDebt.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red));
        } else {
            debtText = "0₫";
            tvTuitionDebt.setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground));
        }
        tvTuitionDebt.setText(debtText);
        tvTuitionStatus.setText(items.size() > 0 ? "Đã cập nhật" : "Không có dữ liệu");

        String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
        tvTuitionUpdated.setText("Cập nhật: " + time);
    }

    private void updateTuitionUi(String cachedJson) {
        // TODO: Parse cached JSON and update UI
        // For now, just show cached status
        tvTuitionUpdated.setText("Đã lưu");
    }

    @Override
    public void onDestroyView() {
        if (tuitionCall != null) {
            tuitionCall.cancel();
        }
        super.onDestroyView();
    }
}