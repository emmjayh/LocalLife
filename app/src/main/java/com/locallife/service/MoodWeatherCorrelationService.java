package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.model.MoodEntry;
import com.locallife.model.DayRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analyzing correlations between mood and weather patterns
 * Identifies how weather conditions affect user mood
 */
public class MoodWeatherCorrelationService {
    private static final String TAG = "MoodWeatherCorrelation";
    private MoodTrackingService moodTrackingService;
    
    public MoodWeatherCorrelationService(Context context) {
        moodTrackingService = new MoodTrackingService(context);
    }
    
    /**
     * Analyzes correlation between mood and various weather factors
     */
    public WeatherMoodCorrelation analyzeWeatherCorrelation(int days) {
        // Get mood entries for the specified period
        List<MoodEntry> moodEntries = moodTrackingService.getRecentMoodEntries(days * 5); // Approximate
        
        if (moodEntries.isEmpty()) {
            return new WeatherMoodCorrelation();
        }
        
        return new WeatherMoodCorrelation(moodEntries);
    }
    
    /**
     * Analyzes seasonal mood patterns
     */
    public SeasonalMoodPattern analyzeSeasonalPatterns(int months) {
        // Get mood entries for the specified period
        List<MoodEntry> moodEntries = moodTrackingService.getRecentMoodEntries(months * 30 * 3); // Approximate
        
        return new SeasonalMoodPattern(moodEntries);
    }
    
    /**
     * Analyzes mood correlation with air quality
     */
    public AirQualityMoodCorrelation analyzeAirQualityCorrelation(int days) {
        List<MoodEntry> moodEntries = moodTrackingService.getRecentMoodEntries(days * 3); // Approximate
        
        return new AirQualityMoodCorrelation(moodEntries);
    }
    
    /**
     * Analyzes mood correlation with daylight hours
     */
    public DaylightMoodCorrelation analyzeDaylightCorrelation(int days) {
        List<MoodEntry> moodEntries = moodTrackingService.getRecentMoodEntries(days * 3); // Approximate
        
        return new DaylightMoodCorrelation(moodEntries);
    }
    
    /**
     * Predicts mood based on weather forecast
     */
    public WeatherMoodPrediction predictMoodFromWeather(float temperature, String weatherCondition, 
                                                       float humidity, float atmosphericPressure, 
                                                       int airQualityIndex) {
        // Get historical data for similar conditions
        List<MoodEntry> historicalEntries = moodTrackingService.getRecentMoodEntries(200);
        
        List<MoodEntry> similarConditions = new ArrayList<>();
        
        for (MoodEntry entry : historicalEntries) {
            if (isWeatherSimilar(entry, temperature, weatherCondition, humidity, atmosphericPressure, airQualityIndex)) {
                similarConditions.add(entry);
            }
        }
        
        return new WeatherMoodPrediction(similarConditions, temperature, weatherCondition);
    }
    
    /**
     * Generates weather-based mood recommendations
     */
    public List<WeatherMoodRecommendation> generateWeatherRecommendations(float temperature, 
                                                                         String weatherCondition, 
                                                                         int airQualityIndex) {
        List<WeatherMoodRecommendation> recommendations = new ArrayList<>();
        
        // Temperature-based recommendations
        if (temperature < 10) {
            recommendations.add(new WeatherMoodRecommendation(
                "Cold Weather", 
                "Consider indoor activities and warm drinks to boost mood",
                "high"
            ));
        } else if (temperature > 30) {
            recommendations.add(new WeatherMoodRecommendation(
                "Hot Weather", 
                "Stay hydrated and seek shade during peak hours",
                "medium"
            ));
        } else {
            recommendations.add(new WeatherMoodRecommendation(
                "Pleasant Weather", 
                "Great weather for outdoor activities and exercise",
                "low"
            ));
        }
        
        // Weather condition recommendations
        if (weatherCondition != null) {
            switch (weatherCondition.toLowerCase()) {
                case "rain":
                case "heavy rain":
                    recommendations.add(new WeatherMoodRecommendation(
                        "Rainy Weather", 
                        "Rainy days can affect mood. Try indoor hobbies or meditation",
                        "high"
                    ));
                    break;
                case "snow":
                    recommendations.add(new WeatherMoodRecommendation(
                        "Snowy Weather", 
                        "Embrace winter activities or cozy indoor time",
                        "medium"
                    ));
                    break;
                case "overcast":
                case "cloudy":
                    recommendations.add(new WeatherMoodRecommendation(
                        "Cloudy Weather", 
                        "Consider light therapy or bright indoor environments",
                        "medium"
                    ));
                    break;
                case "clear sky":
                case "sunny":
                    recommendations.add(new WeatherMoodRecommendation(
                        "Sunny Weather", 
                        "Perfect for outdoor activities and vitamin D absorption",
                        "low"
                    ));
                    break;
            }
        }
        
        // Air quality recommendations
        if (airQualityIndex > 150) {
            recommendations.add(new WeatherMoodRecommendation(
                "Poor Air Quality", 
                "Limit outdoor activities and consider air purifiers indoors",
                "high"
            ));
        } else if (airQualityIndex > 100) {
            recommendations.add(new WeatherMoodRecommendation(
                "Moderate Air Quality", 
                "Sensitive individuals should limit prolonged outdoor exertion",
                "medium"
            ));
        }
        
        return recommendations;
    }
    
