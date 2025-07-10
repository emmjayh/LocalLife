package com.locallife.service;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.locallife.model.Achievement;
import com.locallife.model.DayRecord;
import com.locallife.model.Goal;
import com.locallife.model.ShareableContent;
import com.locallife.model.ShareableContent.SharePlatform;
import com.locallife.model.UserLevel;

/**
 * Example implementation showing how to use the SocialSharingService
 */
public class SocialSharingExample {
    private static final String TAG = "SocialSharingExample";
    
    private Activity activity;
    private SocialSharingService socialSharingService;
    
    public SocialSharingExample(Activity activity) {
        this.activity = activity;
        this.socialSharingService = new SocialSharingService(activity);
    }
    
    /**
     * Example: Share achievement unlock
     */
    public void shareAchievementExample() {
        // Create a sample achievement
        Achievement achievement = new Achievement();
        achievement.setId(1);
        achievement.setTitle("First Steps");
        achievement.setDescription("Walked 1,000 steps in a day");
        achievement.setTier(Achievement.AchievementTier.BRONZE);
        achievement.setUnlocked(true);
        achievement.setUnlockedAt(new java.util.Date());
        
        // Share to Twitter
        socialSharingService.shareAchievement(achievement, SharePlatform.TWITTER, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Achievement shared successfully to " + platform.name() + " with ID: " + shareId);
                    showToast("Achievement shared to " + platform.name() + "!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing achievement to " + platform.name(), error);
                    showToast("Failed to share achievement: " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Achievement share cancelled for " + platform.name());
                    showToast("Share cancelled");
                }
            });
    }
    
    /**
     * Example: Share level up
     */
    public void shareLevelUpExample() {
        // Create a sample user level
        UserLevel userLevel = new UserLevel();
        userLevel.setCurrentLevel(5);
        userLevel.setCurrentTitle("Novice");
        userLevel.setTotalXP(1250);
        userLevel.setAchievementsUnlocked(3);
        userLevel.setGoalsCompleted(8);
        
        // Share to Instagram
        socialSharingService.shareLevelUp(userLevel, SharePlatform.INSTAGRAM, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Level up shared successfully to " + platform.name());
                    showToast("Level up shared to " + platform.name() + "!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing level up to " + platform.name(), error);
                    showToast("Failed to share level up: " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Level up share cancelled for " + platform.name());
                    showToast("Share cancelled");
                }
            });
    }
    
    /**
     * Example: Share goal completion
     */
    public void shareGoalCompletionExample() {
        // Create a sample goal
        Goal goal = new Goal();
        goal.setId(1);
        goal.setTitle("Daily Step Goal");
        goal.setDescription("Walk 10,000 steps daily");
        goal.setTargetValue(10000);
        goal.setCurrentValue(10000);
        goal.setTargetUnit("steps");
        goal.setCompleted(true);
        goal.setLastCompletedDate(new java.util.Date());
        
        // Share to LinkedIn
        socialSharingService.shareGoalCompletion(goal, SharePlatform.LINKEDIN, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Goal completion shared successfully to " + platform.name());
                    showToast("Goal completion shared to " + platform.name() + "!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing goal completion to " + platform.name(), error);
                    showToast("Failed to share goal completion: " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Goal completion share cancelled for " + platform.name());
                    showToast("Share cancelled");
                }
            });
    }
    
    /**
     * Example: Share streak milestone
     */
    public void shareStreakMilestoneExample() {
        // Create a sample goal with streak
        Goal goal = new Goal();
        goal.setId(1);
        goal.setTitle("Morning Workout");
        goal.setDescription("Exercise for 30 minutes every morning");
        goal.setStreakCount(7);
        goal.setLastCompletedDate(new java.util.Date());
        
        // Share to WhatsApp
        socialSharingService.shareStreakMilestone(goal, SharePlatform.WHATSAPP, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Streak milestone shared successfully to " + platform.name());
                    showToast("Streak milestone shared to " + platform.name() + "!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing streak milestone to " + platform.name(), error);
                    showToast("Failed to share streak milestone: " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Streak milestone share cancelled for " + platform.name());
                    showToast("Share cancelled");
                }
            });
    }
    
    /**
     * Example: Share daily summary
     */
    public void shareDailySummaryExample() {
        // Create a sample day record
        DayRecord dayRecord = new DayRecord();
        dayRecord.setDate("2024-01-15");
        dayRecord.setStepCount(12500);
        dayRecord.setPlacesVisited(5);
        dayRecord.setPhotoCount(8);
        dayRecord.setActivityScore(85.5f);
        dayRecord.setActiveMinutes(45);
        
        // Share to Facebook
        socialSharingService.shareDailySummary(dayRecord, SharePlatform.FACEBOOK, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Daily summary shared successfully to " + platform.name());
                    showToast("Daily summary shared to " + platform.name() + "!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing daily summary to " + platform.name(), error);
                    showToast("Failed to share daily summary: " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Daily summary share cancelled for " + platform.name());
                    showToast("Share cancelled");
                }
            });
    }
    
    /**
     * Example: Show share dialog for user selection
     */
    public void showShareDialogExample() {
        // Create shareable content
        Achievement achievement = new Achievement();
        achievement.setTitle("Fitness Enthusiast");
        achievement.setDescription("Completed 30 workouts this month");
        achievement.setTier(Achievement.AchievementTier.GOLD);
        achievement.setUnlocked(true);
        
        ShareableContent content = ShareableContent.createAchievementShare(achievement);
        
        // Show dialog for platform selection
        socialSharingService.showShareDialog(activity, content, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Content shared successfully to " + platform.name());
                    showToast("Shared to " + platform.name() + "!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing content to " + platform.name(), error);
                    showToast("Failed to share: " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Share cancelled");
                    showToast("Share cancelled");
                }
            });
    }
    
    /**
     * Example: Share to multiple platforms
     */
    public void shareToMultiplePlatformsExample() {
        // Create shareable content
        UserLevel userLevel = new UserLevel();
        userLevel.setCurrentLevel(10);
        userLevel.setCurrentTitle("Intermediate");
        userLevel.setTotalXP(2500);
        
        ShareableContent content = ShareableContent.createLevelUpShare(userLevel);
        
        // Share to multiple platforms
        SharePlatform[] platforms = {SharePlatform.TWITTER, SharePlatform.FACEBOOK, SharePlatform.LINKEDIN};
        
        socialSharingService.shareToMultiplePlatforms(content, platforms, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Content shared successfully to " + platform.name());
                    showToast("Shared to " + platform.name() + "!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing content to " + platform.name(), error);
                    showToast("Failed to share to " + platform.name() + ": " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Share cancelled for " + platform.name());
                    showToast("Share cancelled for " + platform.name());
                }
            });
    }
    
    /**
     * Example: Check platform availability
     */
    public void checkPlatformAvailabilityExample() {
        Log.d(TAG, "Platform availability:");
        
        for (SharePlatform platform : SharePlatform.values()) {
            boolean isAvailable = socialSharingService.isPlatformAvailable(platform);
            Log.d(TAG, platform.name() + ": " + (isAvailable ? "Available" : "Not available"));
        }
        
        // Get most popular platform
        SharePlatform mostPopular = socialSharingService.getMostPopularPlatform();
        Log.d(TAG, "Most popular platform: " + mostPopular.name());
    }
    
    /**
     * Example: Get sharing statistics
     */
    public void getShareStatisticsExample() {
        java.util.Map<String, Object> stats = socialSharingService.getShareStatistics();
        
        Log.d(TAG, "Share statistics:");
        for (java.util.Map.Entry<String, Object> entry : stats.entrySet()) {
            Log.d(TAG, entry.getKey() + ": " + entry.getValue());
        }
    }
    
    /**
     * Example: Get share history
     */
    public void getShareHistoryExample() {
        java.util.Map<String, Object> history = socialSharingService.getShareHistory(20);
        
        Log.d(TAG, "Share history:");
        for (java.util.Map.Entry<String, Object> entry : history.entrySet()) {
            Log.d(TAG, entry.getKey() + ": " + entry.getValue());
        }
    }
    
    /**
     * Example: Clear share cache
     */
    public void clearShareCacheExample() {
        socialSharingService.clearShareCache();
        Log.d(TAG, "Share cache cleared");
        showToast("Share cache cleared");
    }
    
    /**
     * Helper method to show toast messages
     */
    private void showToast(String message) {
        if (activity != null) {
            activity.runOnUiThread(() -> 
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
        }
    }
    
    /**
     * Example: Custom shareable content
     */
    public void createCustomShareableContentExample() {
        // Create custom shareable content
        ShareableContent content = new ShareableContent();
        content.setTitle("My LocalLife Journey");
        content.setDescription("Just reached level 15 in my personal development journey!");
        content.setShareType(ShareableContent.ShareType.ACTIVITY_MILESTONE);
        content.setShareText("🚀 Milestone achieved! Just reached level 15 in my personal development journey with LocalLife! #PersonalGrowth #LocalLife");
        content.setHashtags("#LocalLife #PersonalGrowth #Milestone #Achievement");
        content.setAdditionalData("custom_milestone:level_15");
        
        // Share custom content
        socialSharingService.shareContent(content, SharePlatform.TWITTER, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Custom content shared successfully");
                    showToast("Custom content shared!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Error sharing custom content", error);
                    showToast("Failed to share custom content");
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Custom content share cancelled");
                    showToast("Share cancelled");
                }
            });
    }
    
    /**
     * Example: Handle share result (call this from your Activity's onActivityResult)
     */
    public void handleShareResultExample(int requestCode, int resultCode, android.content.Intent data) {
        socialSharingService.handleShareResult(requestCode, resultCode, data, 
            new SocialSharingService.ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    Log.d(TAG, "Share result: Success for " + platform.name());
                    showToast("Share completed successfully!");
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    Log.e(TAG, "Share result: Error for " + platform.name(), error);
                    showToast("Share failed: " + error.getMessage());
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    Log.d(TAG, "Share result: Cancelled for " + platform.name());
                    showToast("Share was cancelled");
                }
            });
    }
}