package com.locallife.service;

import android.content.Context;
import android.location.Location;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

/**
 * Service for fetching weather data from Open-Meteo API
 * No API key required - free weather service
 */
public class WeatherService {
    private static final String TAG = "WeatherService";
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String CURRENT_WEATHER_URL = "https://api.open-meteo.com/v1/current";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Weather code mappings
    private static final Map<Integer, String> WEATHER_CODES = new HashMap<>();
    static {
        WEATHER_CODES.put(0, "Clear sky");
        WEATHER_CODES.put(1, "Mainly clear");
        WEATHER_CODES.put(2, "Partly cloudy");
        WEATHER_CODES.put(3, "Overcast");
        WEATHER_CODES.put(45, "Fog");
        WEATHER_CODES.put(48, "Depositing rime fog");
        WEATHER_CODES.put(51, "Light drizzle");
        WEATHER_CODES.put(53, "Moderate drizzle");
        WEATHER_CODES.put(55, "Dense drizzle");
        WEATHER_CODES.put(56, "Light freezing drizzle");
        WEATHER_CODES.put(57, "Dense freezing drizzle");
        WEATHER_CODES.put(61, "Slight rain");
        WEATHER_CODES.put(63, "Moderate rain");
        WEATHER_CODES.put(65, "Heavy rain");
        WEATHER_CODES.put(66, "Light freezing rain");
        WEATHER_CODES.put(67, "Heavy freezing rain");
        WEATHER_CODES.put(71, "Slight snow fall");
        WEATHER_CODES.put(73, "Moderate snow fall");
        WEATHER_CODES.put(75, "Heavy snow fall");
        WEATHER_CODES.put(77, "Snow grains");
        WEATHER_CODES.put(80, "Slight rain showers");
        WEATHER_CODES.put(81, "Moderate rain showers");
        WEATHER_CODES.put(82, "Violent rain showers");
        WEATHER_CODES.put(85, "Slight snow showers");
        WEATHER_CODES.put(86, "Heavy snow showers");
        WEATHER_CODES.put(95, "Thunderstorm");
        WEATHER_CODES.put(96, "Thunderstorm with slight hail");
        WEATHER_CODES.put(99, "Thunderstorm with heavy hail");
    }
    
    public interface WeatherCallback {
        void onWeatherReceived(WeatherData weatherData);
        void onError(String error);
    }
    
    public static class WeatherData {
        private float temperature;
        private float humidity;
        private String condition;
        private int weatherCode;
        private float windSpeed;
        private float precipitation;
        private float cloudCover;
        private String location;
        private Date timestamp;
        
        public WeatherData() {
            this.timestamp = new Date();
        }
        
        // Getters and setters
        public float getTemperature() { return temperature; }
        public void setTemperature(float temperature) { this.temperature = temperature; }
        
        public float getHumidity() { return humidity; }
        public void setHumidity(float humidity) { this.humidity = humidity; }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        
        public int getWeatherCode() { return weatherCode; }
        public void setWeatherCode(int weatherCode) { 
            this.weatherCode = weatherCode;
            this.condition = WEATHER_CODES.getOrDefault(weatherCode, "Unknown");
        }
        
        public float getWindSpeed() { return windSpeed; }
        public void setWindSpeed(float windSpeed) { this.windSpeed = windSpeed; }
        
        public float getPrecipitation() { return precipitation; }
        public void setPrecipitation(float precipitation) { this.precipitation = precipitation; }
        
        public float getCloudCover() { return cloudCover; }
        public void setCloudCover(float cloudCover) { this.cloudCover = cloudCover; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return "WeatherData{" +
                    "temperature=" + temperature +
                    ", humidity=" + humidity +
                    ", condition='" + condition + '\'' +
                    ", windSpeed=" + windSpeed +
                    ", precipitation=" + precipitation +
                    ", location='" + location + '\'' +
                    '}';
        }
    }
    
