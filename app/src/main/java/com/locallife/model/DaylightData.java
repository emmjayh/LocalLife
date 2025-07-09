package com.locallife.model;

import java.util.Date;

/**
 * Daylight data model containing sunrise/sunset and circadian information
 */
public class DaylightData {
    private long id;
    private String date;
    private Date timestamp;
    private double latitude;
    private double longitude;
    private String sunriseTime;
    private String sunsetTime;
    private String solarNoonTime;
    private int dayLength;
    private int nightLength;
    private String season;
    private float daylightChange;
    private float circadianScore;
    
    public DaylightData() {
        this.timestamp = new Date();
    }
    
    public DaylightData(String date, double latitude, double longitude, String sunriseTime, 
                       String sunsetTime, int dayLength) {
        this();
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sunriseTime = sunriseTime;
        this.sunsetTime = sunsetTime;
        this.dayLength = dayLength;
        this.nightLength = 1440 - dayLength; // 1440 minutes in a day
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getSunriseTime() { return sunriseTime; }
    public void setSunriseTime(String sunriseTime) { this.sunriseTime = sunriseTime; }
    
    public String getSunsetTime() { return sunsetTime; }
    public void setSunsetTime(String sunsetTime) { this.sunsetTime = sunsetTime; }
    
    public String getSolarNoonTime() { return solarNoonTime; }
    public void setSolarNoonTime(String solarNoonTime) { this.solarNoonTime = solarNoonTime; }
    
    public int getDayLength() { return dayLength; }
    public void setDayLength(int dayLength) { this.dayLength = dayLength; }
    
    public int getNightLength() { return nightLength; }
    public void setNightLength(int nightLength) { this.nightLength = nightLength; }
    
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public float getDaylightChange() { return daylightChange; }
    public void setDaylightChange(float daylightChange) { this.daylightChange = daylightChange; }
    
    public float getCircadianScore() { return circadianScore; }
    public void setCircadianScore(float circadianScore) { this.circadianScore = circadianScore; }
    
    /**
     * Get formatted day length as hours and minutes
     */
    public String getFormattedDayLength() {
        int hours = dayLength / 60;
        int minutes = dayLength % 60;
        return String.format("%d hours, %d minutes", hours, minutes);
    }
    
    /**
     * Get formatted night length as hours and minutes
     */
    public String getFormattedNightLength() {
        int hours = nightLength / 60;
        int minutes = nightLength % 60;
        return String.format("%d hours, %d minutes", hours, minutes);
    }
    
    /**
     * Calculate season from date and latitude
     */
    public static String calculateSeason(String date, double latitude) {
        // Simple season calculation based on month
        String[] dateParts = date.split("-");
        if (dateParts.length < 2) return "Unknown";
        
        int month = Integer.parseInt(dateParts[1]);
        boolean isNorthernHemisphere = latitude > 0;
        
        if (isNorthernHemisphere) {
            if (month >= 3 && month <= 5) return "Spring";
            if (month >= 6 && month <= 8) return "Summer";
            if (month >= 9 && month <= 11) return "Autumn";
            return "Winter";
        } else {
            if (month >= 3 && month <= 5) return "Autumn";
            if (month >= 6 && month <= 8) return "Winter";
            if (month >= 9 && month <= 11) return "Spring";
            return "Summer";
        }
    }
    
    /**
     * Get daylight change description
     */
    public String getDaylightChangeDescription() {
        if (daylightChange > 2) {
            return "Days are getting significantly longer";
        } else if (daylightChange > 0.5) {
            return "Days are getting longer";
        } else if (daylightChange > -0.5) {
            return "Daylight is relatively stable";
        } else if (daylightChange > -2) {
            return "Days are getting shorter";
        } else {
            return "Days are getting significantly shorter";
        }
    }
    
    /**
     * Get circadian rhythm recommendations
     */
    public String getCircadianRecommendation() {
        if (circadianScore >= 0.8) {
            return "Excellent circadian rhythm alignment. Great for productivity and sleep.";
        } else if (circadianScore >= 0.6) {
            return "Good circadian rhythm. Consider morning sunlight exposure.";
        } else if (circadianScore >= 0.4) {
            return "Moderate circadian rhythm. Try to maintain consistent sleep schedule.";
        } else if (circadianScore >= 0.2) {
            return "Poor circadian rhythm. Limit evening screen time and get morning light.";
        } else {
            return "Very poor circadian rhythm. Consider consulting a sleep specialist.";
        }
    }
    
    /**
     * Get optimal wake time based on sunrise
     */
    public String getOptimalWakeTime() {
        // Parse sunrise time and suggest wake time 30 minutes before
        String[] timeParts = sunriseTime.split(":");
        if (timeParts.length < 2) return "Unknown";
        
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        // Subtract 30 minutes
        minute -= 30;
        if (minute < 0) {
            minute += 60;
            hour -= 1;
        }
        if (hour < 0) hour += 24;
        
        return String.format("%02d:%02d", hour, minute);
    }
    
    /**
     * Get optimal sleep time based on sunset
     */
    public String getOptimalSleepTime() {
        // Parse sunset time and suggest sleep time 2-3 hours after
        String[] timeParts = sunsetTime.split(":");
        if (timeParts.length < 2) return "Unknown";
        
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        // Add 2.5 hours
        minute += 30;
        hour += 2;
        if (minute >= 60) {
            minute -= 60;
            hour += 1;
        }
        if (hour >= 24) hour -= 24;
        
        return String.format("%02d:%02d", hour, minute);
    }
    
    /**
     * Get activity recommendations based on daylight
     */
    public String getActivityRecommendation() {
        if (dayLength > 720) { // More than 12 hours
            return "Long daylight hours - great for outdoor activities and vitamin D synthesis.";
        } else if (dayLength > 480) { // 8-12 hours
            return "Moderate daylight - good for balanced indoor/outdoor activities.";
        } else { // Less than 8 hours
            return "Short daylight hours - maximize sunlight exposure and consider vitamin D supplements.";
        }
    }
}