package com.locallife.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background location tracking service with place detection
 */
public class LocationService extends Service implements LocationListener {
    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "location_service_channel";
    private static final int NOTIFICATION_ID = 1002;
    
    // Location update parameters
    private static final long UPDATE_INTERVAL = 2 * 60 * 1000; // 2 minutes
    private static final long FASTEST_INTERVAL = 60 * 1000; // 1 minute
    private static final int MIN_DISTANCE_CHANGE = 50; // 50 meters
    private static final int SIGNIFICANT_DISTANCE_CHANGE = 200; // 200 meters
    
    // Place detection parameters
    private static final int PLACE_DETECTION_RADIUS = 100; // meters
    private static final long MIN_STAY_DURATION = 5 * 60 * 1000; // 5 minutes
    private static final long MAX_VISIT_DURATION = 8 * 60 * 60 * 1000; // 8 hours
    
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private DatabaseHelper databaseHelper;
    private NotificationManager notificationManager;
    private Geocoder geocoder;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Place detection variables
    private Map<String, PlaceVisit> activeVisits = new ConcurrentHashMap<>();
    private List<DetectedPlace> knownPlaces = new ArrayList<>();
    private Location lastSignificantLocation;
    private long lastLocationTime = 0;
    private int totalDistanceTraveled = 0;
    
    // Statistics
    private int locationsProcessed = 0;
    private int placesDetected = 0;
    
    // Place visit tracking
    private static class PlaceVisit {
        String placeId;
        String placeName;
        double latitude;
        double longitude;
        long arrivalTime;
        long departureTime;
        int durationMinutes;
        String category;
        
        PlaceVisit(String placeId, String placeName, double lat, double lng) {
            this.placeId = placeId;
            this.placeName = placeName;
            this.latitude = lat;
            this.longitude = lng;
            this.arrivalTime = System.currentTimeMillis();
        }
    }
    
    // Detected place information
    private static class DetectedPlace {
        String id;
        String name;
        double latitude;
        double longitude;
        String category;
        int visitCount;
        long totalDuration;
        
        DetectedPlace(String id, String name, double lat, double lng, String category) {
            this.id = id;
            this.name = name;
            this.latitude = lat;
            this.longitude = lng;
            this.category = category;
            this.visitCount = 1;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationService created");
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        databaseHelper = DatabaseHelper.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
        
        // Create notification channel
        createNotificationChannel();
        
        // Initialize location request
        initializeLocationRequest();
        
        // Load known places
        loadKnownPlaces();
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationService started");
        startLocationUpdates();
        return START_STICKY;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks your location for place detection");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Tracking Active")
                .setContentText("Detected " + placesDetected + " places today")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
    }
    
