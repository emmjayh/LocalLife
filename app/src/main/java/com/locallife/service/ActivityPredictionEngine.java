package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.ActivityType;
import com.locallife.model.DayRecord;
import com.locallife.model.PredictionResult;
import com.locallife.model.Recommendation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main Activity Prediction Engine that uses weather data and historical patterns
 * to recommend optimal activities and predict user behavior
 */
public class ActivityPredictionEngine {
    private static final String TAG = "ActivityPredictionEngine";
    private static final int MIN_HISTORICAL_DAYS = 7;
    private static final double CONFIDENCE_THRESHOLD = 0.6;
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private WeatherActivityCorrelationService correlationService;
    private ActivityRecommendationService recommendationService;
    private PredictionAccuracyTracker accuracyTracker;
    
    // ML models
    private WeatherPatternModel weatherPatternModel;
    private UserBehaviorModel userBehaviorModel;
    private ActivityPatternModel activityPatternModel;
    
    // Prediction cache
    private Map<String, PredictionResult> predictionCache;
    private Map<String, List<Recommendation>> recommendationCache;
    
    public ActivityPredictionEngine(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.correlationService = new WeatherActivityCorrelationService(context);
        this.recommendationService = new ActivityRecommendationService(context, this);
        this.accuracyTracker = new PredictionAccuracyTracker(context);
        
        // Initialize ML models
        this.weatherPatternModel = new WeatherPatternModel();
        this.userBehaviorModel = new UserBehaviorModel();
        this.activityPatternModel = new ActivityPatternModel();
        
        // Initialize caches
        this.predictionCache = new HashMap<>();
        this.recommendationCache = new HashMap<>();
        
        // Initialize engine
        initializeEngine();
    }
    
    private void initializeEngine() {
        Log.d(TAG, "Initializing Activity Prediction Engine");
        
        // Load historical data and train models
        new Thread(() -> {
            try {
                loadHistoricalDataAndTrainModels();
                Log.d(TAG, "Activity Prediction Engine initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Activity Prediction Engine", e);
            }
        }).start();
    }
    
    /**
     * Load historical data and train ML models
     */
    private void loadHistoricalDataAndTrainModels() {
        List<DayRecord> historicalData = databaseHelper.getAllDayRecords();
        
        if (historicalData.size() < MIN_HISTORICAL_DAYS) {
            Log.w(TAG, "Insufficient historical data for training. Need at least " + MIN_HISTORICAL_DAYS + " days");
            return;
        }
        
        Log.d(TAG, "Training models with " + historicalData.size() + " days of data");
        
        // Train weather pattern model
        weatherPatternModel.train(historicalData);
        
        // Train user behavior model
        userBehaviorModel.train(historicalData);
        
        // Train activity pattern model
        activityPatternModel.train(historicalData);
        
        Log.d(TAG, "ML models trained successfully");
    }
    
