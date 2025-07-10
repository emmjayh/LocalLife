package com.locallife.model;

import java.util.Date;

/**
 * Model for user-defined goals and progress tracking
 */
public class Goal {
    private int id;
    private String title;
    private String description;
    private GoalType type;
    private GoalCategory category;
    private float targetValue;
    private String targetUnit;
    private float currentValue;
    private GoalFrequency frequency;
    private Date startDate;
    private Date endDate;
    private Date createdAt;
    private Date updatedAt;
    private boolean isActive;
    private boolean isCompleted;
    private int streakCount;
    private int totalCompletions;
    private float bestValue;
    private Date lastCompletedDate;
    private String motivationalMessage;
    private int priority; // 1-5 scale
    private String color; // Hex color for UI
    
    public Goal() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.isCompleted = false;
        this.streakCount = 0;
        this.totalCompletions = 0;
        this.currentValue = 0f;
        this.priority = 3;
        this.color = "#4CAF50"; // Default green
    }
    
    public Goal(String title, GoalType type, GoalCategory category, float targetValue, String targetUnit, GoalFrequency frequency) {
        this();
        this.title = title;
        this.type = type;
        this.category = category;
        this.targetValue = targetValue;
        this.targetUnit = targetUnit;
        this.frequency = frequency;
        this.startDate = new Date();
        
        // Set end date based on frequency
        setEndDateBasedOnFrequency();
    }
    
    private void setEndDateBasedOnFrequency() {
        if (startDate == null) return;
        
        long startTime = startDate.getTime();
        long endTime = startTime;
        
        switch (frequency) {
            case DAILY:
                endTime += 24 * 60 * 60 * 1000L; // 1 day
                break;
            case WEEKLY:
                endTime += 7 * 24 * 60 * 60 * 1000L; // 7 days
                break;
            case MONTHLY:
                endTime += 30L * 24 * 60 * 60 * 1000L; // 30 days
                break;
            case YEARLY:
                endTime += 365L * 24 * 60 * 60 * 1000L; // 365 days
                break;
        }
        
        this.endDate = new Date(endTime);
    }
    
    /**
     * Update current progress value
     */
    public void updateProgress(float value) {
        this.currentValue = value;
        this.updatedAt = new Date();
        
        // Update best value if this is better
        if (type == GoalType.MAXIMIZE && value > bestValue) {
            bestValue = value;
        } else if (type == GoalType.MINIMIZE && (bestValue == 0 || value < bestValue)) {
            bestValue = value;
        }
        
        // Check if goal is completed
        checkCompletion();
    }
    
    /**
     * Add incremental progress
     */
    public void addProgress(float increment) {
        updateProgress(currentValue + increment);
    }
    
    /**
     * Check if goal is completed based on current progress
     */
    private void checkCompletion() {
        boolean wasCompleted = isCompleted;
        
        switch (type) {
            case ACHIEVE:
                isCompleted = currentValue >= targetValue;
                break;
            case MAXIMIZE:
                isCompleted = currentValue >= targetValue;
                break;
            case MINIMIZE:
                isCompleted = currentValue <= targetValue;
                break;
            case MAINTAIN:
                // For maintain goals, check if we're within acceptable range
                float tolerance = targetValue * 0.1f; // 10% tolerance
                isCompleted = Math.abs(currentValue - targetValue) <= tolerance;
                break;
        }
        
        // Handle completion
        if (isCompleted && !wasCompleted) {
            onGoalCompleted();
        }
    }
    
    /**
     * Handle goal completion
     */
    private void onGoalCompleted() {
        this.lastCompletedDate = new Date();
        this.totalCompletions++;
        
        // Update streak
        if (isConsecutiveCompletion()) {
            streakCount++;
        } else {
            streakCount = 1;
        }
        
        // Generate motivational message
        generateMotivationalMessage();
    }
    
    /**
     * Check if this is a consecutive completion for streak tracking
     */
    private boolean isConsecutiveCompletion() {
        if (lastCompletedDate == null) return true;
        
        long timeDiff = new Date().getTime() - lastCompletedDate.getTime();
        long expectedInterval = getExpectedIntervalMs();
        
        // Allow some tolerance (up to 2x the expected interval)
        return timeDiff <= expectedInterval * 2;
    }
    
    /**
     * Get expected interval in milliseconds based on frequency
     */
    private long getExpectedIntervalMs() {
        switch (frequency) {
            case DAILY:
                return 24 * 60 * 60 * 1000L;
            case WEEKLY:
                return 7 * 24 * 60 * 60 * 1000L;
            case MONTHLY:
                return 30L * 24 * 60 * 60 * 1000L;
            case YEARLY:
                return 365L * 24 * 60 * 60 * 1000L;
            default:
                return 24 * 60 * 60 * 1000L;
        }
    }
    
    /**
     * Generate motivational message based on progress
     */
    public void generateMotivationalMessage() {
        float progressPercentage = getProgressPercentage();
        
        if (isCompleted) {
            if (streakCount > 1) {
                motivationalMessage = "Amazing! " + streakCount + " day streak! 🔥";
            } else {
                motivationalMessage = "Goal completed! Well done! 🎉";
            }
        } else if (progressPercentage >= 80) {
            motivationalMessage = "So close! You've got this! 💪";
        } else if (progressPercentage >= 50) {
            motivationalMessage = "Halfway there! Keep going! 🚀";
        } else if (progressPercentage >= 25) {
            motivationalMessage = "Great start! Building momentum! ⭐";
        } else {
            motivationalMessage = "Every step counts! Let's do this! 🌟";
        }
    }
    
    /**
     * Calculate progress percentage
     */
    public float getProgressPercentage() {
        if (targetValue == 0) return 0f;
        
        switch (type) {
            case ACHIEVE:
            case MAXIMIZE:
                return Math.min(100f, (currentValue / targetValue) * 100f);
            case MINIMIZE:
                // For minimize goals, progress is inverse
                if (currentValue <= targetValue) return 100f;
                return Math.max(0f, 100f - ((currentValue - targetValue) / targetValue) * 100f);
            case MAINTAIN:
                float tolerance = targetValue * 0.1f;
                float deviation = Math.abs(currentValue - targetValue);
                if (deviation <= tolerance) return 100f;
                return Math.max(0f, 100f - ((deviation - tolerance) / targetValue) * 100f);
            default:
                return 0f;
        }
    }
    
    /**
     * Get remaining time to goal deadline
     */
    public long getRemainingTimeMs() {
        if (endDate == null) return 0;
        return Math.max(0, endDate.getTime() - new Date().getTime());
    }
    
    /**
     * Check if goal is overdue
     */
    public boolean isOverdue() {
        return endDate != null && new Date().after(endDate) && !isCompleted;
    }
    
    /**
     * Get formatted remaining time
     */
    public String getFormattedRemainingTime() {
        long remainingMs = getRemainingTimeMs();
        if (remainingMs == 0) return "Expired";
        
        long days = remainingMs / (24 * 60 * 60 * 1000);
        long hours = (remainingMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        
        if (days > 0) {
            return days + "d " + hours + "h";
        } else {
            return hours + "h";
        }
    }
    
    /**
     * Get formatted progress display
     */
    public String getFormattedProgress() {
        return String.format("%.1f / %.1f %s", currentValue, targetValue, targetUnit);
    }
    
    /**
     * Get status text
     */
    public String getStatusText() {
        if (isCompleted) return "Completed";
        if (isOverdue()) return "Overdue";
        if (!isActive) return "Inactive";
        return "In Progress";
    }
    
    /**
     * Get priority text
     */
    public String getPriorityText() {
        switch (priority) {
            case 5:
                return "Critical";
            case 4:
                return "High";
            case 3:
                return "Medium";
            case 2:
                return "Low";
            case 1:
                return "Very Low";
            default:
                return "Medium";
        }
    }
    
    /**
     * Get formatted streak text
     */
    public String getFormattedStreak() {
        if (streakCount == 0) return "No streak";
        if (streakCount == 1) return "1 day streak";
        return streakCount + " day streak";
    }
    
    /**
     * Reset goal for next cycle (for recurring goals)
     */
    public void resetForNextCycle() {
        this.currentValue = 0f;
        this.isCompleted = false;
        this.updatedAt = new Date();
        
        // Update dates for next cycle
        if (frequency != GoalFrequency.ONCE) {
            this.startDate = new Date();
            setEndDateBasedOnFrequency();
        }
    }
    
    // Enums
    public enum GoalType {
        ACHIEVE,    // Reach a target value
        MAXIMIZE,   // Maximize value (higher is better)
        MINIMIZE,   // Minimize value (lower is better)
        MAINTAIN    // Maintain value within range
    }
    
    public enum GoalCategory {
        FITNESS,
        HEALTH,
        PRODUCTIVITY,
        SOCIAL,
        LEARNING,
        WELLNESS,
        HABIT,
        CUSTOM
    }
    
    public enum GoalFrequency {
        ONCE,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public GoalType getType() { return type; }
    public void setType(GoalType type) { this.type = type; }
    
    public GoalCategory getCategory() { return category; }
    public void setCategory(GoalCategory category) { this.category = category; }
    
    public float getTargetValue() { return targetValue; }
    public void setTargetValue(float targetValue) { this.targetValue = targetValue; }
    
    public String getTargetUnit() { return targetUnit; }
    public void setTargetUnit(String targetUnit) { this.targetUnit = targetUnit; }
    
    public float getCurrentValue() { return currentValue; }
    public void setCurrentValue(float currentValue) { this.currentValue = currentValue; }
    
    public GoalFrequency getFrequency() { return frequency; }
    public void setFrequency(GoalFrequency frequency) { this.frequency = frequency; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }
    
    public int getTotalCompletions() { return totalCompletions; }
    public void setTotalCompletions(int totalCompletions) { this.totalCompletions = totalCompletions; }
    
    public float getBestValue() { return bestValue; }
    public void setBestValue(float bestValue) { this.bestValue = bestValue; }
    
    public Date getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(Date lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }
    
    public String getMotivationalMessage() { return motivationalMessage; }
    public void setMotivationalMessage(String motivationalMessage) { this.motivationalMessage = motivationalMessage; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}