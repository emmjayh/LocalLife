package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.SleepData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for tracking sleep quality and patterns
 */
public class SleepTrackingService {
    private static final String TAG = "SleepTrackingService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public SleepTrackingService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Log sleep data
     */
    public void logSleepData(SleepData sleepData, SleepCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                // Calculate sleep quality score
                sleepData.calculateSleepQualityScore();
                
                // Insert into database
                long id = databaseHelper.insertSleepData(sleepData);
                sleepData.setId((int) id);
                
                Log.d(TAG, "Sleep data logged: " + sleepData.getFormattedSleepDuration() + 
                          " (Quality: " + sleepData.getSleepQualityScore() + ")");
                
                if (callback != null) {
                    callback.onSleepDataLogged(sleepData);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error logging sleep data", e);
                if (callback != null) {
                    callback.onError("Failed to log sleep data: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get sleep data for a specific date
     */
    public void getSleepDataForDate(String date, SleepDataCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                SleepData sleepData = databaseHelper.getSleepDataForDate(date);
                if (callback != null) {
                    callback.onSleepDataReceived(sleepData);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting sleep data", e);
                if (callback != null) {
                    callback.onError("Failed to get sleep data: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get sleep statistics for a period
     */
    public void getSleepStatistics(int days, SleepStatisticsCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<SleepData> sleepDataList = databaseHelper.getSleepDataForPeriod(days);
                SleepStatistics stats = calculateSleepStatistics(sleepDataList);
                
                if (callback != null) {
                    callback.onSleepStatisticsReceived(stats);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error calculating sleep statistics", e);
                if (callback != null) {
                    callback.onError("Failed to calculate sleep statistics: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Analyze sleep patterns
     */
    public void analyzeSleepPatterns(SleepPatternCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<SleepData> sleepDataList = databaseHelper.getSleepDataForPeriod(30);
                
                if (sleepDataList.size() < 7) {
                    if (callback != null) {
                        callback.onError("Not enough sleep data for pattern analysis");
                    }
                    return;
                }
                
                SleepPatternAnalysis analysis = new SleepPatternAnalysis();
                
                // Calculate average sleep times
                analysis.averageBedTime = calculateAverageBedTime(sleepDataList);
                analysis.averageWakeTime = calculateAverageWakeTime(sleepDataList);
                analysis.averageSleepDuration = calculateAverageSleepDuration(sleepDataList);
                
                // Analyze sleep quality trends
                analysis.sleepQualityTrend = calculateSleepQualityTrend(sleepDataList);
                
                // Identify patterns
                analysis.weekdayVsWeekendPattern = analyzeWeekdayVsWeekend(sleepDataList);
                analysis.optimalSleepDuration = findOptimalSleepDuration(sleepDataList);
                
                // Environmental correlations
                analysis.temperatureCorrelation = analyzeEnvironmentalCorrelation(sleepDataList, "temperature");
                analysis.humidityCorrelation = analyzeEnvironmentalCorrelation(sleepDataList, "humidity");
                
                if (callback != null) {
                    callback.onSleepPatternAnalysisReceived(analysis);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing sleep patterns", e);
                if (callback != null) {
                    callback.onError("Failed to analyze sleep patterns: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Generate sleep recommendations
     */
    public void generateSleepRecommendations(SleepRecommendationCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<SleepData> recentSleep = databaseHelper.getSleepDataForPeriod(7);
                List<SleepRecommendation> recommendations = new ArrayList<>();
                
                if (recentSleep.isEmpty()) {
                    recommendations.add(new SleepRecommendation(
                        "Start Tracking", 
                        "Begin logging your sleep data to get personalized recommendations",
                        SleepRecommendation.Priority.HIGH
                    ));
                } else {
                    // Analyze recent sleep data
                    float avgQuality = 0f;
                    int totalSleep = 0;
                    int shortSleepCount = 0;
                    
                    for (SleepData sleep : recentSleep) {
                        avgQuality += sleep.getSleepQualityScore();
                        totalSleep += sleep.getTotalSleepMinutes();
                        if (sleep.getTotalSleepMinutes() < 420) { // Less than 7 hours
                            shortSleepCount++;
                        }
                    }
                    
                    avgQuality /= recentSleep.size();
                    float avgSleepHours = (totalSleep / (float) recentSleep.size()) / 60f;
                    
                    // Generate recommendations based on analysis
                    if (avgQuality < 60f) {
                        recommendations.add(new SleepRecommendation(
                            "Improve Sleep Quality",
                            "Your average sleep quality is below optimal. Consider improving your sleep environment",
                            SleepRecommendation.Priority.HIGH
                        ));
                    }
                    
                    if (avgSleepHours < 7f) {
                        recommendations.add(new SleepRecommendation(
                            "Increase Sleep Duration",
                            String.format("You're averaging %.1f hours of sleep. Aim for 7-9 hours", avgSleepHours),
                            SleepRecommendation.Priority.MEDIUM
                        ));
                    }
                    
                    if (shortSleepCount > 3) {
                        recommendations.add(new SleepRecommendation(
                            "Consistent Sleep Schedule",
                            "Try to maintain a regular bedtime and wake time",
                            SleepRecommendation.Priority.MEDIUM
                        ));
                    }
                    
                    // Environmental recommendations
                    float avgTemp = 0f;
                    int tempCount = 0;
                    for (SleepData sleep : recentSleep) {
                        if (sleep.getRoomTemperature() > 0) {
                            avgTemp += sleep.getRoomTemperature();
                            tempCount++;
                        }
                    }
                    
                    if (tempCount > 0) {
                        avgTemp /= tempCount;
                        if (avgTemp > 22f || avgTemp < 18f) {
                            recommendations.add(new SleepRecommendation(
                                "Optimize Room Temperature",
                                "Keep your bedroom between 18-22°C for optimal sleep",
                                SleepRecommendation.Priority.LOW
                            ));
                        }
                    }
                }
                
                if (callback != null) {
                    callback.onSleepRecommendationsReceived(recommendations);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating sleep recommendations", e);
                if (callback != null) {
                    callback.onError("Failed to generate recommendations: " + e.getMessage());
                }
            }
        });
    }
    
    // Helper methods for analysis
    private SleepStatistics calculateSleepStatistics(List<SleepData> sleepDataList) {
        SleepStatistics stats = new SleepStatistics();
        
        if (sleepDataList.isEmpty()) {
            return stats;
        }
        
        float totalQuality = 0f;
        int totalSleep = 0;
        int totalDeepSleep = 0;
        int totalRemSleep = 0;
        int totalAwakenings = 0;
        
        for (SleepData sleep : sleepDataList) {
            totalQuality += sleep.getSleepQualityScore();
            totalSleep += sleep.getTotalSleepMinutes();
            totalDeepSleep += sleep.getDeepSleepMinutes();
            totalRemSleep += sleep.getRemSleepMinutes();
            totalAwakenings += sleep.getAwakeDuringNight();
        }
        
        int count = sleepDataList.size();
        stats.averageQuality = totalQuality / count;
        stats.averageSleepDuration = totalSleep / count;
        stats.averageDeepSleep = totalDeepSleep / count;
        stats.averageRemSleep = totalRemSleep / count;
        stats.averageAwakenings = totalAwakenings / (float) count;
        
        return stats;
    }
    
    private Date calculateAverageBedTime(List<SleepData> sleepDataList) {
        if (sleepDataList.isEmpty()) return new Date();
        
        long totalTime = 0;
        int count = 0;
        
        for (SleepData sleep : sleepDataList) {
            if (sleep.getBedTime() != null) {
                // Convert to minutes since midnight
                Calendar cal = Calendar.getInstance();
                cal.setTime(sleep.getBedTime());
                int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                totalTime += minutes;
                count++;
            }
        }
        
        if (count == 0) return new Date();
        
        int avgMinutes = (int) (totalTime / count);
        Calendar avgTime = Calendar.getInstance();
        avgTime.set(Calendar.HOUR_OF_DAY, avgMinutes / 60);
        avgTime.set(Calendar.MINUTE, avgMinutes % 60);
        
        return avgTime.getTime();
    }
    
    private Date calculateAverageWakeTime(List<SleepData> sleepDataList) {
        // Similar to calculateAverageBedTime but for wake time
        if (sleepDataList.isEmpty()) return new Date();
        
        long totalTime = 0;
        int count = 0;
        
        for (SleepData sleep : sleepDataList) {
            if (sleep.getWakeTime() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sleep.getWakeTime());
                int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                totalTime += minutes;
                count++;
            }
        }
        
        if (count == 0) return new Date();
        
        int avgMinutes = (int) (totalTime / count);
        Calendar avgTime = Calendar.getInstance();
        avgTime.set(Calendar.HOUR_OF_DAY, avgMinutes / 60);
        avgTime.set(Calendar.MINUTE, avgMinutes % 60);
        
        return avgTime.getTime();
    }
    
    private int calculateAverageSleepDuration(List<SleepData> sleepDataList) {
        if (sleepDataList.isEmpty()) return 0;
        
        int totalMinutes = 0;
        for (SleepData sleep : sleepDataList) {
            totalMinutes += sleep.getTotalSleepMinutes();
        }
        
        return totalMinutes / sleepDataList.size();
    }
    
    private String calculateSleepQualityTrend(List<SleepData> sleepDataList) {
        if (sleepDataList.size() < 3) return "Insufficient data";
        
        // Compare first third to last third
        int thirdSize = sleepDataList.size() / 3;
        float firstThirdAvg = 0f;
        float lastThirdAvg = 0f;
        
        for (int i = 0; i < thirdSize; i++) {
            firstThirdAvg += sleepDataList.get(i).getSleepQualityScore();
        }
        firstThirdAvg /= thirdSize;
        
        for (int i = sleepDataList.size() - thirdSize; i < sleepDataList.size(); i++) {
            lastThirdAvg += sleepDataList.get(i).getSleepQualityScore();
        }
        lastThirdAvg /= thirdSize;
        
        float difference = lastThirdAvg - firstThirdAvg;
        
        if (difference > 5f) return "Improving";
        if (difference < -5f) return "Declining";
        return "Stable";
    }
    
    private String analyzeWeekdayVsWeekend(List<SleepData> sleepDataList) {
        float weekdayAvg = 0f;
        float weekendAvg = 0f;
        int weekdayCount = 0;
        int weekendCount = 0;
        
        for (SleepData sleep : sleepDataList) {
            try {
                Date sleepDate = dateFormat.parse(sleep.getDate());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sleepDate);
                
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    weekendAvg += sleep.getTotalSleepMinutes();
                    weekendCount++;
                } else {
                    weekdayAvg += sleep.getTotalSleepMinutes();
                    weekdayCount++;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing sleep date", e);
            }
        }
        
        if (weekdayCount > 0) weekdayAvg /= weekdayCount;
        if (weekendCount > 0) weekendAvg /= weekendCount;
        
        float difference = (weekendAvg - weekdayAvg) / 60f; // Convert to hours
        
        if (difference > 1f) {
            return "Sleep significantly more on weekends";
        } else if (difference < -1f) {
            return "Sleep less on weekends";
        } else {
            return "Consistent sleep pattern";
        }
    }
    
    private int findOptimalSleepDuration(List<SleepData> sleepDataList) {
        // Find sleep duration that correlates with highest quality
        float bestQuality = 0f;
        int optimalDuration = 480; // 8 hours default
        
        for (SleepData sleep : sleepDataList) {
            if (sleep.getSleepQualityScore() > bestQuality) {
                bestQuality = sleep.getSleepQualityScore();
                optimalDuration = sleep.getTotalSleepMinutes();
            }
        }
        
        return optimalDuration;
    }
    
    private float analyzeEnvironmentalCorrelation(List<SleepData> sleepDataList, String factor) {
        // Simplified correlation analysis
        if (sleepDataList.size() < 5) return 0f;
        
        List<Float> x = new ArrayList<>();
        List<Float> y = new ArrayList<>();
        
        for (SleepData sleep : sleepDataList) {
            float envFactor = 0f;
            
            if ("temperature".equals(factor)) {
                envFactor = sleep.getRoomTemperature();
            } else if ("humidity".equals(factor)) {
                envFactor = sleep.getRoomHumidity();
            }
            
            if (envFactor > 0f) {
                x.add(envFactor);
                y.add(sleep.getSleepQualityScore());
            }
        }
        
        if (x.size() < 3) return 0f;
        
        return calculateCorrelation(x, y);
    }
    
    private float calculateCorrelation(List<Float> x, List<Float> y) {
        if (x.size() != y.size() || x.size() < 2) return 0f;
        
        int n = x.size();
        float meanX = 0f, meanY = 0f;
        
        for (int i = 0; i < n; i++) {
            meanX += x.get(i);
            meanY += y.get(i);
        }
        meanX /= n;
        meanY /= n;
        
        float numerator = 0f;
        float sumXX = 0f, sumYY = 0f;
        
        for (int i = 0; i < n; i++) {
            float dx = x.get(i) - meanX;
            float dy = y.get(i) - meanY;
            
            numerator += dx * dy;
            sumXX += dx * dx;
            sumYY += dy * dy;
        }
        
        float denominator = (float) Math.sqrt(sumXX * sumYY);
        
        if (denominator == 0f) return 0f;
        
        return numerator / denominator;
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes
    public static class SleepStatistics {
        public float averageQuality;
        public int averageSleepDuration;
        public int averageDeepSleep;
        public int averageRemSleep;
        public float averageAwakenings;
    }
    
    public static class SleepPatternAnalysis {
        public Date averageBedTime;
        public Date averageWakeTime;
        public int averageSleepDuration;
        public String sleepQualityTrend;
        public String weekdayVsWeekendPattern;
        public int optimalSleepDuration;
        public float temperatureCorrelation;
        public float humidityCorrelation;
    }
    
    public static class SleepRecommendation {
        public String title;
        public String description;
        public Priority priority;
        
        public SleepRecommendation(String title, String description, Priority priority) {
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
        
        public enum Priority {
            LOW, MEDIUM, HIGH
        }
    }
    
    // Callback interfaces
    public interface SleepCallback {
        void onSleepDataLogged(SleepData sleepData);
        void onError(String error);
    }
    
    public interface SleepDataCallback {
        void onSleepDataReceived(SleepData sleepData);
        void onError(String error);
    }
    
    public interface SleepStatisticsCallback {
        void onSleepStatisticsReceived(SleepStatistics statistics);
        void onError(String error);
    }
    
    public interface SleepPatternCallback {
        void onSleepPatternAnalysisReceived(SleepPatternAnalysis analysis);
        void onError(String error);
    }
    
    public interface SleepRecommendationCallback {
        void onSleepRecommendationsReceived(List<SleepRecommendation> recommendations);
        void onError(String error);
    }
}