    private void initializeLocationRequest() {
        locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setSmallestDisplacement(MIN_DISTANCE_CHANGE);
        
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                
                for (Location location : locationResult.getLocations()) {
                    processLocation(location);
                }
            }
        };
    }
    
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permissions not granted");
            return;
        }
        
        // Start fused location provider
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        
        // Also use system location manager as backup
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, MIN_DISTANCE_CHANGE, this);
        }
        
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, MIN_DISTANCE_CHANGE, this);
        }
        
        Log.d(TAG, "Location updates started");
    }
    
    private void processLocation(Location location) {
        if (location == null) return;
        
        long currentTime = System.currentTimeMillis();
        locationsProcessed++;
        
        // Log location for debugging
        Log.d(TAG, "Location: " + location.getLatitude() + "," + location.getLongitude() + 
                   " Accuracy: " + location.getAccuracy() + "m");
        
        // Calculate distance traveled
        if (lastSignificantLocation != null) {
            float distance = lastSignificantLocation.distanceTo(location);
            if (distance > SIGNIFICANT_DISTANCE_CHANGE) {
                totalDistanceTraveled += distance;
                lastSignificantLocation = location;
                
                // End any active visits when moving significantly
                endActiveVisits();
            }
        } else {
            lastSignificantLocation = location;
        }
        
        // Update location time
        lastLocationTime = currentTime;
        
        // Place detection
        detectPlaces(location);
        
        // Update notification
        updateNotification();
        
        // Save to database every 10 minutes or significant movement
        if (locationsProcessed % 10 == 0 || shouldSaveLocation(location)) {
            saveLocationToDatabase(location);
        }
    }
    
    private void detectPlaces(Location location) {
        // Check if we're near any known places
        DetectedPlace nearbyPlace = findNearbyPlace(location);
        
        if (nearbyPlace != null) {
            // Start or continue visit to known place
            String placeId = nearbyPlace.id;
            if (!activeVisits.containsKey(placeId)) {
                PlaceVisit visit = new PlaceVisit(placeId, nearbyPlace.name, location.getLatitude(), location.getLongitude());
                visit.category = nearbyPlace.category;
                activeVisits.put(placeId, visit);
                
                Log.d(TAG, "Started visit to: " + nearbyPlace.name);
            }
        } else {
            // Check if we should create a new place
            if (shouldCreateNewPlace(location)) {
                createNewPlace(location);
            }
        }
        
        // End visits for places we've moved away from
        checkForEndedVisits(location);
    }
    
    private DetectedPlace findNearbyPlace(Location location) {
        for (DetectedPlace place : knownPlaces) {
            float distance = distanceBetween(location.getLatitude(), location.getLongitude(), 
                                           place.latitude, place.longitude);
            if (distance <= PLACE_DETECTION_RADIUS) {
                return place;
            }
        }
        return null;
    }
    
    private boolean shouldCreateNewPlace(Location location) {
        // Create new place if we've been stationary for a while
        if (lastSignificantLocation == null) return false;
        
        float distance = lastSignificantLocation.distanceTo(location);
        long timeDiff = System.currentTimeMillis() - lastLocationTime;
        
        return distance < PLACE_DETECTION_RADIUS && timeDiff > MIN_STAY_DURATION;
    }
    
    private void createNewPlace(Location location) {
        String placeName = getPlaceNameFromLocation(location);
        String placeCategory = categorizePlace(placeName);
        String placeId = generatePlaceId(location);
        
        DetectedPlace newPlace = new DetectedPlace(placeId, placeName, location.getLatitude(), location.getLongitude(), placeCategory);
        knownPlaces.add(newPlace);
        
        // Start visit
        PlaceVisit visit = new PlaceVisit(placeId, placeName, location.getLatitude(), location.getLongitude());
        visit.category = placeCategory;
        activeVisits.put(placeId, visit);
        
        placesDetected++;
        Log.d(TAG, "Created new place: " + placeName + " (" + placeCategory + ")");
    }
    
    private void checkForEndedVisits(Location currentLocation) {
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, PlaceVisit> entry : activeVisits.entrySet()) {
            PlaceVisit visit = entry.getValue();
            float distance = distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                           visit.latitude, visit.longitude);
            
            if (distance > PLACE_DETECTION_RADIUS * 2) { // Double the radius for departure
                // End the visit
                visit.departureTime = System.currentTimeMillis();
                visit.durationMinutes = (int) ((visit.departureTime - visit.arrivalTime) / 60000);
                
                // Only save visits longer than minimum duration
                if (visit.durationMinutes >= (MIN_STAY_DURATION / 60000)) {
                    savePlaceVisit(visit);
                    updateKnownPlace(visit);
                }
                
                toRemove.add(entry.getKey());
                Log.d(TAG, "Ended visit to: " + visit.placeName + " (" + visit.durationMinutes + " min)");
            }
        }
        
        for (String key : toRemove) {
            activeVisits.remove(key);
        }
    }
    
    private void endActiveVisits() {
        for (PlaceVisit visit : activeVisits.values()) {
            visit.departureTime = System.currentTimeMillis();
            visit.durationMinutes = (int) ((visit.departureTime - visit.arrivalTime) / 60000);
            
            if (visit.durationMinutes >= (MIN_STAY_DURATION / 60000)) {
                savePlaceVisit(visit);
                updateKnownPlace(visit);
            }
        }
        activeVisits.clear();
    }
    
    private void updateKnownPlace(PlaceVisit visit) {
        for (DetectedPlace place : knownPlaces) {
            if (place.id.equals(visit.placeId)) {
                place.visitCount++;
                place.totalDuration += visit.durationMinutes;
                break;
            }
        }
    }
    
    private String getPlaceNameFromLocation(Location location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                // Try to get a meaningful place name
                if (address.getFeatureName() != null) {
                    return address.getFeatureName();
                } else if (address.getThoroughfare() != null) {
                    return address.getThoroughfare();
                } else if (address.getSubLocality() != null) {
                    return address.getSubLocality();
                } else if (address.getLocality() != null) {
                    return address.getLocality();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting place name", e);
        }
        
        return "Unknown Place";
    }
    
    private String categorizePlace(String placeName) {
        if (placeName == null) return "unknown";
        
        String lowerName = placeName.toLowerCase();
        
        // Home indicators
        if (lowerName.contains("home") || lowerName.contains("house") || lowerName.contains("apartment")) {
            return "home";
        }
        
        // Work indicators
        if (lowerName.contains("office") || lowerName.contains("work") || lowerName.contains("building")) {
            return "work";
        }
        
        // Food places
        if (lowerName.contains("restaurant") || lowerName.contains("cafe") || lowerName.contains("food") || 
            lowerName.contains("bar") || lowerName.contains("pub")) {
            return "food";
        }
        
        // Shopping
        if (lowerName.contains("store") || lowerName.contains("shop") || lowerName.contains("mall") || 
            lowerName.contains("market")) {
            return "shopping";
        }
        
        // Health & fitness
        if (lowerName.contains("gym") || lowerName.contains("hospital") || lowerName.contains("clinic") || 
            lowerName.contains("fitness")) {
            return "health";
        }
        
        // Transportation
        if (lowerName.contains("station") || lowerName.contains("airport") || lowerName.contains("bus") || 
            lowerName.contains("train")) {
            return "transport";
        }
        
        // Entertainment
        if (lowerName.contains("cinema") || lowerName.contains("theater") || lowerName.contains("park") || 
            lowerName.contains("museum")) {
            return "entertainment";
        }
        
        return "other";
    }
    
    private String generatePlaceId(Location location) {
        return "place_" + Math.round(location.getLatitude() * 1000) + "_" + Math.round(location.getLongitude() * 1000);
    }
    
    private float distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }
    
    private boolean shouldSaveLocation(Location location) {
        // Save significant locations or every 10 minutes
        return lastSignificantLocation == null || 
               lastSignificantLocation.distanceTo(location) > SIGNIFICANT_DISTANCE_CHANGE ||
               (System.currentTimeMillis() - lastLocationTime) > (10 * 60 * 1000);
    }
    
    private void saveLocationToDatabase(Location location) {
        try {
            String currentDate = dateFormat.format(new Date());
            DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
            
            if (dayRecord == null) {
                dayRecord = new DayRecord();
                dayRecord.setDate(currentDate);
            }
            
            // Update location stats
            dayRecord.setTotalTravelDistance(totalDistanceTraveled);
            dayRecord.setPlacesVisited(placesDetected);
            
            // Set primary location (most recent)
            String primaryLocation = getPlaceNameFromLocation(location);
            dayRecord.setPrimaryLocation(primaryLocation);
            
            // Recalculate activity score
            dayRecord.calculateActivityScore();
            
            if (dayRecord.getId() > 0) {
                databaseHelper.updateDayRecord(dayRecord);
            } else {
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            Log.d(TAG, "Location data saved to database");
        } catch (Exception e) {
            Log.e(TAG, "Error saving location data", e);
        }
    }
    
    private void savePlaceVisit(PlaceVisit visit) {
        try {
            String currentDate = dateFormat.format(new Date(visit.arrivalTime));
            DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
            
            if (dayRecord == null) {
                dayRecord = new DayRecord();
                dayRecord.setDate(currentDate);
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            // Add location visit
            DayRecord.LocationVisit locationVisit = new DayRecord.LocationVisit(visit.placeName, visit.latitude, visit.longitude);
            locationVisit.setArrivalTime(new Date(visit.arrivalTime));
            locationVisit.setDepartureTime(new Date(visit.departureTime));
            locationVisit.setDurationMinutes(visit.durationMinutes);
            locationVisit.setPlaceCategory(visit.category);
            
            dayRecord.getLocationVisits().add(locationVisit);
            dayRecord.setPlacesVisited(dayRecord.getLocationVisits().size());
            
            // Update day record
            databaseHelper.updateDayRecord(dayRecord);
            
            Log.d(TAG, "Place visit saved: " + visit.placeName);
        } catch (Exception e) {
            Log.e(TAG, "Error saving place visit", e);
        }
    }
    
    private void loadKnownPlaces() {
        // Load known places from database or initialize with common places
        // This is a simplified implementation
        knownPlaces.clear();
    }
    
    private void updateNotification() {
        Notification notification = createNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    
    // LocationListener methods (backup)
    @Override
    public void onLocationChanged(Location location) {
        processLocation(location);
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationService destroyed");
        
        // Stop location updates
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        
        // End any active visits
        endActiveVisits();
    }
    
    // Public methods for external access
    public int getPlacesDetected() {
        return placesDetected;
    }
    
    public int getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }
    
    public List<DetectedPlace> getKnownPlaces() {
        return new ArrayList<>(knownPlaces);
    }
    
    public Map<String, PlaceVisit> getActiveVisits() {
        return new HashMap<>(activeVisits);
    }
}