package com.locallife.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.WorkManager;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;
import com.locallife.service.WeatherService;
import com.locallife.service.PhotoMetadataService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Master service that coordinates all data collection services
 * Manages StepCounterService, LocationService, WeatherService, and other data sources
 */
public class DataCollectionService extends Service {
    private static final String TAG = "DataCollectionService";
    private static final String CHANNEL_ID = "data_collection_channel";
    private static final int NOTIFICATION_ID = 1000;
    
    // Service coordination intervals
    private static final long COORDINATION_INTERVAL = 5 * 60 * 1000; // 5 minutes
    private static final long WEATHER_UPDATE_INTERVAL = 30 * 60 * 1000; // 30 minutes
    private static final long DATA_SYNC_INTERVAL = 15 * 60 * 1000; // 15 minutes
    private static final long CLEANUP_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    
    // Preferences
    private static final String PREFS_NAME = "data_collection_prefs";
    private static final String KEY_LAST_SYNC = "last_sync_time";
    private static final String KEY_LAST_CLEANUP = "last_cleanup_time";
    private static final String KEY_SERVICE_ENABLED = "service_enabled";
    
    private DatabaseHelper databaseHelper;
    private NotificationManager notificationManager;
    private SharedPreferences preferences;
    private WeatherService weatherService;
    private PhotoMetadataService photoMetadataService;
    private Handler mainHandler;
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService backgroundExecutor;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Service states
    private boolean isRunning = false;
    private boolean stepCounterRunning = false;
    private boolean locationServiceRunning = false;
    private long lastWeatherUpdate = 0;
    private long lastDataSync = 0;
    
    // Statistics
    private int dataCollectionCycles = 0;
    private int weatherUpdates = 0;
    private int errorCount = 0;
    private String lastError = "";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DataCollectionService created");
        
        // Initialize components
        databaseHelper = DatabaseHelper.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        weatherService = new WeatherService(this);
        photoMetadataService = new PhotoMetadataService(this);
        mainHandler = new Handler(Looper.getMainLooper());
        scheduledExecutor = Executors.newScheduledThreadPool(3);
        backgroundExecutor = Executors.newFixedThreadPool(2);
        
        // Create notification channel
        createNotificationChannel();
        
