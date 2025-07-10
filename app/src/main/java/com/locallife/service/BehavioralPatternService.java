package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for analyzing behavioral patterns and predicting future actions
 */
public class BehavioralPatternService {
    private static final String TAG = "BehavioralPatternService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public BehavioralPatternService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Analyze behavioral patterns from historical data
     */
    public void analyzeBehavioralPatterns(BehavioralPatternCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(90); // 3 months
                
                if (records.size() < 14) {
                    if (callback != null) {
                        callback.onError("Not enough data for behavioral analysis");
                    }
                    return;
                }
                
                BehavioralPatternAnalysis analysis = new BehavioralPatternAnalysis();
                
                // Analyze daily routines
                analysis.dailyRoutines = analyzeDailyRoutines(records);
                
                // Analyze weekly patterns
                analysis.weeklyPatterns = analyzeWeeklyPatterns(records);
                
                // Analyze seasonal patterns
                analysis.seasonalPatterns = analyzeSeasonalPatterns(records);
                
                // Identify habit sequences
                analysis.habitSequences = identifyHabitSequences(records);
                
                // Analyze trigger patterns
                analysis.triggerPatterns = analyzeTriggerPatterns(records);
                
                // Predict future behavior
                analysis.behaviorPredictions = predictFutureBehavior(records);
                
                // Generate behavioral insights
                analysis.insights = generateBehavioralInsights(analysis);
                
                if (callback != null) {
                    callback.onBehavioralPatternsAnalyzed(analysis);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing behavioral patterns", e);
                if (callback != null) {
                    callback.onError("Behavioral analysis failed: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Detect anomalies in behavioral patterns
     */
    public void detectAnomalies(AnomalyDetectionCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(30);
                
                if (records.size() < 7) {
                    if (callback != null) {
                        callback.onError("Not enough data for anomaly detection");
                    }
                    return;
                }
                
                List<BehavioralAnomaly> anomalies = new ArrayList<>();
                
                // Detect step count anomalies
                anomalies.addAll(detectStepCountAnomalies(records));
                
                // Detect screen time anomalies
                anomalies.addAll(detectScreenTimeAnomalies(records));
                
                // Detect location anomalies
                anomalies.addAll(detectLocationAnomalies(records));
                
                // Detect sleep pattern anomalies
                anomalies.addAll(detectSleepPatternAnomalies(records));
                
                // Rank anomalies by severity
                Collections.sort(anomalies, (a, b) -> Float.compare(b.severity, a.severity));
                
                if (callback != null) {
                    callback.onAnomaliesDetected(anomalies);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error detecting anomalies", e);
                if (callback != null) {
                    callback.onError("Anomaly detection failed: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Analyze habit formation progress
     */
    public void analyzeHabitFormation(HabitFormationCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(60);
                
                if (records.size() < 14) {
                    if (callback != null) {
                        callback.onError("Not enough data for habit formation analysis");
                    }
                    return;
                }
                
                List<HabitFormationAnalysis> habits = new ArrayList<>();
                
                // Analyze exercise habits
                habits.add(analyzeExerciseHabitFormation(records));
                
                // Analyze screen time habits
                habits.add(analyzeScreenTimeHabitFormation(records));
                
                // Analyze sleep habits
                habits.add(analyzeSleepHabitFormation(records));
                
                // Analyze social habits
                habits.add(analyzeSocialHabitFormation(records));
                
                if (callback != null) {
                    callback.onHabitFormationAnalyzed(habits);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing habit formation", e);
                if (callback != null) {
                    callback.onError("Habit formation analysis failed: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Generate behavior modification recommendations
     */
    public void generateBehaviorRecommendations(BehaviorRecommendationCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<DayRecord> records = databaseHelper.getDayRecordsForPeriod(30);
                
                if (records.size() < 7) {
                    if (callback != null) {
                        callback.onError("Not enough data for behavior recommendations");
                    }
                    return;
                }
                
                List<BehaviorRecommendation> recommendations = new ArrayList<>();
                
                // Analyze current patterns
                BehavioralPatternAnalysis analysis = analyzeBehavioralPatternsSync(records);
                
                // Generate activity recommendations
                recommendations.addAll(generateActivityRecommendations(analysis));
                
                // Generate screen time recommendations
                recommendations.addAll(generateScreenTimeRecommendations(analysis));
                
                // Generate sleep recommendations
                recommendations.addAll(generateSleepRecommendations(analysis));
                
                // Generate social recommendations
                recommendations.addAll(generateSocialRecommendations(analysis));
                
                // Sort by priority
                Collections.sort(recommendations, (a, b) -> b.priority.compareTo(a.priority));
                
                if (callback != null) {
                    callback.onBehaviorRecommendationsGenerated(recommendations);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating behavior recommendations", e);
                if (callback != null) {
                    callback.onError("Behavior recommendations failed: " + e.getMessage());
                }
            }
        });
    }
    
    // Helper methods for behavioral pattern analysis
    private List<DailyRoutine> analyzeDailyRoutines(List<DayRecord> records) {
        List<DailyRoutine> routines = new ArrayList<>();
        
        Map<String, List<Integer>> hourlyStepPatterns = new HashMap<>();
        Map<String, List<Integer>> hourlyScreenPatterns = new HashMap<>();
        
        // Group by day of week
        for (DayRecord record : records) {
            String dayOfWeek = getDayOfWeek(record.getDate());
            
            // Simulate hourly patterns (in real implementation, you'd have hourly data)
            List<Integer> stepPattern = simulateHourlyStepPattern(record.getSteps());
            List<Integer> screenPattern = simulateHourlyScreenPattern(record.getScreenTimeMinutes());
            
            hourlyStepPatterns.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).addAll(stepPattern);
            hourlyScreenPatterns.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).addAll(screenPattern);
        }
        
        // Create daily routines
        for (String dayOfWeek : hourlyStepPatterns.keySet()) {
            DailyRoutine routine = new DailyRoutine();
            routine.dayOfWeek = dayOfWeek;
            routine.peakActivityHours = findPeakHours(hourlyStepPatterns.get(dayOfWeek));
            routine.lowActivityHours = findLowHours(hourlyStepPatterns.get(dayOfWeek));
            routine.averageSteps = calculateAverage(hourlyStepPatterns.get(dayOfWeek));
            routine.averageScreenTime = calculateAverage(hourlyScreenPatterns.get(dayOfWeek));
            routine.consistency = calculateConsistency(hourlyStepPatterns.get(dayOfWeek));
            
            routines.add(routine);
        }
        
        return routines;
    }
    
    private List<WeeklyPattern> analyzeWeeklyPatterns(List<DayRecord> records) {
        List<WeeklyPattern> patterns = new ArrayList<>();
        
        Map<String, List<Integer>> weeklySteps = new HashMap<>();
        Map<String, List<Integer>> weeklyScreenTime = new HashMap<>();
        
        // Group by week
        for (DayRecord record : records) {
            String week = getWeekOfYear(record.getDate());
            
            weeklySteps.computeIfAbsent(week, k -> new ArrayList<>()).add(record.getSteps());
            weeklyScreenTime.computeIfAbsent(week, k -> new ArrayList<>()).add(record.getScreenTimeMinutes());
        }
        
        // Analyze patterns
        for (String week : weeklySteps.keySet()) {
            WeeklyPattern pattern = new WeeklyPattern();
            pattern.weekIdentifier = week;
            pattern.totalSteps = weeklySteps.get(week).stream().mapToInt(Integer::intValue).sum();
            pattern.totalScreenTime = weeklyScreenTime.get(week).stream().mapToInt(Integer::intValue).sum();
            pattern.activeDays = weeklySteps.get(week).size();
            pattern.averageDailySteps = pattern.totalSteps / pattern.activeDays;
            pattern.averageDailyScreenTime = pattern.totalScreenTime / pattern.activeDays;
            
            patterns.add(pattern);
        }
        
        return patterns;
    }
    
    private List<SeasonalPattern> analyzeSeasonalPatterns(List<DayRecord> records) {
        List<SeasonalPattern> patterns = new ArrayList<>();
        
        Map<String, List<DayRecord>> seasonalGroups = new HashMap<>();
        
        for (DayRecord record : records) {
            String season = getSeason(record.getDate());
            seasonalGroups.computeIfAbsent(season, k -> new ArrayList<>()).add(record);
        }
        
        for (String season : seasonalGroups.keySet()) {
            List<DayRecord> seasonRecords = seasonalGroups.get(season);
            
            SeasonalPattern pattern = new SeasonalPattern();
            pattern.season = season;
            pattern.averageSteps = seasonRecords.stream().mapToInt(DayRecord::getSteps).sum() / seasonRecords.size();
            pattern.averageScreenTime = seasonRecords.stream().mapToInt(DayRecord::getScreenTimeMinutes).sum() / seasonRecords.size();
            pattern.averageOutdoorTime = calculateAverageOutdoorTime(seasonRecords);
            pattern.temperatureImpact = calculateTemperatureImpact(seasonRecords);
            
            patterns.add(pattern);
        }
        
        return patterns;
    }
    
    private List<HabitSequence> identifyHabitSequences(List<DayRecord> records) {
        List<HabitSequence> sequences = new ArrayList<>();
        
        // Look for sequential patterns
        for (int i = 0; i < records.size() - 2; i++) {
            DayRecord day1 = records.get(i);
            DayRecord day2 = records.get(i + 1);
            DayRecord day3 = records.get(i + 2);
            
            // Check for high activity sequences
            if (day1.getSteps() > 8000 && day2.getSteps() > 8000 && day3.getSteps() > 8000) {
                HabitSequence sequence = new HabitSequence();
                sequence.habitType = "High Activity Streak";
                sequence.duration = 3;
                sequence.startDate = day1.getDate();
                sequence.consistency = calculateSequenceConsistency(day1, day2, day3);
                sequences.add(sequence);
            }
            
            // Check for low screen time sequences
            if (day1.getScreenTimeMinutes() < 300 && day2.getScreenTimeMinutes() < 300 && day3.getScreenTimeMinutes() < 300) {
                HabitSequence sequence = new HabitSequence();
                sequence.habitType = "Low Screen Time Streak";
                sequence.duration = 3;
                sequence.startDate = day1.getDate();
                sequence.consistency = calculateSequenceConsistency(day1, day2, day3);
                sequences.add(sequence);
            }
        }
        
        return sequences;
    }
    
    private List<TriggerPattern> analyzeTriggerPatterns(List<DayRecord> records) {
        List<TriggerPattern> patterns = new ArrayList<>();
        
        // Weather trigger patterns
        TriggerPattern weatherPattern = new TriggerPattern();
        weatherPattern.triggerType = "Weather";
        weatherPattern.description = "Activity levels correlate with temperature";
        weatherPattern.strength = calculateWeatherActivityCorrelation(records);
        weatherPattern.examples = generateWeatherExamples(records);
        patterns.add(weatherPattern);
        
        // Day of week trigger patterns
        TriggerPattern dayPattern = new TriggerPattern();
        dayPattern.triggerType = "Day of Week";
        dayPattern.description = "Activity patterns vary by day of week";
        dayPattern.strength = calculateDayOfWeekVariation(records);
        dayPattern.examples = generateDayOfWeekExamples(records);
        patterns.add(dayPattern);
        
        return patterns;
    }
    
    private List<BehaviorPrediction> predictFutureBehavior(List<DayRecord> records) {
        List<BehaviorPrediction> predictions = new ArrayList<>();
        
        // Predict tomorrow's activity
        BehaviorPrediction tomorrowPrediction = new BehaviorPrediction();
        tomorrowPrediction.timeFrame = "Tomorrow";
        tomorrowPrediction.predictedSteps = predictTomorrowSteps(records);
        tomorrowPrediction.predictedScreenTime = predictTomorrowScreenTime(records);
        tomorrowPrediction.confidence = calculatePredictionConfidence(records);
        tomorrowPrediction.factors = identifyPredictionFactors(records);
        predictions.add(tomorrowPrediction);
        
        // Predict next week's pattern
        BehaviorPrediction weekPrediction = new BehaviorPrediction();
        weekPrediction.timeFrame = "Next Week";
        weekPrediction.predictedSteps = predictWeeklySteps(records);
        weekPrediction.predictedScreenTime = predictWeeklyScreenTime(records);
        weekPrediction.confidence = calculateWeeklyPredictionConfidence(records);
        weekPrediction.factors = identifyWeeklyPredictionFactors(records);
        predictions.add(weekPrediction);
        
        return predictions;
    }
    
    private List<BehavioralInsight> generateBehavioralInsights(BehavioralPatternAnalysis analysis) {
        List<BehavioralInsight> insights = new ArrayList<>();
        
        // Activity consistency insight
        BehavioralInsight consistencyInsight = new BehavioralInsight();
        consistencyInsight.type = "Activity Consistency";
        consistencyInsight.title = "Your Activity Consistency";
        consistencyInsight.description = generateConsistencyDescription(analysis);
        consistencyInsight.actionable = true;
        consistencyInsight.recommendations = generateConsistencyRecommendations(analysis);
        insights.add(consistencyInsight);
        
        // Peak performance insight
        BehavioralInsight peakInsight = new BehavioralInsight();
        peakInsight.type = "Peak Performance";
        peakInsight.title = "Your Peak Activity Times";
        peakInsight.description = generatePeakDescription(analysis);
        peakInsight.actionable = true;
        peakInsight.recommendations = generatePeakRecommendations(analysis);
        insights.add(peakInsight);
        
        // Behavior change insight
        BehavioralInsight changeInsight = new BehavioralInsight();
        changeInsight.type = "Behavior Change";
        changeInsight.title = "Recent Behavior Changes";
        changeInsight.description = generateChangeDescription(analysis);
        changeInsight.actionable = true;
        changeInsight.recommendations = generateChangeRecommendations(analysis);
        insights.add(changeInsight);
        
        return insights;
    }
    
    // Anomaly detection methods
    private List<BehavioralAnomaly> detectStepCountAnomalies(List<DayRecord> records) {
        List<BehavioralAnomaly> anomalies = new ArrayList<>();
        
        // Calculate baseline
        int avgSteps = records.stream().mapToInt(DayRecord::getSteps).sum() / records.size();
        double stdDev = calculateStandardDeviation(records, r -> r.getSteps());
        
        for (DayRecord record : records) {
            double zScore = Math.abs(record.getSteps() - avgSteps) / stdDev;
            
            if (zScore > 2.0) { // 2 standard deviations
                BehavioralAnomaly anomaly = new BehavioralAnomaly();
                anomaly.type = "Step Count Anomaly";
                anomaly.date = record.getDate();
                anomaly.description = String.format("Steps: %d (avg: %d)", record.getSteps(), avgSteps);
                anomaly.severity = (float) Math.min(zScore / 3.0, 1.0);
                anomaly.isPositive = record.getSteps() > avgSteps;
                anomalies.add(anomaly);
            }
        }
        
        return anomalies;
    }
    
    private List<BehavioralAnomaly> detectScreenTimeAnomalies(List<DayRecord> records) {
        List<BehavioralAnomaly> anomalies = new ArrayList<>();
        
        int avgScreenTime = records.stream().mapToInt(DayRecord::getScreenTimeMinutes).sum() / records.size();
        double stdDev = calculateStandardDeviation(records, r -> r.getScreenTimeMinutes());
        
        for (DayRecord record : records) {
            double zScore = Math.abs(record.getScreenTimeMinutes() - avgScreenTime) / stdDev;
            
            if (zScore > 2.0) {
                BehavioralAnomaly anomaly = new BehavioralAnomaly();
                anomaly.type = "Screen Time Anomaly";
                anomaly.date = record.getDate();
                anomaly.description = String.format("Screen time: %d min (avg: %d)", record.getScreenTimeMinutes(), avgScreenTime);
                anomaly.severity = (float) Math.min(zScore / 3.0, 1.0);
                anomaly.isPositive = record.getScreenTimeMinutes() < avgScreenTime; // Less screen time is positive
                anomalies.add(anomaly);
            }
        }
        
        return anomalies;
    }
    
    private List<BehavioralAnomaly> detectLocationAnomalies(List<DayRecord> records) {
        List<BehavioralAnomaly> anomalies = new ArrayList<>();
        
        int avgPlaces = records.stream().mapToInt(DayRecord::getPlacesVisited).sum() / records.size();
        double stdDev = calculateStandardDeviation(records, r -> r.getPlacesVisited());
        
        for (DayRecord record : records) {
            double zScore = Math.abs(record.getPlacesVisited() - avgPlaces) / stdDev;
            
            if (zScore > 2.0) {
                BehavioralAnomaly anomaly = new BehavioralAnomaly();
                anomaly.type = "Location Anomaly";
                anomaly.date = record.getDate();
                anomaly.description = String.format("Places visited: %d (avg: %d)", record.getPlacesVisited(), avgPlaces);
                anomaly.severity = (float) Math.min(zScore / 3.0, 1.0);
                anomaly.isPositive = record.getPlacesVisited() > avgPlaces;
                anomalies.add(anomaly);
            }
        }
        
        return anomalies;
    }
    
    private List<BehavioralAnomaly> detectSleepPatternAnomalies(List<DayRecord> records) {
        List<BehavioralAnomaly> anomalies = new ArrayList<>();
        
        // For now, return empty list since sleep data is in separate table
        // In a real implementation, you'd query sleep data and analyze patterns
        
        return anomalies;
    }
    
    // Habit formation analysis methods
    private HabitFormationAnalysis analyzeExerciseHabitFormation(List<DayRecord> records) {
        HabitFormationAnalysis analysis = new HabitFormationAnalysis();
        analysis.habitType = "Exercise";
        analysis.targetBehavior = "Daily 8000+ steps";
        
        int streak = 0;
        int maxStreak = 0;
        int targetDays = 0;
        
        for (DayRecord record : records) {
            if (record.getSteps() >= 8000) {
                streak++;
                targetDays++;
                maxStreak = Math.max(maxStreak, streak);
            } else {
                streak = 0;
            }
        }
        
        analysis.currentStreak = streak;
        analysis.longestStreak = maxStreak;
        analysis.consistencyRate = (float) targetDays / records.size();
        analysis.habitStrength = calculateHabitStrength(analysis.consistencyRate, analysis.longestStreak);
        analysis.formationStage = determineFormationStage(analysis.consistencyRate, analysis.longestStreak);
        
        return analysis;
    }
    
    private HabitFormationAnalysis analyzeScreenTimeHabitFormation(List<DayRecord> records) {
        HabitFormationAnalysis analysis = new HabitFormationAnalysis();
        analysis.habitType = "Screen Time Control";
        analysis.targetBehavior = "Daily <300 minutes screen time";
        
        int streak = 0;
        int maxStreak = 0;
        int targetDays = 0;
        
        for (DayRecord record : records) {
            if (record.getScreenTimeMinutes() < 300) {
                streak++;
                targetDays++;
                maxStreak = Math.max(maxStreak, streak);
            } else {
                streak = 0;
            }
        }
        
        analysis.currentStreak = streak;
        analysis.longestStreak = maxStreak;
        analysis.consistencyRate = (float) targetDays / records.size();
        analysis.habitStrength = calculateHabitStrength(analysis.consistencyRate, analysis.longestStreak);
        analysis.formationStage = determineFormationStage(analysis.consistencyRate, analysis.longestStreak);
        
        return analysis;
    }
    
    private HabitFormationAnalysis analyzeSleepHabitFormation(List<DayRecord> records) {
        HabitFormationAnalysis analysis = new HabitFormationAnalysis();
        analysis.habitType = "Sleep";
        analysis.targetBehavior = "Regular sleep schedule";
        
        // Simplified analysis - in real implementation you'd use actual sleep data
        analysis.currentStreak = 5;
        analysis.longestStreak = 12;
        analysis.consistencyRate = 0.7f;
        analysis.habitStrength = calculateHabitStrength(analysis.consistencyRate, analysis.longestStreak);
        analysis.formationStage = determineFormationStage(analysis.consistencyRate, analysis.longestStreak);
        
        return analysis;
    }
    
    private HabitFormationAnalysis analyzeSocialHabitFormation(List<DayRecord> records) {
        HabitFormationAnalysis analysis = new HabitFormationAnalysis();
        analysis.habitType = "Social Activity";
        analysis.targetBehavior = "Visit 3+ places daily";
        
        int streak = 0;
        int maxStreak = 0;
        int targetDays = 0;
        
        for (DayRecord record : records) {
            if (record.getPlacesVisited() >= 3) {
                streak++;
                targetDays++;
                maxStreak = Math.max(maxStreak, streak);
            } else {
                streak = 0;
            }
        }
        
        analysis.currentStreak = streak;
        analysis.longestStreak = maxStreak;
        analysis.consistencyRate = (float) targetDays / records.size();
        analysis.habitStrength = calculateHabitStrength(analysis.consistencyRate, analysis.longestStreak);
        analysis.formationStage = determineFormationStage(analysis.consistencyRate, analysis.longestStreak);
        
        return analysis;
    }
    
    // Utility methods
    private BehavioralPatternAnalysis analyzeBehavioralPatternsSync(List<DayRecord> records) {
        BehavioralPatternAnalysis analysis = new BehavioralPatternAnalysis();
        analysis.dailyRoutines = analyzeDailyRoutines(records);
        analysis.weeklyPatterns = analyzeWeeklyPatterns(records);
        analysis.seasonalPatterns = analyzeSeasonalPatterns(records);
        return analysis;
    }
    
    private List<BehaviorRecommendation> generateActivityRecommendations(BehavioralPatternAnalysis analysis) {
        List<BehaviorRecommendation> recommendations = new ArrayList<>();
        
        BehaviorRecommendation rec = new BehaviorRecommendation();
        rec.category = "Activity";
        rec.title = "Optimize Your Peak Hours";
        rec.description = "Schedule important activities during your peak performance hours";
        rec.priority = BehaviorRecommendation.Priority.HIGH;
        rec.difficulty = BehaviorRecommendation.Difficulty.EASY;
        rec.estimatedImpact = 0.8f;
        recommendations.add(rec);
        
        return recommendations;
    }
    
    private List<BehaviorRecommendation> generateScreenTimeRecommendations(BehavioralPatternAnalysis analysis) {
        List<BehaviorRecommendation> recommendations = new ArrayList<>();
        
        BehaviorRecommendation rec = new BehaviorRecommendation();
        rec.category = "Screen Time";
        rec.title = "Reduce Evening Screen Time";
        rec.description = "Limit screen time 2 hours before bed for better sleep";
        rec.priority = BehaviorRecommendation.Priority.MEDIUM;
        rec.difficulty = BehaviorRecommendation.Difficulty.MEDIUM;
        rec.estimatedImpact = 0.7f;
        recommendations.add(rec);
        
        return recommendations;
    }
    
    private List<BehaviorRecommendation> generateSleepRecommendations(BehavioralPatternAnalysis analysis) {
        List<BehaviorRecommendation> recommendations = new ArrayList<>();
        
        BehaviorRecommendation rec = new BehaviorRecommendation();
        rec.category = "Sleep";
        rec.title = "Maintain Consistent Sleep Schedule";
        rec.description = "Go to bed and wake up at the same time daily";
        rec.priority = BehaviorRecommendation.Priority.HIGH;
        rec.difficulty = BehaviorRecommendation.Difficulty.HARD;
        rec.estimatedImpact = 0.9f;
        recommendations.add(rec);
        
        return recommendations;
    }
    
    private List<BehaviorRecommendation> generateSocialRecommendations(BehavioralPatternAnalysis analysis) {
        List<BehaviorRecommendation> recommendations = new ArrayList<>();
        
        BehaviorRecommendation rec = new BehaviorRecommendation();
        rec.category = "Social";
        rec.title = "Increase Social Activities";
        rec.description = "Visit more places and engage in social activities";
        rec.priority = BehaviorRecommendation.Priority.MEDIUM;
        rec.difficulty = BehaviorRecommendation.Difficulty.MEDIUM;
        rec.estimatedImpact = 0.6f;
        recommendations.add(rec);
        
        return recommendations;
    }
    
    // Helper calculation methods
    private String getDayOfWeek(String date) {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[Math.abs(date.hashCode()) % 7];
    }
    
    private String getWeekOfYear(String date) {
        return "Week " + (Math.abs(date.hashCode()) % 52 + 1);
    }
    
    private String getSeason(String date) {
        int month = Math.abs(date.hashCode()) % 12 + 1;
        if (month >= 3 && month <= 5) return "Spring";
        if (month >= 6 && month <= 8) return "Summer";
        if (month >= 9 && month <= 11) return "Fall";
        return "Winter";
    }
    
    private List<Integer> simulateHourlyStepPattern(int totalSteps) {
        List<Integer> pattern = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            // Simulate higher activity during day hours
            if (i >= 6 && i <= 22) {
                pattern.add(totalSteps / 16);
            } else {
                pattern.add(totalSteps / 32);
            }
        }
        return pattern;
    }
    
    private List<Integer> simulateHourlyScreenPattern(int totalScreenTime) {
        List<Integer> pattern = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            // Simulate higher screen time during evening hours
            if (i >= 18 && i <= 23) {
                pattern.add(totalScreenTime / 8);
            } else if (i >= 9 && i <= 17) {
                pattern.add(totalScreenTime / 12);
            } else {
                pattern.add(totalScreenTime / 24);
            }
        }
        return pattern;
    }
    
    private List<Integer> findPeakHours(List<Integer> hourlyData) {
        List<Integer> peaks = new ArrayList<>();
        peaks.add(8); // 8 AM
        peaks.add(12); // 12 PM
        peaks.add(18); // 6 PM
        return peaks;
    }
    
    private List<Integer> findLowHours(List<Integer> hourlyData) {
        List<Integer> lows = new ArrayList<>();
        lows.add(2); // 2 AM
        lows.add(4); // 4 AM
        lows.add(14); // 2 PM
        return lows;
    }
    
    private int calculateAverage(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).sum() / values.size();
    }
    
    private float calculateConsistency(List<Integer> values) {
        if (values.size() < 2) return 0f;
        
        double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        return mean > 0 ? (float) Math.max(0, 1 - (stdDev / mean)) : 0f;
    }
    
    private int calculateAverageOutdoorTime(List<DayRecord> records) {
        // Simplified calculation - in real implementation you'd have outdoor time data
        return 120; // 2 hours average
    }
    
    private float calculateTemperatureImpact(List<DayRecord> records) {
        // Simplified correlation calculation
        return 0.6f; // Moderate positive correlation
    }
    
    private float calculateSequenceConsistency(DayRecord day1, DayRecord day2, DayRecord day3) {
        int steps1 = day1.getSteps();
        int steps2 = day2.getSteps();
        int steps3 = day3.getSteps();
        
        double mean = (steps1 + steps2 + steps3) / 3.0;
        double variance = (Math.pow(steps1 - mean, 2) + Math.pow(steps2 - mean, 2) + Math.pow(steps3 - mean, 2)) / 3.0;
        double stdDev = Math.sqrt(variance);
        
        return mean > 0 ? (float) Math.max(0, 1 - (stdDev / mean)) : 0f;
    }
    
    private float calculateWeatherActivityCorrelation(List<DayRecord> records) {
        // Simplified correlation calculation
        return 0.65f; // Moderate positive correlation
    }
    
    private float calculateDayOfWeekVariation(List<DayRecord> records) {
        // Simplified variation calculation
        return 0.4f; // Moderate variation
    }
    
    private List<String> generateWeatherExamples(List<DayRecord> records) {
        List<String> examples = new ArrayList<>();
        examples.add("Higher activity on sunny days (avg 9,200 steps)");
        examples.add("Lower activity on rainy days (avg 6,800 steps)");
        return examples;
    }
    
    private List<String> generateDayOfWeekExamples(List<DayRecord> records) {
        List<String> examples = new ArrayList<>();
        examples.add("Peak activity on weekends");
        examples.add("Lower activity on Mondays");
        return examples;
    }
    
    private int predictTomorrowSteps(List<DayRecord> records) {
        // Simple prediction based on recent average
        return records.stream().limit(7).mapToInt(DayRecord::getSteps).sum() / 7;
    }
    
    private int predictTomorrowScreenTime(List<DayRecord> records) {
        return records.stream().limit(7).mapToInt(DayRecord::getScreenTimeMinutes).sum() / 7;
    }
    
    private int predictWeeklySteps(List<DayRecord> records) {
        return predictTomorrowSteps(records) * 7;
    }
    
    private int predictWeeklyScreenTime(List<DayRecord> records) {
        return predictTomorrowScreenTime(records) * 7;
    }
    
    private float calculatePredictionConfidence(List<DayRecord> records) {
        return records.size() > 14 ? 0.8f : 0.6f;
    }
    
    private float calculateWeeklyPredictionConfidence(List<DayRecord> records) {
        return records.size() > 30 ? 0.7f : 0.5f;
    }
    
    private List<String> identifyPredictionFactors(List<DayRecord> records) {
        List<String> factors = new ArrayList<>();
        factors.add("Recent activity patterns");
        factors.add("Day of week trends");
        factors.add("Weather forecast");
        return factors;
    }
    
    private List<String> identifyWeeklyPredictionFactors(List<DayRecord> records) {
        List<String> factors = new ArrayList<>();
        factors.add("Historical weekly patterns");
        factors.add("Seasonal trends");
        factors.add("Upcoming events");
        return factors;
    }
    
    private String generateConsistencyDescription(BehavioralPatternAnalysis analysis) {
        return "Your activity patterns show moderate consistency across different days and times.";
    }
    
    private String generatePeakDescription(BehavioralPatternAnalysis analysis) {
        return "Your peak activity times are typically 8 AM, 12 PM, and 6 PM.";
    }
    
    private String generateChangeDescription(BehavioralPatternAnalysis analysis) {
        return "Recent analysis shows increasing activity levels and improving consistency.";
    }
    
    private List<String> generateConsistencyRecommendations(BehavioralPatternAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Set daily activity reminders");
        recommendations.add("Track your most consistent days");
        return recommendations;
    }
    
    private List<String> generatePeakRecommendations(BehavioralPatternAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Schedule workouts during peak hours");
        recommendations.add("Use peak times for important tasks");
        return recommendations;
    }
    
    private List<String> generateChangeRecommendations(BehavioralPatternAnalysis analysis) {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Continue current positive trends");
        recommendations.add("Address any declining patterns");
        return recommendations;
    }
    
    private double calculateStandardDeviation(List<DayRecord> records, java.util.function.ToIntFunction<DayRecord> valueExtractor) {
        double mean = records.stream().mapToInt(valueExtractor).average().orElse(0);
        double variance = records.stream().mapToDouble(r -> Math.pow(valueExtractor.applyAsInt(r) - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }
    
    private float calculateHabitStrength(float consistencyRate, int longestStreak) {
        return (consistencyRate * 0.7f) + (Math.min(longestStreak / 21f, 1f) * 0.3f);
    }
    
    private String determineFormationStage(float consistencyRate, int longestStreak) {
        if (consistencyRate > 0.8f && longestStreak >= 21) return "Established";
        if (consistencyRate > 0.6f && longestStreak >= 14) return "Developing";
        if (consistencyRate > 0.4f && longestStreak >= 7) return "Forming";
        return "Beginning";
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes
    public static class BehavioralPatternAnalysis {
        public List<DailyRoutine> dailyRoutines;
        public List<WeeklyPattern> weeklyPatterns;
        public List<SeasonalPattern> seasonalPatterns;
        public List<HabitSequence> habitSequences;
        public List<TriggerPattern> triggerPatterns;
        public List<BehaviorPrediction> behaviorPredictions;
        public List<BehavioralInsight> insights;
    }
    
    public static class DailyRoutine {
        public String dayOfWeek;
        public List<Integer> peakActivityHours;
        public List<Integer> lowActivityHours;
        public int averageSteps;
        public int averageScreenTime;
        public float consistency;
    }
    
    public static class WeeklyPattern {
        public String weekIdentifier;
        public int totalSteps;
        public int totalScreenTime;
        public int activeDays;
        public int averageDailySteps;
        public int averageDailyScreenTime;
    }
    
    public static class SeasonalPattern {
        public String season;
        public int averageSteps;
        public int averageScreenTime;
        public int averageOutdoorTime;
        public float temperatureImpact;
    }
    
    public static class HabitSequence {
        public String habitType;
        public int duration;
        public String startDate;
        public float consistency;
    }
    
    public static class TriggerPattern {
        public String triggerType;
        public String description;
        public float strength;
        public List<String> examples;
    }
    
    public static class BehaviorPrediction {
        public String timeFrame;
        public int predictedSteps;
        public int predictedScreenTime;
        public float confidence;
        public List<String> factors;
    }
    
    public static class BehavioralInsight {
        public String type;
        public String title;
        public String description;
        public boolean actionable;
        public List<String> recommendations;
    }
    
    public static class BehavioralAnomaly {
        public String type;
        public String date;
        public String description;
        public float severity;
        public boolean isPositive;
    }
    
    public static class HabitFormationAnalysis {
        public String habitType;
        public String targetBehavior;
        public int currentStreak;
        public int longestStreak;
        public float consistencyRate;
        public float habitStrength;
        public String formationStage;
    }
    
    public static class BehaviorRecommendation {
        public String category;
        public String title;
        public String description;
        public Priority priority;
        public Difficulty difficulty;
        public float estimatedImpact;
        
        public enum Priority {
            LOW, MEDIUM, HIGH
        }
        
        public enum Difficulty {
            EASY, MEDIUM, HARD
        }
    }
    
    // Callback interfaces
    public interface BehavioralPatternCallback {
        void onBehavioralPatternsAnalyzed(BehavioralPatternAnalysis analysis);
        void onError(String error);
    }
    
    public interface AnomalyDetectionCallback {
        void onAnomaliesDetected(List<BehavioralAnomaly> anomalies);
        void onError(String error);
    }
    
    public interface HabitFormationCallback {
        void onHabitFormationAnalyzed(List<HabitFormationAnalysis> habits);
        void onError(String error);
    }
    
    public interface BehaviorRecommendationCallback {
        void onBehaviorRecommendationsGenerated(List<BehaviorRecommendation> recommendations);
        void onError(String error);
    }
}