package com.locallife.model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Enhanced model for daily data storage
 * Stores all collected sensor and activity data for a single day
 */
public class DayRecord {
    private long id;
    private String date; // Format: YYYY-MM-DD
    private int stepCount;
    private float totalDistance; // in meters
    private int activeMinutes;
    private float activityScore;
    
    // Weather data
    private float temperature;
    private float humidity;
    private String weatherCondition;
    private float windSpeed;
    
    // Location data
    private int placesVisited;
    private String primaryLocation;
    private float totalTravelDistance;
    private List<LocationVisit> locationVisits;
    
    // Screen and battery data
    private int screenTimeMinutes;
    private float batteryUsagePercent;
    private int phoneUnlocks;
    
    // Photo data
    private int photoCount;
    private float photoActivityScore;
    
    // Aggregated scores
    private float physicalActivityScore;
    private float socialActivityScore;
    private float productivityScore;
    private float overallWellbeingScore;
    
    // Timestamps
    private Date createdAt;
    private Date updatedAt;
    
    // Constructor
    public DayRecord() {
        this.locationVisits = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Inner class for location visits
    public static class LocationVisit {
        private String placeName;
        private double latitude;
        private double longitude;
        private Date arrivalTime;
        private Date departureTime;
        private int durationMinutes;
        private String placeCategory; // home, work, restaurant, gym, etc.
        
        public LocationVisit(String placeName, double latitude, double longitude) {
            this.placeName = placeName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.arrivalTime = new Date();
        }
        
        // Getters and setters
        public String getPlaceName() { return placeName; }
        public void setPlaceName(String placeName) { this.placeName = placeName; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public Date getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(Date arrivalTime) { this.arrivalTime = arrivalTime; }
        
        public Date getDepartureTime() { return departureTime; }
        public void setDepartureTime(Date departureTime) { 
            this.departureTime = departureTime;
            if (arrivalTime != null && departureTime != null) {
                this.durationMinutes = (int) ((departureTime.getTime() - arrivalTime.getTime()) / 60000);
            }
        }
        
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        
        public String getPlaceCategory() { return placeCategory; }
        public void setPlaceCategory(String placeCategory) { this.placeCategory = placeCategory; }
    }
    
    // Calculate activity score based on collected data
    public void calculateActivityScore() {
        // Physical activity score (0-100)
        physicalActivityScore = Math.min(100, (stepCount / 100.0f) + (activeMinutes / 3.0f));
        
        // Social activity score based on places visited and photos
        socialActivityScore = Math.min(100, placesVisited * 15 + (locationVisits.size() * 10) + (photoCount * 5));
        
        // Productivity score based on screen time and battery usage
        float screenTimeScore = Math.max(0, 100 - (screenTimeMinutes / 4.8f)); // 8 hours = 0 score
        productivityScore = (screenTimeScore + (100 - batteryUsagePercent)) / 2;
        
        // Overall wellbeing score with photo activity contribution
        overallWellbeingScore = (physicalActivityScore * 0.35f + 
                                socialActivityScore * 0.25f + 
                                productivityScore * 0.25f +
                                photoActivityScore * 0.15f);
        
        // Final activity score
        activityScore = overallWellbeingScore;
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public int getStepCount() { return stepCount; }
    public void setStepCount(int stepCount) { this.stepCount = stepCount; }
    
    public float getTotalDistance() { return totalDistance; }
    public void setTotalDistance(float totalDistance) { this.totalDistance = totalDistance; }
    
    public int getActiveMinutes() { return activeMinutes; }
    public void setActiveMinutes(int activeMinutes) { this.activeMinutes = activeMinutes; }
    
    public float getActivityScore() { return activityScore; }
    public void setActivityScore(float activityScore) { this.activityScore = activityScore; }
    
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    
    public float getHumidity() { return humidity; }
    public void setHumidity(float humidity) { this.humidity = humidity; }
    
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    
    public float getWindSpeed() { return windSpeed; }
    public void setWindSpeed(float windSpeed) { this.windSpeed = windSpeed; }
    
    public int getPlacesVisited() { return placesVisited; }
    public void setPlacesVisited(int placesVisited) { this.placesVisited = placesVisited; }
    
    public String getPrimaryLocation() { return primaryLocation; }
    public void setPrimaryLocation(String primaryLocation) { this.primaryLocation = primaryLocation; }
    
    public float getTotalTravelDistance() { return totalTravelDistance; }
    public void setTotalTravelDistance(float totalTravelDistance) { this.totalTravelDistance = totalTravelDistance; }
    
    public List<LocationVisit> getLocationVisits() { return locationVisits; }
    public void setLocationVisits(List<LocationVisit> locationVisits) { this.locationVisits = locationVisits; }
    
    public int getScreenTimeMinutes() { return screenTimeMinutes; }
    public void setScreenTimeMinutes(int screenTimeMinutes) { this.screenTimeMinutes = screenTimeMinutes; }
    
    public float getBatteryUsagePercent() { return batteryUsagePercent; }
    public void setBatteryUsagePercent(float batteryUsagePercent) { this.batteryUsagePercent = batteryUsagePercent; }
    
    public int getPhoneUnlocks() { return phoneUnlocks; }
    public void setPhoneUnlocks(int phoneUnlocks) { this.phoneUnlocks = phoneUnlocks; }
    
    public float getPhysicalActivityScore() { return physicalActivityScore; }
    public void setPhysicalActivityScore(float physicalActivityScore) { this.physicalActivityScore = physicalActivityScore; }
    
    public float getSocialActivityScore() { return socialActivityScore; }
    public void setSocialActivityScore(float socialActivityScore) { this.socialActivityScore = socialActivityScore; }
    
    public float getProductivityScore() { return productivityScore; }
    public void setProductivityScore(float productivityScore) { this.productivityScore = productivityScore; }
    
    public float getOverallWellbeingScore() { return overallWellbeingScore; }
    public void setOverallWellbeingScore(float overallWellbeingScore) { this.overallWellbeingScore = overallWellbeingScore; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public int getPhotoCount() { return photoCount; }
    public void setPhotoCount(int photoCount) { this.photoCount = photoCount; }
    
    public float getPhotoActivityScore() { return photoActivityScore; }
    public void setPhotoActivityScore(float photoActivityScore) { this.photoActivityScore = photoActivityScore; }
}