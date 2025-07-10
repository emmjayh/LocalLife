package com.locallife.model;

import java.util.Date;

/**
 * Model for user achievements and badges in the gamification system
 */
public class Achievement {
    private int id;
    private String title;
    private String description;
    private AchievementType type;
    private AchievementCategory category;
    private AchievementTier tier;
    private String iconName;
    private String badgeColor;
    private int targetValue;
    private String targetUnit;
    private int currentProgress;
    private boolean isUnlocked;
    private Date unlockedAt;
    private Date createdAt;
    private String requirements;
    private int pointsValue;
    private boolean isHidden; // Secret achievements
    private int streakRequirement;
    private String customCondition;
    
    public Achievement() {
        this.createdAt = new Date();
        this.isUnlocked = false;
        this.isHidden = false;
        this.currentProgress = 0;
        this.pointsValue = 10;
    }
    
    public Achievement(String title, String description, AchievementType type, 
                      AchievementCategory category, AchievementTier tier, 
                      int targetValue, String targetUnit) {
        this();
        this.title = title;
        this.description = description;
        this.type = type;
        this.category = category;
        this.tier = tier;
        this.targetValue = targetValue;
        this.targetUnit = targetUnit;
        setDefaultsBasedOnTier();
    }
    
    /**
     * Set default values based on achievement tier
     */
    private void setDefaultsBasedOnTier() {
        switch (tier) {
            case BRONZE:
                this.pointsValue = 10;
                this.badgeColor = "#CD7F32";
                break;
            case SILVER:
                this.pointsValue = 25;
                this.badgeColor = "#C0C0C0";
                break;
            case GOLD:
                this.pointsValue = 50;
                this.badgeColor = "#FFD700";
                break;
            case PLATINUM:
                this.pointsValue = 100;
                this.badgeColor = "#E5E4E2";
                break;
            case DIAMOND:
                this.pointsValue = 200;
                this.badgeColor = "#B9F2FF";
                break;
            case LEGENDARY:
                this.pointsValue = 500;
                this.badgeColor = "#FF6B35";
                break;
        }
    }
    
    /**
     * Update achievement progress
     */
    public void updateProgress(int newProgress) {
        this.currentProgress = newProgress;
        
        // Check if achievement should be unlocked
        if (!isUnlocked && shouldUnlock()) {
            unlock();
        }
    }
    
    /**
     * Add incremental progress
     */
    public void addProgress(int increment) {
        updateProgress(currentProgress + increment);
    }
    
    /**
     * Check if achievement should be unlocked based on current progress
     */
    private boolean shouldUnlock() {
        switch (type) {
            case MILESTONE:
                return currentProgress >= targetValue;
            case STREAK:
                return currentProgress >= streakRequirement;
            case CUMULATIVE:
                return currentProgress >= targetValue;
            case SPECIAL:
                return evaluateSpecialCondition();
            case SOCIAL:
                return currentProgress >= targetValue;
            default:
                return false;
        }
    }
    
    /**
     * Evaluate special achievement conditions
     */
    private boolean evaluateSpecialCondition() {
        // This would be implemented based on custom conditions
        // For now, return false for special achievements
        return false;
    }
    
    /**
     * Unlock the achievement
     */
    public void unlock() {
        if (!isUnlocked) {
            this.isUnlocked = true;
            this.unlockedAt = new Date();
        }
    }
    
    /**
     * Get progress percentage
     */
    public float getProgressPercentage() {
        if (targetValue == 0) return 0f;
        return Math.min(100f, ((float) currentProgress / targetValue) * 100f);
    }
    
    /**
     * Get formatted progress text
     */
    public String getFormattedProgress() {
        if (targetUnit != null && !targetUnit.isEmpty()) {
            return currentProgress + " / " + targetValue + " " + targetUnit;
        }
        return currentProgress + " / " + targetValue;
    }
    
    /**
     * Get achievement status text
     */
    public String getStatusText() {
        if (isUnlocked) {
            return "Unlocked";
        } else if (isHidden && currentProgress == 0) {
            return "Hidden";
        } else {
            return "In Progress";
        }
    }
    
    /**
     * Get tier display name
     */
    public String getTierDisplayName() {
        return tier.name().charAt(0) + tier.name().substring(1).toLowerCase();
    }
    
    /**
     * Get icon resource name based on category and tier
     */
    public String getIconResourceName() {
        if (iconName != null && !iconName.isEmpty()) {
            return iconName;
        }
        
        // Generate default icon name
        String categoryPrefix = category.name().toLowerCase();
        String tierSuffix = tier.name().toLowerCase();
        return "achievement_" + categoryPrefix + "_" + tierSuffix;
    }
    
    /**
     * Get formatted requirements text
     */
    public String getFormattedRequirements() {
        if (requirements != null && !requirements.isEmpty()) {
            return requirements;
        }
        
        switch (type) {
            case MILESTONE:
                return "Reach " + targetValue + " " + (targetUnit != null ? targetUnit : "points");
            case STREAK:
                return "Maintain a " + streakRequirement + " day streak";
            case CUMULATIVE:
                return "Accumulate " + targetValue + " " + (targetUnit != null ? targetUnit : "points") + " total";
            case SPECIAL:
                return customCondition != null ? customCondition : "Complete special condition";
            case SOCIAL:
                return "Social interaction achievement";
            default:
                return "Complete the challenge";
        }
    }
    
    // Enums
    public enum AchievementType {
        MILESTONE,     // Reach a specific target
        STREAK,        // Maintain activity for consecutive days
        CUMULATIVE,    // Accumulate total over time
        SPECIAL,       // Special events or conditions
        SOCIAL         // Social interactions
    }
    
    public enum AchievementCategory {
        FITNESS,       // Step count, distance, activity
        HEALTH,        // Sleep, heart rate, wellness
        PRODUCTIVITY,  // Screen time limits, focus time
        SOCIAL,        // Places visited, social interactions
        EXPLORATION,   // New locations, travel
        PHOTOGRAPHY,   // Photos taken, photo quality
        CONSISTENCY,   // Daily usage, habit formation
        ENVIRONMENTAL, // Weather interactions, outdoor time
        LEARNING,      // App usage, feature discovery
        SPECIAL        // Holiday events, milestones
    }
    
    public enum AchievementTier {
        BRONZE,        // Basic achievements
        SILVER,        // Intermediate achievements
        GOLD,          // Advanced achievements
        PLATINUM,      // Expert achievements
        DIAMOND,       // Master achievements
        LEGENDARY      // Epic achievements
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public AchievementType getType() { return type; }
    public void setType(AchievementType type) { this.type = type; }
    
    public AchievementCategory getCategory() { return category; }
    public void setCategory(AchievementCategory category) { this.category = category; }
    
    public AchievementTier getTier() { return tier; }
    public void setTier(AchievementTier tier) { this.tier = tier; }
    
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    
    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }
    
    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }
    
    public String getTargetUnit() { return targetUnit; }
    public void setTargetUnit(String targetUnit) { this.targetUnit = targetUnit; }
    
    public int getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }
    
    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    
    public Date getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Date unlockedAt) { this.unlockedAt = unlockedAt; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    
    public int getPointsValue() { return pointsValue; }
    public void setPointsValue(int pointsValue) { this.pointsValue = pointsValue; }
    
    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }
    
    public int getStreakRequirement() { return streakRequirement; }
    public void setStreakRequirement(int streakRequirement) { this.streakRequirement = streakRequirement; }
    
    public String getCustomCondition() { return customCondition; }
    public void setCustomCondition(String customCondition) { this.customCondition = customCondition; }
}