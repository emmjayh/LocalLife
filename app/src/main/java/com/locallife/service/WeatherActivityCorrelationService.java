package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.ActivityType;
import com.locallife.model.DayRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to analyze correlations between weather conditions and user activities
 */
public class WeatherActivityCorrelationService {
    private static final String TAG = "WeatherActivityCorrelation";
    private static final double MINIMUM_CORRELATION_THRESHOLD = 0.1;
    private static final int MIN_DATA_POINTS = 10;
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private Map<String, WeatherActivityCorrelation> correlationCache;
    private Map<ActivityType, WeatherPreferences> activityWeatherPreferences;
    
    public WeatherActivityCorrelationService(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.correlationCache = new HashMap<>();
        this.activityWeatherPreferences = new HashMap<>();
        
        // Initialize correlation analysis
        initializeCorrelationAnalysis();
    }
    
    /**
     * Initialize correlation analysis with historical data
     */
    private void initializeCorrelationAnalysis() {
        new Thread(() -> {
            try {
                analyzeHistoricalCorrelations();
                Log.d(TAG, "Weather-activity correlation analysis completed");
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing weather-activity correlations", e);
            }
        }).start();
    }
    
    /**
     * Analyze historical correlations between weather and activities
     */
    private void analyzeHistoricalCorrelations() {
        List<DayRecord> records = databaseHelper.getAllDayRecords();
        
        if (records.size() < MIN_DATA_POINTS) {
            Log.w(TAG, "Insufficient data for correlation analysis");
            return;
        }
        
        Log.d(TAG, "Analyzing correlations with " + records.size() + " data points");
        
        // Analyze correlations for each activity type
        for (ActivityType activityType : ActivityType.values()) {
            analyzeActivityWeatherCorrelation(activityType, records);
        }
        
        // Analyze general weather patterns
        analyzeWeatherPatterns(records);
        
        // Cache weather activity correlations
        cacheWeatherActivityCorrelations(records);
    }
    
    /**
     * Analyze weather correlation for a specific activity type
     */
    private void analyzeActivityWeatherCorrelation(ActivityType activityType, List<DayRecord> records) {
        WeatherPreferences preferences = new WeatherPreferences();
        
        // Group records by activity level for this type
        List<DayRecord> highActivityDays = new ArrayList<>();
        List<DayRecord> lowActivityDays = new ArrayList<>();
        
        for (DayRecord record : records) {
            double activityLevel = getActivityLevelForType(activityType, record);
            if (activityLevel > 0.7) {
                highActivityDays.add(record);
            } else if (activityLevel < 0.3) {
                lowActivityDays.add(record);
            }
        }
        
        if (highActivityDays.size() < 3 || lowActivityDays.size() < 3) {
            // Not enough data for this activity type
            return;
        }
        
        // Analyze temperature preferences
        preferences.setOptimalTemperatureRange(
            calculateOptimalTemperatureRange(highActivityDays, lowActivityDays)
        );
        
        // Analyze weather condition preferences
        preferences.setWeatherConditionPreferences(
            analyzeWeatherConditionPreferences(highActivityDays, lowActivityDays)
        );
        
        // Analyze humidity preferences
        preferences.setOptimalHumidityRange(
            calculateOptimalHumidityRange(highActivityDays, lowActivityDays)
        );
        
        // Analyze wind speed preferences
        preferences.setOptimalWindSpeedRange(
            calculateOptimalWindSpeedRange(highActivityDays, lowActivityDays)
        );
        
        // Analyze UV index preferences
        preferences.setOptimalUVRange(
            calculateOptimalUVRange(highActivityDays, lowActivityDays)
        );
        
        // Calculate correlation strength
        preferences.setCorrelationStrength(
            calculateCorrelationStrength(highActivityDays, lowActivityDays)
        );
        
        activityWeatherPreferences.put(activityType, preferences);
    }
    
