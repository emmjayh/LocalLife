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
    private float atmosphericPressure;
    private float cloudCoverPercentage;
    private float visibility;
    private String windDirection;
    
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
    
    // Media consumption data
    private int totalMediaMinutes;
    private int videoMinutes;
    private int audioMinutes;
    private int bingeWatchingMinutes;
    private int uniqueMediaPlatforms;
    private float mediaConsumptionScore;
    
    // Environmental data
    // Air quality data
    private int airQualityIndex;
    private String airQualityLevel;
    private float pm25Level;
    private float pm10Level;
    private float no2Level;
    private float o3Level;
    private float coLevel;
    private float airQualityActivityImpact;
    
    // Moon phase data
    private String moonPhase;
    private int moonPhaseIndex;
    private double moonIllumination;
    private double moonAge;
    private boolean isSupermoon;
    private float moonPhaseActivityImpact;
    
    // UV index data
    private double uvIndex;
    private double uvMax;
    private String uvCategory;
    private int burnTimeMinutes;
    private int tanTimeMinutes;
    private double vitaminDTimeMinutes;
    private float uvActivityImpact;
    
    // Daylight data
    private Date sunriseTime;
    private Date sunsetTime;
    private Date solarNoonTime;
    private long dayLengthMinutes;
    private long nightLengthMinutes;
    private String season;
    private double daylightChangeMinutes;
    private String currentCircadianPhase;
    private float circadianActivityScore;
    
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
        
        // Productivity score based on screen time, battery usage, and media consumption
        float screenTimeScore = Math.max(0, 100 - (screenTimeMinutes / 4.8f)); // 8 hours = 0 score
        
        // Media consumption score (moderate consumption is healthy, excessive is not)
        float mediaScore = calculateMediaConsumptionScore();
        
        productivityScore = (screenTimeScore + (100 - batteryUsagePercent) + mediaScore) / 3;
        
        // Base wellbeing score with photo activity contribution
        float baseWellbeingScore = (physicalActivityScore * 0.35f + 
                                   socialActivityScore * 0.25f + 
                                   productivityScore * 0.25f +
                                   photoActivityScore * 0.15f);
        
        // Apply environmental factors
        float environmentalMultiplier = calculateEnvironmentalMultiplier();
        
        // Final scores
        overallWellbeingScore = baseWellbeingScore * environmentalMultiplier;
        activityScore = overallWellbeingScore;
    }
    
    // Calculate environmental impact multiplier
    private float calculateEnvironmentalMultiplier() {
        float multiplier = 1.0f;
        
        // Air quality impact
        if (airQualityActivityImpact > 0) {
            multiplier *= airQualityActivityImpact;
        }
        
        // Moon phase impact
        if (moonPhaseActivityImpact > 0) {
            multiplier *= moonPhaseActivityImpact;
        }
        
        // UV index impact
        if (uvActivityImpact > 0) {
            multiplier *= uvActivityImpact;
        }
        
        // Circadian rhythm impact
        if (circadianActivityScore > 0) {
            multiplier *= circadianActivityScore;
        }
        
        // Weather impact (basic)
        if (weatherCondition != null) {
            switch (weatherCondition.toLowerCase()) {
                case "clear sky":
                case "mainly clear":
                    multiplier *= 1.1f;
                    break;
                case "overcast":
                case "fog":
                    multiplier *= 0.9f;
                    break;
                case "heavy rain":
                case "thunderstorm":
                    multiplier *= 0.7f;
                    break;
                default:
                    // No change
                    break;
            }
        }
        
        // Ensure multiplier stays within reasonable bounds
        return Math.max(0.3f, Math.min(1.5f, multiplier));
    }
    
    // Calculate media consumption score
    private float calculateMediaConsumptionScore() {
        // Optimal media consumption: 1-3 hours per day
        // Score decreases for excessive consumption or complete avoidance
        
        if (totalMediaMinutes == 0) {
            return 60; // Slight penalty for no media consumption
        }
        
        float hours = totalMediaMinutes / 60.0f;
        
        // Optimal range: 1-3 hours = 100 points
        if (hours >= 1.0f && hours <= 3.0f) {
            return 100;
        }
        
        // Less than 1 hour: linear scale from 60 to 100
        if (hours < 1.0f) {
            return 60 + (hours * 40);
        }
        
        // More than 3 hours: decreasing score
        if (hours <= 6.0f) {
            return 100 - ((hours - 3.0f) * 20); // 20 points per hour over 3
        }
        
        // Excessive consumption (>6 hours): significant penalty
        return Math.max(20, 40 - ((hours - 6.0f) * 10));
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
    
    public float getAtmosphericPressure() { return atmosphericPressure; }
    public void setAtmosphericPressure(float atmosphericPressure) { this.atmosphericPressure = atmosphericPressure; }
    
    public float getCloudCoverPercentage() { return cloudCoverPercentage; }
    public void setCloudCoverPercentage(float cloudCoverPercentage) { this.cloudCoverPercentage = cloudCoverPercentage; }
    
    public float getVisibility() { return visibility; }
    public void setVisibility(float visibility) { this.visibility = visibility; }
    
    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }
    
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
    
    // Media consumption getters and setters
    public int getTotalMediaMinutes() { return totalMediaMinutes; }
    public void setTotalMediaMinutes(int totalMediaMinutes) { this.totalMediaMinutes = totalMediaMinutes; }
    
    public int getVideoMinutes() { return videoMinutes; }
    public void setVideoMinutes(int videoMinutes) { this.videoMinutes = videoMinutes; }
    
    public int getAudioMinutes() { return audioMinutes; }
    public void setAudioMinutes(int audioMinutes) { this.audioMinutes = audioMinutes; }
    
    public int getBingeWatchingMinutes() { return bingeWatchingMinutes; }
    public void setBingeWatchingMinutes(int bingeWatchingMinutes) { this.bingeWatchingMinutes = bingeWatchingMinutes; }
    
    public int getUniqueMediaPlatforms() { return uniqueMediaPlatforms; }
    public void setUniqueMediaPlatforms(int uniqueMediaPlatforms) { this.uniqueMediaPlatforms = uniqueMediaPlatforms; }
    
    public float getMediaConsumptionScore() { return mediaConsumptionScore; }
    public void setMediaConsumptionScore(float mediaConsumptionScore) { this.mediaConsumptionScore = mediaConsumptionScore; }
    
    // Environmental data getters and setters
    // Air quality
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
    
    public float getAirQualityActivityImpact() { return airQualityActivityImpact; }
    public void setAirQualityActivityImpact(float airQualityActivityImpact) { this.airQualityActivityImpact = airQualityActivityImpact; }
    
    // Moon phase
    public String getMoonPhase() { return moonPhase; }
    public void setMoonPhase(String moonPhase) { this.moonPhase = moonPhase; }
    
    public int getMoonPhaseIndex() { return moonPhaseIndex; }
    public void setMoonPhaseIndex(int moonPhaseIndex) { this.moonPhaseIndex = moonPhaseIndex; }
    
    public double getMoonIllumination() { return moonIllumination; }
    public void setMoonIllumination(double moonIllumination) { this.moonIllumination = moonIllumination; }
    
    public double getMoonAge() { return moonAge; }
    public void setMoonAge(double moonAge) { this.moonAge = moonAge; }
    
    public boolean isSupermoon() { return isSupermoon; }
    public void setSupermoon(boolean supermoon) { isSupermoon = supermoon; }
    
    public float getMoonPhaseActivityImpact() { return moonPhaseActivityImpact; }
    public void setMoonPhaseActivityImpact(float moonPhaseActivityImpact) { this.moonPhaseActivityImpact = moonPhaseActivityImpact; }
    
    // UV index
    public double getUvIndex() { return uvIndex; }
    public void setUvIndex(double uvIndex) { this.uvIndex = uvIndex; }
    
    public double getUvMax() { return uvMax; }
    public void setUvMax(double uvMax) { this.uvMax = uvMax; }
    
    public String getUvCategory() { return uvCategory; }
    public void setUvCategory(String uvCategory) { this.uvCategory = uvCategory; }
    
    public int getBurnTimeMinutes() { return burnTimeMinutes; }
    public void setBurnTimeMinutes(int burnTimeMinutes) { this.burnTimeMinutes = burnTimeMinutes; }
    
    public int getTanTimeMinutes() { return tanTimeMinutes; }
    public void setTanTimeMinutes(int tanTimeMinutes) { this.tanTimeMinutes = tanTimeMinutes; }
    
    public double getVitaminDTimeMinutes() { return vitaminDTimeMinutes; }
    public void setVitaminDTimeMinutes(double vitaminDTimeMinutes) { this.vitaminDTimeMinutes = vitaminDTimeMinutes; }
    
    public float getUvActivityImpact() { return uvActivityImpact; }
    public void setUvActivityImpact(float uvActivityImpact) { this.uvActivityImpact = uvActivityImpact; }
    
    // Daylight data
    public Date getSunriseTime() { return sunriseTime; }
    public void setSunriseTime(Date sunriseTime) { this.sunriseTime = sunriseTime; }
    
    public Date getSunsetTime() { return sunsetTime; }
    public void setSunsetTime(Date sunsetTime) { this.sunsetTime = sunsetTime; }
    
    public Date getSolarNoonTime() { return solarNoonTime; }
    public void setSolarNoonTime(Date solarNoonTime) { this.solarNoonTime = solarNoonTime; }
    
    public long getDayLengthMinutes() { return dayLengthMinutes; }
    public void setDayLengthMinutes(long dayLengthMinutes) { this.dayLengthMinutes = dayLengthMinutes; }
    
    public long getNightLengthMinutes() { return nightLengthMinutes; }
    public void setNightLengthMinutes(long nightLengthMinutes) { this.nightLengthMinutes = nightLengthMinutes; }
    
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public double getDaylightChangeMinutes() { return daylightChangeMinutes; }
    public void setDaylightChangeMinutes(double daylightChangeMinutes) { this.daylightChangeMinutes = daylightChangeMinutes; }
    
    public String getCurrentCircadianPhase() { return currentCircadianPhase; }
    public void setCurrentCircadianPhase(String currentCircadianPhase) { this.currentCircadianPhase = currentCircadianPhase; }
    
    public float getCircadianActivityScore() { return circadianActivityScore; }
    public void setCircadianActivityScore(float circadianActivityScore) { this.circadianActivityScore = circadianActivityScore; }
}