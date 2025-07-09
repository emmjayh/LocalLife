package com.locallife.service;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

/**
 * Service for fetching UV index data from OpenUV API
 * Free tier available with registration
 */
public class UVIndexService {
    private static final String TAG = "UVIndexService";
    private static final String BASE_URL = "https://api.openuv.io/api/v1/uv";
    private static final String FORECAST_URL = "https://api.openuv.io/api/v1/forecast";
    
    // Alternative free UV calculation (no API key required)
    private static final boolean USE_CALCULATED_UV = true;
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    
    // UV Index categories
    private static final String[] UV_CATEGORIES = {
        "Low", "Low", "Low", "Moderate", "Moderate", "Moderate", 
        "High", "High", "Very High", "Very High", "Extreme"
    };
    
    // UV protection recommendations
    private static final Map<String, String> UV_RECOMMENDATIONS = new HashMap<>();
    static {
        UV_RECOMMENDATIONS.put("Low", "No protection needed. Safe for outdoor activities.");
        UV_RECOMMENDATIONS.put("Moderate", "Seek shade during midday hours. Wear sunscreen and protective clothing.");
        UV_RECOMMENDATIONS.put("High", "Protection required. Seek shade, wear sunscreen SPF 30+, and protective clothing.");
        UV_RECOMMENDATIONS.put("Very High", "Extra protection needed. Avoid midday sun, wear sunscreen SPF 50+, and full protective clothing.");
        UV_RECOMMENDATIONS.put("Extreme", "Avoid sun exposure. Stay indoors during peak hours, use maximum sun protection.");
    }
    
    // Skin type factors
    private static final Map<String, Float> SKIN_TYPE_FACTORS = new HashMap<>();
    static {
        SKIN_TYPE_FACTORS.put("Very Fair", 0.5f);
        SKIN_TYPE_FACTORS.put("Fair", 0.7f);
        SKIN_TYPE_FACTORS.put("Medium", 1.0f);
        SKIN_TYPE_FACTORS.put("Olive", 1.3f);
        SKIN_TYPE_FACTORS.put("Brown", 1.6f);
        SKIN_TYPE_FACTORS.put("Dark", 2.0f);
    }
    
    public interface UVIndexCallback {
        void onUVIndexReceived(UVIndexData uvIndexData);
        void onError(String error);
    }
    
    public static class UVIndexData {
        private Date date;
        private Date timestamp;
        private double latitude;
        private double longitude;
        private double uvIndex;
        private double uvMax;
        private String uvCategory;
        private String safetyRecommendation;
        private int burnTimeMinutes;
        private int tanTimeMinutes;
        private Date sunriseTime;
        private Date sunsetTime;
        private String peakUVTime;
        private List<HourlyUVData> hourlyForecast;
        private float activityImpactScore;
        private String skinTypeRecommendation;
        private double vitaminDTime; // Minutes needed for vitamin D synthesis
        
        public UVIndexData() {
            this.timestamp = new Date();
            this.hourlyForecast = new ArrayList<>();
        }
        
        public static class HourlyUVData {
            private Date time;
            private double uvIndex;
            private String uvCategory;
            
            public HourlyUVData(Date time, double uvIndex) {
                this.time = time;
                this.uvIndex = uvIndex;
                this.uvCategory = getUVCategory(uvIndex);
            }
            
            // Getters and setters
            public Date getTime() { return time; }
            public void setTime(Date time) { this.time = time; }
            
            public double getUvIndex() { return uvIndex; }
            public void setUvIndex(double uvIndex) { this.uvIndex = uvIndex; }
            
            public String getUvCategory() { return uvCategory; }
            public void setUvCategory(String uvCategory) { this.uvCategory = uvCategory; }
        }
        
        // Getters and setters
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public double getUvIndex() { return uvIndex; }
        public void setUvIndex(double uvIndex) { this.uvIndex = uvIndex; }
        
        public double getUvMax() { return uvMax; }
        public void setUvMax(double uvMax) { this.uvMax = uvMax; }
        