    /**
     * Get activity level for a specific activity type from a day record
     */
    private double getActivityLevelForType(ActivityType activityType, DayRecord record) {
        // This is a simplified mapping - in a real implementation,
        // you'd need more sophisticated activity classification
        switch (activityType) {
            case OUTDOOR_EXERCISE:
                return Math.min(1.0, record.getStepCount() / 15000.0);
            case INDOOR_EXERCISE:
                return Math.min(1.0, record.getActiveMinutes() / 60.0);
            case SOCIAL_ACTIVITY:
                return Math.min(1.0, record.getPlacesVisited() / 5.0);
            case WORK_PRODUCTIVITY:
                return Math.min(1.0, record.getProductivityScore() / 100.0);
            case RECREATIONAL:
                return Math.min(1.0, record.getPhotoCount() / 10.0);
            case RELAXATION:
                return Math.min(1.0, (480 - record.getScreenTimeMinutes()) / 480.0);
            case TRAVEL:
                return Math.min(1.0, record.getTotalTravelDistance() / 10000.0);
            case PHOTOGRAPHY:
                return Math.min(1.0, record.getPhotoCount() / 20.0);
            case INDOOR_ACTIVITIES:
                return Math.min(1.0, record.getScreenTimeMinutes() / 360.0);
            case OUTDOOR_LEISURE:
                return Math.min(1.0, (record.getStepCount() / 10000.0 + record.getPlacesVisited() / 3.0) / 2.0);
            default:
                return 0.5;
        }
    }
    
    /**
     * Calculate optimal temperature range for an activity
     */
    private TemperatureRange calculateOptimalTemperatureRange(List<DayRecord> highActivityDays, List<DayRecord> lowActivityDays) {
        double highAvg = highActivityDays.stream().mapToDouble(DayRecord::getTemperature).average().orElse(20.0);
        double lowAvg = lowActivityDays.stream().mapToDouble(DayRecord::getTemperature).average().orElse(20.0);
        
        double highStd = calculateStandardDeviation(highActivityDays.stream().mapToDouble(DayRecord::getTemperature).toArray());
        
        double optimalMin = highAvg - highStd;
        double optimalMax = highAvg + highStd;
        
        return new TemperatureRange(optimalMin, optimalMax, highAvg);
    }
    
    /**
     * Analyze weather condition preferences
     */
    private Map<String, Double> analyzeWeatherConditionPreferences(List<DayRecord> highActivityDays, List<DayRecord> lowActivityDays) {
        Map<String, Double> preferences = new HashMap<>();
        
        // Count weather conditions in high activity days
        Map<String, Long> highConditionCounts = highActivityDays.stream()
            .collect(Collectors.groupingBy(
                record -> record.getWeatherCondition() != null ? record.getWeatherCondition().toLowerCase() : "unknown",
                Collectors.counting()
            ));
        
        // Count weather conditions in low activity days
        Map<String, Long> lowConditionCounts = lowActivityDays.stream()
            .collect(Collectors.groupingBy(
                record -> record.getWeatherCondition() != null ? record.getWeatherCondition().toLowerCase() : "unknown",
                Collectors.counting()
            ));
        
        // Calculate preference scores
        for (String condition : highConditionCounts.keySet()) {
            long highCount = highConditionCounts.get(condition);
            long lowCount = lowConditionCounts.getOrDefault(condition, 0L);
            
            double totalHigh = highActivityDays.size();
            double totalLow = lowActivityDays.size();
            
            double highRatio = highCount / totalHigh;
            double lowRatio = lowCount / totalLow;
            
            double preference = (highRatio - lowRatio) / (highRatio + lowRatio + 0.1); // Add small epsilon
            preferences.put(condition, preference);
        }
        
        return preferences;
    }
    
    /**
     * Calculate optimal humidity range
     */
    private HumidityRange calculateOptimalHumidityRange(List<DayRecord> highActivityDays, List<DayRecord> lowActivityDays) {
        double highAvg = highActivityDays.stream().mapToDouble(DayRecord::getHumidity).average().orElse(50.0);
        double lowAvg = lowActivityDays.stream().mapToDouble(DayRecord::getHumidity).average().orElse(50.0);
        
        double highStd = calculateStandardDeviation(highActivityDays.stream().mapToDouble(DayRecord::getHumidity).toArray());
        
        double optimalMin = Math.max(0, highAvg - highStd);
        double optimalMax = Math.min(100, highAvg + highStd);
        
        return new HumidityRange(optimalMin, optimalMax, highAvg);
    }
    
