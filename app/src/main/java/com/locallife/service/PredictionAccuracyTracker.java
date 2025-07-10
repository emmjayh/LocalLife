package com.locallife.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.locallife.model.ActivityType;
import com.locallife.model.PredictionResult;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Service to track and improve prediction accuracy of the Activity Prediction Engine
 */
public class PredictionAccuracyTracker {
    private static final String TAG = "PredictionAccuracyTracker";
    private static final String PREFS_NAME = "prediction_accuracy_tracker";
    private static final int MAX_STORED_PREDICTIONS = 1000;
    private static final int ACCURACY_CALCULATION_WINDOW = 30; // days
    
    private Context context;
    private SharedPreferences preferences;
    private Queue<PredictionResult> predictionHistory;
    private Map<String, AccuracyMetrics> accuracyMetrics;
    private Map<ActivityType, ActivityAccuracy> activityAccuracies;
    private Map<String, WeatherAccuracy> weatherAccuracies;
    private Map<String, TimeAccuracy> timeAccuracies;
    
    // Accuracy tracking
    private double overallAccuracy;
    private long totalPredictions;
    private long correctPredictions;
    private Date lastUpdateTime;
    
    public PredictionAccuracyTracker(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.predictionHistory = new ConcurrentLinkedQueue<>();
        this.accuracyMetrics = new ConcurrentHashMap<>();
        this.activityAccuracies = new ConcurrentHashMap<>();
        this.weatherAccuracies = new ConcurrentHashMap<>();
        this.timeAccuracies = new ConcurrentHashMap<>();
        
        // Load saved data
        loadSavedData();
        
        // Initialize metrics
        initializeMetrics();
    }
    
    /**
     * Load saved accuracy data from preferences
     */
    private void loadSavedData() {
        overallAccuracy = preferences.getFloat("overall_accuracy", 0.0f);
        totalPredictions = preferences.getLong("total_predictions", 0);
        correctPredictions = preferences.getLong("correct_predictions", 0);
        lastUpdateTime = new Date(preferences.getLong("last_update_time", System.currentTimeMillis()));
        
        Log.d(TAG, "Loaded accuracy data: " + overallAccuracy + " (" + correctPredictions + "/" + totalPredictions + ")");
    }
    