        public String getUvCategory() { return uvCategory; }
        public void setUvCategory(String uvCategory) { this.uvCategory = uvCategory; }
        
        public String getSafetyRecommendation() { return safetyRecommendation; }
        public void setSafetyRecommendation(String safetyRecommendation) { this.safetyRecommendation = safetyRecommendation; }
        
        public int getBurnTimeMinutes() { return burnTimeMinutes; }
        public void setBurnTimeMinutes(int burnTimeMinutes) { this.burnTimeMinutes = burnTimeMinutes; }
        
        public int getTanTimeMinutes() { return tanTimeMinutes; }
        public void setTanTimeMinutes(int tanTimeMinutes) { this.tanTimeMinutes = tanTimeMinutes; }
        
        public Date getSunriseTime() { return sunriseTime; }
        public void setSunriseTime(Date sunriseTime) { this.sunriseTime = sunriseTime; }
        
        public Date getSunsetTime() { return sunsetTime; }
        public void setSunsetTime(Date sunsetTime) { this.sunsetTime = sunsetTime; }
        
        public String getPeakUVTime() { return peakUVTime; }
        public void setPeakUVTime(String peakUVTime) { this.peakUVTime = peakUVTime; }
        
        public List<HourlyUVData> getHourlyForecast() { return hourlyForecast; }
        public void setHourlyForecast(List<HourlyUVData> hourlyForecast) { this.hourlyForecast = hourlyForecast; }
        
        public float getActivityImpactScore() { return activityImpactScore; }
        public void setActivityImpactScore(float activityImpactScore) { this.activityImpactScore = activityImpactScore; }
        
        public String getSkinTypeRecommendation() { return skinTypeRecommendation; }
        public void setSkinTypeRecommendation(String skinTypeRecommendation) { this.skinTypeRecommendation = skinTypeRecommendation; }
        
        public double getVitaminDTime() { return vitaminDTime; }
        public void setVitaminDTime(double vitaminDTime) { this.vitaminDTime = vitaminDTime; }
        
        @Override
        public String toString() {
            return "UVIndexData{" +
                    "uvIndex=" + uvIndex +
                    ", uvCategory='" + uvCategory + '\'' +
                    ", burnTimeMinutes=" + burnTimeMinutes +
                    ", peakUVTime='" + peakUVTime + '\'' +
                    '}';
        }
    }
    
