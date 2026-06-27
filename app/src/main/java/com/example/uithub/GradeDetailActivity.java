package com.example.uithub;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.adapter.GradeSubjectAdapter;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.GradeSemester;
import com.example.uithub.models.GradeSubject;
import com.example.uithub.models.GradesResponse;
import com.example.uithub.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradeDetailActivity extends BaseActivity {

    private PreferenceManager preferenceManager;
    private ProgressBar loadingProgressBar;

    private TextView tvStudentName, tvStudentId;
    private TextView tvGpaAccumulated, tvCreditAccumulated;
    private Spinner spinnerYear, spinnerSemester;
    private TextView tvSemesterCredit, tvSemesterGpa;
    private TextView tvSubjectHeader, tvEmptySubjects;
    private RecyclerView rvSubjects;

    private GradeSubjectAdapter subjectAdapter;
    private List<GradeSemester> allSemesters = new ArrayList<>();

    private List<Integer> years = new ArrayList<>();
    private List<Integer> semesters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_detail);

        preferenceManager = new PreferenceManager(this);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvGpaAccumulated = findViewById(R.id.tvGpaAccumulated);
        tvCreditAccumulated = findViewById(R.id.tvCreditAccumulated);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        tvSemesterCredit = findViewById(R.id.tvSemesterCredit);
        tvSemesterGpa = findViewById(R.id.tvSemesterGpa);
        tvSubjectHeader = findViewById(R.id.tvSubjectHeader);
        tvEmptySubjects = findViewById(R.id.tvEmptySubjects);
        rvSubjects = findViewById(R.id.rvSubjects);

        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        subjectAdapter = new GradeSubjectAdapter(null);
        rvSubjects.setAdapter(subjectAdapter);

        setupSpinnerListeners();
        fetchGradeData();
    }

    private void setupSpinnerListeners() {
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateSemesterSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                showSelectedSemesterData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchGradeData() {
        String token = preferenceManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingProgressBar.setVisibility(android.view.View.VISIBLE);

        RetrofitClient.getApiService().getGrades("Bearer " + token, null, null)
                .enqueue(new Callback<GradesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GradesResponse> call, @NonNull Response<GradesResponse> response) {
                        loadingProgressBar.setVisibility(android.view.View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            populateData(response.body());
                        } else {
                            Toast.makeText(GradeDetailActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GradesResponse> call, @NonNull Throwable t) {
                        loadingProgressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(GradeDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateData(GradesResponse response) {
        if (response.getData() == null) return;

        // Student info
        if (response.getData().getStudentProfile() != null) {
            String name = response.getData().getStudentProfile().getHoTen();
            String mssv = response.getData().getStudentProfile().getMssv();
            if (name != null) tvStudentName.setText(name);
            if (mssv != null) tvStudentId.setText("MSSV: " + mssv);
        }

        // Summary
        if (response.getData().getSummary() != null) {
            double gpa = response.getData().getSummary().getGpaTichLuy();
            double credits = response.getData().getSummary().getTinChiTichLuy();
            tvGpaAccumulated.setText(String.format("%.2f", gpa));
            tvCreditAccumulated.setText(String.format("%.0f TC", credits));
        }

        // Semesters
        allSemesters = response.getData().getSemesters();
        if (allSemesters == null || allSemesters.isEmpty()) {
            tvEmptySubjects.setText("Không có dữ liệu điểm");
            tvEmptySubjects.setVisibility(android.view.View.VISIBLE);
            return;
        }

        // Fix years to 2024 and 2025
        years = new ArrayList<>(Arrays.asList(2024, 2025));

        // Setup year spinner
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void updateSemesterSpinner() {
        if (years.isEmpty() || spinnerYear.getSelectedItemPosition() < 0) return;

        int selectedYear = years.get(spinnerYear.getSelectedItemPosition());

        // Collect semesters for this year
        Set<Integer> semSet = new TreeSet<>();
        for (GradeSemester sem : allSemesters) {
            if (sem.getNamhoc() == selectedYear) {
                semSet.add(sem.getHocky());
            }
        }
        semesters = new ArrayList<>(semSet);

        ArrayAdapter<Integer> semAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semesters);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semAdapter);
    }

    private void showSelectedSemesterData() {
        if (years.isEmpty() || semesters.isEmpty()) return;
        if (spinnerYear.getSelectedItemPosition() < 0 || spinnerSemester.getSelectedItemPosition() < 0) return;

        int selectedYear = years.get(spinnerYear.getSelectedItemPosition());
        int selectedSemester = semesters.get(spinnerSemester.getSelectedItemPosition());

        // Find matching semester
        GradeSemester matchedSem = null;
        for (GradeSemester sem : allSemesters) {
            if (sem.getNamhoc() == selectedYear && sem.getHocky() == selectedSemester) {
                matchedSem = sem;
                break;
            }
        }

        if (matchedSem == null) return;

        // Show semester info
        findViewById(R.id.layoutSemesterInfo).setVisibility(android.view.View.VISIBLE);
        tvSemesterCredit.setText("TC: " + String.format("%.0f", matchedSem.getSoTinChi()));
        String dtb = matchedSem.getDiemTrungBinh();
        tvSemesterGpa.setText("ĐTB: " + (dtb != null ? dtb : "--"));

        // Show subjects
        List<GradeSubject> subjects = matchedSem.getSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            subjectAdapter.setSubjects(subjects);
            tvSubjectHeader.setVisibility(android.view.View.VISIBLE);
            rvSubjects.setVisibility(android.view.View.VISIBLE);
            tvEmptySubjects.setVisibility(android.view.View.GONE);
        } else {
            subjectAdapter.setSubjects(null);
            tvSubjectHeader.setVisibility(android.view.View.GONE);
            rvSubjects.setVisibility(android.view.View.GONE);
            tvEmptySubjects.setText("Không có môn học nào cho học kỳ này");
            tvEmptySubjects.setVisibility(android.view.View.VISIBLE);
        }
    }
}