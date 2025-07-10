package com.locallife.service;

import android.util.Log;

import com.locallife.model.ActivityType;
import com.locallife.model.DayRecord;
import com.locallife.model.PredictionResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Machine Learning model for predicting activity based on weather patterns
 */
public class WeatherPatternModel {
    private static final String TAG = "WeatherPatternModel";
    private static final double LEARNING_RATE = 0.01;
    private static final int MIN_TRAINING_SAMPLES = 20;
    
    private boolean isTrained = false;
    private Map<String, WeatherPattern> weatherPatterns;
    private Map<ActivityType, Map<String, Double>> activityWeatherWeights;
    private Map<String, Double> featureWeights;
    private double bias;
    
    // Training statistics
    private int trainingDataSize;
    private double trainingAccuracy;
    private Date lastTrainingTime;
    
    public WeatherPatternModel() {
        this.weatherPatterns = new HashMap<>();
        this.activityWeatherWeights = new HashMap<>();
        this.featureWeights = new HashMap<>();
        this.bias = 0.0;
        
        // Initialize feature weights
        initializeFeatureWeights();
    }
    
    /**
     * Initialize feature weights for weather-based prediction
     */
    private void initializeFeatureWeights() {
        featureWeights.put("temperature", 0.3);
        featureWeights.put("humidity", 0.2);
        featureWeights.put("weather_condition", 0.25);
        featureWeights.put("wind_speed", 0.15);
        featureWeights.put("uv_index", 0.1);
    }
    
    /**
     * Train the model with historical data
     */
    public void train(List<DayRecord> historicalData) {
        if (historicalData.size() < MIN_TRAINING_SAMPLES) {
            Log.w(TAG, "Insufficient training data: " + historicalData.size() + " samples");
            return;
        }
        
        Log.d(TAG, "Training weather pattern model with " + historicalData.size() + " samples");
        
        // Extract weather patterns
        extractWeatherPatterns(historicalData);
        
        // Train activity-weather associations
        trainActivityWeatherAssociations(historicalData);
        
        // Optimize feature weights
        optimizeFeatureWeights(historicalData);
        
        this.isTrained = true;
        this.trainingDataSize = historicalData.size();
        this.lastTrainingTime = new Date();
        
        // Calculate training accuracy
        this.trainingAccuracy = calculateTrainingAccuracy(historicalData);
        
        Log.d(TAG, "Model training completed with accuracy: " + String.format("%.2f%%", trainingAccuracy * 100));
    }
    
    /**
     * Extract weather patterns from historical data
     */
    private void extractWeatherPatterns(List<DayRecord> historicalData) {
        for (DayRecord record : historicalData) {
            String patternKey = createWeatherPatternKey(record);
            
            WeatherPattern pattern = weatherPatterns.computeIfAbsent(patternKey, k -> new WeatherPattern());
            pattern.addDataPoint(record);
        }
        
        Log.d(TAG, "Extracted " + weatherPatterns.size() + " weather patterns");
    }
    
    /**
     * Train activity-weather associations
     */
    private void trainActivityWeatherAssociations(List<DayRecord> historicalData) {
        for (ActivityType activityType : ActivityType.values()) {
            Map<String, Double> weatherWeights = new HashMap<>();
            
            for (DayRecord record : historicalData) {
                String patternKey = createWeatherPatternKey(record);
                double activityLevel = calculateActivityLevel(activityType, record);
                
                // Update weights based on activity level
                weatherWeights.merge(patternKey, activityLevel, (existing, newValue) -> {
                    return existing + (LEARNING_RATE * (newValue - existing));
                });
            }
            
            activityWeatherWeights.put(activityType, weatherWeights);
        }
    }
    
    /**
     * Optimize feature weights using simple gradient descent
     */
    private void optimizeFeatureWeights(List<DayRecord> historicalData) {
        for (int iteration = 0; iteration < 100; iteration++) {
            Map<String, Double> gradients = new HashMap<>();
            
            for (String feature : featureWeights.keySet()) {
                gradients.put(feature, 0.0);
            }
            
            // Calculate gradients
            for (DayRecord record : historicalData) {
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
                double gradient = gradients.get(feature) / historicalData.size();
                featureWeights.put(feature, featureWeights.get(feature) + LEARNING_RATE * gradient);
            }
        }
    }
    
