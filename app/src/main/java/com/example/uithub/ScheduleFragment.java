package com.example.uithub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.example.uithub.utils.CalendarUtils;
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
    private View btnReload, btnSyncAll;
    private TextView tvExamHint;
    private View btnSyncAllExams;

    // Schedule views
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    // Dropdowns
    private com.google.android.material.textfield.MaterialAutoCompleteTextView actHocKy, actNamHoc, actLanThi;

    // Selected values
    private int selectedHocKy = -1;
    private int selectedNamHoc = -1;
    private int selectedLanThi = -1;

    // Pending calendar event data (saved while permission is being requested)
    private String pendingTitle;
    private String pendingLocation;
    private String pendingDescription;
    private long pendingBeginTime;
    private long pendingEndTime;
    private String pendingDay;
    private String pendingDate;
    private String pendingEndDate;
    private boolean pendingIsExam;

    // Calendar permission launcher
    private final ActivityResultLauncher<String[]> calendarPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readGranted = result.getOrDefault(Manifest.permission.READ_CALENDAR, false);
                Boolean writeGranted = result.getOrDefault(Manifest.permission.WRITE_CALENDAR, false);
                if (Boolean.TRUE.equals(readGranted) && Boolean.TRUE.equals(writeGranted)) {
                    Log.d(TAG, "Calendar permissions granted, executing pending event");
                    executePendingCalendarAction();
                } else {
                    Toast.makeText(requireContext(), "Cần cấp quyền lịch để đồng bộ", Toast.LENGTH_SHORT).show();
                }
            });

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
        btnSyncAll = view.findViewById(R.id.btnSyncAll);
        tvExamHint = view.findViewById(R.id.tvExamHint);

        // Schedule views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        adapter = new SchedulePagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Load cached schedule first. Refreshing from the API is explicit via the reload button
        // so cached schedules remain readable after the server-side session expires.
        loadScheduleFromCache(tabLayout, viewPager);

        // Refresh button
        btnReload.setOnClickListener(v -> {
            v.animate().rotationBy(360f).setDuration(500).start();
            Log.d(TAG, "Refresh button clicked, reloading schedule...");
            loadScheduleFromApi(tabLayout, viewPager);
        });

        // Sync All button
        btnSyncAll.setOnClickListener(v -> {
            syncAllScheduleToCalendar();
        });

        // Sync All Exams button - only visible when exams are loaded
        btnSyncAllExams = view.findViewById(R.id.btnSyncAllExams);
        btnSyncAllExams.setOnClickListener(v -> {
            syncAllExamsToCalendar();
        });
        // Initially hidden, shown when exams load
        btnSyncAllExams.setVisibility(View.GONE);

        // Exam views
        RecyclerView rvExam = view.findViewById(R.id.rvExamSchedule);
        rvExam.setLayoutManager(new LinearLayoutManager(getContext()));
        examAdapter = new ExamScheduleAdapter(new ArrayList<>());
        examAdapter.setOnCalendarSyncListener((title, location, description, beginTime, endTime) -> {
            addCalendarEvent(title, location, description, beginTime, endTime, true);
        });
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


    private void syncAllScheduleToCalendar() {
        // Collect all items from map
        List<ScheduleItem> allItems = new ArrayList<>();
        if (map != null) {
            for (List<ScheduleItem> items : map.values()) {
                allItems.addAll(items);
            }
        }
        if (allItems.isEmpty()) {
            Toast.makeText(requireContext(), "Không có lịch học để đồng bộ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare event data
        List<CalendarUtils.EventData> events = new ArrayList<>();
        for (ScheduleItem item : allItems) {
            String effectiveDate = item.start_date;
            long start = CalendarUtils.convertToMillis(effectiveDate, item.start_time);
            long end = CalendarUtils.convertToMillis(effectiveDate, item.end_time);
            String title = item.code + " - " + item.room;
            String description = "Lớp: " + item.code + "\n" +
                    "(" + item.name + ")\n" +
                    "- " + item.room + ",\n" +
                    item.day + ",\n" +
                    "Tiết " + item.period + ",\n" +
                    "Giảng viên: " + item.teacher + ",\n" +
                    "từ " + item.start_date + " - " + item.end_date;
            boolean biWeekly = CalendarUtils.isBiWeekly(item.room);
            String rrule = CalendarUtils.buildRRule(item.day, item.end_date, biWeekly);
            Log.d(TAG, "syncAll: title=" + title + " date=" + effectiveDate + " start=" + start + " end=" + end + " rrule=" + rrule);
            events.add(new CalendarUtils.EventData(title, item.room, description, start, end, rrule));
        }

        // Check permission and sync
        if (checkCalendarPermission()) {
            CalendarUtils.BatchInsertResult result = CalendarUtils.insertEventsBatch(requireContext(), events);
            Log.d(TAG, "syncAllScheduleToCalendar: inserted " + result.inserted + "/" + result.getTotal());
            
            // Show appropriate message based on results
            if (result.duplicates > 0 && result.inserted == 0) {
                // All events are duplicates
                Toast.makeText(requireContext(), "Lịch đã tồn tại trong Calendar", Toast.LENGTH_SHORT).show();
            } else if (result.duplicates > 0 && result.inserted > 0) {
                // Some duplicates, some inserted
                Toast.makeText(requireContext(), "Đã đồng bộ " + result.inserted + "/" + result.getTotal() + " lịch. " + result.duplicates + " lịch đã tồn tại.", Toast.LENGTH_SHORT).show();
            } else {
                // All inserted successfully
                Toast.makeText(requireContext(), "Đã xuất lịch thành công. Các sự kiện có thể mất vài phút để hiển thị trên Calendar.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Save events as pending and set flag
            pendingEvents = events;
            pendingSyncAll = true;
            Log.d(TAG, "syncAllScheduleToCalendar: saving " + events.size() + " events as pending, requesting permission");
            requestCalendarPermission();
        }
    }


    private void syncAllExamsToCalendar() {
        List<ExamModel> exams = examAdapter.getData();
        if (exams == null || exams.isEmpty()) {
            Toast.makeText(requireContext(), "Không có lịch thi để đồng bộ", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CalendarUtils.EventData> events = new ArrayList<>();
        for (ExamModel exam : exams) {
            // Prefer exam_datetime if available, otherwise use start_time or default to 07:00
            String timeStr = exam.getStart_time();
            if (timeStr == null || timeStr.isEmpty()) {
                timeStr = "07:00"; // default exam time
            }
            long start = convertToMillis(exam.getExam_date(), timeStr);
            long end = start + (2 * 60 * 60 * 1000); // 2 hours default
            String title = exam.getCourse_code() + " - " + exam.getRoom();
            String description = "Môn: " + exam.getCourse_code() + "\n" +
                    "Lớp: " + exam.getClass_code() + "\n" +
                    "Ca thi: " + (exam.getExam_shift() != null ? exam.getExam_shift() : "N/A") + "\n" +
                    "Ngày: " + exam.getExam_date() + " (" + exam.getWeekday() + ")\n" +
                    "Phòng: " + exam.getRoom();
            events.add(new CalendarUtils.EventData(title, exam.getRoom(), description, start, end, null));
        }

        if (checkCalendarPermission()) {
            CalendarUtils.BatchInsertResult result = CalendarUtils.insertEventsBatch(requireContext(), events);
            
            // Show appropriate message based on results
            if (result.duplicates > 0 && result.inserted == 0) {
                // All events are duplicates
                Toast.makeText(requireContext(), "Lịch đã tồn tại trong Calendar", Toast.LENGTH_SHORT).show();
            } else if (result.duplicates > 0 && result.inserted > 0) {
                // Some duplicates, some inserted
                Toast.makeText(requireContext(), "Đã đồng bộ " + result.inserted + "/" + result.getTotal() + " lịch. " + result.duplicates + " lịch đã tồn tại.", Toast.LENGTH_SHORT).show();
            } else {
                // All inserted successfully
                Toast.makeText(requireContext(), "Đã xuất lịch thành công. Các sự kiện có thể mất vài phút để hiển thị trên Calendar.", Toast.LENGTH_SHORT).show();
            }
        } else {
            pendingEvents = new ArrayList<>(events);
            pendingSyncAll = true;
            requestCalendarPermission();
        }
    }

    private long convertToMillis(String date, String time) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            java.util.Date d = sdf.parse(date + " " + time);
            return d != null ? d.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private List<CalendarUtils.EventData> pendingEvents;
    private boolean pendingSyncAll = false;


    public void addCalendarEvent(String title, String location, String description,
                                 String vietnameseDay, String dateStr, String endDateStr,
                                 long beginTime, long endTime) {
        // Save event data
        pendingTitle = title;
        pendingLocation = location;
        pendingDescription = description;
        pendingBeginTime = beginTime;
        pendingEndTime = endTime;
        pendingDay = vietnameseDay;
        pendingDate = dateStr;
        pendingEndDate = endDateStr;
        pendingIsExam = false;
        pendingSyncAll = false;

        if (checkCalendarPermission()) {
            CalendarUtils.addScheduleEvent(requireContext(), title, location, description,
                    vietnameseDay, dateStr, endDateStr, beginTime, endTime);
            Toast.makeText(requireContext(), "Đã xuất lịch thành công. Các sự kiện có thể mất vài giây để hiển thị trên Calendar.", Toast.LENGTH_SHORT).show();
        } else {
            requestCalendarPermission();
        }
    }


    public void addCalendarEvent(String title, String location, String description,
                                 long beginTime, long endTime, boolean isExam) {
        pendingTitle = title;
        pendingLocation = location;
        pendingDescription = description;
        pendingBeginTime = beginTime;
        pendingEndTime = endTime;
        pendingIsExam = true;
        pendingSyncAll = false;

        if (checkCalendarPermission()) {
            CalendarUtils.addExamEvent(requireContext(), title, location, description, beginTime, endTime);
            Toast.makeText(requireContext(), "Đã xuất lịch thành công. Các sự kiện có thể mất vài giây để hiển thị trên Calendar.", Toast.LENGTH_SHORT).show();
        } else {
            requestCalendarPermission();
        }
    }

    private boolean checkCalendarPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CALENDAR)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCalendarPermission() {
        calendarPermissionLauncher.launch(new String[]{
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
        });
    }

    private void executePendingCalendarAction() {
        if (pendingSyncAll && pendingEvents != null && !pendingEvents.isEmpty()) {
            CalendarUtils.BatchInsertResult result = CalendarUtils.insertEventsBatch(requireContext(), pendingEvents);
            
            // Show appropriate message based on results
            if (result.duplicates > 0 && result.inserted == 0) {
                // All events are duplicates
                Toast.makeText(requireContext(), "Lịch đã tồn tại trong Calendar", Toast.LENGTH_SHORT).show();
            } else if (result.duplicates > 0 && result.inserted > 0) {
                // Some duplicates, some inserted
                Toast.makeText(requireContext(), "Đã đồng bộ " + result.inserted + "/" + result.getTotal() + " lịch. " + result.duplicates + " lịch đã tồn tại.", Toast.LENGTH_SHORT).show();
            } else {
                // All inserted successfully
                Toast.makeText(requireContext(), "Đã xuất lịch thành công. Các sự kiện có thể mất vài phút để hiển thị trên Calendar.", Toast.LENGTH_SHORT).show();
            }
            
            pendingEvents = null;
            pendingSyncAll = false;
            return;
        }

        if (pendingTitle == null) return;
        try {
            if (pendingIsExam) {
                CalendarUtils.addExamEvent(requireContext(), pendingTitle, pendingLocation,
                        pendingDescription, pendingBeginTime, pendingEndTime);
            } else {
                CalendarUtils.addScheduleEvent(requireContext(), pendingTitle, pendingLocation,
                        pendingDescription, pendingDay, pendingDate, pendingEndDate,
                        pendingBeginTime, pendingEndTime);
            }
            Toast.makeText(requireContext(), "Đã xuất lịch thành công. Các sự kiện có thể mất vài giây để hiển thị trên Calendar.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error adding calendar event", e);
            Toast.makeText(requireContext(), "Không thể mở lịch", Toast.LENGTH_SHORT).show();
        }
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
        // Don't load unless all three dropdowns are selected
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
                            // Show sync button when exams are loaded
                            btnSyncAllExams.setVisibility(View.VISIBLE);
                            Log.d(TAG, "Exam schedule loaded successfully: " + exams.size() + " exams" +
                                    (body.isCached() ? " (cached)" : ""));
                        } else {
                            Log.w(TAG, "Exam schedule loaded but data is empty");
                            examAdapter.setData(new ArrayList<>());
                            btnSyncAllExams.setVisibility(View.GONE);
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
