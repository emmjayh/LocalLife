package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for analyzing correlations between environmental factors and activity patterns
 */
public class CorrelationAnalysisService {
    private static final String TAG = "CorrelationAnalysisService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    
    public CorrelationAnalysisService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Analyze correlations between weather and activity
     */
    public void analyzeWeatherActivityCorrelation(CorrelationCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getAllDayRecords();
                
                if (records.size() < 10) {
                    callback.onError("Not enough data for correlation analysis");
                    return;
                }
                
                CorrelationResults results = new CorrelationResults();
                
                // Temperature vs Activity
                results.temperatureActivityCorr = calculateCorrelation(records, 
                    record -> record.getTemperature(), 
                    record -> record.getActivityScore());
                
                // Temperature vs Steps
                results.temperatureStepsCorr = calculateCorrelation(records,
                    record -> record.getTemperature(),
                    record -> (float) record.getStepCount());
                
                // Humidity vs Activity
                results.humidityActivityCorr = calculateCorrelation(records,
                    record -> record.getHumidity(),
                    record -> record.getActivityScore());
                
                // UV Index vs Activity
                results.uvActivityCorr = calculateCorrelation(records,
                    record -> (float) record.getUvIndex(),
                    record -> record.getActivityScore());
                
                // Air Quality vs Activity
                results.airQualityActivityCorr = calculateCorrelation(records,
                    record -> (float) record.getAirQualityIndex(),
                    record -> record.getActivityScore());
                
                // Screen Time vs Weather
                results.weatherScreenTimeCorr = calculateCorrelation(records,
                    record -> record.getTemperature(),
                    record -> (float) record.getScreenTimeMinutes());
                
                // Media Consumption vs Weather
                results.weatherMediaCorr = calculateCorrelation(records,
                    record -> record.getTemperature(),
                    record -> (float) record.getTotalMediaMinutes());
                
                // Generate insights
                results.insights = generateInsights(results);
                
                callback.onCorrelationResults(results);
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing correlations", e);
                callback.onError("Analysis failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Calculate Pearson correlation coefficient
     */
    private float calculateCorrelation(List<DayRecord> records, 
                                     ValueExtractor x, ValueExtractor y) {
        List<Float> xValues = new ArrayList<>();
        List<Float> yValues = new ArrayList<>();
        
        for (DayRecord record : records) {
            float xVal = x.extract(record);
            float yVal = y.extract(record);
            
            // Skip invalid data
            if (Float.isNaN(xVal) || Float.isNaN(yVal) || xVal == 0 || yVal == 0) {
                continue;
            }
            
            xValues.add(xVal);
            yValues.add(yVal);
        }
        
        if (xValues.size() < 5) {
            return 0f; // Not enough data
        }
        
        return pearsonCorrelation(xValues, yValues);
    }
    
    /**
     * Calculate Pearson correlation coefficient
     */
    private float pearsonCorrelation(List<Float> x, List<Float> y) {
        if (x.size() != y.size() || x.size() < 2) {
            return 0f;
        }
        
        int n = x.size();
        
        // Calculate means
        float meanX = 0, meanY = 0;
        for (int i = 0; i < n; i++) {
            meanX += x.get(i);
            meanY += y.get(i);
        }
        meanX /= n;
        meanY /= n;
        
        // Calculate correlation
        float numerator = 0;
        float sumXX = 0, sumYY = 0;
        
        for (int i = 0; i < n; i++) {
            float dx = x.get(i) - meanX;
            float dy = y.get(i) - meanY;
            
            numerator += dx * dy;
            sumXX += dx * dx;
            sumYY += dy * dy;
        }
        
        float denominator = (float) Math.sqrt(sumXX * sumYY);
        
        if (denominator == 0) {
            return 0f;
        }
        
        return numerator / denominator;
    }
    
    /**
     * Generate insights from correlation results
     */
    private List<CorrelationInsight> generateInsights(CorrelationResults results) {
        List<CorrelationInsight> insights = new ArrayList<>();
        
        // Temperature insights
        if (Math.abs(results.temperatureActivityCorr) > 0.3f) {
            String direction = results.temperatureActivityCorr > 0 ? "higher" : "lower";
            insights.add(new CorrelationInsight(
                "Temperature Impact",
                String.format("Warmer temperatures correlate with %s activity levels", direction),
                "temperature",
                results.temperatureActivityCorr,
                CorrelationInsight.Strength.getStrength(results.temperatureActivityCorr)
            ));
        }
        
        // UV Index insights
        if (Math.abs(results.uvActivityCorr) > 0.25f) {
            String direction = results.uvActivityCorr > 0 ? "increase" : "decrease";
            insights.add(new CorrelationInsight(
                "UV Index Effect",
                String.format("Higher UV levels tend to %s your activity", direction),
                "uv",
                results.uvActivityCorr,
                CorrelationInsight.Strength.getStrength(results.uvActivityCorr)
            ));
        }
        
        // Air Quality insights
        if (Math.abs(results.airQualityActivityCorr) > 0.2f) {
            String direction = results.airQualityActivityCorr > 0 ? "better" : "worse";
            insights.add(new CorrelationInsight(
                "Air Quality Impact",
                String.format("%s air quality correlates with higher activity", 
                    results.airQualityActivityCorr < 0 ? "Better" : "Worse"),
                "air_quality",
                results.airQualityActivityCorr,
                CorrelationInsight.Strength.getStrength(results.airQualityActivityCorr)
            ));
        }
        
        // Screen Time vs Weather
        if (Math.abs(results.weatherScreenTimeCorr) > 0.25f) {
            String weather = results.weatherScreenTimeCorr > 0 ? "warmer" : "colder";
            String screenTime = results.weatherScreenTimeCorr > 0 ? "more" : "less";
            insights.add(new CorrelationInsight(
                "Weather & Screen Time",
                String.format("You tend to use screens %s during %s weather", screenTime, weather),
                "screen_weather",
                results.weatherScreenTimeCorr,
                CorrelationInsight.Strength.getStrength(results.weatherScreenTimeCorr)
            ));
        }
        
        // Media Consumption vs Weather
        if (Math.abs(results.weatherMediaCorr) > 0.25f) {
            String weather = results.weatherMediaCorr > 0 ? "warmer" : "colder";
            String media = results.weatherMediaCorr > 0 ? "more" : "less";
            insights.add(new CorrelationInsight(
                "Weather & Media",
                String.format("You consume %s media during %s weather", media, weather),
                "media_weather",
                results.weatherMediaCorr,
                CorrelationInsight.Strength.getStrength(results.weatherMediaCorr)
            ));
        }
        
        return insights;
    }
    
    /**
     * Analyze activity patterns by weather conditions
     */
    public void analyzeActivityPatternsByWeather(WeatherPatternCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getAllDayRecords();
                
                Map<String, List<Float>> activityByCondition = new HashMap<>();
                Map<String, List<Float>> stepsByCondition = new HashMap<>();
                
                for (DayRecord record : records) {
                    String condition = record.getWeatherCondition();
                    if (condition == null || condition.isEmpty()) {
                        condition = "Unknown";
                    }
                    
                    activityByCondition.computeIfAbsent(condition, k -> new ArrayList<>())
                        .add(record.getActivityScore());
                    stepsByCondition.computeIfAbsent(condition, k -> new ArrayList<>())
                        .add((float) record.getStepCount());
                }
                
                WeatherPatternResults results = new WeatherPatternResults();
                results.activityByCondition = activityByCondition;
                results.stepsByCondition = stepsByCondition;
                
                // Calculate averages for each condition
                results.averageActivityByCondition = new HashMap<>();
                results.averageStepsByCondition = new HashMap<>();
                
                for (Map.Entry<String, List<Float>> entry : activityByCondition.entrySet()) {
                    float avg = entry.getValue().stream()
                        .reduce(0f, Float::sum) / entry.getValue().size();
                    results.averageActivityByCondition.put(entry.getKey(), avg);
                }
                
                for (Map.Entry<String, List<Float>> entry : stepsByCondition.entrySet()) {
                    float avg = entry.getValue().stream()
                        .reduce(0f, Float::sum) / entry.getValue().size();
                    results.averageStepsByCondition.put(entry.getKey(), avg);
                }
                
                callback.onWeatherPatternResults(results);
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing weather patterns", e);
                callback.onError("Weather pattern analysis failed: " + e.getMessage());
            }
        });
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Functional interface for value extraction
    private interface ValueExtractor {
        float extract(DayRecord record);
    }
    
