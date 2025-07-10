package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.model.MoodEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analyzing mood patterns and providing insights
 * Calculates trends, patterns, and correlations in mood data
 */
public class MoodAnalyticsService {
    private static final String TAG = "MoodAnalyticsService";
    private MoodTrackingService moodTrackingService;
    
    public MoodAnalyticsService(Context context) {
        moodTrackingService = new MoodTrackingService(context);
    }
    
    /**
     * Analyzes mood trends over different time periods
     */
    public MoodTrendAnalysis analyzeMoodTrends(int days) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodEntry> moodEntries = moodTrackingService.getMoodEntriesInRange(startDate, endDate);
        
        return new MoodTrendAnalysis(moodEntries, days);
    }
    
    /**
     * Analyzes weekly mood patterns
     */
    public WeeklyMoodPattern analyzeWeeklyPattern(int weeks) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.WEEK_OF_YEAR, -weeks);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodEntry> moodEntries = moodTrackingService.getMoodEntriesInRange(startDate, endDate);
        
        return new WeeklyMoodPattern(moodEntries);
    }
    
    /**
     * Analyzes mood triggers and their frequency
     */
    public MoodTriggerAnalysis analyzeMoodTriggers(int days) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodEntry> moodEntries = moodTrackingService.getMoodEntriesInRange(startDate, endDate);
        
        return new MoodTriggerAnalysis(moodEntries);
    }
    
    /**
     * Analyzes correlation between activities and mood
     */
    public ActivityMoodCorrelation analyzeActivityCorrelation(int days) {
        Calendar cal = Calendar.getInstance();
        String endDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        cal.add(Calendar.DAY_OF_MONTH, -days);
        String startDate = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        
        List<MoodEntry> moodEntries = moodTrackingService.getMoodEntriesInRange(startDate, endDate);
        
        return new ActivityMoodCorrelation(moodEntries);
    }
    
    /**
     * Generates personalized mood insights
     */
    public MoodInsights generateInsights(int days) {
        MoodTrendAnalysis trendAnalysis = analyzeMoodTrends(days);
        WeeklyMoodPattern weeklyPattern = analyzeWeeklyPattern(4);
        MoodTriggerAnalysis triggerAnalysis = analyzeMoodTriggers(days);
        ActivityMoodCorrelation activityCorrelation = analyzeActivityCorrelation(days);
        
        return new MoodInsights(trendAnalysis, weeklyPattern, triggerAnalysis, activityCorrelation);
    }
    
    /**
     * Calculates mood stability score
     */
    public float calculateMoodStability(List<MoodEntry> moodEntries) {
        if (moodEntries.size() < 2) return 1.0f;
        
        float totalVariation = 0;
        for (int i = 1; i < moodEntries.size(); i++) {
            float variation = Math.abs(moodEntries.get(i).getMoodScore() - moodEntries.get(i-1).getMoodScore());
            totalVariation += variation;
        }
        
        float averageVariation = totalVariation / (moodEntries.size() - 1);
        return Math.max(0, 1.0f - (averageVariation / 9.0f)); // Normalize to 0-1 scale
    }
    
    /**
     * Predicts mood based on historical patterns
     */
    public MoodPrediction predictMood(String date, float temperature, String weatherCondition) {
        // Get historical data for similar conditions
        List<MoodEntry> recentEntries = moodTrackingService.getRecentMoodEntries(100);
        
        // Filter entries with similar weather conditions
        List<MoodEntry> similarConditions = new ArrayList<>();
        for (MoodEntry entry : recentEntries) {
            if (Math.abs(entry.getTemperature() - temperature) < 5 && 
                entry.getWeatherCondition() != null && 
                entry.getWeatherCondition().equals(weatherCondition)) {
                similarConditions.add(entry);
            }
        }
        
        if (similarConditions.isEmpty()) {
            // Use general average if no similar conditions found
            float avgMood = calculateAverageMood(recentEntries);
            return new MoodPrediction(avgMood, 0.5f, "No similar weather patterns found");
        }
        
        float avgMood = calculateAverageMood(similarConditions);
        float confidence = Math.min(1.0f, similarConditions.size() / 20.0f); // Higher confidence with more data
        
        return new MoodPrediction(avgMood, confidence, 
                "Based on " + similarConditions.size() + " similar weather patterns");
    }
    
    /**
     * Calculates average mood from a list of entries
     */
    private float calculateAverageMood(List<MoodEntry> entries) {
        if (entries.isEmpty()) return 5.0f; // Neutral mood
        
        float sum = 0;
        for (MoodEntry entry : entries) {
            sum += entry.getMoodScore();
        }
        
        return sum / entries.size();
    }
    
    /**
     * Inner class for mood trend analysis
     */
    public static class MoodTrendAnalysis {
        private float averageMood;
        private float moodChange;
        private String trend;
        private float volatility;
        private List<Float> dailyAverages;
        
        public MoodTrendAnalysis(List<MoodEntry> entries, int days) {
            calculateTrend(entries, days);
        }
        
        private void calculateTrend(List<MoodEntry> entries, int days) {
            if (entries.isEmpty()) {
                averageMood = 5.0f;
                moodChange = 0.0f;
                trend = "stable";
                volatility = 0.0f;
                dailyAverages = new ArrayList<>();
                return;
            }
            
            // Calculate average mood
            float sum = 0;
            for (MoodEntry entry : entries) {
                sum += entry.getMoodScore();
            }
            averageMood = sum / entries.size();
            
            // Calculate daily averages
            Map<String, List<Float>> dailyScores = new HashMap<>();
            for (MoodEntry entry : entries) {
                dailyScores.computeIfAbsent(entry.getDate(), k -> new ArrayList<>()).add((float) entry.getMoodScore());
            }
            
            dailyAverages = new ArrayList<>();
            for (List<Float> scores : dailyScores.values()) {
                float dailySum = 0;
                for (Float score : scores) {
                    dailySum += score;
                }
                dailyAverages.add(dailySum / scores.size());
            }
            
            // Calculate trend
            if (dailyAverages.size() >= 2) {
                float firstHalf = 0, secondHalf = 0;
                int midPoint = dailyAverages.size() / 2;
                
                for (int i = 0; i < midPoint; i++) {
                    firstHalf += dailyAverages.get(i);
                }
                firstHalf /= midPoint;
                
                for (int i = midPoint; i < dailyAverages.size(); i++) {
                    secondHalf += dailyAverages.get(i);
                }
                secondHalf /= (dailyAverages.size() - midPoint);
                
                moodChange = secondHalf - firstHalf;
                
                if (moodChange > 0.5f) {
                    trend = "improving";
                } else if (moodChange < -0.5f) {
                    trend = "declining";
                } else {
                    trend = "stable";
                }
            } else {
                moodChange = 0.0f;
                trend = "stable";
            }
            
            // Calculate volatility
            float variance = 0;
            for (MoodEntry entry : entries) {
                variance += Math.pow(entry.getMoodScore() - averageMood, 2);
            }
            volatility = (float) Math.sqrt(variance / entries.size());
        }
        
        // Getters
        public float getAverageMood() { return averageMood; }
        public float getMoodChange() { return moodChange; }
        public String getTrend() { return trend; }
        public float getVolatility() { return volatility; }
        public List<Float> getDailyAverages() { return dailyAverages; }
    }
    
    /**
     * Inner class for weekly mood pattern analysis
     */
    public static class WeeklyMoodPattern {
        private Map<String, Float> dayOfWeekAverages;
        private String bestDay;
        private String worstDay;
        
        public WeeklyMoodPattern(List<MoodEntry> entries) {
            calculateWeeklyPattern(entries);
        }
        
        private void calculateWeeklyPattern(List<MoodEntry> entries) {
            dayOfWeekAverages = new HashMap<>();
            Map<String, List<Float>> dayScores = new HashMap<>();
            
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            
            for (MoodEntry entry : entries) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(entry.getTimestamp());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                
                String dayName = days[(dayOfWeek + 5) % 7]; // Adjust for Monday = 0
                dayScores.computeIfAbsent(dayName, k -> new ArrayList<>()).add((float) entry.getMoodScore());
            }
            
            // Calculate averages for each day
            for (String day : days) {
                List<Float> scores = dayScores.get(day);
                if (scores != null && !scores.isEmpty()) {
                    float sum = 0;
                    for (Float score : scores) {
                        sum += score;
                    }
                    dayOfWeekAverages.put(day, sum / scores.size());
                } else {
                    dayOfWeekAverages.put(day, 5.0f); // Neutral if no data
                }
            }
            
            // Find best and worst days
            float maxMood = Collections.max(dayOfWeekAverages.values());
            float minMood = Collections.min(dayOfWeekAverages.values());
            
            for (Map.Entry<String, Float> entry : dayOfWeekAverages.entrySet()) {
                if (entry.getValue() == maxMood) {
                    bestDay = entry.getKey();
                }
                if (entry.getValue() == minMood) {
                    worstDay = entry.getKey();
                }
            }
        }
        
        // Getters
        public Map<String, Float> getDayOfWeekAverages() { return dayOfWeekAverages; }
        public String getBestDay() { return bestDay; }
        public String getWorstDay() { return worstDay; }
    }
    
    /**
     * Inner class for mood trigger analysis
     */
    public static class MoodTriggerAnalysis {
        private Map<String, Integer> triggerFrequency;
        private Map<String, Float> triggerMoodImpact;
        private List<String> topPositiveTriggers;
        private List<String> topNegativeTriggers;
        
        public MoodTriggerAnalysis(List<MoodEntry> entries) {
            analyzeTriggers(entries);
        }
        
        private void analyzeTriggers(List<MoodEntry> entries) {
            triggerFrequency = new HashMap<>();
            Map<String, List<Float>> triggerScores = new HashMap<>();
            
            for (MoodEntry entry : entries) {
                if (entry.getMoodTriggers() != null) {
                    for (String trigger : entry.getMoodTriggers()) {
                        triggerFrequency.put(trigger, triggerFrequency.getOrDefault(trigger, 0) + 1);
                        triggerScores.computeIfAbsent(trigger, k -> new ArrayList<>()).add((float) entry.getMoodScore());
                    }
                }
            }
            
            // Calculate mood impact for each trigger
            triggerMoodImpact = new HashMap<>();
            for (Map.Entry<String, List<Float>> entry : triggerScores.entrySet()) {
                float sum = 0;
                for (Float score : entry.getValue()) {
                    sum += score;
                }
                triggerMoodImpact.put(entry.getKey(), sum / entry.getValue().size());
            }
            
            // Find top positive and negative triggers
            topPositiveTriggers = new ArrayList<>();
            topNegativeTriggers = new ArrayList<>();
            
            for (Map.Entry<String, Float> entry : triggerMoodImpact.entrySet()) {
                if (entry.getValue() > 6.0f) {
                    topPositiveTriggers.add(entry.getKey());
                } else if (entry.getValue() < 4.0f) {
                    topNegativeTriggers.add(entry.getKey());
                }
            }
        }
        
        // Getters
        public Map<String, Integer> getTriggerFrequency() { return triggerFrequency; }
        public Map<String, Float> getTriggerMoodImpact() { return triggerMoodImpact; }
        public List<String> getTopPositiveTriggers() { return topPositiveTriggers; }
        public List<String> getTopNegativeTriggers() { return topNegativeTriggers; }
    }
    
    /**
     * Inner class for activity-mood correlation
     */
    public static class ActivityMoodCorrelation {
        private Map<String, Float> activityMoodImpact;
        private List<String> moodBoostingActivities;
        private List<String> moodLoweringActivities;
        
        public ActivityMoodCorrelation(List<MoodEntry> entries) {
            analyzeActivityCorrelation(entries);
        }
        
        private void analyzeActivityCorrelation(List<MoodEntry> entries) {
            Map<String, List<Float>> activityScores = new HashMap<>();
            
            for (MoodEntry entry : entries) {
                if (entry.getActivities() != null) {
                    for (String activity : entry.getActivities()) {
                        activityScores.computeIfAbsent(activity, k -> new ArrayList<>()).add((float) entry.getMoodScore());
                    }
                }
            }
            
            // Calculate mood impact for each activity
            activityMoodImpact = new HashMap<>();
            for (Map.Entry<String, List<Float>> entry : activityScores.entrySet()) {
                float sum = 0;
                for (Float score : entry.getValue()) {
                    sum += score;
                }
                activityMoodImpact.put(entry.getKey(), sum / entry.getValue().size());
            }
            
            // Find mood boosting and lowering activities
            moodBoostingActivities = new ArrayList<>();
            moodLoweringActivities = new ArrayList<>();
            
            for (Map.Entry<String, Float> entry : activityMoodImpact.entrySet()) {
                if (entry.getValue() > 6.5f) {
                    moodBoostingActivities.add(entry.getKey());
                } else if (entry.getValue() < 4.5f) {
                    moodLoweringActivities.add(entry.getKey());
                }
            }
        }
        
        // Getters
        public Map<String, Float> getActivityMoodImpact() { return activityMoodImpact; }
        public List<String> getMoodBoostingActivities() { return moodBoostingActivities; }
        public List<String> getMoodLoweringActivities() { return moodLoweringActivities; }
    }
    
    /**
     * Inner class for mood prediction
     */
    public static class MoodPrediction {
        private float predictedMood;
        private float confidence;
        private String explanation;
        
        public MoodPrediction(float predictedMood, float confidence, String explanation) {
            this.predictedMood = predictedMood;
            this.confidence = confidence;
            this.explanation = explanation;
        }
        
        // Getters
        public float getPredictedMood() { return predictedMood; }
        public float getConfidence() { return confidence; }
        public String getExplanation() { return explanation; }
    }
    
    /**
     * Inner class for comprehensive mood insights
     */
    public static class MoodInsights {
        private MoodTrendAnalysis trendAnalysis;
        private WeeklyMoodPattern weeklyPattern;
        private MoodTriggerAnalysis triggerAnalysis;
        private ActivityMoodCorrelation activityCorrelation;
        private List<String> insights;
        
        public MoodInsights(MoodTrendAnalysis trendAnalysis, WeeklyMoodPattern weeklyPattern,
                           MoodTriggerAnalysis triggerAnalysis, ActivityMoodCorrelation activityCorrelation) {
            this.trendAnalysis = trendAnalysis;
            this.weeklyPattern = weeklyPattern;
            this.triggerAnalysis = triggerAnalysis;
            this.activityCorrelation = activityCorrelation;
            generateInsights();
        }
        
        private void generateInsights() {
            insights = new ArrayList<>();
            
            // Trend insights
            if (trendAnalysis.getTrend().equals("improving")) {
                insights.add("Your mood has been improving lately! Keep up the good work.");
            } else if (trendAnalysis.getTrend().equals("declining")) {
                insights.add("Your mood has been declining. Consider reaching out for support.");
            }
            
            // Weekly pattern insights
            if (weeklyPattern.getBestDay() != null) {
                insights.add("Your mood tends to be best on " + weeklyPattern.getBestDay() + "s.");
            }
            if (weeklyPattern.getWorstDay() != null) {
                insights.add("Your mood tends to be lowest on " + weeklyPattern.getWorstDay() + "s.");
            }
            
            // Activity insights
            if (!activityCorrelation.getMoodBoostingActivities().isEmpty()) {
                insights.add("Activities that boost your mood: " + 
                    String.join(", ", activityCorrelation.getMoodBoostingActivities()));
            }
            
            // Trigger insights
            if (!triggerAnalysis.getTopNegativeTriggers().isEmpty()) {
                insights.add("Watch out for these mood triggers: " + 
                    String.join(", ", triggerAnalysis.getTopNegativeTriggers()));
            }
        }
        
        // Getters
        public MoodTrendAnalysis getTrendAnalysis() { return trendAnalysis; }
        public WeeklyMoodPattern getWeeklyPattern() { return weeklyPattern; }
        public MoodTriggerAnalysis getTriggerAnalysis() { return triggerAnalysis; }
        public ActivityMoodCorrelation getActivityCorrelation() { return activityCorrelation; }
        public List<String> getInsights() { return insights; }
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