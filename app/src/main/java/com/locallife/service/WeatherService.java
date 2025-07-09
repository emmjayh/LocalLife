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
        private float atmosphericPressure;
        private float visibility;
        private int windDirection;
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
        
        public float getAtmosphericPressure() { return atmosphericPressure; }
        public void setAtmosphericPressure(float atmosphericPressure) { this.atmosphericPressure = atmosphericPressure; }
        
        public float getVisibility() { return visibility; }
        public void setVisibility(float visibility) { this.visibility = visibility; }
        
        public int getWindDirection() { return windDirection; }
        public void setWindDirection(int windDirection) { this.windDirection = windDirection; }
        
        @Override
        public String toString() {
            return "WeatherData{" +
                    "temperature=" + temperature +
                    ", humidity=" + humidity +
                    ", condition='" + condition + '\'' +
                    ", windSpeed=" + windSpeed +
                    ", precipitation=" + precipitation +
                    ", cloudCover=" + cloudCover +
                    ", atmosphericPressure=" + atmosphericPressure +
                    ", visibility=" + visibility +
                    ", windDirection=" + windDirection +
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
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,precipitation,cloud_cover,surface_pressure,visibility" +
                "&timezone=auto";
    }
    
    private String buildForecastUrl(double latitude, double longitude, int days) {
        return BASE_URL + "?" +
                "latitude=" + latitude +
                "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,precipitation,cloud_cover,surface_pressure,visibility" +
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
        weatherData.setWindDirection((int) current.optDouble("wind_direction_10m", 0));
        weatherData.setPrecipitation((float) current.optDouble("precipitation", 0.0));
        weatherData.setCloudCover((float) current.optDouble("cloud_cover", 0.0));
        weatherData.setAtmosphericPressure((float) current.optDouble("surface_pressure", 1013.25));
        weatherData.setVisibility((float) current.optDouble("visibility", 10000.0));
        
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
            weatherData.setWindDirection((int) current.optDouble("wind_direction_10m", 0));
            weatherData.setPrecipitation((float) current.optDouble("precipitation", 0.0));
            weatherData.setCloudCover((float) current.optDouble("cloud_cover", 0.0));
            weatherData.setAtmosphericPressure((float) current.optDouble("surface_pressure", 1013.25));
            weatherData.setVisibility((float) current.optDouble("visibility", 10000.0));
            
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
     * Get wind direction as compass direction string
     */
    public static String getWindDirectionString(int degrees) {
        if (degrees < 0) degrees += 360;
        degrees = degrees % 360;
        
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                               "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        
        int index = (int) Math.round(degrees / 22.5) % 16;
        return directions[index];
    }
    
    /**
     * Get atmospheric pressure category
     */
    public static String getAtmosphericPressureCategory(float pressure) {
        if (pressure < 1009) return "Low";
        if (pressure > 1019) return "High";
        return "Normal";
    }
    
    /**
     * Get visibility category
     */
    public static String getVisibilityCategory(float visibility) {
        if (visibility < 1000) return "Very Poor";
        if (visibility < 4000) return "Poor";
        if (visibility < 10000) return "Moderate";
        return "Good";
    }
    
    /**
     * Check if weather conditions are good for outdoor activities
     */
    public static boolean isGoodWeatherForOutdoorActivity(WeatherData weatherData) {
        // Enhanced good weather criteria:
        // - Temperature between 10-30°C
        // - No precipitation or light precipitation
        // - Wind speed < 25 km/h
        // - Weather code indicates clear/partly cloudy conditions
        // - Good visibility (>4000m)
        // - Normal atmospheric pressure (1009-1019 hPa)
        
        float temp = weatherData.getTemperature();
        float precipitation = weatherData.getPrecipitation();
        float windSpeed = weatherData.getWindSpeed();
        int weatherCode = weatherData.getWeatherCode();
        float visibility = weatherData.getVisibility();
        float pressure = weatherData.getAtmosphericPressure();
        
        boolean goodTemperature = temp >= 10 && temp <= 30;
        boolean lowPrecipitation = precipitation <= 2.0; // Less than 2mm
        boolean moderateWind = windSpeed <= 25; // Less than 25 km/h
        boolean clearWeather = weatherCode <= 3 || weatherCode == 51; // Clear, partly cloudy, or light drizzle
        boolean goodVisibility = visibility >= 4000; // Good visibility
        boolean stablePressure = pressure >= 1009 && pressure <= 1019; // Stable pressure
        
        return goodTemperature && lowPrecipitation && moderateWind && clearWeather && goodVisibility && stablePressure;
    }
    
    /**
     * Get weather impact on activity score
     */
    public static float getWeatherActivityMultiplier(WeatherData weatherData) {
        float multiplier = 1.0f;
        
        // Temperature impact
        float temp = weatherData.getTemperature();
        if (temp >= 15 && temp <= 25) {
            multiplier *= 1.1f; // Ideal temperature
        } else if (temp < 0 || temp > 35) {
            multiplier *= 0.7f; // Extreme temperature
        }
        
        // Precipitation impact
        float precipitation = weatherData.getPrecipitation();
        if (precipitation > 5.0) {
            multiplier *= 0.6f; // Heavy rain
        } else if (precipitation > 2.0) {
            multiplier *= 0.8f; // Moderate rain
        }
        
        // Wind impact
        float windSpeed = weatherData.getWindSpeed();
        if (windSpeed > 40) {
            multiplier *= 0.5f; // Very windy
        } else if (windSpeed > 25) {
            multiplier *= 0.8f; // Windy
        }
        
        // Visibility impact
        float visibility = weatherData.getVisibility();
        if (visibility < 1000) {
            multiplier *= 0.6f; // Poor visibility
        } else if (visibility < 4000) {
            multiplier *= 0.8f; // Limited visibility
        }
        
        // Atmospheric pressure impact
        float pressure = weatherData.getAtmosphericPressure();
        if (pressure < 1000 || pressure > 1025) {
            multiplier *= 0.9f; // Unstable pressure
        }
        
        return Math.max(0.3f, Math.min(1.5f, multiplier)); // Clamp between 0.3 and 1.5
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