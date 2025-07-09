package com.locallife.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.PhotoMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for extracting metadata from photos in the device gallery
 * Privacy-focused implementation that only reads metadata, not image content
 */
public class PhotoMetadataService {
    private static final String TAG = "PhotoMetadataService";
    
    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final ExecutorService backgroundExecutor;
    private final Handler mainHandler;
    private final Geocoder geocoder;
    
    // Photo scanning configuration
    private static final int BATCH_SIZE = 50;
    private static final long SCAN_INTERVAL = 30 * 60 * 1000; // 30 minutes
    private static final long CLEANUP_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    
    // Date formats for EXIF data
    private static final SimpleDateFormat EXIF_DATE_FORMAT = 
        new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
    
    // Listeners for photo metadata events
    public interface PhotoMetadataListener {
        void onPhotoProcessed(PhotoMetadata metadata);
        void onBatchProcessed(int count);
        void onScanComplete(int totalPhotos);
        void onError(String error);
    }
    
    private final List<PhotoMetadataListener> listeners = new ArrayList<>();
    
    // Statistics
    private int totalPhotosScanned = 0;
    private int photosWithLocation = 0;
    private int photosProcessedToday = 0;
    private long lastScanTime = 0;
    private String lastError = null;
    
    public PhotoMetadataService(Context context) {
        this.context = context.getApplicationContext();
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }
    
