package com.example.uithub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uithub.api.RetrofitClient;
import com.example.uithub.models.LoginRequest;
import com.example.uithub.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private View progressBar;
    private PreferenceManager preferenceManager;
    private Call<ResponseBody> loginCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.getToken() != null) {
            startMainActivity();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        LoginRequest loginRequest = new LoginRequest(username, password);
        loginCall = RetrofitClient.getApiService().login(loginRequest);
        loginCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                setLoading(false);

                try {
                    String responseData = "";
                    if (response.body() != null) {
                        responseData = response.body().string();
                    } else if (response.errorBody() != null) {
                        responseData = response.errorBody().string();
                    }

                    if (response.isSuccessful()) {
                        String token = responseData.trim();
                        try {
                            if (token.startsWith("{")) {
                                JSONObject json = new JSONObject(token);
                                token = json.optString("access_token", json.optString("token", token));
                            } else if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
                                token = token.substring(1, token.length() - 1);
                            }
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Token parsing failed, using raw response", e);
                        }

                        if (token != null && !token.isEmpty()) {
                            preferenceManager.saveToken(token);
                            startMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, "Received empty token", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            JSONArray detailArr = jsonObject.optJSONArray("detail");
                            String errorMsg = "Login failed";
                            if (detailArr != null && detailArr.length() > 0) {
                                errorMsg = detailArr.getJSONObject(0).optString("msg", "Login failed");
                            } else {
                                errorMsg = jsonObject.optString("detail", "Login failed");
                            }
                            String finalErrorMsg = errorMsg;
                            Toast.makeText(LoginActivity.this, finalErrorMsg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "Login failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                setLoading(false);

                if (call.isCanceled()) {
                    return;
                }

                if (t instanceof SocketTimeoutException) {
                    Toast.makeText(LoginActivity.this, "Login is taking too long. Please try again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in..." : "Login");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        if (loginCall != null) {
            loginCall.cancel();
        }
        super.onDestroy();
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
