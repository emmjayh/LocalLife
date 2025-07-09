package com.locallife.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Background service that tracks daily steps using Android's built-in step counter sensor
 */
public class StepCounterService extends Service implements SensorEventListener {
    private static final String TAG = "StepCounterService";
    private static final String CHANNEL_ID = "step_counter_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private static final String PREF_NAME = "step_counter_prefs";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";
    private static final String KEY_DAILY_STEPS = "daily_steps";
    private static final String KEY_SENSOR_INITIAL_VALUE = "sensor_initial_value";
    private static final String KEY_LAST_SENSOR_VALUE = "last_sensor_value";
    
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private NotificationManager notificationManager;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private String currentDate;
    private int dailySteps = 0;
    private long sensorInitialValue = 0;
    private long lastSensorValue = 0;
    private boolean isInitialized = false;
    
    // Step counting variables
    private long lastStepTime = 0;
    private static final long STEP_DELAY_MS = 250; // Minimum time between steps
    private static final float STEP_THRESHOLD = 6.0f; // Threshold for step detection
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "StepCounterService created");
        
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        databaseHelper = DatabaseHelper.getInstance(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        currentDate = dateFormat.format(new Date());
        
        // Initialize sensors
        initializeSensors();
        
        // Create notification channel
        createNotificationChannel();
        
        // Load saved data
        loadSavedData();
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
    }
    
    private void initializeSensors() {
        // Try to use step counter sensor first (more accurate)
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        
        if (stepCounterSensor != null) {
            Log.d(TAG, "Step Counter sensor available");
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d(TAG, "Step Counter sensor not available, using Step Detector");
            // Fallback to step detector
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (stepDetectorSensor != null) {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No step sensors available");
            }
        }
    }
    
    private void loadSavedData() {
        String lastResetDate = sharedPreferences.getString(KEY_LAST_RESET_DATE, "");
        
        if (!currentDate.equals(lastResetDate)) {
            // New day - reset counters
            resetDailyCounters();
        } else {
            // Same day - load existing data
            dailySteps = sharedPreferences.getInt(KEY_DAILY_STEPS, 0);
            sensorInitialValue = sharedPreferences.getLong(KEY_SENSOR_INITIAL_VALUE, 0);
            lastSensorValue = sharedPreferences.getLong(KEY_LAST_SENSOR_VALUE, 0);
            isInitialized = sensorInitialValue > 0;
        }
    }
    
    private void resetDailyCounters() {
        Log.d(TAG, "Resetting daily counters for new day: " + currentDate);
        
        // Save previous day's data to database
        String previousDate = sharedPreferences.getString(KEY_LAST_RESET_DATE, "");
        if (!previousDate.isEmpty()) {
            saveDayRecordToDatabase(previousDate, sharedPreferences.getInt(KEY_DAILY_STEPS, 0));
        }
        
        // Reset counters
        dailySteps = 0;
        sensorInitialValue = 0;
        lastSensorValue = 0;
        isInitialized = false;
        
        // Update preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LAST_RESET_DATE, currentDate);
        editor.putInt(KEY_DAILY_STEPS, 0);
        editor.putLong(KEY_SENSOR_INITIAL_VALUE, 0);
        editor.putLong(KEY_LAST_SENSOR_VALUE, 0);
        editor.apply();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "StepCounterService started");
        return START_STICKY; // Restart service if killed
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleStepCounter(event);
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            handleStepDetector(event);
        }
    }
    
    private void handleStepCounter(SensorEvent event) {
        long currentSensorValue = (long) event.values[0];
        
        // Check if it's a new day
        String newDate = dateFormat.format(new Date());
        if (!newDate.equals(currentDate)) {
            currentDate = newDate;
            resetDailyCounters();
        }
        
        if (!isInitialized) {
            // First reading of the day
            sensorInitialValue = currentSensorValue;
            lastSensorValue = currentSensorValue;
            isInitialized = true;
            
            // Save initial value
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(KEY_SENSOR_INITIAL_VALUE, sensorInitialValue);
            editor.putLong(KEY_LAST_SENSOR_VALUE, lastSensorValue);
            editor.apply();
            
            Log.d(TAG, "Initialized step counter with value: " + sensorInitialValue);
        } else {
            // Calculate steps since last reading
            long stepsSinceLastReading = currentSensorValue - lastSensorValue;
            
            // Validate step increment (should be reasonable)
            if (stepsSinceLastReading > 0 && stepsSinceLastReading < 1000) {
                dailySteps = (int) (currentSensorValue - sensorInitialValue);
                lastSensorValue = currentSensorValue;
                
                // Save data
                saveStepData();
                
                // Update notification
                updateNotification();
                
                Log.d(TAG, "Steps updated: " + dailySteps + " (+" + stepsSinceLastReading + ")");
            } else {
                Log.w(TAG, "Invalid step increment: " + stepsSinceLastReading);
            }
        }
    }
    
    private void handleStepDetector(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        
        // Check if it's a new day
        String newDate = dateFormat.format(new Date());
        if (!newDate.equals(currentDate)) {
            currentDate = newDate;
            resetDailyCounters();
        }
        
        // Simple step detection with time-based filtering
        if (currentTime - lastStepTime > STEP_DELAY_MS) {
            dailySteps++;
            lastStepTime = currentTime;
            
            // Save data
            saveStepData();
            
            // Update notification
            updateNotification();
            
            Log.d(TAG, "Step detected. Total steps: " + dailySteps);
        }
    }
    
    private void saveStepData() {
        // Save to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_DAILY_STEPS, dailySteps);
        editor.putLong(KEY_LAST_SENSOR_VALUE, lastSensorValue);
        editor.apply();
        
        // Save to database every 100 steps or every 10 minutes
        if (dailySteps % 100 == 0 || shouldSaveToDatabase()) {
            saveDayRecordToDatabase(currentDate, dailySteps);
        }
    }
    
    private boolean shouldSaveToDatabase() {
        // Implement time-based saving logic
        return System.currentTimeMillis() % (10 * 60 * 1000) < 1000; // Approximately every 10 minutes
    }
    
    private void saveDayRecordToDatabase(String date, int steps) {
        try {
            DayRecord existingRecord = databaseHelper.getDayRecord(date);
            
            if (existingRecord != null) {
                // Update existing record
                existingRecord.setStepCount(steps);
                existingRecord.calculateActivityScore();
                databaseHelper.updateDayRecord(existingRecord);
            } else {
                // Create new record
                DayRecord dayRecord = new DayRecord();
                dayRecord.setDate(date);
                dayRecord.setStepCount(steps);
                dayRecord.calculateActivityScore();
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            // Also save to step data table
            databaseHelper.insertStepData(date, steps, "daily");
            
            Log.d(TAG, "Saved step data to database: " + steps + " steps for " + date);
        } catch (Exception e) {
            Log.e(TAG, "Error saving step data to database", e);
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Sensor accuracy changed: " + accuracy);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks your daily steps");
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
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter Active")
                .setContentText(dailySteps + " steps today")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
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
        Log.d(TAG, "StepCounterService destroyed");
        
        // Unregister sensor listeners
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        
        // Save final step count
        if (dailySteps > 0) {
            saveDayRecordToDatabase(currentDate, dailySteps);
        }
    }
    
    // Public methods for external access
    public int getDailySteps() {
        return dailySteps;
    }
    
    public boolean isStepCounterAvailable() {
        return stepCounterSensor != null || stepDetectorSensor != null;
    }
    
    public String getCurrentDate() {
        return currentDate;
    }
}