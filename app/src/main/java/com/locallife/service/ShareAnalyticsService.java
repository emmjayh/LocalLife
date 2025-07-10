package com.locallife.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.ShareableContent;
import com.locallife.model.ShareableContent.SharePlatform;
import com.locallife.model.ShareableContent.ShareType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service for tracking and analyzing social sharing statistics
 */
public class ShareAnalyticsService {
    private static final String TAG = "ShareAnalyticsService";
    
    // Database table names
    private static final String TABLE_SHARE_EVENTS = "share_events";
    private static final String TABLE_SHARE_STATS = "share_stats";
    
    // Share events table columns
    private static final String COL_ID = "id";
    private static final String COL_SHARE_ID = "share_id";
    private static final String COL_SHARE_TYPE = "share_type";
    private static final String COL_PLATFORM = "platform";
    private static final String COL_STATUS = "status"; // attempt, success, error, cancel
    private static final String COL_CONTENT_TITLE = "content_title";
    private static final String COL_CONTENT_DATA = "content_data";
    private static final String COL_ERROR_MESSAGE = "error_message";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_DATE = "date";
    
    // Share stats table columns
    private static final String COL_STAT_ID = "stat_id";
    private static final String COL_STAT_DATE = "date";
    private static final String COL_STAT_PLATFORM = "platform";
    private static final String COL_STAT_TYPE = "share_type";
    private static final String COL_ATTEMPTS = "attempts";
    private static final String COL_SUCCESSES = "successes";
    private static final String COL_ERRORS = "errors";
    private static final String COL_CANCELS = "cancels";
    
    private Context context;
    private DatabaseHelper dbHelper;
    
    public ShareAnalyticsService(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        createTables();
    }
    
    /**
     * Create analytics tables if they don't exist
     */
    private void createTables() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Create share events table
        String createEventsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARE_EVENTS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_SHARE_ID + " TEXT, " +
            COL_SHARE_TYPE + " TEXT NOT NULL, " +
            COL_PLATFORM + " TEXT NOT NULL, " +
            COL_STATUS + " TEXT NOT NULL, " +
            COL_CONTENT_TITLE + " TEXT, " +
            COL_CONTENT_DATA + " TEXT, " +
            COL_ERROR_MESSAGE + " TEXT, " +
            COL_TIMESTAMP + " INTEGER NOT NULL, " +
            COL_DATE + " TEXT NOT NULL" +
            ")";
        
        // Create share stats table
        String createStatsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SHARE_STATS + " (" +
            COL_STAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_STAT_DATE + " TEXT NOT NULL, " +
            COL_STAT_PLATFORM + " TEXT NOT NULL, " +
            COL_STAT_TYPE + " TEXT NOT NULL, " +
            COL_ATTEMPTS + " INTEGER DEFAULT 0, " +
            COL_SUCCESSES + " INTEGER DEFAULT 0, " +
            COL_ERRORS + " INTEGER DEFAULT 0, " +
            COL_CANCELS + " INTEGER DEFAULT 0, " +
            "UNIQUE(" + COL_STAT_DATE + ", " + COL_STAT_PLATFORM + ", " + COL_STAT_TYPE + ")" +
            ")";
        
        db.execSQL(createEventsTable);
        db.execSQL(createStatsTable);
        
