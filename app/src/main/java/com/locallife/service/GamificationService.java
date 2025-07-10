package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.Achievement;
import com.locallife.model.DayRecord;
import com.locallife.model.Goal;
import com.locallife.model.UserLevel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for managing gamification elements including achievements, levels, and XP
 */
public class GamificationService {
    private static final String TAG = "GamificationService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService backgroundExecutor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public GamificationService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Check and unlock achievements based on current user data
     */
    public void checkAchievements(AchievementCheckCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Achievement> newlyUnlocked = new ArrayList<>();
                List<Achievement> allAchievements = databaseHelper.getAllAchievements();
                String today = dateFormat.format(new Date());
                DayRecord todayRecord = databaseHelper.getDayRecord(today);
                
                if (todayRecord == null) {
                    if (callback != null) callback.onAchievementsChecked(newlyUnlocked);
                    return;
                }
                
                for (Achievement achievement : allAchievements) {
                    if (!achievement.isUnlocked()) {
                        boolean shouldUnlock = evaluateAchievement(achievement, todayRecord);
                        if (shouldUnlock) {
                            achievement.unlock();
                            databaseHelper.updateAchievement(achievement);
                            newlyUnlocked.add(achievement);
                            
                            // Award XP for unlocking achievement
                            awardXP(UserLevel.ActivityType.ACHIEVEMENT_UNLOCKED, achievement.getPointsValue());
                        }
                    }
                }
                
                if (callback != null) {
                    callback.onAchievementsChecked(newlyUnlocked);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking achievements", e);
                if (callback != null) {
                    callback.onError("Failed to check achievements: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Evaluate if an achievement should be unlocked
     */
    private boolean evaluateAchievement(Achievement achievement, DayRecord dayRecord) {
        switch (achievement.getCategory()) {
            case FITNESS:
                return evaluateFitnessAchievement(achievement, dayRecord);
            case HEALTH:
                return evaluateHealthAchievement(achievement, dayRecord);
            case PRODUCTIVITY:
                return evaluateProductivityAchievement(achievement, dayRecord);
            case SOCIAL:
                return evaluateSocialAchievement(achievement, dayRecord);
            case EXPLORATION:
                return evaluateExplorationAchievement(achievement, dayRecord);
            case PHOTOGRAPHY:
                return evaluatePhotographyAchievement(achievement, dayRecord);
            case CONSISTENCY:
                return evaluateConsistencyAchievement(achievement);
            case ENVIRONMENTAL:
                return evaluateEnvironmentalAchievement(achievement, dayRecord);
            case LEARNING:
                return evaluateLearningAchievement(achievement);
            case SPECIAL:
                return evaluateSpecialAchievement(achievement, dayRecord);
            default:
                return false;
        }
    }
    
    private boolean evaluateFitnessAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("first steps")) {
            achievement.updateProgress(dayRecord.getSteps());
            return dayRecord.getSteps() >= 1000;
        } else if (title.contains("step master")) {
            achievement.updateProgress(dayRecord.getSteps());
            return dayRecord.getSteps() >= achievement.getTargetValue();
        } else if (title.contains("marathon walker")) {
            achievement.updateProgress(dayRecord.getSteps());
            return dayRecord.getSteps() >= 25000;
        } else if (title.contains("distance traveler")) {
            int distance = (int) dayRecord.getTotalTravelDistance();
            achievement.updateProgress(distance);
            return distance >= achievement.getTargetValue();
        }
        
        return false;
    }
    
    private boolean evaluateHealthAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("wellness warrior")) {
            achievement.updateProgress(dayRecord.getOverallWellbeingScore());
            return dayRecord.getOverallWellbeingScore() >= achievement.getTargetValue();
        } else if (title.contains("active lifestyle")) {
            achievement.updateProgress(dayRecord.getActivityScore());
            return dayRecord.getActivityScore() >= achievement.getTargetValue();
        }
        
        return false;
    }
    