    /**
     * Predict activity for a specific time and weather conditions
     */
    public PredictionResult predictActivity(Date targetTime, float temperature, float humidity, 
                                          String weatherCondition, float windSpeed, double uvIndex,
                                          int airQualityIndex, String moonPhase, long dayLengthMinutes) {
        
        String cacheKey = generateCacheKey(targetTime, temperature, humidity, weatherCondition);
        
        // Check cache first
        if (predictionCache.containsKey(cacheKey)) {
            PredictionResult cached = predictionCache.get(cacheKey);
            if (!cached.isOutdated()) {
                return cached;
            } else {
                predictionCache.remove(cacheKey);
            }
        }
        
        PredictionResult result = new PredictionResult();
        result.setTargetTime(targetTime);
        
        // Create weather context
        boolean isWeekend = isWeekend(targetTime);
        String timeOfDay = getTimeOfDay(targetTime);
        
        PredictionResult.WeatherContext weatherContext = new PredictionResult.WeatherContext(
            temperature, humidity, weatherCondition, windSpeed, uvIndex, airQualityIndex,
            moonPhase, dayLengthMinutes, isWeekend, timeOfDay
        );
        result.setWeatherContext(weatherContext);
        
        // Create user context
        PredictionResult.UserContext userContext = createUserContext();
        result.setUserContext(userContext);
        
        // Use ensemble approach for prediction
        Map<ActivityType, Double> predictions = new HashMap<>();
        Map<String, Double> featureImportance = new HashMap<>();
        
        // ML-based prediction
        if (weatherPatternModel.isTrained() && userBehaviorModel.isTrained() && activityPatternModel.isTrained()) {
            Map<ActivityType, Double> mlPredictions = predictWithMLModels(weatherContext, userContext, featureImportance);
            mergePredictions(predictions, mlPredictions, 0.5);
            result.setPredictionMethod("ML");
        }
        
        // Correlation-based prediction
        Map<ActivityType, Double> correlationPredictions = predictWithCorrelations(weatherContext, userContext, featureImportance);
        mergePredictions(predictions, correlationPredictions, 0.3);
        
        // Rule-based prediction
        Map<ActivityType, Double> rulePredictions = predictWithRules(weatherContext, userContext, featureImportance);
        mergePredictions(predictions, rulePredictions, 0.2);
        
        // Finalize prediction
        ActivityType predictedActivity = getBestPrediction(predictions);
        double confidence = predictions.getOrDefault(predictedActivity, 0.0);
        
        result.setPredictedActivity(predictedActivity);
        result.setConfidenceScore(confidence);
        result.setFeatureImportance(featureImportance);
        
        // Set alternative activities
        List<ActivityType> alternatives = predictions.entrySet().stream()
            .filter(entry -> entry.getKey() != predictedActivity)
            .sorted(Map.Entry.<ActivityType, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .limit(3)
            .collect(Collectors.toList());
        result.setAlternativeActivities(alternatives);
        
        // Generate reasoning
        result.setReasoning(generatePredictionReasoning(weatherContext, userContext, predictedActivity, confidence));
        
        // Update prediction method if hybrid
        if (result.getPredictionMethod() == null) {
            result.setPredictionMethod("HYBRID");
        }
        
        // Cache the result
        predictionCache.put(cacheKey, result);
        
        return result;
    }
    
    /**
     * Get activity recommendations for current conditions
     */
    public List<Recommendation> getRecommendations(int maxRecommendations) {
        String cacheKey = "recommendations_" + System.currentTimeMillis() / (1000 * 60 * 15); // 15-minute cache
        
        if (recommendationCache.containsKey(cacheKey)) {
            return recommendationCache.get(cacheKey);
        }
        
        List<Recommendation> recommendations = recommendationService.generateRecommendations(maxRecommendations);
        
        // Cache recommendations
        recommendationCache.put(cacheKey, recommendations);
        
        return recommendations;
    }
    
    /**
     * Get personalized recommendations based on user preferences and current context
     */
    public List<Recommendation> getPersonalizedRecommendations(List<ActivityType> preferredActivities, 
                                                             String currentLocation, int maxRecommendations) {
        return recommendationService.generatePersonalizedRecommendations(
            preferredActivities, currentLocation, maxRecommendations
        );
    }
    
    /**
     * Predict activity patterns for the next week
     */
    public Map<Date, List<PredictionResult>> predictWeeklyPatterns() {
        Map<Date, List<PredictionResult>> weeklyPredictions = new HashMap<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        
        // Get weather forecast (if available)
        // For now, we'll use current weather as baseline
        DayRecord today = databaseHelper.getTodayRecord();
        if (today == null) {
            Log.w(TAG, "No current weather data available for weekly prediction");
            return weeklyPredictions;
        }
        
        for (int i = 0; i < 7; i++) {
            Date targetDate = calendar.getTime();
            List<PredictionResult> dayPredictions = new ArrayList<>();
            
            // Predict for key times of day
            String[] timeSlots = {"06:00", "09:00", "12:00", "15:00", "18:00", "21:00"};
            
            for (String timeSlot : timeSlots) {
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(targetDate);
                String[] timeParts = timeSlot.split(":");
                timeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                timeCalendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                
                PredictionResult prediction = predictActivity(
                    timeCalendar.getTime(),
                    today.getTemperature(),
                    today.getHumidity(),
                    today.getWeatherCondition(),
                    today.getWindSpeed(),
                    today.getUvIndex(),
                    today.getAirQualityIndex(),
                    today.getMoonPhase(),
                    today.getDayLengthMinutes()
                );
                
                dayPredictions.add(prediction);
            }
            
            weeklyPredictions.put(targetDate, dayPredictions);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return weeklyPredictions;
    }
    
    /**
     * Update prediction accuracy with actual user activity
     */
    public void updatePredictionAccuracy(String predictionId, ActivityType actualActivity) {
        PredictionResult result = findPredictionById(predictionId);
        if (result != null) {
            result.validate(actualActivity);
            accuracyTracker.recordPredictionAccuracy(result);
            
            // Update ML models with new data
            updateMLModelsWithFeedback(result);
        }
    }
    
    /**
     * Get prediction accuracy statistics
     */
    public Map<String, Double> getPredictionAccuracyStats() {
        return accuracyTracker.getAccuracyStatistics();
    }
    
    /**
     * Get activity correlation insights
     */
    public Map<String, Object> getActivityCorrelationInsights() {
        return correlationService.getCorrelationInsights();
    }
    
    // Private helper methods
    
    private String generateCacheKey(Date targetTime, float temperature, float humidity, String weatherCondition) {
        long timeSlot = targetTime.getTime() / (1000 * 60 * 30); // 30-minute slots
        return String.format("pred_%d_%.1f_%.1f_%s", timeSlot, temperature, humidity, 
                           weatherCondition != null ? weatherCondition.hashCode() : 0);
    }
    
    private boolean isWeekend(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }
    
    private String getTimeOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 6 && hour < 12) return "MORNING";
        if (hour >= 12 && hour < 17) return "AFTERNOON";
        if (hour >= 17 && hour < 21) return "EVENING";
        return "NIGHT";
    }
    
