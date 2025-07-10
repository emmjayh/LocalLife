package com.locallife.model;

import java.util.Date;

/**
 * Model for tracking user mood entries with detailed metadata
 */
public class MoodEntry {
    private int id;
    private String date;
    private MoodLevel moodLevel;
    private String moodEmoji;
    private String notes;
    private String triggers;
    private String activities;
    private MoodCategory category;
    private int energyLevel; // 1-10 scale
    private int stressLevel; // 1-10 scale
    private int socialLevel; // 1-10 scale
    private String weatherCondition;
    private float temperature;
    private String location;
    private int sleepHours;
    private boolean hasExercised;
    private String medications;
    private Date createdAt;
    private Date updatedAt;
    
    public MoodEntry() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.energyLevel = 5;
        this.stressLevel = 5;
        this.socialLevel = 5;
    }
    
    public MoodEntry(String date, MoodLevel moodLevel, String moodEmoji) {
        this();
        this.date = date;
        this.moodLevel = moodLevel;
        this.moodEmoji = moodEmoji;
        this.category = determineMoodCategory(moodLevel);
    }
    
    /**
     * Determine mood category based on mood level
     */
    private MoodCategory determineMoodCategory(MoodLevel level) {
        switch (level) {
            case TERRIBLE:
            case VERY_BAD:
                return MoodCategory.NEGATIVE;
            case BAD:
            case POOR:
                return MoodCategory.LOW;
            case NEUTRAL:
                return MoodCategory.NEUTRAL;
            case GOOD:
            case VERY_GOOD:
                return MoodCategory.POSITIVE;
            case EXCELLENT:
            case AMAZING:
                return MoodCategory.HIGH;
            default:
                return MoodCategory.NEUTRAL;
        }
    }
    
    /**
     * Get mood score (1-10 scale)
     */
    public int getMoodScore() {
        return moodLevel.getScore();
    }
    
    /**
     * Get overall wellness score combining mood, energy, and stress
     */
    public float getWellnessScore() {
        float moodScore = getMoodScore();
        float energyScore = energyLevel;
        float stressScore = 11 - stressLevel; // Invert stress (lower stress = better)
        
        return (moodScore + energyScore + stressScore) / 3.0f;
    }
    
    /**
     * Get formatted mood display text
     */
    public String getFormattedMood() {
        return moodEmoji + " " + moodLevel.getDisplayName();
    }
    
    /**
     * Get mood color for UI display
     */
    public String getMoodColor() {
        switch (moodLevel) {
            case TERRIBLE:
            case VERY_BAD:
                return "#F44336"; // Red
            case BAD:
                return "#FF5722"; // Deep Orange
            case POOR:
                return "#FF9800"; // Orange
            case NEUTRAL:
                return "#9E9E9E"; // Gray
            case GOOD:
                return "#8BC34A"; // Light Green
            case VERY_GOOD:
                return "#4CAF50"; // Green
            case EXCELLENT:
                return "#2196F3"; // Blue
            case AMAZING:
                return "#9C27B0"; // Purple
            default:
                return "#9E9E9E";
        }
    }
    
    /**
     * Check if this is a positive mood entry
     */
    public boolean isPositiveMood() {
        return getMoodScore() >= 6;
    }
    
    /**
     * Check if this is a negative mood entry
     */
    public boolean isNegativeMood() {
        return getMoodScore() <= 4;
    }
    
    /**
     * Get formatted summary text
     */
    public String getSummaryText() {
        StringBuilder summary = new StringBuilder();
        summary.append(getFormattedMood());
        
        if (notes != null && !notes.isEmpty()) {
            summary.append(" - ").append(notes);
        }
        
        return summary.toString();
    }
    
    /**
     * Get detailed context for analysis
     */
    public String getContextDetails() {
        StringBuilder context = new StringBuilder();
        
        context.append("Energy: ").append(energyLevel).append("/10");
        context.append(", Stress: ").append(stressLevel).append("/10");
        context.append(", Social: ").append(socialLevel).append("/10");
        
        if (weatherCondition != null) {
            context.append(", Weather: ").append(weatherCondition);
        }
        
        if (sleepHours > 0) {
            context.append(", Sleep: ").append(sleepHours).append("h");
        }
        
        return context.toString();
    }
    
    // Enums
    public enum MoodLevel {
        TERRIBLE(1, "Terrible", "😭"),
        VERY_BAD(2, "Very Bad", "😢"),
        BAD(3, "Bad", "😞"),
        POOR(4, "Poor", "🙁"),
        NEUTRAL(5, "Neutral", "😐"),
        GOOD(6, "Good", "🙂"),
        VERY_GOOD(7, "Very Good", "😊"),
        EXCELLENT(8, "Excellent", "😄"),
        AMAZING(9, "Amazing", "🤩");
        
        private final int score;
        private final String displayName;
        private final String emoji;
        
        MoodLevel(int score, String displayName, String emoji) {
            this.score = score;
            this.displayName = displayName;
            this.emoji = emoji;
        }
        
        public int getScore() { return score; }
        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        
        public static MoodLevel fromScore(int score) {
            for (MoodLevel level : values()) {
                if (level.score == score) {
                    return level;
                }
            }
            return NEUTRAL;
        }
    }
    
    public enum MoodCategory {
        NEGATIVE("Negative", "#F44336"),
        LOW("Low", "#FF9800"),
        NEUTRAL("Neutral", "#9E9E9E"),
        POSITIVE("Positive", "#4CAF50"),
        HIGH("High", "#2196F3");
        
        private final String displayName;
        private final String color;
        
        MoodCategory(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public MoodLevel getMoodLevel() { return moodLevel; }
    public void setMoodLevel(MoodLevel moodLevel) { 
        this.moodLevel = moodLevel;
        this.category = determineMoodCategory(moodLevel);
    }
    
    public String getMoodEmoji() { return moodEmoji; }
    public void setMoodEmoji(String moodEmoji) { this.moodEmoji = moodEmoji; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getTriggers() { return triggers; }
    public void setTriggers(String triggers) { this.triggers = triggers; }
    
    public String getActivities() { return activities; }
    public void setActivities(String activities) { this.activities = activities; }
    
    public MoodCategory getCategory() { return category; }
    public void setCategory(MoodCategory category) { this.category = category; }
    
    public int getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(int energyLevel) { this.energyLevel = energyLevel; }
    
    public int getStressLevel() { return stressLevel; }
    public void setStressLevel(int stressLevel) { this.stressLevel = stressLevel; }
    
    public int getSocialLevel() { return socialLevel; }
    public void setSocialLevel(int socialLevel) { this.socialLevel = socialLevel; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public int getSleepHours() { return sleepHours; }
    public void setSleepHours(int sleepHours) { this.sleepHours = sleepHours; }
    
    public boolean isHasExercised() { return hasExercised; }
    public void setHasExercised(boolean hasExercised) { this.hasExercised = hasExercised; }
    
    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}