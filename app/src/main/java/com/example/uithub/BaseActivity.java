package com.example.uithub;

import android.content.Intent;
import android.os.Handler;
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
                    // Defer navigation+finish to avoid TopResumedActivityChangeItem crash
                    // on Android 12+. The system may have queued transaction items for
                    // this activity that must be processed before finish() is called.
                    new Handler().post(() -> {
                        PreferenceManager pref = new PreferenceManager(BaseActivity.this);
                        pref.clear();
                        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
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
