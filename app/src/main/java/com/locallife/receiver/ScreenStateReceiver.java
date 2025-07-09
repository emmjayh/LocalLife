package com.locallife.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Broadcast receiver for screen state changes (on/off/unlock)
 */
public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenStateReceiver";
    
    private static final String PREFS_NAME = "screen_state_prefs";
    private static final String KEY_SCREEN_ON_TIME = "screen_on_time";
    private static final String KEY_SCREEN_OFF_TIME = "screen_off_time";
    private static final String KEY_UNLOCK_COUNT = "unlock_count";
    private static final String KEY_LAST_UNLOCK_TIME = "last_unlock_time";
    private static final String KEY_CURRENT_DATE = "current_date";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action != null) {
            Log.d(TAG, "Received action: " + action);
            
            switch (action) {
                case Intent.ACTION_SCREEN_ON:
                    handleScreenOn(context);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    handleScreenOff(context);
                    break;
                case Intent.ACTION_USER_PRESENT:
                    handleUserPresent(context);
                    break;
                default:
                    Log.w(TAG, "Unknown action: " + action);
            }
        }
    }
    
    private void handleScreenOn(Context context) {
        Log.d(TAG, "Screen turned on");
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            String currentDate = getCurrentDate();
            
            // Check if it's a new day
            String savedDate = prefs.getString(KEY_CURRENT_DATE, "");
            if (!currentDate.equals(savedDate)) {
                // New day - reset counters
                prefs.edit()
                    .putString(KEY_CURRENT_DATE, currentDate)
                    .putInt(KEY_UNLOCK_COUNT, 0)
                    .apply();
            }
            
            // Save screen on time
            prefs.edit()
                .putLong(KEY_SCREEN_ON_TIME, currentTime)
                .apply();
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling screen on", e);
        }
    }
    
    private void handleScreenOff(Context context) {
        Log.d(TAG, "Screen turned off");
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            long screenOnTime = prefs.getLong(KEY_SCREEN_ON_TIME, 0);
            
            // Calculate session duration
            if (screenOnTime > 0) {
                long sessionDuration = currentTime - screenOnTime;
                Log.d(TAG, "Screen session duration: " + sessionDuration + "ms");
                
                // Could save session duration to database here
            }
            
            // Save screen off time
            prefs.edit()
                .putLong(KEY_SCREEN_OFF_TIME, currentTime)
                .apply();
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling screen off", e);
        }
    }
    
    private void handleUserPresent(Context context) {
        Log.d(TAG, "User present (unlocked)");
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long currentTime = System.currentTimeMillis();
            String currentDate = getCurrentDate();
            
            // Check if it's a new day
            String savedDate = prefs.getString(KEY_CURRENT_DATE, "");
            if (!currentDate.equals(savedDate)) {
                // New day - reset counters
                prefs.edit()
                    .putString(KEY_CURRENT_DATE, currentDate)
                    .putInt(KEY_UNLOCK_COUNT, 0)
                    .apply();
            }
            
            // Increment unlock count
            int unlockCount = prefs.getInt(KEY_UNLOCK_COUNT, 0) + 1;
            
            prefs.edit()
                .putInt(KEY_UNLOCK_COUNT, unlockCount)
                .putLong(KEY_LAST_UNLOCK_TIME, currentTime)
                .apply();
            
            Log.d(TAG, "Phone unlocked " + unlockCount + " times today");
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling user present", e);
        }
    }
    
    private String getCurrentDate() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
        return dateFormat.format(new java.util.Date());
    }
    
    // Static methods for external access
    public static int getTodayUnlockCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());
        String savedDate = prefs.getString(KEY_CURRENT_DATE, "");
        
        if (currentDate.equals(savedDate)) {
            return prefs.getInt(KEY_UNLOCK_COUNT, 0);
        }
        return 0;
    }
    
    public static long getLastUnlockTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_UNLOCK_TIME, 0);
    }
    
    public static boolean isScreenCurrentlyOn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long screenOnTime = prefs.getLong(KEY_SCREEN_ON_TIME, 0);
        long screenOffTime = prefs.getLong(KEY_SCREEN_OFF_TIME, 0);
        
        return screenOnTime > screenOffTime;
    }
}