    /**
     * Add a listener for photo metadata events
     */
    public void addListener(PhotoMetadataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener for photo metadata events
     */
    public void removeListener(PhotoMetadataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Start scanning for new photos
     */
    public void startPhotoScan() {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Starting photo scan");
                scanForNewPhotos();
                lastScanTime = System.currentTimeMillis();
                Log.d(TAG, "Photo scan completed");
            } catch (Exception e) {
                Log.e(TAG, "Error during photo scan", e);
                notifyError("Photo scan failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get photo metadata for a specific date
     */
    public void getPhotoMetadataForDate(String date, PhotoMetadataCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<PhotoMetadata> metadata = databaseHelper.getPhotoMetadataForDate(date);
                mainHandler.post(() -> callback.onSuccess(metadata));
            } catch (Exception e) {
                Log.e(TAG, "Error getting photo metadata for date", e);
                mainHandler.post(() -> callback.onError("Failed to get photo metadata: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get photo frequency patterns for activity analysis
     */
    public void getPhotoFrequencyPatterns(PhotoFrequencyCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                Map<String, Integer> patterns = calculatePhotoFrequencyPatterns();
                mainHandler.post(() -> callback.onSuccess(patterns));
            } catch (Exception e) {
                Log.e(TAG, "Error calculating photo frequency patterns", e);
                mainHandler.post(() -> callback.onError("Failed to calculate patterns: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Get photo-based activity score for a date
     */
    public void getPhotoActivityScore(String date, ActivityScoreCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                int score = calculatePhotoActivityScore(date);
                mainHandler.post(() -> callback.onSuccess(score));
            } catch (Exception e) {
                Log.e(TAG, "Error calculating photo activity score", e);
                mainHandler.post(() -> callback.onError("Failed to calculate score: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Scan for new photos and extract metadata
     */
    private void scanForNewPhotos() {
        ContentResolver contentResolver = context.getContentResolver();
        
        // Query for images from MediaStore
        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        };
        
        // Only get photos from the last 30 days to avoid processing all photos
        long thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000);
        String selection = MediaStore.Images.Media.DATE_TAKEN + " > ?";
        String[] selectionArgs = {String.valueOf(thirtyDaysAgo)};
        
        try (Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
        )) {
            
            if (cursor != null) {
                int processedCount = 0;
                
                while (cursor.moveToNext() && processedCount < BATCH_SIZE) {
                    try {
                        PhotoMetadata metadata = extractPhotoMetadata(cursor);
                        if (metadata != null && !isPhotoAlreadyProcessed(metadata.getPhotoPath())) {
                            processPhotoMetadata(metadata);
                            processedCount++;
                            
                            // Notify listeners
                            notifyPhotoProcessed(metadata);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing photo metadata", e);
                        continue;
                    }
                }
                
                totalPhotosScanned += processedCount;
                notifyBatchProcessed(processedCount);
                notifyScanComplete(totalPhotosScanned);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning for photos", e);
            notifyError("Photo scan failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract metadata from a photo cursor
     */
    private PhotoMetadata extractPhotoMetadata(Cursor cursor) {
        try {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            long dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
            long dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
            long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
            int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
            
            Uri photoUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            
            PhotoMetadata metadata = new PhotoMetadata(photoUri.toString(), photoPath);
            metadata.setDateTaken(new Date(dateTaken));
            metadata.setDateModified(new Date(dateModified));
            metadata.setFileSize(fileSize);
            metadata.setMimeType(mimeType);
            metadata.setImageWidth(width);
            metadata.setImageHeight(height);
            
            return metadata;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting basic photo metadata", e);
            return null;
        }
    }
    
    /**
     * Process photo metadata by extracting EXIF data
     */
    private void processPhotoMetadata(PhotoMetadata metadata) {
        try {
            // Extract EXIF data
            extractExifData(metadata);
            
            // Analyze photo characteristics
            analyzePhotoCharacteristics(metadata);
            
            // Save to database
            databaseHelper.insertPhotoMetadata(metadata);
            
            metadata.setProcessed(true);
            photosProcessedToday++;
            
            if (metadata.hasLocationData()) {
                photosWithLocation++;
            }
            
            Log.d(TAG, "Processed photo metadata: " + metadata.toString());
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing photo metadata", e);
            metadata.setProcessed(false);
            metadata.setProcessingError(e.getMessage());
        }
    }
    
    /**
     * Extract EXIF data from photo
     */
    private void extractExifData(PhotoMetadata metadata) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(metadata.getPhotoUri()));
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                
                // Extract GPS coordinates
                double[] latLong = exif.getLatLong();
                if (latLong != null) {
                    metadata.setLatitude(latLong[0]);
                    metadata.setLongitude(latLong[1]);
                    
                    // Get altitude
                    double altitude = exif.getAltitude(0);
                    metadata.setAltitude((float) altitude);
                    
                    // Reverse geocode to get location name
                    resolveLocationName(metadata);
                }
                
                // Extract camera information
                metadata.setCameraMake(exif.getAttribute(ExifInterface.TAG_MAKE));
                metadata.setCameraModel(exif.getAttribute(ExifInterface.TAG_MODEL));
                
                // Extract camera settings
                metadata.setFlashMode(exif.getAttribute(ExifInterface.TAG_FLASH));
                metadata.setFocalLength(exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
                metadata.setAperture(exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE));
                metadata.setShutterSpeed(exif.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE));
                metadata.setIso(exif.getAttribute(ExifInterface.TAG_ISO_SPEED));
                metadata.setWhiteBalance(exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
                
                // Extract orientation
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                metadata.setOrientation(orientation);
                
                // Extract precise date/time if available
                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if (dateTime != null) {
                    try {
                        Date exifDate = EXIF_DATE_FORMAT.parse(dateTime);
                        metadata.setDateTaken(exifDate);
                    } catch (ParseException e) {
                        Log.w(TAG, "Could not parse EXIF date: " + dateTime);
                    }
                }
                
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error extracting EXIF data", e);
        }
    }
    
    /**
     * Resolve location name from GPS coordinates
     */
    private void resolveLocationName(PhotoMetadata metadata) {
        try {
            if (geocoder != null && metadata.hasLocationData()) {
                List<Address> addresses = geocoder.getFromLocation(
                    metadata.getLatitude(), metadata.getLongitude(), 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String locationName = address.getFeatureName();
                    if (locationName == null || locationName.isEmpty()) {
                        locationName = address.getLocality();
                    }
                    if (locationName == null || locationName.isEmpty()) {
                        locationName = address.getSubAdminArea();
                    }
                    if (locationName == null || locationName.isEmpty()) {
                        locationName = address.getAdminArea();
                    }
                    
                    metadata.setLocationName(locationName);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Could not resolve location name", e);
        } catch (Exception e) {
            Log.e(TAG, "Error resolving location name", e);
        }
    }
    
    /**
     * Analyze photo characteristics for activity scoring
     */
    private void analyzePhotoCharacteristics(PhotoMetadata metadata) {
        try {
            // Analyze time of day
            metadata.analyzeTimeOfDay();
            
            // Analyze season
            metadata.analyzeSeason();
            
            // Determine if photo is likely outdoor based on various factors
            analyzeOutdoorActivity(metadata);
            
            // Analyze activity type
            metadata.analyzeActivityType();
            
            // Calculate activity score
            metadata.calculateActivityScore();
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing photo characteristics", e);
        }
    }
    
    /**
     * Analyze if photo represents outdoor activity
     */
    private void analyzeOutdoorActivity(PhotoMetadata metadata) {
        boolean isOutdoor = false;
        
        // Check if photo has GPS data (outdoor photos more likely to have GPS)
        if (metadata.hasLocationData()) {
            isOutdoor = true;
        }
        
        // Check camera flash usage (outdoor photos less likely to use flash)
        String flashMode = metadata.getFlashMode();
        if (flashMode != null && flashMode.contains("0")) { // Flash not fired
            isOutdoor = true;
        }
        
        // Check time of day (outdoor photos more common during day)
        String timeOfDay = metadata.getTimeOfDay();
        if ("morning".equals(timeOfDay) || "afternoon".equals(timeOfDay)) {
            isOutdoor = true;
        }
        
        metadata.setOutdoor(isOutdoor);
    }
    
    /**
     * Check if photo has already been processed
     */
    private boolean isPhotoAlreadyProcessed(String photoPath) {
        try {
            return databaseHelper.isPhotoMetadataExists(photoPath);
        } catch (Exception e) {
            Log.e(TAG, "Error checking if photo is already processed", e);
            return false;
        }
    }
    
    /**
     * Calculate photo frequency patterns for activity analysis
     */
    private Map<String, Integer> calculatePhotoFrequencyPatterns() {
        Map<String, Integer> patterns = new HashMap<>();
        
        try {
            // Get photo counts by time of day
            Map<String, Integer> timeOfDayPatterns = databaseHelper.getPhotoCountsByTimeOfDay();
            patterns.putAll(timeOfDayPatterns);
            
            // Get photo counts by day of week
            Map<String, Integer> dayOfWeekPatterns = databaseHelper.getPhotoCountsByDayOfWeek();
            patterns.putAll(dayOfWeekPatterns);
            
            // Get photo counts by activity type
            Map<String, Integer> activityTypePatterns = databaseHelper.getPhotoCountsByActivityType();
            patterns.putAll(activityTypePatterns);
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating photo frequency patterns", e);
        }
        
        return patterns;
    }
    
    /**
     * Calculate photo-based activity score for a specific date
     */
    private int calculatePhotoActivityScore(String date) {
        try {
            List<PhotoMetadata> photos = databaseHelper.getPhotoMetadataForDate(date);
            
            if (photos.isEmpty()) {
                return 0;
            }
            
            int totalScore = 0;
            int photoCount = photos.size();
            
            for (PhotoMetadata photo : photos) {
                totalScore += photo.getActivityScore();
            }
            
            // Average score with bonus for photo frequency
            int averageScore = totalScore / photoCount;
            int frequencyBonus = Math.min(20, photoCount * 2); // Max 20 bonus points
            
            return Math.min(100, averageScore + frequencyBonus);
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating photo activity score", e);
            return 0;
        }
    }
    
    /**
     * Clean up old photo metadata
     */
    public void cleanupOldPhotoMetadata() {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Cleaning up old photo metadata");
                databaseHelper.deleteOldPhotoMetadata(90); // Keep last 90 days
                Log.d(TAG, "Photo metadata cleanup completed");
            } catch (Exception e) {
                Log.e(TAG, "Error during photo metadata cleanup", e);
                notifyError("Photo metadata cleanup failed: " + e.getMessage());
            }
        });
    }
    
    // Notification methods
    private void notifyPhotoProcessed(PhotoMetadata metadata) {
        mainHandler.post(() -> {
            synchronized (listeners) {
                for (PhotoMetadataListener listener : listeners) {
                    listener.onPhotoProcessed(metadata);
                }
            }
        });
    }
    
    private void notifyBatchProcessed(int count) {
        mainHandler.post(() -> {
            synchronized (listeners) {
                for (PhotoMetadataListener listener : listeners) {
                    listener.onBatchProcessed(count);
                }
            }
        });
    }
    
    private void notifyScanComplete(int totalPhotos) {
        mainHandler.post(() -> {
            synchronized (listeners) {
                for (PhotoMetadataListener listener : listeners) {
                    listener.onScanComplete(totalPhotos);
                }
            }
        });
    }
    
    private void notifyError(String error) {
        lastError = error;
        mainHandler.post(() -> {
            synchronized (listeners) {
                for (PhotoMetadataListener listener : listeners) {
                    listener.onError(error);
                }
            }
        });
    }
    
    // Getters for statistics
    public int getTotalPhotosScanned() { return totalPhotosScanned; }
    public int getPhotosWithLocation() { return photosWithLocation; }
    public int getPhotosProcessedToday() { return photosProcessedToday; }
    public long getLastScanTime() { return lastScanTime; }
    public String getLastError() { return lastError; }
    
    /**
     * Shutdown the service
     */
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
        synchronized (listeners) {
            listeners.clear();
        }
    }
    
    // Callback interfaces
    public interface PhotoMetadataCallback {
        void onSuccess(List<PhotoMetadata> metadata);
        void onError(String error);
    }
    
    public interface PhotoFrequencyCallback {
        void onSuccess(Map<String, Integer> patterns);
        void onError(String error);
    }
    
    public interface ActivityScoreCallback {
        void onSuccess(int score);
        void onError(String error);
    }
}