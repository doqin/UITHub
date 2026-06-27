package com.example.uithub;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uithub.adapter.TuitionAdapter;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.TuitionItem;
import com.example.uithub.models.TuitionResponse;
import com.example.uithub.utils.PreferenceManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TuitionActivity extends BaseActivity {
    private RecyclerView rvTuition;
    private PreferenceManager preferenceManager;
    private TextView tvStudentName, tvStudentId, tvTotalPaid, tvRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuition2);

        preferenceManager = new PreferenceManager(this);
        rvTuition = findViewById(R.id.rvTuition);
        rvTuition.setLayoutManager(new LinearLayoutManager(this));

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        tvRemaining = findViewById(R.id.tvRemaining);

        fetchTuitionData();
    }

    private void fetchTuitionData() {
        String token = preferenceManager.getToken();
        RetrofitClient.getApiService().getTuition("Bearer " + token, null, null)
                .enqueue(new Callback<TuitionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TuitionResponse> call, @NonNull Response<TuitionResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            TuitionResponse tuitionResponse = response.body();

                            // Thiết lập thông tin sinh viên
                            if (tuitionResponse.getStudent() != null) {
                                tvStudentName.setText(tuitionResponse.getStudent().getName());
                                tvStudentId.setText("MSSV: " + tuitionResponse.getStudent().getStudentId());
                            }

                            // Thiết lập tổng hợp
                            if (tuitionResponse.getSummary() != null) {
                                tvTotalPaid.setText(String.format("%,d VNĐ", tuitionResponse.getSummary().getPaid()));
                                tvRemaining.setText(String.format("%,d VNĐ", tuitionResponse.getSummary().getRemaining()));
                            }

                            // Thiết lập danh sách học kỳ
                            List<TuitionItem> semesters = tuitionResponse.getSemesters();
                            if (semesters != null && !semesters.isEmpty()) {
                                rvTuition.setAdapter(new TuitionAdapter(semesters));
                            } else {
                                Toast.makeText(TuitionActivity.this, "Không có dữ liệu học phí", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(TuitionActivity.this, "Không có dữ liệu học phí", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TuitionResponse> call, @NonNull Throwable t) {
                        Toast.makeText(TuitionActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
