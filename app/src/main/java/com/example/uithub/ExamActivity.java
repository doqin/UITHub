package com.example.uithub;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uithub.adapter.ExamAdapter;
import com.example.uithub.api.ApiService;
import com.example.uithub.models.ExamModel;
import com.example.uithub.models.ExamScheduleResponse;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.utils.PreferenceManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamActivity extends BaseActivity {
    private RecyclerView rvExam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        rvExam = findViewById(R.id.rvExam);
        rvExam.setLayoutManager(new LinearLayoutManager(this));

        fetchExamData();
    }

    private void fetchExamData() {
        String token = new PreferenceManager(this).getToken();

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.getExamSchedule("Bearer " + token, 1, 1, 2025).enqueue(new Callback<ExamScheduleResponse>() {
            @Override
            public void onResponse(Call<ExamScheduleResponse> call, Response<ExamScheduleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ExamModel> list = response.body().getData();
                    ExamAdapter adapter = new ExamAdapter(list);
                    rvExam.setAdapter(adapter);
                } else {
                    Toast.makeText(ExamActivity.this, "Không tải được lịch thi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ExamScheduleResponse> call, Throwable t) {
                Toast.makeText(ExamActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}