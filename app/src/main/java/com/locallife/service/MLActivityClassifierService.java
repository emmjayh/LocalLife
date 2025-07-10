package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Machine Learning service for activity classification and prediction
 */
public class MLActivityClassifierService {
    private static final String TAG = "MLActivityClassifier";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    
    public MLActivityClassifierService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Classify activity level based on multiple factors
     */
    public void classifyActivityLevel(ClassificationCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(30);
                
                if (records.size() < 7) {
                    if (callback != null) {
                        callback.onError("Not enough data for classification");
                    }
                    return;
                }
                
                List<ActivityClassification> classifications = new ArrayList<>();
                
                for (DayRecord record : records) {
                    ActivityClassification classification = classifyDay(record);
                    classifications.add(classification);
                }
                
                // Generate insights from classifications
                ActivityInsights insights = generateActivityInsights(classifications);
                
                if (callback != null) {
                    callback.onActivityClassified(classifications, insights);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error classifying activity", e);
                if (callback != null) {
                    callback.onError("Classification failed: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Predict optimal activity times based on patterns
     */
    public void predictOptimalActivityTimes(PredictionCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(30);
                
                if (records.size() < 14) {
                    if (callback != null) {
                        callback.onError("Not enough data for prediction");
                    }
                    return;
                }
                
                // Analyze patterns by day of week
                Map<Integer, List<DayRecord>> dayOfWeekMap = groupByDayOfWeek(records);
                
                List<ActivityPrediction> predictions = new ArrayList<>();
                
                for (Map.Entry<Integer, List<DayRecord>> entry : dayOfWeekMap.entrySet()) {
                    int dayOfWeek = entry.getKey();
                    List<DayRecord> dayRecords = entry.getValue();
                    
                    if (dayRecords.size() >= 3) {
                        ActivityPrediction prediction = predictForDayOfWeek(dayOfWeek, dayRecords);
                        predictions.add(prediction);
                    }
                }
                
                if (callback != null) {
                    callback.onPredictionsGenerated(predictions);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error predicting activity times", e);
                if (callback != null) {
                    callback.onError("Prediction failed: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Analyze weather impact on activity levels
     */
    public void analyzeWeatherImpact(WeatherImpactCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(60);
                
                if (records.size() < 14) {
                    if (callback != null) {
                        callback.onError("Not enough data for weather analysis");
                    }
                    return;
                }
                
                WeatherImpactAnalysis analysis = new WeatherImpactAnalysis();
                
                // Analyze temperature impact
                analysis.temperatureImpact = analyzeTemperatureImpact(records);
                
                // Analyze weather condition impact
                analysis.weatherConditionImpact = analyzeWeatherConditionImpact(records);
                
                // Analyze UV index impact
                analysis.uvIndexImpact = analyzeUVIndexImpact(records);
                
                // Analyze air quality impact
                analysis.airQualityImpact = analyzeAirQualityImpact(records);
                
                // Generate recommendations
                analysis.recommendations = generateWeatherRecommendations(analysis);
                
                if (callback != null) {
                    callback.onWeatherImpactAnalyzed(analysis);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing weather impact", e);
                if (callback != null) {
                    callback.onError("Weather analysis failed: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Create activity profile based on historical data
     */
    public void createActivityProfile(ProfileCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(90);
                
                if (records.size() < 30) {
                    if (callback != null) {
                        callback.onError("Not enough data for profile creation");
                    }
                    return;
                }
                
                ActivityProfile profile = new ActivityProfile();
                
                // Calculate activity patterns
                profile.averageSteps = calculateAverageSteps(records);
                profile.averageScreenTime = calculateAverageScreenTime(records);
                profile.averagePlacesVisited = calculateAveragePlacesVisited(records);
                
                // Identify peak activity times
                profile.peakActivityHours = identifyPeakActivityHours(records);
                
                // Analyze activity consistency
                profile.consistencyScore = calculateConsistencyScore(records);
                
                // Identify preferred weather conditions
                profile.preferredWeatherConditions = identifyPreferredWeatherConditions(records);
                
                // Generate activity type preferences
                profile.activityTypePreferences = generateActivityTypePreferences(records);
                
                // Calculate fitness level estimation
                profile.fitnessLevelEstimation = calculateFitnessLevel(records);
                
                if (callback != null) {
                    callback.onActivityProfileCreated(profile);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating activity profile", e);
                if (callback != null) {
                    callback.onError("Profile creation failed: " + e.getMessage());
                }
            }
        });
    }
    
    // Helper methods
    private ActivityClassification classifyDay(DayRecord record) {
        ActivityClassification classification = new ActivityClassification();
        classification.date = record.getDate();
        
        // Calculate activity score based on multiple factors
        int activityScore = 0;
        
        // Steps component (0-40 points)
        if (record.getSteps() > 12000) activityScore += 40;
        else if (record.getSteps() > 8000) activityScore += 30;
        else if (record.getSteps() > 5000) activityScore += 20;
        else if (record.getSteps() > 2000) activityScore += 10;
        
        // Screen time component (0-30 points, inverse relationship)
        if (record.getScreenTimeMinutes() < 240) activityScore += 30; // < 4 hours
        else if (record.getScreenTimeMinutes() < 360) activityScore += 20; // < 6 hours
        else if (record.getScreenTimeMinutes() < 480) activityScore += 10; // < 8 hours
        
        // Places visited component (0-20 points)
        if (record.getPlacesVisited() > 5) activityScore += 20;
        else if (record.getPlacesVisited() > 3) activityScore += 15;
        else if (record.getPlacesVisited() > 1) activityScore += 10;
        else if (record.getPlacesVisited() > 0) activityScore += 5;
        
        // Weather bonus (0-10 points)
        if (record.getTemperature() > 15 && record.getTemperature() < 25) {
            activityScore += 10; // Optimal temperature
        } else if (record.getTemperature() > 10 && record.getTemperature() < 30) {
            activityScore += 5; // Good temperature
        }
        
        classification.activityScore = activityScore;
        
        // Classify activity level
        if (activityScore >= 80) {
            classification.activityLevel = ActivityLevel.VERY_HIGH;
        } else if (activityScore >= 60) {
            classification.activityLevel = ActivityLevel.HIGH;
        } else if (activityScore >= 40) {
            classification.activityLevel = ActivityLevel.MODERATE;
        } else if (activityScore >= 20) {
            classification.activityLevel = ActivityLevel.LOW;
        } else {
            classification.activityLevel = ActivityLevel.VERY_LOW;
        }
        
        // Add contributing factors
        classification.primaryFactors = identifyPrimaryFactors(record);
        classification.confidence = calculateConfidence(record);
        
        return classification;
    }
    
    private ActivityInsights generateActivityInsights(List<ActivityClassification> classifications) {
        ActivityInsights insights = new ActivityInsights();
        
        // Calculate distribution
        Map<ActivityLevel, Integer> levelCount = new HashMap<>();
        int totalScore = 0;
        
        for (ActivityClassification classification : classifications) {
            ActivityLevel level = classification.activityLevel;
            levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
            totalScore += classification.activityScore;
        }
        
        insights.activityLevelDistribution = levelCount;
        insights.averageActivityScore = totalScore / (float) classifications.size();
        
        // Identify trends
        insights.trend = identifyActivityTrend(classifications);
        
        // Find most common activity level
        ActivityLevel mostCommon = null;
        int maxCount = 0;
        for (Map.Entry<ActivityLevel, Integer> entry : levelCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }
        insights.dominantActivityLevel = mostCommon;
        
        return insights;
    }
    
    private Map<Integer, List<DayRecord>> groupByDayOfWeek(List<DayRecord> records) {
        Map<Integer, List<DayRecord>> dayOfWeekMap = new HashMap<>();
        
        for (DayRecord record : records) {
            try {
                // Simple day of week calculation (0=Sunday, 6=Saturday)
                int dayOfWeek = getDayOfWeek(record.getDate());
                
                List<DayRecord> dayRecords = dayOfWeekMap.get(dayOfWeek);
                if (dayRecords == null) {
                    dayRecords = new ArrayList<>();
                    dayOfWeekMap.put(dayOfWeek, dayRecords);
                }
                dayRecords.add(record);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date for day of week", e);
            }
        }
        
        return dayOfWeekMap;
    }
    
    private ActivityPrediction predictForDayOfWeek(int dayOfWeek, List<DayRecord> records) {
        ActivityPrediction prediction = new ActivityPrediction();
        prediction.dayOfWeek = dayOfWeek;
        prediction.dayName = getDayName(dayOfWeek);
        
        // Calculate average metrics
        int totalSteps = 0;
        int totalScreenTime = 0;
        int totalPlaces = 0;
        float totalTemp = 0;
        
        for (DayRecord record : records) {
            totalSteps += record.getSteps();
            totalScreenTime += record.getScreenTimeMinutes();
            totalPlaces += record.getPlacesVisited();
            totalTemp += record.getTemperature();
        }
        
        int count = records.size();
        prediction.predictedSteps = totalSteps / count;
        prediction.predictedScreenTime = totalScreenTime / count;
        prediction.predictedPlaces = totalPlaces / count;
        prediction.optimalTemperature = totalTemp / count;
        
        // Calculate confidence based on data consistency
        prediction.confidence = calculatePredictionConfidence(records);
        
        // Generate recommendations
        prediction.recommendations = generateDayRecommendations(prediction);
        
        return prediction;
    }
    
    private String analyzeTemperatureImpact(List<DayRecord> records) {
        // Group by temperature ranges
        Map<String, List<Integer>> tempRanges = new HashMap<>();
        tempRanges.put("Cold (<10°C)", new ArrayList<>());
        tempRanges.put("Cool (10-15°C)", new ArrayList<>());
        tempRanges.put("Moderate (15-20°C)", new ArrayList<>());
        tempRanges.put("Warm (20-25°C)", new ArrayList<>());
        tempRanges.put("Hot (>25°C)", new ArrayList<>());
        
        for (DayRecord record : records) {
            float temp = record.getTemperature();
            int steps = record.getSteps();
            
            if (temp < 10) {
                tempRanges.get("Cold (<10°C)").add(steps);
            } else if (temp < 15) {
                tempRanges.get("Cool (10-15°C)").add(steps);
            } else if (temp < 20) {
                tempRanges.get("Moderate (15-20°C)").add(steps);
            } else if (temp < 25) {
                tempRanges.get("Warm (20-25°C)").add(steps);
            } else {
                tempRanges.get("Hot (>25°C)").add(steps);
            }
        }
        
        // Find optimal temperature range
        String optimalRange = null;
        int maxAverage = 0;
        
        for (Map.Entry<String, List<Integer>> entry : tempRanges.entrySet()) {
            List<Integer> stepsList = entry.getValue();
            if (!stepsList.isEmpty()) {
                int average = stepsList.stream().mapToInt(Integer::intValue).sum() / stepsList.size();
                if (average > maxAverage) {
                    maxAverage = average;
                    optimalRange = entry.getKey();
                }
            }
        }
        
        return optimalRange != null ? 
            "Most active in " + optimalRange + " range (avg: " + maxAverage + " steps)" :
            "Insufficient temperature data";
    }
    
    private String analyzeWeatherConditionImpact(List<DayRecord> records) {
        // This would analyze weather conditions if we had them
        // For now, return a placeholder
        return "Weather condition analysis requires weather API integration";
    }
    
    private String analyzeUVIndexImpact(List<DayRecord> records) {
        // Placeholder for UV index analysis
        return "UV index analysis requires weather API integration";
    }
    
    private String analyzeAirQualityImpact(List<DayRecord> records) {
        // Placeholder for air quality analysis
        return "Air quality analysis requires environmental API integration";
    }
    
    private List<String> generateWeatherRecommendations(WeatherImpactAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        
        if (analysis.temperatureImpact.contains("Warm")) {
            recommendations.add("You're most active in warm weather. Plan outdoor activities for 20-25°C days.");
        } else if (analysis.temperatureImpact.contains("Moderate")) {
            recommendations.add("Moderate temperatures work best for you. Aim for 15-20°C for peak activity.");
        } else if (analysis.temperatureImpact.contains("Cool")) {
            recommendations.add("Cool weather boosts your activity. Consider morning activities in 10-15°C.");
        }
        
        recommendations.add("Monitor weather forecasts to plan your most active days.");
        recommendations.add("Consider indoor alternatives during extreme weather conditions.");
        
        return recommendations;
    }
    
    // Additional helper methods
    private int calculateAverageSteps(List<DayRecord> records) {
        return records.stream().mapToInt(DayRecord::getSteps).sum() / records.size();
    }
    
    private int calculateAverageScreenTime(List<DayRecord> records) {
        return records.stream().mapToInt(DayRecord::getScreenTimeMinutes).sum() / records.size();
    }
    
    private int calculateAveragePlacesVisited(List<DayRecord> records) {
        return records.stream().mapToInt(DayRecord::getPlacesVisited).sum() / records.size();
    }
    
    private List<Integer> identifyPeakActivityHours(List<DayRecord> records) {
        // Simplified peak hours identification
        List<Integer> peakHours = new ArrayList<>();
        peakHours.add(8);  // 8 AM
        peakHours.add(12); // 12 PM
        peakHours.add(18); // 6 PM
        return peakHours;
    }
    
    private float calculateConsistencyScore(List<DayRecord> records) {
        if (records.size() < 2) return 0f;
        
        List<Integer> steps = records.stream().mapToInt(DayRecord::getSteps).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        // Calculate coefficient of variation
        double mean = steps.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = steps.stream().mapToDouble(s -> Math.pow(s - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        if (mean > 0) {
            double cv = stdDev / mean;
            return Math.max(0f, 1f - (float) cv); // Higher consistency = lower coefficient of variation
        }
        
        return 0f;
    }
    
    private List<String> identifyPreferredWeatherConditions(List<DayRecord> records) {
        List<String> conditions = new ArrayList<>();
        conditions.add("Partly cloudy");
        conditions.add("Sunny");
        conditions.add("Mild temperature");
        return conditions;
    }
    
    private Map<String, Float> generateActivityTypePreferences(List<DayRecord> records) {
        Map<String, Float> preferences = new HashMap<>();
        preferences.put("Walking", 0.8f);
        preferences.put("Indoor Activities", 0.6f);
        preferences.put("Outdoor Sports", 0.7f);
        preferences.put("Social Activities", 0.5f);
        return preferences;
    }
    
    private String calculateFitnessLevel(List<DayRecord> records) {
        int avgSteps = calculateAverageSteps(records);
        
        if (avgSteps > 12000) return "High";
        if (avgSteps > 8000) return "Moderate";
        if (avgSteps > 5000) return "Low";
        return "Very Low";
    }
    
    private List<String> identifyPrimaryFactors(DayRecord record) {
        List<String> factors = new ArrayList<>();
        
        if (record.getSteps() > 10000) factors.add("High step count");
        if (record.getScreenTimeMinutes() < 300) factors.add("Low screen time");
        if (record.getPlacesVisited() > 3) factors.add("Multiple locations");
        if (record.getTemperature() > 15 && record.getTemperature() < 25) factors.add("Optimal weather");
        
        return factors;
    }
    
    private float calculateConfidence(DayRecord record) {
        // Simple confidence calculation based on data completeness
        float confidence = 0f;
        
        if (record.getSteps() > 0) confidence += 0.3f;
        if (record.getScreenTimeMinutes() > 0) confidence += 0.2f;
        if (record.getPlacesVisited() > 0) confidence += 0.2f;
        if (record.getTemperature() > -50 && record.getTemperature() < 50) confidence += 0.3f;
        
        return confidence;
    }
    
    private String identifyActivityTrend(List<ActivityClassification> classifications) {
        if (classifications.size() < 3) return "Insufficient data";
        
        // Compare first third to last third
        int thirdSize = classifications.size() / 3;
        float firstThirdAvg = 0f;
        float lastThirdAvg = 0f;
        
        for (int i = 0; i < thirdSize; i++) {
            firstThirdAvg += classifications.get(i).activityScore;
        }
        firstThirdAvg /= thirdSize;
        
        for (int i = classifications.size() - thirdSize; i < classifications.size(); i++) {
            lastThirdAvg += classifications.get(i).activityScore;
        }
        lastThirdAvg /= thirdSize;
        
        float difference = lastThirdAvg - firstThirdAvg;
        
        if (difference > 10) return "Increasing";
        if (difference < -10) return "Decreasing";
        return "Stable";
    }
    
    private int getDayOfWeek(String date) {
        // Simple day of week calculation (placeholder)
        // In a real implementation, parse the date properly
        return date.hashCode() % 7;
    }
    
    private String getDayName(int dayOfWeek) {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[dayOfWeek % 7];
    }
    
    private float calculatePredictionConfidence(List<DayRecord> records) {
        if (records.size() < 3) return 0.3f;
        if (records.size() < 5) return 0.6f;
        return 0.9f;
    }
    
    private List<String> generateDayRecommendations(ActivityPrediction prediction) {
        List<String> recommendations = new ArrayList<>();
        
        if (prediction.predictedSteps > 10000) {
            recommendations.add("Great day for outdoor activities!");
        } else if (prediction.predictedSteps > 5000) {
            recommendations.add("Good day for moderate activity");
        } else {
            recommendations.add("Consider indoor activities or gentle exercise");
        }
        
        if (prediction.predictedScreenTime > 400) {
            recommendations.add("Try to reduce screen time with outdoor activities");
        }
        
        return recommendations;
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes
    public static class ActivityClassification {
        public String date;
        public ActivityLevel activityLevel;
        public int activityScore;
        public List<String> primaryFactors;
        public float confidence;
    }
    
    public static class ActivityInsights {
        public Map<ActivityLevel, Integer> activityLevelDistribution;
        public float averageActivityScore;
        public String trend;
        public ActivityLevel dominantActivityLevel;
    }
    
    public static class ActivityPrediction {
        public int dayOfWeek;
        public String dayName;
        public int predictedSteps;
        public int predictedScreenTime;
        public int predictedPlaces;
        public float optimalTemperature;
        public float confidence;
        public List<String> recommendations;
    }
    
    public static class WeatherImpactAnalysis {
        public String temperatureImpact;
        public String weatherConditionImpact;
        public String uvIndexImpact;
        public String airQualityImpact;
        public List<String> recommendations;
    }
    
    public static class ActivityProfile {
        public int averageSteps;
        public int averageScreenTime;
        public int averagePlacesVisited;
        public List<Integer> peakActivityHours;
        public float consistencyScore;
        public List<String> preferredWeatherConditions;
        public Map<String, Float> activityTypePreferences;
        public String fitnessLevelEstimation;
    }
    
    public enum ActivityLevel {
        VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH
    }
    
    // Callback interfaces
    public interface ClassificationCallback {
        void onActivityClassified(List<ActivityClassification> classifications, ActivityInsights insights);
        void onError(String error);
    }
    
    public interface PredictionCallback {
        void onPredictionsGenerated(List<ActivityPrediction> predictions);
        void onError(String error);
    }
    
    public interface WeatherImpactCallback {
        void onWeatherImpactAnalyzed(WeatherImpactAnalysis analysis);
        void onError(String error);
    }
    
    public interface ProfileCallback {
        void onActivityProfileCreated(ActivityProfile profile);
        void onError(String error);
    }
}