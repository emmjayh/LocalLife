package com.locallife.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.locallife.service.DataCollectionService;
import com.locallife.worker.WorkerManager;

/**
 * Broadcast receiver that starts services when device boots up
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action != null) {
            Log.d(TAG, "Received action: " + action);
            
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    handleBootCompleted(context);
                    break;
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                case Intent.ACTION_PACKAGE_REPLACED:
                    handlePackageReplaced(context);
                    break;
                default:
                    Log.w(TAG, "Unknown action: " + action);
            }
        }
    }
    
    private void handleBootCompleted(Context context) {
        Log.d(TAG, "Device boot completed - starting services");
        
        try {
            // Start DataCollectionService
            Intent serviceIntent = new Intent(context, DataCollectionService.class);
            context.startForegroundService(serviceIntent);
            
            // Schedule WorkManager tasks
            WorkerManager workerManager = new WorkerManager(context);
            workerManager.scheduleAllTasks();
            
            Log.d(TAG, "Services started successfully after boot");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting services after boot", e);
        }
    }
    
    private void handlePackageReplaced(Context context) {
        Log.d(TAG, "Package replaced - restarting services");
        
        try {
            // Restart DataCollectionService
            Intent serviceIntent = new Intent(context, DataCollectionService.class);
            context.startForegroundService(serviceIntent);
            
            // Reschedule WorkManager tasks
            WorkerManager workerManager = new WorkerManager(context);
            workerManager.restartAllTasks();
            
            Log.d(TAG, "Services restarted successfully after package replacement");
            
        } catch (Exception e) {
            Log.e(TAG, "Error restarting services after package replacement", e);
        }
    }
}