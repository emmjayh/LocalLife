package com.locallife.service;

import android.util.Log;

import com.locallife.model.ActivityType;
import com.locallife.model.DayRecord;
import com.locallife.model.PredictionResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Machine Learning model for predicting activity patterns based on combined weather and user behavior
 */
public class ActivityPatternModel {
    private static final String TAG = "ActivityPatternModel";
    private static final double LEARNING_RATE = 0.015;
    private static final int MIN_TRAINING_SAMPLES = 25;
    private static final int SEQUENCE_LENGTH = 5;
    
    private boolean isTrained = false;
    private Map<String, ActivitySequence> activitySequences;
    private Map<ActivityType, Map<String, Double>> transitionWeights;
    private Map<String, Double> contextWeights;
    private double bias;
    
    // Pattern recognition
    private Map<String, Double> timePatterns;
    private Map<String, Double> weatherPatterns;
    private Map<String, Double> behaviorPatterns;
    private Map<String, Double> seasonalPatterns;
    
    // Training statistics
    private int trainingDataSize;
    private double trainingAccuracy;
    private Date lastTrainingTime;
    
    public ActivityPatternModel() {
        this.activitySequences = new HashMap<>();
        this.transitionWeights = new HashMap<>();
        this.contextWeights = new HashMap<>();
        this.timePatterns = new HashMap<>();
        this.weatherPatterns = new HashMap<>();
        this.behaviorPatterns = new HashMap<>();
        this.seasonalPatterns = new HashMap<>();
        this.bias = 0.0;
        
        // Initialize context weights
        initializeContextWeights();
    }
    
    /**
     * Initialize context weights for pattern recognition
     */
    private void initializeContextWeights() {
        contextWeights.put("weather_context", 0.35);
        contextWeights.put("user_behavior", 0.25);
        contextWeights.put("time_context", 0.2);
        contextWeights.put("sequence_context", 0.15);
        contextWeights.put("seasonal_context", 0.05);
    }
    
    /**
     * Train the model with historical data
     */
    public void train(List<DayRecord> historicalData) {
        if (historicalData.size() < MIN_TRAINING_SAMPLES) {
            Log.w(TAG, "Insufficient training data: " + historicalData.size() + " samples");
            return;
        }
        
        Log.d(TAG, "Training activity pattern model with " + historicalData.size() + " samples");
        
        // Sort data by date
        List<DayRecord> sortedData = historicalData.stream()
            .sorted(Comparator.comparing(DayRecord::getDate))
            .collect(Collectors.toList());
        
        // Extract activity sequences
        extractActivitySequences(sortedData);
        
        // Learn time patterns
        learnTimePatterns(sortedData);
        
        // Learn weather patterns
        learnWeatherPatterns(sortedData);
        
        // Learn behavior patterns
        learnBehaviorPatterns(sortedData);
        
        // Learn seasonal patterns
        learnSeasonalPatterns(sortedData);
        
        // Train transition weights
        trainTransitionWeights(sortedData);
        
        // Optimize context weights
        optimizeContextWeights(sortedData);
        
        this.isTrained = true;
        this.trainingDataSize = sortedData.size();
        this.lastTrainingTime = new Date();
        
        // Calculate training accuracy
        this.trainingAccuracy = calculateTrainingAccuracy(sortedData);
        
        Log.d(TAG, "Model training completed with accuracy: " + String.format("%.2f%%", trainingAccuracy * 100));
    }
    
    /**
     * Extract activity sequences from historical data
     */
    private void extractActivitySequences(List<DayRecord> data) {
        for (int i = 0; i < data.size() - SEQUENCE_LENGTH; i++) {
            List<DayRecord> sequence = data.subList(i, i + SEQUENCE_LENGTH);
            List<ActivityType> activitySequence = sequence.stream()
                .map(this::getPrimaryActivity)
                .collect(Collectors.toList());
            
            String sequenceKey = createSequenceKey(activitySequence);
            
            // Get next activity
            ActivityType nextActivity = getPrimaryActivity(data.get(i + SEQUENCE_LENGTH));
            
            ActivitySequence activitySeq = activitySequences.computeIfAbsent(sequenceKey, k -> new ActivitySequence());
            activitySeq.addNextActivity(nextActivity);
        }
        
        Log.d(TAG, "Extracted " + activitySequences.size() + " activity sequences");
    }
    