        // Create indexes for better performance
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_share_events_date ON " + 
            TABLE_SHARE_EVENTS + "(" + COL_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_share_events_platform ON " + 
            TABLE_SHARE_EVENTS + "(" + COL_PLATFORM + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_share_events_type ON " + 
            TABLE_SHARE_EVENTS + "(" + COL_SHARE_TYPE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_share_stats_date ON " + 
            TABLE_SHARE_STATS + "(" + COL_STAT_DATE + ")");
    }
    
    /**
     * Track share attempt
     */
    public void trackShareAttempt(ShareableContent content, SharePlatform platform) {
        recordShareEvent(content, platform, "attempt", null, null);
        updateDailyStats(platform, content.getShareType(), "attempt");
    }
    
    /**
     * Track successful share
     */
    public void trackShareSuccess(ShareableContent content, SharePlatform platform, String shareId) {
        recordShareEvent(content, platform, "success", shareId, null);
        updateDailyStats(platform, content.getShareType(), "success");
    }
    
    /**
     * Track share error
     */
    public void trackShareError(ShareableContent content, SharePlatform platform, Exception error) {
        String errorMessage = error != null ? error.getMessage() : "Unknown error";
        recordShareEvent(content, platform, "error", null, errorMessage);
        updateDailyStats(platform, content.getShareType(), "error");
    }
    
    /**
     * Track share cancel
     */
    public void trackShareCancel(SharePlatform platform) {
        recordShareEvent(null, platform, "cancel", null, null);
        updateDailyStats(platform, null, "cancel");
    }
    
    /**
     * Record individual share event
     */
    private void recordShareEvent(ShareableContent content, SharePlatform platform, 
                                String status, String shareId, String errorMessage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COL_SHARE_ID, shareId);
        values.put(COL_SHARE_TYPE, content != null ? content.getShareType().name() : "UNKNOWN");
        values.put(COL_PLATFORM, platform.name());
        values.put(COL_STATUS, status);
        values.put(COL_CONTENT_TITLE, content != null ? content.getTitle() : null);
        values.put(COL_CONTENT_DATA, content != null ? content.getAdditionalData() : null);
        values.put(COL_ERROR_MESSAGE, errorMessage);
        values.put(COL_TIMESTAMP, System.currentTimeMillis());
        values.put(COL_DATE, getCurrentDate());
        
        try {
            db.insert(TABLE_SHARE_EVENTS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error recording share event", e);
        }
    }
    
    /**
     * Update daily statistics
     */
    private void updateDailyStats(SharePlatform platform, ShareType shareType, String eventType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        String date = getCurrentDate();
        String type = shareType != null ? shareType.name() : "UNKNOWN";
        
        // Check if record exists
        String query = "SELECT * FROM " + TABLE_SHARE_STATS + " WHERE " +
            COL_STAT_DATE + " = ? AND " + COL_STAT_PLATFORM + " = ? AND " + COL_STAT_TYPE + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{date, platform.name(), type});
        
        ContentValues values = new ContentValues();
        values.put(COL_STAT_DATE, date);
        values.put(COL_STAT_PLATFORM, platform.name());
        values.put(COL_STAT_TYPE, type);
        
        if (cursor.moveToFirst()) {
            // Update existing record
            int attempts = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ATTEMPTS));
            int successes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUCCESSES));
            int errors = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ERRORS));
            int cancels = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CANCELS));
            
            switch (eventType) {
                case "attempt":
                    attempts++;
                    break;
                case "success":
                    successes++;
                    break;
                case "error":
                    errors++;
                    break;
                case "cancel":
                    cancels++;
                    break;
            }
            
            values.put(COL_ATTEMPTS, attempts);
            values.put(COL_SUCCESSES, successes);
            values.put(COL_ERRORS, errors);
            values.put(COL_CANCELS, cancels);
            
            String whereClause = COL_STAT_DATE + " = ? AND " + COL_STAT_PLATFORM + " = ? AND " + COL_STAT_TYPE + " = ?";
            db.update(TABLE_SHARE_STATS, values, whereClause, new String[]{date, platform.name(), type});
        } else {
            // Insert new record
            switch (eventType) {
                case "attempt":
                    values.put(COL_ATTEMPTS, 1);
                    break;
                case "success":
                    values.put(COL_SUCCESSES, 1);
                    break;
                case "error":
                    values.put(COL_ERRORS, 1);
                    break;
                case "cancel":
                    values.put(COL_CANCELS, 1);
                    break;
            }
            
            db.insert(TABLE_SHARE_STATS, null, values);
        }
        
        cursor.close();
    }
    
    /**
     * Get overall share statistics
     */
    public Map<String, Object> getShareStatistics() {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Get total counts
        String totalQuery = "SELECT " +
            "SUM(" + COL_ATTEMPTS + ") as total_attempts, " +
            "SUM(" + COL_SUCCESSES + ") as total_successes, " +
            "SUM(" + COL_ERRORS + ") as total_errors, " +
            "SUM(" + COL_CANCELS + ") as total_cancels " +
            "FROM " + TABLE_SHARE_STATS;
        
        Cursor cursor = db.rawQuery(totalQuery, null);
        if (cursor.moveToFirst()) {
            stats.put("total_attempts", cursor.getInt(0));
            stats.put("total_successes", cursor.getInt(1));
            stats.put("total_errors", cursor.getInt(2));
            stats.put("total_cancels", cursor.getInt(3));
            
            int totalAttempts = cursor.getInt(0);
            int totalSuccesses = cursor.getInt(1);
            stats.put("success_rate", totalAttempts > 0 ? (double) totalSuccesses / totalAttempts : 0.0);
        }
        cursor.close();
        
        // Get platform breakdown
        Map<String, Map<String, Integer>> platformStats = new HashMap<>();
        String platformQuery = "SELECT " + COL_STAT_PLATFORM + ", " +
            "SUM(" + COL_ATTEMPTS + ") as attempts, " +
            "SUM(" + COL_SUCCESSES + ") as successes " +
            "FROM " + TABLE_SHARE_STATS + " GROUP BY " + COL_STAT_PLATFORM;
        
        cursor = db.rawQuery(platformQuery, null);
        while (cursor.moveToNext()) {
            String platform = cursor.getString(0);
            Map<String, Integer> platformData = new HashMap<>();
            platformData.put("attempts", cursor.getInt(1));
            platformData.put("successes", cursor.getInt(2));
            platformStats.put(platform, platformData);
        }
        cursor.close();
        
        stats.put("platform_breakdown", platformStats);
        
        // Get share type breakdown
        Map<String, Map<String, Integer>> typeStats = new HashMap<>();
        String typeQuery = "SELECT " + COL_STAT_TYPE + ", " +
            "SUM(" + COL_ATTEMPTS + ") as attempts, " +
            "SUM(" + COL_SUCCESSES + ") as successes " +
            "FROM " + TABLE_SHARE_STATS + " GROUP BY " + COL_STAT_TYPE;
        
        cursor = db.rawQuery(typeQuery, null);
        while (cursor.moveToNext()) {
            String type = cursor.getString(0);
            Map<String, Integer> typeData = new HashMap<>();
            typeData.put("attempts", cursor.getInt(1));
            typeData.put("successes", cursor.getInt(2));
            typeStats.put(type, typeData);
        }
        cursor.close();
        
        stats.put("type_breakdown", typeStats);
        
        return stats;
    }
    
    /**
     * Get most popular sharing platform
     */
    public SharePlatform getMostPopularPlatform() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT " + COL_STAT_PLATFORM + ", SUM(" + COL_SUCCESSES + ") as total " +
            "FROM " + TABLE_SHARE_STATS + " GROUP BY " + COL_STAT_PLATFORM + " ORDER BY total DESC LIMIT 1";
        
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            String platformName = cursor.getString(0);
            cursor.close();
            
            try {
                return SharePlatform.valueOf(platformName);
            } catch (IllegalArgumentException e) {
                return SharePlatform.GENERIC;
            }
        }
        cursor.close();
        
        return SharePlatform.GENERIC;
    }
    
    /**
     * Get share history
     */
    public Map<String, Object> getShareHistory(int limit) {
        Map<String, Object> history = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_SHARE_EVENTS + 
            " ORDER BY " + COL_TIMESTAMP + " DESC LIMIT ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});
        
        List<Map<String, Object>> events = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
            event.put("share_id", cursor.getString(cursor.getColumnIndexOrThrow(COL_SHARE_ID)));
            event.put("share_type", cursor.getString(cursor.getColumnIndexOrThrow(COL_SHARE_TYPE)));
            event.put("platform", cursor.getString(cursor.getColumnIndexOrThrow(COL_PLATFORM)));
            event.put("status", cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
            event.put("content_title", cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT_TITLE)));
            event.put("error_message", cursor.getString(cursor.getColumnIndexOrThrow(COL_ERROR_MESSAGE)));
            event.put("timestamp", cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
            event.put("date", cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
            events.add(event);
        }
        cursor.close();
        
        history.put("events", events);
        history.put("total_count", events.size());
        
        return history;
    }
    
    /**
     * Get daily statistics for a specific date
     */
    public Map<String, Object> getDailyStats(String date) {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT " + COL_STAT_PLATFORM + ", " + COL_STAT_TYPE + ", " +
            COL_ATTEMPTS + ", " + COL_SUCCESSES + ", " + COL_ERRORS + ", " + COL_CANCELS +
            " FROM " + TABLE_SHARE_STATS + " WHERE " + COL_STAT_DATE + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{date});
        
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        int totalAttempts = 0, totalSuccesses = 0, totalErrors = 0, totalCancels = 0;
        
        while (cursor.moveToNext()) {
            Map<String, Object> statRecord = new HashMap<>();
            statRecord.put("platform", cursor.getString(0));
            statRecord.put("type", cursor.getString(1));
            statRecord.put("attempts", cursor.getInt(2));
            statRecord.put("successes", cursor.getInt(3));
            statRecord.put("errors", cursor.getInt(4));
            statRecord.put("cancels", cursor.getInt(5));
            
            totalAttempts += cursor.getInt(2);
            totalSuccesses += cursor.getInt(3);
            totalErrors += cursor.getInt(4);
            totalCancels += cursor.getInt(5);
            
            dailyStats.add(statRecord);
        }
        cursor.close();
        
        stats.put("date", date);
        stats.put("records", dailyStats);
        stats.put("total_attempts", totalAttempts);
        stats.put("total_successes", totalSuccesses);
        stats.put("total_errors", totalErrors);
        stats.put("total_cancels", totalCancels);
        stats.put("success_rate", totalAttempts > 0 ? (double) totalSuccesses / totalAttempts : 0.0);
        
        return stats;
    }
    
    /**
     * Get weekly statistics
     */
    public Map<String, Object> getWeeklyStats() {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Get last 7 days
        String query = "SELECT " + COL_STAT_DATE + ", " +
            "SUM(" + COL_ATTEMPTS + ") as attempts, " +
            "SUM(" + COL_SUCCESSES + ") as successes " +
            "FROM " + TABLE_SHARE_STATS + " WHERE " + COL_STAT_DATE + " >= date('now', '-7 days') " +
            "GROUP BY " + COL_STAT_DATE + " ORDER BY " + COL_STAT_DATE + " ASC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", cursor.getString(0));
            dayData.put("attempts", cursor.getInt(1));
            dayData.put("successes", cursor.getInt(2));
            weeklyData.add(dayData);
        }
        cursor.close();
        
        stats.put("weekly_data", weeklyData);
        
        return stats;
    }
    
    /**
     * Get monthly statistics
     */
    public Map<String, Object> getMonthlyStats() {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Get current month
        String query = "SELECT " + COL_STAT_PLATFORM + ", " +
            "SUM(" + COL_ATTEMPTS + ") as attempts, " +
            "SUM(" + COL_SUCCESSES + ") as successes " +
            "FROM " + TABLE_SHARE_STATS + " WHERE " + COL_STAT_DATE + " >= date('now', 'start of month') " +
            "GROUP BY " + COL_STAT_PLATFORM + " ORDER BY successes DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, Object> platformData = new HashMap<>();
            platformData.put("platform", cursor.getString(0));
            platformData.put("attempts", cursor.getInt(1));
            platformData.put("successes", cursor.getInt(2));
            monthlyData.add(platformData);
        }
        cursor.close();
        
        stats.put("monthly_data", monthlyData);
        
        return stats;
    }
    
    /**
     * Clear old analytics data
     */
    public void clearOldData(int daysToKeep) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        String cutoffDate = getCurrentDate();
        // This is a simplified approach - in reality you'd calculate the actual cutoff date
        
        try {
            db.delete(TABLE_SHARE_EVENTS, COL_DATE + " < date('now', '-" + daysToKeep + " days')", null);
            db.delete(TABLE_SHARE_STATS, COL_STAT_DATE + " < date('now', '-" + daysToKeep + " days')", null);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing old analytics data", e);
        }
    }
    
    /**
     * Get current date string
     */
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
    
    /**
     * Export analytics data
     */
    public Map<String, Object> exportAnalyticsData() {
        Map<String, Object> exportData = new HashMap<>();
        
        // Export all statistics
        exportData.put("overall_stats", getShareStatistics());
        exportData.put("weekly_stats", getWeeklyStats());
        exportData.put("monthly_stats", getMonthlyStats());
        exportData.put("share_history", getShareHistory(100));
        exportData.put("export_timestamp", System.currentTimeMillis());
        
        return exportData;
    }
    
    /**
     * Get sharing trends
     */
    public Map<String, Object> getSharingTrends() {
        Map<String, Object> trends = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Get trending platforms (last 30 days)
        String platformTrendQuery = "SELECT " + COL_STAT_PLATFORM + ", " +
            "SUM(" + COL_SUCCESSES + ") as successes, " +
            "AVG(" + COL_SUCCESSES + ") as avg_daily " +
            "FROM " + TABLE_SHARE_STATS + " WHERE " + COL_STAT_DATE + " >= date('now', '-30 days') " +
            "GROUP BY " + COL_STAT_PLATFORM + " ORDER BY successes DESC";
        
        Cursor cursor = db.rawQuery(platformTrendQuery, null);
        
        List<Map<String, Object>> platformTrends = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("platform", cursor.getString(0));
            trend.put("total_successes", cursor.getInt(1));
            trend.put("avg_daily", cursor.getDouble(2));
            platformTrends.add(trend);
        }
        cursor.close();
        
        trends.put("platform_trends", platformTrends);
        
        // Get trending content types
        String typeTrendQuery = "SELECT " + COL_STAT_TYPE + ", " +
            "SUM(" + COL_SUCCESSES + ") as successes " +
            "FROM " + TABLE_SHARE_STATS + " WHERE " + COL_STAT_DATE + " >= date('now', '-30 days') " +
            "GROUP BY " + COL_STAT_TYPE + " ORDER BY successes DESC";
        
        cursor = db.rawQuery(typeTrendQuery, null);
        
        List<Map<String, Object>> typeTrends = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("type", cursor.getString(0));
            trend.put("total_successes", cursor.getInt(1));
            typeTrends.add(trend);
        }
        cursor.close();
        
        trends.put("type_trends", typeTrends);
        
        return trends;
    }
}