    /**
     * Checks if weather conditions are similar
     */
    private boolean isWeatherSimilar(MoodEntry entry, float temperature, String weatherCondition, 
                                    float humidity, float atmosphericPressure, int airQualityIndex) {
        // Temperature similarity (within 5 degrees)
        boolean tempSimilar = Math.abs(entry.getTemperature() - temperature) < 5;
        
        // Weather condition similarity
        boolean conditionSimilar = entry.getWeatherCondition() != null && 
                                  entry.getWeatherCondition().equals(weatherCondition);
        
        // Humidity similarity (within 20%)
        boolean humiditySimilar = Math.abs(entry.getHumidity() - humidity) < 20;
        
        // Air quality similarity (within 50 points)
        boolean airQualitySimilar = Math.abs(entry.getAirQualityIndex() - airQualityIndex) < 50;
        
        return tempSimilar && conditionSimilar && humiditySimilar && airQualitySimilar;
    }
    
    /**
     * Calculates Pearson correlation coefficient
     */
    private float calculateCorrelation(List<Float> x, List<Float> y) {
        if (x.size() != y.size() || x.size() < 2) {
            return 0.0f;
        }
        
        float n = x.size();
        float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
            sumY2 += y.get(i) * y.get(i);
        }
        
        float numerator = n * sumXY - sumX * sumY;
        float denominator = (float) Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        
        if (denominator == 0) {
            return 0.0f;
        }
        