    // Data classes
    public static class CorrelationResults {
        public float temperatureActivityCorr;
        public float temperatureStepsCorr;
        public float humidityActivityCorr;
        public float uvActivityCorr;
        public float airQualityActivityCorr;
        public float weatherScreenTimeCorr;
        public float weatherMediaCorr;
        public List<CorrelationInsight> insights;
    }
    
    public static class CorrelationInsight {
        public String title;
        public String description;
        public String category;
        public float correlation;
        public Strength strength;
        
        public CorrelationInsight(String title, String description, String category, 
                                float correlation, Strength strength) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.correlation = correlation;
            this.strength = strength;
        }
        
        public enum Strength {
            WEAK, MODERATE, STRONG, VERY_STRONG;
            
            public static Strength getStrength(float correlation) {
                float abs = Math.abs(correlation);
                if (abs < 0.3f) return WEAK;
                if (abs < 0.5f) return MODERATE;
                if (abs < 0.7f) return STRONG;
                return VERY_STRONG;
            }
        }
    }
    
    public static class WeatherPatternResults {
        public Map<String, List<Float>> activityByCondition;
        public Map<String, List<Float>> stepsByCondition;
        public Map<String, Float> averageActivityByCondition;
        public Map<String, Float> averageStepsByCondition;
    }
    
    // Callback interfaces
    public interface CorrelationCallback {
        void onCorrelationResults(CorrelationResults results);
        void onError(String error);
    }
    
    public interface WeatherPatternCallback {
        void onWeatherPatternResults(WeatherPatternResults results);
        void onError(String error);
    }
}