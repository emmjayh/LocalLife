package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;
import com.locallife.model.MoodEntry;

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

/**
 * Service for tracking and managing user mood entries
 */
public class MoodTrackingService {
    private static final String TAG = "MoodTrackingService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public MoodTrackingService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Save a mood entry
     */
    public void saveMoodEntry(MoodEntry moodEntry, MoodSaveCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                // Enrich mood entry with context
                enrichMoodEntryWithContext(moodEntry);
                
                // Check if entry already exists for today
                MoodEntry existingEntry = databaseHelper.getMoodEntryForDate(moodEntry.getDate());
                
                long id;
                if (existingEntry != null) {
                    // Update existing entry
                    moodEntry.setId(existingEntry.getId());
                    moodEntry.setCreatedAt(existingEntry.getCreatedAt());
                    moodEntry.setUpdatedAt(new Date());
                    id = databaseHelper.updateMoodEntry(moodEntry);
                } else {
                    // Insert new entry
                    id = databaseHelper.insertMoodEntry(moodEntry);
                    moodEntry.setId((int) id);
                }
                
                Log.d(TAG, "Saved mood entry: " + moodEntry.getFormattedMood() + " for " + moodEntry.getDate());
                
                if (callback != null) {
                    callback.onMoodSaved(moodEntry);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving mood entry", e);
                if (callback != null) {
                    callback.onError("Failed to save mood: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Enrich mood entry with contextual data
     */
    private void enrichMoodEntryWithContext(MoodEntry moodEntry) {
        try {
            // Get weather data for the day
            DayRecord dayRecord = databaseHelper.getDayRecord(moodEntry.getDate());
            if (dayRecord != null) {
                moodEntry.setWeatherCondition(dayRecord.getWeatherCondition());
                moodEntry.setTemperature(dayRecord.getTemperature());
                moodEntry.setLocation(dayRecord.getPrimaryLocation());
                
                // Infer exercise from activity data
                if (dayRecord.getSteps() > 8000 || dayRecord.getActivityScore() > 70) {
                    moodEntry.setHasExercised(true);
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Could not enrich mood entry with context", e);
        }
    }
    
    /**
     * Get mood entry for specific date
     */
    public void getMoodForDate(String date, MoodCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                MoodEntry moodEntry = databaseHelper.getMoodEntryForDate(date);
                
                if (callback != null) {
                    callback.onMoodReceived(moodEntry);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting mood for date", e);
                if (callback != null) {
                    callback.onError("Failed to get mood: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get mood entries for date range
     */
    public void getMoodEntriesForRange(String startDate, String endDate, MoodListCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<MoodEntry> moodEntries = databaseHelper.getMoodEntriesForDateRange(startDate, endDate);
                
                if (callback != null) {
                    callback.onMoodEntriesReceived(moodEntries);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting mood entries for range", e);
                if (callback != null) {
                    callback.onError("Failed to get mood entries: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get recent mood entries (last 30 days)
     */
    public void getRecentMoodEntries(MoodListCallback callback) {
        Calendar calendar = Calendar.getInstance();
        String endDate = dateFormat.format(calendar.getTime());
        
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        String startDate = dateFormat.format(calendar.getTime());
        
        getMoodEntriesForRange(startDate, endDate, callback);
    }
    
    /**
     * Get mood statistics for analysis
     */
    public void getMoodStatistics(MoodStatsCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                // Get recent mood entries
                Calendar calendar = Calendar.getInstance();
                String endDate = dateFormat.format(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, -30);
                String startDate = dateFormat.format(calendar.getTime());
                
                List<MoodEntry> moodEntries = databaseHelper.getMoodEntriesForDateRange(startDate, endDate);
                
                MoodStatistics stats = calculateMoodStatistics(moodEntries);
                
                if (callback != null) {
                    callback.onStatsReceived(stats);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting mood statistics", e);
                if (callback != null) {
                    callback.onError("Failed to get mood statistics: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Calculate comprehensive mood statistics
     */
    private MoodStatistics calculateMoodStatistics(List<MoodEntry> moodEntries) {
        MoodStatistics stats = new MoodStatistics();
        
        if (moodEntries.isEmpty()) {
            return stats;
        }
        
        // Basic counts
        stats.totalEntries = moodEntries.size();
        stats.positiveEntries = 0;
        stats.negativeEntries = 0;
        stats.neutralEntries = 0;
        
        // Sums for averages
        float totalMoodScore = 0;
        float totalEnergyLevel = 0;
        float totalStressLevel = 0;
        float totalSocialLevel = 0;
        float totalWellnessScore = 0;
        
        // Category counts
        Map<MoodEntry.MoodCategory, Integer> categoryMap = new HashMap<>();
        Map<String, Integer> weatherMap = new HashMap<>();
        
        for (MoodEntry entry : moodEntries) {
            // Mood scoring
            int moodScore = entry.getMoodScore();
            totalMoodScore += moodScore;
            
            if (entry.isPositiveMood()) {
                stats.positiveEntries++;
            } else if (entry.isNegativeMood()) {
                stats.negativeEntries++;
            } else {
                stats.neutralEntries++;
            }
            
            // Other metrics
            totalEnergyLevel += entry.getEnergyLevel();
            totalStressLevel += entry.getStressLevel();
            totalSocialLevel += entry.getSocialLevel();
            totalWellnessScore += entry.getWellnessScore();
            
            // Category tracking
            MoodEntry.MoodCategory category = entry.getCategory();
            categoryMap.put(category, categoryMap.getOrDefault(category, 0) + 1);
            
            // Weather tracking
            if (entry.getWeatherCondition() != null) {
                String weather = entry.getWeatherCondition();
                weatherMap.put(weather, weatherMap.getOrDefault(weather, 0) + 1);
            }
            
            // Track best and worst days
            if (stats.bestMoodEntry == null || moodScore > stats.bestMoodEntry.getMoodScore()) {
                stats.bestMoodEntry = entry;
            }
            if (stats.worstMoodEntry == null || moodScore < stats.worstMoodEntry.getMoodScore()) {
                stats.worstMoodEntry = entry;
            }
        }
        
        // Calculate averages
        stats.averageMoodScore = totalMoodScore / stats.totalEntries;
        stats.averageEnergyLevel = totalEnergyLevel / stats.totalEntries;
        stats.averageStressLevel = totalStressLevel / stats.totalEntries;
        stats.averageSocialLevel = totalSocialLevel / stats.totalEntries;
        stats.averageWellnessScore = totalWellnessScore / stats.totalEntries;
        
        // Calculate percentages
        stats.positivePercentage = (float) stats.positiveEntries / stats.totalEntries * 100;
        stats.negativePercentage = (float) stats.negativeEntries / stats.totalEntries * 100;
        stats.neutralPercentage = (float) stats.neutralEntries / stats.totalEntries * 100;
        
        // Find most common category and weather
        stats.mostCommonCategory = getMostCommonKey(categoryMap);
        stats.mostCommonWeather = getMostCommonKey(weatherMap);
        
        // Calculate mood trend
        stats.moodTrend = calculateMoodTrend(moodEntries);
        
        return stats;
    }
    
    /**
     * Calculate mood trend over time
     */
    private String calculateMoodTrend(List<MoodEntry> moodEntries) {
        if (moodEntries.size() < 7) {
            return "Insufficient data";
        }
        
        // Compare recent week with previous week
        int recentWeekSum = 0;
        int previousWeekSum = 0;
        
        for (int i = 0; i < Math.min(7, moodEntries.size()); i++) {
            recentWeekSum += moodEntries.get(i).getMoodScore();
        }
        
        for (int i = 7; i < Math.min(14, moodEntries.size()); i++) {
            previousWeekSum += moodEntries.get(i).getMoodScore();
        }
        
        float recentAvg = (float) recentWeekSum / Math.min(7, moodEntries.size());
        float previousAvg = (float) previousWeekSum / Math.min(7, moodEntries.size() - 7);
        
        float difference = recentAvg - previousAvg;
        
        if (difference > 0.5) {
            return "Improving";
        } else if (difference < -0.5) {
            return "Declining";
        } else {
            return "Stable";
        }
    }
    
    /**
     * Get most common key from map
     */
    private <T> T getMostCommonKey(Map<T, Integer> map) {
        T mostCommon = null;
        int maxCount = 0;
        
        for (Map.Entry<T, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }
        
        return mostCommon;
    }
    
    /**
     * Get mood insights and recommendations
     */
    public void getMoodInsights(MoodInsightsCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                getMoodStatistics(new MoodStatsCallback() {
                    @Override
                    public void onStatsReceived(MoodStatistics stats) {
                        List<String> insights = generateMoodInsights(stats);
                        if (callback != null) {
                            callback.onInsightsReceived(insights);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting mood insights", e);
                if (callback != null) {
                    callback.onError("Failed to get insights: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Generate mood insights from statistics
     */
    private List<String> generateMoodInsights(MoodStatistics stats) {
        List<String> insights = new ArrayList<>();
        
        if (stats.totalEntries == 0) {
            insights.add("Start tracking your mood to get personalized insights!");
            return insights;
        }
        
        // Mood trend insight
        insights.add("Your mood trend is " + stats.moodTrend.toLowerCase() + " over the past weeks.");
        
        // Positivity insight
        if (stats.positivePercentage > 60) {
            insights.add("Great job! You have positive moods " + String.format("%.0f%%", stats.positivePercentage) + " of the time.");
        } else if (stats.negativePercentage > 40) {
            insights.add("You've had challenging days " + String.format("%.0f%%", stats.negativePercentage) + " of the time. Consider stress management techniques.");
        }
        
        // Energy insight
        if (stats.averageEnergyLevel < 4) {
            insights.add("Your energy levels are low (avg: " + String.format("%.1f", stats.averageEnergyLevel) + "/10). Focus on sleep and nutrition.");
        } else if (stats.averageEnergyLevel > 7) {
            insights.add("You maintain high energy levels (avg: " + String.format("%.1f", stats.averageEnergyLevel) + "/10). Keep it up!");
        }
        
        // Stress insight
        if (stats.averageStressLevel > 7) {
            insights.add("Your stress levels are high (avg: " + String.format("%.1f", stats.averageStressLevel) + "/10). Try relaxation techniques.");
        } else if (stats.averageStressLevel < 4) {
            insights.add("You manage stress well (avg: " + String.format("%.1f", stats.averageStressLevel) + "/10). Great stress management!");
        }
        
        // Weather insight
        if (stats.mostCommonWeather != null) {
            insights.add("You track mood most often during " + stats.mostCommonWeather.toLowerCase() + " weather.");
        }
        
        // Best day insight
        if (stats.bestMoodEntry != null) {
            insights.add("Your best recent day was " + stats.bestMoodEntry.getFormattedMood() + " on " + stats.bestMoodEntry.getDate() + ".");
        }
        
        return insights;
    }
    
    /**
     * Delete mood entry
     */
    public void deleteMoodEntry(int moodId, MoodDeleteCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                int deleted = databaseHelper.deleteMoodEntry(moodId);
                
                if (callback != null) {
                    callback.onMoodDeleted(deleted > 0);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error deleting mood entry", e);
                if (callback != null) {
                    callback.onError("Failed to delete mood: " + e.getMessage());
                }
            }
        });
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes
    public static class MoodStatistics {
        public int totalEntries;
        public int positiveEntries;
        public int negativeEntries;
        public int neutralEntries;
        public float averageMoodScore;
        public float averageEnergyLevel;
        public float averageStressLevel;
        public float averageSocialLevel;
        public float averageWellnessScore;
        public float positivePercentage;
        public float negativePercentage;
        public float neutralPercentage;
        public MoodEntry.MoodCategory mostCommonCategory;
        public String mostCommonWeather;
        public String moodTrend;
        public MoodEntry bestMoodEntry;
        public MoodEntry worstMoodEntry;
    }
    
    // Callback interfaces
    public interface MoodSaveCallback {
        void onMoodSaved(MoodEntry moodEntry);
        void onError(String error);
    }
    
    public interface MoodCallback {
        void onMoodReceived(MoodEntry moodEntry);
        void onError(String error);
    }
    
    public interface MoodListCallback {
        void onMoodEntriesReceived(List<MoodEntry> moodEntries);
        void onError(String error);
    }
    
    public interface MoodStatsCallback {
        void onStatsReceived(MoodStatistics statistics);
        void onError(String error);
    }
    
    public interface MoodInsightsCallback {
        void onInsightsReceived(List<String> insights);
        void onError(String error);
    }
    
    public interface MoodDeleteCallback {
        void onMoodDeleted(boolean success);
        void onError(String error);
    }
}