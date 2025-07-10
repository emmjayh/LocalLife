package com.locallife.service;

import android.util.Log;

import com.locallife.model.ActivityType;
import com.locallife.model.DayRecord;
import com.locallife.model.PredictionResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Machine Learning model for predicting activity based on user behavior patterns
 */
public class UserBehaviorModel {
    private static final String TAG = "UserBehaviorModel";
    private static final double LEARNING_RATE = 0.02;
    private static final int MIN_TRAINING_SAMPLES = 15;
    private static final int PATTERN_WINDOW_DAYS = 7;
    
    private boolean isTrained = false;
    private Map<String, UserBehaviorPattern> behaviorPatterns;
    private Map<ActivityType, Map<String, Double>> activityBehaviorWeights;
    private Map<String, Double> featureWeights;
    private double bias;
    
    // User preference patterns
    private Map<String, Double> timePreferences; // hour -> preference
    private Map<String, Double> dayPreferences; // day_of_week -> preference
    private Map<ActivityType, Double> activityPreferences;
    private Map<String, Double> locationPreferences;
    
    // Behavioral metrics
    private double averageStepCount;
    private double averageActiveMinutes;
    private double averageScreenTime;
    private double averagePlacesVisited;
    
    // Training statistics
    private int trainingDataSize;
    private double trainingAccuracy;
    private Date lastTrainingTime;
    
    public UserBehaviorModel() {
        this.behaviorPatterns = new HashMap<>();
        this.activityBehaviorWeights = new HashMap<>();
        this.featureWeights = new HashMap<>();
        this.timePreferences = new HashMap<>();
        this.dayPreferences = new HashMap<>();
        this.activityPreferences = new HashMap<>();
        this.locationPreferences = new HashMap<>();
        this.bias = 0.0;
        
        // Initialize feature weights
        initializeFeatureWeights();
    }
    
    /**
     * Initialize feature weights for user behavior prediction
     */
    private void initializeFeatureWeights() {
        featureWeights.put("step_count", 0.25);
        featureWeights.put("active_minutes", 0.2);
        featureWeights.put("screen_time", 0.15);
        featureWeights.put("places_visited", 0.15);
        featureWeights.put("time_of_day", 0.15);
        featureWeights.put("day_of_week", 0.1);
    }
    
    /**
     * Train the model with historical data
     */
    public void train(List<DayRecord> historicalData) {
        if (historicalData.size() < MIN_TRAINING_SAMPLES) {
            Log.w(TAG, "Insufficient training data: " + historicalData.size() + " samples");
            return;
        }
        
        Log.d(TAG, "Training user behavior model with " + historicalData.size() + " samples");
        
        // Sort data by date
        List<DayRecord> sortedData = historicalData.stream()
            .sorted(Comparator.comparing(DayRecord::getDate))
            .collect(Collectors.toList());
        
        // Calculate user averages
        calculateUserAverages(sortedData);
        
        // Extract behavior patterns
        extractBehaviorPatterns(sortedData);
        
        // Learn user preferences
        learnUserPreferences(sortedData);
        
        // Train activity-behavior associations
        trainActivityBehaviorAssociations(sortedData);
        
        // Optimize feature weights
        optimizeFeatureWeights(sortedData);
        
        this.isTrained = true;
        this.trainingDataSize = sortedData.size();
        this.lastTrainingTime = new Date();
        
        // Calculate training accuracy
        this.trainingAccuracy = calculateTrainingAccuracy(sortedData);
        
        Log.d(TAG, "Model training completed with accuracy: " + String.format("%.2f%%", trainingAccuracy * 100));
    }
    
