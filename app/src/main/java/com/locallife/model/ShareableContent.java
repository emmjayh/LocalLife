package com.locallife.model;

import android.graphics.Bitmap;
import java.util.Date;

/**
 * Model for content that can be shared on social media
 */
public class ShareableContent {
    private String title;
    private String description;
    private String imageUrl;
    private Bitmap imageBitmap;
    private ShareType shareType;
    private String shareText;
    private String hashtags;
    private Date createdAt;
    private String additionalData;
    
    public ShareableContent() {
        this.createdAt = new Date();
    }
    
    public ShareableContent(String title, String description, ShareType shareType) {
        this();
        this.title = title;
        this.description = description;
        this.shareType = shareType;
        generateShareContent();
    }
    
    /**
     * Generate appropriate share content based on type
     */
    private void generateShareContent() {
        switch (shareType) {
            case ACHIEVEMENT:
                generateAchievementShareContent();
                break;
            case LEVEL_UP:
                generateLevelUpShareContent();
                break;
            case GOAL_COMPLETION:
                generateGoalShareContent();
                break;
            case STREAK_MILESTONE:
                generateStreakShareContent();
                break;
            case DAILY_SUMMARY:
                generateDailySummaryShareContent();
                break;
            case PHOTO_MEMORY:
                generatePhotoMemoryShareContent();
                break;
            case ACTIVITY_MILESTONE:
                generateActivityMilestoneShareContent();
                break;
        }
    }
    
    private void generateAchievementShareContent() {
        this.shareText = "🏆 Just unlocked: " + title + "! " + description;
        this.hashtags = "#LocalLife #Achievement #PersonalGrowth #Goals";
    }
    
    private void generateLevelUpShareContent() {
        this.shareText = "🎉 Level up! " + title + " - " + description;
        this.hashtags = "#LocalLife #LevelUp #Progress #Growth";
    }
    
    private void generateGoalShareContent() {
        this.shareText = "✅ Goal achieved: " + title + "! " + description;
        this.hashtags = "#LocalLife #GoalAchieved #Success #PersonalDevelopment";
    }
    
    private void generateStreakShareContent() {
        this.shareText = "🔥 " + title + " streak! " + description;
        this.hashtags = "#LocalLife #Streak #Consistency #Motivation";
    }
    
    private void generateDailySummaryShareContent() {
        this.shareText = "📊 My day: " + description;
        this.hashtags = "#LocalLife #DailyLife #Statistics #Tracking";
    }
    
    private void generatePhotoMemoryShareContent() {
        this.shareText = "📸 " + title + " - " + description;
        this.hashtags = "#LocalLife #Memories #Photography #Life";
    }
    
    private void generateActivityMilestoneShareContent() {
        this.shareText = "🚀 " + title + "! " + description;
        this.hashtags = "#LocalLife #Milestone #Activity #Achievement";
    }
    
    /**
     * Get formatted share text for different platforms
     */
    public String getFormattedShareText(SharePlatform platform) {
        StringBuilder text = new StringBuilder();
        
        switch (platform) {
            case TWITTER:
                text.append(shareText);
                if (hashtags != null && !hashtags.isEmpty()) {
                    text.append(" ").append(hashtags);
                }
                // Limit to 280 characters
                if (text.length() > 280) {
                    text.setLength(277);
                    text.append("...");
                }
                break;
                
            case INSTAGRAM:
                text.append(shareText).append("\n\n");
                if (hashtags != null && !hashtags.isEmpty()) {
                    text.append(hashtags.replace("#", "#️⃣"));
                }
                break;
                
            case FACEBOOK:
                text.append(shareText);
                if (description != null && !description.isEmpty()) {
                    text.append("\n\n").append(description);
                }
                break;
                
            case LINKEDIN:
                text.append(shareText);
                if (description != null && !description.isEmpty()) {
                    text.append("\n\n").append(description);
                }
                text.append("\n\n#PersonalDevelopment #Goals #Progress");
                break;
                
            case WHATSAPP:
            case TELEGRAM:
                text.append(shareText);
                break;
                
            case GENERIC:
            default:
                text.append(shareText);
                break;
        }
        
        return text.toString();
    }
    
