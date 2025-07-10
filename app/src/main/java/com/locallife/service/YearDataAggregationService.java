package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.app.views.YearInPixelsView;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for aggregating and processing year-level data for visualizations
 */
public class YearDataAggregationService {
    private static final String TAG = "YearDataAggregationService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public YearDataAggregationService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Load year data asynchronously
     */
    public void loadYearDataAsync(int year, YearDataCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                Map<String, YearInPixelsView.DayData> yearData = loadYearData(year);
                YearStatistics stats = calculateYearStatistics(yearData, year);
                
                // Return results on main thread
                if (callback != null) {
                    callback.onDataLoaded(yearData, stats);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading year data", e);
                if (callback != null) {
                    callback.onError("Failed to load year data: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Load data for a specific year
     */
    public Map<String, YearInPixelsView.DayData> loadYearData(int year) {
        Map<String, YearInPixelsView.DayData> yearData = new HashMap<>();
        
        // Get all day records for the year
        List<DayRecord> dayRecords = databaseHelper.getAllDayRecords();
        
        for (DayRecord record : dayRecords) {
            if (record.getDate().startsWith(String.valueOf(year))) {
                YearInPixelsView.DayData dayData = new YearInPixelsView.DayData(
                    record.getActivityScore(),
                    record.getStepCount(),
                    record.getPlacesVisited(),
                    record.getScreenTimeMinutes(),
                    record.getTotalMediaMinutes()
                );
                
                yearData.put(record.getDate(), dayData);
            }
        }
        
        // Fill in missing days with empty data
        fillMissingDays(yearData, year);
        
        return yearData;
    }
    
    /**
     * Fill missing days in the year with empty data
     */
    private void fillMissingDays(Map<String, YearInPixelsView.DayData> yearData, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, 0, 1); // January 1st
        
        int daysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
        
        for (int day = 1; day <= daysInYear; day++) {
            calendar.set(Calendar.DAY_OF_YEAR, day);
            String dateKey = dateFormat.format(calendar.getTime());
            
            if (!yearData.containsKey(dateKey)) {
                yearData.put(dateKey, new YearInPixelsView.DayData());
            }
        }
    }
    
    /**
     * Calculate comprehensive statistics for the year
     */
    public YearStatistics calculateYearStatistics(Map<String, YearInPixelsView.DayData> yearData, int year) {
        YearStatistics stats = new YearStatistics();
        stats.year = year;
        
        if (yearData.isEmpty()) {
            return stats;
        }
        
        // Calculate basic statistics
        float totalActivityScore = 0f;
        int totalSteps = 0;
        int totalPlaces = 0;
        int totalScreenTime = 0;
        int totalMediaTime = 0;
        
        float maxActivityScore = 0f;
        int maxSteps = 0;
        int maxPlaces = 0;
        int maxScreenTime = 0;
        int maxMediaTime = 0;
        
        int activeDays = 0;
        int dataAvailableDays = 0;
        
        for (Map.Entry<String, YearInPixelsView.DayData> entry : yearData.entrySet()) {
            YearInPixelsView.DayData dayData = entry.getValue();
            
            // Check if day has any data
            if (dayData.activityScore > 0 || dayData.steps > 0 || dayData.placesVisited > 0 ||
                dayData.screenTimeMinutes > 0 || dayData.mediaMinutes > 0) {
                dataAvailableDays++;
            }
            
            // Count active days (days with significant activity)
            if (dayData.activityScore > 20 || dayData.steps > 1000) {
                activeDays++;
            }
            
            // Accumulate totals
            totalActivityScore += dayData.activityScore;
            totalSteps += dayData.steps;
            totalPlaces += dayData.placesVisited;
            totalScreenTime += dayData.screenTimeMinutes;
            totalMediaTime += dayData.mediaMinutes;
            
            // Track maximums
            maxActivityScore = Math.max(maxActivityScore, dayData.activityScore);
            maxSteps = Math.max(maxSteps, dayData.steps);
            maxPlaces = Math.max(maxPlaces, dayData.placesVisited);
            maxScreenTime = Math.max(maxScreenTime, dayData.screenTimeMinutes);
            maxMediaTime = Math.max(maxMediaTime, dayData.mediaMinutes);
        }
        
        // Calculate averages
        int totalDays = yearData.size();
        stats.totalDays = totalDays;
        stats.dataAvailableDays = dataAvailableDays;
        stats.activeDays = activeDays;
        stats.dataPercentage = (dataAvailableDays / (float) totalDays) * 100f;
        
        stats.averageActivityScore = totalActivityScore / totalDays;
        stats.averageSteps = totalSteps / totalDays;
        stats.averagePlaces = totalPlaces / (float) totalDays;
        stats.averageScreenTime = totalScreenTime / (float) totalDays;
        stats.averageMediaTime = totalMediaTime / (float) totalDays;
        
        stats.maxActivityScore = maxActivityScore;
        stats.maxSteps = maxSteps;
        stats.maxPlaces = maxPlaces;
        stats.maxScreenTime = maxScreenTime;
        stats.maxMediaTime = maxMediaTime;
        
        // Calculate streaks
        calculateStreaks(yearData, stats);
        
        // Calculate seasonal patterns
        calculateSeasonalPatterns(yearData, stats);
        
        // Calculate day-of-week patterns
        calculateDayOfWeekPatterns(yearData, stats);
        
        return stats;
    }
    
    /**
     * Calculate activity streaks
     */
    private void calculateStreaks(Map<String, YearInPixelsView.DayData> yearData, YearStatistics stats) {
        List<String> sortedDates = new ArrayList<>(yearData.keySet());
        sortedDates.sort(String::compareTo);
        
        int currentStreak = 0;
        int longestStreak = 0;
        int currentInactiveStreak = 0;
        int longestInactiveStreak = 0;
        
        for (String date : sortedDates) {
            YearInPixelsView.DayData dayData = yearData.get(date);
            
            // Check if day is active
            boolean isActive = dayData.activityScore > 20 || dayData.steps > 1000;
            
            if (isActive) {
                currentStreak++;
                currentInactiveStreak = 0;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentInactiveStreak++;
                currentStreak = 0;
                longestInactiveStreak = Math.max(longestInactiveStreak, currentInactiveStreak);
            }
        }
        
        stats.longestActiveStreak = longestStreak;
        stats.longestInactiveStreak = longestInactiveStreak;
        stats.currentStreak = currentStreak;
    }
    
    /**
     * Calculate seasonal activity patterns
     */
    private void calculateSeasonalPatterns(Map<String, YearInPixelsView.DayData> yearData, YearStatistics stats) {
        float[] seasonalActivity = new float[4]; // Spring, Summer, Fall, Winter
        int[] seasonalCounts = new int[4];
        
        Calendar calendar = Calendar.getInstance();
        
        for (Map.Entry<String, YearInPixelsView.DayData> entry : yearData.entrySet()) {
            try {
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.getKey()));
                int month = calendar.get(Calendar.MONTH);
                
                int season = getSeason(month);
                seasonalActivity[season] += entry.getValue().activityScore;
                seasonalCounts[season]++;
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date for seasonal analysis", e);
            }
        }
        
        // Calculate averages
        for (int i = 0; i < 4; i++) {
            if (seasonalCounts[i] > 0) {
                seasonalActivity[i] /= seasonalCounts[i];
            }
        }
        
        stats.seasonalActivity = seasonalActivity;
    }
    
    /**
     * Calculate day-of-week activity patterns
     */
    private void calculateDayOfWeekPatterns(Map<String, YearInPixelsView.DayData> yearData, YearStatistics stats) {
        float[] dayOfWeekActivity = new float[7]; // Sunday to Saturday
        int[] dayOfWeekCounts = new int[7];
        
        Calendar calendar = Calendar.getInstance();
        
        for (Map.Entry<String, YearInPixelsView.DayData> entry : yearData.entrySet()) {
            try {
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.getKey()));
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
                
                dayOfWeekActivity[dayOfWeek] += entry.getValue().activityScore;
                dayOfWeekCounts[dayOfWeek]++;
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date for day-of-week analysis", e);
            }
        }
        
