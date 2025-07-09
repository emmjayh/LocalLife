package com.locallife.service;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
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
 * Service for fetching air quality data from OpenAQ API
 * No API key required - free air quality service
 */
public class AirQualityService {
    private static final String TAG = "AirQualityService";
    private static final String BASE_URL = "https://api.openaq.org/v2/latest";
    private static final String MEASUREMENTS_URL = "https://api.openaq.org/v2/measurements";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Air quality parameters
    private static final Map<String, String> POLLUTANT_NAMES = new HashMap<>();
    static {
        POLLUTANT_NAMES.put("pm25", "PM2.5");
        POLLUTANT_NAMES.put("pm10", "PM10");
        POLLUTANT_NAMES.put("no2", "NO2");
        POLLUTANT_NAMES.put("o3", "O3");
        POLLUTANT_NAMES.put("co", "CO");
        POLLUTANT_NAMES.put("so2", "SO2");
        POLLUTANT_NAMES.put("bc", "Black Carbon");
    }
    
    // AQI breakpoints for PM2.5 (US EPA standard)
    private static final int[] PM25_BREAKPOINTS = {0, 12, 35, 55, 150, 250, 350, 500};
    private static final int[] AQI_BREAKPOINTS = {0, 50, 100, 150, 200, 300, 400, 500};
    
    public interface AirQualityCallback {
        void onAirQualityReceived(AirQualityData airQualityData);
        void onError(String error);
    }
    
    public static class AirQualityData {
        private double latitude;
        private double longitude;
        private String locationName;
        private String city;
        private String country;
        private Date timestamp;
        private Map<String, PollutantData> pollutants;
        private int aqi;
        private String aqiLevel;
        private String healthRecommendation;
        private float activityImpactScore;
        
        public AirQualityData() {
            this.pollutants = new HashMap<>();
            this.timestamp = new Date();
        }
        
        public static class PollutantData {
            private String parameter;
            private double value;
            private String unit;
            private Date lastUpdated;
            private String sourceName;
            
            public PollutantData(String parameter, double value, String unit) {
                this.parameter = parameter;
                this.value = value;
                this.unit = unit;
                this.lastUpdated = new Date();
            }
            
            // Getters and setters
            public String getParameter() { return parameter; }
            public void setParameter(String parameter) { this.parameter = parameter; }
            
            public double getValue() { return value; }
            public void setValue(double value) { this.value = value; }
            
            public String getUnit() { return unit; }
            public void setUnit(String unit) { this.unit = unit; }
            
            public Date getLastUpdated() { return lastUpdated; }
            public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
            
            public String getSourceName() { return sourceName; }
            public void setSourceName(String sourceName) { this.sourceName = sourceName; }
        }
        
        // Getters and setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        
        public Map<String, PollutantData> getPollutants() { return pollutants; }
        public void setPollutants(Map<String, PollutantData> pollutants) { this.pollutants = pollutants; }
        
        public int getAqi() { return aqi; }
        public void setAqi(int aqi) { this.aqi = aqi; }
        
        public String getAqiLevel() { return aqiLevel; }
        public void setAqiLevel(String aqiLevel) { this.aqiLevel = aqiLevel; }
        
        public String getHealthRecommendation() { return healthRecommendation; }
        public void setHealthRecommendation(String healthRecommendation) { this.healthRecommendation = healthRecommendation; }
        
        public float getActivityImpactScore() { return activityImpactScore; }
        public void setActivityImpactScore(float activityImpactScore) { this.activityImpactScore = activityImpactScore; }
        
        @Override
        public String toString() {
            return "AirQualityData{" +
                    "city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", aqi=" + aqi +
                    ", aqiLevel='" + aqiLevel + '\'' +
                    ", pollutants=" + pollutants.size() +
                    '}';
        }
    }
    
