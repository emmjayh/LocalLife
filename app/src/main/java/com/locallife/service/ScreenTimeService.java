package com.locallife.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for tracking screen time and app usage using UsageStatsManager
 */
public class ScreenTimeService extends Service {
    private static final String TAG = "ScreenTimeService";
    private static final String CHANNEL_ID = "screen_time_channel";
    private static final int NOTIFICATION_ID = 1003;
    
    private static final long UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutes
    private static final long SAVE_INTERVAL = 15 * 60 * 1000; // 15 minutes
    
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private DatabaseHelper databaseHelper;
    private NotificationManager notificationManager;
    private Handler mainHandler;
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService backgroundExecutor;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Screen time tracking
    private long dailyScreenTime = 0; // in milliseconds
    private int phoneUnlocks = 0;
    private Map<String, AppUsageData> appUsageMap = new HashMap<>();
    private long lastUpdateTime = 0;
    private String currentDate;
    
    // App usage data structure
    private static class AppUsageData {
        String packageName;
        String appName;
        long totalTime;
        long lastUsed;
        int sessions;
        
        AppUsageData(String packageName, String appName) {
            this.packageName = packageName;
            this.appName = appName;
            this.totalTime = 0;
            this.lastUsed = 0;
            this.sessions = 0;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ScreenTimeService created");
        
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = getPackageManager();
        databaseHelper = DatabaseHelper.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mainHandler = new Handler(Looper.getMainLooper());
        scheduledExecutor = Executors.newScheduledThreadPool(2);
        backgroundExecutor = Executors.newSingleThreadExecutor();
        
        currentDate = dateFormat.format(new Date());
        
        // Create notification channel
        createNotificationChannel();
        
        // Check if usage access permission is granted
        if (!hasUsageStatsPermission()) {
            Log.w(TAG, "Usage stats permission not granted");
            // Could show a notification to request permission
        }
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Start monitoring
        startScreenTimeMonitoring();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ScreenTimeService started");
        return START_STICKY;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Time Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks screen time and app usage");
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
        
        String screenTimeText = formatScreenTime(dailyScreenTime);
        String contentText = screenTimeText + " today, " + phoneUnlocks + " unlocks";
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen Time Tracking")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
    }
    
    private void startScreenTimeMonitoring() {
        if (!hasUsageStatsPermission()) {
            Log.w(TAG, "Cannot start monitoring without usage stats permission");
            return;
        }
        
        // Schedule periodic updates
        scheduledExecutor.scheduleAtFixedRate(this::updateScreenTimeData, 0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
        scheduledExecutor.scheduleAtFixedRate(this::saveScreenTimeData, 0, SAVE_INTERVAL, TimeUnit.MILLISECONDS);
        
        Log.d(TAG, "Screen time monitoring started");
    }
    
    private void updateScreenTimeData() {
        backgroundExecutor.execute(() -> {
            try {
                // Check if it's a new day
                String newDate = dateFormat.format(new Date());
                if (!newDate.equals(currentDate)) {
                    // Save previous day's data
                    saveScreenTimeData();
                    
                    // Reset for new day
                    currentDate = newDate;
                    dailyScreenTime = 0;
                    phoneUnlocks = 0;
                    appUsageMap.clear();
                }
                
                // Get usage stats for today
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startTime = calendar.getTimeInMillis();
                long endTime = System.currentTimeMillis();
                
                // Get usage stats
                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
                
                if (usageStatsList != null && !usageStatsList.isEmpty()) {
                    processUsageStats(usageStatsList);
                }
                
                // Update notification
                mainHandler.post(this::updateNotification);
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating screen time data", e);
            }
        });
    }
    
    private void processUsageStats(List<UsageStats> usageStatsList) {
        long totalScreenTime = 0;
        int totalUnlocks = 0;
        Map<String, AppUsageData> newAppUsageMap = new HashMap<>();
        
        for (UsageStats stats : usageStatsList) {
            if (stats.getTotalTimeInForeground() > 0) {
                String packageName = stats.getPackageName();
                
                // Skip system apps and launcher
                if (isSystemApp(packageName) || isLauncher(packageName)) {
                    continue;
                }
                
                String appName = getAppName(packageName);
                if (appName == null) {
                    appName = packageName;
                }
                
                AppUsageData appUsage = new AppUsageData(packageName, appName);
                appUsage.totalTime = stats.getTotalTimeInForeground();
                appUsage.lastUsed = stats.getLastTimeUsed();
                
                // Try to get session count (available on some Android versions)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // This is not directly available, but we can estimate
                    appUsage.sessions = estimateSessionCount(stats);
                }
                
                newAppUsageMap.put(packageName, appUsage);
                totalScreenTime += stats.getTotalTimeInForeground();
                totalUnlocks += estimateUnlockCount(stats);
            }
        }
        
        // Update global stats
        dailyScreenTime = totalScreenTime;
        phoneUnlocks = totalUnlocks;
        appUsageMap = newAppUsageMap;
        lastUpdateTime = System.currentTimeMillis();
        
        Log.d(TAG, "Updated screen time: " + formatScreenTime(totalScreenTime) + 
                   ", unlocks: " + totalUnlocks + ", apps: " + appUsageMap.size());
    }
    
