package com.example.uithub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uithub.adapter.ExamScheduleAdapter;
import com.example.uithub.adapter.SchedulePagerAdapter;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.ExamModel;
import com.example.uithub.models.ExamScheduleResponse;
import com.example.uithub.models.ScheduleItem;
import com.example.uithub.utils.JSONParser;
import com.example.uithub.utils.PreferenceManager;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleFragment extends Fragment {

    private static final String TAG = "ScheduleFragment";

    private SchedulePagerAdapter adapter;
    private Map<String, List<ScheduleItem>> map;

    // Exam-related
    private ExamScheduleAdapter examAdapter;
    private Call<ExamScheduleResponse> examCall;
    private Call<ResponseBody> scheduleCall;
    private PreferenceManager preferenceManager;

    // UI
    private MaterialButtonToggleGroup toggleGroup;
    private View scheduleContainer, examContainer;
    private ProgressBar progressBar;
    private View btnReload;
    private TextView tvExamHint;

    // Schedule views
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    // Dropdowns
    private com.google.android.material.textfield.MaterialAutoCompleteTextView actHocKy, actNamHoc, actLanThi;

    // Selected values
    private int selectedHocKy = -1;
    private int selectedNamHoc = -1;
    private int selectedLanThi = -1;

    public ScheduleFragment() {
        super(R.layout.fragment_schedule);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());

        // UI
        toggleGroup = view.findViewById(R.id.toggleGroup);
        scheduleContainer = view.findViewById(R.id.scheduleContainer);
        examContainer = view.findViewById(R.id.examContainer);
        progressBar = view.findViewById(R.id.progressBar);
        btnReload = view.findViewById(R.id.btnReload);
        tvExamHint = view.findViewById(R.id.tvExamHint);

        // Schedule views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        adapter = new SchedulePagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Load cached schedule first, then fetch from API
        loadScheduleFromCache(tabLayout, viewPager);
        loadScheduleFromApi(tabLayout, viewPager);

        // Refresh button
        btnReload.setOnClickListener(v -> {
            Log.d(TAG, "Refresh button clicked, reloading schedule...");
            loadScheduleFromApi(tabLayout, viewPager);
        });

        // Exam views
        RecyclerView rvExam = view.findViewById(R.id.rvExamSchedule);
        rvExam.setLayoutManager(new LinearLayoutManager(getContext()));
        examAdapter = new ExamScheduleAdapter(new ArrayList<>());
        rvExam.setAdapter(examAdapter);

        // Setup dropdowns
        setupExamDropdowns(view);

        // Toggle listener
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnSchedule) {
                scheduleContainer.setVisibility(View.VISIBLE);
                examContainer.setVisibility(View.GONE);
                Log.d(TAG, "Switched to schedule tab");
            } else {
                scheduleContainer.setVisibility(View.GONE);
                examContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "Switched to exam tab");
                updateExamHintVisibility();
                if (selectedHocKy != -1 && selectedNamHoc != -1 && selectedLanThi != -1) {
                    loadExamSchedule();
                }
            }
        });
    }

    private void updateExamHintVisibility() {
        boolean allSelected = selectedHocKy != -1 && selectedNamHoc != -1 && selectedLanThi != -1;
        tvExamHint.setVisibility(allSelected ? View.GONE : View.VISIBLE);
    }

    private void setupExamDropdowns(View view) {
        actHocKy = view.findViewById(R.id.actHocKy);
        actNamHoc = view.findViewById(R.id.actNamHoc);
        actLanThi = view.findViewById(R.id.actLanThi);

        // Học kỳ: 1, 2
        String[] hocKyValues = {"1", "2"};
        ArrayAdapter<String> hocKyAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, hocKyValues);
        actHocKy.setAdapter(hocKyAdapter);
        actHocKy.setOnItemClickListener((parent, v, position, id) -> {
            selectedHocKy = Integer.parseInt(hocKyValues[position]);
            actHocKy.setText(hocKyValues[position], false);
            Log.d(TAG, "Selected hocKy=" + selectedHocKy);
            updateExamHintVisibility();
            if (examContainer.getVisibility() == View.VISIBLE && selectedHocKy != -1 && selectedNamHoc != -1 && selectedLanThi != -1) {
                loadExamSchedule();
            }
        });

        // Năm học: 2024-2027
        String[] namHocValues = {"2024", "2025", "2026", "2027"};
        ArrayAdapter<String> namHocAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, namHocValues);
        actNamHoc.setAdapter(namHocAdapter);
        actNamHoc.setOnItemClickListener((parent, v, position, id) -> {
            selectedNamHoc = Integer.parseInt(namHocValues[position]);
            actNamHoc.setText(namHocValues[position], false);
            Log.d(TAG, "Selected namHoc=" + selectedNamHoc);
            updateExamHintVisibility();
            if (examContainer.getVisibility() == View.VISIBLE && selectedHocKy != -1 && selectedNamHoc != -1 && selectedLanThi != -1) {
                loadExamSchedule();
            }
        });

        // Kì thi: GK, CK
        String[] lanThiValues = {"GK", "CK"};
        ArrayAdapter<String> lanThiAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, lanThiValues);
        actLanThi.setAdapter(lanThiAdapter);
        actLanThi.setOnItemClickListener((parent, v, position, id) -> {
            String selected = lanThiValues[position];
            // GK -> 1, CK -> 2
            selectedLanThi = "GK".equals(selected) ? 1 : 2;
            actLanThi.setText(selected, false);
            Log.d(TAG, "Selected lanThi=" + selectedLanThi);
            updateExamHintVisibility();
            if (examContainer.getVisibility() == View.VISIBLE && selectedHocKy != -1 && selectedNamHoc != -1 && selectedLanThi != -1) {
                loadExamSchedule();
            }
        });
    }

    private void loadScheduleFromCache(TabLayout tabLayout, ViewPager2 viewPager) {
        String cachedJson = preferenceManager.getScheduleJson();
        if (cachedJson == null) return;

        try {
            List<ScheduleItem> list = JSONParser.parseSchedule(cachedJson);
            if (list.isEmpty()) return;

            map = JSONParser.groupByDay(list);
            adapter.setData(map);

            List<String> days = new ArrayList<>(map.keySet());
            Log.d(TAG, "Schedule loaded from cache: " + list.size() + " items across " + days.size() + " days");

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(days.get(position));
            }).attach();

        } catch (Exception e) {
            Log.e(TAG, "Error parsing cached schedule JSON", e);
        }
    }

    private void loadScheduleFromApi(TabLayout tabLayout, ViewPager2 viewPager) {
        String token = preferenceManager.getToken();
        if (token == null) {
            Log.e(TAG, "Cannot load schedule: token is null");
            Toast.makeText(requireContext(), "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching schedule from API...");

        // Cancel previous call if any
        if (scheduleCall != null) {
            scheduleCall.cancel();
        }

        scheduleCall = RetrofitClient.getApiService().getSchedule("Bearer " + token);
        scheduleCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Log.d(TAG, "Schedule API response received, length=" + json.length());

                        // Cache the raw JSON
                        preferenceManager.saveScheduleJson(json);

                        List<ScheduleItem> list = JSONParser.parseSchedule(json);
                        map = JSONParser.groupByDay(list);
                        adapter.setData(map);

                        List<String> days = new ArrayList<>(map.keySet());
                        Log.d(TAG, "Schedule loaded successfully: " + list.size() + " items across " + days.size() + " days");

                        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                            tab.setText(days.get(position));
                        }).attach();

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing schedule JSON", e);
                        Toast.makeText(requireContext(), "Lỗi xử lý dữ liệu lịch học", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int statusCode = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException ignored) {
                    }
                    Log.e(TAG, "Schedule API error: HTTP " + statusCode + " - " + errorBody);

                    // If we have cached data, don't show error toast for failed refresh
                    String cachedJson = preferenceManager.getScheduleJson();
                    if (cachedJson == null) {
                        Toast.makeText(requireContext(), "Lỗi tải lịch học (Mã: " + statusCode + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Schedule API network failure: " + t.getMessage(), t);

                // Only show error if no cached data
                String cachedJson = preferenceManager.getScheduleJson();
                if (cachedJson == null) {
                    Toast.makeText(requireContext(), "Lỗi mạng: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadExamSchedule() {
        // Should not be called unless all are selected, but check just in case
        if (selectedHocKy == -1 || selectedNamHoc == -1 || selectedLanThi == -1) {
            return;
        }

        String token = preferenceManager.getToken();
        if (token == null) {
            Log.e(TAG, "Cannot load exam schedule: token is null");
            Toast.makeText(requireContext(), "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading exam schedule: HK=" + selectedHocKy + ", Nam=" + selectedNamHoc + ", Lan=" + selectedLanThi);

        // Try loading from cache first
        String cacheKey = "exam_" + selectedLanThi + "_" + selectedHocKy + "_" + selectedNamHoc;
        loadExamFromCache(cacheKey);

        // Cancel previous call
        if (examCall != null) {
            examCall.cancel();
        }

        progressBar.setVisibility(View.VISIBLE);

        examCall = RetrofitClient.getApiService().getExamSchedule(
                "Bearer " + token,
                selectedLanThi,
                selectedHocKy,
                selectedNamHoc
        );

        examCall.enqueue(new Callback<ExamScheduleResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExamScheduleResponse> call, @NonNull Response<ExamScheduleResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ExamScheduleResponse body = response.body();
                    if (body.isSuccess()) {
                        // Cache the exam schedule as JSON
                        Gson gson = new Gson();
                        preferenceManager.saveExamScheduleJson(gson.toJson(body));

                        List<ExamModel> exams = body.getData();
                        if (exams != null && !exams.isEmpty()) {
                            // Sort: UPCOMING first, then COMPLETED; within same status, sort by exam_date ascending
                            Collections.sort(exams, (a, b) -> {
                                boolean aUpcoming = isUpcoming(a.getExam_date());
                                boolean bUpcoming = isUpcoming(b.getExam_date());
                                if (aUpcoming != bUpcoming) {
                                    return aUpcoming ? -1 : 1;
                                }
                                return a.getExam_date().compareTo(b.getExam_date());
                            });
                            examAdapter.setData(exams);
                            Log.d(TAG, "Exam schedule loaded successfully: " + exams.size() + " exams" +
                                    (body.isCached() ? " (cached)" : ""));
                        } else {
                            Log.w(TAG, "Exam schedule loaded but data is empty");
                            examAdapter.setData(new ArrayList<>());
                            Toast.makeText(requireContext(), "Không có lịch thi cho kỳ này", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Exam schedule API returned success=false");
                        Toast.makeText(requireContext(), "Không thể tải lịch thi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int statusCode = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException ignored) {
                    }
                    Log.e(TAG, "Exam schedule API error: HTTP " + statusCode + " - " + errorBody);

                    // Only show error if no cached data was displayed
                    if (examAdapter.getItemCount() == 0) {
                        Toast.makeText(requireContext(), "Lỗi tải lịch thi (Mã: " + statusCode + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExamScheduleResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Exam schedule network failure: " + t.getMessage(), t);

                if (examAdapter.getItemCount() == 0) {
                    Toast.makeText(requireContext(), "Lỗi mạng: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadExamFromCache(String cacheKey) {
        String cachedJson = preferenceManager.getExamScheduleJson();
        if (cachedJson == null) return;

        try {
            Gson gson = new Gson();
            ExamScheduleResponse cachedResponse = gson.fromJson(cachedJson, ExamScheduleResponse.class);
            List<ExamModel> exams = cachedResponse.getData();

            if (exams != null && !exams.isEmpty()) {
                Collections.sort(exams, (a, b) -> {
                    boolean aUpcoming = isUpcoming(a.getExam_date());
                    boolean bUpcoming = isUpcoming(b.getExam_date());
                    if (aUpcoming != bUpcoming) {
                        return aUpcoming ? -1 : 1;
                    }
                    return a.getExam_date().compareTo(b.getExam_date());
                });
                examAdapter.setData(exams);
                Log.d(TAG, "Exam schedule loaded from cache: " + exams.size() + " exams");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing cached exam schedule", e);
        }
    }

    /**
     * Determines if an exam is upcoming (exam_date is today or in the future).
     */
    private boolean isUpcoming(String examDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            Date exam = sdf.parse(examDate);
            Date now = new Date();
            return exam != null && !exam.before(now);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        if (examCall != null) {
            examCall.cancel();
        }
        if (scheduleCall != null) {
            scheduleCall.cancel();
        }
        super.onDestroyView();
    }
}