    /**
     * Calculate user behavioral averages
     */
    private void calculateUserAverages(List<DayRecord> data) {
        averageStepCount = data.stream().mapToInt(DayRecord::getStepCount).average().orElse(0.0);
        averageActiveMinutes = data.stream().mapToInt(DayRecord::getActiveMinutes).average().orElse(0.0);
        averageScreenTime = data.stream().mapToInt(DayRecord::getScreenTimeMinutes).average().orElse(0.0);
        averagePlacesVisited = data.stream().mapToInt(DayRecord::getPlacesVisited).average().orElse(0.0);
        
        Log.d(TAG, String.format("User averages - Steps: %.0f, Active: %.0f min, Screen: %.0f min, Places: %.0f",
            averageStepCount, averageActiveMinutes, averageScreenTime, averagePlacesVisited));
    }
    
    /**
     * Extract behavior patterns from historical data
     */
    private void extractBehaviorPatterns(List<DayRecord> data) {
        for (int i = 0; i < data.size(); i++) {
            DayRecord record = data.get(i);
            
            // Get context window
            List<DayRecord> contextWindow = getContextWindow(data, i);
            
            String patternKey = createBehaviorPatternKey(record, contextWindow);
            
            UserBehaviorPattern pattern = behaviorPatterns.computeIfAbsent(patternKey, k -> new UserBehaviorPattern());
            pattern.addDataPoint(record);
        }
        
        Log.d(TAG, "Extracted " + behaviorPatterns.size() + " behavior patterns");
    }
    
    /**
     * Learn user preferences from historical data
     */
    private void learnUserPreferences(List<DayRecord> data) {
        // Learn time preferences
        Map<String, List<Double>> timeActivityLevels = new HashMap<>();
        
        // Learn day preferences
        Map<String, List<Double>> dayActivityLevels = new HashMap<>();
        
        // Learn activity preferences
        Map<ActivityType, List<Double>> activityLevels = new HashMap<>();
        
        for (DayRecord record : data) {
            String dayOfWeek = getDayOfWeek(record.getDate());
            String timeSlot = "12"; // Simplified - would need actual time data
            
            // Calculate overall activity level
            double activityLevel = record.getActivityScore() / 100.0;
            
            // Time preferences
            timeActivityLevels.computeIfAbsent(timeSlot, k -> new ArrayList<>()).add(activityLevel);
            
            // Day preferences
            dayActivityLevels.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(activityLevel);
            
            // Activity type preferences
            for (ActivityType activityType : ActivityType.values()) {
                double typeLevel = calculateActivityLevel(activityType, record);
                activityLevels.computeIfAbsent(activityType, k -> new ArrayList<>()).add(typeLevel);
            }
        }
        
        // Convert to preferences
        for (Map.Entry<String, List<Double>> entry : timeActivityLevels.entrySet()) {
            double avgLevel = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            timePreferences.put(entry.getKey(), avgLevel);
        }
        
        for (Map.Entry<String, List<Double>> entry : dayActivityLevels.entrySet()) {
            double avgLevel = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            dayPreferences.put(entry.getKey(), avgLevel);
        }
        
        for (Map.Entry<ActivityType, List<Double>> entry : activityLevels.entrySet()) {
            double avgLevel = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            activityPreferences.put(entry.getKey(), avgLevel);
        }
    }
    
    /**
     * Train activity-behavior associations
     */
    private void trainActivityBehaviorAssociations(List<DayRecord> data) {
        for (ActivityType activityType : ActivityType.values()) {
            Map<String, Double> behaviorWeights = new HashMap<>();
            
            for (int i = 0; i < data.size(); i++) {
                DayRecord record = data.get(i);
                List<DayRecord> contextWindow = getContextWindow(data, i);
                
                String patternKey = createBehaviorPatternKey(record, contextWindow);
                double activityLevel = calculateActivityLevel(activityType, record);
                
                // Update weights based on activity level
                behaviorWeights.merge(patternKey, activityLevel, (existing, newValue) -> {
                    return existing + (LEARNING_RATE * (newValue - existing));
                });
            }
            
            activityBehaviorWeights.put(activityType, behaviorWeights);
        }
    }
    
