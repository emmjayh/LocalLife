package com.locallife.model;

import java.util.Date;

/**
 * Model for user level progression and experience points
 */
public class UserLevel {
    private int id;
    private int currentLevel;
    private int currentXP;
    private int totalXP;
    private Date lastLevelUp;
    private Date createdAt;
    private Date updatedAt;
    private String currentTitle;
    private int achievementsUnlocked;
    private int streaksCompleted;
    private int goalsCompleted;
    
    // Level progression constants
    private static final int BASE_XP_REQUIREMENT = 100;
    private static final double LEVEL_MULTIPLIER = 1.5;
    private static final int MAX_LEVEL = 100;
    
    public UserLevel() {
        this.currentLevel = 1;
        this.currentXP = 0;
        this.totalXP = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.currentTitle = "Beginner";
        this.achievementsUnlocked = 0;
        this.streaksCompleted = 0;
        this.goalsCompleted = 0;
    }
    
    /**
     * Add experience points and check for level up
     */
    public boolean addXP(int xp) {
        if (xp <= 0) return false;
        
        this.currentXP += xp;
        this.totalXP += xp;
        this.updatedAt = new Date();
        
        return checkLevelUp();
    }
    
    /**
     * Check if user should level up and process level up
     */
    private boolean checkLevelUp() {
        int requiredXP = getXPRequiredForLevel(currentLevel + 1);
        
        if (currentXP >= requiredXP && currentLevel < MAX_LEVEL) {
            levelUp();
            return true;
        }
        
        return false;
    }
    
    /**
     * Process level up
     */
    private void levelUp() {
        int requiredXP = getXPRequiredForLevel(currentLevel + 1);
        this.currentXP -= requiredXP;
        this.currentLevel++;
        this.lastLevelUp = new Date();
        this.currentTitle = getTitleForLevel(currentLevel);
    }
    
    /**
     * Calculate XP required for a specific level
     */
    public static int getXPRequiredForLevel(int level) {
        if (level <= 1) return 0;
        return (int) (BASE_XP_REQUIREMENT * Math.pow(LEVEL_MULTIPLIER, level - 2));
    }
    
    /**
     * Get XP required for next level
     */
    public int getXPRequiredForNextLevel() {
        if (currentLevel >= MAX_LEVEL) return 0;
        return getXPRequiredForLevel(currentLevel + 1);
    }
    
    /**
     * Get progress percentage to next level
     */
    public float getProgressToNextLevel() {
        if (currentLevel >= MAX_LEVEL) return 100f;
        
        int requiredXP = getXPRequiredForNextLevel();
        if (requiredXP == 0) return 100f;
        
        return Math.min(100f, ((float) currentXP / requiredXP) * 100f);
    }
    
    /**
     * Get title based on level
     */
    public static String getTitleForLevel(int level) {
        if (level >= 90) return "Grandmaster";
        if (level >= 80) return "Legend";
        if (level >= 70) return "Champion";
        if (level >= 60) return "Expert";
        if (level >= 50) return "Master";
        if (level >= 40) return "Veteran";
        if (level >= 30) return "Professional";
        if (level >= 25) return "Advanced";
        if (level >= 20) return "Skilled";
        if (level >= 15) return "Experienced";
        if (level >= 10) return "Intermediate";
        if (level >= 5) return "Novice";
        return "Beginner";
    }
    
    /**
     * Get rank based on level for display
     */
    public String getRank() {
        if (currentLevel >= 90) return "S+";
        if (currentLevel >= 80) return "S";
        if (currentLevel >= 70) return "A+";
        if (currentLevel >= 60) return "A";
        if (currentLevel >= 50) return "B+";
        if (currentLevel >= 40) return "B";
        if (currentLevel >= 30) return "C+";
        if (currentLevel >= 20) return "C";
        if (currentLevel >= 10) return "D";
        return "E";
    }
    
    /**
     * Get level color for UI display
     */
    public String getLevelColor() {
        if (currentLevel >= 90) return "#FF6B35"; // Legendary Orange
        if (currentLevel >= 80) return "#B9F2FF"; // Diamond Blue
        if (currentLevel >= 70) return "#E5E4E2"; // Platinum
        if (currentLevel >= 60) return "#FFD700"; // Gold
        if (currentLevel >= 50) return "#C0C0C0"; // Silver
        if (currentLevel >= 40) return "#CD7F32"; // Bronze
        if (currentLevel >= 30) return "#9C27B0"; // Purple
        if (currentLevel >= 20) return "#2196F3"; // Blue
        if (currentLevel >= 10) return "#4CAF50"; // Green
        return "#FF9800"; // Orange for beginners
    }
    
    /**
     * Get formatted level display text
     */
    public String getFormattedLevel() {
        return "Level " + currentLevel + " " + currentTitle;
    }
    
    /**
     * Get formatted XP display text
     */
    public String getFormattedXP() {
        if (currentLevel >= MAX_LEVEL) {
            return "Max Level - " + totalXP + " Total XP";
        }
        return currentXP + " / " + getXPRequiredForNextLevel() + " XP";
    }
    
    /**
     * Get overall statistics summary
     */
    public String getStatsSummary() {
        return String.format(
            "Level %d • %d Achievements • %d Goals • %,d Total XP",
            currentLevel, achievementsUnlocked, goalsCompleted, totalXP
        );
    }
    
    /**
     * Award XP for different activities
     */
    public static int getXPForActivity(ActivityType activity) {
        switch (activity) {
            case GOAL_COMPLETED: return 50;
            case ACHIEVEMENT_UNLOCKED: return 100;
            case STREAK_MILESTONE: return 75;
            case DAILY_LOGIN: return 5;
            case PHOTO_TAKEN: return 2;
            case PLACE_VISITED: return 10;
            case STEP_MILESTONE: return 25;
            case SOCIAL_INTERACTION: return 15;
            case FEATURE_DISCOVERED: return 20;
            case PERFECT_DAY: return 100; // All goals completed
            default: return 10;
        }
    }
    
    /**
     * Update statistics
     */
    public void updateStats(int achievements, int streaks, int goals) {
        this.achievementsUnlocked = achievements;
        this.streaksCompleted = streaks;
        this.goalsCompleted = goals;
        this.updatedAt = new Date();
    }
    
    // Enums
    public enum ActivityType {
        GOAL_COMPLETED,
        ACHIEVEMENT_UNLOCKED,
        STREAK_MILESTONE,
        DAILY_LOGIN,
        PHOTO_TAKEN,
        PLACE_VISITED,
        STEP_MILESTONE,
        SOCIAL_INTERACTION,
        FEATURE_DISCOVERED,
        PERFECT_DAY
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    
    public int getCurrentXP() { return currentXP; }
    public void setCurrentXP(int currentXP) { this.currentXP = currentXP; }
    
    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
    
    public Date getLastLevelUp() { return lastLevelUp; }
    public void setLastLevelUp(Date lastLevelUp) { this.lastLevelUp = lastLevelUp; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCurrentTitle() { return currentTitle; }
    public void setCurrentTitle(String currentTitle) { this.currentTitle = currentTitle; }
    
    public int getAchievementsUnlocked() { return achievementsUnlocked; }
    public void setAchievementsUnlocked(int achievementsUnlocked) { this.achievementsUnlocked = achievementsUnlocked; }
    
    public int getStreaksCompleted() { return streaksCompleted; }
    public void setStreaksCompleted(int streaksCompleted) { this.streaksCompleted = streaksCompleted; }
    
    public int getGoalsCompleted() { return goalsCompleted; }
    public void setGoalsCompleted(int goalsCompleted) { this.goalsCompleted = goalsCompleted; }
}