    /**
     * Calculate optimal wind speed range
     */
    private WindSpeedRange calculateOptimalWindSpeedRange(List<DayRecord> highActivityDays, List<DayRecord> lowActivityDays) {
        double highAvg = highActivityDays.stream().mapToDouble(DayRecord::getWindSpeed).average().orElse(10.0);
        double lowAvg = lowActivityDays.stream().mapToDouble(DayRecord::getWindSpeed).average().orElse(10.0);
        
        double highStd = calculateStandardDeviation(highActivityDays.stream().mapToDouble(DayRecord::getWindSpeed).toArray());
        
        double optimalMin = Math.max(0, highAvg - highStd);
        double optimalMax = highAvg + highStd;
        
        return new WindSpeedRange(optimalMin, optimalMax, highAvg);
    }
    
    /**
     * Calculate optimal UV range
     */
    private UVRange calculateOptimalUVRange(List<DayRecord> highActivityDays, List<DayRecord> lowActivityDays) {
        double highAvg = highActivityDays.stream().mapToDouble(DayRecord::getUvIndex).average().orElse(5.0);
        double lowAvg = lowActivityDays.stream().mapToDouble(DayRecord::getUvIndex).average().orElse(5.0);
        
        double highStd = calculateStandardDeviation(highActivityDays.stream().mapToDouble(DayRecord::getUvIndex).toArray());
        
        double optimalMin = Math.max(0, highAvg - highStd);
        double optimalMax = highAvg + highStd;
        
        return new UVRange(optimalMin, optimalMax, highAvg);
    }
    
    /**
     * Calculate correlation strength between weather and activity
     */
    private double calculateCorrelationStrength(List<DayRecord> highActivityDays, List<DayRecord> lowActivityDays) {
        // Simplified correlation strength calculation
        if (highActivityDays.size() < 3 || lowActivityDays.size() < 3) {
            return 0.0;
        }
        
        double tempCorr = Math.abs(
            highActivityDays.stream().mapToDouble(DayRecord::getTemperature).average().orElse(20.0) -
            lowActivityDays.stream().mapToDouble(DayRecord::getTemperature).average().orElse(20.0)
        ) / 30.0; // Normalize by typical temperature range
        
        double humidityCorr = Math.abs(
            highActivityDays.stream().mapToDouble(DayRecord::getHumidity).average().orElse(50.0) -
            lowActivityDays.stream().mapToDouble(DayRecord::getHumidity).average().orElse(50.0)
        ) / 100.0; // Normalize by humidity range
        
        return Math.min(1.0, (tempCorr + humidityCorr) / 2.0);
    }
    
    /**
     * Analyze general weather patterns
     */
    private void analyzeWeatherPatterns(List<DayRecord> records) {
        // Analyze seasonal patterns
        Map<String, List<DayRecord>> seasonalData = records.stream()
            .collect(Collectors.groupingBy(record -> record.getSeason() != null ? record.getSeason() : "unknown"));
        
        // Analyze daily patterns
        // This would require more sophisticated time-based analysis
        
        // Store patterns for future use
        Log.d(TAG, "Analyzed weather patterns for " + seasonalData.size() + " seasons");
    }
    
    /**
     * Cache weather activity correlations for quick lookup
     */
    private void cacheWeatherActivityCorrelations(List<DayRecord> records) {
        // Create correlation cache for temperature ranges
        for (int temp = -20; temp <= 50; temp += 5) {
            for (int humidity = 20; humidity <= 80; humidity += 20) {
                String key = String.format("temp_%d_humidity_%d", temp, humidity);
                WeatherActivityCorrelation correlation = calculateCorrelationForConditions(records, temp, humidity);
                correlationCache.put(key, correlation);
            }
        }
    }
    
    /**
     * Calculate correlation for specific weather conditions
     */
    private WeatherActivityCorrelation calculateCorrelationForConditions(List<DayRecord> records, int temperature, int humidity) {
        WeatherActivityCorrelation correlation = new WeatherActivityCorrelation();
        
        // Filter records close to the specified conditions
        List<DayRecord> matchingRecords = records.stream()
            .filter(record -> Math.abs(record.getTemperature() - temperature) <= 5 &&
                             Math.abs(record.getHumidity() - humidity) <= 15)
            .collect(Collectors.toList());
        
        if (matchingRecords.size() < 3) {
            // Not enough data for reliable correlation
            return correlation;
        }
        
        // Calculate activity scores for each activity type
        for (ActivityType activityType : ActivityType.values()) {
            double avgActivityLevel = matchingRecords.stream()
                .mapToDouble(record -> getActivityLevelForType(activityType, record))
                .average()
                .orElse(0.0);
            
            correlation.setActivityScore(activityType, avgActivityLevel);
        }
        
        correlation.setTemperature(temperature);
        correlation.setHumidity(humidity);
        correlation.setDataPoints(matchingRecords.size());
        
        return correlation;
    }
    
