package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.ActivityType;
import com.locallife.model.DayRecord;
import com.locallife.model.Recommendation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that provides personalized activity recommendations based on weather, user patterns, and preferences
 */
public class ActivityRecommendationService {
    private static final String TAG = "ActivityRecommendationService";
    private static final int MAX_RECOMMENDATIONS = 10;
    private static final double MIN_RECOMMENDATION_SCORE = 0.4;
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ActivityPredictionEngine predictionEngine;
    private WeatherActivityCorrelationService correlationService;
    
    // Recommendation strategies
    private Map<String, RecommendationStrategy> strategies;
    
    public ActivityRecommendationService(Context context, ActivityPredictionEngine predictionEngine) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.predictionEngine = predictionEngine;
        this.correlationService = new WeatherActivityCorrelationService(context);
        
        initializeStrategies();
    }
    
    /**
     * Initialize recommendation strategies
     */
    private void initializeStrategies() {
        strategies = new HashMap<>();
        strategies.put("weather_based", new WeatherBasedStrategy());
        strategies.put("pattern_based", new PatternBasedStrategy());
        strategies.put("goal_based", new GoalBasedStrategy());
        strategies.put("time_based", new TimeBasedStrategy());
        strategies.put("location_based", new LocationBasedStrategy());
        strategies.put("social_based", new SocialBasedStrategy());
        strategies.put("mood_based", new MoodBasedStrategy());
    }
    
    /**
     * Generate general activity recommendations
     */
    public List<Recommendation> generateRecommendations(int maxRecommendations) {
        Log.d(TAG, "Generating " + maxRecommendations + " activity recommendations");
        
        List<Recommendation> recommendations = new ArrayList<>();
        DayRecord currentDay = databaseHelper.getTodayRecord();
        
        if (currentDay == null) {
            Log.w(TAG, "No current day data available for recommendations");
            return generateDefaultRecommendations(maxRecommendations);
        }
        
        // Get current context
        RecommendationContext context = createRecommendationContext(currentDay);
        
        // Apply each strategy
        for (RecommendationStrategy strategy : strategies.values()) {
            List<Recommendation> strategyRecommendations = strategy.generateRecommendations(context, maxRecommendations);
            recommendations.addAll(strategyRecommendations);
        }
        
        // Merge and rank recommendations
        List<Recommendation> mergedRecommendations = mergeAndRankRecommendations(recommendations);
        
        // Filter by minimum score
        mergedRecommendations = mergedRecommendations.stream()
            .filter(rec -> rec.getConfidenceScore() >= MIN_RECOMMENDATION_SCORE)
            .collect(Collectors.toList());
        
        // Limit to requested count
        return mergedRecommendations.stream()
            .limit(maxRecommendations)
            .collect(Collectors.toList());
    }
    
    /**
     * Generate personalized recommendations based on user preferences
     */
    public List<Recommendation> generatePersonalizedRecommendations(List<ActivityType> preferredActivities, 
                                                                   String currentLocation, int maxRecommendations) {
        Log.d(TAG, "Generating personalized recommendations for " + preferredActivities.size() + " preferred activities");
        
        List<Recommendation> recommendations = new ArrayList<>();
        DayRecord currentDay = databaseHelper.getTodayRecord();
        
        if (currentDay == null) {
            return generateDefaultRecommendations(maxRecommendations);
        }
        
        RecommendationContext context = createRecommendationContext(currentDay);
        context.setPreferredActivities(preferredActivities);
        context.setCurrentLocation(currentLocation);
        
        // Generate recommendations for preferred activities
        for (ActivityType preferredActivity : preferredActivities) {
            Recommendation recommendation = generateRecommendationForActivity(preferredActivity, context);
            if (recommendation != null && recommendation.getConfidenceScore() >= MIN_RECOMMENDATION_SCORE) {
                recommendations.add(recommendation);
            }
        }
        
        // Fill remaining slots with general recommendations
        if (recommendations.size() < maxRecommendations) {
            List<Recommendation> generalRecommendations = generateRecommendations(maxRecommendations - recommendations.size());
            
            // Avoid duplicates
            Set<ActivityType> existingTypes = recommendations.stream()
                .map(Recommendation::getActivityType)
                .collect(Collectors.toSet());
            
            generalRecommendations.stream()
                .filter(rec -> !existingTypes.contains(rec.getActivityType()))
                .forEach(recommendations::add);
        }
        
        return recommendations.stream()
            .limit(maxRecommendations)
            .collect(Collectors.toList());
    }
    
    /**
     * Generate recommendation for a specific activity
     */
    private Recommendation generateRecommendationForActivity(ActivityType activityType, RecommendationContext context) {
        Recommendation recommendation = new Recommendation();
        recommendation.setActivityType(activityType);
        recommendation.setTitle(generateTitle(activityType, context));
        recommendation.setDescription(generateDescription(activityType, context));
        
        // Calculate weather suitability
        Recommendation.WeatherSuitability weatherSuitability = calculateWeatherSuitability(activityType, context);
        recommendation.setWeatherSuitability(weatherSuitability);
        
        // Calculate personalization score
        Recommendation.UserPersonalizationScore personalizationScore = calculatePersonalizationScore(activityType, context);
        recommendation.setPersonalizationScore(personalizationScore);
        
        // Calculate overall confidence score
        double confidence = recommendation.calculateOverallScore();
        recommendation.setConfidenceScore(confidence);
        
        // Set timing and location
        recommendation.setRecommendedTime(new Date());
        recommendation.setLocation(suggestLocation(activityType, context));
        recommendation.setDurationMinutes(suggestDuration(activityType, context));
        
        // Set requirements and benefits
        recommendation.setRequirements(generateRequirements(activityType, context));
        recommendation.setBenefits(generateBenefits(activityType, context));
        
        // Generate reasoning
        recommendation.setReasoning(generateReasoning(activityType, context, confidence));
        
        return recommendation;
    }
    
    /**
     * Create recommendation context from current day data
     */
    private RecommendationContext createRecommendationContext(DayRecord currentDay) {
        RecommendationContext context = new RecommendationContext();
        
        // Current weather
        context.setTemperature(currentDay.getTemperature());
        context.setHumidity(currentDay.getHumidity());
        context.setWeatherCondition(currentDay.getWeatherCondition());
        context.setWindSpeed(currentDay.getWindSpeed());
        context.setUvIndex(currentDay.getUvIndex());
        context.setAirQualityIndex(currentDay.getAirQualityIndex());
        
        // User context
        context.setCurrentStepCount(currentDay.getStepCount());
        context.setCurrentActivityScore(currentDay.getActivityScore());
        context.setScreenTime(currentDay.getScreenTimeMinutes());
        context.setPlacesVisited(currentDay.getPlacesVisited());
        context.setPrimaryLocation(currentDay.getPrimaryLocation());
        
        // Time context
        context.setCurrentTime(new Date());
        context.setIsWeekend(isWeekend(new Date()));
        context.setTimeOfDay(getTimeOfDay(new Date()));
        
        // Historical context
        List<DayRecord> recentDays = databaseHelper.getRecentDayRecords(14);
        context.setRecentDays(recentDays);
        
        return context;
    }
    
    /**
     * Calculate weather suitability for activity
     */
    private Recommendation.WeatherSuitability calculateWeatherSuitability(ActivityType activityType, RecommendationContext context) {
        double temperatureScore = calculateTemperatureScore(activityType, context.getTemperature());
        double weatherConditionScore = calculateWeatherConditionScore(activityType, context.getWeatherCondition());
        double humidityScore = calculateHumidityScore(activityType, context.getHumidity());
        double windScore = calculateWindScore(activityType, context.getWindSpeed());
        double uvScore = calculateUVScore(activityType, context.getUvIndex());
        
        double overallScore = (temperatureScore * 0.3 + weatherConditionScore * 0.25 + 
                             humidityScore * 0.2 + windScore * 0.15 + uvScore * 0.1);
        
        String weatherReasoning = generateWeatherReasoning(activityType, context, overallScore);
        
        return new Recommendation.WeatherSuitability(overallScore, temperatureScore, weatherConditionScore,
                                                   humidityScore, windScore, uvScore, weatherReasoning);
    }
    
    /**
     * Calculate personalization score for activity
     */
    private Recommendation.UserPersonalizationScore calculatePersonalizationScore(ActivityType activityType, RecommendationContext context) {
        double historicalScore = calculateHistoricalPreferenceScore(activityType, context);
        double timeScore = calculateTimeBasedScore(activityType, context);
        double locationScore = calculateLocationBasedScore(activityType, context);
        double frequencyScore = calculateActivityFrequencyScore(activityType, context);
        double socialScore = calculateSocialContextScore(activityType, context);
        
        String reasoning = generatePersonalizationReasoning(activityType, context, historicalScore, timeScore, locationScore, frequencyScore, socialScore);
        
        return new Recommendation.UserPersonalizationScore(historicalScore, timeScore, locationScore, frequencyScore, socialScore, reasoning);
    }
    
    /**
     * Merge and rank recommendations from different strategies
     */
    private List<Recommendation> mergeAndRankRecommendations(List<Recommendation> recommendations) {
        // Group recommendations by activity type
        Map<ActivityType, List<Recommendation>> groupedRecommendations = recommendations.stream()
            .collect(Collectors.groupingBy(Recommendation::getActivityType));
        
        List<Recommendation> mergedRecommendations = new ArrayList<>();
        
        // For each activity type, merge recommendations or take the best one
        for (Map.Entry<ActivityType, List<Recommendation>> entry : groupedRecommendations.entrySet()) {
            List<Recommendation> activityRecommendations = entry.getValue();
            
            if (activityRecommendations.size() == 1) {
                mergedRecommendations.add(activityRecommendations.get(0));
            } else {
                // Merge multiple recommendations for the same activity
                Recommendation merged = mergeRecommendations(activityRecommendations);
                mergedRecommendations.add(merged);
            }
        }
        
        // Sort by confidence score
        mergedRecommendations.sort((r1, r2) -> Double.compare(r2.getConfidenceScore(), r1.getConfidenceScore()));
        
        return mergedRecommendations;
    }
    
    /**
     * Merge multiple recommendations for the same activity
     */
    private Recommendation mergeRecommendations(List<Recommendation> recommendations) {
        // Take the recommendation with highest confidence as base
        Recommendation best = recommendations.stream()
            .max(Comparator.comparingDouble(Recommendation::getConfidenceScore))
            .orElse(recommendations.get(0));
        
        // Average the confidence scores
        double avgConfidence = recommendations.stream()
            .mapToDouble(Recommendation::getConfidenceScore)
            .average()
            .orElse(best.getConfidenceScore());
        
        best.setConfidenceScore(avgConfidence);
        
        // Merge reasoning
        String combinedReasoning = recommendations.stream()
            .map(Recommendation::getReasoning)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
        best.setReasoning(combinedReasoning);
        
        return best;
    }
    
    /**
     * Generate default recommendations when no data is available
     */
    private List<Recommendation> generateDefaultRecommendations(int maxRecommendations) {
        List<Recommendation> defaultRecommendations = new ArrayList<>();
        
        // Create basic recommendations for common activities
        ActivityType[] commonActivities = {
            ActivityType.RELAXATION, ActivityType.INDOOR_ACTIVITIES, ActivityType.WORK_PRODUCTIVITY,
            ActivityType.SOCIAL_ACTIVITY, ActivityType.RECREATIONAL
        };
        
        for (int i = 0; i < Math.min(maxRecommendations, commonActivities.length); i++) {
            ActivityType activityType = commonActivities[i];
            Recommendation recommendation = new Recommendation();
            recommendation.setActivityType(activityType);
            recommendation.setTitle(activityType.getDisplayName());
            recommendation.setDescription("A good activity to consider today");
            recommendation.setConfidenceScore(0.5);
            recommendation.setReasoning("General recommendation based on common activities");
            defaultRecommendations.add(recommendation);
        }
        
        return defaultRecommendations;
    }
    
    // Helper methods for scoring
    
    private double calculateTemperatureScore(ActivityType activityType, float temperature) {
        return activityType.getWeatherSuitability(temperature, "clear", 50, 10, 5);
    }
    
    private double calculateWeatherConditionScore(ActivityType activityType, String weatherCondition) {
        return activityType.getWeatherSuitability(20, weatherCondition, 50, 10, 5);
    }
    
    private double calculateHumidityScore(ActivityType activityType, float humidity) {
        return activityType.getWeatherSuitability(20, "clear", humidity, 10, 5);
    }
    
    private double calculateWindScore(ActivityType activityType, float windSpeed) {
        return activityType.getWeatherSuitability(20, "clear", 50, windSpeed, 5);
    }
    
    private double calculateUVScore(ActivityType activityType, double uvIndex) {
        return activityType.getWeatherSuitability(20, "clear", 50, 10, uvIndex);
    }
    
    private double calculateHistoricalPreferenceScore(ActivityType activityType, RecommendationContext context) {
        if (context.getRecentDays() == null || context.getRecentDays().isEmpty()) {
            return 0.5;
        }
        
        // Calculate how often this activity type was performed recently
        long activityCount = context.getRecentDays().stream()
            .mapToLong(day -> getActivityOccurrenceCount(activityType, day))
            .sum();
        
        double frequency = (double) activityCount / context.getRecentDays().size();
        return Math.min(1.0, frequency + 0.3); // Boost for familiarity
    }
    
    private long getActivityOccurrenceCount(ActivityType activityType, DayRecord day) {
        // Simplified heuristic - in real implementation, this would be more sophisticated
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                return day.getStepCount() > 10000 ? 1 : 0;
            case SOCIAL_ACTIVITY:
                return day.getPlacesVisited() > 3 ? 1 : 0;
            case PHOTOGRAPHY:
                return day.getPhotoCount() > 5 ? 1 : 0;
            case INDOOR_ACTIVITIES:
                return day.getScreenTimeMinutes() > 300 ? 1 : 0;
            default:
                return 0;
        }
    }
    
    private double calculateTimeBasedScore(ActivityType activityType, RecommendationContext context) {
        String timeOfDay = context.getTimeOfDay();
        boolean isWeekend = context.isWeekend();
        
        double score = 0.5;
        
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
                break;
            case "AFTERNOON":
                if (activityType == ActivityType.SOCIAL_ACTIVITY) score += 0.15;
                break;
            case "EVENING":
                if (activityType == ActivityType.RELAXATION) score += 0.2;
                break;
            case "NIGHT":
                if (activityType == ActivityType.INDOOR_ACTIVITIES) score += 0.2;
                break;
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    private double calculateLocationBasedScore(ActivityType activityType, RecommendationContext context) {
        String primaryLocation = context.getPrimaryLocation();
        if (primaryLocation == null) return 0.5;
        
        // Adjust score based on current location
        if (primaryLocation.toLowerCase().contains("home")) {
            if (activityType == ActivityType.INDOOR_ACTIVITIES || 
                activityType == ActivityType.RELAXATION) {
                return 0.8;
            }
        } else if (primaryLocation.toLowerCase().contains("work") || 
                   primaryLocation.toLowerCase().contains("office")) {
            if (activityType == ActivityType.WORK_PRODUCTIVITY) {
                return 0.9;
            }
        } else if (primaryLocation.toLowerCase().contains("park") || 
                   primaryLocation.toLowerCase().contains("outdoor")) {
            if (activityType == ActivityType.OUTDOOR_EXERCISE || 
                activityType == ActivityType.OUTDOOR_LEISURE) {
                return 0.9;
            }
        }
        
        return 0.5;
    }
    
    private double calculateActivityFrequencyScore(ActivityType activityType, RecommendationContext context) {
        if (context.getRecentDays() == null || context.getRecentDays().isEmpty()) {
            return 0.5;
        }
        
        // Calculate recent activity frequency
        long recentCount = context.getRecentDays().stream()
            .mapToLong(day -> getActivityOccurrenceCount(activityType, day))
            .sum();
        
        double frequency = (double) recentCount / context.getRecentDays().size();
        
        // Avoid over-recommending frequently done activities
        if (frequency > 0.7) {
            return 0.3; // Reduce score for over-done activities
        } else if (frequency < 0.2) {
            return 0.8; // Boost score for under-done activities
        }
        
        return 0.5;
    }
    
    private double calculateSocialContextScore(ActivityType activityType, RecommendationContext context) {
        int placesVisited = context.getPlacesVisited();
        
        if (placesVisited > 3) {
            // User has been social today
            if (activityType == ActivityType.RELAXATION || 
                activityType == ActivityType.INDOOR_ACTIVITIES) {
                return 0.8; // Suggest balance
            }
        } else if (placesVisited < 2) {
            // User hasn't been very social
            if (activityType == ActivityType.SOCIAL_ACTIVITY || 
                activityType == ActivityType.RECREATIONAL) {
                return 0.8; // Encourage social activity
            }
        }
        
        return 0.5;
    }
    
    // Helper methods for content generation
    
    private String generateTitle(ActivityType activityType, RecommendationContext context) {
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                return "Go for a " + (context.getTemperature() > 20 ? "run" : "walk") + " outside";
            case SOCIAL_ACTIVITY:
                return "Meet up with friends";
            case PHOTOGRAPHY:
                return "Take some photos";
            case RELAXATION:
                return "Take time to relax";
            case WORK_PRODUCTIVITY:
                return "Focus on productive work";
            case RECREATIONAL:
                return "Enjoy some recreational time";
            default:
                return activityType.getDisplayName();
        }
    }
    
    private String generateDescription(ActivityType activityType, RecommendationContext context) {
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                return "The weather conditions are good for outdoor physical activity";
            case SOCIAL_ACTIVITY:
                return "It's a great time to connect with friends and family";
            case PHOTOGRAPHY:
                return "Capture the beauty around you with some photography";
            case RELAXATION:
                return "Take a break and focus on your wellbeing";
            case WORK_PRODUCTIVITY:
                return "Channel your energy into productive work";
            case RECREATIONAL:
                return "Enjoy some entertainment and leisure activities";
            default:
                return "A good activity to consider based on current conditions";
        }
    }
    
    private String generateReasoning(ActivityType activityType, RecommendationContext context, double confidence) {
        StringBuilder reasoning = new StringBuilder();
        
        reasoning.append("Based on current weather (")
                .append(String.format("%.1f°C, %s", context.getTemperature(), context.getWeatherCondition()))
                .append(") and your recent activity patterns, ");
        
        if (confidence > 0.8) {
            reasoning.append("this is an excellent choice for you right now.");
        } else if (confidence > 0.6) {
            reasoning.append("this would be a good activity to consider.");
        } else {
            reasoning.append("you might want to consider this activity.");
        }
        
        return reasoning.toString();
    }
    
    private String generateWeatherReasoning(ActivityType activityType, RecommendationContext context, double overallScore) {
        if (overallScore > 0.8) {
            return "Weather conditions are excellent for this activity";
        } else if (overallScore > 0.6) {
            return "Weather conditions are good for this activity";
        } else if (overallScore > 0.4) {
            return "Weather conditions are acceptable for this activity";
        } else {
            return "Weather conditions are challenging for this activity";
        }
    }
    
    private String generatePersonalizationReasoning(ActivityType activityType, RecommendationContext context, 
                                                   double historicalScore, double timeScore, double locationScore, 
                                                   double frequencyScore, double socialScore) {
        StringBuilder reasoning = new StringBuilder();
        
        if (historicalScore > 0.7) {
            reasoning.append("You've enjoyed this activity recently. ");
        } else if (frequencyScore > 0.7) {
            reasoning.append("You haven't done this activity much lately. ");
        }
        
        if (timeScore > 0.7) {
            reasoning.append("The timing is perfect for this activity. ");
        }
        
        if (locationScore > 0.7) {
            reasoning.append("Your current location is ideal for this. ");
        }
        
        if (socialScore > 0.7) {
            reasoning.append("This would provide good social balance. ");
        }
        
        return reasoning.toString();
    }
    
    private String suggestLocation(ActivityType activityType, RecommendationContext context) {
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                return "Nearby park or trail";
            case SOCIAL_ACTIVITY:
                return "Cafe or restaurant";
            case PHOTOGRAPHY:
                return "Scenic location";
            case RELAXATION:
                return "Comfortable indoor space";
            case WORK_PRODUCTIVITY:
                return "Quiet workspace";
            case RECREATIONAL:
                return "Entertainment venue";
            default:
                return "Suitable location";
        }
    }
    
    private int suggestDuration(ActivityType activityType, RecommendationContext context) {
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                return 45;
            case SOCIAL_ACTIVITY:
                return 120;
            case PHOTOGRAPHY:
                return 60;
            case RELAXATION:
                return 30;
            case WORK_PRODUCTIVITY:
                return 90;
            case RECREATIONAL:
                return 90;
            default:
                return 60;
        }
    }
    
    private List<String> generateRequirements(ActivityType activityType, RecommendationContext context) {
        List<String> requirements = new ArrayList<>();
        
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                requirements.add("Comfortable shoes");
                requirements.add("Weather-appropriate clothing");
                if (context.getUvIndex() > 6) {
                    requirements.add("Sunscreen");
                }
                break;
            case SOCIAL_ACTIVITY:
                requirements.add("Social contacts");
                requirements.add("Transportation");
                break;
            case PHOTOGRAPHY:
                requirements.add("Camera or smartphone");
                requirements.add("Good lighting");
                break;
            case RELAXATION:
                requirements.add("Quiet space");
                requirements.add("Comfortable seating");
                break;
            case WORK_PRODUCTIVITY:
                requirements.add("Workspace");
                requirements.add("Necessary tools");
                break;
            case RECREATIONAL:
                requirements.add("Entertainment options");
                requirements.add("Free time");
                break;
        }
        
        return requirements;
    }
    
    private List<String> generateBenefits(ActivityType activityType, RecommendationContext context) {
        List<String> benefits = new ArrayList<>();
        
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                benefits.add("Improved physical fitness");
                benefits.add("Fresh air and vitamin D");
                benefits.add("Enhanced mood");
                break;
            case SOCIAL_ACTIVITY:
                benefits.add("Strengthened relationships");
                benefits.add("Improved social wellbeing");
                benefits.add("Stress relief");
                break;
            case PHOTOGRAPHY:
                benefits.add("Creative expression");
                benefits.add("Mindfulness practice");
                benefits.add("Memory preservation");
                break;
            case RELAXATION:
                benefits.add("Stress reduction");
                benefits.add("Mental clarity");
                benefits.add("Improved wellbeing");
                break;
            case WORK_PRODUCTIVITY:
                benefits.add("Goal achievement");
                benefits.add("Skill development");
                benefits.add("Professional growth");
                break;
            case RECREATIONAL:
                benefits.add("Entertainment and joy");
                benefits.add("Stress relief");
                benefits.add("Work-life balance");
                break;
        }
        
        return benefits;
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
    
    // Recommendation context class
    private static class RecommendationContext {
        private float temperature;
        private float humidity;
        private String weatherCondition;
        private float windSpeed;
        private double uvIndex;
        private int airQualityIndex;
        private int currentStepCount;
        private float currentActivityScore;
        private int screenTime;
        private int placesVisited;
        private String primaryLocation;
        private Date currentTime;
        private boolean isWeekend;
        private String timeOfDay;
        private List<DayRecord> recentDays;
        private List<ActivityType> preferredActivities;
        private String currentLocation;
        
        // Getters and setters
        public float getTemperature() { return temperature; }
        public void setTemperature(float temperature) { this.temperature = temperature; }
        
        public float getHumidity() { return humidity; }
        public void setHumidity(float humidity) { this.humidity = humidity; }
        
        public String getWeatherCondition() { return weatherCondition; }
        public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
        
        public float getWindSpeed() { return windSpeed; }
        public void setWindSpeed(float windSpeed) { this.windSpeed = windSpeed; }
        
        public double getUvIndex() { return uvIndex; }
        public void setUvIndex(double uvIndex) { this.uvIndex = uvIndex; }
        
        public int getAirQualityIndex() { return airQualityIndex; }
        public void setAirQualityIndex(int airQualityIndex) { this.airQualityIndex = airQualityIndex; }
        
        public int getCurrentStepCount() { return currentStepCount; }
        public void setCurrentStepCount(int currentStepCount) { this.currentStepCount = currentStepCount; }
        
        public float getCurrentActivityScore() { return currentActivityScore; }
        public void setCurrentActivityScore(float currentActivityScore) { this.currentActivityScore = currentActivityScore; }
        
        public int getScreenTime() { return screenTime; }
        public void setScreenTime(int screenTime) { this.screenTime = screenTime; }
        
        public int getPlacesVisited() { return placesVisited; }
        public void setPlacesVisited(int placesVisited) { this.placesVisited = placesVisited; }
        
        public String getPrimaryLocation() { return primaryLocation; }
        public void setPrimaryLocation(String primaryLocation) { this.primaryLocation = primaryLocation; }
        
        public Date getCurrentTime() { return currentTime; }
        public void setCurrentTime(Date currentTime) { this.currentTime = currentTime; }
        
        public boolean isWeekend() { return isWeekend; }
        public void setWeekend(boolean weekend) { isWeekend = weekend; }
        
        public String getTimeOfDay() { return timeOfDay; }
        public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }
        
        public List<DayRecord> getRecentDays() { return recentDays; }
        public void setRecentDays(List<DayRecord> recentDays) { this.recentDays = recentDays; }
        
        public List<ActivityType> getPreferredActivities() { return preferredActivities; }
        public void setPreferredActivities(List<ActivityType> preferredActivities) { this.preferredActivities = preferredActivities; }
        
        public String getCurrentLocation() { return currentLocation; }
        public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    }
    
    // Recommendation strategy interfaces and implementations
    private interface RecommendationStrategy {
        List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount);
    }
    
    private class WeatherBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount) {
            List<Recommendation> recommendations = new ArrayList<>();
            
            // Get weather-activity correlations
            Map<ActivityType, Double> correlations = correlationService.getWeatherActivityCorrelations(
                context.getTemperature(), context.getHumidity(), 
                context.getWeatherCondition(), context.getWindSpeed()
            );
            
            // Create recommendations for top correlated activities
            correlations.entrySet().stream()
                .sorted(Map.Entry.<ActivityType, Double>comparingByValue().reversed())
                .limit(maxCount)
                .forEach(entry -> {
                    if (entry.getValue() > 0.4) {
                        Recommendation rec = generateRecommendationForActivity(entry.getKey(), context);
                        if (rec != null) {
                            recommendations.add(rec);
                        }
                    }
                });
            
            return recommendations;
        }
    }
    
    private class PatternBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount) {
            List<Recommendation> recommendations = new ArrayList<>();
            
            if (context.getRecentDays() == null || context.getRecentDays().isEmpty()) {
                return recommendations;
            }
            
            // Analyze patterns in recent days
            Map<ActivityType, Integer> activityCounts = new HashMap<>();
            
            for (DayRecord day : context.getRecentDays()) {
                // Simple pattern detection based on activity levels
                if (day.getStepCount() > 10000) {
                    activityCounts.merge(ActivityType.OUTDOOR_EXERCISE, 1, Integer::sum);
                }
                if (day.getPlacesVisited() > 3) {
                    activityCounts.merge(ActivityType.SOCIAL_ACTIVITY, 1, Integer::sum);
                }
                if (day.getPhotoCount() > 5) {
                    activityCounts.merge(ActivityType.PHOTOGRAPHY, 1, Integer::sum);
                }
            }
            
            // Recommend activities that are under-represented
            for (ActivityType activityType : ActivityType.values()) {
                int count = activityCounts.getOrDefault(activityType, 0);
                if (count < context.getRecentDays().size() / 3) { // Less than 1/3 of days
                    Recommendation rec = generateRecommendationForActivity(activityType, context);
                    if (rec != null && rec.getConfidenceScore() > 0.5) {
                        recommendations.add(rec);
                    }
                }
            }
            
            return recommendations.stream().limit(maxCount).collect(Collectors.toList());
        }
    }
    
    private class GoalBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount) {
            List<Recommendation> recommendations = new ArrayList<>();
            
            // Check if user is meeting activity goals
            if (context.getCurrentStepCount() < 8000) {
                Recommendation rec = generateRecommendationForActivity(ActivityType.OUTDOOR_EXERCISE, context);
                if (rec != null) {
                    rec.setReasoning("Help reach your daily step goal");
                    recommendations.add(rec);
                }
            }
            
            if (context.getScreenTime() > 360) { // 6 hours
                Recommendation rec = generateRecommendationForActivity(ActivityType.OUTDOOR_LEISURE, context);
                if (rec != null) {
                    rec.setReasoning("Balance screen time with outdoor activity");
                    recommendations.add(rec);
                }
            }
            
            return recommendations.stream().limit(maxCount).collect(Collectors.toList());
        }
    }
    
    private class TimeBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount) {
            List<Recommendation> recommendations = new ArrayList<>();
            
            String timeOfDay = context.getTimeOfDay();
            
            switch (timeOfDay) {
                case "MORNING":
                    recommendations.add(generateRecommendationForActivity(ActivityType.OUTDOOR_EXERCISE, context));
                    recommendations.add(generateRecommendationForActivity(ActivityType.WORK_PRODUCTIVITY, context));
                    break;
                case "AFTERNOON":
                    recommendations.add(generateRecommendationForActivity(ActivityType.SOCIAL_ACTIVITY, context));
                    recommendations.add(generateRecommendationForActivity(ActivityType.RECREATIONAL, context));
                    break;
                case "EVENING":
                    recommendations.add(generateRecommendationForActivity(ActivityType.RELAXATION, context));
                    recommendations.add(generateRecommendationForActivity(ActivityType.INDOOR_ACTIVITIES, context));
                    break;
                case "NIGHT":
                    recommendations.add(generateRecommendationForActivity(ActivityType.RELAXATION, context));
                    break;
            }
            
            return recommendations.stream()
                .filter(Objects::nonNull)
                .limit(maxCount)
                .collect(Collectors.toList());
        }
    }
    
    private class LocationBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount) {
            List<Recommendation> recommendations = new ArrayList<>();
            
            String location = context.getPrimaryLocation();
            if (location == null) return recommendations;
            
            if (location.toLowerCase().contains("home")) {
                recommendations.add(generateRecommendationForActivity(ActivityType.INDOOR_ACTIVITIES, context));
                recommendations.add(generateRecommendationForActivity(ActivityType.RELAXATION, context));
            } else if (location.toLowerCase().contains("work")) {
                recommendations.add(generateRecommendationForActivity(ActivityType.WORK_PRODUCTIVITY, context));
            } else if (location.toLowerCase().contains("park")) {
                recommendations.add(generateRecommendationForActivity(ActivityType.OUTDOOR_EXERCISE, context));
                recommendations.add(generateRecommendationForActivity(ActivityType.PHOTOGRAPHY, context));
            }
            
            return recommendations.stream()
                .filter(Objects::nonNull)
                .limit(maxCount)
                .collect(Collectors.toList());
        }
    }
    
    private class SocialBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount) {
            List<Recommendation> recommendations = new ArrayList<>();
            
            if (context.getPlacesVisited() < 2) {
                // User hasn't been social today
                recommendations.add(generateRecommendationForActivity(ActivityType.SOCIAL_ACTIVITY, context));
            } else if (context.getPlacesVisited() > 5) {
                // User has been very social
                recommendations.add(generateRecommendationForActivity(ActivityType.RELAXATION, context));
            }
            
            return recommendations.stream()
                .filter(Objects::nonNull)
                .limit(maxCount)
                .collect(Collectors.toList());
        }
    }
    
    private class MoodBasedStrategy implements RecommendationStrategy {
        @Override
        public List<Recommendation> generateRecommendations(RecommendationContext context, int maxCount) {
            List<Recommendation> recommendations = new ArrayList<>();
            
            // Infer mood from activity patterns
            if (context.getCurrentActivityScore() < 30) {
                // Low activity suggests need for motivation
                recommendations.add(generateRecommendationForActivity(ActivityType.OUTDOOR_EXERCISE, context));
                recommendations.add(generateRecommendationForActivity(ActivityType.SOCIAL_ACTIVITY, context));
            } else if (context.getCurrentActivityScore() > 80) {
                // High activity suggests need for balance
                recommendations.add(generateRecommendationForActivity(ActivityType.RELAXATION, context));
            }
            
            return recommendations.stream()
                .filter(Objects::nonNull)
                .limit(maxCount)
                .collect(Collectors.toList());
        }
    }
}