package com.locallife.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.locallife.database.DatabaseHelper;

/**
 * Broadcast receiver for battery state changes
 */
public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action != null) {
            Log.d(TAG, "Received action: " + action);
            
            switch (action) {
                case Intent.ACTION_BATTERY_CHANGED:
                    handleBatteryChanged(context, intent);
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    handlePowerConnected(context);
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    handlePowerDisconnected(context);
                    break;
                default:
                    Log.w(TAG, "Unknown action: " + action);
            }
        }
    }
    
    private void handleBatteryChanged(Context context, Intent intent) {
        try {
            // Get battery information
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            
            if (level != -1 && scale != -1) {
                int batteryLevel = (int) ((level / (float) scale) * 100);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                   status == BatteryManager.BATTERY_STATUS_FULL;
                
                String batteryHealth = getBatteryHealthString(health);
                
                Log.d(TAG, "Battery: " + batteryLevel + "% " + (isCharging ? "Charging" : "Discharging") + 
                           " Health: " + batteryHealth);
                
                // Save to database
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
                String currentDate = java.text.DateFormat.getDateInstance().format(new java.util.Date());
                databaseHelper.insertBatteryData(currentDate, batteryLevel, isCharging, batteryHealth);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling battery change", e);
        }
    }
    
    private void handlePowerConnected(Context context) {
        Log.d(TAG, "Power connected - charging started");
        
        try {
            // Could trigger specific actions when charging starts
            // For now, just log the event
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling power connected", e);
        }
    }
    
    private void handlePowerDisconnected(Context context) {
        Log.d(TAG, "Power disconnected - charging stopped");
        
        try {
            // Could trigger specific actions when charging stops
            // For now, just log the event
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling power disconnected", e);
        }
    }
    
    private String getBatteryHealthString(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return "Overheat";
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return "Dead";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return "Unspecified Failure";
            case BatteryManager.BATTERY_HEALTH_COLD:
                return "Cold";
            default:
                return "Unknown";
        }
    }
}