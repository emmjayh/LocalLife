package com.locallife.model;

import java.util.Date;

/**
 * Moon phase data model containing lunar information
 */
public class MoonPhase {
    private long id;
    private String date;
    private Date timestamp;
    private String moonPhase;
    private int moonPhaseIndex;
    private float moonIllumination;
    private float moonAge;
    private float moonDistance;
    private String zodiacSign;
    private boolean isSupermoon;
    private float moonActivityImpact;
    
    public MoonPhase() {
        this.timestamp = new Date();
    }
    
    public MoonPhase(String date, String moonPhase, int moonPhaseIndex, float moonIllumination, float moonAge) {
        this();
        this.date = date;
        this.moonPhase = moonPhase;
        this.moonPhaseIndex = moonPhaseIndex;
        this.moonIllumination = moonIllumination;
        this.moonAge = moonAge;
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    
    public String getMoonPhase() { return moonPhase; }
    public void setMoonPhase(String moonPhase) { this.moonPhase = moonPhase; }
    
    public int getMoonPhaseIndex() { return moonPhaseIndex; }
    public void setMoonPhaseIndex(int moonPhaseIndex) { this.moonPhaseIndex = moonPhaseIndex; }
    
    public float getMoonIllumination() { return moonIllumination; }
    public void setMoonIllumination(float moonIllumination) { this.moonIllumination = moonIllumination; }
    
    public float getMoonAge() { return moonAge; }
    public void setMoonAge(float moonAge) { this.moonAge = moonAge; }
    
    public float getMoonDistance() { return moonDistance; }
    public void setMoonDistance(float moonDistance) { this.moonDistance = moonDistance; }
    
    public String getZodiacSign() { return zodiacSign; }
    public void setZodiacSign(String zodiacSign) { this.zodiacSign = zodiacSign; }
    
    public boolean isSupermoon() { return isSupermoon; }
    public void setSupermoon(boolean supermoon) { this.isSupermoon = supermoon; }
    
    public float getMoonActivityImpact() { return moonActivityImpact; }
    public void setMoonActivityImpact(float moonActivityImpact) { this.moonActivityImpact = moonActivityImpact; }
    
    /**
     * Get moon phase emoji representation
     */
    public String getMoonEmoji() {
        switch (moonPhase) {
            case "New Moon": return "🌑";
            case "Waxing Crescent": return "🌒";
            case "First Quarter": return "🌓";
            case "Waxing Gibbous": return "🌔";
            case "Full Moon": return "🌕";
            case "Waning Gibbous": return "🌖";
            case "Last Quarter": return "🌗";
            case "Waning Crescent": return "🌘";
            default: return "🌙";
        }
    }
    
    /**
     * Get moon phase description
     */
    public String getPhaseDescription() {
        switch (moonPhase) {
            case "New Moon": 
                return "New beginnings, setting intentions, introspection";
            case "Waxing Crescent": 
                return "Taking action, building momentum, growth";
            case "First Quarter": 
                return "Overcoming challenges, making decisions, persistence";
            case "Waxing Gibbous": 
                return "Refinement, adjustment, preparation";
            case "Full Moon": 
                return "Completion, manifestation, high energy";
            case "Waning Gibbous": 
                return "Gratitude, sharing, teaching";
            case "Last Quarter": 
                return "Release, forgiveness, letting go";
            case "Waning Crescent": 
                return "Rest, reflection, preparation for new cycle";
            default: 
                return "Lunar energy influences daily rhythms";
        }
    }
    
    /**
     * Get activity recommendations based on moon phase
     */
    public String getActivityRecommendation() {
        switch (moonPhase) {
            case "New Moon": 
                return "Good time for planning, meditation, and setting new goals";
            case "Waxing Crescent": 
                return "Great for starting new projects and taking initial steps";
            case "First Quarter": 
                return "Focus on overcoming obstacles and making important decisions";
            case "Waxing Gibbous": 
                return "Perfect for refining plans and making adjustments";
            case "Full Moon": 
                return "High energy time - great for completing projects and social activities";
            case "Waning Gibbous": 
                return "Good for sharing knowledge and helping others";
            case "Last Quarter": 
                return "Time for releasing what no longer serves you";
            case "Waning Crescent": 
                return "Focus on rest, reflection, and preparing for the next cycle";
            default: 
                return "Follow your natural rhythms and lunar influences";
        }
    }
    
    /**
     * Calculate moon phase from age
     */
    public static String calculatePhaseFromAge(float age) {
        if (age < 1.84566) return "New Moon";
        if (age < 5.53699) return "Waxing Crescent";
        if (age < 9.22831) return "First Quarter";
        if (age < 12.91963) return "Waxing Gibbous";
        if (age < 16.61096) return "Full Moon";
        if (age < 20.30228) return "Waning Gibbous";
        if (age < 23.99361) return "Last Quarter";
        if (age < 27.68493) return "Waning Crescent";
        return "New Moon";
    }
}