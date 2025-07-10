package com.locallife.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.locallife.model.NotificationPreference;
import com.locallife.service.SmartNotificationService.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * ML-based Notification Scheduler that optimizes notification timing
 * based on user behavior patterns and engagement metrics.
 */
public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";
    private static final String PREFS_NAME = "notification_scheduler";
    private static final String KEY_SCHEDULED_NOTIFICATIONS = "scheduled_notifications";
    
    private Context context;
    private AlarmManager alarmManager;
    private SharedPreferences preferences;
    private NotificationTimingModel timingModel;
    
    // Scheduled notifications tracking
    private Map<String, ScheduledNotificationData> scheduledNotifications;
    private Map<String, NotificationWindow> optimalWindows;
    
    // ML optimization parameters
    private static final float CONFIDENCE_THRESHOLD = 0.7f;
    private static final int MAX_DAILY_NOTIFICATIONS = 8;
    private static final long MIN_NOTIFICATION_INTERVAL = TimeUnit.HOURS.toMillis(2);
    
    public NotificationScheduler(Context context, NotificationTimingModel timingModel) {
        this.context = context;
        this.timingModel = timingModel;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.scheduledNotifications = new ConcurrentHashMap<>();
        this.optimalWindows = new ConcurrentHashMap<>();
        
        loadScheduledNotifications();
        initializeOptimalWindows();
    }
    
    private void loadScheduledNotifications() {
        try {
            String json = preferences.getString(KEY_SCHEDULED_NOTIFICATIONS, "{}");
            JSONObject data = new JSONObject(json);
            
            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject notifData = data.getJSONObject(key);
                ScheduledNotificationData scheduled = ScheduledNotificationData.fromJson(notifData);
                scheduledNotifications.put(key, scheduled);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading scheduled notifications", e);
        }
    }
    
    private void saveScheduledNotifications() {
        try {
            JSONObject data = new JSONObject();
            for (Map.Entry<String, ScheduledNotificationData> entry : scheduledNotifications.entrySet()) {
                data.put(entry.getKey(), entry.getValue().toJson());
            }
            
            preferences.edit()
                .putString(KEY_SCHEDULED_NOTIFICATIONS, data.toString())
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving scheduled notifications", e);
        }
    }
    
    private void initializeOptimalWindows() {
        // Initialize optimal time windows for each notification type
        for (NotificationType type : NotificationType.values()) {
            optimalWindows.put(type.name(), calculateOptimalWindow(type));
        }
    }
    
    private NotificationWindow calculateOptimalWindow(NotificationType type) {
        // Calculate optimal notification windows based on type
        NotificationWindow window = new NotificationWindow();
        
        switch (type) {
            case ACTIVITY_REMINDER:
                // Morning and evening activity reminders
                window.addTimeSlot(7, 9);   // Morning
                window.addTimeSlot(17, 19); // Evening
                break;
                
            case MOOD_CHECK:
                // Mid-morning and afternoon mood checks
                window.addTimeSlot(10, 11);
                window.addTimeSlot(15, 16);
                window.addTimeSlot(20, 21);
                break;
                
            case GOAL_PROGRESS:
                // End of day progress updates
                window.addTimeSlot(19, 21);
                break;
                
            case HEALTH_TIP:
                // Morning health tips
                window.addTimeSlot(8, 10);
                break;
                
            default:
                // Default windows for other types
                window.addTimeSlot(9, 21);
                break;
        }
        
        return window;
    }
    
    public void scheduleNotification(ScheduledNotification notification) {
        try {
            String notificationId = generateNotificationId(notification);
            
            // Check if we should schedule this notification
            if (!shouldScheduleNotification(notification)) {
                Log.d(TAG, "Skipping notification scheduling: " + notificationId);
                return;
            }
            
            // Get optimal scheduling time
            long scheduledTime = notification.getTiming().getScheduledTime();
            
            // Apply ML optimization
            scheduledTime = optimizeSchedulingTime(scheduledTime, notification);
            
            // Create pending intent
            Intent intent = new Intent(context, SmartNotificationService.class);
            intent.setAction("TRIGGER_NOTIFICATION");
            intent.putExtra("notification_id", notificationId);
            intent.putExtra("notification_type", notification.getType().name());
            intent.putExtra("notification_data", notification.toJson().toString());
            
            PendingIntent pendingIntent = PendingIntent.getService(
                context,
                notificationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Schedule with AlarmManager
            scheduleAlarm(scheduledTime, pendingIntent);
            
            // Track scheduled notification
            ScheduledNotificationData data = new ScheduledNotificationData(
                notificationId,
                notification,
                scheduledTime
            );
            scheduledNotifications.put(notificationId, data);
            saveScheduledNotifications();
            
            Log.d(TAG, "Scheduled notification: " + notificationId + " at " + new Date(scheduledTime));
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }
    
    private boolean shouldScheduleNotification(ScheduledNotification notification) {
        // Check daily notification limit
        if (getScheduledCountForToday() >= MAX_DAILY_NOTIFICATIONS) {
            return false;
        }
        
        // Check minimum interval between notifications
        long lastNotificationTime = getLastScheduledTime(notification.getType());
        if (System.currentTimeMillis() - lastNotificationTime < MIN_NOTIFICATION_INTERVAL) {
            return false;
        }
        
        // Check user preferences
        NotificationPreference pref = getUserPreference(notification.getType());
        if (!pref.isEnabled()) {
            return false;
        }
        
        // Check confidence score
        return notification.getTiming().getConfidenceScore() >= CONFIDENCE_THRESHOLD;
    }
    
    private long optimizeSchedulingTime(long proposedTime, ScheduledNotification notification) {
        // Get optimal window for this notification type
        NotificationWindow window = optimalWindows.get(notification.getType().name());
        
        // Check if proposed time is within optimal window
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(proposedTime);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        if (!window.isInWindow(hour)) {
            // Find nearest optimal time
            proposedTime = window.getNearestOptimalTime(proposedTime);
        }
        
        // Apply user-specific optimization
        proposedTime = applyUserPatternOptimization(proposedTime, notification);
        
        // Avoid clustering with other notifications
        proposedTime = avoidNotificationClustering(proposedTime);
        
        return proposedTime;
    }
    
    private long applyUserPatternOptimization(long time, ScheduledNotification notification) {
        // Get user engagement patterns
        Map<Integer, Float> engagementByHour = getUserEngagementPattern();
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        
        // Look for better engagement times within ±2 hours
        int bestHour = currentHour;
        float bestScore = engagementByHour.getOrDefault(currentHour, 0.5f);
        
        for (int offset = -2; offset <= 2; offset++) {
            int testHour = (currentHour + offset + 24) % 24;
            float score = engagementByHour.getOrDefault(testHour, 0.5f);
            
            if (score > bestScore && isAcceptableTime(testHour)) {
                bestHour = testHour;
                bestScore = score;
            }
        }
        
        if (bestHour != currentHour) {
            cal.set(Calendar.HOUR_OF_DAY, bestHour);
            return cal.getTimeInMillis();
        }
        
        return time;
    }
    
    private long avoidNotificationClustering(long time) {
        // Check for nearby scheduled notifications
        long windowStart = time - TimeUnit.MINUTES.toMillis(30);
        long windowEnd = time + TimeUnit.MINUTES.toMillis(30);
        
        List<Long> nearbyTimes = new ArrayList<>();
        for (ScheduledNotificationData data : scheduledNotifications.values()) {
            if (data.scheduledTime >= windowStart && data.scheduledTime <= windowEnd) {
                nearbyTimes.add(data.scheduledTime);
            }
        }
        
        if (!nearbyTimes.isEmpty()) {
            // Adjust time to avoid clustering
            Collections.sort(nearbyTimes);
            
            // Find gap in schedule
            long adjustedTime = findScheduleGap(nearbyTimes, time);
            if (adjustedTime != time) {
                Log.d(TAG, "Adjusted notification time to avoid clustering");
                return adjustedTime;
            }
        }
        
        return time;
    }
    
    private long findScheduleGap(List<Long> existingTimes, long proposedTime) {
        // Find a 30-minute gap for the new notification
        long gapSize = TimeUnit.MINUTES.toMillis(30);
        
        // Try times after existing notifications
        for (Long existing : existingTimes) {
            long candidateTime = existing + gapSize;
            if (isTimeSlotAvailable(candidateTime, existingTimes)) {
                return candidateTime;
            }
        }
        
        // Try before first notification
        if (!existingTimes.isEmpty()) {
            long candidateTime = existingTimes.get(0) - gapSize;
            if (candidateTime > System.currentTimeMillis() && isTimeSlotAvailable(candidateTime, existingTimes)) {
                return candidateTime;
            }
        }
        
        return proposedTime;
    }
    
    private boolean isTimeSlotAvailable(long time, List<Long> existingTimes) {
        long buffer = TimeUnit.MINUTES.toMillis(25);
        for (Long existing : existingTimes) {
            if (Math.abs(time - existing) < buffer) {
                return false;
            }
        }
        return true;
    }
    
    private void scheduleAlarm(long time, PendingIntent pendingIntent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            );
        }
    }
    
    public void rescheduleNotification(ScheduledNotification notification, NotificationTiming newTiming) {
        String notificationId = generateNotificationId(notification);
        
        // Cancel existing alarm
        cancelNotification(notificationId);
        
        // Update timing
        notification.updateTiming(newTiming);
        
        // Reschedule
        scheduleNotification(notification);
    }
    
    public void cancelNotification(String notificationId) {
        ScheduledNotificationData data = scheduledNotifications.get(notificationId);
        if (data != null) {
            // Cancel alarm
            Intent intent = new Intent(context, SmartNotificationService.class);
            PendingIntent pendingIntent = PendingIntent.getService(
                context,
                notificationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            alarmManager.cancel(pendingIntent);
            
            // Remove from tracking
            scheduledNotifications.remove(notificationId);
            saveScheduledNotifications();
        }
    }
    
    public List<ScheduledNotification> getScheduledNotifications() {
        List<ScheduledNotification> result = new ArrayList<>();
        for (ScheduledNotificationData data : scheduledNotifications.values()) {
            result.add(data.notification);
        }
        return result;
    }
    
    private int getScheduledCountForToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        
        int count = 0;
        for (ScheduledNotificationData data : scheduledNotifications.values()) {
            if (data.scheduledTime >= today.getTimeInMillis() && 
                data.scheduledTime < tomorrow.getTimeInMillis()) {
                count++;
            }
        }
        
        return count;
    }
    
    private long getLastScheduledTime(NotificationType type) {
        long lastTime = 0;
        for (ScheduledNotificationData data : scheduledNotifications.values()) {
            if (data.notification.getType() == type) {
                lastTime = Math.max(lastTime, data.scheduledTime);
            }
        }
        return lastTime;
    }
    
    private NotificationPreference getUserPreference(NotificationType type) {
        // Load user preferences from database
        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        NotificationPreference pref = new NotificationPreference();
        pref.setType(type);
        pref.setEnabled(prefs.getBoolean(type.name() + "_enabled", true));
        pref.setFrequency(prefs.getString(type.name() + "_frequency", "normal"));
        return pref;
    }
    
    private Map<Integer, Float> getUserEngagementPattern() {
        // This would be loaded from the ML model
        Map<Integer, Float> pattern = new HashMap<>();
        
        // Default pattern (would be replaced with actual user data)
        pattern.put(8, 0.7f);   // Morning
        pattern.put(9, 0.8f);
        pattern.put(12, 0.6f);  // Lunch
        pattern.put(15, 0.7f);  // Afternoon
        pattern.put(18, 0.8f);  // Evening
        pattern.put(20, 0.9f);  // Night
        
        return pattern;
    }
    
    private boolean isAcceptableTime(int hour) {
        // Don't schedule during typical sleep hours
        return hour >= 7 && hour <= 22;
    }
    
    private String generateNotificationId(ScheduledNotification notification) {
        return notification.getType().name() + "_" + System.currentTimeMillis();
    }
    
    // Helper classes
    private static class ScheduledNotificationData {
        String id;
        ScheduledNotification notification;
        long scheduledTime;
        
        ScheduledNotificationData(String id, ScheduledNotification notification, long scheduledTime) {
            this.id = id;
            this.notification = notification;
            this.scheduledTime = scheduledTime;
        }
        
        JSONObject toJson() {
            try {
                JSONObject json = new JSONObject();
                json.put("id", id);
                json.put("notification", notification.toJson());
                json.put("scheduled_time", scheduledTime);
                return json;
            } catch (Exception e) {
                return new JSONObject();
            }
        }
        
        static ScheduledNotificationData fromJson(JSONObject json) {
            try {
                return new ScheduledNotificationData(
                    json.getString("id"),
                    ScheduledNotification.fromJson(json.getJSONObject("notification")),
                    json.getLong("scheduled_time")
                );
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    private static class NotificationWindow {
        private List<TimeSlot> timeSlots = new ArrayList<>();
        
        void addTimeSlot(int startHour, int endHour) {
            timeSlots.add(new TimeSlot(startHour, endHour));
        }
        
        boolean isInWindow(int hour) {
            for (TimeSlot slot : timeSlots) {
                if (hour >= slot.start && hour < slot.end) {
                    return true;
                }
            }
            return false;
        }
        
        long getNearestOptimalTime(long currentTime) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(currentTime);
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            
            // Find nearest time slot
            int nearestHour = -1;
            int minDistance = Integer.MAX_VALUE;
            
            for (TimeSlot slot : timeSlots) {
                // Check start of slot
                int distance = Math.abs(currentHour - slot.start);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestHour = slot.start;
                }
                
                // Check middle of slot
                int middle = (slot.start + slot.end) / 2;
                distance = Math.abs(currentHour - middle);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestHour = middle;
                }
            }
            
            if (nearestHour != -1 && nearestHour != currentHour) {
                cal.set(Calendar.HOUR_OF_DAY, nearestHour);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                
                // If time is in the past, move to next day
                if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }
                
                return cal.getTimeInMillis();
            }
            
            return currentTime;
        }
        
        private static class TimeSlot {
            int start;
            int end;
            
            TimeSlot(int start, int end) {
                this.start = start;
                this.end = end;
            }
        }
    }
}

// Extension methods for ScheduledNotification
class ScheduledNotificationExtensions {
    static void updateTiming(ScheduledNotification notification, NotificationTiming newTiming) {
        // Update the notification's timing
        try {
            java.lang.reflect.Field timingField = notification.getClass().getDeclaredField("timing");
            timingField.setAccessible(true);
            timingField.set(notification, newTiming);
        } catch (Exception e) {
            Log.e("NotificationScheduler", "Error updating timing", e);
        }
    }
    
    static JSONObject toJson(ScheduledNotification notification) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", notification.getType().name());
            // Add other fields as needed
            return json;
        } catch (Exception e) {
            return new JSONObject();
        }
    }
    
    static ScheduledNotification fromJson(JSONObject json) {
        // Reconstruct notification from JSON
        return null; // Simplified for now
    }
}