    /**
     * Optimize feature weights using gradient descent
     */
    private void optimizeFeatureWeights(List<DayRecord> data) {
        for (int iteration = 0; iteration < 50; iteration++) {
            Map<String, Double> gradients = new HashMap<>();
            
            for (String feature : featureWeights.keySet()) {
                gradients.put(feature, 0.0);
            }
            
            // Calculate gradients
            for (DayRecord record : data) {
                Map<ActivityType, Double> predictions = predict(record);
                Map<ActivityType, Double> actuals = calculateActualActivities(record);
                
                for (ActivityType activityType : ActivityType.values()) {
                    double predicted = predictions.getOrDefault(activityType, 0.0);
                    double actual = actuals.getOrDefault(activityType, 0.0);
                    double error = actual - predicted;
                    
                    // Update gradients
                    for (String feature : featureWeights.keySet()) {
                        double featureValue = getFeatureValue(feature, record);
                        gradients.merge(feature, error * featureValue, Double::sum);
                    }
                }
            }
            
            // Update weights
            for (String feature : featureWeights.keySet()) {
                double gradient = gradients.get(feature) / data.size();
                featureWeights.put(feature, featureWeights.get(feature) + LEARNING_RATE * gradient);
            }
        }
    }
    
    /**
     * Predict activity probabilities based on user context
     */
    public Map<ActivityType, Double> predict(PredictionResult.UserContext userContext) {
        if (!isTrained) {
            Log.w(TAG, "Model not trained, returning default predictions");
            return getDefaultPredictions();
        }
        
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        for (ActivityType activityType : ActivityType.values()) {
            double score = calculateActivityScore(activityType, userContext);
            predictions.put(activityType, score);
        }
        
        return normalizePredictions(predictions);
    }
    
    /**
     * Predict activity probabilities based on day record
     */
    public Map<ActivityType, Double> predict(DayRecord record) {
        if (!isTrained) {
            Log.w(TAG, "Model not trained, returning default predictions");
            return getDefaultPredictions();
        }
        
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        // Get recent context (simplified)
        List<DayRecord> contextWindow = Arrays.asList(record);
        String patternKey = createBehaviorPatternKey(record, contextWindow);
        
        for (ActivityType activityType : ActivityType.values()) {
            Map<String, Double> behaviorWeights = activityBehaviorWeights.get(activityType);
            double score = behaviorWeights.getOrDefault(patternKey, 0.0);
            
            // Apply feature weights
            score *= calculateFeatureScore(record);
            
            // Apply user preferences
            score *= getActivityPreference(activityType);
            
            predictions.put(activityType, score);
        }
        
        return normalizePredictions(predictions);
    }
    
