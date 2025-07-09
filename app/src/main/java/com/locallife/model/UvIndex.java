package com.locallife.model;

import java.util.Date;

/**
 * UV index data model containing UV levels and safety information
 */
public class UvIndex {
    private long id;
    private String date;
    private Date timestamp;
    private double latitude;
    private double longitude;
    private float uvIndex;
    private float uvMax;
    private String uvCategory;
    private int burnTime;
    private int tanTime;
    private String peakUvTime;
    private float vitaminDTime;
    private float uvActivityImpact;
    
    public UvIndex() {
        this.timestamp = new Date();
    }
    
    public UvIndex(String date, double latitude, double longitude, float uvIndex, String uvCategory) {
        this();
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uvIndex = uvIndex;
        this.uvCategory = uvCategory;
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
    
    public float getUvIndex() { return uvIndex; }
    public void setUvIndex(float uvIndex) { this.uvIndex = uvIndex; }
    
    public float getUvMax() { return uvMax; }
    public void setUvMax(float uvMax) { this.uvMax = uvMax; }
    
    public String getUvCategory() { return uvCategory; }
    public void setUvCategory(String uvCategory) { this.uvCategory = uvCategory; }
    
    public int getBurnTime() { return burnTime; }
    public void setBurnTime(int burnTime) { this.burnTime = burnTime; }
    
    public int getTanTime() { return tanTime; }
    public void setTanTime(int tanTime) { this.tanTime = tanTime; }
    
    public String getPeakUvTime() { return peakUvTime; }
    public void setPeakUvTime(String peakUvTime) { this.peakUvTime = peakUvTime; }
    
    public float getVitaminDTime() { return vitaminDTime; }
    public void setVitaminDTime(float vitaminDTime) { this.vitaminDTime = vitaminDTime; }
    
    public float getUvActivityImpact() { return uvActivityImpact; }
    public void setUvActivityImpact(float uvActivityImpact) { this.uvActivityImpact = uvActivityImpact; }
    
    /**
     * Get UV category based on index
     */
    public String getUvCategoryFromIndex() {
        if (uvIndex <= 2) return "Low";
        if (uvIndex <= 5) return "Moderate";
        if (uvIndex <= 7) return "High";
        if (uvIndex <= 10) return "Very High";
        return "Extreme";
    }
    
    /**
     * Get color recommendation based on UV index
     */
    public String getColorRecommendation() {
        if (uvIndex <= 2) return "#289500"; // Green
        if (uvIndex <= 5) return "#F7E400"; // Yellow
        if (uvIndex <= 7) return "#F85900"; // Orange
        if (uvIndex <= 10) return "#D8001D"; // Red
        return "#6B49C8"; // Violet
    }
    
    /**
     * Get protection recommendations based on UV index
     */
    public String getProtectionRecommendation() {
        if (uvIndex <= 2) {
            return "Minimal protection needed. Safe for most outdoor activities.";
        } else if (uvIndex <= 5) {
            return "Moderate protection needed. Use sunscreen and wear a hat.";
        } else if (uvIndex <= 7) {
            return "High protection needed. Sunscreen, hat, and sunglasses essential.";
        } else if (uvIndex <= 10) {
            return "Very high protection needed. Seek shade during peak hours.";
        } else {
            return "Extreme protection needed. Avoid outdoor activities during peak hours.";
        }
    }
    
    /**
     * Calculate estimated burn time for different skin types
     */
    public int calculateBurnTime(int skinType) {
        // Base burn time in minutes for skin type I (very fair)
        int baseBurnTime = 10;
        
        // Multiply by skin type factor
        switch (skinType) {
            case 1: baseBurnTime = 10; break;  // Very fair
            case 2: baseBurnTime = 15; break;  // Fair
            case 3: baseBurnTime = 20; break;  // Light
            case 4: baseBurnTime = 30; break;  // Medium
            case 5: baseBurnTime = 45; break;  // Dark
            case 6: baseBurnTime = 60; break;  // Very dark
            default: baseBurnTime = 20;
        }
        
        return (int) (baseBurnTime / Math.max(uvIndex, 1));
    }
    
    /**
     * Get optimal vitamin D exposure time
     */
    public float getOptimalVitaminDTime(int skinType) {
        // Base time for vitamin D synthesis (in minutes)
        float baseTime = 15;
        
        // Adjust for skin type
        switch (skinType) {
            case 1: baseTime = 10; break;  // Very fair - shorter time
            case 2: baseTime = 12; break;  // Fair
            case 3: baseTime = 15; break;  // Light
            case 4: baseTime = 20; break;  // Medium
            case 5: baseTime = 25; break;  // Dark
            case 6: baseTime = 30; break;  // Very dark - longer time
            default: baseTime = 15;
        }
        
        return baseTime / Math.max(uvIndex, 1);
    }
    
    /**
     * Get activity recommendations based on UV index
     */
    public String getActivityRecommendation() {
        if (uvIndex <= 2) {
            return "Great for all outdoor activities. No special precautions needed.";
        } else if (uvIndex <= 5) {
            return "Good for outdoor activities. Apply sunscreen and wear protective clothing.";
        } else if (uvIndex <= 7) {
            return "Limit outdoor activities during peak hours (10am-4pm). Use protection.";
        } else if (uvIndex <= 10) {
            return "Avoid outdoor activities during peak hours. Seek shade and use maximum protection.";
        } else {
            return "Stay indoors during peak hours. If outdoors, use extreme protection measures.";
        }
    }
}