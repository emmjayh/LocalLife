package com.locallife.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Privacy manager for photo metadata access
 * Handles permissions, user preferences, and privacy controls
 */
public class PhotoPrivacyManager {
    private static final String TAG = "PhotoPrivacyManager";
    private static final String PREFS_NAME = "photo_privacy_prefs";
    
    // Privacy preference keys
    private static final String KEY_PHOTO_SCANNING_ENABLED = "photo_scanning_enabled";
    private static final String KEY_LOCATION_EXTRACTION_ENABLED = "location_extraction_enabled";
    private static final String KEY_METADATA_STORAGE_ENABLED = "metadata_storage_enabled";
    private static final String KEY_PHOTO_INSIGHTS_ENABLED = "photo_insights_enabled";
    private static final String KEY_PHOTO_ACTIVITY_SCORING_ENABLED = "photo_activity_scoring_enabled";
    private static final String KEY_LAST_PRIVACY_REVIEW = "last_privacy_review";
    
    // Permission request codes
    public static final int REQUEST_MEDIA_PERMISSIONS = 1001;
    public static final int REQUEST_LOCATION_PERMISSIONS = 1002;
    
    private final Context context;
    private final SharedPreferences preferences;
    
    // Privacy settings
    private boolean photoScanningEnabled = true;
    private boolean locationExtractionEnabled = true;
    private boolean metadataStorageEnabled = true;
    private boolean photoInsightsEnabled = true;
    private boolean photoActivityScoringEnabled = true;
    
