package com.example.uithub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private View profileSkeleton;
    private View scrollContent;
    private Call<StudentProfile> profileCall;
    private boolean isProfileExpanded = false;
    private boolean dataLoaded = false;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());

        progressBar = view.findViewById(R.id.profileProgressBar);
        profileSkeleton = view.findViewById(R.id.profileSkeleton);
        scrollContent = view.findViewById(R.id.scrollContent);

        // Show skeleton while loading
        profileSkeleton.setVisibility(View.VISIBLE);
        scrollContent.setVisibility(View.GONE);

        // Set student info
        TextView tvStudentName = view.findViewById(R.id.tvStudentName);
        TextView tvStudentId = view.findViewById(R.id.tvStudentId);

        // Show cached MSSV while loading
        String mssv = preferenceManager.getMssv();
        if (mssv != null && !mssv.isEmpty()) {
            tvStudentId.setText("MSSV: " + mssv);
        }

        // Load cached profile first
        if (!loadCachedProfile()) {
            // If no cache, start loading from API
            loadProfile();
        } else {
            // Cache loaded, also fetch from API for refresh
            loadProfile();
        }

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

    private boolean loadCachedProfile() {
        String cachedJson = preferenceManager.getProfileJson();
        if (cachedJson == null || cachedJson.isEmpty()) {
            return false;
        }

        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            StudentProfile cachedProfile = gson.fromJson(cachedJson, StudentProfile.class);
            if (cachedProfile != null && cachedProfile.getHoTen() != null) {
                updateProfileUi(cachedProfile);
                return true;
            }
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error parsing cached profile", e);
        }
        return false;
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
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    preferenceManager.saveProfileJson(gson.toJson(response.body()));
                    updateProfileUi(response.body());
                } else {
                    // If API fails and we already showed data, keep it
                    if (!dataLoaded) {
                        profileSkeleton.setVisibility(View.GONE);
                        scrollContent.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StudentProfile> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                // If no data loaded yet, hide skeleton and show content (with placeholders)
                if (!dataLoaded) {
                    profileSkeleton.setVisibility(View.GONE);
                    scrollContent.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updateProfileUi(StudentProfile profile) {
        dataLoaded = true;

        // Hide skeleton, show real content
        profileSkeleton.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);

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

    // Removed profileToJson - using Gson instead

    @Override
    public void onDestroyView() {
        if (profileCall != null) {
            profileCall.cancel();
        }
        super.onDestroyView();
    }
}
