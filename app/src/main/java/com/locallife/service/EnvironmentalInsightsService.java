package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service for analyzing environmental data patterns and providing insights
 * Correlates air quality, moon phases, UV index, and circadian rhythms with activity data
 */
public class EnvironmentalInsightsService {
    private static final String TAG = "EnvironmentalInsights";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Insight categories
    public static class EnvironmentalInsight {
        private String category;
        private String title;
        private String description;
        private String recommendation;
        private float confidenceScore;
        private Date timestamp;
        
        public EnvironmentalInsight(String category, String title, String description, 
                                  String recommendation, float confidenceScore) {
            this.category = category;
            this.title = title;
            this.description = description;
            this.recommendation = recommendation;
            this.confidenceScore = confidenceScore;
            this.timestamp = new Date();
        }
        
        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        
        public float getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(float confidenceScore) { this.confidenceScore = confidenceScore; }
        
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }
    
    public EnvironmentalInsightsService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }
    
    /**
     * Generate comprehensive environmental insights for the past period
     */
    public List<EnvironmentalInsight> generateInsights(int daysPast) {
        List<EnvironmentalInsight> insights = new ArrayList<>();
        
        try {
            // Get historical data
            List<DayRecord> records = getHistoricalRecords(daysPast);
            
            if (records.size() < 7) {
                Log.w(TAG, "Not enough data for meaningful insights");
                return insights;
            }
            
            // Analyze different environmental factors
            insights.addAll(analyzeAirQualityPatterns(records));
            insights.addAll(analyzeMoonPhasePatterns(records));
            insights.addAll(analyzeUVIndexPatterns(records));
            insights.addAll(analyzeCircadianPatterns(records));
            insights.addAll(analyzeSeasonalPatterns(records));
            insights.addAll(analyzeWeatherCorrelations(records));
            
            // Sort by confidence score (highest first)
            insights.sort((a, b) -> Float.compare(b.getConfidenceScore(), a.getConfidenceScore()));
            
            Log.d(TAG, "Generated " + insights.size() + " environmental insights");
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating environmental insights", e);
        }
        
        return insights;
    }
    
    /**
     * Analyze air quality impact on activity patterns
     */
    private List<EnvironmentalInsight> analyzeAirQualityPatterns(List<DayRecord> records) {
        List<EnvironmentalInsight> insights = new ArrayList<>();
        
        try {
            float goodAirQualityActivityAvg = 0;
            float poorAirQualityActivityAvg = 0;
            int goodAirQualityDays = 0;
            int poorAirQualityDays = 0;
            
            for (DayRecord record : records) {
                if (record.getAqi() > 0) {
                    if (record.getAqi() <= 50) {
                        goodAirQualityActivityAvg += record.getActivityScore();
                        goodAirQualityDays++;
                    } else if (record.getAqi() > 100) {
                        poorAirQualityActivityAvg += record.getActivityScore();
                        poorAirQualityDays++;
                    }
                }
            }
            
            if (goodAirQualityDays > 0 && poorAirQualityDays > 0) {
                goodAirQualityActivityAvg /= goodAirQualityDays;
                poorAirQualityActivityAvg /= poorAirQualityDays;
                
                float activityDifference = goodAirQualityActivityAvg - poorAirQualityActivityAvg;
                
                if (activityDifference > 10) {
                    float confidence = Math.min(0.9f, activityDifference / 50f);
                    insights.add(new EnvironmentalInsight(
                        "Air Quality",
                        "Air Quality Significantly Impacts Your Activity",
                        String.format("Your activity score is %.1f points higher on days with good air quality (AQI ≤50) compared to poor air quality days (AQI >100).", activityDifference),
                        "Check air quality forecasts and plan outdoor activities on days with better air quality. Consider indoor activities when AQI is above 100.",
                        confidence
                    ));
                }
            }
            
            // Analyze PM2.5 specific impacts
            analyzePM25Patterns(records, insights);
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing air quality patterns", e);
        }
        
        return insights;
    }
    
    /**
     * Analyze PM2.5 specific patterns
     */
    private void analyzePM25Patterns(List<DayRecord> records, List<EnvironmentalInsight> insights) {
        float highPM25Count = 0;
        float lowPM25ActivitySum = 0;
        float highPM25ActivitySum = 0;
        int lowPM25Days = 0;
        int highPM25Days = 0;
        
        for (DayRecord record : records) {
            if (record.getPm25Level() > 0) {
                if (record.getPm25Level() > 25) { // WHO guideline
                    highPM25ActivitySum += record.getActivityScore();
                    highPM25Days++;
                    highPM25Count++;
                } else {
                    lowPM25ActivitySum += record.getActivityScore();
                    lowPM25Days++;
                }
            }
        }
        
        if (highPM25Count > records.size() * 0.3) { // More than 30% of days
            insights.add(new EnvironmentalInsight(
                "Air Quality",
                "High PM2.5 Exposure Detected",
                String.format("You experienced high PM2.5 levels (>25 μg/m³) on %.0f%% of days analyzed.", (highPM25Count / records.size()) * 100),
                "Consider using air purifiers indoors and wearing masks during outdoor activities on high PM2.5 days. Check local air quality apps regularly.",
                0.8f
            ));
        }
    }
    
    /**
     * Analyze moon phase impact on activity and sleep patterns
     */
    private List<EnvironmentalInsight> analyzeMoonPhasePatterns(List<DayRecord> records) {
        List<EnvironmentalInsight> insights = new ArrayList<>();
        
        try {
            Map<String, Float> moonPhaseActivity = new HashMap<>();
            Map<String, Integer> moonPhaseCount = new HashMap<>();
            
            for (DayRecord record : records) {
                String moonPhase = record.getMoonPhase();
                if (moonPhase != null && !moonPhase.isEmpty()) {
                    moonPhaseActivity.put(moonPhase, 
                        moonPhaseActivity.getOrDefault(moonPhase, 0f) + record.getActivityScore());
                    moonPhaseCount.put(moonPhase, 
                        moonPhaseCount.getOrDefault(moonPhase, 0) + 1);
                }
            }
            
            // Calculate averages
            Map<String, Float> moonPhaseAverages = new HashMap<>();
            for (String phase : moonPhaseActivity.keySet()) {
                if (moonPhaseCount.get(phase) > 0) {
                    moonPhaseAverages.put(phase, moonPhaseActivity.get(phase) / moonPhaseCount.get(phase));
                }
            }
            
            // Find most and least active moon phases
            String mostActivePhase = null;
            String leastActivePhase = null;
            float maxActivity = 0;
            float minActivity = Float.MAX_VALUE;
            
            for (Map.Entry<String, Float> entry : moonPhaseAverages.entrySet()) {
                if (entry.getValue() > maxActivity) {
                    maxActivity = entry.getValue();
                    mostActivePhase = entry.getKey();
                }
                if (entry.getValue() < minActivity) {
                    minActivity = entry.getValue();
                    leastActivePhase = entry.getKey();
                }
            }
            
            if (mostActivePhase != null && leastActivePhase != null && maxActivity - minActivity > 5) {
                float confidence = Math.min(0.7f, (maxActivity - minActivity) / 30f);
                insights.add(new EnvironmentalInsight(
                    "Moon Phase",
                    "Moon Phase Affects Your Activity Levels",
                    String.format("You're most active during %s (%.1f avg score) and least active during %s (%.1f avg score).", 
                        mostActivePhase, maxActivity, leastActivePhase, minActivity),
                    String.format("Plan important activities during %s phases and allow for more rest during %s phases.", 
                        mostActivePhase, leastActivePhase),
                    confidence
                ));
            }
            
            // Analyze full moon effects
            analyzeFullMoonEffects(records, insights);
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing moon phase patterns", e);
        }
        
        return insights;
    }
    
    /**
     * Analyze full moon specific effects
     */
    private void analyzeFullMoonEffects(List<DayRecord> records, List<EnvironmentalInsight> insights) {
        float fullMoonActivitySum = 0;
        float otherPhasesActivitySum = 0;
        int fullMoonDays = 0;
        int otherPhasesDays = 0;
        
        for (DayRecord record : records) {
            if ("full moon".equalsIgnoreCase(record.getMoonPhase())) {
                fullMoonActivitySum += record.getActivityScore();
                fullMoonDays++;
            } else if (record.getMoonPhase() != null && !record.getMoonPhase().isEmpty()) {
                otherPhasesActivitySum += record.getActivityScore();
                otherPhasesDays++;
            }
        }
        
        if (fullMoonDays > 0 && otherPhasesDays > 0) {
            float fullMoonAvg = fullMoonActivitySum / fullMoonDays;
            float otherPhasesAvg = otherPhasesActivitySum / otherPhasesDays;
            float difference = Math.abs(fullMoonAvg - otherPhasesAvg);
            
            if (difference > 8) {
                String effect = fullMoonAvg > otherPhasesAvg ? "increased" : "decreased";
                insights.add(new EnvironmentalInsight(
                    "Moon Phase",
                    "Full Moon Impact Detected",
                    String.format("Your activity levels are %s by %.1f points during full moon phases.", effect, difference),
                    "Be aware of lunar cycles when planning activities and managing energy levels. Track your mood and sleep patterns during full moons.",
                    0.6f
                ));
            }
        }
    }
    
    /**
     * Analyze UV index patterns and vitamin D optimization
     */
    private List<EnvironmentalInsight> analyzeUVIndexPatterns(List<DayRecord> records) {
        List<EnvironmentalInsight> insights = new ArrayList<>();
        
        try {
            float totalUVExposure = 0;
            float optimalUVDays = 0;
            float highUVDays = 0;
            int uvDataDays = 0;
            
            for (DayRecord record : records) {
                if (record.getUvIndex() > 0) {
                    totalUVExposure += record.getUvIndex();
                    uvDataDays++;
                    
                    if (record.getUvIndex() >= 3 && record.getUvIndex() <= 7) {
                        optimalUVDays++;
                    } else if (record.getUvIndex() > 8) {
                        highUVDays++;
                    }
                }
            }
            
            if (uvDataDays > 0) {
                float avgUVIndex = totalUVExposure / uvDataDays;
                float optimalUVPercentage = (optimalUVDays / uvDataDays) * 100;
                float highUVPercentage = (highUVDays / uvDataDays) * 100;
                
                // Vitamin D synthesis insights
                if (optimalUVPercentage > 50) {
                    insights.add(new EnvironmentalInsight(
                        "UV Index",
                        "Good Vitamin D Synthesis Opportunities",
                        String.format("%.0f%% of days had optimal UV levels (3-7) for vitamin D synthesis.", optimalUVPercentage),
                        "Take advantage of these moderate UV days for 10-15 minutes of direct sunlight exposure to maintain vitamin D levels.",
                        0.8f
                    ));
                }
                
                // High UV exposure warnings
                if (highUVPercentage > 30) {
                    insights.add(new EnvironmentalInsight(
                        "UV Index",
                        "High UV Exposure Risk",
                        String.format("%.0f%% of days had high UV levels (>8) requiring sun protection.", highUVPercentage),
                        "Use SPF 30+ sunscreen, wear protective clothing, and limit direct sun exposure during peak hours (10am-4pm) on high UV days.",
                        0.9f
                    ));
                }
                
                // Seasonal UV pattern analysis
                analyzeSeasonalUVPatterns(records, insights);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing UV index patterns", e);
        }
        
        return insights;
    }
    
    /**
     * Analyze seasonal UV patterns
     */
    private void analyzeSeasonalUVPatterns(List<DayRecord> records, List<EnvironmentalInsight> insights) {
        Map<String, Float> seasonalUV = new HashMap<>();
        Map<String, Integer> seasonalCount = new HashMap<>();
        
        for (DayRecord record : records) {
            if (record.getUvIndex() > 0) {
                String season = getSeasonFromDate(record.getDate());
                seasonalUV.put(season, seasonalUV.getOrDefault(season, 0f) + record.getUvIndex());
                seasonalCount.put(season, seasonalCount.getOrDefault(season, 0) + 1);
            }
        }
        
        // Calculate seasonal averages and provide recommendations
        for (String season : seasonalUV.keySet()) {
            if (seasonalCount.get(season) > 0) {
                float avgUV = seasonalUV.get(season) / seasonalCount.get(season);
                
                if ("winter".equals(season) && avgUV < 3) {
                    insights.add(new EnvironmentalInsight(
                        "UV Index",
                        "Low Winter UV Detected",
                        String.format("Winter UV levels averaged %.1f, which may be insufficient for vitamin D synthesis.", avgUV),
                        "Consider vitamin D supplements during winter months and maximize outdoor time during sunny winter days.",
                        0.7f
                    ));
                }
            }
        }
    }
    
    /**
     * Analyze circadian rhythm patterns
     */
    private List<EnvironmentalInsight> analyzeCircadianPatterns(List<DayRecord> records) {
        List<EnvironmentalInsight> insights = new ArrayList<>();
        
        try {
            Map<String, Float> circadianActivity = new HashMap<>();
            Map<String, Integer> circadianCount = new HashMap<>();
            
            for (DayRecord record : records) {
                String phase = record.getCurrentCircadianPhase();
                if (phase != null && !phase.isEmpty()) {
                    circadianActivity.put(phase, 
                        circadianActivity.getOrDefault(phase, 0f) + record.getActivityScore());
                    circadianCount.put(phase, 
                        circadianCount.getOrDefault(phase, 0) + 1);
                }
            }
            
            // Calculate averages and find optimal times
            Map<String, Float> circadianAverages = new HashMap<>();
            for (String phase : circadianActivity.keySet()) {
                if (circadianCount.get(phase) > 0) {
                    circadianAverages.put(phase, circadianActivity.get(phase) / circadianCount.get(phase));
                }
            }
            
            // Find peak performance times
            String peakPhase = null;
            float maxActivity = 0;
            for (Map.Entry<String, Float> entry : circadianAverages.entrySet()) {
                if (entry.getValue() > maxActivity) {
                    maxActivity = entry.getValue();
                    peakPhase = entry.getKey();
                }
            }
            
            if (peakPhase != null) {
                insights.add(new EnvironmentalInsight(
                    "Circadian Rhythm",
                    "Peak Performance Time Identified",
                    String.format("Your highest activity levels occur during %s hours (%.1f avg score).", peakPhase, maxActivity),
                    String.format("Schedule important tasks and workouts during %s hours for optimal performance.", peakPhase),
                    0.8f
                ));
            }
            
            // Analyze daylight duration impact
            analyzeDaylightDurationImpact(records, insights);
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing circadian patterns", e);
        }
        
        return insights;
    }
    
    /**
     * Analyze daylight duration impact on activity
     */
    private void analyzeDaylightDurationImpact(List<DayRecord> records, List<EnvironmentalInsight> insights) {
        float longDayActivitySum = 0;
        float shortDayActivitySum = 0;
        int longDayCount = 0;
        int shortDayCount = 0;
        
        for (DayRecord record : records) {
            if (record.getDayLengthMinutes() > 0) {
                if (record.getDayLengthMinutes() > 12 * 60) { // More than 12 hours
                    longDayActivitySum += record.getActivityScore();
                    longDayCount++;
                } else if (record.getDayLengthMinutes() < 10 * 60) { // Less than 10 hours
                    shortDayActivitySum += record.getActivityScore();
                    shortDayCount++;
                }
            }
        }
        
        if (longDayCount > 0 && shortDayCount > 0) {
            float longDayAvg = longDayActivitySum / longDayCount;
            float shortDayAvg = shortDayActivitySum / shortDayCount;
            float difference = longDayAvg - shortDayAvg;
            
            if (difference > 5) {
                insights.add(new EnvironmentalInsight(
                    "Circadian Rhythm",
                    "Daylight Duration Affects Your Activity",
                    String.format("Your activity levels are %.1f points higher on long daylight days (>12h) compared to short daylight days (<10h).", difference),
                    "Consider light therapy or increased indoor lighting during short daylight periods. Plan outdoor activities to maximize natural light exposure.",
                    0.7f
                ));
            }
        }
    }
    
    /**
     * Analyze seasonal patterns and recommendations
     */
    private List<EnvironmentalInsight> analyzeSeasonalPatterns(List<DayRecord> records) {
        List<EnvironmentalInsight> insights = new ArrayList<>();
        
        try {
            Map<String, Float> seasonalActivity = new HashMap<>();
            Map<String, Integer> seasonalCount = new HashMap<>();
            
            for (DayRecord record : records) {
                String season = getSeasonFromDate(record.getDate());
                seasonalActivity.put(season, seasonalActivity.getOrDefault(season, 0f) + record.getActivityScore());
                seasonalCount.put(season, seasonalCount.getOrDefault(season, 0) + 1);
            }
            
            // Calculate seasonal averages
            Map<String, Float> seasonalAverages = new HashMap<>();
            for (String season : seasonalActivity.keySet()) {
                if (seasonalCount.get(season) > 0) {
                    seasonalAverages.put(season, seasonalActivity.get(season) / seasonalCount.get(season));
                }
            }
            
            // Find most and least active seasons
            String mostActiveSeason = null;
            String leastActiveSeason = null;
            float maxActivity = 0;
            float minActivity = Float.MAX_VALUE;
            
            for (Map.Entry<String, Float> entry : seasonalAverages.entrySet()) {
                if (entry.getValue() > maxActivity) {
                    maxActivity = entry.getValue();
                    mostActiveSeason = entry.getKey();
                }
                if (entry.getValue() < minActivity) {
                    minActivity = entry.getValue();
                    leastActiveSeason = entry.getKey();
                }
            }
            
            if (mostActiveSeason != null && leastActiveSeason != null && maxActivity - minActivity > 10) {
                insights.add(new EnvironmentalInsight(
                    "Seasonal Patterns",
                    "Seasonal Activity Variation Detected",
                    String.format("Your activity levels are highest in %s (%.1f avg) and lowest in %s (%.1f avg).", 
                        mostActiveSeason, maxActivity, leastActiveSeason, minActivity),
                    String.format("Prepare for seasonal changes by planning indoor activities during %s and taking advantage of %s weather.", 
                        leastActiveSeason, mostActiveSeason),
                    0.8f
                ));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing seasonal patterns", e);
        }
        
        return insights;
    }
    
    /**
     * Analyze weather correlations with activity
     */
    private List<EnvironmentalInsight> analyzeWeatherCorrelations(List<DayRecord> records) {
        List<EnvironmentalInsight> insights = new ArrayList<>();
        
        try {
            // Temperature correlation
            analyzeTemperatureCorrelation(records, insights);
            
            // Humidity correlation
            analyzeHumidityCorrelation(records, insights);
            
            // Weather condition analysis
            analyzeWeatherConditionImpact(records, insights);
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing weather correlations", e);
        }
        
        return insights;
    }
    
    /**
     * Analyze temperature correlation with activity
     */
    private void analyzeTemperatureCorrelation(List<DayRecord> records, List<EnvironmentalInsight> insights) {
        float optimalTempActivity = 0;
        float extremeTempActivity = 0;
        int optimalTempDays = 0;
        int extremeTempDays = 0;
        
        for (DayRecord record : records) {
            float temp = record.getTemperature();
            if (temp > -50 && temp < 50) { // Valid temperature range
                if (temp >= 18 && temp <= 24) { // Optimal temperature
                    optimalTempActivity += record.getActivityScore();
                    optimalTempDays++;
                } else if (temp < 5 || temp > 30) { // Extreme temperature
                    extremeTempActivity += record.getActivityScore();
                    extremeTempDays++;
                }
            }
        }
        
        if (optimalTempDays > 0 && extremeTempDays > 0) {
            float optimalAvg = optimalTempActivity / optimalTempDays;
            float extremeAvg = extremeTempActivity / extremeTempDays;
            float difference = optimalAvg - extremeAvg;
            
            if (difference > 8) {
                insights.add(new EnvironmentalInsight(
                    "Weather Correlation",
                    "Temperature Significantly Affects Your Activity",
                    String.format("Your activity levels are %.1f points higher in optimal temperatures (18-24°C) compared to extreme temperatures (<5°C or >30°C).", difference),
                    "Plan outdoor activities during moderate temperature days and consider indoor alternatives during extreme weather.",
                    0.8f
                ));
            }
        }
    }
    
    /**
     * Analyze humidity correlation with activity
     */
    private void analyzeHumidityCorrelation(List<DayRecord> records, List<EnvironmentalInsight> insights) {
        float lowHumidityActivity = 0;
        float highHumidityActivity = 0;
        int lowHumidityDays = 0;
        int highHumidityDays = 0;
        
        for (DayRecord record : records) {
            float humidity = record.getHumidity();
            if (humidity > 0 && humidity <= 100) {
                if (humidity < 30) { // Low humidity
                    lowHumidityActivity += record.getActivityScore();
                    lowHumidityDays++;
                } else if (humidity > 70) { // High humidity
                    highHumidityActivity += record.getActivityScore();
                    highHumidityDays++;
                }
            }
        }
        
        if (lowHumidityDays > 0 && highHumidityDays > 0) {
            float lowAvg = lowHumidityActivity / lowHumidityDays;
            float highAvg = highHumidityActivity / highHumidityDays;
            float difference = Math.abs(lowAvg - highAvg);
            
            if (difference > 5) {
                String preferredCondition = lowAvg > highAvg ? "low humidity" : "high humidity";
                insights.add(new EnvironmentalInsight(
                    "Weather Correlation",
                    "Humidity Affects Your Activity Levels",
                    String.format("You're more active in %s conditions (%.1f point difference).", preferredCondition, difference),
                    String.format("Monitor humidity levels and adjust your activity planning accordingly. Consider using humidifiers or dehumidifiers to optimize your environment."),
                    0.6f
                ));
            }
        }
    }
    
    /**
     * Analyze weather condition impact on activity
     */
    private void analyzeWeatherConditionImpact(List<DayRecord> records, List<EnvironmentalInsight> insights) {
        Map<String, Float> conditionActivity = new HashMap<>();
        Map<String, Integer> conditionCount = new HashMap<>();
        
        for (DayRecord record : records) {
            String condition = record.getWeatherCondition();
            if (condition != null && !condition.isEmpty()) {
                conditionActivity.put(condition, conditionActivity.getOrDefault(condition, 0f) + record.getActivityScore());
                conditionCount.put(condition, conditionCount.getOrDefault(condition, 0) + 1);
            }
        }
        
        // Calculate averages and find best/worst conditions
        Map<String, Float> conditionAverages = new HashMap<>();
        for (String condition : conditionActivity.keySet()) {
            if (conditionCount.get(condition) > 2) { // At least 3 occurrences
                conditionAverages.put(condition, conditionActivity.get(condition) / conditionCount.get(condition));
            }
        }
        
        if (conditionAverages.size() >= 2) {
            String bestCondition = null;
            String worstCondition = null;
            float maxActivity = 0;
            float minActivity = Float.MAX_VALUE;
            
            for (Map.Entry<String, Float> entry : conditionAverages.entrySet()) {
                if (entry.getValue() > maxActivity) {
                    maxActivity = entry.getValue();
                    bestCondition = entry.getKey();
                }
                if (entry.getValue() < minActivity) {
                    minActivity = entry.getValue();
                    worstCondition = entry.getKey();
                }
            }
            
            if (bestCondition != null && worstCondition != null && maxActivity - minActivity > 8) {
                insights.add(new EnvironmentalInsight(
                    "Weather Correlation",
                    "Weather Conditions Impact Your Activity",
                    String.format("You're most active during %s (%.1f avg) and least active during %s (%.1f avg).", 
                        bestCondition, maxActivity, worstCondition, minActivity),
                    String.format("Plan outdoor activities during %s weather and have indoor alternatives ready for %s conditions.", 
                        bestCondition, worstCondition),
                    0.7f
                ));
            }
        }
    }
    
    /**
     * Get historical records with environmental data loaded
     */
    private List<DayRecord> getHistoricalRecords(int daysPast) {
        List<DayRecord> records = new ArrayList<>();
        
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -daysPast);
            
            for (int i = 0; i < daysPast; i++) {
                String date = dateFormat.format(calendar.getTime());
                DayRecord record = databaseHelper.getDayRecord(date);
                
                if (record != null) {
                    // Load environmental data
                    databaseHelper.loadEnvironmentalData(record, date);
                    records.add(record);
                }
                
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting historical records", e);
        }
        
        return records;
    }
    
    /**
     * Get season from date string
     */
    private String getSeasonFromDate(String dateString) {
        try {
            Date date = dateFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int month = calendar.get(Calendar.MONTH);
            
            switch (month) {
                case Calendar.DECEMBER:
                case Calendar.JANUARY:
                case Calendar.FEBRUARY:
                    return "winter";
                case Calendar.MARCH:
                case Calendar.APRIL:
                case Calendar.MAY:
                    return "spring";
                case Calendar.JUNE:
                case Calendar.JULY:
                case Calendar.AUGUST:
                    return "summer";
                case Calendar.SEPTEMBER:
                case Calendar.OCTOBER:
                case Calendar.NOVEMBER:
                    return "autumn";
                default:
                    return "unknown";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date for season", e);
            return "unknown";
        }
    }
}