        return numerator / denominator;
    }
    
    /**
     * Inner class for weather-mood correlation analysis
     */
    public static class WeatherMoodCorrelation {
        private float temperatureCorrelation;
        private float humidityCorrelation;
        private float pressureCorrelation;
        private Map<String, Float> weatherConditionMoodImpact;
        private String strongestWeatherFactor;
        private String weatherMoodProfile;
        
        public WeatherMoodCorrelation() {
            // Default values for empty data
            this.temperatureCorrelation = 0.0f;
            this.humidityCorrelation = 0.0f;
            this.pressureCorrelation = 0.0f;
            this.weatherConditionMoodImpact = new HashMap<>();
            this.strongestWeatherFactor = "none";
            this.weatherMoodProfile = "insufficient_data";
        }
        
        public WeatherMoodCorrelation(List<MoodEntry> moodEntries) {
            analyzeCorrelations(moodEntries);
        }
        
        private void analyzeCorrelations(List<MoodEntry> moodEntries) {
            if (moodEntries.isEmpty()) {
                return;
            }
            
            List<Float> moodScores = new ArrayList<>();
            List<Float> temperatures = new ArrayList<>();
            List<Float> humidities = new ArrayList<>();
            List<Float> pressures = new ArrayList<>();
            Map<String, List<Float>> conditionMoods = new HashMap<>();
            
            for (MoodEntry entry : moodEntries) {
                moodScores.add((float) entry.getMoodScore());
                temperatures.add(entry.getTemperature());
                humidities.add(entry.getHumidity());
                pressures.add(entry.getAtmosphericPressure());
                
                if (entry.getWeatherCondition() != null) {
                    conditionMoods.computeIfAbsent(entry.getWeatherCondition(), k -> new ArrayList<>())
                                 .add((float) entry.getMoodScore());
                }
            }
            
            // Calculate correlations
            temperatureCorrelation = calculateCorrelation(temperatures, moodScores);
            humidityCorrelation = calculateCorrelation(humidities, moodScores);
            pressureCorrelation = calculateCorrelation(pressures, moodScores);
            
            // Calculate weather condition impact
            weatherConditionMoodImpact = new HashMap<>();
            for (Map.Entry<String, List<Float>> entry : conditionMoods.entrySet()) {
                float sum = 0;
                for (Float mood : entry.getValue()) {
                    sum += mood;
                }
                weatherConditionMoodImpact.put(entry.getKey(), sum / entry.getValue().size());
            }
            
            // Determine strongest weather factor
            float maxCorrelation = 0;
            if (Math.abs(temperatureCorrelation) > maxCorrelation) {
                maxCorrelation = Math.abs(temperatureCorrelation);
                strongestWeatherFactor = "temperature";
            }
            if (Math.abs(humidityCorrelation) > maxCorrelation) {
                maxCorrelation = Math.abs(humidityCorrelation);
                strongestWeatherFactor = "humidity";
            }
            if (Math.abs(pressureCorrelation) > maxCorrelation) {
                maxCorrelation = Math.abs(pressureCorrelation);
                strongestWeatherFactor = "pressure";
            }
            
            // Generate weather mood profile
            if (temperatureCorrelation > 0.3f) {
                weatherMoodProfile = "temperature_sensitive_positive";
            } else if (temperatureCorrelation < -0.3f) {
                weatherMoodProfile = "temperature_sensitive_negative";
            } else if (humidityCorrelation < -0.3f) {
                weatherMoodProfile = "humidity_sensitive";
            } else if (pressureCorrelation > 0.3f) {
                weatherMoodProfile = "pressure_sensitive";
            } else {
                weatherMoodProfile = "weather_resilient";
            }
        }
        
        private float calculateCorrelation(List<Float> x, List<Float> y) {
            if (x.size() != y.size() || x.size() < 2) {
                return 0.0f;
            }
            
            float n = x.size();
            float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
            
            for (int i = 0; i < n; i++) {
                sumX += x.get(i);
                sumY += y.get(i);
                sumXY += x.get(i) * y.get(i);
                sumX2 += x.get(i) * x.get(i);
                sumY2 += y.get(i) * y.get(i);
            }
            
            float numerator = n * sumXY - sumX * sumY;
            float denominator = (float) Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
            
            if (denominator == 0) {
                return 0.0f;
            }
            
            return numerator / denominator;
        }
        
        // Getters
        public float getTemperatureCorrelation() { return temperatureCorrelation; }
        public float getHumidityCorrelation() { return humidityCorrelation; }
        public float getPressureCorrelation() { return pressureCorrelation; }
        public Map<String, Float> getWeatherConditionMoodImpact() { return weatherConditionMoodImpact; }
        public String getStrongestWeatherFactor() { return strongestWeatherFactor; }
        public String getWeatherMoodProfile() { return weatherMoodProfile; }
    }
    
    /**
     * Inner class for seasonal mood pattern analysis
     */
    public static class SeasonalMoodPattern {
        private Map<String, Float> seasonalMoodAverages;
        private String bestSeason;
        private String worstSeason;
        private boolean hasSAD; // Seasonal Affective Disorder indicators
        
        public SeasonalMoodPattern(List<MoodEntry> moodEntries) {
            analyzeSeasonalPatterns(moodEntries);
        }
        
        private void analyzeSeasonalPatterns(List<MoodEntry> moodEntries) {
            Map<String, List<Float>> seasonalMoods = new HashMap<>();
            seasonalMoodAverages = new HashMap<>();
            
            for (MoodEntry entry : moodEntries) {
                String season = getSeasonFromDayLength(entry.getDayLengthMinutes());
                seasonalMoods.computeIfAbsent(season, k -> new ArrayList<>()).add((float) entry.getMoodScore());
            }
            
            // Calculate seasonal averages
            for (Map.Entry<String, List<Float>> entry : seasonalMoods.entrySet()) {
                float sum = 0;
                for (Float mood : entry.getValue()) {
                    sum += mood;
                }
                seasonalMoodAverages.put(entry.getKey(), sum / entry.getValue().size());
            }
            
            // Find best and worst seasons
            float maxMood = 0;
            float minMood = 10;
            
            for (Map.Entry<String, Float> entry : seasonalMoodAverages.entrySet()) {
                if (entry.getValue() > maxMood) {
                    maxMood = entry.getValue();
                    bestSeason = entry.getKey();
                }
                if (entry.getValue() < minMood) {
                    minMood = entry.getValue();
                    worstSeason = entry.getKey();
                }
            }
            
            // Check for SAD indicators
            float winterMood = seasonalMoodAverages.getOrDefault("winter", 5.0f);
            float summerMood = seasonalMoodAverages.getOrDefault("summer", 5.0f);
            
            hasSAD = (summerMood - winterMood) > 1.5f;
        }
        
        private String getSeasonFromDayLength(long dayLengthMinutes) {
            if (dayLengthMinutes < 9 * 60) {
                return "winter";
            } else if (dayLengthMinutes < 12 * 60) {
                return "spring";
            } else if (dayLengthMinutes < 15 * 60) {
                return "summer";
            } else {
                return "autumn";
            }
        }
        
        // Getters
        public Map<String, Float> getSeasonalMoodAverages() { return seasonalMoodAverages; }
        public String getBestSeason() { return bestSeason; }
        public String getWorstSeason() { return worstSeason; }
        public boolean hasSAD() { return hasSAD; }
    }
    
    /**
     * Inner class for air quality mood correlation
     */
    public static class AirQualityMoodCorrelation {
        private float airQualityCorrelation;
        private Map<String, Float> airQualityLevelMoodImpact;
        private boolean isAirQualitySensitive;
        
        public AirQualityMoodCorrelation(List<MoodEntry> moodEntries) {
            analyzeAirQualityCorrelation(moodEntries);
        }
        
        private void analyzeAirQualityCorrelation(List<MoodEntry> moodEntries) {
            List<Float> moodScores = new ArrayList<>();
            List<Float> airQualityValues = new ArrayList<>();
            Map<String, List<Float>> levelMoods = new HashMap<>();
            
            for (MoodEntry entry : moodEntries) {
                moodScores.add((float) entry.getMoodScore());
                airQualityValues.add((float) entry.getAirQualityIndex());
                
                String level = getAirQualityLevel(entry.getAirQualityIndex());
                levelMoods.computeIfAbsent(level, k -> new ArrayList<>()).add((float) entry.getMoodScore());
            }
            
            // Calculate correlation
            airQualityCorrelation = calculateCorrelation(airQualityValues, moodScores);
            
            // Calculate level impact
            airQualityLevelMoodImpact = new HashMap<>();
            for (Map.Entry<String, List<Float>> entry : levelMoods.entrySet()) {
                float sum = 0;
                for (Float mood : entry.getValue()) {
                    sum += mood;
                }
                airQualityLevelMoodImpact.put(entry.getKey(), sum / entry.getValue().size());
            }
            
            // Determine sensitivity
            isAirQualitySensitive = Math.abs(airQualityCorrelation) > 0.3f;
        }
        
        private String getAirQualityLevel(int aqi) {
            if (aqi <= 50) return "good";
            if (aqi <= 100) return "moderate";
            if (aqi <= 150) return "unhealthy_sensitive";
            if (aqi <= 200) return "unhealthy";
            if (aqi <= 300) return "very_unhealthy";
            return "hazardous";
        }
        
        private float calculateCorrelation(List<Float> x, List<Float> y) {
            if (x.size() != y.size() || x.size() < 2) {
                return 0.0f;
            }
            
            float n = x.size();
            float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
            
            for (int i = 0; i < n; i++) {
                sumX += x.get(i);
                sumY += y.get(i);
                sumXY += x.get(i) * y.get(i);
                sumX2 += x.get(i) * x.get(i);
                sumY2 += y.get(i) * y.get(i);
            }
            
            float numerator = n * sumXY - sumX * sumY;
            float denominator = (float) Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
            
            if (denominator == 0) {
                return 0.0f;
            }
            
            return numerator / denominator;
        }
        
        // Getters
        public float getAirQualityCorrelation() { return airQualityCorrelation; }
        public Map<String, Float> getAirQualityLevelMoodImpact() { return airQualityLevelMoodImpact; }
        public boolean isAirQualitySensitive() { return isAirQualitySensitive; }
    }
    
    /**
     * Inner class for daylight mood correlation
     */
    public static class DaylightMoodCorrelation {
        private float daylightCorrelation;
        private boolean isLightSensitive;
        private String lightSensitivityType;
        
        public DaylightMoodCorrelation(List<MoodEntry> moodEntries) {
            analyzeDaylightCorrelation(moodEntries);
        }
        
        private void analyzeDaylightCorrelation(List<MoodEntry> moodEntries) {
            List<Float> moodScores = new ArrayList<>();
            List<Float> daylightHours = new ArrayList<>();
            
            for (MoodEntry entry : moodEntries) {
                moodScores.add((float) entry.getMoodScore());
                daylightHours.add(entry.getDayLengthMinutes() / 60.0f);
            }
            
            // Calculate correlation
            daylightCorrelation = calculateCorrelation(daylightHours, moodScores);
            
            // Determine sensitivity
            isLightSensitive = Math.abs(daylightCorrelation) > 0.3f;
            
            if (daylightCorrelation > 0.3f) {
                lightSensitivityType = "positive_light_sensitive";
            } else if (daylightCorrelation < -0.3f) {
                lightSensitivityType = "negative_light_sensitive";
            } else {
                lightSensitivityType = "light_neutral";
            }
        }
        
        private float calculateCorrelation(List<Float> x, List<Float> y) {
            if (x.size() != y.size() || x.size() < 2) {
                return 0.0f;
            }
            
            float n = x.size();
            float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
            
            for (int i = 0; i < n; i++) {
                sumX += x.get(i);
                sumY += y.get(i);
                sumXY += x.get(i) * y.get(i);
                sumX2 += x.get(i) * x.get(i);
                sumY2 += y.get(i) * y.get(i);
            }
            
            float numerator = n * sumXY - sumX * sumY;
            float denominator = (float) Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
            
            if (denominator == 0) {
                return 0.0f;
            }
            
            return numerator / denominator;
        }
        
        // Getters
        public float getDaylightCorrelation() { return daylightCorrelation; }
        public boolean isLightSensitive() { return isLightSensitive; }
        public String getLightSensitivityType() { return lightSensitivityType; }
    }
    
    /**
     * Inner class for weather mood prediction
     */
    public static class WeatherMoodPrediction {
        private float predictedMood;
        private float confidence;
        private String explanation;
        private List<String> weatherFactors;
        
        public WeatherMoodPrediction(List<MoodEntry> similarConditions, float temperature, String weatherCondition) {
            predictMood(similarConditions, temperature, weatherCondition);
        }
        
        private void predictMood(List<MoodEntry> similarConditions, float temperature, String weatherCondition) {
            weatherFactors = new ArrayList<>();
            
            if (similarConditions.isEmpty()) {
                predictedMood = 5.0f; // Neutral
                confidence = 0.0f;
                explanation = "No similar weather patterns found in historical data";
                return;
            }
            
            // Calculate predicted mood
            float sum = 0;
            for (MoodEntry entry : similarConditions) {
                sum += entry.getMoodScore();
            }
            predictedMood = sum / similarConditions.size();
            
            // Calculate confidence
            confidence = Math.min(1.0f, similarConditions.size() / 20.0f);
            
            // Generate explanation
            explanation = String.format("Based on %d similar weather patterns. ", similarConditions.size());
            
            if (temperature < 10) {
                weatherFactors.add("cold temperature");
            } else if (temperature > 30) {
                weatherFactors.add("hot temperature");
            }
            
            if (weatherCondition != null) {
                weatherFactors.add(weatherCondition);
            }
            
            if (!weatherFactors.isEmpty()) {
                explanation += "Weather factors: " + String.join(", ", weatherFactors);
            }
        }
        
        // Getters
        public float getPredictedMood() { return predictedMood; }
        public float getConfidence() { return confidence; }
        public String getExplanation() { return explanation; }
        public List<String> getWeatherFactors() { return weatherFactors; }
    }
    
    /**
     * Inner class for weather mood recommendations
     */
    public static class WeatherMoodRecommendation {
        private String weatherFactor;
        private String recommendation;
        private String priority;
        
        public WeatherMoodRecommendation(String weatherFactor, String recommendation, String priority) {
            this.weatherFactor = weatherFactor;
            this.recommendation = recommendation;
            this.priority = priority;
        }
        
        // Getters
        public String getWeatherFactor() { return weatherFactor; }
        public String getRecommendation() { return recommendation; }
        public String getPriority() { return priority; }
    }
    
    /**
     * Cleanup method
     */
    public void close() {
        if (moodTrackingService != null) {
            moodTrackingService.close();
        }
    }
}