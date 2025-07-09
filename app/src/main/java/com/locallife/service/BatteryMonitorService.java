package com.locallife.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for monitoring battery usage, charging patterns, and power states
 */
public class BatteryMonitorService extends Service {
    private static final String TAG = "BatteryMonitorService";
    private static final String CHANNEL_ID = "battery_monitor_channel";
    private static final int NOTIFICATION_ID = 1004;
    
    private static final long MONITOR_INTERVAL = 60 * 1000; // 1 minute
    private static final long SAVE_INTERVAL = 10 * 60 * 1000; // 10 minutes
    
    private BatteryManager batteryManager;
    private PowerManager powerManager;
    private DatabaseHelper databaseHelper;
    private NotificationManager notificationManager;
    private Handler mainHandler;
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService backgroundExecutor;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private String currentDate;
    
    // Battery monitoring variables
    private List<BatteryReading> batteryReadings = new ArrayList<>();
    private boolean isCharging = false;
    private boolean wasCharging = false;
    private long chargingStartTime = 0;
    private long totalChargingTime = 0;
    private int batteryLevel = 0;
    private int minBatteryLevel = 100;
    private int maxBatteryLevel = 0;
    private String batteryHealth = "Unknown";
    private float batteryTemperature = 0.0f;
    private int batteryVoltage = 0;
    private String batteryTechnology = "Unknown";
    
    // Usage patterns
    private int chargingCycles = 0;
    private long lastFullChargeTime = 0;
    private long screenOnTime = 0;
    private long screenOffTime = 0;
    private boolean isScreenOn = false;
    
    // Battery receiver for real-time updates
    private BatteryReceiver batteryReceiver;
    
    // Battery reading data structure
    private static class BatteryReading {
        long timestamp;
        int level;
        boolean charging;
        float temperature;
        int voltage;
        String health;
        
        BatteryReading(long timestamp, int level, boolean charging, float temperature, int voltage, String health) {
            this.timestamp = timestamp;
            this.level = level;
            this.charging = charging;
            this.temperature = temperature;
            this.voltage = voltage;
            this.health = health;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BatteryMonitorService created");
        
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        databaseHelper = DatabaseHelper.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());
        scheduledExecutor = Executors.newScheduledThreadPool(2);
        backgroundExecutor = Executors.newSingleThreadExecutor();
        
        currentDate = dateFormat.format(new Date());
        
        // Create notification channel
        createNotificationChannel();
        
        // Initialize battery receiver
        batteryReceiver = new BatteryReceiver();
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Start monitoring
        startBatteryMonitoring();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BatteryMonitorService started");
        return START_STICKY;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Battery Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors battery usage and charging patterns");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String statusText = batteryLevel + "% " + (isCharging ? "Charging" : "Discharging");
        String detailText = "Cycles: " + chargingCycles + ", Health: " + batteryHealth;
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Battery Monitor")
                .setContentText(statusText)
                .setSubText(detailText)
                .setSmallIcon(android.R.drawable.ic_menu_preferences)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
    }
    
    private void startBatteryMonitoring() {
        // Register battery receiver
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        batteryFilter.addAction(Intent.ACTION_SCREEN_ON);
        batteryFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(batteryReceiver, batteryFilter);
        
        // Get initial battery state
        updateBatteryState();
        
        // Schedule periodic monitoring
        scheduledExecutor.scheduleAtFixedRate(this::monitorBattery, 0, MONITOR_INTERVAL, TimeUnit.MILLISECONDS);
        scheduledExecutor.scheduleAtFixedRate(this::saveBatteryData, 0, SAVE_INTERVAL, TimeUnit.MILLISECONDS);
        
        Log.d(TAG, "Battery monitoring started");
    }
    
