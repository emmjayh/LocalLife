package com.locallife.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Manages WorkManager tasks for background data collection
 */
public class WorkerManager {
    private static final String TAG = "WorkerManager";
    
    // Work request names
    private static final String WORK_PERIODIC_SYNC = "periodic_sync_work";
    private static final String WORK_WEATHER_UPDATE = "weather_update_work";
    private static final String WORK_DATA_CLEANUP = "data_cleanup_work";
    private static final String WORK_SERVICE_MONITOR = "service_monitor_work";
    
    // Work intervals
    private static final long SYNC_INTERVAL_MINUTES = 15;
    private static final long WEATHER_INTERVAL_MINUTES = 30;
    private static final long CLEANUP_INTERVAL_HOURS = 24;
    private static final long MONITOR_INTERVAL_MINUTES = 5;
    
    private final Context context;
    private final WorkManager workManager;
    
    public WorkerManager(Context context) {
        this.context = context.getApplicationContext();
        this.workManager = WorkManager.getInstance(this.context);
    }
    
    /**
     * Schedule all periodic background tasks
     */
    public void scheduleAllTasks() {
        Log.d(TAG, "Scheduling all background tasks");
        
        schedulePeriodicSync();
        scheduleWeatherUpdates();
        scheduleDataCleanup();
        scheduleServiceMonitoring();
    }
    
    /**
     * Schedule periodic data synchronization
     */
    public void schedulePeriodicSync() {
        Log.d(TAG, "Scheduling periodic sync task");
        
        Data inputData = new Data.Builder()
                .putString(DataCollectionWorker.KEY_WORKER_TYPE, DataCollectionWorker.TYPE_PERIODIC_SYNC)
                .build();
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build();
        
        PeriodicWorkRequest syncWork = new PeriodicWorkRequest.Builder(
                DataCollectionWorker.class,
                SYNC_INTERVAL_MINUTES,
                TimeUnit.MINUTES,
                5, // flex interval
                TimeUnit.MINUTES
        )
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag("data_collection")
                .addTag("periodic_sync")
                .build();
        
        workManager.enqueueUniquePeriodicWork(
                WORK_PERIODIC_SYNC,
                ExistingPeriodicWorkPolicy.REPLACE,
                syncWork
        );
    }
    
    /**
     * Schedule weather data updates
     */
    public void scheduleWeatherUpdates() {
        Log.d(TAG, "Scheduling weather update task");
        
        Data inputData = new Data.Builder()
                .putString(DataCollectionWorker.KEY_WORKER_TYPE, DataCollectionWorker.TYPE_WEATHER_UPDATE)
                .build();
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build();
        
        PeriodicWorkRequest weatherWork = new PeriodicWorkRequest.Builder(
                DataCollectionWorker.class,
                WEATHER_INTERVAL_MINUTES,
                TimeUnit.MINUTES,
                10, // flex interval
                TimeUnit.MINUTES
        )
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 60, TimeUnit.SECONDS)
                .addTag("data_collection")
                .addTag("weather")
                .build();
        
        workManager.enqueueUniquePeriodicWork(
                WORK_WEATHER_UPDATE,
                ExistingPeriodicWorkPolicy.REPLACE,
                weatherWork
        );
    }
    
    /**
     * Schedule data cleanup tasks
     */
    public void scheduleDataCleanup() {
        Log.d(TAG, "Scheduling data cleanup task");
        
        Data inputData = new Data.Builder()
                .putString(DataCollectionWorker.KEY_WORKER_TYPE, DataCollectionWorker.TYPE_DATA_CLEANUP)
                .build();
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(true)
                .build();
        
        PeriodicWorkRequest cleanupWork = new PeriodicWorkRequest.Builder(
                DataCollectionWorker.class,
                CLEANUP_INTERVAL_HOURS,
                TimeUnit.HOURS,
                2, // flex interval
                TimeUnit.HOURS
        )
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .addTag("data_collection")
                .addTag("cleanup")
                .build();
        
        workManager.enqueueUniquePeriodicWork(
                WORK_DATA_CLEANUP,
                ExistingPeriodicWorkPolicy.REPLACE,
                cleanupWork
        );
    }
    
    /**
     * Schedule service monitoring
     */
    public void scheduleServiceMonitoring() {
        Log.d(TAG, "Scheduling service monitoring task");
        
        Data inputData = new Data.Builder()
                .putString(DataCollectionWorker.KEY_WORKER_TYPE, DataCollectionWorker.TYPE_SERVICE_MONITOR)
                .build();
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build();
        
        PeriodicWorkRequest monitorWork = new PeriodicWorkRequest.Builder(
                DataCollectionWorker.class,
                MONITOR_INTERVAL_MINUTES,
                TimeUnit.MINUTES,
                2, // flex interval
                TimeUnit.MINUTES
        )
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .addTag("data_collection")
                .addTag("monitoring")
                .build();
        
        workManager.enqueueUniquePeriodicWork(
                WORK_SERVICE_MONITOR,
                ExistingPeriodicWorkPolicy.REPLACE,
                monitorWork
        );
    }
    
    /**
     * Cancel all scheduled tasks
     */
    public void cancelAllTasks() {
        Log.d(TAG, "Cancelling all background tasks");
        
        workManager.cancelUniqueWork(WORK_PERIODIC_SYNC);
        workManager.cancelUniqueWork(WORK_WEATHER_UPDATE);
        workManager.cancelUniqueWork(WORK_DATA_CLEANUP);
        workManager.cancelUniqueWork(WORK_SERVICE_MONITOR);
        
        // Also cancel by tag
        workManager.cancelAllWorkByTag("data_collection");
    }
    
    /**
     * Cancel specific task
     */
    public void cancelTask(String taskName) {
        Log.d(TAG, "Cancelling task: " + taskName);
        workManager.cancelUniqueWork(taskName);
    }
    
    /**
     * Check if tasks are scheduled
     */
    public boolean areTasksScheduled() {
        // This is a simplified check
        // In a real implementation, you'd check the WorkManager status
        return true;
    }
    
    /**
     * Restart all tasks
     */
    public void restartAllTasks() {
        Log.d(TAG, "Restarting all background tasks");
        cancelAllTasks();
        scheduleAllTasks();
    }
    
    /**
     * Get work manager instance
     */
    public WorkManager getWorkManager() {
        return workManager;
    }
}