        // Initialize service state
        isRunning = preferences.getBoolean(KEY_SERVICE_ENABLED, true);
        lastDataSync = preferences.getLong(KEY_LAST_SYNC, 0);
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Start coordination
        startDataCollection();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "DataCollectionService started");
        
        if (intent != null && intent.hasExtra("action")) {
            String action = intent.getStringExtra("action");
            handleAction(action);
        }
        
        return START_STICKY; // Restart if killed
    }
    
    private void handleAction(String action) {
        switch (action) {
            case "start_collection":
                startDataCollection();
                break;
            case "stop_collection":
                stopDataCollection();
                break;
            case "force_sync":
                forceSyncData();
                break;
            case "restart_services":
                restartServices();
                break;
            default:
                Log.w(TAG, "Unknown action: " + action);
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Data Collection",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Coordinates all data collection services");
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
        
        String statusText = isRunning ? "Active - " + dataCollectionCycles + " cycles" : "Stopped";
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Data Collection Service")
                .setContentText(statusText)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
    }
    
    private void startDataCollection() {
        if (isRunning) {
            Log.d(TAG, "Data collection already running");
            return;
        }
        
        isRunning = true;
        preferences.edit().putBoolean(KEY_SERVICE_ENABLED, true).apply();
        
        Log.d(TAG, "Starting data collection");
        
        // Start individual services
        startStepCounterService();
        startLocationService();
        
        // Schedule periodic tasks
        scheduleDataSynchronization();
        scheduleWeatherUpdates();
        schedulePhotoScanning();
        scheduleDataCleanup();
        scheduleServiceCoordination();
        
        updateNotification();
    }
    
    private void stopDataCollection() {
        if (!isRunning) {
            Log.d(TAG, "Data collection already stopped");
            return;
        }
        
        isRunning = false;
        preferences.edit().putBoolean(KEY_SERVICE_ENABLED, false).apply();
        
        Log.d(TAG, "Stopping data collection");
        
        // Stop individual services
        stopStepCounterService();
        stopLocationService();
        
        // Cancel scheduled tasks
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = Executors.newScheduledThreadPool(3);
        }
        
        updateNotification();
    }
    
    private void startStepCounterService() {
        try {
            Intent intent = new Intent(this, StepCounterService.class);
            startService(intent);
            stepCounterRunning = true;
            Log.d(TAG, "StepCounterService started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting StepCounterService", e);
            recordError("Failed to start StepCounterService: " + e.getMessage());
        }
    }
    
    private void stopStepCounterService() {
        try {
            Intent intent = new Intent(this, StepCounterService.class);
            stopService(intent);
            stepCounterRunning = false;
            Log.d(TAG, "StepCounterService stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping StepCounterService", e);
        }
    }
    
    private void startLocationService() {
        try {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
            locationServiceRunning = true;
            Log.d(TAG, "LocationService started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting LocationService", e);
            recordError("Failed to start LocationService: " + e.getMessage());
        }
    }
    
    private void stopLocationService() {
        try {
            Intent intent = new Intent(this, LocationService.class);
            stopService(intent);
            locationServiceRunning = false;
            Log.d(TAG, "LocationService stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping LocationService", e);
        }
    }
    
    private void scheduleDataSynchronization() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                syncAllData();
            }
        }, 0, DATA_SYNC_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void scheduleWeatherUpdates() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                updateWeatherData();
            }
        }, 0, WEATHER_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void schedulePhotoScanning() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                scanPhotos();
            }
        }, 0, WEATHER_UPDATE_INTERVAL, TimeUnit.MILLISECONDS); // Same interval as weather
    }
    
    private void scheduleDataCleanup() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                performDataCleanup();
            }
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void scheduleServiceCoordination() {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (isRunning) {
                coordinateServices();
            }
        }, 0, COORDINATION_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    private void syncAllData() {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Syncing all data");
                
                // Get or create today's record
                String today = dateFormat.format(new Date());
                DayRecord todayRecord = databaseHelper.getDayRecord(today);
                
                if (todayRecord == null) {
                    todayRecord = new DayRecord();
                    todayRecord.setDate(today);
                    databaseHelper.insertDayRecord(todayRecord);
                }
                
                // Sync data from various sources
                syncStepData(todayRecord);
                syncLocationData(todayRecord);
                syncBatteryData(todayRecord);
                syncScreenTimeData(todayRecord);
                syncPhotoData(todayRecord);
                
                // Recalculate activity score
                todayRecord.calculateActivityScore();
                
                // Update record
                databaseHelper.updateDayRecord(todayRecord);
                
                // Update sync time
                lastDataSync = System.currentTimeMillis();
                preferences.edit().putLong(KEY_LAST_SYNC, lastDataSync).apply();
                
                dataCollectionCycles++;
                Log.d(TAG, "Data sync completed - cycle " + dataCollectionCycles);
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing data", e);
                recordError("Data sync failed: " + e.getMessage());
            }
        });
    }
    
    private void syncStepData(DayRecord dayRecord) {
        try {
            // This would typically interface with StepCounterService
            // For now, we'll use a placeholder
            Log.d(TAG, "Syncing step data");
            
            // Update step count from StepCounterService
            // dayRecord.setStepCount(stepCounterService.getDailySteps());
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing step data", e);
        }
    }
    
    private void syncLocationData(DayRecord dayRecord) {
        try {
            Log.d(TAG, "Syncing location data");
            
            // Update location data from LocationService
            // dayRecord.setPlacesVisited(locationService.getPlacesDetected());
            // dayRecord.setTotalTravelDistance(locationService.getTotalDistanceTraveled());
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing location data", e);
        }
    }
    
    private void syncBatteryData(DayRecord dayRecord) {
        try {
            Log.d(TAG, "Syncing battery data");
            
            // Get battery information
            IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, batteryFilter);
            
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                
                if (level != -1 && scale != -1) {
                    float batteryPct = (level / (float) scale) * 100;
                    dayRecord.setBatteryUsagePercent(100 - batteryPct); // Usage is inverse of level
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing battery data", e);
        }
    }
    
    private void syncScreenTimeData(DayRecord dayRecord) {
        try {
            Log.d(TAG, "Syncing screen time data");
            
            // This would typically use UsageStatsManager
            // For now, we'll use a placeholder
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing screen time data", e);
        }
    }
    
    private void syncPhotoData(DayRecord dayRecord) {
        try {
            Log.d(TAG, "Syncing photo data");
            
            // Get photo count and activity score for today
            String today = dateFormat.format(new Date());
            int photoCount = databaseHelper.getPhotoCountForDate(today);
            
            // Calculate photo activity score
            photoMetadataService.getPhotoActivityScore(today, new PhotoMetadataService.ActivityScoreCallback() {
                @Override
                public void onSuccess(int score) {
                    dayRecord.setPhotoCount(photoCount);
                    dayRecord.setPhotoActivityScore(score);
                    Log.d(TAG, "Photo data synced - Count: " + photoCount + ", Score: " + score);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error getting photo activity score: " + error);
                    dayRecord.setPhotoCount(photoCount);
                    dayRecord.setPhotoActivityScore(0);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error syncing photo data", e);
        }
    }
    
    private void updateWeatherData() {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Updating weather data");
                
                // Get weather for current location
                weatherService.getCurrentLocationWeather(new WeatherService.WeatherCallback() {
                    @Override
                    public void onWeatherReceived(WeatherService.WeatherData weatherData) {
                        Log.d(TAG, "Weather data received: " + weatherData);
                        lastWeatherUpdate = System.currentTimeMillis();
                        weatherUpdates++;
                        
                        mainHandler.post(() -> updateNotification());
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Weather update failed: " + error);
                        recordError("Weather update failed: " + error);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating weather data", e);
                recordError("Weather update error: " + e.getMessage());
            }
        });
    }
    
    private void scanPhotos() {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Scanning photos");
                
                // Add photo metadata listener
                photoMetadataService.addListener(new PhotoMetadataService.PhotoMetadataListener() {
                    @Override
                    public void onPhotoProcessed(com.locallife.model.PhotoMetadata metadata) {
                        Log.d(TAG, "Photo processed: " + metadata.getPhotoPath());
                    }
                    
                    @Override
                    public void onBatchProcessed(int count) {
                        Log.d(TAG, "Photo batch processed: " + count + " photos");
                    }
                    
                    @Override
                    public void onScanComplete(int totalPhotos) {
                        Log.d(TAG, "Photo scan complete: " + totalPhotos + " photos processed");
                        mainHandler.post(() -> updateNotification());
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Photo scan error: " + error);
                        recordError("Photo scan failed: " + error);
                    }
                });
                
                // Start photo scan
                photoMetadataService.startPhotoScan();
                
            } catch (Exception e) {
                Log.e(TAG, "Error scanning photos", e);
                recordError("Photo scan error: " + e.getMessage());
            }
        });
    }
    
    private void performDataCleanup() {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Performing data cleanup");
                
                // Delete old records (keep last 90 days)
                databaseHelper.deleteOldRecords(90);
                
                // Clean up old photo metadata
                photoMetadataService.cleanupOldPhotoMetadata();
                
                // Update cleanup time
                preferences.edit().putLong(KEY_LAST_CLEANUP, System.currentTimeMillis()).apply();
                
                Log.d(TAG, "Data cleanup completed");
                
            } catch (Exception e) {
                Log.e(TAG, "Error during data cleanup", e);
                recordError("Data cleanup failed: " + e.getMessage());
            }
        });
    }
    
    private void coordinateServices() {
        try {
            Log.d(TAG, "Coordinating services");
            
            // Check if services are running and restart if needed
            if (!stepCounterRunning && isRunning) {
                Log.w(TAG, "StepCounterService not running, restarting");
                startStepCounterService();
            }
            
            if (!locationServiceRunning && isRunning) {
                Log.w(TAG, "LocationService not running, restarting");
                startLocationService();
            }
            
            // Update notification
            updateNotification();
            
        } catch (Exception e) {
            Log.e(TAG, "Error coordinating services", e);
            recordError("Service coordination failed: " + e.getMessage());
        }
    }
    
    private void forceSyncData() {
        Log.d(TAG, "Force syncing data");
        syncAllData();
    }
    
    private void restartServices() {
        Log.d(TAG, "Restarting all services");
        
        stopStepCounterService();
        stopLocationService();
        
        // Wait a moment
        mainHandler.postDelayed(() -> {
            startStepCounterService();
            startLocationService();
        }, 2000);
    }
    
    private void recordError(String error) {
        errorCount++;
        lastError = error;
        Log.e(TAG, "Error recorded: " + error);
        
        // Could send to crash reporting service here
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
        Log.d(TAG, "DataCollectionService destroyed");
        
        // Stop all services
        stopDataCollection();
        
        // Shutdown executors
        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
        }
        
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
        
        // Shutdown weather service
        if (weatherService != null) {
            weatherService.shutdown();
        }
        
        // Shutdown photo metadata service
        if (photoMetadataService != null) {
            photoMetadataService.shutdown();
        }
    }
    
    // Public methods for status checking
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isStepCounterRunning() {
        return stepCounterRunning;
    }
    
    public boolean isLocationServiceRunning() {
        return locationServiceRunning;
    }
    
    public int getDataCollectionCycles() {
        return dataCollectionCycles;
    }
    
    public int getWeatherUpdates() {
        return weatherUpdates;
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public String getLastError() {
        return lastError;
    }
    
    public long getLastDataSync() {
        return lastDataSync;
    }
    
    public long getLastWeatherUpdate() {
        return lastWeatherUpdate;
    }
    
    // Static methods for external control
    public static void startDataCollection(Context context) {
        Intent intent = new Intent(context, DataCollectionService.class);
        intent.putExtra("action", "start_collection");
        context.startService(intent);
    }
    
    public static void stopDataCollection(Context context) {
        Intent intent = new Intent(context, DataCollectionService.class);
        intent.putExtra("action", "stop_collection");
        context.startService(intent);
    }
    
    public static void forceSyncData(Context context) {
        Intent intent = new Intent(context, DataCollectionService.class);
        intent.putExtra("action", "force_sync");
        context.startService(intent);
    }
}