    private PredictionResult.UserContext createUserContext() {
        DayRecord today = databaseHelper.getTodayRecord();
        List<DayRecord> recentDays = databaseHelper.getRecentDayRecords(7);
        
        if (today == null) {
            // Return default context
            return new PredictionResult.UserContext(0, 0, "unknown", 0, 
                new ArrayList<>(), "neutral", 0, "unknown", new ArrayList<>());
        }
        
        // Calculate recent activities
        List<String> recentActivities = recentDays.stream()
            .map(day -> inferPrimaryActivity(day))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        // Basic user preferences (could be enhanced with user settings)
        List<String> preferences = Arrays.asList("moderate_exercise", "social_activities", "outdoor_activities");
        
        return new PredictionResult.UserContext(
            today.getStepCount(),
            today.getActivityScore(),
            today.getPrimaryLocation(),
            today.getScreenTimeMinutes(),
            recentActivities,
            "neutral", // Could be inferred from data
            today.getPlacesVisited(),
            isWeekend(new Date()) ? "weekend" : "weekday",
            preferences
        );
    }
    
    private String inferPrimaryActivity(DayRecord day) {
        // Simple heuristic to infer primary activity from day record
        if (day.getStepCount() > 10000) return "high_activity";
        if (day.getPlacesVisited() > 3) return "social_activity";
        if (day.getScreenTimeMinutes() > 300) return "indoor_activity";
        if (day.getPhotoCount() > 5) return "photography";
        return "moderate_activity";
    }
    
    private Map<ActivityType, Double> predictWithMLModels(PredictionResult.WeatherContext weatherContext,
                                                         PredictionResult.UserContext userContext,
                                                         Map<String, Double> featureImportance) {
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        // Weather pattern prediction
        Map<ActivityType, Double> weatherPredictions = weatherPatternModel.predict(weatherContext);
        featureImportance.put("weather_pattern", 0.4);
        
        // User behavior prediction
        Map<ActivityType, Double> behaviorPredictions = userBehaviorModel.predict(userContext);
        featureImportance.put("user_behavior", 0.3);
        
        // Activity pattern prediction
        Map<ActivityType, Double> activityPredictions = activityPatternModel.predict(weatherContext, userContext);
        featureImportance.put("activity_pattern", 0.3);
        
        // Ensemble predictions
        for (ActivityType activityType : ActivityType.values()) {
            double score = weatherPredictions.getOrDefault(activityType, 0.0) * 0.4 +
                          behaviorPredictions.getOrDefault(activityType, 0.0) * 0.3 +
                          activityPredictions.getOrDefault(activityType, 0.0) * 0.3;
            predictions.put(activityType, score);
        }
        
        return predictions;
    }
    