        // Calculate averages
        for (int i = 0; i < 7; i++) {
            if (dayOfWeekCounts[i] > 0) {
                dayOfWeekActivity[i] /= dayOfWeekCounts[i];
            }
        }
        
        stats.dayOfWeekActivity = dayOfWeekActivity;
    }
    
    /**
     * Get season index from month (0=Spring, 1=Summer, 2=Fall, 3=Winter)
     */
    private int getSeason(int month) {
        if (month >= 2 && month <= 4) return 0; // Spring: March, April, May
        if (month >= 5 && month <= 7) return 1; // Summer: June, July, August
        if (month >= 8 && month <= 10) return 2; // Fall: September, October, November
        return 3; // Winter: December, January, February
    }
    
    /**
     * Get available years with data
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        List<DayRecord> allRecords = databaseHelper.getAllDayRecords();
        
        for (DayRecord record : allRecords) {
            try {
                int year = Integer.parseInt(record.getDate().substring(0, 4));
                if (!years.contains(year)) {
                    years.add(year);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing year from date: " + record.getDate(), e);
            }
        }
        
        years.sort(Integer::compareTo);
        return years;
    }
    
    /**
     * Compare two years and return comparison statistics
     */
    public YearComparisonData compareYears(int year1, int year2) {
        Map<String, YearInPixelsView.DayData> data1 = loadYearData(year1);
        Map<String, YearInPixelsView.DayData> data2 = loadYearData(year2);
        
        YearStatistics stats1 = calculateYearStatistics(data1, year1);
        YearStatistics stats2 = calculateYearStatistics(data2, year2);
        
        return new YearComparisonData(stats1, stats2);
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes
    public static class YearStatistics {
        public int year;
        public int totalDays;
        public int dataAvailableDays;
        public int activeDays;
        public float dataPercentage;
        
        public float averageActivityScore;
        public float averageSteps;
        public float averagePlaces;
        public float averageScreenTime;
        public float averageMediaTime;
        
        public float maxActivityScore;
        public int maxSteps;
        public int maxPlaces;
        public int maxScreenTime;
        public int maxMediaTime;
        
        public int longestActiveStreak;
        public int longestInactiveStreak;
        public int currentStreak;
        
        public float[] seasonalActivity = new float[4];
        public float[] dayOfWeekActivity = new float[7];
    }
    
    public static class YearComparisonData {
        public YearStatistics year1Stats;
        public YearStatistics year2Stats;
        
        public YearComparisonData(YearStatistics year1Stats, YearStatistics year2Stats) {
            this.year1Stats = year1Stats;
            this.year2Stats = year2Stats;
        }
        
        public float getActivityScoreChange() {
            return year2Stats.averageActivityScore - year1Stats.averageActivityScore;
        }
        
        public float getStepsChange() {
            return year2Stats.averageSteps - year1Stats.averageSteps;
        }
        
        public float getPlacesChange() {
            return year2Stats.averagePlaces - year1Stats.averagePlaces;
        }
    }
    
    // Callback interface
    public interface YearDataCallback {
        void onDataLoaded(Map<String, YearInPixelsView.DayData> yearData, YearStatistics statistics);
        void onError(String error);
    }
}