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
    private static final String KEY_GRADES_JSON = "grades_json";
    private static final String KEY_DEADLINES_JSON = "deadlines_json";
    private static final String KEY_ANNOUNCEMENTS_PREFIX = "announcements_json_";
    private static final String KEY_ANNOUNCEMENT_DETAIL_PREFIX = "announcement_detail_json_";
    private static final String KEY_DARK_MODE = "dark_mode";
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

    public void saveGradesJson(String gradesJson) {
        sharedPreferences.edit().putString(KEY_GRADES_JSON, gradesJson).apply();
    }

    public String getGradesJson() {
        return sharedPreferences.getString(KEY_GRADES_JSON, null);
    }

    public void saveDeadlinesJson(String deadlinesJson) {
        sharedPreferences.edit().putString(KEY_DEADLINES_JSON, deadlinesJson).apply();
    }

    public String getDeadlinesJson() {
        return sharedPreferences.getString(KEY_DEADLINES_JSON, null);
    }

    public void saveAnnouncementsJson(String topic, String announcementsJson) {
        sharedPreferences.edit()
                .putString(KEY_ANNOUNCEMENTS_PREFIX + normalizeCachePart(topic), announcementsJson)
                .apply();
    }

    public String getAnnouncementsJson(String topic) {
        return sharedPreferences.getString(KEY_ANNOUNCEMENTS_PREFIX + normalizeCachePart(topic), null);
    }

    public void saveAnnouncementDetailJson(String nodeId, String announcementJson) {
        sharedPreferences.edit()
                .putString(KEY_ANNOUNCEMENT_DETAIL_PREFIX + normalizeCachePart(nodeId), announcementJson)
                .apply();
    }

    public String getAnnouncementDetailJson(String nodeId) {
        return sharedPreferences.getString(KEY_ANNOUNCEMENT_DETAIL_PREFIX + normalizeCachePart(nodeId), null);
    }

    private String normalizeCachePart(String value) {
        if (value == null || value.isEmpty()) {
            return "all";
        }
        return value.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }

    public void saveCredentials(String username, String password) {
        sharedPreferences.edit()
                .putString("saved_username", username)
                .putString("saved_password", password)
                .putBoolean("remember_me", true)
                .apply();
    }

    public void clearCredentials() {
        sharedPreferences.edit()
                .remove("saved_username")
                .remove("saved_password")
                .putBoolean("remember_me", false)
                .apply();
    }

    public void clearSession() {
        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_MSSV)
                .apply();
    }

    public String getSavedUsername() {
        return sharedPreferences.getString("saved_username", "");
    }

    public String getSavedPassword() {
        return sharedPreferences.getString("saved_password", "");
    }

    public boolean isRememberMe() {
        return sharedPreferences.getBoolean("remember_me", false);
    }

    public void setDarkMode(boolean isDarkMode) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void clear() {
        boolean remember = isRememberMe();
        String user = getSavedUsername();
        String pass = getSavedPassword();

        sharedPreferences.edit().clear().commit();

        if (remember) {
            saveCredentials(user, pass);
        }
    }
}