    private Map<ActivityType, Double> predictWithCorrelations(PredictionResult.WeatherContext weatherContext,
                                                            PredictionResult.UserContext userContext,
                                                            Map<String, Double> featureImportance) {
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        // Use correlation service to get weather-activity correlations
        Map<ActivityType, Double> correlations = correlationService.getWeatherActivityCorrelations(
            weatherContext.getTemperature(), weatherContext.getHumidity(), 
            weatherContext.getWeatherCondition(), weatherContext.getWindSpeed()
        );
        
        featureImportance.put("weather_correlation", 0.6);
        featureImportance.put("time_correlation", 0.4);
        
        // Apply time-based adjustments
        for (ActivityType activityType : ActivityType.values()) {
            double baseScore = correlations.getOrDefault(activityType, 0.5);
            double timeAdjustment = getTimeBasedAdjustment(activityType, weatherContext.getTimeOfDay());
            predictions.put(activityType, baseScore * timeAdjustment);
        }
        
        return predictions;
    }
    
    private Map<ActivityType, Double> predictWithRules(PredictionResult.WeatherContext weatherContext,
                                                      PredictionResult.UserContext userContext,
                                                      Map<String, Double> featureImportance) {
        Map<ActivityType, Double> predictions = new HashMap<>();
        
        featureImportance.put("weather_rules", 0.5);
        featureImportance.put("user_rules", 0.3);
        featureImportance.put("time_rules", 0.2);
        
        // Apply weather-based rules
        for (ActivityType activityType : ActivityType.values()) {
            double weatherScore = activityType.getWeatherSuitability(
                weatherContext.getTemperature(), weatherContext.getWeatherCondition(),
                weatherContext.getHumidity(), weatherContext.getWindSpeed(), weatherContext.getUvIndex()
            );
            
            // Apply user context rules
            double userScore = getUserContextScore(activityType, userContext);
            
            // Apply time-based rules
            double timeScore = getTimeBasedScore(activityType, weatherContext.getTimeOfDay(), weatherContext.isWeekend());
            
            double finalScore = weatherScore * 0.5 + userScore * 0.3 + timeScore * 0.2;
            predictions.put(activityType, finalScore);
        }
        
        return predictions;
    }
    
    private double getTimeBasedAdjustment(ActivityType activityType, String timeOfDay) {
        switch (timeOfDay) {
            case "MORNING":
                if (activityType == ActivityType.OUTDOOR_EXERCISE || 
                    activityType == ActivityType.WORK_PRODUCTIVITY) return 1.2;
                break;
            case "AFTERNOON":
                if (activityType == ActivityType.SOCIAL_ACTIVITY || 
                    activityType == ActivityType.RECREATIONAL) return 1.1;
                break;
            case "EVENING":
                if (activityType == ActivityType.RELAXATION || 
                    activityType == ActivityType.INDOOR_ACTIVITIES) return 1.1;
                break;
            case "NIGHT":
                if (activityType == ActivityType.RELAXATION || 
                    activityType == ActivityType.INDOOR_ACTIVITIES) return 1.2;
                break;
        }
        return 1.0;
    }
    
