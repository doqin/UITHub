package com.example.uithub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.StudentProfile;
import com.example.uithub.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private PreferenceManager preferenceManager;
    private ProgressBar progressBar;
    private Call<StudentProfile> profileCall;
    private boolean isProfileExpanded = false;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());

        progressBar = view.findViewById(R.id.profileProgressBar);

        // Set student info
        TextView tvStudentName = view.findViewById(R.id.tvStudentName);
        TextView tvStudentId = view.findViewById(R.id.tvStudentId);

        // Show cached MSSV while loading
        String mssv = preferenceManager.getMssv();
        if (mssv != null && !mssv.isEmpty()) {
            tvStudentId.setText("MSSV: " + mssv);
        }

        // Load cached profile first
        loadCachedProfile();

        // Fetch fresh profile from API
        loadProfile();

        // Toggle profile details
        view.findViewById(R.id.btnProfileInfo).setOnClickListener(v -> {
            isProfileExpanded = !isProfileExpanded;
            View layoutDetails = view.findViewById(R.id.layoutProfileDetails);
            TextView tvExpandIcon = view.findViewById(R.id.tvExpandIcon);
            layoutDetails.setVisibility(isProfileExpanded ? View.VISIBLE : View.GONE);
            tvExpandIcon.setRotation(isProfileExpanded ? 180f : 0f);
        });

        view.findViewById(R.id.btnAccount).setOnClickListener(v ->
                Toast.makeText(getContext(), "Tài khoản", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                Toast.makeText(getContext(), "Cài đặt", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            preferenceManager.clear();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void loadCachedProfile() {
        String cached = preferenceManager.getProfileJson();
        if (cached != null && !cached.isEmpty()) {
            try {
                // Simple parsing - extract key fields from cached JSON
                // For now, just show that we have cached data
                updateLastUpdatedTime();
            } catch (Exception ignored) {}
        }
    }

    private void loadProfile() {
        String token = preferenceManager.getToken();
        if (token == null) return;

        progressBar.setVisibility(View.VISIBLE);
        profileCall = RetrofitClient.getApiService().getProfile("Bearer " + token);
        profileCall.enqueue(new Callback<StudentProfile>() {
            @Override
            public void onResponse(@NonNull Call<StudentProfile> call, @NonNull Response<StudentProfile> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    preferenceManager.saveProfileJson(profileToJson(response.body()));
                    updateProfileUi(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StudentProfile> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateProfileUi(StudentProfile profile) {
        TextView tvStudentName = getView().findViewById(R.id.tvStudentName);
        TextView tvStudentId = getView().findViewById(R.id.tvStudentId);
        TextView tvDob = getView().findViewById(R.id.tvDob);
        TextView tvGender = getView().findViewById(R.id.tvGender);
        TextView tvClass = getView().findViewById(R.id.tvClass);
        TextView tvFaculty = getView().findViewById(R.id.tvFaculty);
        TextView tvTrainingLevel = getView().findViewById(R.id.tvTrainingLevel);
        TextView tvTrainingSystem = getView().findViewById(R.id.tvTrainingSystem);
        TextView tvMajor = getView().findViewById(R.id.tvMajor);
        TextView tvLastUpdated = getView().findViewById(R.id.tvLastUpdated);

        if (profile.getHoTen() != null) {
            tvStudentName.setText(profile.getHoTen());
        }
        if (profile.getMssv() != null) {
            tvStudentId.setText("MSSV: " + profile.getMssv());
        }
        if (profile.getNgaySinh() != null) {
            tvDob.setText("Ngày sinh: " + profile.getNgaySinh());
        }
        if (profile.getGioiTinh() != null) {
            tvGender.setText("Giới tính: " + profile.getGioiTinh());
        }
        if (profile.getLopSinhHoat() != null) {
            tvClass.setText("Lớp: " + profile.getLopSinhHoat());
        }
        if (profile.getKhoa() != null) {
            tvFaculty.setText("Khoa: " + profile.getKhoa());
        }
        if (profile.getBacDaoTao() != null) {
            tvTrainingLevel.setText("Bậc đào tạo: " + profile.getBacDaoTao());
        }
        if (profile.getHeDaoTao() != null) {
            tvTrainingSystem.setText("Hệ đào tạo: " + profile.getHeDaoTao());
        }
        if (profile.getNganh() != null) {
            tvMajor.setText("Ngành: " + profile.getNganh());
        }

        updateLastUpdatedTime();
    }

    private void updateLastUpdatedTime() {
        TextView tvLastUpdated = getView().findViewById(R.id.tvLastUpdated);
        if (tvLastUpdated != null) {
            String time = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            tvLastUpdated.setText("Cập nhật: " + time);
        }
    }

    private String profileToJson(StudentProfile profile) {
        return String.format(
            "{\"ho_ten\":\"%s\",\"mssv\":\"%s\",\"ngay_sinh\":\"%s\",\"gioi_tinh\":\"%s\"," +
            "\"lop_sinh_hoat\":\"%s\",\"khoa\":\"%s\",\"bac_dao_tao\":\"%s\"," +
            "\"he_dao_tao\":\"%s\",\"nganh\":\"%s\"}",
            profile.getHoTen() != null ? profile.getHoTen() : "",
            profile.getMssv() != null ? profile.getMssv() : "",
            profile.getNgaySinh() != null ? profile.getNgaySinh() : "",
            profile.getGioiTinh() != null ? profile.getGioiTinh() : "",
            profile.getLopSinhHoat() != null ? profile.getLopSinhHoat() : "",
            profile.getKhoa() != null ? profile.getKhoa() : "",
            profile.getBacDaoTao() != null ? profile.getBacDaoTao() : "",
            profile.getHeDaoTao() != null ? profile.getHeDaoTao() : "",
            profile.getNganh() != null ? profile.getNganh() : ""
        );
    }

    @Override
    public void onDestroyView() {
        if (profileCall != null) {
            profileCall.cancel();
        }
        super.onDestroyView();
    }
}
