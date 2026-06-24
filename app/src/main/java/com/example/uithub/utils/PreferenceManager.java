package com.example.uithub.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "UITHubPrefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_MSSV = "mssv";
    private static final String KEY_SCHEDULE_JSON = "schedule_json";
    private static final String KEY_EXAM_SCHEDULE_JSON = "exam_schedule_json";
    private static final String KEY_TUITION_JSON = "tuition_json";
    private static final String KEY_TUITION_TIMESTAMP = "tuition_timestamp";
    private static final String KEY_PROFILE_JSON = "profile_json";
    private static final String KEY_PROFILE_TIMESTAMP = "profile_timestamp";
    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).commit();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void saveMssv(String mssv) {
        sharedPreferences.edit().putString(KEY_MSSV, mssv).apply();
    }

    public String getMssv() {
        return sharedPreferences.getString(KEY_MSSV, null);
    }

    public void saveScheduleJson(String scheduleJson) {
        sharedPreferences.edit().putString(KEY_SCHEDULE_JSON, scheduleJson).apply();
    }

    public String getScheduleJson() {
        return sharedPreferences.getString(KEY_SCHEDULE_JSON, null);
    }

    public void saveExamScheduleJson(String examScheduleJson) {
        sharedPreferences.edit().putString(KEY_EXAM_SCHEDULE_JSON, examScheduleJson).apply();
    }

    public String getExamScheduleJson() {
        return sharedPreferences.getString(KEY_EXAM_SCHEDULE_JSON, null);
    }

    public void saveTuitionJson(String tuitionJson) {
        sharedPreferences.edit().putString(KEY_TUITION_JSON, tuitionJson).apply();
        sharedPreferences.edit().putLong(KEY_TUITION_TIMESTAMP, System.currentTimeMillis()).apply();
    }

    public String getTuitionJson() {
        return sharedPreferences.getString(KEY_TUITION_JSON, null);
    }

    public long getTuitionTimestamp() {
        return sharedPreferences.getLong(KEY_TUITION_TIMESTAMP, 0);
    }

    public void saveProfileJson(String profileJson) {
        sharedPreferences.edit().putString(KEY_PROFILE_JSON, profileJson).apply();
        sharedPreferences.edit().putLong(KEY_PROFILE_TIMESTAMP, System.currentTimeMillis()).apply();
    }

    public String getProfileJson() {
        return sharedPreferences.getString(KEY_PROFILE_JSON, null);
    }

    public long getProfileTimestamp() {
        return sharedPreferences.getLong(KEY_PROFILE_TIMESTAMP, 0);
    }

    public void clear() {
        sharedPreferences.edit().clear().commit();
    }
}
