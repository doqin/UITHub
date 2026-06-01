package com.example.caidat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnChangePassword, btnCancel, btnSave;
    private CheckBox cbNotification, cbDarkMode;
    private RadioGroup rgLanguage;
    private RadioButton rbVi, rbEn;
    private TextView btnPhucKhao, btnGiaHanHocPhi, btnNghiaVuQuanSu, btnNghiHoc, btnBaoLuu;

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "CaiDatPrefs";

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("CaiDatPrefs", Context.MODE_PRIVATE);
        String lang = prefs.getString("language", "vi");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = newBase.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        boolean isDarkOn = sharedPreferences.getBoolean("darkmode", false);
        if (isDarkOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        cbNotification = findViewById(R.id.cbNotification);
        cbDarkMode = findViewById(R.id.cbDarkMode);
        rgLanguage = findViewById(R.id.rgLanguage);
        rbVi = findViewById(R.id.rbVi);
        rbEn = findViewById(R.id.rbEn);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        btnPhucKhao = findViewById(R.id.btnPhucKhao);
        btnGiaHanHocPhi = findViewById(R.id.btnGiaHanHocPhi);
        btnNghiaVuQuanSu = findViewById(R.id.btnNghiaVuQuanSu);
        btnNghiHoc = findViewById(R.id.btnNghiHoc);
        btnBaoLuu = findViewById(R.id.btnBaoLuu);

        loadSettings();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPass = edtPassword.getText().toString();
                if (!newPass.isEmpty()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("password", newPass);
                    editor.apply();
                    Toast.makeText(MainActivity.this, getString(R.string.btn_change_password), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", edtUsername.getText().toString());
                editor.putString("password", edtPassword.getText().toString());
                editor.putBoolean("notification", cbNotification.isChecked());

                boolean currentDarkState = cbDarkMode.isChecked();
                editor.putBoolean("darkmode", currentDarkState);

                String currentLang = rbVi.isChecked() ? "vi" : "en";
                editor.putString("language", currentLang);
                editor.apply();

                if (currentDarkState) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                recreate();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSettings();
            }
        });

        View.OnClickListener stubListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        };
        btnPhucKhao.setOnClickListener(stubListener);
        btnGiaHanHocPhi.setOnClickListener(stubListener);
        btnNghiaVuQuanSu.setOnClickListener(stubListener);
        btnNghiHoc.setOnClickListener(stubListener);
        btnBaoLuu.setOnClickListener(stubListener);
    }

    private void loadSettings() {
        String savedUser = sharedPreferences.getString("username", "");
        String savedPass = sharedPreferences.getString("password", "");
        boolean isNotifOn = sharedPreferences.getBoolean("notification", true);
        boolean isDarkOn = sharedPreferences.getBoolean("darkmode", false);
        String savedLang = sharedPreferences.getString("language", "vi");

        edtUsername.setText(savedUser);
        edtPassword.setText(savedPass);
        cbNotification.setChecked(isNotifOn);
        cbDarkMode.setChecked(isDarkOn);

        if (savedLang.equals("vi")) {
            rbVi.setChecked(true);
        } else {
            rbEn.setChecked(true);
        }
    }
}