    public PhotoPrivacyManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadPrivacyPreferences();
    }
    
    /**
     * Load privacy preferences from SharedPreferences
     */
    private void loadPrivacyPreferences() {
        photoScanningEnabled = preferences.getBoolean(KEY_PHOTO_SCANNING_ENABLED, true);
        locationExtractionEnabled = preferences.getBoolean(KEY_LOCATION_EXTRACTION_ENABLED, true);
        metadataStorageEnabled = preferences.getBoolean(KEY_METADATA_STORAGE_ENABLED, true);
        photoInsightsEnabled = preferences.getBoolean(KEY_PHOTO_INSIGHTS_ENABLED, true);
        photoActivityScoringEnabled = preferences.getBoolean(KEY_PHOTO_ACTIVITY_SCORING_ENABLED, true);
        
        Log.d(TAG, "Privacy preferences loaded - Photo scanning: " + photoScanningEnabled + 
              ", Location extraction: " + locationExtractionEnabled);
    }
    
    /**
     * Save privacy preferences to SharedPreferences
     */
    private void savePrivacyPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_PHOTO_SCANNING_ENABLED, photoScanningEnabled);
        editor.putBoolean(KEY_LOCATION_EXTRACTION_ENABLED, locationExtractionEnabled);
        editor.putBoolean(KEY_METADATA_STORAGE_ENABLED, metadataStorageEnabled);
        editor.putBoolean(KEY_PHOTO_INSIGHTS_ENABLED, photoInsightsEnabled);
        editor.putBoolean(KEY_PHOTO_ACTIVITY_SCORING_ENABLED, photoActivityScoringEnabled);
        editor.putLong(KEY_LAST_PRIVACY_REVIEW, System.currentTimeMillis());
        editor.apply();
        
        Log.d(TAG, "Privacy preferences saved");
    }
    
    /**
     * Check if all required permissions are granted
     */
    public boolean hasRequiredPermissions() {
        return hasMediaPermissions() && (hasLocationPermissions() || !locationExtractionEnabled);
    }
    
    /**
     * Check if media permissions are granted
     */
    public boolean hasMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Request media permissions from the user
     */
    public void requestMediaPermissions(Activity activity) {
        List<String> permissions = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), REQUEST_MEDIA_PERMISSIONS);
        }
    }
    
    /**
     * Request location permissions from the user (if location extraction is enabled)
     */
    public void requestLocationPermissions(Activity activity) {
        if (!locationExtractionEnabled) {
            return;
        }
        
        List<String> permissions = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION);
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), REQUEST_LOCATION_PERMISSIONS);
        }
    }
    
    /**
     * Check if photo scanning is enabled by user
     */
    public boolean isPhotoScanningEnabled() {
        return photoScanningEnabled && hasMediaPermissions();
    }
    
    /**
     * Enable or disable photo scanning
     */
    public void setPhotoScanningEnabled(boolean enabled) {
        this.photoScanningEnabled = enabled;
        savePrivacyPreferences();
    }
    
    /**
     * Check if location extraction is enabled by user
     */
    public boolean isLocationExtractionEnabled() {
        return locationExtractionEnabled && hasLocationPermissions();
    }
    
    /**
     * Enable or disable location extraction from photos
     */
    public void setLocationExtractionEnabled(boolean enabled) {
        this.locationExtractionEnabled = enabled;
        savePrivacyPreferences();
    }
    
    /**
     * Check if metadata storage is enabled by user
     */
    public boolean isMetadataStorageEnabled() {
        return metadataStorageEnabled;
    }
    
    /**
     * Enable or disable metadata storage
     */
    public void setMetadataStorageEnabled(boolean enabled) {
        this.metadataStorageEnabled = enabled;
        savePrivacyPreferences();
    }
    
    /**
     * Check if photo insights are enabled by user
     */
    public boolean isPhotoInsightsEnabled() {
        return photoInsightsEnabled;
    }
    
    /**
     * Enable or disable photo insights
     */
    public void setPhotoInsightsEnabled(boolean enabled) {
        this.photoInsightsEnabled = enabled;
        savePrivacyPreferences();
    }
    
    /**
     * Check if photo activity scoring is enabled by user
     */
    public boolean isPhotoActivityScoringEnabled() {
        return photoActivityScoringEnabled;
    }
    
    /**
     * Enable or disable photo activity scoring
     */
    public void setPhotoActivityScoringEnabled(boolean enabled) {
        this.photoActivityScoringEnabled = enabled;
        savePrivacyPreferences();
    }
    
    /**
     * Get the last time user reviewed privacy settings
     */
    public long getLastPrivacyReview() {
        return preferences.getLong(KEY_LAST_PRIVACY_REVIEW, 0);
    }
    
    /**
     * Check if user should be prompted to review privacy settings
     */
    public boolean shouldPromptPrivacyReview() {
        long lastReview = getLastPrivacyReview();
        long thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000);
        return lastReview < thirtyDaysAgo;
    }
    
    /**
     * Mark privacy settings as reviewed
     */
    public void markPrivacyReviewed() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_LAST_PRIVACY_REVIEW, System.currentTimeMillis());
        editor.apply();
    }
    
    /**
     * Get a summary of current privacy settings
     */
    public String getPrivacySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Photo Privacy Settings:\\n");
        summary.append("- Photo Scanning: ").append(photoScanningEnabled ? "Enabled" : "Disabled").append("\\n");
        summary.append("- Location Extraction: ").append(locationExtractionEnabled ? "Enabled" : "Disabled").append("\\n");
        summary.append("- Metadata Storage: ").append(metadataStorageEnabled ? "Enabled" : "Disabled").append("\\n");
        summary.append("- Photo Insights: ").append(photoInsightsEnabled ? "Enabled" : "Disabled").append("\\n");
        summary.append("- Activity Scoring: ").append(photoActivityScoringEnabled ? "Enabled" : "Disabled").append("\\n");
        summary.append("- Media Permissions: ").append(hasMediaPermissions() ? "Granted" : "Not Granted").append("\\n");
        summary.append("- Location Permissions: ").append(hasLocationPermissions() ? "Granted" : "Not Granted");
        return summary.toString();
    }
    
    /**
     * Reset all privacy settings to default values
     */
    public void resetToDefaults() {
        photoScanningEnabled = true;
        locationExtractionEnabled = true;
        metadataStorageEnabled = true;
        photoInsightsEnabled = true;
        photoActivityScoringEnabled = true;
        savePrivacyPreferences();
        Log.d(TAG, "Privacy settings reset to defaults");
    }
    
    /**
     * Disable all photo-related features (maximum privacy)
     */
    public void enableMaximumPrivacy() {
        photoScanningEnabled = false;
        locationExtractionEnabled = false;
        metadataStorageEnabled = false;
        photoInsightsEnabled = false;
        photoActivityScoringEnabled = false;
        savePrivacyPreferences();
        Log.d(TAG, "Maximum privacy mode enabled");
    }
    
    /**
     * Check if the app can process photos based on current privacy settings
     */
    public boolean canProcessPhotos() {
        return isPhotoScanningEnabled() && hasRequiredPermissions();
    }
    
    /**
     * Check if the app can extract location data from photos
     */
    public boolean canExtractLocation() {
        return isLocationExtractionEnabled() && hasLocationPermissions();
    }
    
    /**
     * Check if the app can store photo metadata
     */
    public boolean canStoreMetadata() {
        return isMetadataStorageEnabled();
    }
    
    /**
     * Check if the app can generate photo insights
     */
    public boolean canGenerateInsights() {
        return isPhotoInsightsEnabled() && canProcessPhotos();
    }
    
    /**
     * Check if the app can calculate photo activity scores
     */
    public boolean canCalculateActivityScores() {
        return isPhotoActivityScoringEnabled() && canProcessPhotos();
    }
    
    /**
     * Interface for privacy setting change callbacks
     */
    public interface PrivacySettingsChangeListener {
        void onPrivacySettingsChanged();
    }
    
    private final List<PrivacySettingsChangeListener> listeners = new ArrayList<>();
    
    /**
     * Add a listener for privacy settings changes
     */
    public void addPrivacySettingsChangeListener(PrivacySettingsChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener for privacy settings changes
     */
    public void removePrivacySettingsChangeListener(PrivacySettingsChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners that privacy settings have changed
     */
    private void notifyPrivacySettingsChanged() {
        for (PrivacySettingsChangeListener listener : listeners) {
            listener.onPrivacySettingsChanged();
        }
    }
}