package com.locallife.model;

import java.util.Date;

/**
 * Model class for photo metadata storage
 * Contains EXIF data and location information extracted from photos
 */
public class PhotoMetadata {
    private long id;
    private String photoUri;
    private String photoPath;
    private Date dateTaken;
    private Date dateModified;
    private double latitude;
    private double longitude;
    private float altitude;
    private String locationName;
    private String cameraModel;
    private String cameraMake;
    private int imageWidth;
    private int imageHeight;
    private int orientation;
    private String flashMode;
    private String focalLength;
    private String aperture;
    private String shutterSpeed;
    private String iso;
    private String whiteBalance;
    private long fileSize;
    private String mimeType;
    private boolean hasLocationData;
    private boolean isProcessed;
    private String processingError;
    private Date createdAt;
    private Date updatedAt;
    
    // Activity analysis fields
    private String activityType; // outdoor, indoor, social, travel, etc.
    private int activityScore; // 0-100 based on photo characteristics
    private String timeOfDay; // morning, afternoon, evening, night
    private String season; // spring, summer, fall, winter
    private boolean isOutdoor;
    private boolean hasPeople;
    private String dominantColors;
    
    public PhotoMetadata() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isProcessed = false;
        this.hasLocationData = false;
        this.isOutdoor = false;
        this.hasPeople = false;
    }
    
    public PhotoMetadata(String photoUri, String photoPath) {
        this();
        this.photoUri = photoUri;
        this.photoPath = photoPath;
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    
    public Date getDateTaken() { return dateTaken; }
    public void setDateTaken(Date dateTaken) { this.dateTaken = dateTaken; }
    
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { 
        this.latitude = latitude;
        this.hasLocationData = (latitude != 0.0 || longitude != 0.0);
    }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { 
        this.longitude = longitude;
        this.hasLocationData = (latitude != 0.0 || longitude != 0.0);
    }
    
    public float getAltitude() { return altitude; }
    public void setAltitude(float altitude) { this.altitude = altitude; }
    
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    
    public String getCameraModel() { return cameraModel; }
    public void setCameraModel(String cameraModel) { this.cameraModel = cameraModel; }
    
    public String getCameraMake() { return cameraMake; }
    public void setCameraMake(String cameraMake) { this.cameraMake = cameraMake; }
    
    public int getImageWidth() { return imageWidth; }
    public void setImageWidth(int imageWidth) { this.imageWidth = imageWidth; }
    
    public int getImageHeight() { return imageHeight; }
    public void setImageHeight(int imageHeight) { this.imageHeight = imageHeight; }
    
    public int getOrientation() { return orientation; }
    public void setOrientation(int orientation) { this.orientation = orientation; }
    
    public String getFlashMode() { return flashMode; }
    public void setFlashMode(String flashMode) { this.flashMode = flashMode; }
    
    public String getFocalLength() { return focalLength; }
    public void setFocalLength(String focalLength) { this.focalLength = focalLength; }
    
    public String getAperture() { return aperture; }
    public void setAperture(String aperture) { this.aperture = aperture; }
    
    public String getShutterSpeed() { return shutterSpeed; }
    public void setShutterSpeed(String shutterSpeed) { this.shutterSpeed = shutterSpeed; }
    
    public String getIso() { return iso; }
    public void setIso(String iso) { this.iso = iso; }
    
    public String getWhiteBalance() { return whiteBalance; }
    public void setWhiteBalance(String whiteBalance) { this.whiteBalance = whiteBalance; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public boolean hasLocationData() { return hasLocationData; }
    public void setHasLocationData(boolean hasLocationData) { this.hasLocationData = hasLocationData; }
    
    public boolean isProcessed() { return isProcessed; }
    public void setProcessed(boolean processed) { this.isProcessed = processed; }
    
    public String getProcessingError() { return processingError; }
    public void setProcessingError(String processingError) { this.processingError = processingError; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    // Activity analysis getters and setters
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    
    public int getActivityScore() { return activityScore; }
    public void setActivityScore(int activityScore) { this.activityScore = activityScore; }
    
    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }
    
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public boolean isOutdoor() { return isOutdoor; }
    public void setOutdoor(boolean outdoor) { this.isOutdoor = outdoor; }
    
    public boolean hasPeople() { return hasPeople; }
    public void setHasPeople(boolean hasPeople) { this.hasPeople = hasPeople; }
    
    public String getDominantColors() { return dominantColors; }
    public void setDominantColors(String dominantColors) { this.dominantColors = dominantColors; }
    
    /**
     * Calculate activity score based on photo characteristics
     */
    public void calculateActivityScore() {
        int score = 0;
        
        // Base score for taking a photo
        score += 10;
        
        // Location data bonus
        if (hasLocationData) {
            score += 15;
        }
        
        // Outdoor activity bonus
        if (isOutdoor) {
            score += 20;
        }
        
        // Social activity bonus (people in photo)
        if (hasPeople) {
            score += 15;
        }
        
        // Time of day considerations
        if ("morning".equals(timeOfDay) || "evening".equals(timeOfDay)) {
            score += 10; // Golden hour photography
        }
        
        // Activity type bonuses
        if ("travel".equals(activityType)) {
            score += 25;
        } else if ("outdoor".equals(activityType)) {
            score += 20;
        } else if ("social".equals(activityType)) {
            score += 15;
        }
        
        // Camera settings suggest intentional photography
        if (aperture != null && !aperture.isEmpty()) {
            score += 5;
        }
        if (focalLength != null && !focalLength.isEmpty()) {
            score += 5;
        }
        
        // Cap the score at 100
        this.activityScore = Math.min(100, score);
    }
    
    /**
     * Determine time of day based on photo timestamp
     */
    public void analyzeTimeOfDay() {
        if (dateTaken != null) {
            int hour = dateTaken.getHours();
            if (hour >= 5 && hour < 12) {
                this.timeOfDay = "morning";
            } else if (hour >= 12 && hour < 17) {
                this.timeOfDay = "afternoon";
            } else if (hour >= 17 && hour < 21) {
                this.timeOfDay = "evening";
            } else {
                this.timeOfDay = "night";
            }
        }
    }
    
    /**
     * Determine season based on photo timestamp
     */
    public void analyzeSeason() {
        if (dateTaken != null) {
            int month = dateTaken.getMonth() + 1; // getMonth() is 0-based
            if (month >= 3 && month <= 5) {
                this.season = "spring";
            } else if (month >= 6 && month <= 8) {
                this.season = "summer";
            } else if (month >= 9 && month <= 11) {
                this.season = "fall";
            } else {
                this.season = "winter";
            }
        }
    }
    
    /**
     * Analyze activity type based on various factors
     */
    public void analyzeActivityType() {
        if (hasLocationData) {
            // This could be enhanced with location-based activity detection
            this.activityType = "outdoor";
        } else if (hasPeople) {
            this.activityType = "social";
        } else if (isOutdoor) {
            this.activityType = "outdoor";
        } else {
            this.activityType = "indoor";
        }
    }
    
    /**
     * Get formatted location string
     */
    public String getFormattedLocation() {
        if (locationName != null && !locationName.isEmpty()) {
            return locationName;
        } else if (hasLocationData) {
            return String.format("%.4f, %.4f", latitude, longitude);
        } else {
            return "Unknown location";
        }
    }
    
    /**
     * Get formatted camera info
     */
    public String getFormattedCameraInfo() {
        if (cameraMake != null && cameraModel != null) {
            return cameraMake + " " + cameraModel;
        } else if (cameraModel != null) {
            return cameraModel;
        } else {
            return "Unknown camera";
        }
    }
    
    /**
     * Get formatted image dimensions
     */
    public String getFormattedDimensions() {
        if (imageWidth > 0 && imageHeight > 0) {
            return imageWidth + " × " + imageHeight;
        } else {
            return "Unknown dimensions";
        }
    }
    
    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize > 0) {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format("%.1f KB", fileSize / 1024.0);
            } else {
                return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
            }
        } else {
            return "Unknown size";
        }
    }
    
    @Override
    public String toString() {
        return "PhotoMetadata{" +
                "id=" + id +
                ", photoUri='" + photoUri + '\'' +
                ", dateTaken=" + dateTaken +
                ", hasLocationData=" + hasLocationData +
                ", activityType='" + activityType + '\'' +
                ", activityScore=" + activityScore +
                ", isProcessed=" + isProcessed +
                '}';
    }
}