    /**
     * Calculate activity score for a specific activity type and user context
     */
    private double calculateActivityScore(ActivityType activityType, PredictionResult.UserContext userContext) {
        double score = 0.0;
        
        // Base activity preference
        score += getActivityPreference(activityType) * 0.3;
        
        // Recent activity patterns
        if (userContext.getRecentStepCount() > averageStepCount * 1.2) {
            if (activityType == ActivityType.RELAXATION) score += 0.2;
        } else if (userContext.getRecentStepCount() < averageStepCount * 0.8) {
            if (activityType == ActivityType.OUTDOOR_EXERCISE) score += 0.2;
        }
        
        // Screen time patterns
        if (userContext.getScreenTimeMinutes() > averageScreenTime * 1.5) {
            if (activityType == ActivityType.OUTDOOR_EXERCISE || activityType == ActivityType.SOCIAL_ACTIVITY) {
                score += 0.15;
            }
        }
        
        // Social patterns
        if (userContext.getSocialInteractions() > averagePlacesVisited * 1.2) {
            if (activityType == ActivityType.RELAXATION) score += 0.1;
        } else if (userContext.getSocialInteractions() < averagePlacesVisited * 0.8) {
            if (activityType == ActivityType.SOCIAL_ACTIVITY) score += 0.15;
        }
        
        // Location-based adjustments
        String location = userContext.getPrimaryLocation();
        if (location != null) {
            if (location.toLowerCase().contains("home")) {
                if (activityType == ActivityType.INDOOR_ACTIVITIES || activityType == ActivityType.RELAXATION) {
                    score += 0.1;
                }
            } else if (location.toLowerCase().contains("work")) {
                if (activityType == ActivityType.WORK_PRODUCTIVITY) {
                    score += 0.2;
                }
            }
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Calculate feature score for a day record
     */
    private double calculateFeatureScore(DayRecord record) {
        double score = 0.0;
        
        // Step count feature
        double stepScore = normalizeStepCount(record.getStepCount());
        score += stepScore * featureWeights.get("step_count");
        
        // Active minutes feature
        double activeScore = normalizeActiveMinutes(record.getActiveMinutes());
        score += activeScore * featureWeights.get("active_minutes");
        
        // Screen time feature
        double screenScore = normalizeScreenTime(record.getScreenTimeMinutes());
        score += screenScore * featureWeights.get("screen_time");
        
        // Places visited feature
        double placesScore = normalizePlacesVisited(record.getPlacesVisited());
        score += placesScore * featureWeights.get("places_visited");
        
        // Time of day feature (simplified)
        double timeScore = 0.5; // Would need actual time data
        score += timeScore * featureWeights.get("time_of_day");
        
        // Day of week feature
        String dayOfWeek = getDayOfWeek(record.getDate());
        double dayScore = dayPreferences.getOrDefault(dayOfWeek, 0.5);
        score += dayScore * featureWeights.get("day_of_week");
        
        return score + bias;
    }
    
    /**
     * Get context window for pattern analysis
     */
    private List<DayRecord> getContextWindow(List<DayRecord> data, int currentIndex) {
        List<DayRecord> window = new ArrayList<>();
        
        int startIndex = Math.max(0, currentIndex - PATTERN_WINDOW_DAYS);
        int endIndex = Math.min(data.size(), currentIndex + 1);
        
        for (int i = startIndex; i < endIndex; i++) {
            window.add(data.get(i));
        }
        
        return window;
    }
    
    /**
     * Create behavior pattern key for grouping similar patterns
     */
    private String createBehaviorPatternKey(DayRecord record, List<DayRecord> contextWindow) {
        // Activity level classification
        String activityLevel = classifyActivityLevel(record.getActivityScore());
        
        // Screen time classification
        String screenLevel = classifyScreenTime(record.getScreenTimeMinutes());
        
        // Social level classification
        String socialLevel = classifyPlacesVisited(record.getPlacesVisited());
        
        // Day type
        String dayType = getDayOfWeek(record.getDate()).contains("S") ? "weekend" : "weekday";
        
        // Recent trend
        String trend = calculateTrend(contextWindow);
        
        return String.format("%s_%s_%s_%s_%s", activityLevel, screenLevel, socialLevel, dayType, trend);
    }
    
    /**
     * Classify activity level
     */
    private String classifyActivityLevel(double activityScore) {
        if (activityScore >= 70) return "high";
        if (activityScore >= 40) return "medium";
        return "low";
    }
    
    /**
     * Classify screen time
     */
    private String classifyScreenTime(int screenTimeMinutes) {
        if (screenTimeMinutes >= 360) return "high"; // 6+ hours
        if (screenTimeMinutes >= 180) return "medium"; // 3-6 hours
        return "low";
    }
    
    /**
     * Classify places visited
     */
    private String classifyPlacesVisited(int placesVisited) {
        if (placesVisited >= 4) return "high";
        if (placesVisited >= 2) return "medium";
        return "low";
    }
    
    /**
     * Calculate recent trend
     */
    private String calculateTrend(List<DayRecord> contextWindow) {
        if (contextWindow.size() < 2) return "stable";
        
        double firstHalf = contextWindow.subList(0, contextWindow.size() / 2).stream()
            .mapToDouble(DayRecord::getActivityScore)
            .average()
            .orElse(0.0);
        
        double secondHalf = contextWindow.subList(contextWindow.size() / 2, contextWindow.size()).stream()
            .mapToDouble(DayRecord::getActivityScore)
            .average()
            .orElse(0.0);
        
        if (secondHalf > firstHalf * 1.1) return "increasing";
        if (secondHalf < firstHalf * 0.9) return "decreasing";
        return "stable";
    }
    
    /**
     * Get day of week from date string
     */
    private String getDayOfWeek(String dateString) {
        // Simplified - would need proper date parsing
        return "Monday"; // Placeholder
    }
    
    /**
     * Calculate activity level for a specific activity type
     */
    private double calculateActivityLevel(ActivityType activityType, DayRecord record) {
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                return Math.min(1.0, record.getStepCount() / 15000.0);
            case INDOOR_EXERCISE:
                return Math.min(1.0, record.getActiveMinutes() / 60.0);
            case SOCIAL_ACTIVITY:
                return Math.min(1.0, record.getPlacesVisited() / 5.0);
            case WORK_PRODUCTIVITY:
                return Math.min(1.0, record.getProductivityScore() / 100.0);
            case RECREATIONAL:
                return Math.min(1.0, record.getPhotoCount() / 10.0);
            case RELAXATION:
                return Math.min(1.0, Math.max(0.0, (480 - record.getScreenTimeMinutes()) / 480.0));
            case TRAVEL:
                return Math.min(1.0, record.getTotalTravelDistance() / 10000.0);
            case PHOTOGRAPHY:
                return Math.min(1.0, record.getPhotoCount() / 20.0);
            case INDOOR_ACTIVITIES:
                return Math.min(1.0, record.getScreenTimeMinutes() / 360.0);
            case OUTDOOR_LEISURE:
                return Math.min(1.0, (record.getStepCount() / 10000.0 + record.getPlacesVisited() / 3.0) / 2.0);
            default:
                return 0.0;
        }
    }
    
    /**
     * Calculate actual activity levels from a day record
     */
    private Map<ActivityType, Double> calculateActualActivities(DayRecord record) {
        Map<ActivityType, Double> actuals = new HashMap<>();
        
        for (ActivityType activityType : ActivityType.values()) {
            double level = calculateActivityLevel(activityType, record);
            actuals.put(activityType, level);
        }
        
        return actuals;
    }
    
    /**
     * Get feature value for gradient calculation
     */
    private double getFeatureValue(String feature, DayRecord record) {
        switch (feature) {
            case "step_count":
                return normalizeStepCount(record.getStepCount());
            case "active_minutes":
                return normalizeActiveMinutes(record.getActiveMinutes());
            case "screen_time":
                return normalizeScreenTime(record.getScreenTimeMinutes());
            case "places_visited":
                return normalizePlacesVisited(record.getPlacesVisited());
            case "time_of_day":
                return 0.5; // Simplified
            case "day_of_week":
                return dayPreferences.getOrDefault(getDayOfWeek(record.getDate()), 0.5);
            default:
                return 0.0;
        }
    }
    
    /**
     * Normalize step count to 0-1 range
     */
    private double normalizeStepCount(int stepCount) {
        return Math.max(0.0, Math.min(1.0, stepCount / 20000.0));
    }
    
    /**
     * Normalize active minutes to 0-1 range
     */
    private double normalizeActiveMinutes(int activeMinutes) {
        return Math.max(0.0, Math.min(1.0, activeMinutes / 120.0));
    }
    
    /**
     * Normalize screen time to 0-1 range
     */
    private double normalizeScreenTime(int screenTimeMinutes) {
        return Math.max(0.0, Math.min(1.0, screenTimeMinutes / 720.0)); // 12 hours max
    }
    
    /**
     * Normalize places visited to 0-1 range
     */
    private double normalizePlacesVisited(int placesVisited) {
        return Math.max(0.0, Math.min(1.0, placesVisited / 10.0));
    }
    
    /**
     * Get activity preference for a specific activity type
     */
    private double getActivityPreference(ActivityType activityType) {
        return activityPreferences.getOrDefault(activityType, 0.5);
    }
    
    /**
     * Normalize prediction scores to sum to 1.0
     */
    private Map<ActivityType, Double> normalizePredictions(Map<ActivityType, Double> predictions) {
        double sum = predictions.values().stream().mapToDouble(Double::doubleValue).sum();
        
        if (sum == 0.0) {
            return getDefaultPredictions();
        }
        
        Map<ActivityType, Double> normalized = new HashMap<>();
        for (Map.Entry<ActivityType, Double> entry : predictions.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / sum);
        }
        
        return normalized;
    }
    