    public AirQualityService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Fetch air quality data for given coordinates
     */
    public void getAirQualityData(double latitude, double longitude, AirQualityCallback callback) {
        executorService.execute(() -> {
            try {
                String url = buildAirQualityUrl(latitude, longitude);
                String response = makeHttpRequest(url);
                AirQualityData airQualityData = parseAirQualityResponse(response);
                
                if (airQualityData != null) {
                    airQualityData.setLatitude(latitude);
                    airQualityData.setLongitude(longitude);
                    
                    // Calculate AQI and health recommendations
                    calculateAQI(airQualityData);
                    calculateActivityImpact(airQualityData);
                    
                    // Save to database
                    saveAirQualityToDatabase(airQualityData);
                    
                    // Update day record
                    updateDayRecordWithAirQuality(airQualityData);
                    
                    callback.onAirQualityReceived(airQualityData);
                } else {
                    callback.onError("No air quality data found for location");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching air quality data", e);
                callback.onError("Failed to fetch air quality data: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get nearest air quality stations
     */
    public void getNearestStations(double latitude, double longitude, int radius, AirQualityCallback callback) {
        executorService.execute(() -> {
            try {
                String url = buildStationsUrl(latitude, longitude, radius);
                String response = makeHttpRequest(url);
                
                // Parse and find the best station
                AirQualityData airQualityData = parseStationsResponse(response);
                
                if (airQualityData != null) {
                    callback.onAirQualityReceived(airQualityData);
                } else {
                    callback.onError("No air quality stations found nearby");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching air quality stations", e);
                callback.onError("Failed to fetch air quality stations: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get air quality history for a location
     */
    public void getAirQualityHistory(double latitude, double longitude, int days, AirQualityCallback callback) {
        executorService.execute(() -> {
            try {
                String url = buildHistoryUrl(latitude, longitude, days);
                String response = makeHttpRequest(url);
                
                // This would parse historical data
                // For now, just return current data
                AirQualityData airQualityData = parseAirQualityResponse(response);
                
                if (airQualityData != null) {
                    callback.onAirQualityReceived(airQualityData);
                } else {
                    callback.onError("No historical air quality data found");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching air quality history", e);
                callback.onError("Failed to fetch air quality history: " + e.getMessage());
            }
        });
    }
    
    private String buildAirQualityUrl(double latitude, double longitude) {
        return BASE_URL + "?" +
                "coordinates=" + latitude + "," + longitude +
                "&radius=25000" + // 25km radius
                "&limit=10" +
                "&page=1" +
                "&offset=0" +
                "&sort=desc" +
                "&order_by=lastUpdated";
    }
    
    private String buildStationsUrl(double latitude, double longitude, int radius) {
        return BASE_URL + "?" +
                "coordinates=" + latitude + "," + longitude +
                "&radius=" + radius +
                "&limit=20" +
                "&page=1" +
                "&offset=0" +
                "&sort=desc" +
                "&order_by=distance";
    }
    
    private String buildHistoryUrl(double latitude, double longitude, int days) {
        return MEASUREMENTS_URL + "?" +
                "coordinates=" + latitude + "," + longitude +
                "&radius=25000" +
                "&date_from=" + getDateDaysAgo(days) +
                "&date_to=" + dateFormat.format(new Date()) +
                "&limit=100" +
                "&page=1" +
                "&offset=0" +
                "&sort=desc" +
                "&order_by=datetime";
    }
    
    private String getDateDaysAgo(int days) {
        long daysAgo = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000);
        return dateFormat.format(new Date(daysAgo));
    }
    
    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000); // 15 seconds
            connection.setReadTimeout(15000); // 15 seconds
            connection.setRequestProperty("User-Agent", "LocalLife-Android-App");
            
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
    
    private AirQualityData parseAirQualityResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray results = jsonResponse.getJSONArray("results");
        
        if (results.length() == 0) {
            return null;
        }
        
        AirQualityData airQualityData = new AirQualityData();
        Map<String, AirQualityData.PollutantData> pollutants = new HashMap<>();
        
        // Process all measurements from different stations
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            
            // Get location info from first result
            if (i == 0) {
                airQualityData.setCity(result.optString("city", "Unknown"));
                airQualityData.setCountry(result.optString("country", "Unknown"));
                airQualityData.setLocationName(result.optString("location", "Unknown"));
                
                JSONArray coordinates = result.optJSONArray("coordinates");
                if (coordinates != null && coordinates.length() >= 2) {
                    airQualityData.setLatitude(coordinates.getDouble(1));
                    airQualityData.setLongitude(coordinates.getDouble(0));
                }
            }
            
            // Get measurements
            JSONArray measurements = result.optJSONArray("measurements");
            if (measurements != null) {
                for (int j = 0; j < measurements.length(); j++) {
                    JSONObject measurement = measurements.getJSONObject(j);
                    
                    String parameter = measurement.optString("parameter", "");
                    double value = measurement.optDouble("value", 0.0);
                    String unit = measurement.optString("unit", "");
                    String sourceName = measurement.optString("sourceName", "");
                    
                    if (!parameter.isEmpty() && value > 0) {
                        AirQualityData.PollutantData pollutantData = new AirQualityData.PollutantData(parameter, value, unit);
                        pollutantData.setSourceName(sourceName);
                        
                        // Keep the most recent measurement for each pollutant
                        if (!pollutants.containsKey(parameter) || 
                            pollutants.get(parameter).getValue() < value) {
                            pollutants.put(parameter, pollutantData);
                        }
                    }
                }
            }
        }
        
        airQualityData.setPollutants(pollutants);
        return airQualityData;
    }
    
    private AirQualityData parseStationsResponse(String response) throws JSONException {
        // Similar to parseAirQualityResponse but focused on station data
        return parseAirQualityResponse(response);
    }
    
    private void calculateAQI(AirQualityData airQualityData) {
        int maxAqi = 0;
        String worstPollutant = "";
        
        // Calculate AQI for each pollutant and take the worst
        for (Map.Entry<String, AirQualityData.PollutantData> entry : airQualityData.getPollutants().entrySet()) {
            String parameter = entry.getKey();
            AirQualityData.PollutantData pollutantData = entry.getValue();
            
            int aqi = calculateParameterAQI(parameter, pollutantData.getValue());
            if (aqi > maxAqi) {
                maxAqi = aqi;
                worstPollutant = parameter;
            }
        }
        
        airQualityData.setAqi(maxAqi);
        airQualityData.setAqiLevel(getAQILevel(maxAqi));
        airQualityData.setHealthRecommendation(getHealthRecommendation(maxAqi, worstPollutant));
    }
    
    private int calculateParameterAQI(String parameter, double concentration) {
        // Simplified AQI calculation for PM2.5
        if ("pm25".equals(parameter)) {
            return calculatePM25AQI(concentration);
        } else if ("pm10".equals(parameter)) {
            return calculatePM10AQI(concentration);
        } else if ("no2".equals(parameter)) {
            return calculateNO2AQI(concentration);
        } else if ("o3".equals(parameter)) {
            return calculateO3AQI(concentration);
        } else if ("co".equals(parameter)) {
            return calculateCOAQI(concentration);
        }
        
        return 0; // Unknown parameter
    }
    
    private int calculatePM25AQI(double concentration) {
        if (concentration <= 12) return (int) ((50.0 / 12.0) * concentration);
        if (concentration <= 35) return (int) (50 + ((50.0 / 23.0) * (concentration - 12)));
        if (concentration <= 55) return (int) (100 + ((50.0 / 20.0) * (concentration - 35)));
        if (concentration <= 150) return (int) (150 + ((50.0 / 95.0) * (concentration - 55)));
        if (concentration <= 250) return (int) (200 + ((100.0 / 100.0) * (concentration - 150)));
        if (concentration <= 350) return (int) (300 + ((100.0 / 100.0) * (concentration - 250)));
        return Math.min(500, (int) (400 + ((100.0 / 150.0) * (concentration - 350))));
    }
    
    private int calculatePM10AQI(double concentration) {
        if (concentration <= 54) return (int) ((50.0 / 54.0) * concentration);
        if (concentration <= 154) return (int) (50 + ((50.0 / 100.0) * (concentration - 54)));
        if (concentration <= 254) return (int) (100 + ((50.0 / 100.0) * (concentration - 154)));
        if (concentration <= 354) return (int) (150 + ((50.0 / 100.0) * (concentration - 254)));
        if (concentration <= 424) return (int) (200 + ((100.0 / 70.0) * (concentration - 354)));
        if (concentration <= 504) return (int) (300 + ((100.0 / 80.0) * (concentration - 424)));
        return Math.min(500, (int) (400 + ((100.0 / 96.0) * (concentration - 504))));
    }
    
    private int calculateNO2AQI(double concentration) {
        // Convert ppb to µg/m³ if needed and calculate AQI
        // Simplified calculation
        if (concentration <= 53) return (int) ((50.0 / 53.0) * concentration);
        if (concentration <= 100) return (int) (50 + ((50.0 / 47.0) * (concentration - 53)));
        if (concentration <= 360) return (int) (100 + ((50.0 / 260.0) * (concentration - 100)));
        if (concentration <= 649) return (int) (150 + ((50.0 / 289.0) * (concentration - 360)));
        if (concentration <= 1249) return (int) (200 + ((100.0 / 600.0) * (concentration - 649)));
        return Math.min(500, (int) (300 + ((200.0 / 1000.0) * (concentration - 1249))));
    }
    
    private int calculateO3AQI(double concentration) {
        // 8-hour average O3 AQI calculation
        if (concentration <= 54) return (int) ((50.0 / 54.0) * concentration);
        if (concentration <= 70) return (int) (50 + ((50.0 / 16.0) * (concentration - 54)));
        if (concentration <= 85) return (int) (100 + ((50.0 / 15.0) * (concentration - 70)));
        if (concentration <= 105) return (int) (150 + ((50.0 / 20.0) * (concentration - 85)));
        if (concentration <= 200) return (int) (200 + ((100.0 / 95.0) * (concentration - 105)));
        return Math.min(500, (int) (300 + ((200.0 / 100.0) * (concentration - 200))));
    }
    
    private int calculateCOAQI(double concentration) {
        // 8-hour average CO AQI calculation
        if (concentration <= 4.4) return (int) ((50.0 / 4.4) * concentration);
        if (concentration <= 9.4) return (int) (50 + ((50.0 / 5.0) * (concentration - 4.4)));
        if (concentration <= 12.4) return (int) (100 + ((50.0 / 3.0) * (concentration - 9.4)));
        if (concentration <= 15.4) return (int) (150 + ((50.0 / 3.0) * (concentration - 12.4)));
        if (concentration <= 30.4) return (int) (200 + ((100.0 / 15.0) * (concentration - 15.4)));
        if (concentration <= 40.4) return (int) (300 + ((100.0 / 10.0) * (concentration - 30.4)));
        return Math.min(500, (int) (400 + ((100.0 / 10.0) * (concentration - 40.4))));
    }
    
    private String getAQILevel(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }
    
    private String getHealthRecommendation(int aqi, String worstPollutant) {
        if (aqi <= 50) {
            return "Air quality is good. Ideal for outdoor activities.";
        } else if (aqi <= 100) {
            return "Air quality is moderate. Sensitive individuals should consider reducing outdoor activities.";
        } else if (aqi <= 150) {
            return "Unhealthy for sensitive groups. Consider indoor activities if you have respiratory conditions.";
        } else if (aqi <= 200) {
            return "Unhealthy air quality. Limit outdoor activities and consider wearing a mask.";
        } else if (aqi <= 300) {
            return "Very unhealthy air quality. Avoid outdoor activities and stay indoors.";
        } else {
            return "Hazardous air quality. Stay indoors and avoid all outdoor activities.";
        }
    }
    
    private void calculateActivityImpact(AirQualityData airQualityData) {
        int aqi = airQualityData.getAqi();
        float activityImpact;
        
        if (aqi <= 50) {
            activityImpact = 1.0f; // No impact
        } else if (aqi <= 100) {
            activityImpact = 0.9f; // Minor impact
        } else if (aqi <= 150) {
            activityImpact = 0.7f; // Moderate impact
        } else if (aqi <= 200) {
            activityImpact = 0.5f; // Significant impact
        } else if (aqi <= 300) {
            activityImpact = 0.3f; // High impact
        } else {
            activityImpact = 0.1f; // Severe impact
        }
        
        airQualityData.setActivityImpactScore(activityImpact);
    }
    
    private void saveAirQualityToDatabase(AirQualityData airQualityData) {
        try {
            String currentDate = dateFormat.format(airQualityData.getTimestamp());
            
            // This would save to a dedicated air quality table
            // For now, we'll add this functionality to DatabaseHelper
            databaseHelper.insertAirQualityData(
                    currentDate,
                    airQualityData.getLatitude(),
                    airQualityData.getLongitude(),
                    airQualityData.getCity(),
                    airQualityData.getCountry(),
                    airQualityData.getAqi(),
                    airQualityData.getAqiLevel(),
                    airQualityData.getPollutants(),
                    airQualityData.getActivityImpactScore()
            );
            
            Log.d(TAG, "Air quality data saved to database");
        } catch (Exception e) {
            Log.e(TAG, "Error saving air quality data to database", e);
        }
    }
    
    private void updateDayRecordWithAirQuality(AirQualityData airQualityData) {
        try {
            String currentDate = dateFormat.format(airQualityData.getTimestamp());
            DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
            
            if (dayRecord == null) {
                dayRecord = new DayRecord();
                dayRecord.setDate(currentDate);
            }
            
            // Update air quality information in DayRecord
            // This requires updating the DayRecord model first
            
            // Recalculate activity score with air quality impact
            dayRecord.calculateActivityScore();
            
            if (dayRecord.getId() > 0) {
                databaseHelper.updateDayRecord(dayRecord);
            } else {
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            Log.d(TAG, "Day record updated with air quality data");
        } catch (Exception e) {
            Log.e(TAG, "Error updating day record with air quality data", e);
        }
    }
    
    /**
     * Get air quality impact on outdoor activities
     */
    public static boolean isGoodAirQualityForOutdoorActivity(AirQualityData airQualityData) {
        return airQualityData.getAqi() <= 100; // Good to moderate air quality
    }
    
    /**
     * Get air quality activity multiplier for activity score calculation
     */
    public static float getAirQualityActivityMultiplier(AirQualityData airQualityData) {
        return airQualityData.getActivityImpactScore();
    }
    
    /**
     * Get pollutant-specific health recommendations
     */
    public static String getPollutantHealthRecommendation(String pollutant, double concentration) {
        switch (pollutant) {
            case "pm25":
                if (concentration > 35) return "High PM2.5 levels. Avoid outdoor exercise.";
                if (concentration > 12) return "Moderate PM2.5 levels. Consider indoor activities.";
                return "Good PM2.5 levels. Safe for outdoor activities.";
            case "pm10":
                if (concentration > 154) return "High PM10 levels. Limit outdoor exposure.";
                if (concentration > 54) return "Moderate PM10 levels. Sensitive groups should be cautious.";
                return "Good PM10 levels. Safe for outdoor activities.";
            case "no2":
                if (concentration > 100) return "High NO2 levels. Avoid busy roads and traffic.";
                if (concentration > 53) return "Moderate NO2 levels. Limit exposure near traffic.";
                return "Good NO2 levels. Safe for outdoor activities.";
            case "o3":
                if (concentration > 85) return "High ozone levels. Avoid outdoor activities during peak hours.";
                if (concentration > 70) return "Moderate ozone levels. Consider indoor exercise.";
                return "Good ozone levels. Safe for outdoor activities.";
            case "co":
                if (concentration > 12.4) return "High CO levels. Ensure good ventilation.";
                if (concentration > 9.4) return "Moderate CO levels. Avoid enclosed spaces with poor ventilation.";
                return "Good CO levels. Safe conditions.";
            default:
                return "Monitor air quality levels for safety.";
        }
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}