    private int estimateSessionCount(UsageStats stats) {
        // This is an estimation based on time patterns
        // A more accurate approach would require additional monitoring
        long totalTime = stats.getTotalTimeInForeground();
        if (totalTime < 60000) { // Less than 1 minute
            return 1;
        } else if (totalTime < 300000) { // Less than 5 minutes
            return 2;
        } else {
            return (int) (totalTime / 180000) + 1; // Estimate based on 3-minute sessions
        }
    }
    
    private int estimateUnlockCount(UsageStats stats) {
        // This is a rough estimation
        // Real unlock counting would require additional system monitoring
        return estimateSessionCount(stats);
    }
    
    private boolean isSystemApp(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    private boolean isLauncher(String packageName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<android.content.pm.ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        
        for (android.content.pm.ResolveInfo resolveInfo : resolveInfos) {
            if (packageName.equals(resolveInfo.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
    
    private String getAppName(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    
    private void saveScreenTimeData() {
        backgroundExecutor.execute(() -> {
            try {
                // Save to database
                DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
                
                if (dayRecord == null) {
                    dayRecord = new DayRecord();
                    dayRecord.setDate(currentDate);
                }
                
                // Update screen time data
                dayRecord.setScreenTimeMinutes((int) (dailyScreenTime / 60000));
                dayRecord.setPhoneUnlocks(phoneUnlocks);
                
                // Recalculate activity score
                dayRecord.calculateActivityScore();
                
                if (dayRecord.getId() > 0) {
                    databaseHelper.updateDayRecord(dayRecord);
                } else {
                    databaseHelper.insertDayRecord(dayRecord);
                }
                
                // Save individual app usage data
                for (AppUsageData appUsage : appUsageMap.values()) {
                    databaseHelper.insertScreenTimeData(
                            currentDate,
                            appUsage.appName,
                            appUsage.packageName,
                            (int) (appUsage.totalTime / 60000), // Convert to minutes
                            dateFormat.format(new Date(appUsage.lastUsed))
                    );
                }
                
                Log.d(TAG, "Screen time data saved to database");
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving screen time data", e);
            }
        });
    }
    
    private boolean hasUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Check if we have usage stats permission
                long now = System.currentTimeMillis();
                List<UsageStats> stats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, now - 1000, now);
                
                return stats != null && !stats.isEmpty();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    private String formatScreenTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format(Locale.US, "%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format(Locale.US, "%dm", minutes);
        } else {
            return String.format(Locale.US, "%ds", seconds);
        }
    }
    
    private void updateNotification() {
        Notification notification = createNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ScreenTimeService destroyed");
        
        // Save final data
        saveScreenTimeData();
        
        // Shutdown executors
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
        }
        
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Public methods for external access
    public long getDailyScreenTime() {
        return dailyScreenTime;
    }
    
    public int getPhoneUnlocks() {
        return phoneUnlocks;
    }
    
    public Map<String, AppUsageData> getAppUsageMap() {
        return new HashMap<>(appUsageMap);
    }
    
    public boolean isPermissionGranted() {
        return hasUsageStatsPermission();
    }
    
    public List<AppUsageData> getTopApps(int limit) {
        List<AppUsageData> apps = new ArrayList<>(appUsageMap.values());
        apps.sort((a, b) -> Long.compare(b.totalTime, a.totalTime));
        return apps.subList(0, Math.min(limit, apps.size()));
    }
    
    /**
     * Request usage stats permission
     */
    public static void requestUsageStatsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}