    private double getUserContextScore(ActivityType activityType, PredictionResult.UserContext userContext) {
        double score = 0.5; // Base score
        
        // Adjust based on recent step count
        if (userContext.getRecentStepCount() > 8000) {
            if (activityType == ActivityType.OUTDOOR_EXERCISE) score += 0.2;
            if (activityType == ActivityType.RELAXATION) score += 0.1;
        } else if (userContext.getRecentStepCount() < 3000) {
            if (activityType == ActivityType.INDOOR_ACTIVITIES) score += 0.2;
            if (activityType == ActivityType.RELAXATION) score += 0.15;
        }
        
        // Adjust based on screen time
        if (userContext.getScreenTimeMinutes() > 360) { // 6 hours
            if (activityType == ActivityType.OUTDOOR_EXERCISE) score += 0.2;
            if (activityType == ActivityType.SOCIAL_ACTIVITY) score += 0.15;
        }
        
        // Adjust based on recent activities
        if (userContext.getRecentActivities().contains("high_activity")) {
            if (activityType == ActivityType.RELAXATION) score += 0.1;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    private double getTimeBasedScore(ActivityType activityType, String timeOfDay, boolean isWeekend) {
        double score = 0.5; // Base score
        
        // Weekend adjustments
        if (isWeekend) {
            if (activityType == ActivityType.SOCIAL_ACTIVITY || 
                activityType == ActivityType.RECREATIONAL || 
                activityType == ActivityType.OUTDOOR_LEISURE) {
                score += 0.2;
            }
        } else {
            if (activityType == ActivityType.WORK_PRODUCTIVITY) {
                score += 0.3;
            }
        }
        
        // Time of day adjustments
        switch (timeOfDay) {
            case "MORNING":
                if (activityType == ActivityType.OUTDOOR_EXERCISE) score += 0.2;
                if (activityType == ActivityType.WORK_PRODUCTIVITY) score += 0.1;
                break;
            case "AFTERNOON":
                if (activityType == ActivityType.SOCIAL_ACTIVITY) score += 0.15;
                if (activityType == ActivityType.RECREATIONAL) score += 0.1;
                break;
            case "EVENING":
                if (activityType == ActivityType.RELAXATION) score += 0.2;
                if (activityType == ActivityType.INDOOR_ACTIVITIES) score += 0.1;
                break;
            case "NIGHT":
                if (activityType == ActivityType.RELAXATION) score += 0.3;
                if (activityType == ActivityType.INDOOR_ACTIVITIES) score += 0.2;
                break;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    private void mergePredictions(Map<ActivityType, Double> target, Map<ActivityType, Double> source, double weight) {
        for (Map.Entry<ActivityType, Double> entry : source.entrySet()) {
            ActivityType activity = entry.getKey();
            double value = entry.getValue() * weight;
            target.merge(activity, value, Double::sum);
        }
    }
    
    private ActivityType getBestPrediction(Map<ActivityType, Double> predictions) {
        return predictions.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(ActivityType.INDOOR_ACTIVITIES);
    }
    
    private String generatePredictionReasoning(PredictionResult.WeatherContext weatherContext,
                                             PredictionResult.UserContext userContext,
                                             ActivityType predictedActivity, double confidence) {
        StringBuilder reasoning = new StringBuilder();
        
        reasoning.append("Based on current weather conditions (")
                .append(String.format("%.1f°C, %s", weatherContext.getTemperature(), weatherContext.getWeatherCondition()))
                .append(") and your recent activity patterns, ");
        
        if (confidence > 0.8) {
            reasoning.append("I'm highly confident that ");
        } else if (confidence > 0.6) {
            reasoning.append("I believe that ");
        } else {
            reasoning.append("you might consider ");
        }
        
        reasoning.append(predictedActivity.getDisplayName().toLowerCase())
                .append(" would be a good choice for you right now.");
        
        // Add specific reasoning based on weather
        if (weatherContext.getTemperature() > 25) {
            reasoning.append(" The warm weather is ideal for outdoor activities.");
        } else if (weatherContext.getTemperature() < 10) {
            reasoning.append(" The cool weather suggests indoor activities might be more comfortable.");
        }
        
        // Add reasoning based on user context
        if (userContext.getRecentStepCount() < 3000) {
            reasoning.append(" You haven't been very active recently, so this could help boost your activity level.");
        }
        
        return reasoning.toString();
    }
    
    private PredictionResult findPredictionById(String predictionId) {
        return predictionCache.values().stream()
            .filter(result -> result.getPredictionId().equals(predictionId))
            .findFirst()
            .orElse(null);
    }
    
    private void updateMLModelsWithFeedback(PredictionResult result) {
        // Update models with feedback (simplified incremental learning)
        if (result.getPredictionAccuracy() > 0.5) {
            // Positive feedback - reinforce prediction
            weatherPatternModel.reinforcePositiveFeedback(result);
            userBehaviorModel.reinforcePositiveFeedback(result);
            activityPatternModel.reinforcePositiveFeedback(result);
        } else {
            // Negative feedback - adjust models
            weatherPatternModel.adjustForNegativeFeedback(result);
            userBehaviorModel.adjustForNegativeFeedback(result);
            activityPatternModel.adjustForNegativeFeedback(result);
        }
    }
    
    /**
     * Clear prediction cache
     */
    public void clearCache() {
        predictionCache.clear();
        recommendationCache.clear();
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("prediction_cache_size", predictionCache.size());
        stats.put("recommendation_cache_size", recommendationCache.size());
        return stats;
    }
}