    /**
     * Predict activity probabilities based on weather context
     */
    public Map<ActivityType, Double> predict(PredictionResult.WeatherContext weatherContext) {
        if (!isTrained) {
            Log.w(TAG, "Model not trained, returning default predictions");
            return getDefaultPredictions();
        }
        
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        for (ActivityType activityType : ActivityType.values()) {
            double score = calculateActivityScore(activityType, weatherContext);
            predictions.put(activityType, score);
        }
        
        // Normalize predictions
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
        String patternKey = createWeatherPatternKey(record);
        
        for (ActivityType activityType : ActivityType.values()) {
            Map<String, Double> weatherWeights = activityWeatherWeights.get(activityType);
            double score = weatherWeights.getOrDefault(patternKey, 0.0);
            
            // Apply feature weights
            score *= calculateFeatureScore(record);
            
            predictions.put(activityType, score);
        }
        
        return normalizePredictions(predictions);
    }
    
    /**
     * Calculate activity score for a specific activity type and weather context
     */
    private double calculateActivityScore(ActivityType activityType, PredictionResult.WeatherContext weatherContext) {
        double score = 0.0;
        
        // Weather suitability score
        double weatherSuitability = activityType.getWeatherSuitability(
            weatherContext.getTemperature(),
            weatherContext.getWeatherCondition(),
            weatherContext.getHumidity(),
            weatherContext.getWindSpeed(),
            weatherContext.getUvIndex()
        );
        
        score += weatherSuitability * 0.6;
        
        // Pattern-based score
        String patternKey = createWeatherPatternKey(weatherContext);
        WeatherPattern pattern = weatherPatterns.get(patternKey);
        if (pattern != null) {
            score += pattern.getActivityScore(activityType) * 0.4;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Calculate feature score for a day record
     */
    private double calculateFeatureScore(DayRecord record) {
        double score = 0.0;
        
        // Temperature score
        double tempScore = normalizeTemperature(record.getTemperature());
        score += tempScore * featureWeights.get("temperature");
        
        // Humidity score
        double humidityScore = normalizeHumidity(record.getHumidity());
        score += humidityScore * featureWeights.get("humidity");
        
        // Weather condition score
        double conditionScore = getWeatherConditionScore(record.getWeatherCondition());
        score += conditionScore * featureWeights.get("weather_condition");
        
        // Wind speed score
        double windScore = normalizeWindSpeed(record.getWindSpeed());
        score += windScore * featureWeights.get("wind_speed");
        
        // UV index score
        double uvScore = normalizeUVIndex(record.getUvIndex());
        score += uvScore * featureWeights.get("uv_index");
        
        return score + bias;
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
     * Create weather pattern key for grouping similar weather conditions
     */
    private String createWeatherPatternKey(DayRecord record) {
        int tempRange = (int) (record.getTemperature() / 5) * 5; // 5-degree ranges
        int humidityRange = (int) (record.getHumidity() / 20) * 20; // 20% ranges
        String condition = normalizeWeatherCondition(record.getWeatherCondition());
        
        return String.format("%d_%d_%s", tempRange, humidityRange, condition);
    }
    
    /**
     * Create weather pattern key for weather context
     */
    private String createWeatherPatternKey(PredictionResult.WeatherContext context) {
        int tempRange = (int) (context.getTemperature() / 5) * 5;
        int humidityRange = (int) (context.getHumidity() / 20) * 20;
        String condition = normalizeWeatherCondition(context.getWeatherCondition());
        
        return String.format("%d_%d_%s", tempRange, humidityRange, condition);
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
     * Get feature value for gradient calculation
     */
    private double getFeatureValue(String feature, DayRecord record) {
        switch (feature) {
            case "temperature":
                return normalizeTemperature(record.getTemperature());
            case "humidity":
                return normalizeHumidity(record.getHumidity());
            case "weather_condition":
                return getWeatherConditionScore(record.getWeatherCondition());
            case "wind_speed":
                return normalizeWindSpeed(record.getWindSpeed());
            case "uv_index":
                return normalizeUVIndex(record.getUvIndex());
            default:
                return 0.0;
        }
    }
    
    /**
     * Normalize temperature to 0-1 range
     */
    private double normalizeTemperature(double temperature) {
        // Normalize to roughly 0-1 range, assuming -20°C to 50°C range
        return Math.max(0.0, Math.min(1.0, (temperature + 20) / 70.0));
    }
    
    /**
     * Normalize humidity to 0-1 range
     */
    private double normalizeHumidity(double humidity) {
        return Math.max(0.0, Math.min(1.0, humidity / 100.0));
    }
    
    /**
     * Normalize wind speed to 0-1 range
     */
    private double normalizeWindSpeed(double windSpeed) {
        // Normalize to 0-1 range, assuming 0-50 km/h range
        return Math.max(0.0, Math.min(1.0, windSpeed / 50.0));
    }
    
    /**
     * Normalize UV index to 0-1 range
     */
    private double normalizeUVIndex(double uvIndex) {
        // Normalize to 0-1 range, assuming 0-12 UV index range
        return Math.max(0.0, Math.min(1.0, uvIndex / 12.0));
    }
    
    /**
     * Get weather condition score
     */
    private double getWeatherConditionScore(String condition) {
        String normalized = normalizeWeatherCondition(condition);
        
        switch (normalized) {
            case "clear":
                return 1.0;
            case "cloudy":
                return 0.7;
            case "rain":
                return 0.4;
            case "storm":
                return 0.2;
            case "snow":
                return 0.3;
            case "fog":
                return 0.5;
            default:
                return 0.5;
        }
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
        
        if (predictedActivity == actualActivity && result.getWeatherContext() != null) {
            String patternKey = createWeatherPatternKey(result.getWeatherContext());
            
            // Reinforce the pattern
            Map<String, Double> weatherWeights = activityWeatherWeights.get(predictedActivity);
            if (weatherWeights != null) {
                double currentWeight = weatherWeights.getOrDefault(patternKey, 0.0);
                weatherWeights.put(patternKey, currentWeight + (LEARNING_RATE * 0.1));
            }
        }
    }
    
    /**
     * Adjust for negative feedback
     */
    public void adjustForNegativeFeedback(PredictionResult result) {
        if (!isTrained) return;
        
        ActivityType predictedActivity = result.getPredictedActivity();
        ActivityType actualActivity = result.getActualActivity();
        
        if (predictedActivity != actualActivity && result.getWeatherContext() != null) {
            String patternKey = createWeatherPatternKey(result.getWeatherContext());
            
            // Reduce weight for incorrect prediction
            Map<String, Double> weatherWeights = activityWeatherWeights.get(predictedActivity);
            if (weatherWeights != null) {
                double currentWeight = weatherWeights.getOrDefault(patternKey, 0.0);
                weatherWeights.put(patternKey, Math.max(0.0, currentWeight - (LEARNING_RATE * 0.05)));
            }
            
            // Increase weight for actual activity
            Map<String, Double> actualWeights = activityWeatherWeights.get(actualActivity);
            if (actualWeights != null) {
                double currentWeight = actualWeights.getOrDefault(patternKey, 0.0);
                actualWeights.put(patternKey, currentWeight + (LEARNING_RATE * 0.05));
            }
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
        stats.put("weather_patterns_count", weatherPatterns.size());
        stats.put("feature_weights", new HashMap<>(featureWeights));
        
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
    
    /**
     * Inner class for weather pattern representation
     */
    private static class WeatherPattern {
        private int sampleCount;
        private Map<ActivityType, Double> activityScores;
        private double avgTemperature;
        private double avgHumidity;
        private double avgWindSpeed;
        private double avgUVIndex;
        
        public WeatherPattern() {
            this.sampleCount = 0;
            this.activityScores = new HashMap<>();
            this.avgTemperature = 0.0;
            this.avgHumidity = 0.0;
            this.avgWindSpeed = 0.0;
            this.avgUVIndex = 0.0;
        }
        
        public void addDataPoint(DayRecord record) {
            sampleCount++;
            
            // Update averages
            avgTemperature = ((avgTemperature * (sampleCount - 1)) + record.getTemperature()) / sampleCount;
            avgHumidity = ((avgHumidity * (sampleCount - 1)) + record.getHumidity()) / sampleCount;
            avgWindSpeed = ((avgWindSpeed * (sampleCount - 1)) + record.getWindSpeed()) / sampleCount;
            avgUVIndex = ((avgUVIndex * (sampleCount - 1)) + record.getUvIndex()) / sampleCount;
            
            // Update activity scores
            for (ActivityType activityType : ActivityType.values()) {
                double activityLevel = calculateActivityLevel(activityType, record);
                double currentScore = activityScores.getOrDefault(activityType, 0.0);
                activityScores.put(activityType, ((currentScore * (sampleCount - 1)) + activityLevel) / sampleCount);
            }
        }
        
        public double getActivityScore(ActivityType activityType) {
            return activityScores.getOrDefault(activityType, 0.0);
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