    private boolean evaluateProductivityAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("screen time master")) {
            // Invert for screen time - lower is better
            int screenTimeScore = Math.max(0, 480 - dayRecord.getScreenTimeMinutes()); // 8 hours max
            achievement.updateProgress(screenTimeScore);
            return screenTimeScore >= achievement.getTargetValue();
        } else if (title.contains("focus champion")) {
            achievement.updateProgress(dayRecord.getProductivityScore());
            return dayRecord.getProductivityScore() >= achievement.getTargetValue();
        }
        
        return false;
    }
    
    private boolean evaluateSocialAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("explorer")) {
            achievement.updateProgress(dayRecord.getPlacesVisited());
            return dayRecord.getPlacesVisited() >= achievement.getTargetValue();
        } else if (title.contains("social butterfly")) {
            achievement.updateProgress(dayRecord.getSocialActivityScore());
            return dayRecord.getSocialActivityScore() >= achievement.getTargetValue();
        }
        
        return false;
    }
    
    private boolean evaluateExplorationAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("wanderer")) {
            achievement.updateProgress(dayRecord.getPlacesVisited());
            return dayRecord.getPlacesVisited() >= achievement.getTargetValue();
        } else if (title.contains("adventurer")) {
            int travelDistance = (int) dayRecord.getTotalTravelDistance();
            achievement.updateProgress(travelDistance);
            return travelDistance >= achievement.getTargetValue();
        }
        
        return false;
    }
    
    private boolean evaluatePhotographyAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("photographer")) {
            achievement.updateProgress(dayRecord.getPhotoCount());
            return dayRecord.getPhotoCount() >= achievement.getTargetValue();
        } else if (title.contains("memory keeper")) {
            achievement.updateProgress(dayRecord.getPhotoActivityScore());
            return dayRecord.getPhotoActivityScore() >= achievement.getTargetValue();
        }
        
        return false;
    }
    
    private boolean evaluateConsistencyAchievement(Achievement achievement) {
        // Check streak-based achievements
        List<Goal> activeGoals = databaseHelper.getActiveGoals();
        int maxStreak = 0;
        
        for (Goal goal : activeGoals) {
            maxStreak = Math.max(maxStreak, goal.getStreakCount());
        }
        
        achievement.updateProgress(maxStreak);
        return maxStreak >= achievement.getStreakRequirement();
    }
    
    private boolean evaluateEnvironmentalAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("weather watcher")) {
            // Achievement for tracking weather daily
            achievement.addProgress(1);
            return achievement.getCurrentProgress() >= achievement.getTargetValue();
        }
        
        return false;
    }
    
    private boolean evaluateLearningAchievement(Achievement achievement) {
        // These would be triggered by specific app interactions
        // For now, return false as they require manual triggering
        return false;
    }
    
    private boolean evaluateSpecialAchievement(Achievement achievement, DayRecord dayRecord) {
        String title = achievement.getTitle().toLowerCase();
        
        if (title.contains("perfect day")) {
            // Check if all goals were completed today
            List<Goal> todaysGoals = databaseHelper.getActiveGoals();
            boolean allCompleted = true;
            
            for (Goal goal : todaysGoals) {
                if (goal.getFrequency() == Goal.GoalFrequency.DAILY && !goal.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                achievement.updateProgress(1);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Award XP for activity
     */
    public void awardXP(UserLevel.ActivityType activityType, Integer customAmount) {
        backgroundExecutor.execute(() -> {
            try {
                UserLevel userLevel = databaseHelper.getUserLevel();
                if (userLevel == null) {
                    userLevel = new UserLevel();
                    databaseHelper.insertUserLevel(userLevel);
                }
                
                int xpAmount = customAmount != null ? customAmount : UserLevel.getXPForActivity(activityType);
                boolean leveledUp = userLevel.addXP(xpAmount);
                
                databaseHelper.updateUserLevel(userLevel);
                
                Log.d(TAG, "Awarded " + xpAmount + " XP for " + activityType + 
                      (leveledUp ? " - LEVEL UP to " + userLevel.getCurrentLevel() : ""));
                
            } catch (Exception e) {
                Log.e(TAG, "Error awarding XP", e);
            }
        });
    }
    
    /**
     * Award XP with default amount
     */
    public void awardXP(UserLevel.ActivityType activityType) {
        awardXP(activityType, null);
    }
    
    /**
     * Get user level information
     */
    public void getUserLevel(UserLevelCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                UserLevel userLevel = databaseHelper.getUserLevel();
                if (userLevel == null) {
                    userLevel = new UserLevel();
                    databaseHelper.insertUserLevel(userLevel);
                }
                
                // Update stats
                int achievements = databaseHelper.getUnlockedAchievementsCount();
                List<Goal> goals = databaseHelper.getAllGoals();
                int completedGoals = 0;
                int totalStreaks = 0;
                
                for (Goal goal : goals) {
                    if (goal.isCompleted()) completedGoals++;
                    totalStreaks += goal.getStreakCount();
                }
                
                userLevel.updateStats(achievements, totalStreaks, completedGoals);
                databaseHelper.updateUserLevel(userLevel);
                
                if (callback != null) {
                    callback.onUserLevelReceived(userLevel);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting user level", e);
                if (callback != null) {
                    callback.onError("Failed to get user level: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get achievements by category
     */
    public void getAchievementsByCategory(Achievement.AchievementCategory category, AchievementListCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Achievement> achievements = databaseHelper.getAchievementsByCategory(category);
                
                if (callback != null) {
                    callback.onAchievementsReceived(achievements);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting achievements by category", e);
                if (callback != null) {
                    callback.onError("Failed to get achievements: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get all achievements with filter options
     */
    public void getAllAchievements(boolean unlockedOnly, boolean hiddenIncluded, AchievementListCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Achievement> achievements = databaseHelper.getAllAchievements();
                List<Achievement> filteredAchievements = new ArrayList<>();
                
                for (Achievement achievement : achievements) {
                    // Filter by unlocked status
                    if (unlockedOnly && !achievement.isUnlocked()) {
                        continue;
                    }
                    
                    // Filter by hidden status
                    if (!hiddenIncluded && achievement.isHidden() && !achievement.isUnlocked()) {
                        continue;
                    }
                    
                    filteredAchievements.add(achievement);
                }
                
                if (callback != null) {
                    callback.onAchievementsReceived(filteredAchievements);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting all achievements", e);
                if (callback != null) {
                    callback.onError("Failed to get achievements: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Create default achievements for new users
     */
    public void createDefaultAchievements(AchievementListCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<Achievement> achievements = new ArrayList<>();
                
                // Fitness achievements
                achievements.add(new Achievement("First Steps", "Take your first 1,000 steps",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.FITNESS,
                    Achievement.AchievementTier.BRONZE, 1000, "steps"));
                
                achievements.add(new Achievement("Step Master", "Walk 10,000 steps in a day",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.FITNESS,
                    Achievement.AchievementTier.SILVER, 10000, "steps"));
                
                achievements.add(new Achievement("Marathon Walker", "Walk 25,000 steps in a day",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.FITNESS,
                    Achievement.AchievementTier.GOLD, 25000, "steps"));
                
                // Social achievements
                achievements.add(new Achievement("Explorer", "Visit 5 different places in a day",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.SOCIAL,
                    Achievement.AchievementTier.BRONZE, 5, "places"));
                
                achievements.add(new Achievement("Wanderer", "Visit 10 different places in a day",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.EXPLORATION,
                    Achievement.AchievementTier.SILVER, 10, "places"));
                
                // Photography achievements
                achievements.add(new Achievement("Photographer", "Take 20 photos in a day",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.PHOTOGRAPHY,
                    Achievement.AchievementTier.BRONZE, 20, "photos"));
                
                achievements.add(new Achievement("Memory Keeper", "Achieve high photo activity score",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.PHOTOGRAPHY,
                    Achievement.AchievementTier.SILVER, 80, "points"));
                
                // Consistency achievements
                Achievement streakAchievement = new Achievement("Consistency Champion", "Maintain a 7-day goal streak",
                    Achievement.AchievementType.STREAK, Achievement.AchievementCategory.CONSISTENCY,
                    Achievement.AchievementTier.GOLD, 7, "days");
                streakAchievement.setStreakRequirement(7);
                achievements.add(streakAchievement);
                
                // Productivity achievements
                achievements.add(new Achievement("Screen Time Master", "Limit screen time effectively",
                    Achievement.AchievementType.MILESTONE, Achievement.AchievementCategory.PRODUCTIVITY,
                    Achievement.AchievementTier.SILVER, 300, "points"));
                
                // Special achievements
                Achievement perfectDay = new Achievement("Perfect Day", "Complete all daily goals",
                    Achievement.AchievementType.SPECIAL, Achievement.AchievementCategory.SPECIAL,
                    Achievement.AchievementTier.PLATINUM, 1, "perfect day");
                perfectDay.setCustomCondition("Complete all daily goals in a single day");
                achievements.add(perfectDay);
                
                // Insert all achievements
                for (Achievement achievement : achievements) {
                    long id = databaseHelper.insertAchievement(achievement);
                    achievement.setId((int) id);
                }
                
                if (callback != null) {
                    callback.onAchievementsReceived(achievements);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating default achievements", e);
                if (callback != null) {
                    callback.onError("Failed to create achievements: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Trigger daily activity check
     */
    public void processDailyActivity() {
        backgroundExecutor.execute(() -> {
            // Award daily login XP
            awardXP(UserLevel.ActivityType.DAILY_LOGIN);
            
            // Check for achievements
            checkAchievements(null);
            
            Log.d(TAG, "Processed daily activity");
        });
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Callback interfaces
    public interface AchievementCheckCallback {
        void onAchievementsChecked(List<Achievement> newlyUnlocked);
        void onError(String error);
    }
    
    public interface AchievementListCallback {
        void onAchievementsReceived(List<Achievement> achievements);
        void onError(String error);
    }
    
    public interface UserLevelCallback {
        void onUserLevelReceived(UserLevel userLevel);
        void onError(String error);
    }
}