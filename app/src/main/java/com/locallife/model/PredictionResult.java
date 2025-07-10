package com.locallife.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of activity prediction with confidence metrics and analysis
 */
public class PredictionResult {
    private String predictionId;
    private Date predictionTime;
    private Date targetTime;
    private ActivityType predictedActivity;
    private double confidenceScore;
    private String predictionMethod; // "ML", "CORRELATION", "RULE_BASED", "HYBRID"
    private List<ActivityType> alternativeActivities;
    private Map<String, Double> featureImportance;
    private WeatherContext weatherContext;
    private UserContext userContext;
    private String reasoning;
    private boolean isValidated;
    private Date validationTime;
    private ActivityType actualActivity;
    private double predictionAccuracy;
    private String errorAnalysis;
    
    public PredictionResult() {
        this.predictionId = generatePredictionId();
        this.predictionTime = new Date();
        this.isValidated = false;
        this.predictionAccuracy = -1.0;
    }
    
    private String generatePredictionId() {
        return "pred_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * Inner class for weather context at prediction time
     */
    public static class WeatherContext {
        private float temperature;
        private float humidity;
        private String weatherCondition;
        private float windSpeed;
        private double uvIndex;
        private int airQualityIndex;
        private String moonPhase;
        private long dayLengthMinutes;
        private boolean isWeekend;
        private String timeOfDay;
        
        public WeatherContext(float temperature, float humidity, String weatherCondition, 
                            float windSpeed, double uvIndex, int airQualityIndex, 
                            String moonPhase, long dayLengthMinutes, boolean isWeekend, String timeOfDay) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.weatherCondition = weatherCondition;
            this.windSpeed = windSpeed;
            this.uvIndex = uvIndex;
            this.airQualityIndex = airQualityIndex;
            this.moonPhase = moonPhase;
            this.dayLengthMinutes = dayLengthMinutes;
            this.isWeekend = isWeekend;
            this.timeOfDay = timeOfDay;
        }
        
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
        
        public String getMoonPhase() { return moonPhase; }
        public void setMoonPhase(String moonPhase) { this.moonPhase = moonPhase; }
        
        public long getDayLengthMinutes() { return dayLengthMinutes; }
        public void setDayLengthMinutes(long dayLengthMinutes) { this.dayLengthMinutes = dayLengthMinutes; }
        
        public boolean isWeekend() { return isWeekend; }
        public void setWeekend(boolean weekend) { isWeekend = weekend; }
        
        public String getTimeOfDay() { return timeOfDay; }
        public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }
    }
    
    /**
     * Inner class for user context at prediction time
     */
    public static class UserContext {
        private int recentStepCount;
        private float recentActivityScore;
        private String primaryLocation;
        private int screenTimeMinutes;
        private List<String> recentActivities;
        private String userMood; // if available
        private int socialInteractions;
        private String workSchedule;
        private List<String> preferences;
        
        public UserContext(int recentStepCount, float recentActivityScore, String primaryLocation, 
                         int screenTimeMinutes, List<String> recentActivities, String userMood, 
                         int socialInteractions, String workSchedule, List<String> preferences) {
            this.recentStepCount = recentStepCount;
            this.recentActivityScore = recentActivityScore;
            this.primaryLocation = primaryLocation;
            this.screenTimeMinutes = screenTimeMinutes;
            this.recentActivities = recentActivities;
            this.userMood = userMood;
            this.socialInteractions = socialInteractions;
            this.workSchedule = workSchedule;
            this.preferences = preferences;
        }
        
        // Getters and setters
        public int getRecentStepCount() { return recentStepCount; }
        public void setRecentStepCount(int recentStepCount) { this.recentStepCount = recentStepCount; }
        
        public float getRecentActivityScore() { return recentActivityScore; }
        public void setRecentActivityScore(float recentActivityScore) { this.recentActivityScore = recentActivityScore; }
        
        public String getPrimaryLocation() { return primaryLocation; }
        public void setPrimaryLocation(String primaryLocation) { this.primaryLocation = primaryLocation; }
        
        public int getScreenTimeMinutes() { return screenTimeMinutes; }
        public void setScreenTimeMinutes(int screenTimeMinutes) { this.screenTimeMinutes = screenTimeMinutes; }
        
        public List<String> getRecentActivities() { return recentActivities; }
        public void setRecentActivities(List<String> recentActivities) { this.recentActivities = recentActivities; }
        
        public String getUserMood() { return userMood; }
        public void setUserMood(String userMood) { this.userMood = userMood; }
        
        public int getSocialInteractions() { return socialInteractions; }
        public void setSocialInteractions(int socialInteractions) { this.socialInteractions = socialInteractions; }
        
        public String getWorkSchedule() { return workSchedule; }
        public void setWorkSchedule(String workSchedule) { this.workSchedule = workSchedule; }
        
        public List<String> getPreferences() { return preferences; }
        public void setPreferences(List<String> preferences) { this.preferences = preferences; }
    }
    
    /**
     * Validate the prediction against actual activity
     */
    public void validate(ActivityType actualActivity) {
        this.actualActivity = actualActivity;
        this.isValidated = true;
        this.validationTime = new Date();
        
        // Calculate accuracy
        if (actualActivity == predictedActivity) {
            this.predictionAccuracy = 1.0;
        } else {
            // Check if actual activity is in alternatives
            if (alternativeActivities != null && alternativeActivities.contains(actualActivity)) {
                this.predictionAccuracy = 0.5;
            } else {
                this.predictionAccuracy = 0.0;
            }
        }
        
        // Generate error analysis
        if (predictionAccuracy < 1.0) {
            generateErrorAnalysis();
        }
    }
    
    private void generateErrorAnalysis() {
        StringBuilder analysis = new StringBuilder();
        
        if (actualActivity == null) {
            analysis.append("No actual activity recorded. ");
        } else if (predictedActivity != actualActivity) {
            analysis.append("Predicted ").append(predictedActivity.getDisplayName())
                    .append(" but actual was ").append(actualActivity.getDisplayName()).append(". ");
            
            // Analyze potential reasons
            if (weatherContext != null) {
                // Check if weather conditions changed unexpectedly
                analysis.append("Weather conditions may have influenced the actual choice. ");
            }
            
            if (userContext != null) {
                // Check if user context was different than expected
                analysis.append("User context factors may have been different than predicted. ");
            }
            
            if (confidenceScore < 0.6) {
                analysis.append("Low confidence score indicated uncertain prediction. ");
            }
        }
        
        this.errorAnalysis = analysis.toString();
    }
    
    /**
     * Get prediction quality assessment
     */
    public String getPredictionQuality() {
        if (!isValidated) return "UNVALIDATED";
        
        if (predictionAccuracy >= 0.8) return "EXCELLENT";
        if (predictionAccuracy >= 0.6) return "GOOD";
        if (predictionAccuracy >= 0.4) return "FAIR";
        return "POOR";
    }
    
    /**
     * Check if prediction is outdated
     */
    public boolean isOutdated() {
        if (targetTime == null) return false;
        
        long currentTime = new Date().getTime();
        return currentTime > targetTime.getTime();
    }
    
    /**
     * Get time until prediction target
     */
    public long getTimeUntilTarget() {
        if (targetTime == null) return -1;
        
        long currentTime = new Date().getTime();
        return targetTime.getTime() - currentTime;
    }
    
    // Getters and setters
    public String getPredictionId() { return predictionId; }
    public void setPredictionId(String predictionId) { this.predictionId = predictionId; }
    
    public Date getPredictionTime() { return predictionTime; }
    public void setPredictionTime(Date predictionTime) { this.predictionTime = predictionTime; }
    
    public Date getTargetTime() { return targetTime; }
    public void setTargetTime(Date targetTime) { this.targetTime = targetTime; }
    
    public ActivityType getPredictedActivity() { return predictedActivity; }
    public void setPredictedActivity(ActivityType predictedActivity) { this.predictedActivity = predictedActivity; }
    
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public String getPredictionMethod() { return predictionMethod; }
    public void setPredictionMethod(String predictionMethod) { this.predictionMethod = predictionMethod; }
    
    public List<ActivityType> getAlternativeActivities() { return alternativeActivities; }
    public void setAlternativeActivities(List<ActivityType> alternativeActivities) { this.alternativeActivities = alternativeActivities; }
    
    public Map<String, Double> getFeatureImportance() { return featureImportance; }
    public void setFeatureImportance(Map<String, Double> featureImportance) { this.featureImportance = featureImportance; }
    
    public WeatherContext getWeatherContext() { return weatherContext; }
    public void setWeatherContext(WeatherContext weatherContext) { this.weatherContext = weatherContext; }
    
    public UserContext getUserContext() { return userContext; }
    public void setUserContext(UserContext userContext) { this.userContext = userContext; }
    
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    
    public boolean isValidated() { return isValidated; }
    public void setValidated(boolean validated) { isValidated = validated; }
    
    public Date getValidationTime() { return validationTime; }
    public void setValidationTime(Date validationTime) { this.validationTime = validationTime; }
    
    public ActivityType getActualActivity() { return actualActivity; }
    public void setActualActivity(ActivityType actualActivity) { this.actualActivity = actualActivity; }
    
    public double getPredictionAccuracy() { return predictionAccuracy; }
    public void setPredictionAccuracy(double predictionAccuracy) { this.predictionAccuracy = predictionAccuracy; }
    
    public String getErrorAnalysis() { return errorAnalysis; }
    public void setErrorAnalysis(String errorAnalysis) { this.errorAnalysis = errorAnalysis; }
}