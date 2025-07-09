package com.locallife.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "LocalLifePrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_LOCATION_ENABLED = "location_enabled";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_FIRST_TIME = "first_time";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // User preferences
    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public void setUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public void setUserEmail(String userEmail) {
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    // App preferences
    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    public boolean isNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setLocationEnabled(boolean enabled) {
        editor.putBoolean(KEY_LOCATION_ENABLED, enabled);
        editor.apply();
    }

    public boolean isLocationEnabled() {
        return sharedPreferences.getBoolean(KEY_LOCATION_ENABLED, true);
    }

    public void setDarkMode(boolean enabled) {
        editor.putBoolean(KEY_DARK_MODE, enabled);
        editor.apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setFirstTime(boolean firstTime) {
        editor.putBoolean(KEY_FIRST_TIME, firstTime);
        editor.apply();
    }

    public boolean isFirstTime() {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true);
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}