package com.locallife.worker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.locallife.service.DataCollectionService;

/**
 * WorkManager worker for periodic data collection
 */
public class DataCollectionWorker extends Worker {
    private static final String TAG = "DataCollectionWorker";
    
    public static final String KEY_WORKER_TYPE = "worker_type";
    public static final String TYPE_PERIODIC_SYNC = "periodic_sync";
    public static final String TYPE_WEATHER_UPDATE = "weather_update";
    public static final String TYPE_DATA_CLEANUP = "data_cleanup";
    public static final String TYPE_SERVICE_MONITOR = "service_monitor";
    
    public DataCollectionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            String workerType = getInputData().getString(KEY_WORKER_TYPE);
            if (workerType == null) {
                workerType = TYPE_PERIODIC_SYNC;
            }
            
            Log.d(TAG, "Starting work: " + workerType);
            
            switch (workerType) {
                case TYPE_PERIODIC_SYNC:
                    return performPeriodicSync();
                case TYPE_WEATHER_UPDATE:
                    return performWeatherUpdate();
                case TYPE_DATA_CLEANUP:
                    return performDataCleanup();
                case TYPE_SERVICE_MONITOR:
                    return performServiceMonitor();
                default:
                    Log.w(TAG, "Unknown worker type: " + workerType);
                    return Result.failure();
            }
        } catch (Exception e) {
            Log.e(TAG, "Worker failed", e);
            return Result.retry();
        }
    }
    
    private Result performPeriodicSync() {
        try {
            Log.d(TAG, "Performing periodic data sync");
            
            // Trigger data collection service sync
            DataCollectionService.forceSyncData(getApplicationContext());
            
            // Check if services are running
            ensureServicesRunning();
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Periodic sync failed", e);
            return Result.retry();
        }
    }
    
    private Result performWeatherUpdate() {
        try {
            Log.d(TAG, "Performing weather update");
            
            // Weather updates are handled by DataCollectionService
            // We just need to ensure it's running
            ensureDataCollectionServiceRunning();
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Weather update failed", e);
            return Result.retry();
        }
    }
    
    private Result performDataCleanup() {
        try {
            Log.d(TAG, "Performing data cleanup");
            
            // Data cleanup is handled by DataCollectionService
            ensureDataCollectionServiceRunning();
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Data cleanup failed", e);
            return Result.retry();
        }
    }
    
    private Result performServiceMonitor() {
        try {
            Log.d(TAG, "Monitoring services");
            
            // Check and restart services if needed
            ensureServicesRunning();
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Service monitoring failed", e);
            return Result.retry();
        }
    }
    
    private void ensureServicesRunning() {
        Context context = getApplicationContext();
        
        // Start DataCollectionService if not running
        ensureDataCollectionServiceRunning();
        
        // The DataCollectionService will handle starting other services
    }
    
    private void ensureDataCollectionServiceRunning() {
        Context context = getApplicationContext();
        
        try {
            Intent serviceIntent = new Intent(context, DataCollectionService.class);
            context.startForegroundService(serviceIntent);
            Log.d(TAG, "DataCollectionService started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start DataCollectionService", e);
        }
    }
}