    public WeatherService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Fetch current weather for given location
     */
    public void getCurrentWeather(double latitude, double longitude, WeatherCallback callback) {
        executorService.execute(() -> {
            try {
                String url = buildCurrentWeatherUrl(latitude, longitude);
                String response = makeHttpRequest(url);
                WeatherData weatherData = parseCurrentWeatherResponse(response);
                weatherData.setLocation(latitude + "," + longitude);
                
                // Save to database
                saveWeatherToDatabase(weatherData);
                
                // Update day record
                updateDayRecordWithWeather(weatherData);
                
                callback.onWeatherReceived(weatherData);
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching current weather", e);
                callback.onError("Failed to fetch weather data: " + e.getMessage());
            }
        });
    }
    
    /**
     * Fetch weather forecast for given location
     */
    public void getWeatherForecast(double latitude, double longitude, int days, WeatherCallback callback) {
        executorService.execute(() -> {
            try {
                String url = buildForecastUrl(latitude, longitude, days);
                String response = makeHttpRequest(url);
                WeatherData weatherData = parseCurrentWeatherFromForecast(response);
                weatherData.setLocation(latitude + "," + longitude);
                
                // Save to database
                saveWeatherToDatabase(weatherData);
                
                // Update day record
                updateDayRecordWithWeather(weatherData);
                
                callback.onWeatherReceived(weatherData);
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather forecast", e);
                callback.onError("Failed to fetch weather forecast: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get weather for current location (if available)
     */
    public void getCurrentLocationWeather(WeatherCallback callback) {
        // This would typically use LocationService to get current location
        // For now, we'll use a default location (can be updated when LocationService is integrated)
        
        // Example coordinates (San Francisco)
        double defaultLat = 37.7749;
        double defaultLng = -122.4194;
        
        getCurrentWeather(defaultLat, defaultLng, callback);
    }
    
    private String buildCurrentWeatherUrl(double latitude, double longitude) {
        return BASE_URL + "?" +
                "latitude=" + latitude +
                "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,precipitation,cloud_cover" +
                "&timezone=auto";
    }
    
    private String buildForecastUrl(double latitude, double longitude, int days) {
        return BASE_URL + "?" +
                "latitude=" + latitude +
                "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,precipitation,cloud_cover" +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code" +
                "&forecast_days=" + days +
                "&timezone=auto";
    }
    
    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds
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
    
    private WeatherData parseCurrentWeatherResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        JSONObject current = jsonResponse.getJSONObject("current");
        
        WeatherData weatherData = new WeatherData();
        weatherData.setTemperature((float) current.getDouble("temperature_2m"));
        weatherData.setHumidity((float) current.getDouble("relative_humidity_2m"));
        weatherData.setWeatherCode(current.getInt("weather_code"));
        weatherData.setWindSpeed((float) current.getDouble("wind_speed_10m"));
        weatherData.setPrecipitation((float) current.optDouble("precipitation", 0.0));
        weatherData.setCloudCover((float) current.optDouble("cloud_cover", 0.0));
        
        return weatherData;
    }
    
    private WeatherData parseCurrentWeatherFromForecast(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        
        // Get current weather from forecast response
        if (jsonResponse.has("current")) {
            JSONObject current = jsonResponse.getJSONObject("current");
            
            WeatherData weatherData = new WeatherData();
            weatherData.setTemperature((float) current.getDouble("temperature_2m"));
            weatherData.setHumidity((float) current.getDouble("relative_humidity_2m"));
            weatherData.setWeatherCode(current.getInt("weather_code"));
            weatherData.setWindSpeed((float) current.getDouble("wind_speed_10m"));
            weatherData.setPrecipitation((float) current.optDouble("precipitation", 0.0));
            weatherData.setCloudCover((float) current.optDouble("cloud_cover", 0.0));
            
            return weatherData;
        }
        
        throw new JSONException("No current weather data in forecast response");
    }
    
    private void saveWeatherToDatabase(WeatherData weatherData) {
        try {
            String currentDate = dateFormat.format(weatherData.getTimestamp());
            
            databaseHelper.insertWeatherData(
                    currentDate,
                    weatherData.getLocation(),
                    weatherData.getTemperature(),
                    weatherData.getHumidity(),
                    weatherData.getCondition(),
                    weatherData.getWeatherCode(),
                    weatherData.getWindSpeed(),
                    weatherData.getPrecipitation(),
                    weatherData.getCloudCover()
            );
            
            Log.d(TAG, "Weather data saved to database");
        } catch (Exception e) {
            Log.e(TAG, "Error saving weather data to database", e);
        }
    }
    
    private void updateDayRecordWithWeather(WeatherData weatherData) {
        try {
            String currentDate = dateFormat.format(weatherData.getTimestamp());
            DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
            
            if (dayRecord == null) {
                dayRecord = new DayRecord();
                dayRecord.setDate(currentDate);
            }
            
            // Update weather information
            dayRecord.setTemperature(weatherData.getTemperature());
            dayRecord.setHumidity(weatherData.getHumidity());
            dayRecord.setWeatherCondition(weatherData.getCondition());
            dayRecord.setWindSpeed(weatherData.getWindSpeed());
            
            // Recalculate activity score
            dayRecord.calculateActivityScore();
            
            if (dayRecord.getId() > 0) {
                databaseHelper.updateDayRecord(dayRecord);
            } else {
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            Log.d(TAG, "Day record updated with weather data");
        } catch (Exception e) {
            Log.e(TAG, "Error updating day record with weather data", e);
        }
    }
    
    /**
     * Get weather condition description from weather code
     */
    public static String getWeatherCondition(int weatherCode) {
        return WEATHER_CODES.getOrDefault(weatherCode, "Unknown");
    }
    
    /**
     * Check if weather conditions are good for outdoor activities
     */
    public static boolean isGoodWeatherForOutdoorActivity(WeatherData weatherData) {
        // Good weather criteria:
        // - Temperature between 10-30°C
        // - No precipitation or light precipitation
        // - Wind speed < 25 km/h
        // - Weather code indicates clear/partly cloudy conditions
        
        float temp = weatherData.getTemperature();
        float precipitation = weatherData.getPrecipitation();
        float windSpeed = weatherData.getWindSpeed();
        int weatherCode = weatherData.getWeatherCode();
        
        boolean goodTemperature = temp >= 10 && temp <= 30;
        boolean lowPrecipitation = precipitation <= 2.0; // Less than 2mm
        boolean moderateWind = windSpeed <= 25; // Less than 25 km/h
        boolean clearWeather = weatherCode <= 3 || weatherCode == 51; // Clear, partly cloudy, or light drizzle
        
        return goodTemperature && lowPrecipitation && moderateWind && clearWeather;
    }
    
    /**
     * Get weather impact on activity score
     */
    public static float getWeatherActivityMultiplier(WeatherData weatherData) {
        if (isGoodWeatherForOutdoorActivity(weatherData)) {
            return 1.2f; // Boost activity score by 20% for good weather
        } else if (weatherData.getPrecipitation() > 5.0 || weatherData.getWindSpeed() > 40) {
            return 0.8f; // Reduce activity score by 20% for bad weather
        }
        return 1.0f; // Normal weather
    }
    
    /**
     * Schedule periodic weather updates
     */
    public void schedulePeriodicWeatherUpdates(double latitude, double longitude, long intervalMs) {
        executorService.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    getCurrentWeather(latitude, longitude, new WeatherCallback() {
                        @Override
                        public void onWeatherReceived(WeatherData weatherData) {
                            Log.d(TAG, "Periodic weather update: " + weatherData);
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Periodic weather update failed: " + error);
                        }
                    });
                    
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Error in periodic weather update", e);
                }
            }
        });
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}