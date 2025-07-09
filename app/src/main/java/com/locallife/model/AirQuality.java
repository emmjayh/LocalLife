package com.locallife.model;

import java.util.Date;

/**
 * Air quality data model containing AQI and pollutant levels
 */
public class AirQuality {
    private long id;
    private String date;
    private Date timestamp;
    private double latitude;
    private double longitude;
    private String location;
    private int airQualityIndex;
    private String airQualityLevel;
    private float pm25Level;
    private float pm10Level;
    private float no2Level;
    private float o3Level;
    private float coLevel;
    private float airQualityImpact;
    
    public AirQuality() {
        this.timestamp = new Date();
    }
    
    public AirQuality(String date, double latitude, double longitude, int airQualityIndex, 
                     String airQualityLevel, float pm25Level, float pm10Level) {
        this();
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.airQualityIndex = airQualityIndex;
        this.airQualityLevel = airQualityLevel;
        this.pm25Level = pm25Level;
        this.pm10Level = pm10Level;
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
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public int getAirQualityIndex() { return airQualityIndex; }
    public void setAirQualityIndex(int airQualityIndex) { this.airQualityIndex = airQualityIndex; }
    
    public String getAirQualityLevel() { return airQualityLevel; }
    public void setAirQualityLevel(String airQualityLevel) { this.airQualityLevel = airQualityLevel; }
    
    public float getPm25Level() { return pm25Level; }
    public void setPm25Level(float pm25Level) { this.pm25Level = pm25Level; }
    
    public float getPm10Level() { return pm10Level; }
    public void setPm10Level(float pm10Level) { this.pm10Level = pm10Level; }
    
    public float getNo2Level() { return no2Level; }
    public void setNo2Level(float no2Level) { this.no2Level = no2Level; }
    
    public float getO3Level() { return o3Level; }
    public void setO3Level(float o3Level) { this.o3Level = o3Level; }
    
    public float getCoLevel() { return coLevel; }
    public void setCoLevel(float coLevel) { this.coLevel = coLevel; }
    
    public float getAirQualityImpact() { return airQualityImpact; }
    public void setAirQualityImpact(float airQualityImpact) { this.airQualityImpact = airQualityImpact; }
    
    /**
     * Get air quality health category based on AQI
     */
    public String getHealthCategory() {
        if (airQualityIndex <= 50) return "Good";
        if (airQualityIndex <= 100) return "Moderate";
        if (airQualityIndex <= 150) return "Unhealthy for Sensitive Groups";
        if (airQualityIndex <= 200) return "Unhealthy";
        if (airQualityIndex <= 300) return "Very Unhealthy";
        return "Hazardous";
    }
    
    /**
     * Get color recommendation based on air quality
     */
    public String getColorRecommendation() {
        if (airQualityIndex <= 50) return "#00E400"; // Green
        if (airQualityIndex <= 100) return "#FFFF00"; // Yellow
        if (airQualityIndex <= 150) return "#FF7E00"; // Orange
        if (airQualityIndex <= 200) return "#FF0000"; // Red
        if (airQualityIndex <= 300) return "#8F3F97"; // Purple
        return "#7E0023"; // Maroon
    }
    
    /**
     * Get activity recommendations based on air quality
     */
    public String getActivityRecommendation() {
        if (airQualityIndex <= 50) {
            return "Air quality is good. Great for outdoor activities!";
        } else if (airQualityIndex <= 100) {
            return "Air quality is moderate. Sensitive individuals should limit prolonged outdoor exertion.";
        } else if (airQualityIndex <= 150) {
            return "Unhealthy for sensitive groups. Reduce outdoor activities if you have respiratory conditions.";
        } else if (airQualityIndex <= 200) {
            return "Unhealthy air quality. Avoid outdoor activities and wear a mask if you must go outside.";
        } else if (airQualityIndex <= 300) {
            return "Very unhealthy air quality. Stay indoors and avoid all outdoor activities.";
        } else {
            return "Hazardous air quality. Emergency conditions - stay indoors with air purifiers.";
        }
    }
}