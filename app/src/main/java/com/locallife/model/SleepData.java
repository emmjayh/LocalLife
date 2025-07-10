package com.locallife.model;

import java.util.Date;

/**
 * Model for sleep quality data
 */
public class SleepData {
    private int id;
    private String date;
    private Date bedTime;
    private Date sleepTime;
    private Date wakeTime;
    private int totalSleepMinutes;
    private int deepSleepMinutes;
    private int remSleepMinutes;
    private int lightSleepMinutes;
    private int awakeDuringNight;
    private float sleepQualityScore;
    private float restfulnessScore;
    private String sleepMood;
    private String sleepNotes;
    private boolean usedSleepAid;
    private float roomTemperature;
    private float roomHumidity;
    private int noiseLevel;
    private int lightLevel;
    private Date createdAt;
    private Date updatedAt;
    
    public SleepData() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    public SleepData(String date, Date bedTime, Date sleepTime, Date wakeTime) {
        this();
        this.date = date;
        this.bedTime = bedTime;
        this.sleepTime = sleepTime;
        this.wakeTime = wakeTime;
        calculateSleepDuration();
    }
    
    private void calculateSleepDuration() {
        if (sleepTime != null && wakeTime != null) {
            long sleepDuration = wakeTime.getTime() - sleepTime.getTime();
            totalSleepMinutes = (int) (sleepDuration / (1000 * 60));
        }
    }
    
    public void calculateSleepQualityScore() {
        float score = 0f;
        
        // Base score from sleep duration (7-9 hours is optimal)
        float hours = totalSleepMinutes / 60f;
        if (hours >= 7f && hours <= 9f) {
            score += 40f;
        } else if (hours >= 6f && hours <= 10f) {
            score += 30f;
        } else {
            score += 20f;
        }
        
        // Deep sleep percentage (20-25% is optimal)
        if (totalSleepMinutes > 0) {
            float deepSleepPercentage = (deepSleepMinutes / (float) totalSleepMinutes) * 100f;
            if (deepSleepPercentage >= 20f && deepSleepPercentage <= 25f) {
                score += 30f;
            } else if (deepSleepPercentage >= 15f && deepSleepPercentage <= 30f) {
                score += 20f;
            } else {
                score += 10f;
            }
        }
        
        // Awake during night penalty
        score -= Math.min(20f, awakeDuringNight * 5f);
        
        // Environmental factors
        if (roomTemperature >= 18f && roomTemperature <= 22f) {
            score += 10f;
        }
        if (noiseLevel < 30) {
            score += 10f;
        }
        if (lightLevel < 10) {
            score += 10f;
        }
        
        sleepQualityScore = Math.max(0f, Math.min(100f, score));
    }
    
    public String getSleepQualityCategory() {
        if (sleepQualityScore >= 85f) return "Excellent";
        if (sleepQualityScore >= 70f) return "Good";
        if (sleepQualityScore >= 50f) return "Fair";
        return "Poor";
    }
    
    public int getSleepEfficiency() {
        if (bedTime != null && wakeTime != null) {
            long timeInBed = wakeTime.getTime() - bedTime.getTime();
            int timeInBedMinutes = (int) (timeInBed / (1000 * 60));
            if (timeInBedMinutes > 0) {
                return (int) ((totalSleepMinutes / (float) timeInBedMinutes) * 100f);
            }
        }
        return 0;
    }
    
    public String getFormattedSleepDuration() {
        if (totalSleepMinutes == 0) return "0h 0m";
        int hours = totalSleepMinutes / 60;
        int minutes = totalSleepMinutes % 60;
        return String.format("%dh %dm", hours, minutes);
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public Date getBedTime() { return bedTime; }
    public void setBedTime(Date bedTime) { 
        this.bedTime = bedTime;
        calculateSleepDuration();
    }
    
    public Date getSleepTime() { return sleepTime; }
    public void setSleepTime(Date sleepTime) { 
        this.sleepTime = sleepTime;
        calculateSleepDuration();
    }
    
    public Date getWakeTime() { return wakeTime; }
    public void setWakeTime(Date wakeTime) { 
        this.wakeTime = wakeTime;
        calculateSleepDuration();
    }
    
    public int getTotalSleepMinutes() { return totalSleepMinutes; }
    public void setTotalSleepMinutes(int totalSleepMinutes) { this.totalSleepMinutes = totalSleepMinutes; }
    
    public int getDeepSleepMinutes() { return deepSleepMinutes; }
    public void setDeepSleepMinutes(int deepSleepMinutes) { this.deepSleepMinutes = deepSleepMinutes; }
    
    public int getRemSleepMinutes() { return remSleepMinutes; }
    public void setRemSleepMinutes(int remSleepMinutes) { this.remSleepMinutes = remSleepMinutes; }
    
    public int getLightSleepMinutes() { return lightSleepMinutes; }
    public void setLightSleepMinutes(int lightSleepMinutes) { this.lightSleepMinutes = lightSleepMinutes; }
    
    public int getAwakeDuringNight() { return awakeDuringNight; }
    public void setAwakeDuringNight(int awakeDuringNight) { this.awakeDuringNight = awakeDuringNight; }
    
    public float getSleepQualityScore() { return sleepQualityScore; }
    public void setSleepQualityScore(float sleepQualityScore) { this.sleepQualityScore = sleepQualityScore; }
    
    public float getRestfulnessScore() { return restfulnessScore; }
    public void setRestfulnessScore(float restfulnessScore) { this.restfulnessScore = restfulnessScore; }
    
    public String getSleepMood() { return sleepMood; }
    public void setSleepMood(String sleepMood) { this.sleepMood = sleepMood; }
    
    public String getSleepNotes() { return sleepNotes; }
    public void setSleepNotes(String sleepNotes) { this.sleepNotes = sleepNotes; }
    
    public boolean isUsedSleepAid() { return usedSleepAid; }
    public void setUsedSleepAid(boolean usedSleepAid) { this.usedSleepAid = usedSleepAid; }
    
    public float getRoomTemperature() { return roomTemperature; }
    public void setRoomTemperature(float roomTemperature) { this.roomTemperature = roomTemperature; }
    
    public float getRoomHumidity() { return roomHumidity; }
    public void setRoomHumidity(float roomHumidity) { this.roomHumidity = roomHumidity; }
    
    public int getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(int noiseLevel) { this.noiseLevel = noiseLevel; }
    
    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int lightLevel) { this.lightLevel = lightLevel; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}