    private void updateBatteryState() {
        backgroundExecutor.execute(() -> {
            try {
                // Get battery info
                IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = registerReceiver(null, batteryFilter);
                
                if (batteryStatus != null) {
                    // Battery level
                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    
                    if (level != -1 && scale != -1) {
                        batteryLevel = (int) ((level / (float) scale) * 100);
                        
                        // Track min/max levels
                        if (batteryLevel < minBatteryLevel) {
                            minBatteryLevel = batteryLevel;
                        }
                        if (batteryLevel > maxBatteryLevel) {
                            maxBatteryLevel = batteryLevel;
                        }
                    }
                    
                    // Charging status
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    wasCharging = isCharging;
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                status == BatteryManager.BATTERY_STATUS_FULL;
                    
                    // Track charging state changes
                    if (isCharging && !wasCharging) {
                        // Started charging
                        chargingStartTime = System.currentTimeMillis();
                        chargingCycles++;
                    } else if (!isCharging && wasCharging) {
                        // Stopped charging
                        if (chargingStartTime > 0) {
                            totalChargingTime += System.currentTimeMillis() - chargingStartTime;
                        }
                        if (status == BatteryManager.BATTERY_STATUS_FULL) {
                            lastFullChargeTime = System.currentTimeMillis();
                        }
                    }
                    
                    // Battery health
                    int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                    batteryHealth = getBatteryHealthString(health);
                    
                    // Temperature
                    int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                    if (temp != -1) {
                        batteryTemperature = temp / 10.0f; // Convert to Celsius
                    }
                    
                    // Voltage
                    batteryVoltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                    
                    // Technology
                    batteryTechnology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                    if (batteryTechnology == null) {
                        batteryTechnology = "Unknown";
                    }
                    
                    // Screen state
                    if (powerManager != null) {
                        boolean screenOn = powerManager.isInteractive();
                        if (screenOn != isScreenOn) {
                            isScreenOn = screenOn;
                            if (screenOn) {
                                screenOnTime = System.currentTimeMillis();
                            } else {
                                screenOffTime = System.currentTimeMillis();
                            }
                        }
                    }
                    
                    // Create battery reading
                    BatteryReading reading = new BatteryReading(
                            System.currentTimeMillis(),
                            batteryLevel,
                            isCharging,
                            batteryTemperature,
                            batteryVoltage,
                            batteryHealth
                    );
                    
                    batteryReadings.add(reading);
                    
                    // Limit readings to prevent memory issues
                    if (batteryReadings.size() > 1000) {
                        batteryReadings.remove(0);
                    }
                    
                    // Update notification
                    mainHandler.post(this::updateNotification);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating battery state", e);
            }
        });
    }
    
    private void monitorBattery() {
        updateBatteryState();
        
        // Check if it's a new day
        String newDate = dateFormat.format(new Date());
        if (!newDate.equals(currentDate)) {
            // Save previous day's data
            saveBatteryData();
            
            // Reset for new day
            currentDate = newDate;
            chargingCycles = 0;
            totalChargingTime = 0;
            minBatteryLevel = batteryLevel;
            maxBatteryLevel = batteryLevel;
            batteryReadings.clear();
        }
    }
    
    private void saveBatteryData() {
        backgroundExecutor.execute(() -> {
            try {
                // Calculate battery usage percentage
                float batteryUsage = calculateBatteryUsage();
                
                // Save to database
                DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
                
                if (dayRecord == null) {
                    dayRecord = new DayRecord();
                    dayRecord.setDate(currentDate);
                }
                
                // Update battery data
                dayRecord.setBatteryUsagePercent(batteryUsage);
                
                // Recalculate activity score
                dayRecord.calculateActivityScore();
                
                if (dayRecord.getId() > 0) {
                    databaseHelper.updateDayRecord(dayRecord);
                } else {
                    databaseHelper.insertDayRecord(dayRecord);
                }
                
                // Save detailed battery data
                databaseHelper.insertBatteryData(
                        currentDate,
                        batteryLevel,
                        isCharging,
                        batteryHealth
                );
                
                Log.d(TAG, "Battery data saved to database");
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving battery data", e);
            }
        });
    }
    
    private float calculateBatteryUsage() {
        if (batteryReadings.isEmpty()) {
            return 0.0f;
        }
        
        // Calculate based on battery level changes
        int levelDrop = maxBatteryLevel - minBatteryLevel;
        
        // Estimate usage based on level drop and charging time
        float usage = levelDrop;
        
        // Adjust for charging (battery usage is consumption, not charging)
        if (totalChargingTime > 0) {
            long totalTime = System.currentTimeMillis() - batteryReadings.get(0).timestamp;
            float chargingRatio = (float) totalChargingTime / totalTime;
            usage = usage * (1.0f - chargingRatio);
        }
        
        return Math.max(0, Math.min(100, usage));
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
    
    private void updateNotification() {
        Notification notification = createNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    // Broadcast receiver for battery events
    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (action != null) {
                switch (action) {
                    case Intent.ACTION_BATTERY_CHANGED:
                        updateBatteryState();
                        break;
                    case Intent.ACTION_POWER_CONNECTED:
                        Log.d(TAG, "Power connected");
                        updateBatteryState();
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED:
                        Log.d(TAG, "Power disconnected");
                        updateBatteryState();
                        break;
                    case Intent.ACTION_SCREEN_ON:
                        Log.d(TAG, "Screen on");
                        isScreenOn = true;
                        screenOnTime = System.currentTimeMillis();
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        Log.d(TAG, "Screen off");
                        isScreenOn = false;
                        screenOffTime = System.currentTimeMillis();
                        break;
                }
            }
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BatteryMonitorService destroyed");
        
        // Save final data
        saveBatteryData();
        
        // Unregister receiver
        if (batteryReceiver != null) {
            try {
                unregisterReceiver(batteryReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver already unregistered");
            }
        }
        
        // Shutdown executors
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
        }
        
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Public methods for external access
    public int getBatteryLevel() {
        return batteryLevel;
    }
    
    public boolean isCharging() {
        return isCharging;
    }
    
    public String getBatteryHealth() {
        return batteryHealth;
    }
    
    public float getBatteryTemperature() {
        return batteryTemperature;
    }
    
    public int getBatteryVoltage() {
        return batteryVoltage;
    }
    
    public String getBatteryTechnology() {
        return batteryTechnology;
    }
    
    public int getChargingCycles() {
        return chargingCycles;
    }
    
    public long getTotalChargingTime() {
        return totalChargingTime;
    }
    
    public long getLastFullChargeTime() {
        return lastFullChargeTime;
    }
    
    public List<BatteryReading> getBatteryReadings() {
        return new ArrayList<>(batteryReadings);
    }
    
    public BatteryStats getBatteryStats() {
        return new BatteryStats(
                batteryLevel,
                isCharging,
                batteryHealth,
                batteryTemperature,
                batteryVoltage,
                batteryTechnology,
                chargingCycles,
                totalChargingTime,
                minBatteryLevel,
                maxBatteryLevel,
                calculateBatteryUsage()
        );
    }
    
    // Battery stats data structure
    public static class BatteryStats {
        public final int currentLevel;
        public final boolean isCharging;
        public final String health;
        public final float temperature;
        public final int voltage;
        public final String technology;
        public final int chargingCycles;
        public final long totalChargingTime;
        public final int minLevel;
        public final int maxLevel;
        public final float usage;
        
        BatteryStats(int currentLevel, boolean isCharging, String health, float temperature,
                    int voltage, String technology, int chargingCycles, long totalChargingTime,
                    int minLevel, int maxLevel, float usage) {
            this.currentLevel = currentLevel;
            this.isCharging = isCharging;
            this.health = health;
            this.temperature = temperature;
            this.voltage = voltage;
            this.technology = technology;
            this.chargingCycles = chargingCycles;
            this.totalChargingTime = totalChargingTime;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.usage = usage;
        }
    }
}