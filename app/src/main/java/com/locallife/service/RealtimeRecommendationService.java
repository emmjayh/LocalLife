package com.locallife.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.locallife.R;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for providing real-time activity recommendations based on current context
 */
public class RealtimeRecommendationService {
    private static final String TAG = "RealtimeRecommendation";
    private static final String CHANNEL_ID = "activity_recommendations";
    private static final String PREFS_NAME = "recommendation_settings";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    private NotificationManager notificationManager;
    private SharedPreferences preferences;
    
    // Recommendation engine components
    private MLActivityClassifierService mlService;
    private BehavioralPatternService behavioralService;
    
    public RealtimeRecommendationService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        this.mlService = new MLActivityClassifierService(context);
        this.behavioralService = new BehavioralPatternService(context);
        
        createNotificationChannel();
    }
    
    /**
     * Generate real-time recommendations based on current context
     */
    public void generateRealtimeRecommendations(RecommendationCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                // Get current context
                RecommendationContext currentContext = getCurrentContext();
                
                // Generate recommendations
                List<ActivityRecommendation> recommendations = new ArrayList<>();
                
                // Add time-based recommendations
                recommendations.addAll(generateTimeBasedRecommendations(currentContext));
                
                // Add weather-based recommendations
                recommendations.addAll(generateWeatherBasedRecommendations(currentContext));
                
                // Add pattern-based recommendations
                recommendations.addAll(generatePatternBasedRecommendations(currentContext));
                
                // Add goal-based recommendations
                recommendations.addAll(generateGoalBasedRecommendations(currentContext));
                
                // Add health-based recommendations
                recommendations.addAll(generateHealthBasedRecommendations(currentContext));
                
                // Rank recommendations by priority and relevance
                recommendations.sort((a, b) -> {
                    int priorityCompare = b.priority.compareTo(a.priority);
                    if (priorityCompare != 0) return priorityCompare;
                    return Float.compare(b.relevanceScore, a.relevanceScore);
                });
                
                // Filter to top recommendations
                List<ActivityRecommendation> topRecommendations = recommendations.subList(0, Math.min(5, recommendations.size()));
                
                if (callback != null) {
                    callback.onRecommendationsGenerated(topRecommendations, currentContext);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating real-time recommendations", e);
                if (callback != null) {
                    callback.onError("Recommendation generation failed: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Send push notification with activity recommendation
     */
    public void sendRecommendationNotification(ActivityRecommendation recommendation) {
        if (!isNotificationEnabled()) {
            return;
        }
        
        Intent intent = new Intent(context, context.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Activity Recommendation")
                .setContentText(recommendation.title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(recommendation.description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        notificationManager.notify(recommendation.id, builder.build());
    }
    
    /**
     * Schedule periodic recommendation checks
     */
    public void schedulePeriodicRecommendations() {
        backgroundExecutor.execute(() -> {
            try {
                // Check if it's time for a recommendation
                if (shouldGenerateRecommendation()) {
                    generateRealtimeRecommendations(new RecommendationCallback() {
                        @Override
                        public void onRecommendationsGenerated(List<ActivityRecommendation> recommendations, 
                                                              RecommendationContext context) {
                            if (!recommendations.isEmpty()) {
                                ActivityRecommendation topRecommendation = recommendations.get(0);
                                if (topRecommendation.priority.ordinal() >= ActivityRecommendation.Priority.MEDIUM.ordinal()) {
                                    sendRecommendationNotification(topRecommendation);
                                }
                            }
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Periodic recommendation error: " + error);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in periodic recommendation check", e);
            }
        });
    }
    
    /**
     * Analyze user feedback on recommendations
     */
    public void processRecommendationFeedback(int recommendationId, RecommendationFeedback feedback) {
        backgroundExecutor.execute(() -> {
            try {
                // Store feedback for learning
                storeFeedback(recommendationId, feedback);
                
                // Update recommendation algorithm based on feedback
                updateRecommendationAlgorithm(feedback);
                
                Log.d(TAG, "Processed feedback for recommendation " + recommendationId + ": " + feedback.rating);
                
            } catch (Exception e) {
                Log.e(TAG, "Error processing recommendation feedback", e);
            }
        });
    }
    
    // Helper methods for generating different types of recommendations
    private RecommendationContext getCurrentContext() {
        RecommendationContext context = new RecommendationContext();
        
        Calendar calendar = Calendar.getInstance();
        context.currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        context.currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        context.currentMonth = calendar.get(Calendar.MONTH);
        
        // Get today's activity data
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.getTime());
        context.todayRecord = databaseHelper.getDayRecord(today);
        
        // Get recent activity pattern
        List<DayRecord> recentRecords = databaseHelper.getDayRecordsForPeriod(7);
        context.recentActivity = recentRecords;
        
        // Simulate current weather (in real implementation, you'd get from weather API)
        context.currentTemperature = 22.0f;
        context.currentWeather = "Partly Cloudy";
        context.precipitationChance = 20;
        
        // Get user preferences
        context.isNotificationEnabled = isNotificationEnabled();
        context.preferredActivityTypes = getPreferredActivityTypes();
        context.fitnessLevel = getFitnessLevel();
        
        return context;
    }
    
    private List<ActivityRecommendation> generateTimeBasedRecommendations(RecommendationContext context) {
        List<ActivityRecommendation> recommendations = new ArrayList<>();
        
        int hour = context.currentHour;
        
        // Morning recommendations (6-10 AM)
        if (hour >= 6 && hour <= 10) {
            ActivityRecommendation morningWalk = new ActivityRecommendation();
            morningWalk.id = generateRecommendationId("morning_walk");
            morningWalk.title = "Morning Walk";
            morningWalk.description = "Start your day with a 20-minute walk. Morning activity boosts energy and mood.";
            morningWalk.priority = ActivityRecommendation.Priority.HIGH;
            morningWalk.category = ActivityRecommendation.Category.EXERCISE;
            morningWalk.duration = 20;
            morningWalk.relevanceScore = 0.9f;
            morningWalk.actions = createWalkActions();
            recommendations.add(morningWalk);
        }
        
        // Lunch time recommendations (11 AM - 2 PM)
        if (hour >= 11 && hour <= 14) {
            ActivityRecommendation lunchWalk = new ActivityRecommendation();
            lunchWalk.id = generateRecommendationId("lunch_walk");
            lunchWalk.title = "Lunch Break Activity";
            lunchWalk.description = "Take a 15-minute walk during lunch to boost afternoon productivity.";
            lunchWalk.priority = ActivityRecommendation.Priority.MEDIUM;
            lunchWalk.category = ActivityRecommendation.Category.EXERCISE;
            lunchWalk.duration = 15;
            lunchWalk.relevanceScore = 0.7f;
            lunchWalk.actions = createWalkActions();
            recommendations.add(lunchWalk);
        }
        
        // Evening recommendations (5-8 PM)
        if (hour >= 17 && hour <= 20) {
            ActivityRecommendation eveningExercise = new ActivityRecommendation();
            eveningExercise.id = generateRecommendationId("evening_exercise");
            eveningExercise.title = "Evening Exercise";
            eveningExercise.description = "Perfect time for a workout! Your body is warmed up and ready for activity.";
            eveningExercise.priority = ActivityRecommendation.Priority.HIGH;
            eveningExercise.category = ActivityRecommendation.Category.EXERCISE;
            eveningExercise.duration = 30;
            eveningExercise.relevanceScore = 0.8f;
            eveningExercise.actions = createExerciseActions();
            recommendations.add(eveningExercise);
        }
        
        // Late evening recommendations (8-10 PM)
        if (hour >= 20 && hour <= 22) {
            ActivityRecommendation relaxation = new ActivityRecommendation();
            relaxation.id = generateRecommendationId("evening_relaxation");
            relaxation.title = "Wind Down Time";
            relaxation.description = "Time to relax and prepare for sleep. Consider gentle stretching or meditation.";
            relaxation.priority = ActivityRecommendation.Priority.MEDIUM;
            relaxation.category = ActivityRecommendation.Category.WELLNESS;
            relaxation.duration = 15;
            relaxation.relevanceScore = 0.6f;
            relaxation.actions = createRelaxationActions();
            recommendations.add(relaxation);
        }
        
        return recommendations;
    }
    
    private List<ActivityRecommendation> generateWeatherBasedRecommendations(RecommendationContext context) {
        List<ActivityRecommendation> recommendations = new ArrayList<>();
        
        float temp = context.currentTemperature;
        String weather = context.currentWeather;
        int precipChance = context.precipitationChance;
        
        // Good weather recommendations
        if (temp >= 18 && temp <= 25 && precipChance < 30) {
            ActivityRecommendation outdoorActivity = new ActivityRecommendation();
            outdoorActivity.id = generateRecommendationId("outdoor_activity");
            outdoorActivity.title = "Perfect Weather for Outdoor Activity";
            outdoorActivity.description = String.format("Great weather today! %.1f°C and %s. Ideal for outdoor activities.", temp, weather);
            outdoorActivity.priority = ActivityRecommendation.Priority.HIGH;
            outdoorActivity.category = ActivityRecommendation.Category.OUTDOOR;
            outdoorActivity.duration = 45;
            outdoorActivity.relevanceScore = 0.9f;
            outdoorActivity.actions = createOutdoorActions();
            recommendations.add(outdoorActivity);
        }
        
        // Cold weather recommendations
        if (temp < 10) {
            ActivityRecommendation indoorActivity = new ActivityRecommendation();
            indoorActivity.id = generateRecommendationId("indoor_activity");
            indoorActivity.title = "Indoor Exercise Recommended";
            indoorActivity.description = String.format("Cold weather (%.1f°C) - try indoor exercises like yoga or bodyweight workouts.", temp);
            indoorActivity.priority = ActivityRecommendation.Priority.MEDIUM;
            indoorActivity.category = ActivityRecommendation.Category.INDOOR;
            indoorActivity.duration = 30;
            indoorActivity.relevanceScore = 0.7f;
            indoorActivity.actions = createIndoorActions();
            recommendations.add(indoorActivity);
        }
        
        // Rainy weather recommendations
        if (precipChance > 60) {
            ActivityRecommendation rainyDayActivity = new ActivityRecommendation();
            rainyDayActivity.id = generateRecommendationId("rainy_day");
            rainyDayActivity.title = "Rainy Day Alternatives";
            rainyDayActivity.description = String.format("High chance of rain (%d%%). Consider indoor activities or covered exercise areas.", precipChance);
            rainyDayActivity.priority = ActivityRecommendation.Priority.MEDIUM;
            rainyDayActivity.category = ActivityRecommendation.Category.INDOOR;
            rainyDayActivity.duration = 25;
            rainyDayActivity.relevanceScore = 0.6f;
            rainyDayActivity.actions = createRainyDayActions();
            recommendations.add(rainyDayActivity);
        }
        
        return recommendations;
    }
    
    private List<ActivityRecommendation> generatePatternBasedRecommendations(RecommendationContext context) {
        List<ActivityRecommendation> recommendations = new ArrayList<>();
        
        if (context.todayRecord != null) {
            int currentSteps = context.todayRecord.getSteps();
            int currentScreenTime = context.todayRecord.getScreenTimeMinutes();
            
            // Low activity pattern
            if (currentSteps < 5000 && context.currentHour > 12) {
                ActivityRecommendation stepBoost = new ActivityRecommendation();
                stepBoost.id = generateRecommendationId("step_boost");
                stepBoost.title = "Boost Your Daily Steps";
                stepBoost.description = String.format("You have %d steps today. A 20-minute walk could add 2,000+ steps!", currentSteps);
                stepBoost.priority = ActivityRecommendation.Priority.HIGH;
                stepBoost.category = ActivityRecommendation.Category.EXERCISE;
                stepBoost.duration = 20;
                stepBoost.relevanceScore = 0.8f;
                stepBoost.actions = createStepBoostActions();
                recommendations.add(stepBoost);
            }
            
            // High screen time pattern
            if (currentScreenTime > 300 && context.currentHour > 10) {
                ActivityRecommendation screenBreak = new ActivityRecommendation();
                screenBreak.id = generateRecommendationId("screen_break");
                screenBreak.title = "Take a Screen Break";
                screenBreak.description = String.format("You've had %d minutes of screen time today. Take a 10-minute break!", currentScreenTime);
                screenBreak.priority = ActivityRecommendation.Priority.MEDIUM;
                screenBreak.category = ActivityRecommendation.Category.WELLNESS;
                screenBreak.duration = 10;
                screenBreak.relevanceScore = 0.7f;
                screenBreak.actions = createScreenBreakActions();
                recommendations.add(screenBreak);
            }
        }
        
        // Weekly pattern analysis
        if (context.recentActivity != null && context.recentActivity.size() >= 7) {
            int weeklyAvgSteps = context.recentActivity.stream().mapToInt(DayRecord::getSteps).sum() / 7;
            
            if (context.todayRecord != null && context.todayRecord.getSteps() < weeklyAvgSteps * 0.7) {
                ActivityRecommendation catchUp = new ActivityRecommendation();
                catchUp.id = generateRecommendationId("catch_up");
                catchUp.title = "Catch Up on Weekly Goals";
                catchUp.description = String.format("You're behind your weekly average (%d steps). Let's catch up!", weeklyAvgSteps);
                catchUp.priority = ActivityRecommendation.Priority.MEDIUM;
                catchUp.category = ActivityRecommendation.Category.EXERCISE;
                catchUp.duration = 25;
                catchUp.relevanceScore = 0.6f;
                catchUp.actions = createCatchUpActions();
                recommendations.add(catchUp);
            }
        }
        
        return recommendations;
    }
    
    private List<ActivityRecommendation> generateGoalBasedRecommendations(RecommendationContext context) {
        List<ActivityRecommendation> recommendations = new ArrayList<>();
        
        // Daily step goal (assuming 8000 steps)
        if (context.todayRecord != null) {
            int currentSteps = context.todayRecord.getSteps();
            int dailyGoal = 8000;
            
            if (currentSteps < dailyGoal) {
                int remaining = dailyGoal - currentSteps;
                ActivityRecommendation goalPush = new ActivityRecommendation();
                goalPush.id = generateRecommendationId("goal_push");
                goalPush.title = "Daily Goal Push";
                goalPush.description = String.format("You need %d more steps to reach your daily goal. You can do it!", remaining);
                goalPush.priority = ActivityRecommendation.Priority.HIGH;
                goalPush.category = ActivityRecommendation.Category.GOAL;
                goalPush.duration = Math.min(remaining / 100, 30); // Estimate 100 steps per minute
                goalPush.relevanceScore = 0.85f;
                goalPush.actions = createGoalPushActions();
                recommendations.add(goalPush);
            }
        }
        
        // Weekly fitness goal
        if (context.currentDayOfWeek == Calendar.FRIDAY) {
            ActivityRecommendation weeklyGoal = new ActivityRecommendation();
            weeklyGoal.id = generateRecommendationId("weekly_goal");
            weeklyGoal.title = "Weekly Fitness Check";
            weeklyGoal.description = "End the week strong! How about a longer workout to finish your weekly goals?";
            weeklyGoal.priority = ActivityRecommendation.Priority.MEDIUM;
            weeklyGoal.category = ActivityRecommendation.Category.GOAL;
            weeklyGoal.duration = 40;
            weeklyGoal.relevanceScore = 0.7f;
            weeklyGoal.actions = createWeeklyGoalActions();
            recommendations.add(weeklyGoal);
        }
        
        return recommendations;
    }
    
    private List<ActivityRecommendation> generateHealthBasedRecommendations(RecommendationContext context) {
        List<ActivityRecommendation> recommendations = new ArrayList<>();
        
        // Posture break recommendations
        if (context.todayRecord != null && context.todayRecord.getScreenTimeMinutes() > 120) {
            ActivityRecommendation postureBreak = new ActivityRecommendation();
            postureBreak.id = generateRecommendationId("posture_break");
            postureBreak.title = "Posture Break";
            postureBreak.description = "Extended screen time can affect posture. Try some stretching exercises!";
            postureBreak.priority = ActivityRecommendation.Priority.MEDIUM;
            postureBreak.category = ActivityRecommendation.Category.HEALTH;
            postureBreak.duration = 5;
            postureBreak.relevanceScore = 0.6f;
            postureBreak.actions = createPostureBreakActions();
            recommendations.add(postureBreak);
        }
        
        // Hydration reminder with activity
        if (context.currentHour % 2 == 0 && context.currentHour >= 8 && context.currentHour <= 18) {
            ActivityRecommendation hydrationWalk = new ActivityRecommendation();
            hydrationWalk.id = generateRecommendationId("hydration_walk");
            hydrationWalk.title = "Hydration Walk";
            hydrationWalk.description = "Time to hydrate! Take a short walk to the water cooler and stay active.";
            hydrationWalk.priority = ActivityRecommendation.Priority.LOW;
            hydrationWalk.category = ActivityRecommendation.Category.HEALTH;
            hydrationWalk.duration = 3;
            hydrationWalk.relevanceScore = 0.4f;
            hydrationWalk.actions = createHydrationActions();
            recommendations.add(hydrationWalk);
        }
        
        return recommendations;
    }
    
    // Helper methods for creating actions
    private List<RecommendationAction> createWalkActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Start Walking", "Begin your walk now", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Find Route", "Discover walking routes nearby", RecommendationAction.Type.FIND_LOCATION));
        actions.add(new RecommendationAction("Set Reminder", "Remind me in 30 minutes", RecommendationAction.Type.SET_REMINDER));
        return actions;
    }
    
    private List<RecommendationAction> createExerciseActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Start Workout", "Begin exercise routine", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Find Gym", "Locate nearby gyms", RecommendationAction.Type.FIND_LOCATION));
        actions.add(new RecommendationAction("Home Workout", "Start bodyweight exercises", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createRelaxationActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Start Meditation", "Begin guided meditation", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Stretching", "Do gentle stretches", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Deep Breathing", "Practice breathing exercises", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createOutdoorActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Go Outside", "Step outside now", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Find Parks", "Locate nearby parks", RecommendationAction.Type.FIND_LOCATION));
        actions.add(new RecommendationAction("Outdoor Sports", "Find sports activities", RecommendationAction.Type.FIND_LOCATION));
        return actions;
    }
    
    private List<RecommendationAction> createIndoorActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Yoga", "Start yoga routine", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Bodyweight", "Do bodyweight exercises", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Dance", "Put on music and dance", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createRainyDayActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Indoor Gym", "Find covered fitness areas", RecommendationAction.Type.FIND_LOCATION));
        actions.add(new RecommendationAction("Mall Walking", "Walk in shopping centers", RecommendationAction.Type.FIND_LOCATION));
        actions.add(new RecommendationAction("Stair Climbing", "Use building stairs", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createStepBoostActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Quick Walk", "Take a 15-minute walk", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Pace Indoors", "Walk around your space", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Take Stairs", "Use stairs instead of elevators", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createScreenBreakActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Look Away", "Focus on distant objects", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Eye Exercises", "Do eye movement exercises", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Stand Up", "Stand and stretch", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createCatchUpActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Power Walk", "Take a brisk 20-minute walk", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Stairs Challenge", "Climb stairs for 10 minutes", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Active Errands", "Walk to nearby errands", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createGoalPushActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Final Push", "Complete your daily goal", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Track Progress", "Monitor your step count", RecommendationAction.Type.VIEW_STATS));
        actions.add(new RecommendationAction("Celebrate", "You're almost there!", RecommendationAction.Type.VIEW_STATS));
        return actions;
    }
    
    private List<RecommendationAction> createWeeklyGoalActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Long Workout", "40-minute exercise session", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Review Week", "Check weekly progress", RecommendationAction.Type.VIEW_STATS));
        actions.add(new RecommendationAction("Plan Next Week", "Set goals for next week", RecommendationAction.Type.SET_REMINDER));
        return actions;
    }
    
    private List<RecommendationAction> createPostureBreakActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Neck Stretches", "Gentle neck movements", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Shoulder Rolls", "Roll shoulders backwards", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Back Stretch", "Stretch your back", RecommendationAction.Type.START_ACTIVITY));
        return actions;
    }
    
    private List<RecommendationAction> createHydrationActions() {
        List<RecommendationAction> actions = new ArrayList<>();
        actions.add(new RecommendationAction("Drink Water", "Hydrate now", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Walk to Water", "Walk to get water", RecommendationAction.Type.START_ACTIVITY));
        actions.add(new RecommendationAction("Set Reminder", "Remind me to drink water", RecommendationAction.Type.SET_REMINDER));
        return actions;
    }
    
    // Utility methods
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Activity Recommendations";
            String description = "Notifications for activity recommendations";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private boolean isNotificationEnabled() {
        return preferences.getBoolean("notifications_enabled", true);
    }
    
    private List<String> getPreferredActivityTypes() {
        List<String> types = new ArrayList<>();
        types.add("Walking");
        types.add("Exercise");
        types.add("Outdoor");
        return types;
    }
    
    private String getFitnessLevel() {
        return preferences.getString("fitness_level", "Moderate");
    }
    
    private boolean shouldGenerateRecommendation() {
        long lastRecommendation = preferences.getLong("last_recommendation", 0);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastRecommendation;
        long twoHours = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
        
        return timeDiff > twoHours;
    }
    
    private int generateRecommendationId(String type) {
        return (type + System.currentTimeMillis()).hashCode();
    }
    
    private void storeFeedback(int recommendationId, RecommendationFeedback feedback) {
        // Store feedback in preferences or database
        preferences.edit()
                .putFloat("feedback_" + recommendationId, feedback.rating)
                .putString("feedback_comment_" + recommendationId, feedback.comment)
                .apply();
    }
    
    private void updateRecommendationAlgorithm(RecommendationFeedback feedback) {
        // Update algorithm weights based on feedback
        float currentWeight = preferences.getFloat("algorithm_weight", 1.0f);
        float newWeight = currentWeight * (1 + (feedback.rating - 0.5f) * 0.1f);
        preferences.edit().putFloat("algorithm_weight", newWeight).apply();
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
        if (mlService != null) {
            mlService.shutdown();
        }
        if (behavioralService != null) {
            behavioralService.shutdown();
        }
    }
    
    // Data classes
    public static class RecommendationContext {
        public int currentHour;
        public int currentDayOfWeek;
        public int currentMonth;
        public DayRecord todayRecord;
        public List<DayRecord> recentActivity;
        public float currentTemperature;
        public String currentWeather;
        public int precipitationChance;
        public boolean isNotificationEnabled;
        public List<String> preferredActivityTypes;
        public String fitnessLevel;
    }
    
    public static class ActivityRecommendation {
        public int id;
        public String title;
        public String description;
        public Priority priority;
        public Category category;
        public int duration; // in minutes
        public float relevanceScore;
        public List<RecommendationAction> actions;
        
        public enum Priority {
            LOW, MEDIUM, HIGH
        }
        
        public enum Category {
            EXERCISE, OUTDOOR, INDOOR, WELLNESS, HEALTH, GOAL
        }
    }
    
    public static class RecommendationAction {
        public String title;
        public String description;
        public Type type;
        
        public RecommendationAction(String title, String description, Type type) {
            this.title = title;
            this.description = description;
            this.type = type;
        }
        
        public enum Type {
            START_ACTIVITY, FIND_LOCATION, SET_REMINDER, VIEW_STATS
        }
    }
    
    public static class RecommendationFeedback {
        public float rating; // 0.0 to 1.0
        public String comment;
        public boolean wasHelpful;
        public boolean wasActedUpon;
        
        public RecommendationFeedback(float rating, String comment, boolean wasHelpful, boolean wasActedUpon) {
            this.rating = rating;
            this.comment = comment;
            this.wasHelpful = wasHelpful;
            this.wasActedUpon = wasActedUpon;
        }
    }
    
    // Callback interface
    public interface RecommendationCallback {
        void onRecommendationsGenerated(List<ActivityRecommendation> recommendations, RecommendationContext context);
        void onError(String error);
    }
}