    /**
     * Learn time-based patterns
     */
    private void learnTimePatterns(List<DayRecord> data) {
        for (DayRecord record : data) {
            String dayType = getDayType(record.getDate());
            String timeSlot = "midday"; // Simplified - would need actual time data
            
            ActivityType activity = getPrimaryActivity(record);
            String patternKey = dayType + "_" + timeSlot + "_" + activity.name();
            
            timePatterns.merge(patternKey, 1.0, Double::sum);
        }
        
        // Normalize patterns
        double totalPatterns = timePatterns.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalPatterns > 0) {
            timePatterns.replaceAll((k, v) -> v / totalPatterns);
        }
    }
    
    /**
     * Learn weather-based patterns
     */
    private void learnWeatherPatterns(List<DayRecord> data) {
        for (DayRecord record : data) {
            String weatherKey = createWeatherKey(record);
            ActivityType activity = getPrimaryActivity(record);
            String patternKey = weatherKey + "_" + activity.name();
            
            weatherPatterns.merge(patternKey, 1.0, Double::sum);
        }
        
        // Normalize patterns
        double totalPatterns = weatherPatterns.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalPatterns > 0) {
            weatherPatterns.replaceAll((k, v) -> v / totalPatterns);
        }
    }
    
    /**
     * Learn behavior-based patterns
     */
    private void learnBehaviorPatterns(List<DayRecord> data) {
        for (DayRecord record : data) {
            String behaviorKey = createBehaviorKey(record);
            ActivityType activity = getPrimaryActivity(record);
            String patternKey = behaviorKey + "_" + activity.name();
            
            behaviorPatterns.merge(patternKey, 1.0, Double::sum);
        }
        
        // Normalize patterns
        double totalPatterns = behaviorPatterns.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalPatterns > 0) {
            behaviorPatterns.replaceAll((k, v) -> v / totalPatterns);
        }
    }
    
    /**
     * Learn seasonal patterns
     */
    private void learnSeasonalPatterns(List<DayRecord> data) {
        for (DayRecord record : data) {
            String season = record.getSeason() != null ? record.getSeason() : "unknown";
            ActivityType activity = getPrimaryActivity(record);
            String patternKey = season + "_" + activity.name();
            
            seasonalPatterns.merge(patternKey, 1.0, Double::sum);
        }
        
        // Normalize patterns
        double totalPatterns = seasonalPatterns.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalPatterns > 0) {
            seasonalPatterns.replaceAll((k, v) -> v / totalPatterns);
        }
    }
    
    /**
     * Train transition weights between activities
     */
    private void trainTransitionWeights(List<DayRecord> data) {
        for (ActivityType activityType : ActivityType.values()) {
            Map<String, Double> weights = new HashMap<>();
            
            for (int i = 0; i < data.size() - 1; i++) {
                DayRecord currentRecord = data.get(i);
                DayRecord nextRecord = data.get(i + 1);
                
                ActivityType currentActivity = getPrimaryActivity(currentRecord);
                ActivityType nextActivity = getPrimaryActivity(nextRecord);
                
                if (currentActivity == activityType) {
                    String contextKey = createContextKey(currentRecord, nextRecord);
                    double transitionProb = calculateTransitionProbability(currentActivity, nextActivity);
                    
                    weights.merge(contextKey, transitionProb, (existing, newValue) -> {
                        return existing + (LEARNING_RATE * (newValue - existing));
                    });
                }
            }
            
            transitionWeights.put(activityType, weights);
        }
    }
    
    /**
     * Optimize context weights using gradient descent
     */
    private void optimizeContextWeights(List<DayRecord> data) {
        for (int iteration = 0; iteration < 30; iteration++) {
            Map<String, Double> gradients = new HashMap<>();
            
            for (String context : contextWeights.keySet()) {
                gradients.put(context, 0.0);
            }
            
            // Calculate gradients
            for (int i = 0; i < data.size() - 1; i++) {
                DayRecord record = data.get(i);
                DayRecord nextRecord = data.get(i + 1);
                
                Map<ActivityType, Double> predictions = predict(record, nextRecord);
                ActivityType actual = getPrimaryActivity(nextRecord);
                
                double predicted = predictions.getOrDefault(actual, 0.0);
                double error = 1.0 - predicted;
                
                // Update gradients
                for (String context : contextWeights.keySet()) {
                    double contextValue = getContextValue(context, record, nextRecord);
                    gradients.merge(context, error * contextValue, Double::sum);
                }
            }
            
            // Update weights
            for (String context : contextWeights.keySet()) {
                double gradient = gradients.get(context) / (data.size() - 1);
                contextWeights.put(context, Math.max(0.0, contextWeights.get(context) + LEARNING_RATE * gradient));
            }
        }
        
        // Normalize context weights
        double totalWeight = contextWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight > 0) {
            contextWeights.replaceAll((k, v) -> v / totalWeight);
        }
    }
    
    /**
     * Predict activity probabilities based on weather and user context
     */
    public Map<ActivityType, Double> predict(PredictionResult.WeatherContext weatherContext, PredictionResult.UserContext userContext) {
        if (!isTrained) {
            Log.w(TAG, "Model not trained, returning default predictions");
            return getDefaultPredictions();
        }
        
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        for (ActivityType activityType : ActivityType.values()) {
            double score = calculateActivityScore(activityType, weatherContext, userContext);
            predictions.put(activityType, score);
        }
        
        return normalizePredictions(predictions);
    }
    
    /**
     * Predict activity probabilities based on current and next day records
     */
    public Map<ActivityType, Double> predict(DayRecord currentRecord, DayRecord nextRecord) {
        if (!isTrained) {
            Log.w(TAG, "Model not trained, returning default predictions");
            return getDefaultPredictions();
        }
        
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        for (ActivityType activityType : ActivityType.values()) {
            double score = calculateActivityScore(activityType, currentRecord, nextRecord);
            predictions.put(activityType, score);
        }
        
        return normalizePredictions(predictions);
    }
    
    /**
     * Calculate activity score based on weather and user context
     */
    private double calculateActivityScore(ActivityType activityType, PredictionResult.WeatherContext weatherContext, PredictionResult.UserContext userContext) {
        double score = 0.0;
        
        // Weather context score
        String weatherKey = createWeatherKey(weatherContext);
        String weatherPattern = weatherKey + "_" + activityType.name();
        double weatherScore = weatherPatterns.getOrDefault(weatherPattern, 0.0);
        score += weatherScore * contextWeights.get("weather_context");
        
        // User behavior score
        String behaviorKey = createBehaviorKey(userContext);
        String behaviorPattern = behaviorKey + "_" + activityType.name();
        double behaviorScore = behaviorPatterns.getOrDefault(behaviorPattern, 0.0);
        score += behaviorScore * contextWeights.get("user_behavior");
        
        // Time context score
        String timeKey = "midday"; // Simplified - would need actual time
        String timePattern = "weekday_" + timeKey + "_" + activityType.name();
        double timeScore = timePatterns.getOrDefault(timePattern, 0.0);
        score += timeScore * contextWeights.get("time_context");
        
        // Sequence context score (simplified)
        double sequenceScore = 0.5; // Would need recent activity sequence
        score += sequenceScore * contextWeights.get("sequence_context");
        
        // Seasonal context score
        String seasonPattern = "spring_" + activityType.name(); // Simplified
        double seasonScore = seasonalPatterns.getOrDefault(seasonPattern, 0.0);
        score += seasonScore * contextWeights.get("seasonal_context");
        
        return Math.max(0.0, score + bias);
    }
    
    /**
     * Calculate activity score based on day records
     */
    private double calculateActivityScore(ActivityType activityType, DayRecord currentRecord, DayRecord nextRecord) {
        double score = 0.0;
        
        // Weather context score
        String weatherKey = createWeatherKey(currentRecord);
        String weatherPattern = weatherKey + "_" + activityType.name();
        double weatherScore = weatherPatterns.getOrDefault(weatherPattern, 0.0);
        score += weatherScore * contextWeights.get("weather_context");
        
        // User behavior score
        String behaviorKey = createBehaviorKey(currentRecord);
        String behaviorPattern = behaviorKey + "_" + activityType.name();
        double behaviorScore = behaviorPatterns.getOrDefault(behaviorPattern, 0.0);
        score += behaviorScore * contextWeights.get("user_behavior");
        
        // Time context score
        String dayType = getDayType(nextRecord.getDate());
        String timePattern = dayType + "_midday_" + activityType.name();
        double timeScore = timePatterns.getOrDefault(timePattern, 0.0);
        score += timeScore * contextWeights.get("time_context");
        
        // Transition score
        ActivityType currentActivity = getPrimaryActivity(currentRecord);
        String contextKey = createContextKey(currentRecord, nextRecord);
        Map<String, Double> weights = transitionWeights.get(currentActivity);
        double transitionScore = weights != null ? weights.getOrDefault(contextKey, 0.0) : 0.0;
        score += transitionScore * contextWeights.get("sequence_context");
        
        // Seasonal context score
        String season = currentRecord.getSeason() != null ? currentRecord.getSeason() : "unknown";
        String seasonPattern = season + "_" + activityType.name();
        double seasonScore = seasonalPatterns.getOrDefault(seasonPattern, 0.0);
        score += seasonScore * contextWeights.get("seasonal_context");
        
        return Math.max(0.0, score + bias);
    }
    
    /**
     * Get primary activity from day record
     */
    private ActivityType getPrimaryActivity(DayRecord record) {
        // Simple heuristic to determine primary activity
        if (record.getStepCount() > 12000) return ActivityType.OUTDOOR_EXERCISE;
        if (record.getPlacesVisited() > 3) return ActivityType.SOCIAL_ACTIVITY;
        if (record.getPhotoCount() > 10) return ActivityType.PHOTOGRAPHY;
        if (record.getScreenTimeMinutes() > 360) return ActivityType.INDOOR_ACTIVITIES;
        if (record.getActiveMinutes() > 60) return ActivityType.INDOOR_EXERCISE;
        if (record.getProductivityScore() > 70) return ActivityType.WORK_PRODUCTIVITY;
        return ActivityType.RELAXATION;
    }
    
    /**
     * Create sequence key from activity list
     */
    private String createSequenceKey(List<ActivityType> activities) {
        return activities.stream()
            .map(Enum::name)
            .collect(Collectors.joining("_"));
    }
    
    /**
     * Create weather key from day record
     */
    private String createWeatherKey(DayRecord record) {
        int tempRange = (int) (record.getTemperature() / 10) * 10;
        String condition = normalizeWeatherCondition(record.getWeatherCondition());
        return tempRange + "_" + condition;
    }
    
    /**
     * Create weather key from weather context
     */
    private String createWeatherKey(PredictionResult.WeatherContext context) {
        int tempRange = (int) (context.getTemperature() / 10) * 10;
        String condition = normalizeWeatherCondition(context.getWeatherCondition());
        return tempRange + "_" + condition;
    }
    
    /**
     * Create behavior key from day record
     */
    private String createBehaviorKey(DayRecord record) {
        String activityLevel = record.getActivityScore() > 60 ? "high" : 
                              record.getActivityScore() > 30 ? "medium" : "low";
        String socialLevel = record.getPlacesVisited() > 3 ? "high" : 
                           record.getPlacesVisited() > 1 ? "medium" : "low";
        return activityLevel + "_" + socialLevel;
    }
    
    /**
     * Create behavior key from user context
     */
    private String createBehaviorKey(PredictionResult.UserContext context) {
        String activityLevel = context.getRecentActivityScore() > 60 ? "high" : 
                              context.getRecentActivityScore() > 30 ? "medium" : "low";
        String socialLevel = context.getSocialInteractions() > 3 ? "high" : 
                           context.getSocialInteractions() > 1 ? "medium" : "low";
        return activityLevel + "_" + socialLevel;
    }
    
    /**
     * Create context key for transition analysis
     */
    private String createContextKey(DayRecord currentRecord, DayRecord nextRecord) {
        String weatherChange = getWeatherChange(currentRecord, nextRecord);
        String dayType = getDayType(nextRecord.getDate());
        return weatherChange + "_" + dayType;
    }
    
    /**
     * Get weather change between records
     */
    private String getWeatherChange(DayRecord current, DayRecord next) {
        double tempDiff = next.getTemperature() - current.getTemperature();
        if (tempDiff > 5) return "warming";
        if (tempDiff < -5) return "cooling";
        return "stable";
    }
    
    /**
     * Get day type (weekday/weekend)
     */
    private String getDayType(String dateString) {
        // Simplified - would need proper date parsing
        return "weekday"; // Placeholder
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
        return "unknown";
    }
    
    /**
     * Calculate transition probability between activities
     */
    private double calculateTransitionProbability(ActivityType from, ActivityType to) {
        // Simple transition probability based on activity compatibility
        if (from == to) return 0.3; // Some continuity
        
        // Define compatible transitions
        Map<ActivityType, List<ActivityType>> compatibleTransitions = new HashMap<>();
        compatibleTransitions.put(ActivityType.OUTDOOR_EXERCISE, Arrays.asList(ActivityType.RELAXATION, ActivityType.SOCIAL_ACTIVITY));
        compatibleTransitions.put(ActivityType.WORK_PRODUCTIVITY, Arrays.asList(ActivityType.RELAXATION, ActivityType.RECREATIONAL));
        compatibleTransitions.put(ActivityType.SOCIAL_ACTIVITY, Arrays.asList(ActivityType.RECREATIONAL, ActivityType.PHOTOGRAPHY));
        
        List<ActivityType> compatible = compatibleTransitions.get(from);
        if (compatible != null && compatible.contains(to)) {
            return 0.7;
        }
        
        return 0.1; // Low probability for incompatible transitions
    }
    
    /**
     * Get context value for gradient calculation
     */
    private double getContextValue(String context, DayRecord current, DayRecord next) {
        switch (context) {
            case "weather_context":
                return calculateWeatherContextValue(current);
            case "user_behavior":
                return calculateBehaviorContextValue(current);
            case "time_context":
                return calculateTimeContextValue(next);
            case "sequence_context":
                return calculateSequenceContextValue(current, next);
            case "seasonal_context":
                return calculateSeasonalContextValue(current);
            default:
                return 0.0;
        }
    }
    
    /**
     * Calculate weather context value
     */
    private double calculateWeatherContextValue(DayRecord record) {
        // Normalize weather impact
        double tempScore = Math.max(0.0, Math.min(1.0, (record.getTemperature() + 10) / 50.0));
        double humidityScore = Math.max(0.0, Math.min(1.0, (100 - record.getHumidity()) / 100.0));
        return (tempScore + humidityScore) / 2.0;
    }
    
    /**
     * Calculate behavior context value
     */
    private double calculateBehaviorContextValue(DayRecord record) {
        return Math.max(0.0, Math.min(1.0, record.getActivityScore() / 100.0));
    }
    
    /**
     * Calculate time context value
     */
    private double calculateTimeContextValue(DayRecord record) {
        // Simplified time context
        return 0.5; // Would need actual time data
    }
    
    /**
     * Calculate sequence context value
     */
    private double calculateSequenceContextValue(DayRecord current, DayRecord next) {
        ActivityType currentActivity = getPrimaryActivity(current);
        ActivityType nextActivity = getPrimaryActivity(next);
        return calculateTransitionProbability(currentActivity, nextActivity);
    }
    
    /**
     * Calculate seasonal context value
     */
    private double calculateSeasonalContextValue(DayRecord record) {
        String season = record.getSeason();
        if (season == null) return 0.5;
        
        // Seasonal activity preferences
        switch (season.toLowerCase()) {
            case "spring":
            case "summer":
                return 0.8; // Higher outdoor activity
            case "fall":
            case "winter":
                return 0.3; // Lower outdoor activity
            default:
                return 0.5;
        }
    }
    
    /**
     * Normalize prediction scores
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
        int totalPredictions = 0;
        
        for (int i = 0; i < trainingData.size() - 1; i++) {
            DayRecord current = trainingData.get(i);
            DayRecord next = trainingData.get(i + 1);
            
            Map<ActivityType, Double> predictions = predict(current, next);
            ActivityType predictedActivity = predictions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActivityType.INDOOR_ACTIVITIES);
            
            ActivityType actualActivity = getPrimaryActivity(next);
            
            if (predictedActivity == actualActivity) {
                correctPredictions++;
            }
            totalPredictions++;
        }
        
        return totalPredictions > 0 ? (double) correctPredictions / totalPredictions : 0.0;
    }
    
    /**
     * Reinforce positive feedback for incremental learning
     */
    public void reinforcePositiveFeedback(PredictionResult result) {
        if (!isTrained) return;
        
        // Reinforce successful patterns
        ActivityType predictedActivity = result.getPredictedActivity();
        ActivityType actualActivity = result.getActualActivity();
        
        if (predictedActivity == actualActivity && result.getWeatherContext() != null) {
            // Reinforce weather pattern
            String weatherKey = createWeatherKey(result.getWeatherContext());
            String weatherPattern = weatherKey + "_" + predictedActivity.name();
            weatherPatterns.merge(weatherPattern, LEARNING_RATE * 0.1, Double::sum);
        }
    }
    
    /**
     * Adjust for negative feedback
     */
    public void adjustForNegativeFeedback(PredictionResult result) {
        if (!isTrained) return;
        
        // Adjust patterns based on incorrect predictions
        ActivityType predictedActivity = result.getPredictedActivity();
        ActivityType actualActivity = result.getActualActivity();
        
        if (predictedActivity != actualActivity && result.getWeatherContext() != null) {
            // Reduce weight for incorrect pattern
            String weatherKey = createWeatherKey(result.getWeatherContext());
            String incorrectPattern = weatherKey + "_" + predictedActivity.name();
            weatherPatterns.merge(incorrectPattern, -LEARNING_RATE * 0.05, Double::sum);
            
            // Increase weight for correct pattern
            String correctPattern = weatherKey + "_" + actualActivity.name();
            weatherPatterns.merge(correctPattern, LEARNING_RATE * 0.05, Double::sum);
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
        stats.put("activity_sequences_count", activitySequences.size());
        stats.put("context_weights", new HashMap<>(contextWeights));
        stats.put("time_patterns_count", timePatterns.size());
        stats.put("weather_patterns_count", weatherPatterns.size());
        stats.put("behavior_patterns_count", behaviorPatterns.size());
        stats.put("seasonal_patterns_count", seasonalPatterns.size());
        
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
    
    public Map<String, Double> getContextWeights() {
        return new HashMap<>(contextWeights);
    }
    
    /**
     * Inner class for activity sequence representation
     */
    private static class ActivitySequence {
        private Map<ActivityType, Integer> nextActivities;
        private int totalCount;
        
        public ActivitySequence() {
            this.nextActivities = new HashMap<>();
            this.totalCount = 0;
        }
        
        public void addNextActivity(ActivityType activity) {
            nextActivities.merge(activity, 1, Integer::sum);
            totalCount++;
        }
        
        public double getProbability(ActivityType activity) {
            return totalCount > 0 ? (double) nextActivities.getOrDefault(activity, 0) / totalCount : 0.0;
        }
        
        public ActivityType getMostLikelyNext() {
            return nextActivities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ActivityType.INDOOR_ACTIVITIES);
        }
        
        public int getTotalCount() {
            return totalCount;
        }
    }
}