    public UVIndexService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Get current UV index for location
     */
    public void getCurrentUVIndex(double latitude, double longitude, UVIndexCallback callback) {
        executorService.execute(() -> {
            try {
                UVIndexData uvIndexData;
                
                if (USE_CALCULATED_UV) {
                    // Use calculated UV index (no API key required)
                    uvIndexData = calculateUVIndex(latitude, longitude, new Date());
                } else {
                    // Use OpenUV API (requires API key)
                    String url = buildUVIndexUrl(latitude, longitude);
                    String response = makeHttpRequest(url);
                    uvIndexData = parseUVIndexResponse(response);
                }
                
                if (uvIndexData != null) {
                    uvIndexData.setLatitude(latitude);
                    uvIndexData.setLongitude(longitude);
                    
                    // Calculate additional data
                    calculateUVMetrics(uvIndexData);
                    calculateActivityImpact(uvIndexData);
                    
                    // Save to database
                    saveUVIndexToDatabase(uvIndexData);
                    
                    // Update day record
                    updateDayRecordWithUVIndex(uvIndexData);
                    
                    callback.onUVIndexReceived(uvIndexData);
                } else {
                    callback.onError("No UV index data available");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching UV index", e);
                callback.onError("Failed to fetch UV index: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get UV index forecast for location
     */
    public void getUVIndexForecast(double latitude, double longitude, int days, UVIndexCallback callback) {
        executorService.execute(() -> {
            try {
                UVIndexData uvIndexData;
                
                if (USE_CALCULATED_UV) {
                    // Calculate UV forecast
                    uvIndexData = calculateUVForecast(latitude, longitude, days);
                } else {
                    // Use OpenUV API forecast
                    String url = buildUVForecastUrl(latitude, longitude);
                    String response = makeHttpRequest(url);
                    uvIndexData = parseUVForecastResponse(response);
                }
                
                if (uvIndexData != null) {
                    callback.onUVIndexReceived(uvIndexData);
                } else {
                    callback.onError("No UV forecast data available");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching UV forecast", e);
                callback.onError("Failed to fetch UV forecast: " + e.getMessage());
            }
        });
    }
    
    /**
     * Calculate UV index without API (astronomical calculation)
     */
    private UVIndexData calculateUVIndex(double latitude, double longitude, Date date) {
        UVIndexData uvIndexData = new UVIndexData();
        uvIndexData.setDate(date);
        uvIndexData.setLatitude(latitude);
        uvIndexData.setLongitude(longitude);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        // Calculate day of year
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Calculate solar declination
        double declination = 23.45 * Math.sin(Math.toRadians(360 * (284 + dayOfYear) / 365));
        
        // Calculate hour angle
        double hourAngle = 15 * (hour - 12);
        
        // Calculate solar elevation angle
        double elevation = Math.asin(
            Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(declination)) +
            Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(declination)) * Math.cos(Math.toRadians(hourAngle))
        );
        
        // Calculate UV index based on solar elevation
        double uvIndex = 0;
        if (elevation > 0) {
            // Simplified UV calculation
            double elevationDegrees = Math.toDegrees(elevation);
            uvIndex = Math.max(0, (elevationDegrees - 15) / 5); // Rough approximation
            
            // Adjust for season and latitude
            double latitudeAdjustment = 1 - Math.abs(latitude) / 90;
            uvIndex *= latitudeAdjustment;
            
            // Adjust for ozone layer and atmospheric conditions
            uvIndex *= 0.8; // Typical atmospheric absorption
            
            // Cap at reasonable maximum
            uvIndex = Math.min(uvIndex, 12);
        }
        
        uvIndexData.setUvIndex(uvIndex);
        uvIndexData.setUvCategory(getUVCategory(uvIndex));
        
        // Calculate max UV for the day (typically around solar noon)
        double maxUV = calculateMaxUVForDay(latitude, dayOfYear);
        uvIndexData.setUvMax(maxUV);
        
        return uvIndexData;
    }
    
    /**
     * Calculate maximum UV index for the day
     */
    private double calculateMaxUVForDay(double latitude, int dayOfYear) {
        // Calculate solar declination
        double declination = 23.45 * Math.sin(Math.toRadians(360 * (284 + dayOfYear) / 365));
        
        // Calculate solar elevation at solar noon
        double maxElevation = Math.asin(
            Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(declination)) +
            Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(declination))
        );
        
        if (maxElevation > 0) {
            double elevationDegrees = Math.toDegrees(maxElevation);
            double maxUV = Math.max(0, (elevationDegrees - 15) / 5);
            
            // Apply adjustments
            double latitudeAdjustment = 1 - Math.abs(latitude) / 90;
            maxUV *= latitudeAdjustment * 0.8;
            
            return Math.min(maxUV, 12);
        }
        
        return 0;
    }
    
    /**
     * Calculate UV forecast for multiple days
     */
    private UVIndexData calculateUVForecast(double latitude, double longitude, int days) {
        UVIndexData uvIndexData = calculateUVIndex(latitude, longitude, new Date());
        
        // Generate hourly forecast for today
        List<UVIndexData.HourlyUVData> hourlyForecast = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6); // Start at 6 AM
        calendar.set(Calendar.MINUTE, 0);
        
        for (int hour = 6; hour <= 18; hour++) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            UVIndexData hourlyData = calculateUVIndex(latitude, longitude, calendar.getTime());
            UVIndexData.HourlyUVData hourlyUV = new UVIndexData.HourlyUVData(
                calendar.getTime(), hourlyData.getUvIndex()
            );
            hourlyForecast.add(hourlyUV);
        }
        
        uvIndexData.setHourlyForecast(hourlyForecast);
        
        // Find peak UV time
        UVIndexData.HourlyUVData peakUV = hourlyForecast.stream()
            .max((a, b) -> Double.compare(a.getUvIndex(), b.getUvIndex()))
            .orElse(null);
        
        if (peakUV != null) {
            uvIndexData.setPeakUVTime(timeFormat.format(peakUV.getTime()));
        }
        
        return uvIndexData;
    }
    
    /**
     * Get UV category from UV index value
     */
    private static String getUVCategory(double uvIndex) {
        int index = Math.min((int) Math.round(uvIndex), UV_CATEGORIES.length - 1);
        return UV_CATEGORIES[Math.max(0, index)];
    }
    
    /**
     * Build UV index API URL
     */
    private String buildUVIndexUrl(double latitude, double longitude) {
        return BASE_URL + "?" +
                "lat=" + latitude +
                "&lng=" + longitude +
                "&alt=100" + // Altitude in meters
                "&dt=" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(new Date());
    }
    
    /**
     * Build UV forecast API URL
     */
    private String buildUVForecastUrl(double latitude, double longitude) {
        return FORECAST_URL + "?" +
                "lat=" + latitude +
                "&lng=" + longitude +
                "&alt=100";
    }
    
    /**
     * Make HTTP request to UV API
     */
    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "LocalLife-Android-App");
            // Add API key header if using OpenUV API
            // connection.setRequestProperty("x-access-token", "YOUR_API_KEY");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readInputStream(connection.getInputStream());
            } else {
                String errorResponse = readInputStream(connection.getErrorStream());
                throw new IOException("HTTP " + responseCode + ": " + errorResponse);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Read input stream to string
     */
    private String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }
    
    /**
     * Parse UV index API response
     */
    private UVIndexData parseUVIndexResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        JSONObject result = jsonResponse.getJSONObject("result");
        
        UVIndexData uvIndexData = new UVIndexData();
        uvIndexData.setUvIndex(result.getDouble("uv"));
        uvIndexData.setUvMax(result.getDouble("uv_max"));
        
        return uvIndexData;
    }
    
    /**
     * Parse UV forecast API response
     */
    private UVIndexData parseUVForecastResponse(String response) throws JSONException {
        // Similar to parseUVIndexResponse but for forecast data
        return parseUVIndexResponse(response);
    }
    
    /**
     * Calculate UV metrics (burn time, tan time, etc.)
     */
    private void calculateUVMetrics(UVIndexData uvIndexData) {
        double uvIndex = uvIndexData.getUvIndex();
        
        // Calculate burn time for average skin (Type III)
        int burnTimeMinutes = 0;
        if (uvIndex > 0) {
            burnTimeMinutes = (int) (200 / uvIndex); // Rough approximation
        }
        uvIndexData.setBurnTimeMinutes(burnTimeMinutes);
        
        // Calculate tan time (typically 3-4 times burn time)
        int tanTimeMinutes = burnTimeMinutes * 3;
        uvIndexData.setTanTimeMinutes(tanTimeMinutes);
        
        // Calculate vitamin D synthesis time
        double vitaminDTime = 0;
        if (uvIndex > 1) {
            vitaminDTime = 10 / uvIndex; // Minutes for adequate vitamin D
        }
        uvIndexData.setVitaminDTime(vitaminDTime);
        
        // Set safety recommendation
        String category = uvIndexData.getUvCategory();
        uvIndexData.setSafetyRecommendation(UV_RECOMMENDATIONS.get(category));
    }
    
    /**
     * Calculate activity impact based on UV index
     */
    private void calculateActivityImpact(UVIndexData uvIndexData) {
        double uvIndex = uvIndexData.getUvIndex();
        float activityImpact;
        
        if (uvIndex <= 2) {
            activityImpact = 1.0f; // No impact
        } else if (uvIndex <= 5) {
            activityImpact = 0.9f; // Minor impact
        } else if (uvIndex <= 7) {
            activityImpact = 0.7f; // Moderate impact
        } else if (uvIndex <= 10) {
            activityImpact = 0.5f; // Significant impact
        } else {
            activityImpact = 0.3f; // High impact
        }
        
        uvIndexData.setActivityImpactScore(activityImpact);
    }
    
    /**
     * Save UV index data to database
     */
    private void saveUVIndexToDatabase(UVIndexData uvIndexData) {
        try {
            String currentDate = dateFormat.format(uvIndexData.getDate());
            
            // This would save to a dedicated UV index table
            databaseHelper.insertUVIndexData(
                    currentDate,
                    uvIndexData.getLatitude(),
                    uvIndexData.getLongitude(),
                    uvIndexData.getUvIndex(),
                    uvIndexData.getUvMax(),
                    uvIndexData.getUvCategory(),
                    uvIndexData.getBurnTimeMinutes(),
                    uvIndexData.getTanTimeMinutes(),
                    uvIndexData.getPeakUVTime(),
                    uvIndexData.getVitaminDTime(),
                    uvIndexData.getActivityImpactScore()
            );
            
            Log.d(TAG, "UV index data saved to database");
        } catch (Exception e) {
            Log.e(TAG, "Error saving UV index data to database", e);
        }
    }
    
    /**
     * Update day record with UV index data
     */
    private void updateDayRecordWithUVIndex(UVIndexData uvIndexData) {
        try {
            String currentDate = dateFormat.format(uvIndexData.getDate());
            DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
            
            if (dayRecord == null) {
                dayRecord = new DayRecord();
                dayRecord.setDate(currentDate);
            }
            
            // Update UV index information in DayRecord
            // This requires updating the DayRecord model first
            
            // Recalculate activity score with UV impact
            dayRecord.calculateActivityScore();
            
            if (dayRecord.getId() > 0) {
                databaseHelper.updateDayRecord(dayRecord);
            } else {
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            Log.d(TAG, "Day record updated with UV index data");
        } catch (Exception e) {
            Log.e(TAG, "Error updating day record with UV index data", e);
        }
    }
    
    /**
     * Get UV safety recommendation for skin type
     */
    public static String getUVSafetyRecommendation(double uvIndex, String skinType) {
        String category = getUVCategory(uvIndex);
        String baseRecommendation = UV_RECOMMENDATIONS.get(category);
        
        float skinFactor = SKIN_TYPE_FACTORS.getOrDefault(skinType, 1.0f);
        
        if (skinFactor < 1.0f) {
            baseRecommendation += " Your fair skin requires extra protection.";
        } else if (skinFactor > 1.3f) {
            baseRecommendation += " Your skin type provides natural protection, but precautions are still recommended.";
        }
        
        return baseRecommendation;
    }
    
    /**
     * Check if UV levels are safe for outdoor activities
     */
    public static boolean isSafeUVForOutdoorActivity(double uvIndex) {
        return uvIndex <= 5; // Low to moderate UV
    }
    
    /**
     * Get UV activity multiplier
     */
    public static float getUVActivityMultiplier(double uvIndex) {
        if (uvIndex <= 2) return 1.0f;
        if (uvIndex <= 5) return 0.9f;
        if (uvIndex <= 7) return 0.7f;
        if (uvIndex <= 10) return 0.5f;
        return 0.3f;
    }
    
    /**
     * Get seasonal UV pattern analysis
     */
    public void getSeasonalUVPattern(double latitude, double longitude, UVIndexCallback callback) {
        executorService.execute(() -> {
            try {
                // Calculate UV patterns for different seasons
                Calendar calendar = Calendar.getInstance();
                
                // Summer solstice (June 21)
                calendar.set(Calendar.MONTH, Calendar.JUNE);
                calendar.set(Calendar.DAY_OF_MONTH, 21);
                UVIndexData summerUV = calculateUVIndex(latitude, longitude, calendar.getTime());
                
                // Winter solstice (December 21)
                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                calendar.set(Calendar.DAY_OF_MONTH, 21);
                UVIndexData winterUV = calculateUVIndex(latitude, longitude, calendar.getTime());
                
                // Return summer data as example
                callback.onUVIndexReceived(summerUV);
                
            } catch (Exception e) {
                Log.e(TAG, "Error calculating seasonal UV pattern", e);
                callback.onError("Failed to calculate seasonal UV pattern: " + e.getMessage());
            }
        });
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}