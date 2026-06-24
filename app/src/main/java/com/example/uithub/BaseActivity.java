package com.example.uithub;

import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uithub.api.RetrofitClient;
import com.example.uithub.utils.PreferenceManager;

public class BaseActivity extends AppCompatActivity {

    private void handleSessionExpired() {
        if (isFinishing() || isDestroyed()) return;

        new AlertDialog.Builder(this)
                .setTitle("Phiên đăng nhập hết hạn")
                .setMessage("Vui lòng đăng nhập lại để tiếp tục sử dụng.")
                .setCancelable(false)
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    PreferenceManager pref = new PreferenceManager(BaseActivity.this);
                    pref.clear();
                    Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RetrofitClient.setSessionListener(this::handleSessionExpired);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RetrofitClient.setSessionListener(null);
    }
}