    /**
     * Get weather-activity correlations for current conditions
     */
    public Map<ActivityType, Double> getWeatherActivityCorrelations(float temperature, float humidity, 
                                                                   String weatherCondition, float windSpeed) {
        Map<ActivityType, Double> correlations = new HashMap<>();
        
        // Find the closest cached correlation
        String cacheKey = findClosestCacheKey(temperature, humidity);
        WeatherActivityCorrelation cachedCorrelation = correlationCache.get(cacheKey);
        
        if (cachedCorrelation != null) {
            correlations.putAll(cachedCorrelation.getActivityScores());
        } else {
            // Fallback to weather suitability scores
            for (ActivityType activityType : ActivityType.values()) {
                double suitability = activityType.getWeatherSuitability(temperature, weatherCondition, humidity, windSpeed, 0.0);
                correlations.put(activityType, suitability);
            }
        }
        
        // Apply weather condition adjustments
        if (weatherCondition != null) {
            applyWeatherConditionAdjustments(correlations, weatherCondition);
        }
        
        return correlations;
    }
    
    /**
     * Find closest cache key for given conditions
     */
    private String findClosestCacheKey(float temperature, float humidity) {
        int tempRounded = (int) (Math.round(temperature / 5.0) * 5);
        int humidityRounded = (int) (Math.round(humidity / 20.0) * 20);
        
        // Clamp values to cache ranges
        tempRounded = Math.max(-20, Math.min(50, tempRounded));
        humidityRounded = Math.max(20, Math.min(80, humidityRounded));
        
        return String.format("temp_%d_humidity_%d", tempRounded, humidityRounded);
    }
    
    /**
     * Apply weather condition adjustments to correlations
     */
    private void applyWeatherConditionAdjustments(Map<ActivityType, Double> correlations, String weatherCondition) {
        String condition = weatherCondition.toLowerCase();
        
        for (ActivityType activityType : correlations.keySet()) {
            double adjustment = getWeatherConditionAdjustment(activityType, condition);
            correlations.put(activityType, correlations.get(activityType) * adjustment);
        }
    }
    
    /**
     * Get weather condition adjustment factor for activity type
     */
    private double getWeatherConditionAdjustment(ActivityType activityType, String condition) {
        // Use learned preferences if available
        WeatherPreferences preferences = activityWeatherPreferences.get(activityType);
        if (preferences != null && preferences.getWeatherConditionPreferences().containsKey(condition)) {
            double preference = preferences.getWeatherConditionPreferences().get(condition);
            return 1.0 + preference; // Convert preference to adjustment factor
        }
        
        // Fallback to default adjustments
        if (condition.contains("rain") || condition.contains("storm")) {
            return (activityType.getCategory().equals("indoor")) ? 1.2 : 0.6;
        } else if (condition.contains("clear") || condition.contains("sunny")) {
            return (activityType.getCategory().equals("outdoor")) ? 1.2 : 0.9;
        }
        
        return 1.0; // No adjustment
    }
    
    /**
     * Get correlation insights for reporting
     */
    public Map<String, Object> getCorrelationInsights() {
        Map<String, Object> insights = new HashMap<>();
        
        // Activity-weather preference insights
        Map<String, WeatherPreferences> activityPreferences = new HashMap<>();
        for (Map.Entry<ActivityType, WeatherPreferences> entry : activityWeatherPreferences.entrySet()) {
            activityPreferences.put(entry.getKey().getDisplayName(), entry.getValue());
        }
        insights.put("activity_weather_preferences", activityPreferences);
        
        // Cache statistics
        insights.put("cached_correlations", correlationCache.size());
        
        // Top correlated activities
        List<String> topCorrelated = activityWeatherPreferences.entrySet().stream()
            .sorted(Map.Entry.<ActivityType, WeatherPreferences>comparingByValue(
                (p1, p2) -> Double.compare(p2.getCorrelationStrength(), p1.getCorrelationStrength())
            ))
            .limit(5)
            .map(entry -> entry.getKey().getDisplayName())
            .collect(Collectors.toList());
        insights.put("top_correlated_activities", topCorrelated);
        
        return insights;
    }
    
