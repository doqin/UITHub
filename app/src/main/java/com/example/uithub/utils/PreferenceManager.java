package com.example.uithub.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "UITHubPrefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_SCHEDULE_JSON = "schedule_json";
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

    public void saveScheduleJson(String scheduleJson) {
        sharedPreferences.edit().putString(KEY_SCHEDULE_JSON, scheduleJson).apply();
    }

    public String getScheduleJson() {
        return sharedPreferences.getString(KEY_SCHEDULE_JSON, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().commit();
    }
}