    /**
     * Get default predictions when model is not trained
     */
    private Map<ActivityType, Double> getDefaultPredictions() {
        Map<ActivityType, Double> defaults = new HashMap<>();
        double defaultScore = 1.0 / ActivityType.values().length;
        
        for (ActivityType activityType : ActivityType.values()) {
            defaults.put(activityType, defaultScore);
        }
        
        return defaults;
    }
    
    /**
     * Calculate training accuracy
     */
    private double calculateTrainingAccuracy(List<DayRecord> trainingData) {
        int correctPredictions = 0;
        
        for (DayRecord record : trainingData) {
            Map<ActivityType, Double> predictions = predict(record);
            Map<ActivityType, Double> actuals = calculateActualActivities(record);
            
            ActivityType predictedActivity = predictions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActivityType.INDOOR_ACTIVITIES);
            
            ActivityType actualActivity = actuals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActivityType.INDOOR_ACTIVITIES);
            
            if (predictedActivity == actualActivity) {
                correctPredictions++;
            }
        }
        
        return (double) correctPredictions / trainingData.size();
    }
    
    /**
     * Reinforce positive feedback for incremental learning
     */
    public void reinforcePositiveFeedback(PredictionResult result) {
        if (!isTrained) return;
        
        ActivityType predictedActivity = result.getPredictedActivity();
        ActivityType actualActivity = result.getActualActivity();
        
        if (predictedActivity == actualActivity) {
            // Reinforce activity preference
            double currentPreference = activityPreferences.getOrDefault(predictedActivity, 0.5);
            activityPreferences.put(predictedActivity, Math.min(1.0, currentPreference + (LEARNING_RATE * 0.1)));
        }
    }
    
    /**
     * Adjust for negative feedback
     */
    public void adjustForNegativeFeedback(PredictionResult result) {
        if (!isTrained) return;
        
        ActivityType predictedActivity = result.getPredictedActivity();
        ActivityType actualActivity = result.getActualActivity();
        
        if (predictedActivity != actualActivity) {
            // Reduce preference for incorrect prediction
            double currentPreference = activityPreferences.getOrDefault(predictedActivity, 0.5);
            activityPreferences.put(predictedActivity, Math.max(0.0, currentPreference - (LEARNING_RATE * 0.05)));
            
            // Increase preference for actual activity
            double actualPreference = activityPreferences.getOrDefault(actualActivity, 0.5);
            activityPreferences.put(actualActivity, Math.min(1.0, actualPreference + (LEARNING_RATE * 0.05)));
        }
    }
    
    /**
     * Get model statistics
     */
    public Map<String, Object> getModelStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("is_trained", isTrained);
        stats.put("training_data_size", trainingDataSize);
        stats.put("training_accuracy", trainingAccuracy);
        stats.put("last_training_time", lastTrainingTime);
        stats.put("behavior_patterns_count", behaviorPatterns.size());
        stats.put("feature_weights", new HashMap<>(featureWeights));
        stats.put("activity_preferences", new HashMap<>(activityPreferences));
        stats.put("average_step_count", averageStepCount);
        stats.put("average_active_minutes", averageActiveMinutes);
        stats.put("average_screen_time", averageScreenTime);
        stats.put("average_places_visited", averagePlacesVisited);
        
        return stats;
    }
    
    // Getters
    public boolean isTrained() {
        return isTrained;
    }
    
    public double getTrainingAccuracy() {
        return trainingAccuracy;
    }
    
    public int getTrainingDataSize() {
        return trainingDataSize;
    }
    
    public Date getLastTrainingTime() {
        return lastTrainingTime;
    }
    
    public Map<String, Double> getFeatureWeights() {
        return new HashMap<>(featureWeights);
    }
    
    public Map<ActivityType, Double> getActivityPreferences() {
        return new HashMap<>(activityPreferences);
    }
    
    /**
     * Inner class for user behavior pattern representation
     */
    private static class UserBehaviorPattern {
        private int sampleCount;
        private double avgStepCount;
        private double avgActiveMinutes;
        private double avgScreenTime;
        private double avgPlacesVisited;
        private double avgActivityScore;
        private Map<ActivityType, Double> activityLevels;
        
        public UserBehaviorPattern() {
            this.sampleCount = 0;
            this.avgStepCount = 0.0;
            this.avgActiveMinutes = 0.0;
            this.avgScreenTime = 0.0;
            this.avgPlacesVisited = 0.0;
            this.avgActivityScore = 0.0;
            this.activityLevels = new HashMap<>();
        }
        
        public void addDataPoint(DayRecord record) {
            sampleCount++;
            
            // Update averages
            avgStepCount = ((avgStepCount * (sampleCount - 1)) + record.getStepCount()) / sampleCount;
            avgActiveMinutes = ((avgActiveMinutes * (sampleCount - 1)) + record.getActiveMinutes()) / sampleCount;
            avgScreenTime = ((avgScreenTime * (sampleCount - 1)) + record.getScreenTimeMinutes()) / sampleCount;
            avgPlacesVisited = ((avgPlacesVisited * (sampleCount - 1)) + record.getPlacesVisited()) / sampleCount;
            avgActivityScore = ((avgActivityScore * (sampleCount - 1)) + record.getActivityScore()) / sampleCount;
            
            // Update activity levels
            for (ActivityType activityType : ActivityType.values()) {
                double activityLevel = calculateActivityLevel(activityType, record);
                double currentLevel = activityLevels.getOrDefault(activityType, 0.0);
                activityLevels.put(activityType, ((currentLevel * (sampleCount - 1)) + activityLevel) / sampleCount);
            }
        }
        
        public double getActivityLevel(ActivityType activityType) {
            return activityLevels.getOrDefault(activityType, 0.0);
        }
        
        public int getSampleCount() {
            return sampleCount;
        }
        
        private double calculateActivityLevel(ActivityType activityType, DayRecord record) {
            switch (activityType) {
                case OUTDOOR_EXERCISE:
                    return Math.min(1.0, record.getStepCount() / 15000.0);
                case INDOOR_EXERCISE:
                    return Math.min(1.0, record.getActiveMinutes() / 60.0);
                case SOCIAL_ACTIVITY:
                    return Math.min(1.0, record.getPlacesVisited() / 5.0);
                case WORK_PRODUCTIVITY:
                    return Math.min(1.0, record.getProductivityScore() / 100.0);
                case RECREATIONAL:
                    return Math.min(1.0, record.getPhotoCount() / 10.0);
                case RELAXATION:
                    return Math.min(1.0, Math.max(0.0, (480 - record.getScreenTimeMinutes()) / 480.0));
                case TRAVEL:
                    return Math.min(1.0, record.getTotalTravelDistance() / 10000.0);
                case PHOTOGRAPHY:
                    return Math.min(1.0, record.getPhotoCount() / 20.0);
                case INDOOR_ACTIVITIES:
                    return Math.min(1.0, record.getScreenTimeMinutes() / 360.0);
                case OUTDOOR_LEISURE:
                    return Math.min(1.0, (record.getStepCount() / 10000.0 + record.getPlacesVisited() / 3.0) / 2.0);
                default:
                    return 0.0;
            }
        }
    }
}