    /**
     * Get weather preferences for a specific activity
     */
    public WeatherPreferences getWeatherPreferences(ActivityType activityType) {
        return activityWeatherPreferences.get(activityType);
    }
    
    /**
     * Calculate standard deviation
     */
    private double calculateStandardDeviation(double[] values) {
        if (values.length == 0) return 0.0;
        
        double mean = Arrays.stream(values).average().orElse(0.0);
        double variance = Arrays.stream(values)
            .map(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    // Inner classes for data structures
    
    public static class WeatherPreferences {
        private TemperatureRange optimalTemperatureRange;
        private HumidityRange optimalHumidityRange;
        private WindSpeedRange optimalWindSpeedRange;
        private UVRange optimalUVRange;
        private Map<String, Double> weatherConditionPreferences;
        private double correlationStrength;
        
        public WeatherPreferences() {
            this.weatherConditionPreferences = new HashMap<>();
        }
        
        // Getters and setters
        public TemperatureRange getOptimalTemperatureRange() { return optimalTemperatureRange; }
        public void setOptimalTemperatureRange(TemperatureRange optimalTemperatureRange) { this.optimalTemperatureRange = optimalTemperatureRange; }
        
        public HumidityRange getOptimalHumidityRange() { return optimalHumidityRange; }
        public void setOptimalHumidityRange(HumidityRange optimalHumidityRange) { this.optimalHumidityRange = optimalHumidityRange; }
        
        public WindSpeedRange getOptimalWindSpeedRange() { return optimalWindSpeedRange; }
        public void setOptimalWindSpeedRange(WindSpeedRange optimalWindSpeedRange) { this.optimalWindSpeedRange = optimalWindSpeedRange; }
        
        public UVRange getOptimalUVRange() { return optimalUVRange; }
        public void setOptimalUVRange(UVRange optimalUVRange) { this.optimalUVRange = optimalUVRange; }
        
        public Map<String, Double> getWeatherConditionPreferences() { return weatherConditionPreferences; }
        public void setWeatherConditionPreferences(Map<String, Double> weatherConditionPreferences) { this.weatherConditionPreferences = weatherConditionPreferences; }
        
        public double getCorrelationStrength() { return correlationStrength; }
        public void setCorrelationStrength(double correlationStrength) { this.correlationStrength = correlationStrength; }
    }
    
    public static class TemperatureRange {
        private double min, max, optimal;
        
        public TemperatureRange(double min, double max, double optimal) {
            this.min = min;
            this.max = max;
            this.optimal = optimal;
        }
        
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getOptimal() { return optimal; }
    }
    
    public static class HumidityRange {
        private double min, max, optimal;
        
        public HumidityRange(double min, double max, double optimal) {
            this.min = min;
            this.max = max;
            this.optimal = optimal;
        }
        
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getOptimal() { return optimal; }
    }
    
    public static class WindSpeedRange {
        private double min, max, optimal;
        
        public WindSpeedRange(double min, double max, double optimal) {
            this.min = min;
            this.max = max;
            this.optimal = optimal;
        }
        
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getOptimal() { return optimal; }
    }
    
    public static class UVRange {
        private double min, max, optimal;
        
        public UVRange(double min, double max, double optimal) {
            this.min = min;
            this.max = max;
            this.optimal = optimal;
        }
        
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getOptimal() { return optimal; }
    }
    
    public static class WeatherActivityCorrelation {
        private int temperature;
        private int humidity;
        private int dataPoints;
        private Map<ActivityType, Double> activityScores;
        
        public WeatherActivityCorrelation() {
            this.activityScores = new HashMap<>();
        }
        
        public void setActivityScore(ActivityType activityType, double score) {
            activityScores.put(activityType, score);
        }
        
        // Getters and setters
        public int getTemperature() { return temperature; }
        public void setTemperature(int temperature) { this.temperature = temperature; }
        
        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
        
        public int getDataPoints() { return dataPoints; }
        public void setDataPoints(int dataPoints) { this.dataPoints = dataPoints; }
        
        public Map<ActivityType, Double> getActivityScores() { return activityScores; }
    }
}