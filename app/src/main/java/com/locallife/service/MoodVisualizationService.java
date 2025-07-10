package com.locallife.service;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.locallife.model.MoodEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for creating mood visualization data
 * Prepares data for charts, graphs, and visual mood tracking
 */
public class MoodVisualizationService {
    private static final String TAG = "MoodVisualizationService";
    private MoodTrackingService moodTrackingService;
    private MoodAnalyticsService moodAnalyticsService;
    
    public MoodVisualizationService(Context context) {
        moodTrackingService = new MoodTrackingService(context);
        moodAnalyticsService = new MoodAnalyticsService(context);
    }
    
    /**
     * Creates data for mood line chart over time
     */
    public MoodLineChartData createMoodLineChart(int days) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodEntry> moodEntries = moodTrackingService.getMoodEntriesInRange(startDate, endDate);
        
        return new MoodLineChartData(moodEntries, days);
    }
    
    /**
     * Creates data for mood distribution pie chart
     */
    public MoodDistributionChartData createMoodDistributionChart(int days) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodTrackingService.MoodDistribution> distribution = 
                moodTrackingService.getMoodDistribution(startDate, endDate);
        
        return new MoodDistributionChartData(distribution);
    }
    
    /**
     * Creates data for weekly mood pattern chart
     */
    public WeeklyMoodChartData createWeeklyMoodChart(int weeks) {
        MoodAnalyticsService.WeeklyMoodPattern pattern = moodAnalyticsService.analyzeWeeklyPattern(weeks);
        
        return new WeeklyMoodChartData(pattern);
    }
    
    /**
     * Creates data for mood heatmap calendar
     */
    public MoodHeatmapData createMoodHeatmap(int days) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodEntry> moodEntries = moodTrackingService.getMoodEntriesInRange(startDate, endDate);
        
        return new MoodHeatmapData(moodEntries, days);
    }
    
    /**
     * Creates data for mood vs weather correlation chart
     */
    public MoodWeatherChartData createMoodWeatherChart(int days) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodEntry> moodEntries = moodTrackingService.getMoodEntriesInRange(startDate, endDate);
        
        return new MoodWeatherChartData(moodEntries);
    }
    
    /**
     * Creates data for mood trend indicators
     */
    public MoodTrendIndicators createMoodTrendIndicators(int days) {
        MoodAnalyticsService.MoodTrendAnalysis trendAnalysis = moodAnalyticsService.analyzeMoodTrends(days);
        
        return new MoodTrendIndicators(trendAnalysis);
    }
    
    /**
     * Creates data for mood trigger frequency chart
     */
    public MoodTriggerChartData createMoodTriggerChart(int days) {
        MoodAnalyticsService.MoodTriggerAnalysis triggerAnalysis = moodAnalyticsService.analyzeMoodTriggers(days);
        
        return new MoodTriggerChartData(triggerAnalysis);
    }
    
    /**
     * Creates data for activity mood correlation chart
     */
    public ActivityMoodChartData createActivityMoodChart(int days) {
        MoodAnalyticsService.ActivityMoodCorrelation activityCorrelation = 
                moodAnalyticsService.analyzeActivityCorrelation(days);
        
        return new ActivityMoodChartData(activityCorrelation);
    }
    
    /**
     * Inner class for mood line chart data
     */
    public static class MoodLineChartData {
        private List<String> labels;
        private List<Float> moodValues;
        private List<Integer> colors;
        private float minMood;
        private float maxMood;
        private float averageMood;
        
        public MoodLineChartData(List<MoodEntry> moodEntries, int days) {
            generateLineChartData(moodEntries, days);
        }
        
        private void generateLineChartData(List<MoodEntry> moodEntries, int days) {
            labels = new ArrayList<>();
            moodValues = new ArrayList<>();
            colors = new ArrayList<>();
            
            // Group entries by date
            Map<String, List<MoodEntry>> dailyEntries = new HashMap<>();
            for (MoodEntry entry : moodEntries) {
                dailyEntries.computeIfAbsent(entry.getDate(), k -> new ArrayList<>()).add(entry);
            }
            
            // Calculate daily averages
            Calendar cal = Calendar.getInstance();
            float sum = 0;
            int count = 0;
            minMood = 10;
            maxMood = 1;
            
            for (int i = days - 1; i >= 0; i--) {
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String date = String.format("%04d-%02d-%02d", 
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                
                labels.add(String.format("%02d/%02d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
                
                List<MoodEntry> dayEntries = dailyEntries.get(date);
                if (dayEntries != null && !dayEntries.isEmpty()) {
                    float daySum = 0;
                    for (MoodEntry entry : dayEntries) {
                        daySum += entry.getMoodScore();
                    }
                    float dayAverage = daySum / dayEntries.size();
                    moodValues.add(dayAverage);
                    colors.add(getMoodColor(dayAverage));
                    
                    sum += dayAverage;
                    count++;
                    minMood = Math.min(minMood, dayAverage);
                    maxMood = Math.max(maxMood, dayAverage);
                } else {
                    moodValues.add(null); // No data for this day
                    colors.add(Color.GRAY);
                }
                
                cal = Calendar.getInstance(); // Reset calendar
            }
            
            averageMood = count > 0 ? sum / count : 5.0f;
        }
        
        private int getMoodColor(float mood) {
            if (mood <= 2) return Color.parseColor("#FF5252"); // Red
            if (mood <= 4) return Color.parseColor("#FF9800"); // Orange
            if (mood <= 6) return Color.parseColor("#FFC107"); // Yellow
            if (mood <= 8) return Color.parseColor("#8BC34A"); // Light Green
            return Color.parseColor("#4CAF50"); // Green
        }
        
        // Getters
        public List<String> getLabels() { return labels; }
        public List<Float> getMoodValues() { return moodValues; }
        public List<Integer> getColors() { return colors; }
        public float getMinMood() { return minMood; }
        public float getMaxMood() { return maxMood; }
        public float getAverageMood() { return averageMood; }
    }
    
    /**
     * Inner class for mood distribution chart data
     */
    public static class MoodDistributionChartData {
        private List<String> labels;
        private List<Integer> values;
        private List<Integer> colors;
        private int totalEntries;
        
        public MoodDistributionChartData(List<MoodTrackingService.MoodDistribution> distribution) {
            generateDistributionData(distribution);
        }
        
        private void generateDistributionData(List<MoodTrackingService.MoodDistribution> distribution) {
            labels = new ArrayList<>();
            values = new ArrayList<>();
            colors = new ArrayList<>();
            totalEntries = 0;
            
            for (MoodTrackingService.MoodDistribution dist : distribution) {
                labels.add(formatMoodLevel(dist.getMoodLevel()));
                values.add(dist.getCount());
                colors.add(getMoodLevelColor(dist.getMoodLevel()));
                totalEntries += dist.getCount();
            }
        }
        
        private String formatMoodLevel(String moodLevel) {
            if (moodLevel == null) return "Unknown";
            
            switch (moodLevel) {
                case "very_sad": return "Very Sad";
                case "sad": return "Sad";
                case "neutral": return "Neutral";
                case "happy": return "Happy";
                case "very_happy": return "Very Happy";
                default: return moodLevel;
            }
        }
        
        private int getMoodLevelColor(String moodLevel) {
            if (moodLevel == null) return Color.GRAY;
            
            switch (moodLevel) {
                case "very_sad": return Color.parseColor("#FF5252");
                case "sad": return Color.parseColor("#FF9800");
                case "neutral": return Color.parseColor("#FFC107");
                case "happy": return Color.parseColor("#8BC34A");
                case "very_happy": return Color.parseColor("#4CAF50");
                default: return Color.GRAY;
            }
        }
        
        // Getters
        public List<String> getLabels() { return labels; }
        public List<Integer> getValues() { return values; }
        public List<Integer> getColors() { return colors; }
        public int getTotalEntries() { return totalEntries; }
    }
    
    /**
     * Inner class for weekly mood chart data
     */
    public static class WeeklyMoodChartData {
        private List<String> dayLabels;
        private List<Float> moodValues;
        private List<Integer> colors;
        private String bestDay;
        private String worstDay;
        
        public WeeklyMoodChartData(MoodAnalyticsService.WeeklyMoodPattern pattern) {
            generateWeeklyData(pattern);
        }
        
        private void generateWeeklyData(MoodAnalyticsService.WeeklyMoodPattern pattern) {
            dayLabels = new ArrayList<>();
            moodValues = new ArrayList<>();
            colors = new ArrayList<>();
            
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            String[] fullDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            
            for (int i = 0; i < days.length; i++) {
                dayLabels.add(days[i]);
                Float moodValue = pattern.getDayOfWeekAverages().get(fullDays[i]);
                if (moodValue != null) {
                    moodValues.add(moodValue);
                    colors.add(getMoodColor(moodValue));
                } else {
                    moodValues.add(5.0f);
                    colors.add(Color.GRAY);
                }
            }
            
            bestDay = pattern.getBestDay();
            worstDay = pattern.getWorstDay();
        }
        
        private int getMoodColor(float mood) {
            if (mood <= 2) return Color.parseColor("#FF5252");
            if (mood <= 4) return Color.parseColor("#FF9800");
            if (mood <= 6) return Color.parseColor("#FFC107");
            if (mood <= 8) return Color.parseColor("#8BC34A");
            return Color.parseColor("#4CAF50");
        }
        
        // Getters
        public List<String> getDayLabels() { return dayLabels; }
        public List<Float> getMoodValues() { return moodValues; }
        public List<Integer> getColors() { return colors; }
        public String getBestDay() { return bestDay; }
        public String getWorstDay() { return worstDay; }
    }
    
    /**
     * Inner class for mood heatmap data
     */
    public static class MoodHeatmapData {
        private Map<String, Float> dailyMoodData;
        private List<String> dateLabels;
        private List<Float> moodIntensities;
        private List<Integer> colors;
        
        public MoodHeatmapData(List<MoodEntry> moodEntries, int days) {
            generateHeatmapData(moodEntries, days);
        }
        
        private void generateHeatmapData(List<MoodEntry> moodEntries, int days) {
            dailyMoodData = new HashMap<>();
            dateLabels = new ArrayList<>();
            moodIntensities = new ArrayList<>();
            colors = new ArrayList<>();
            
            // Group entries by date
            Map<String, List<MoodEntry>> dailyEntries = new HashMap<>();
            for (MoodEntry entry : moodEntries) {
                dailyEntries.computeIfAbsent(entry.getDate(), k -> new ArrayList<>()).add(entry);
            }
            
            // Calculate daily averages
            Calendar cal = Calendar.getInstance();
            for (int i = days - 1; i >= 0; i--) {
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String date = String.format("%04d-%02d-%02d", 
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                
                dateLabels.add(date);
                
                List<MoodEntry> dayEntries = dailyEntries.get(date);
                if (dayEntries != null && !dayEntries.isEmpty()) {
                    float daySum = 0;
                    for (MoodEntry entry : dayEntries) {
                        daySum += entry.getMoodScore();
                    }
                    float dayAverage = daySum / dayEntries.size();
                    dailyMoodData.put(date, dayAverage);
                    moodIntensities.add(dayAverage / 10.0f); // Normalize to 0-1
                    colors.add(getMoodColor(dayAverage));
                } else {
                    dailyMoodData.put(date, null);
                    moodIntensities.add(0.0f);
                    colors.add(Color.LTGRAY);
                }
                
                cal = Calendar.getInstance(); // Reset calendar
            }
        }
        
        private int getMoodColor(float mood) {
            if (mood <= 2) return Color.parseColor("#FF5252");
            if (mood <= 4) return Color.parseColor("#FF9800");
            if (mood <= 6) return Color.parseColor("#FFC107");
            if (mood <= 8) return Color.parseColor("#8BC34A");
            return Color.parseColor("#4CAF50");
        }
        
        // Getters
        public Map<String, Float> getDailyMoodData() { return dailyMoodData; }
        public List<String> getDateLabels() { return dateLabels; }
        public List<Float> getMoodIntensities() { return moodIntensities; }
        public List<Integer> getColors() { return colors; }
    }
    
    /**
     * Inner class for mood weather chart data
     */
    public static class MoodWeatherChartData {
        private List<Float> temperatures;
        private List<Float> moodScores;
        private List<String> weatherConditions;
        private List<Integer> colors;
        
        public MoodWeatherChartData(List<MoodEntry> moodEntries) {
            generateWeatherData(moodEntries);
        }
        
        private void generateWeatherData(List<MoodEntry> moodEntries) {
            temperatures = new ArrayList<>();
            moodScores = new ArrayList<>();
            weatherConditions = new ArrayList<>();
            colors = new ArrayList<>();
            
            for (MoodEntry entry : moodEntries) {
                temperatures.add(entry.getTemperature());
                moodScores.add((float) entry.getMoodScore());
                weatherConditions.add(entry.getWeatherCondition() != null ? entry.getWeatherCondition() : "Unknown");
                colors.add(getMoodColor(entry.getMoodScore()));
            }
        }
        
        private int getMoodColor(float mood) {
            if (mood <= 2) return Color.parseColor("#FF5252");
            if (mood <= 4) return Color.parseColor("#FF9800");
            if (mood <= 6) return Color.parseColor("#FFC107");
            if (mood <= 8) return Color.parseColor("#8BC34A");
            return Color.parseColor("#4CAF50");
        }
        
        // Getters
        public List<Float> getTemperatures() { return temperatures; }
        public List<Float> getMoodScores() { return moodScores; }
        public List<String> getWeatherConditions() { return weatherConditions; }
        public List<Integer> getColors() { return colors; }
    }
    
    /**
     * Inner class for mood trend indicators
     */
    public static class MoodTrendIndicators {
        private float averageMood;
        private String trendDirection;
        private float trendStrength;
        private float volatility;
        private int trendColor;
        private String trendDescription;
        
        public MoodTrendIndicators(MoodAnalyticsService.MoodTrendAnalysis trendAnalysis) {
            generateTrendIndicators(trendAnalysis);
        }
        
        private void generateTrendIndicators(MoodAnalyticsService.MoodTrendAnalysis trendAnalysis) {
            averageMood = trendAnalysis.getAverageMood();
            trendDirection = trendAnalysis.getTrend();
            trendStrength = Math.abs(trendAnalysis.getMoodChange());
            volatility = trendAnalysis.getVolatility();
            
            // Set trend color
            switch (trendDirection) {
                case "improving":
                    trendColor = Color.parseColor("#4CAF50");
                    trendDescription = "Your mood is improving";
                    break;
                case "declining":
                    trendColor = Color.parseColor("#FF5252");
                    trendDescription = "Your mood is declining";
                    break;
                default:
                    trendColor = Color.parseColor("#FFC107");
                    trendDescription = "Your mood is stable";
                    break;
            }
        }
        
        // Getters
        public float getAverageMood() { return averageMood; }
        public String getTrendDirection() { return trendDirection; }
        public float getTrendStrength() { return trendStrength; }
        public float getVolatility() { return volatility; }
        public int getTrendColor() { return trendColor; }
        public String getTrendDescription() { return trendDescription; }
    }
    
    /**
     * Inner class for mood trigger chart data
     */
    public static class MoodTriggerChartData {
        private List<String> triggerLabels;
        private List<Integer> triggerFrequencies;
        private List<Float> triggerImpacts;
        private List<Integer> colors;
        
        public MoodTriggerChartData(MoodAnalyticsService.MoodTriggerAnalysis triggerAnalysis) {
            generateTriggerData(triggerAnalysis);
        }
        
        private void generateTriggerData(MoodAnalyticsService.MoodTriggerAnalysis triggerAnalysis) {
            triggerLabels = new ArrayList<>();
            triggerFrequencies = new ArrayList<>();
            triggerImpacts = new ArrayList<>();
            colors = new ArrayList<>();
            
            for (Map.Entry<String, Integer> entry : triggerAnalysis.getTriggerFrequency().entrySet()) {
                triggerLabels.add(entry.getKey());
                triggerFrequencies.add(entry.getValue());
                
                Float impact = triggerAnalysis.getTriggerMoodImpact().get(entry.getKey());
                if (impact != null) {
                    triggerImpacts.add(impact);
                    colors.add(getMoodColor(impact));
                } else {
                    triggerImpacts.add(5.0f);
                    colors.add(Color.GRAY);
                }
            }
        }
        
        private int getMoodColor(float mood) {
            if (mood <= 2) return Color.parseColor("#FF5252");
            if (mood <= 4) return Color.parseColor("#FF9800");
            if (mood <= 6) return Color.parseColor("#FFC107");
            if (mood <= 8) return Color.parseColor("#8BC34A");
            return Color.parseColor("#4CAF50");
        }
        
        // Getters
        public List<String> getTriggerLabels() { return triggerLabels; }
        public List<Integer> getTriggerFrequencies() { return triggerFrequencies; }
        public List<Float> getTriggerImpacts() { return triggerImpacts; }
        public List<Integer> getColors() { return colors; }
    }
    
    /**
     * Inner class for activity mood chart data
     */
    public static class ActivityMoodChartData {
        private List<String> activityLabels;
        private List<Float> activityMoodImpacts;
        private List<Integer> colors;
        
        public ActivityMoodChartData(MoodAnalyticsService.ActivityMoodCorrelation activityCorrelation) {
            generateActivityData(activityCorrelation);
        }
        
        private void generateActivityData(MoodAnalyticsService.ActivityMoodCorrelation activityCorrelation) {
            activityLabels = new ArrayList<>();
            activityMoodImpacts = new ArrayList<>();
            colors = new ArrayList<>();
            
            for (Map.Entry<String, Float> entry : activityCorrelation.getActivityMoodImpact().entrySet()) {
                activityLabels.add(entry.getKey());
                activityMoodImpacts.add(entry.getValue());
                colors.add(getMoodColor(entry.getValue()));
            }
        }
        
        private int getMoodColor(float mood) {
            if (mood <= 2) return Color.parseColor("#FF5252");
            if (mood <= 4) return Color.parseColor("#FF9800");
            if (mood <= 6) return Color.parseColor("#FFC107");
            if (mood <= 8) return Color.parseColor("#8BC34A");
            return Color.parseColor("#4CAF50");
        }
        
        // Getters
        public List<String> getActivityLabels() { return activityLabels; }
        public List<Float> getActivityMoodImpacts() { return activityMoodImpacts; }
        public List<Integer> getColors() { return colors; }
    }
    
    /**
     * Cleanup method
     */
    public void close() {
        if (moodTrackingService != null) {
            moodTrackingService.close();
        }
        if (moodAnalyticsService != null) {
            moodAnalyticsService.close();
        }
    }
}