    /**
     * Create shareable content for achievement unlock
     */
    public static ShareableContent createAchievementShare(Achievement achievement) {
        String title = achievement.getTitle();
        String description = achievement.getDescription() + " (" + achievement.getTierDisplayName() + " tier)";
        
        ShareableContent content = new ShareableContent(title, description, ShareType.ACHIEVEMENT);
        content.setAdditionalData("achievement_id:" + achievement.getId());
        
        return content;
    }
    
    /**
     * Create shareable content for level up
     */
    public static ShareableContent createLevelUpShare(UserLevel userLevel) {
        String title = "Level " + userLevel.getCurrentLevel() + " " + userLevel.getCurrentTitle();
        String description = userLevel.getStatsSummary();
        
        ShareableContent content = new ShareableContent(title, description, ShareType.LEVEL_UP);
        content.setAdditionalData("level:" + userLevel.getCurrentLevel());
        
        return content;
    }
    
    /**
     * Create shareable content for goal completion
     */
    public static ShareableContent createGoalShare(Goal goal) {
        String title = goal.getTitle();
        String description = goal.getDescription() + " - " + goal.getFormattedProgress();
        
        ShareableContent content = new ShareableContent(title, description, ShareType.GOAL_COMPLETION);
        content.setAdditionalData("goal_id:" + goal.getId());
        
        return content;
    }
    
    /**
     * Create shareable content for streak milestone
     */
    public static ShareableContent createStreakShare(Goal goal) {
        String title = goal.getStreakCount() + "-day streak";
        String description = "Consistently achieving: " + goal.getTitle();
        
        ShareableContent content = new ShareableContent(title, description, ShareType.STREAK_MILESTONE);
        content.setAdditionalData("streak_count:" + goal.getStreakCount());
        
        return content;
    }
    
    /**
     * Create shareable content for daily summary
     */
    public static ShareableContent createDailySummaryShare(DayRecord dayRecord) {
        String title = "Daily Summary";
        String description = String.format("%,d steps • %d places • %d photos • Activity Score: %d",
                dayRecord.getSteps(), dayRecord.getPlacesVisited(), 
                dayRecord.getPhotoCount(), dayRecord.getActivityScore());
        
        ShareableContent content = new ShareableContent(title, description, ShareType.DAILY_SUMMARY);
        content.setAdditionalData("date:" + dayRecord.getDate());
        
        return content;
    }
    
    /**
     * Create shareable content for activity milestone
     */
    public static ShareableContent createActivityMilestoneShare(String milestone, String details) {
        ShareableContent content = new ShareableContent(milestone, details, ShareType.ACTIVITY_MILESTONE);
        return content;
    }
    
    // Enums
    public enum ShareType {
        ACHIEVEMENT,
        LEVEL_UP,
        GOAL_COMPLETION,
        STREAK_MILESTONE,
        DAILY_SUMMARY,
        PHOTO_MEMORY,
        ACTIVITY_MILESTONE
    }
    
    public enum SharePlatform {
        TWITTER,
        INSTAGRAM,
        FACEBOOK,
        LINKEDIN,
        WHATSAPP,
        TELEGRAM,
        GENERIC
    }
    
    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Bitmap getImageBitmap() { return imageBitmap; }
    public void setImageBitmap(Bitmap imageBitmap) { this.imageBitmap = imageBitmap; }
    
    public ShareType getShareType() { return shareType; }
    public void setShareType(ShareType shareType) { this.shareType = shareType; }
    
    public String getShareText() { return shareText; }
    public void setShareText(String shareText) { this.shareText = shareText; }
    
    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public String getAdditionalData() { return additionalData; }
    public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }
}