    /**
     * Save accuracy data to preferences
     */
    private void saveData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("overall_accuracy", (float) overallAccuracy);
        editor.putLong("total_predictions", totalPredictions);
        editor.putLong("correct_predictions", correctPredictions);
        editor.putLong("last_update_time", new Date().getTime());
        editor.apply();
    }
    
    /**
     * Initialize accuracy metrics
     */
    private void initializeMetrics() {
        // Initialize activity accuracy tracking
        for (ActivityType activityType : ActivityType.values()) {
            activityAccuracies.put(activityType, new ActivityAccuracy(activityType));
        }
        
        // Initialize weather accuracy tracking
        String[] weatherConditions = {"clear", "cloudy", "rain", "storm", "snow", "fog"};
        for (String condition : weatherConditions) {
            weatherAccuracies.put(condition, new WeatherAccuracy(condition));
        }
        
        // Initialize time accuracy tracking
        String[] timeSlots = {"MORNING", "AFTERNOON", "EVENING", "NIGHT"};
        for (String timeSlot : timeSlots) {
            timeAccuracies.put(timeSlot, new TimeAccuracy(timeSlot));
        }
    }
    
    /**
     * Record a prediction result for accuracy tracking
     */
    public void recordPredictionAccuracy(PredictionResult result) {
        if (result == null || !result.isValidated()) {
            return;
        }
        
        Log.d(TAG, "Recording prediction accuracy: " + result.getPredictionAccuracy());
        
        // Add to history
        predictionHistory.offer(result);
        
        // Limit history size
        while (predictionHistory.size() > MAX_STORED_PREDICTIONS) {
            predictionHistory.poll();
        }
        
        // Update overall accuracy
        updateOverallAccuracy(result);
        
        // Update activity-specific accuracy
        updateActivityAccuracy(result);
        
        // Update weather-specific accuracy
        updateWeatherAccuracy(result);
        
        // Update time-specific accuracy
        updateTimeAccuracy(result);
        
        // Update method-specific accuracy
        updateMethodAccuracy(result);
        
        // Save data
        saveData();
        
        // Log improvement suggestions
        logImprovementSuggestions();
    }
    
    /**
     * Update overall accuracy metrics
     */
    private void updateOverallAccuracy(PredictionResult result) {
        totalPredictions++;
        
        if (result.getPredictionAccuracy() >= 0.5) {
            correctPredictions++;
        }
        
        overallAccuracy = (double) correctPredictions / totalPredictions;
        lastUpdateTime = new Date();
        
        Log.d(TAG, "Updated overall accuracy: " + String.format("%.2f%%", overallAccuracy * 100));
    }
    
    /**
     * Update activity-specific accuracy
     */
    private void updateActivityAccuracy(PredictionResult result) {
        ActivityType predictedActivity = result.getPredictedActivity();
        ActivityType actualActivity = result.getActualActivity();
        
        if (predictedActivity != null) {
            ActivityAccuracy accuracy = activityAccuracies.get(predictedActivity);
            if (accuracy != null) {
                accuracy.addPrediction(result.getPredictionAccuracy());
            }
        }
        
        if (actualActivity != null && actualActivity != predictedActivity) {
            ActivityAccuracy accuracy = activityAccuracies.get(actualActivity);
            if (accuracy != null) {
                accuracy.recordMissedPrediction();
            }
        }
    }
    
    /**
     * Update weather-specific accuracy
     */
    private void updateWeatherAccuracy(PredictionResult result) {
        if (result.getWeatherContext() == null) return;
        
        String weatherCondition = result.getWeatherContext().getWeatherCondition();
        if (weatherCondition != null) {
            String normalizedCondition = normalizeWeatherCondition(weatherCondition);
            WeatherAccuracy accuracy = weatherAccuracies.get(normalizedCondition);
            if (accuracy != null) {
                accuracy.addPrediction(result.getPredictionAccuracy());
            }
        }
    }
    
    /**
     * Update time-specific accuracy
     */
    private void updateTimeAccuracy(PredictionResult result) {
        if (result.getWeatherContext() == null) return;
        
        String timeOfDay = result.getWeatherContext().getTimeOfDay();
        if (timeOfDay != null) {
            TimeAccuracy accuracy = timeAccuracies.get(timeOfDay);
            if (accuracy != null) {
                accuracy.addPrediction(result.getPredictionAccuracy());
            }
        }
    }
    
    /**
     * Update method-specific accuracy
     */
    private void updateMethodAccuracy(PredictionResult result) {
        String method = result.getPredictionMethod();
        if (method != null) {
            AccuracyMetrics metrics = accuracyMetrics.computeIfAbsent(method, k -> new AccuracyMetrics(k));
            metrics.addPrediction(result.getPredictionAccuracy());
        }
    }
    
    /**
     * Get overall accuracy statistics
     */
    public Map<String, Double> getAccuracyStatistics() {
        Map<String, Double> stats = new HashMap<>();
        
        // Overall accuracy
        stats.put("overall_accuracy", overallAccuracy);
        stats.put("total_predictions", (double) totalPredictions);
        stats.put("correct_predictions", (double) correctPredictions);
        
        // Recent accuracy (last 30 days)
        double recentAccuracy = calculateRecentAccuracy();
        stats.put("recent_accuracy", recentAccuracy);
        
        // Method-specific accuracy
        for (Map.Entry<String, AccuracyMetrics> entry : accuracyMetrics.entrySet()) {
            stats.put(entry.getKey() + "_accuracy", entry.getValue().getAccuracy());
        }
        
        // Activity-specific accuracy
        for (Map.Entry<ActivityType, ActivityAccuracy> entry : activityAccuracies.entrySet()) {
            stats.put(entry.getKey().name().toLowerCase() + "_accuracy", entry.getValue().getAccuracy());
        }
        
        // Weather-specific accuracy
        for (Map.Entry<String, WeatherAccuracy> entry : weatherAccuracies.entrySet()) {
            stats.put("weather_" + entry.getKey() + "_accuracy", entry.getValue().getAccuracy());
        }
        
        // Time-specific accuracy
        for (Map.Entry<String, TimeAccuracy> entry : timeAccuracies.entrySet()) {
            stats.put("time_" + entry.getKey().toLowerCase() + "_accuracy", entry.getValue().getAccuracy());
        }
        
        return stats;
    }
    
    /**
     * Calculate recent accuracy (last 30 days)
     */
    private double calculateRecentAccuracy() {
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        
        List<PredictionResult> recentPredictions = predictionHistory.stream()
            .filter(result -> result.getPredictionTime().getTime() > thirtyDaysAgo)
            .collect(Collectors.toList());
        
        if (recentPredictions.isEmpty()) {
            return overallAccuracy;
        }
        
        double totalAccuracy = recentPredictions.stream()
            .mapToDouble(PredictionResult::getPredictionAccuracy)
            .sum();
        
        return totalAccuracy / recentPredictions.size();
    }
    
    /**
     * Get detailed accuracy breakdown
     */
    public Map<String, Object> getDetailedAccuracyBreakdown() {
        Map<String, Object> breakdown = new HashMap<>();
        
        // Overall metrics
        Map<String, Double> overallMetrics = new HashMap<>();
        overallMetrics.put("accuracy", overallAccuracy);
        overallMetrics.put("total_predictions", (double) totalPredictions);
        overallMetrics.put("recent_accuracy", calculateRecentAccuracy());
        breakdown.put("overall", overallMetrics);
        
        // Method breakdown
        Map<String, Map<String, Double>> methodBreakdown = new HashMap<>();
        for (Map.Entry<String, AccuracyMetrics> entry : accuracyMetrics.entrySet()) {
            Map<String, Double> methodStats = new HashMap<>();
            AccuracyMetrics metrics = entry.getValue();
            methodStats.put("accuracy", metrics.getAccuracy());
            methodStats.put("total_predictions", (double) metrics.getTotalPredictions());
            methodStats.put("confidence", metrics.getAverageConfidence());
            methodBreakdown.put(entry.getKey(), methodStats);
        }
        breakdown.put("methods", methodBreakdown);
        
        // Activity breakdown
        Map<String, Map<String, Double>> activityBreakdown = new HashMap<>();
        for (Map.Entry<ActivityType, ActivityAccuracy> entry : activityAccuracies.entrySet()) {
            Map<String, Double> activityStats = new HashMap<>();
            ActivityAccuracy accuracy = entry.getValue();
            activityStats.put("accuracy", accuracy.getAccuracy());
            activityStats.put("total_predictions", (double) accuracy.getTotalPredictions());
            activityStats.put("missed_predictions", (double) accuracy.getMissedPredictions());
            activityBreakdown.put(entry.getKey().name(), activityStats);
        }
        breakdown.put("activities", activityBreakdown);
        
        // Weather breakdown
        Map<String, Map<String, Double>> weatherBreakdown = new HashMap<>();
        for (Map.Entry<String, WeatherAccuracy> entry : weatherAccuracies.entrySet()) {
            Map<String, Double> weatherStats = new HashMap<>();
            WeatherAccuracy accuracy = entry.getValue();
            weatherStats.put("accuracy", accuracy.getAccuracy());
            weatherStats.put("total_predictions", (double) accuracy.getTotalPredictions());
            weatherBreakdown.put(entry.getKey(), weatherStats);
        }
        breakdown.put("weather", weatherBreakdown);
        
        // Time breakdown
        Map<String, Map<String, Double>> timeBreakdown = new HashMap<>();
        for (Map.Entry<String, TimeAccuracy> entry : timeAccuracies.entrySet()) {
            Map<String, Double> timeStats = new HashMap<>();
            TimeAccuracy accuracy = entry.getValue();
            timeStats.put("accuracy", accuracy.getAccuracy());
            timeStats.put("total_predictions", (double) accuracy.getTotalPredictions());
            timeBreakdown.put(entry.getKey(), timeStats);
        }
        breakdown.put("time", timeBreakdown);
        
        return breakdown;
    }
    
    /**
     * Get improvement suggestions based on accuracy patterns
     */
    public List<String> getImprovementSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        // Check overall accuracy
        if (overallAccuracy < 0.6) {
            suggestions.add("Overall prediction accuracy is low. Consider collecting more training data.");
        }
        
        // Check method performance
        String bestMethod = accuracyMetrics.entrySet().stream()
            .max(Map.Entry.comparingByValue((m1, m2) -> Double.compare(m1.getAccuracy(), m2.getAccuracy())))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (bestMethod != null) {
            suggestions.add("The " + bestMethod + " method performs best. Consider increasing its weight.");
        }
        
        // Check activity performance
        List<ActivityType> poorActivities = activityAccuracies.entrySet().stream()
            .filter(entry -> entry.getValue().getAccuracy() < 0.4)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        if (!poorActivities.isEmpty()) {
            suggestions.add("Poor prediction accuracy for: " + 
                poorActivities.stream().map(Enum::name).collect(Collectors.joining(", ")));
        }
        
        // Check weather performance
        List<String> poorWeatherConditions = weatherAccuracies.entrySet().stream()
            .filter(entry -> entry.getValue().getAccuracy() < 0.4)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        if (!poorWeatherConditions.isEmpty()) {
            suggestions.add("Poor prediction accuracy for weather conditions: " + 
                String.join(", ", poorWeatherConditions));
        }
        
        // Check time performance
        List<String> poorTimeSlots = timeAccuracies.entrySet().stream()
            .filter(entry -> entry.getValue().getAccuracy() < 0.4)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        if (!poorTimeSlots.isEmpty()) {
            suggestions.add("Poor prediction accuracy for time slots: " + 
                String.join(", ", poorTimeSlots));
        }
        
        // Check data quantity
        if (totalPredictions < 50) {
            suggestions.add("Insufficient prediction data. More predictions needed for reliable accuracy measurement.");
        }
        
        return suggestions;
    }
    
    /**
     * Get accuracy trends over time
     */
    public Map<String, List<Double>> getAccuracyTrends() {
        Map<String, List<Double>> trends = new HashMap<>();
        
        // Calculate weekly accuracy trends
        List<Double> weeklyAccuracy = calculateWeeklyAccuracy();
        trends.put("weekly", weeklyAccuracy);
        
        // Calculate daily accuracy trends
        List<Double> dailyAccuracy = calculateDailyAccuracy();
        trends.put("daily", dailyAccuracy);
        
        return trends;
    }
    
    /**
     * Calculate weekly accuracy trends
     */
    private List<Double> calculateWeeklyAccuracy() {
        List<Double> weeklyAccuracy = new ArrayList<>();
        
        long oneWeek = 7L * 24 * 60 * 60 * 1000;
        long currentTime = System.currentTimeMillis();
        
        for (int i = 0; i < 8; i++) { // Last 8 weeks
            long weekStart = currentTime - (oneWeek * (i + 1));
            long weekEnd = currentTime - (oneWeek * i);
            
            List<PredictionResult> weekPredictions = predictionHistory.stream()
                .filter(result -> {
                    long time = result.getPredictionTime().getTime();
                    return time >= weekStart && time < weekEnd;
                })
                .collect(Collectors.toList());
            
            if (!weekPredictions.isEmpty()) {
                double accuracy = weekPredictions.stream()
                    .mapToDouble(PredictionResult::getPredictionAccuracy)
                    .average()
                    .orElse(0.0);
                weeklyAccuracy.add(accuracy);
            } else {
                weeklyAccuracy.add(0.0);
            }
        }
        
        Collections.reverse(weeklyAccuracy);
        return weeklyAccuracy;
    }
    
    /**
     * Calculate daily accuracy trends
     */
    private List<Double> calculateDailyAccuracy() {
        List<Double> dailyAccuracy = new ArrayList<>();
        
        long oneDay = 24L * 60 * 60 * 1000;
        long currentTime = System.currentTimeMillis();
        
        for (int i = 0; i < 14; i++) { // Last 14 days
            long dayStart = currentTime - (oneDay * (i + 1));
            long dayEnd = currentTime - (oneDay * i);
            
            List<PredictionResult> dayPredictions = predictionHistory.stream()
                .filter(result -> {
                    long time = result.getPredictionTime().getTime();
                    return time >= dayStart && time < dayEnd;
                })
                .collect(Collectors.toList());
            
            if (!dayPredictions.isEmpty()) {
                double accuracy = dayPredictions.stream()
                    .mapToDouble(PredictionResult::getPredictionAccuracy)
                    .average()
                    .orElse(0.0);
                dailyAccuracy.add(accuracy);
            } else {
                dailyAccuracy.add(0.0);
            }
        }
        
        Collections.reverse(dailyAccuracy);
        return dailyAccuracy;
    }
    
    /**
     * Log improvement suggestions
     */
    private void logImprovementSuggestions() {
        if (totalPredictions % 10 == 0) { // Log every 10 predictions
            List<String> suggestions = getImprovementSuggestions();
            for (String suggestion : suggestions) {
                Log.i(TAG, "Improvement suggestion: " + suggestion);
            }
        }
    }
    
    /**
     * Normalize weather condition strings
     */
    private String normalizeWeatherCondition(String condition) {
        if (condition == null) return "unknown";
        
        String normalized = condition.toLowerCase();
        if (normalized.contains("clear") || normalized.contains("sunny")) return "clear";
        if (normalized.contains("cloud") || normalized.contains("overcast")) return "cloudy";
        if (normalized.contains("rain") || normalized.contains("drizzle")) return "rain";
        if (normalized.contains("storm") || normalized.contains("thunder")) return "storm";
        if (normalized.contains("snow") || normalized.contains("sleet")) return "snow";
        if (normalized.contains("fog") || normalized.contains("mist")) return "fog";
        
        return "unknown";
    }
    
    /**
     * Reset accuracy tracking (for testing or fresh start)
     */
    public void resetAccuracyTracking() {
        predictionHistory.clear();
        accuracyMetrics.clear();
        activityAccuracies.clear();
        weatherAccuracies.clear();
        timeAccuracies.clear();
        
        overallAccuracy = 0.0;
        totalPredictions = 0;
        correctPredictions = 0;
        lastUpdateTime = new Date();
        
        // Reinitialize metrics
        initializeMetrics();
        
        // Clear saved data
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        
        Log.d(TAG, "Accuracy tracking reset");
    }
    
    /**
     * Get accuracy summary for reporting
     */
    public String getAccuracySummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("=== Prediction Accuracy Summary ===\n");
        summary.append(String.format("Overall Accuracy: %.2f%% (%d/%d)\n", 
            overallAccuracy * 100, correctPredictions, totalPredictions));
        summary.append(String.format("Recent Accuracy: %.2f%%\n", calculateRecentAccuracy() * 100));
        summary.append(String.format("Last Updated: %s\n", 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(lastUpdateTime)));
        
        summary.append("\n=== Method Performance ===\n");
        for (Map.Entry<String, AccuracyMetrics> entry : accuracyMetrics.entrySet()) {
            AccuracyMetrics metrics = entry.getValue();
            summary.append(String.format("%s: %.2f%% (%d predictions)\n", 
                entry.getKey(), metrics.getAccuracy() * 100, metrics.getTotalPredictions()));
        }
        
        summary.append("\n=== Activity Performance ===\n");
        for (Map.Entry<ActivityType, ActivityAccuracy> entry : activityAccuracies.entrySet()) {
            ActivityAccuracy accuracy = entry.getValue();
            if (accuracy.getTotalPredictions() > 0) {
                summary.append(String.format("%s: %.2f%% (%d predictions)\n", 
                    entry.getKey().name(), accuracy.getAccuracy() * 100, accuracy.getTotalPredictions()));
            }
        }
        
        return summary.toString();
    }
    
    // Inner classes for accuracy tracking
    
    private static class AccuracyMetrics {
        private String methodName;
        private double totalAccuracy;
        private int totalPredictions;
        private double totalConfidence;
        
        public AccuracyMetrics(String methodName) {
            this.methodName = methodName;
            this.totalAccuracy = 0.0;
            this.totalPredictions = 0;
            this.totalConfidence = 0.0;
        }
        
        public void addPrediction(double accuracy) {
            this.totalAccuracy += accuracy;
            this.totalPredictions++;
        }
        
        public void addPrediction(double accuracy, double confidence) {
            this.totalAccuracy += accuracy;
            this.totalConfidence += confidence;
            this.totalPredictions++;
        }
        
        public double getAccuracy() {
            return totalPredictions > 0 ? totalAccuracy / totalPredictions : 0.0;
        }
        
        public double getAverageConfidence() {
            return totalPredictions > 0 ? totalConfidence / totalPredictions : 0.0;
        }
        
        public int getTotalPredictions() {
            return totalPredictions;
        }
        
        public String getMethodName() {
            return methodName;
        }
    }
    
    private static class ActivityAccuracy {
        private ActivityType activityType;
        private double totalAccuracy;
        private int totalPredictions;
        private int missedPredictions;
        
        public ActivityAccuracy(ActivityType activityType) {
            this.activityType = activityType;
            this.totalAccuracy = 0.0;
            this.totalPredictions = 0;
            this.missedPredictions = 0;
        }
        
        public void addPrediction(double accuracy) {
            this.totalAccuracy += accuracy;
            this.totalPredictions++;
        }
        
        public void recordMissedPrediction() {
            this.missedPredictions++;
        }
        
        public double getAccuracy() {
            return totalPredictions > 0 ? totalAccuracy / totalPredictions : 0.0;
        }
        
        public int getTotalPredictions() {
            return totalPredictions;
        }
        
        public int getMissedPredictions() {
            return missedPredictions;
        }
        
        public ActivityType getActivityType() {
            return activityType;
        }
    }
    
    private static class WeatherAccuracy {
        private String weatherCondition;
        private double totalAccuracy;
        private int totalPredictions;
        
        public WeatherAccuracy(String weatherCondition) {
            this.weatherCondition = weatherCondition;
            this.totalAccuracy = 0.0;
            this.totalPredictions = 0;
        }
        
        public void addPrediction(double accuracy) {
            this.totalAccuracy += accuracy;
            this.totalPredictions++;
        }
        
        public double getAccuracy() {
            return totalPredictions > 0 ? totalAccuracy / totalPredictions : 0.0;
        }
        
        public int getTotalPredictions() {
            return totalPredictions;
        }
        
        public String getWeatherCondition() {
            return weatherCondition;
        }
    }
    
    private static class TimeAccuracy {
        private String timeSlot;
        private double totalAccuracy;
        private int totalPredictions;
        
        public TimeAccuracy(String timeSlot) {
            this.timeSlot = timeSlot;
            this.totalAccuracy = 0.0;
            this.totalPredictions = 0;
        }
        
        public void addPrediction(double accuracy) {
            this.totalAccuracy += accuracy;
            this.totalPredictions++;
        }
        
        public double getAccuracy() {
            return totalPredictions > 0 ? totalAccuracy / totalPredictions : 0.0;
        }
        
        public int getTotalPredictions() {
            return totalPredictions;
        }
        
        public String getTimeSlot() {
            return timeSlot;
        }
    }
}