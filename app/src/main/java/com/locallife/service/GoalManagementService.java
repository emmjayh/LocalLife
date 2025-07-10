package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;
import com.locallife.model.Goal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for managing user goals and tracking progress
 */
public class GoalManagementService {
    private static final String TAG = "GoalManagementService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public GoalManagementService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Create a new goal
     */
    public void createGoal(Goal goal, GoalCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                // Validate goal
                if (!validateGoal(goal)) {
                    if (callback != null) {
                        callback.onError("Invalid goal parameters");
                    }
                    return;
                }
                
                // Insert goal into database
                long id = databaseHelper.insertGoal(goal);
                goal.setId((int) id);
                
                // Generate motivational message
                goal.generateMotivationalMessage();
                
                Log.d(TAG, "Created goal: " + goal.getTitle());
                
                if (callback != null) {
                    callback.onGoalCreated(goal);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating goal", e);
                if (callback != null) {
                    callback.onError("Failed to create goal: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Update an existing goal
     */
    public void updateGoal(Goal goal, GoalCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                // Validate goal
                if (!validateGoal(goal)) {
                    if (callback != null) {
                        callback.onError("Invalid goal parameters");
                    }
                    return;
                }
                
                // Update in database
                databaseHelper.updateGoal(goal);
                
                Log.d(TAG, "Updated goal: " + goal.getTitle());
                
                if (callback != null) {
                    callback.onGoalUpdated(goal);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error updating goal", e);
                if (callback != null) {
                    callback.onError("Failed to update goal: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Delete a goal
     */
    public void deleteGoal(int goalId, GoalCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                databaseHelper.deleteGoal(goalId);
                
                Log.d(TAG, "Deleted goal: " + goalId);
                
                if (callback != null) {
                    callback.onGoalDeleted(goalId);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error deleting goal", e);
                if (callback != null) {
                    callback.onError("Failed to delete goal: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get all active goals
     */
    public void getActiveGoals(GoalListCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Goal> goals = databaseHelper.getActiveGoals();
                
                // Update progress for each goal
                for (Goal goal : goals) {
                    updateGoalProgress(goal);
                }
                
                if (callback != null) {
                    callback.onGoalsReceived(goals);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting active goals", e);
                if (callback != null) {
                    callback.onError("Failed to get goals: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get goals by category
     */
    public void getGoalsByCategory(Goal.GoalCategory category, GoalListCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Goal> goals = databaseHelper.getGoalsByCategory(category);
                
                // Update progress for each goal
                for (Goal goal : goals) {
                    updateGoalProgress(goal);
                }
                
                if (callback != null) {
                    callback.onGoalsReceived(goals);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting goals by category", e);
                if (callback != null) {
                    callback.onError("Failed to get goals: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Update goal progress based on current data
     */
    public void updateGoalProgress(Goal goal) {
        try {
            float currentValue = getCurrentValueForGoal(goal);
            goal.updateProgress(currentValue);
            
            // Save updated goal
            databaseHelper.updateGoal(goal);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating goal progress", e);
        }
    }
    
    /**
     * Get current value for a goal based on its category and type
     */
    private float getCurrentValueForGoal(Goal goal) {
        String today = dateFormat.format(new Date());
        DayRecord dayRecord = databaseHelper.getDayRecord(today);
        
        if (dayRecord == null) {
            return 0f;
        }
        
        switch (goal.getCategory()) {
            case FITNESS:
                return getfitnessValue(goal, dayRecord);
            case HEALTH:
                return getHealthValue(goal, dayRecord);
            case PRODUCTIVITY:
                return getProductivityValue(goal, dayRecord);
            case SOCIAL:
                return getSocialValue(goal, dayRecord);
            case WELLNESS:
                return getWellnessValue(goal, dayRecord);
            case HABIT:
                return getHabitValue(goal, dayRecord);
            default:
                return goal.getCurrentValue();
        }
    }
    
    private float getfitnessValue(Goal goal, DayRecord dayRecord) {
        String title = goal.getTitle().toLowerCase();
        
        if (title.contains("steps")) {
            return dayRecord.getSteps();
        } else if (title.contains("distance")) {
            return dayRecord.getTotalTravelDistance();
        } else if (title.contains("calories")) {
            // Estimate calories from steps (rough calculation)
            return dayRecord.getSteps() * 0.04f; // ~0.04 calories per step
        } else if (title.contains("active")) {
            return dayRecord.getActivityScore();
        }
        
        return goal.getCurrentValue();
    }
    
    private float getHealthValue(Goal goal, DayRecord dayRecord) {
        String title = goal.getTitle().toLowerCase();
        
        if (title.contains("water") || title.contains("hydration")) {
            // Placeholder for water intake - would need separate tracking
            return goal.getCurrentValue();
        } else if (title.contains("sleep")) {
            // Would get from sleep tracking service
            return 7.5f; // Default 7.5 hours
        } else if (title.contains("weight")) {
            // Would get from weight tracking
            return goal.getCurrentValue();
        }
        
        return goal.getCurrentValue();
    }
    
    private float getProductivityValue(Goal goal, DayRecord dayRecord) {
        String title = goal.getTitle().toLowerCase();
        
        if (title.contains("screen") && title.contains("limit")) {
            return dayRecord.getScreenTimeMinutes();
        } else if (title.contains("focus")) {
            // Calculate focus time (inverse of screen time)
            return Math.max(0, 480 - dayRecord.getScreenTimeMinutes()); // 8 hours - screen time
        } else if (title.contains("apps")) {
            return dayRecord.getPhoneUnlocks();
        }
        
        return goal.getCurrentValue();
    }
    
    private float getSocialValue(Goal goal, DayRecord dayRecord) {
        String title = goal.getTitle().toLowerCase();
        
        if (title.contains("places") || title.contains("social")) {
            return dayRecord.getPlacesVisited();
        } else if (title.contains("photos")) {
            return dayRecord.getPhotoCount();
        }
        
        return goal.getCurrentValue();
    }
    
    private float getWellnessValue(Goal goal, DayRecord dayRecord) {
        String title = goal.getTitle().toLowerCase();
        
        if (title.contains("mindfulness") || title.contains("meditation")) {
            // Placeholder for meditation tracking
            return goal.getCurrentValue();
        } else if (title.contains("outdoor")) {
            // Estimate outdoor time from places visited
            return dayRecord.getPlacesVisited() * 30; // 30 minutes per place
        }
        
        return goal.getCurrentValue();
    }
    
    private float getHabitValue(Goal goal, DayRecord dayRecord) {
        String title = goal.getTitle().toLowerCase();
        
        if (title.contains("daily") || title.contains("routine")) {
            // Simple binary completion tracking
            return goal.getCurrentValue();
        }
        
        return goal.getCurrentValue();
    }
    
    /**
     * Check and update all goals for daily progress
     */
    public void checkDailyGoalProgress(GoalProgressCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Goal> activeGoals = databaseHelper.getActiveGoals();
                List<GoalProgressUpdate> updates = new ArrayList<>();
                
                for (Goal goal : activeGoals) {
                    float previousValue = goal.getCurrentValue();
                    updateGoalProgress(goal);
                    
                    // Check if goal was completed
                    if (goal.isCompleted() && previousValue != goal.getCurrentValue()) {
                        updates.add(new GoalProgressUpdate(goal, true, true));
                    } else if (goal.getCurrentValue() != previousValue) {
                        updates.add(new GoalProgressUpdate(goal, false, false));
                    }
                }
                
                if (callback != null) {
                    callback.onProgressUpdated(updates);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking daily goal progress", e);
                if (callback != null) {
                    callback.onError("Failed to check goal progress: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get goal statistics
     */
    public void getGoalStatistics(GoalStatsCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Goal> allGoals = databaseHelper.getAllGoals();
                
                GoalStatistics stats = new GoalStatistics();
                stats.totalGoals = allGoals.size();
                stats.activeGoals = 0;
                stats.completedGoals = 0;
                stats.overdueGoals = 0;
                stats.currentStreak = 0;
                stats.longestStreak = 0;
                stats.totalCompletions = 0;
                
                for (Goal goal : allGoals) {
                    if (goal.isActive()) {
                        stats.activeGoals++;
                        
                        if (goal.isCompleted()) {
                            stats.completedGoals++;
                        }
                        
                        if (goal.isOverdue()) {
                            stats.overdueGoals++;
                        }
                        
                        stats.currentStreak = Math.max(stats.currentStreak, goal.getStreakCount());
                        stats.longestStreak = Math.max(stats.longestStreak, goal.getStreakCount());
                        stats.totalCompletions += goal.getTotalCompletions();
                    }
                }
                
                stats.completionRate = stats.totalGoals > 0 ? 
                    (float) stats.completedGoals / stats.totalGoals * 100 : 0;
                
                if (callback != null) {
                    callback.onStatsReceived(stats);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting goal statistics", e);
                if (callback != null) {
                    callback.onError("Failed to get statistics: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Create default goals for new users
     */
    public void createDefaultGoals(GoalListCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Goal> defaultGoals = new ArrayList<>();
                
                // Daily step goal
                Goal stepGoal = new Goal("Daily Steps", Goal.GoalType.ACHIEVE, 
                    Goal.GoalCategory.FITNESS, 8000, "steps", Goal.GoalFrequency.DAILY);
                stepGoal.setDescription("Walk 8,000 steps every day");
                stepGoal.setColor("#4CAF50");
                stepGoal.setPriority(4);
                defaultGoals.add(stepGoal);
                
                // Screen time limit
                Goal screenGoal = new Goal("Screen Time Limit", Goal.GoalType.MINIMIZE, 
                    Goal.GoalCategory.PRODUCTIVITY, 360, "minutes", Goal.GoalFrequency.DAILY);
                screenGoal.setDescription("Limit daily screen time to 6 hours");
                screenGoal.setColor("#FF9800");
                screenGoal.setPriority(3);
                defaultGoals.add(screenGoal);
                
                // Social activity goal
                Goal socialGoal = new Goal("Explore Places", Goal.GoalType.ACHIEVE, 
                    Goal.GoalCategory.SOCIAL, 3, "places", Goal.GoalFrequency.DAILY);
                socialGoal.setDescription("Visit at least 3 different places daily");
                socialGoal.setColor("#2196F3");
                socialGoal.setPriority(2);
                defaultGoals.add(socialGoal);
                
                // Weekly photo goal
                Goal photoGoal = new Goal("Weekly Photos", Goal.GoalType.ACHIEVE, 
                    Goal.GoalCategory.WELLNESS, 20, "photos", Goal.GoalFrequency.WEEKLY);
                photoGoal.setDescription("Capture 20 photos per week");
                photoGoal.setColor("#9C27B0");
                photoGoal.setPriority(1);
                defaultGoals.add(photoGoal);
                
                // Insert all default goals
                for (Goal goal : defaultGoals) {
                    long id = databaseHelper.insertGoal(goal);
                    goal.setId((int) id);
                }
                
                if (callback != null) {
                    callback.onGoalsReceived(defaultGoals);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating default goals", e);
                if (callback != null) {
                    callback.onError("Failed to create default goals: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Reset daily goals for new day
     */
    public void resetDailyGoals() {
        backgroundExecutor.execute(() -> {
            try {
                List<Goal> dailyGoals = databaseHelper.getGoalsByFrequency(Goal.GoalFrequency.DAILY);
                
                for (Goal goal : dailyGoals) {
                    if (goal.isCompleted()) {
                        goal.resetForNextCycle();
                        databaseHelper.updateGoal(goal);
                    }
                }
                
                Log.d(TAG, "Reset " + dailyGoals.size() + " daily goals");
                
            } catch (Exception e) {
                Log.e(TAG, "Error resetting daily goals", e);
            }
        });
    }
    
    /**
     * Validate goal parameters
     */
    private boolean validateGoal(Goal goal) {
        return goal != null && 
               goal.getTitle() != null && !goal.getTitle().trim().isEmpty() &&
               goal.getTargetValue() > 0 &&
               goal.getTargetUnit() != null && !goal.getTargetUnit().trim().isEmpty() &&
               goal.getType() != null &&
               goal.getCategory() != null &&
               goal.getFrequency() != null;
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes
    public static class GoalProgressUpdate {
        public Goal goal;
        public boolean wasCompleted;
        public boolean isNewCompletion;
        
        public GoalProgressUpdate(Goal goal, boolean wasCompleted, boolean isNewCompletion) {
            this.goal = goal;
            this.wasCompleted = wasCompleted;
            this.isNewCompletion = isNewCompletion;
        }
    }
    
    public static class GoalStatistics {
        public int totalGoals;
        public int activeGoals;
        public int completedGoals;
        public int overdueGoals;
        public int currentStreak;
        public int longestStreak;
        public int totalCompletions;
        public float completionRate;
    }
    
    // Callback interfaces
    public interface GoalCallback {
        void onGoalCreated(Goal goal);
        void onGoalUpdated(Goal goal);
        void onGoalDeleted(int goalId);
        void onError(String error);
    }
    
    public interface GoalListCallback {
        void onGoalsReceived(List<Goal> goals);
        void onError(String error);
    }
    
    public interface GoalProgressCallback {
        void onProgressUpdated(List<GoalProgressUpdate> updates);
        void onError(String error);
    }
    
    public interface GoalStatsCallback {
        void onStatsReceived(GoalStatistics stats);
        void onError(String error);
    }
}