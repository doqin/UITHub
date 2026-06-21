package com.example.uithub;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uithub.adapter.TuitionAdapter;
import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.TuitionItem;
import com.example.uithub.utils.PreferenceManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TuitionActivity extends AppCompatActivity {
    private RecyclerView rvTuition;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuition2);

        preferenceManager = new PreferenceManager(this);
        rvTuition = findViewById(R.id.rvTuition);
        rvTuition.setLayoutManager(new LinearLayoutManager(this));

        fetchTuitionData();
    }

    private void fetchTuitionData() {
        String token = preferenceManager.getToken();
        RetrofitClient.getApiService().getTuition("Bearer " + token, null, null)
                .enqueue(new Callback<List<TuitionItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<TuitionItem>> call, @NonNull Response<List<TuitionItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            rvTuition.setAdapter(new TuitionAdapter(response.body()));
                        } else {
                            Toast.makeText(TuitionActivity.this, "Không có dữ liệu học phí", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<TuitionItem>> call, @NonNull Throwable t) {
                        Toast.makeText(TuitionActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}