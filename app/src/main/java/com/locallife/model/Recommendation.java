package com.locallife.model;

import java.util.Date;
import java.util.List;

/**
 * Represents an activity recommendation with confidence score and reasoning
 */
public class Recommendation {
    private String id;
    private ActivityType activityType;
    private String title;
    private String description;
    private double confidenceScore; // 0.0 to 1.0
    private String reasoning;
    private Date recommendedTime;
    private int durationMinutes;
    private String location;
    private List<String> requirements;
    private List<String> benefits;
    private WeatherSuitability weatherSuitability;
    private UserPersonalizationScore personalizationScore;
    private Date createdAt;
    private boolean isActedUpon;
    private Date actionDate;
    private double actualSatisfaction; // User feedback after completing activity
    
    public Recommendation() {
        this.id = generateId();
        this.createdAt = new Date();
        this.isActedUpon = false;
        this.actualSatisfaction = -1.0; // Not rated yet
    }
    
    public Recommendation(ActivityType activityType, String title, String description, double confidenceScore) {
        this();
        this.activityType = activityType;
        this.title = title;
        this.description = description;
        this.confidenceScore = confidenceScore;
    }
    
    private String generateId() {
        return "rec_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * Inner class for weather suitability details
     */
    public static class WeatherSuitability {
        private double overallScore;
        private double temperatureScore;
        private double weatherConditionScore;
        private double humidityScore;
        private double windScore;
        private double uvScore;
        private String weatherReasoning;
        
        public WeatherSuitability(double overallScore, double temperatureScore, 
                                double weatherConditionScore, double humidityScore, 
                                double windScore, double uvScore, String weatherReasoning) {
            this.overallScore = overallScore;
            this.temperatureScore = temperatureScore;
            this.weatherConditionScore = weatherConditionScore;
            this.humidityScore = humidityScore;
            this.windScore = windScore;
            this.uvScore = uvScore;
            this.weatherReasoning = weatherReasoning;
        }
        
        // Getters and setters
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
        
        public double getTemperatureScore() { return temperatureScore; }
        public void setTemperatureScore(double temperatureScore) { this.temperatureScore = temperatureScore; }
        
        public double getWeatherConditionScore() { return weatherConditionScore; }
        public void setWeatherConditionScore(double weatherConditionScore) { this.weatherConditionScore = weatherConditionScore; }
        
        public double getHumidityScore() { return humidityScore; }
        public void setHumidityScore(double humidityScore) { this.humidityScore = humidityScore; }
        
        public double getWindScore() { return windScore; }
        public void setWindScore(double windScore) { this.windScore = windScore; }
        
        public double getUvScore() { return uvScore; }
        public void setUvScore(double uvScore) { this.uvScore = uvScore; }
        
        public String getWeatherReasoning() { return weatherReasoning; }
        public void setWeatherReasoning(String weatherReasoning) { this.weatherReasoning = weatherReasoning; }
    }
    
    /**
     * Inner class for user personalization scoring
     */
    public static class UserPersonalizationScore {
        private double historicalPreferenceScore;
        private double timeBasedScore;
        private double locationBasedScore;
        private double activityFrequencyScore;
        private double socialContextScore;
        private String personalizationReasoning;
        
        public UserPersonalizationScore(double historicalPreferenceScore, double timeBasedScore, 
                                      double locationBasedScore, double activityFrequencyScore, 
                                      double socialContextScore, String personalizationReasoning) {
            this.historicalPreferenceScore = historicalPreferenceScore;
            this.timeBasedScore = timeBasedScore;
            this.locationBasedScore = locationBasedScore;
            this.activityFrequencyScore = activityFrequencyScore;
            this.socialContextScore = socialContextScore;
            this.personalizationReasoning = personalizationReasoning;
        }
        
        // Getters and setters
        public double getHistoricalPreferenceScore() { return historicalPreferenceScore; }
        public void setHistoricalPreferenceScore(double historicalPreferenceScore) { this.historicalPreferenceScore = historicalPreferenceScore; }
        
        public double getTimeBasedScore() { return timeBasedScore; }
        public void setTimeBasedScore(double timeBasedScore) { this.timeBasedScore = timeBasedScore; }
        
        public double getLocationBasedScore() { return locationBasedScore; }
        public void setLocationBasedScore(double locationBasedScore) { this.locationBasedScore = locationBasedScore; }
        
        public double getActivityFrequencyScore() { return activityFrequencyScore; }
        public void setActivityFrequencyScore(double activityFrequencyScore) { this.activityFrequencyScore = activityFrequencyScore; }
        
        public double getSocialContextScore() { return socialContextScore; }
        public void setSocialContextScore(double socialContextScore) { this.socialContextScore = socialContextScore; }
        
        public String getPersonalizationReasoning() { return personalizationReasoning; }
        public void setPersonalizationReasoning(String personalizationReasoning) { this.personalizationReasoning = personalizationReasoning; }
    }
    
    /**
     * Calculate the overall recommendation score based on weather and personalization
     */
    public double calculateOverallScore() {
        double weatherWeight = 0.4;
        double personalizationWeight = 0.6;
        
        double weatherScore = (weatherSuitability != null) ? weatherSuitability.getOverallScore() : 0.5;
        double personalScore = (personalizationScore != null) ? 
            (personalizationScore.getHistoricalPreferenceScore() * 0.3 + 
             personalizationScore.getTimeBasedScore() * 0.2 + 
             personalizationScore.getLocationBasedScore() * 0.2 + 
             personalizationScore.getActivityFrequencyScore() * 0.15 + 
             personalizationScore.getSocialContextScore() * 0.15) : 0.5;
        
        return (weatherScore * weatherWeight) + (personalScore * personalizationWeight);
    }
    
    /**
     * Get priority level based on confidence score
     */
    public String getPriorityLevel() {
        if (confidenceScore >= 0.8) return "HIGH";
        if (confidenceScore >= 0.6) return "MEDIUM";
        if (confidenceScore >= 0.4) return "LOW";
        return "VERY_LOW";
    }
    
    /**
     * Check if recommendation is time-sensitive
     */
    public boolean isTimeSensitive() {
        if (recommendedTime == null) return false;
        
        long timeDiff = Math.abs(new Date().getTime() - recommendedTime.getTime());
        long oneHour = 60 * 60 * 1000;
        
        return timeDiff <= oneHour;
    }
    
    /**
     * Get recommendation freshness (how recent the recommendation is)
     */
    public String getFreshness() {
        if (createdAt == null) return "UNKNOWN";
        
        long timeDiff = new Date().getTime() - createdAt.getTime();
        long oneHour = 60 * 60 * 1000;
        long oneDay = 24 * oneHour;
        
        if (timeDiff <= oneHour) return "FRESH";
        if (timeDiff <= oneDay) return "RECENT";
        return "STALE";
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    
    public Date getRecommendedTime() { return recommendedTime; }
    public void setRecommendedTime(Date recommendedTime) { this.recommendedTime = recommendedTime; }
    
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }
    
    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }
    
    public WeatherSuitability getWeatherSuitability() { return weatherSuitability; }
    public void setWeatherSuitability(WeatherSuitability weatherSuitability) { this.weatherSuitability = weatherSuitability; }
    
    public UserPersonalizationScore getPersonalizationScore() { return personalizationScore; }
    public void setPersonalizationScore(UserPersonalizationScore personalizationScore) { this.personalizationScore = personalizationScore; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public boolean isActedUpon() { return isActedUpon; }
    public void setActedUpon(boolean actedUpon) { isActedUpon = actedUpon; }
    
    public Date getActionDate() { return actionDate; }
    public void setActionDate(Date actionDate) { this.actionDate = actionDate; }
    
    public double getActualSatisfaction() { return actualSatisfaction; }
    public void setActualSatisfaction(double actualSatisfaction) { this.actualSatisfaction = actualSatisfaction; }
}