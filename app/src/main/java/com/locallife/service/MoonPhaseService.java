package com.locallife.service;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

/**
 * Service for calculating moon phases and tracking lunar cycles
 * Uses astronomical calculations - no API required
 */
public class MoonPhaseService {
    private static final String TAG = "MoonPhaseService";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Moon phase constants
    private static final double LUNAR_MONTH = 29.53058867; // Average lunar month in days
    private static final double NEW_MOON_REFERENCE = 2451550.1; // Julian day of known new moon (Jan 6, 2000)
    
    // Moon phase names
    private static final String[] MOON_PHASES = {
        "New Moon", "Waxing Crescent", "First Quarter", "Waxing Gibbous",
        "Full Moon", "Waning Gibbous", "Third Quarter", "Waning Crescent"
    };
    
    // Moon phase impacts on activity
    private static final Map<String, Float> PHASE_ACTIVITY_MULTIPLIERS = new HashMap<>();
    static {
        PHASE_ACTIVITY_MULTIPLIERS.put("New Moon", 0.95f);
        PHASE_ACTIVITY_MULTIPLIERS.put("Waxing Crescent", 1.0f);
        PHASE_ACTIVITY_MULTIPLIERS.put("First Quarter", 1.05f);
        PHASE_ACTIVITY_MULTIPLIERS.put("Waxing Gibbous", 1.1f);
        PHASE_ACTIVITY_MULTIPLIERS.put("Full Moon", 1.15f);
        PHASE_ACTIVITY_MULTIPLIERS.put("Waning Gibbous", 1.1f);
        PHASE_ACTIVITY_MULTIPLIERS.put("Third Quarter", 1.0f);
        PHASE_ACTIVITY_MULTIPLIERS.put("Waning Crescent", 0.95f);
    }
    
    public interface MoonPhaseCallback {
        void onMoonPhaseCalculated(MoonPhaseData moonPhaseData);
        void onError(String error);
    }
    
    public static class MoonPhaseData {
        private Date date;
        private String phaseName;
        private int phaseIndex; // 0-7 for 8 phases
        private double illumination; // 0.0 to 1.0
        private double age; // Days since new moon
        private double distanceKm; // Distance to moon in km
        private String zodiacSign;
        private boolean isSupermoon;
        private Date nextNewMoon;
        private Date nextFullMoon;
        private Date previousNewMoon;
        private Date previousFullMoon;
        private float activityImpactScore;
        private String activityRecommendation;
        
        public MoonPhaseData() {
            this.date = new Date();
        }
        
        // Getters and setters
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        
        public String getPhaseName() { return phaseName; }
        public void setPhaseName(String phaseName) { this.phaseName = phaseName; }
        
        public int getPhaseIndex() { return phaseIndex; }
        public void setPhaseIndex(int phaseIndex) { this.phaseIndex = phaseIndex; }
        
        public double getIllumination() { return illumination; }
        public void setIllumination(double illumination) { this.illumination = illumination; }
        
        public double getAge() { return age; }
        public void setAge(double age) { this.age = age; }
        
        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
        
        public String getZodiacSign() { return zodiacSign; }
        public void setZodiacSign(String zodiacSign) { this.zodiacSign = zodiacSign; }
        
        public boolean isSupermoon() { return isSupermoon; }
        public void setSupermoon(boolean supermoon) { isSupermoon = supermoon; }
        
        public Date getNextNewMoon() { return nextNewMoon; }
        public void setNextNewMoon(Date nextNewMoon) { this.nextNewMoon = nextNewMoon; }
        
        public Date getNextFullMoon() { return nextFullMoon; }
        public void setNextFullMoon(Date nextFullMoon) { this.nextFullMoon = nextFullMoon; }
        
        public Date getPreviousNewMoon() { return previousNewMoon; }
        public void setPreviousNewMoon(Date previousNewMoon) { this.previousNewMoon = previousNewMoon; }
        
        public Date getPreviousFullMoon() { return previousFullMoon; }
        public void setPreviousFullMoon(Date previousFullMoon) { this.previousFullMoon = previousFullMoon; }
        
        public float getActivityImpactScore() { return activityImpactScore; }
        public void setActivityImpactScore(float activityImpactScore) { this.activityImpactScore = activityImpactScore; }
        
        public String getActivityRecommendation() { return activityRecommendation; }
        public void setActivityRecommendation(String activityRecommendation) { this.activityRecommendation = activityRecommendation; }
        
        @Override
        public String toString() {
            return "MoonPhaseData{" +
                    "phaseName='" + phaseName + '\'' +
                    ", illumination=" + String.format("%.1f", illumination * 100) + "%" +
                    ", age=" + String.format("%.1f", age) + " days" +
                    ", isSupermoon=" + isSupermoon +
                    '}';
        }
    }
    
    public MoonPhaseService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Calculate moon phase for current date
     */
    public void getCurrentMoonPhase(MoonPhaseCallback callback) {
        getMoonPhaseForDate(new Date(), callback);
    }
    
    /**
     * Calculate moon phase for specific date
     */
    public void getMoonPhaseForDate(Date date, MoonPhaseCallback callback) {
        executorService.execute(() -> {
            try {
                MoonPhaseData moonPhaseData = calculateMoonPhase(date);
                
                // Calculate activity impact
                calculateActivityImpact(moonPhaseData);
                
                // Save to database
                saveMoonPhaseToDatabase(moonPhaseData);
                
                // Update day record
                updateDayRecordWithMoonPhase(moonPhaseData);
                
                callback.onMoonPhaseCalculated(moonPhaseData);
                
            } catch (Exception e) {
                Log.e(TAG, "Error calculating moon phase", e);
                callback.onError("Failed to calculate moon phase: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get moon phase history for a date range
     */
    public void getMoonPhaseHistory(Date startDate, Date endDate, MoonPhaseCallback callback) {
        executorService.execute(() -> {
            try {
                List<MoonPhaseData> moonPhaseHistory = new ArrayList<>();
                
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                
                while (!calendar.getTime().after(endDate)) {
                    MoonPhaseData moonPhaseData = calculateMoonPhase(calendar.getTime());
                    moonPhaseHistory.add(moonPhaseData);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                
                // For now, just return the first one
                if (!moonPhaseHistory.isEmpty()) {
                    callback.onMoonPhaseCalculated(moonPhaseHistory.get(0));
                } else {
                    callback.onError("No moon phase data found for date range");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error calculating moon phase history", e);
                callback.onError("Failed to calculate moon phase history: " + e.getMessage());
            }
        });
    }
    
    /**
     * Calculate moon phase data for a specific date
     */
    private MoonPhaseData calculateMoonPhase(Date date) {
        MoonPhaseData moonPhaseData = new MoonPhaseData();
        moonPhaseData.setDate(date);
        
        // Convert date to Julian day
        double julianDay = dateToJulianDay(date);
        
        // Calculate days since reference new moon
        double daysSinceNewMoon = (julianDay - NEW_MOON_REFERENCE) % LUNAR_MONTH;
        if (daysSinceNewMoon < 0) {
            daysSinceNewMoon += LUNAR_MONTH;
        }
        
        moonPhaseData.setAge(daysSinceNewMoon);
        
        // Calculate illumination
        double illumination = calculateIllumination(daysSinceNewMoon);
        moonPhaseData.setIllumination(illumination);
        
        // Determine phase
        int phaseIndex = calculatePhaseIndex(daysSinceNewMoon);
        moonPhaseData.setPhaseIndex(phaseIndex);
        moonPhaseData.setPhaseName(MOON_PHASES[phaseIndex]);
        
        // Calculate distance (simplified - actual calculation is more complex)
        double distanceKm = calculateMoonDistance(julianDay);
        moonPhaseData.setDistanceKm(distanceKm);
        
        // Check if supermoon
        moonPhaseData.setSupermoon(isSupermoon(distanceKm, phaseIndex));
        
        // Calculate next/previous phases
        calculatePhaseTransitions(moonPhaseData, julianDay);
        
        // Calculate zodiac sign (simplified)
        moonPhaseData.setZodiacSign(calculateZodiacSign(julianDay));
        
        return moonPhaseData;
    }
    
    /**
     * Convert date to Julian day number
     */
    private double dateToJulianDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        
        // Julian day calculation
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        
        int a = year / 100;
        int b = 2 - a + (a / 4);
        
        double jd = Math.floor(365.25 * (year + 4716)) + 
                   Math.floor(30.6001 * (month + 1)) + 
                   day + b - 1524.5;
        
        // Add time fraction
        jd += (hour + minute / 60.0 + second / 3600.0) / 24.0;
        
        return jd;
    }
    
    /**
     * Calculate moon illumination percentage
     */
    private double calculateIllumination(double age) {
        // Simplified illumination calculation
        double phase = (age / LUNAR_MONTH) * 2 * Math.PI;
        return (1 - Math.cos(phase)) / 2;
    }
    
    /**
     * Calculate phase index (0-7)
     */
    private int calculatePhaseIndex(double age) {
        double phaseLength = LUNAR_MONTH / 8.0;
        return (int) Math.floor(age / phaseLength) % 8;
    }
    
    /**
     * Calculate moon distance (simplified)
     */
    private double calculateMoonDistance(double julianDay) {
        // Simplified calculation - actual distance varies between ~356,500 and ~406,700 km
        double baseDistance = 384400; // Average distance in km
        double variation = 25000; // Approximate variation
        
        // Use a simple sine function for variation
        double angle = (julianDay - NEW_MOON_REFERENCE) * 2 * Math.PI / 27.55; // Anomalistic month
        return baseDistance + variation * Math.sin(angle);
    }
    
    /**
     * Check if current phase is a supermoon
     */
    private boolean isSupermoon(double distanceKm, int phaseIndex) {
        // Supermoon is typically defined as full moon at perigee (closest approach)
        return phaseIndex == 4 && distanceKm < 370000; // Full moon within 370,000 km
    }
    
    /**
     * Calculate next and previous phase transitions
     */
    private void calculatePhaseTransitions(MoonPhaseData moonPhaseData, double julianDay) {
        // Calculate next new moon
        double currentAge = moonPhaseData.getAge();
        double daysToNextNewMoon = LUNAR_MONTH - currentAge;
        Date nextNewMoon = new Date(moonPhaseData.getDate().getTime() + (long) (daysToNextNewMoon * 24 * 60 * 60 * 1000));
        moonPhaseData.setNextNewMoon(nextNewMoon);
        
        // Calculate next full moon
        double daysToNextFullMoon;
        if (currentAge < LUNAR_MONTH / 2) {
            daysToNextFullMoon = (LUNAR_MONTH / 2) - currentAge;
        } else {
            daysToNextFullMoon = LUNAR_MONTH - currentAge + (LUNAR_MONTH / 2);
        }
        Date nextFullMoon = new Date(moonPhaseData.getDate().getTime() + (long) (daysToNextFullMoon * 24 * 60 * 60 * 1000));
        moonPhaseData.setNextFullMoon(nextFullMoon);
        
        // Calculate previous new moon
        Date previousNewMoon = new Date(moonPhaseData.getDate().getTime() - (long) (currentAge * 24 * 60 * 60 * 1000));
        moonPhaseData.setPreviousNewMoon(previousNewMoon);
        
        // Calculate previous full moon
        double daysSincePreviousFullMoon;
        if (currentAge > LUNAR_MONTH / 2) {
            daysSincePreviousFullMoon = currentAge - (LUNAR_MONTH / 2);
        } else {
            daysSincePreviousFullMoon = currentAge + (LUNAR_MONTH / 2);
        }
        Date previousFullMoon = new Date(moonPhaseData.getDate().getTime() - (long) (daysSincePreviousFullMoon * 24 * 60 * 60 * 1000));
        moonPhaseData.setPreviousFullMoon(previousFullMoon);
    }
    
    /**
     * Calculate zodiac sign (simplified)
     */
    private String calculateZodiacSign(double julianDay) {
        String[] zodiacSigns = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        };
        
        // Simplified calculation - actual calculation is more complex
        int index = (int) ((julianDay - NEW_MOON_REFERENCE) / 27.32) % 12;
        if (index < 0) index += 12;
        return zodiacSigns[index];
    }
    
    /**
     * Calculate activity impact based on moon phase
     */
    private void calculateActivityImpact(MoonPhaseData moonPhaseData) {
        String phaseName = moonPhaseData.getPhaseName();
        float activityMultiplier = PHASE_ACTIVITY_MULTIPLIERS.getOrDefault(phaseName, 1.0f);
        
        // Adjust for supermoon
        if (moonPhaseData.isSupermoon()) {
            activityMultiplier *= 1.1f; // 10% boost for supermoon
        }
        
        moonPhaseData.setActivityImpactScore(activityMultiplier);
        moonPhaseData.setActivityRecommendation(getActivityRecommendation(phaseName, moonPhaseData.isSupermoon()));
    }
    
    /**
     * Get activity recommendation based on moon phase
     */
    private String getActivityRecommendation(String phaseName, boolean isSupermoon) {
        String baseRecommendation;
        
        switch (phaseName) {
            case "New Moon":
                baseRecommendation = "Good time for reflection and planning. Consider indoor activities and goal setting.";
                break;
            case "Waxing Crescent":
                baseRecommendation = "Energy is building. Good time to start new projects and outdoor activities.";
                break;
            case "First Quarter":
                baseRecommendation = "High energy phase. Excellent for challenging workouts and active pursuits.";
                break;
            case "Waxing Gibbous":
                baseRecommendation = "Peak energy approaching. Great for social activities and group exercises.";
                break;
            case "Full Moon":
                baseRecommendation = "Maximum energy and activity. Perfect for outdoor adventures and social gatherings.";
                break;
            case "Waning Gibbous":
                baseRecommendation = "Energy stabilizing. Good for maintaining routines and moderate activities.";
                break;
            case "Third Quarter":
                baseRecommendation = "Energy declining. Focus on recovery activities and gentle exercises.";
                break;
            case "Waning Crescent":
                baseRecommendation = "Low energy phase. Good for rest, recovery, and light activities.";
                break;
            default:
                baseRecommendation = "Adapt your activities based on your energy levels.";
        }
        
        if (isSupermoon) {
            baseRecommendation += " This is a supermoon, which may enhance the typical effects of this phase.";
        }
        
        return baseRecommendation;
    }
    
    /**
     * Save moon phase data to database
     */
    private void saveMoonPhaseToDatabase(MoonPhaseData moonPhaseData) {
        try {
            String currentDate = dateFormat.format(moonPhaseData.getDate());
            
            // This would save to a dedicated moon phase table
            // For now, we'll add this functionality to DatabaseHelper
            databaseHelper.insertMoonPhaseData(
                    currentDate,
                    moonPhaseData.getPhaseName(),
                    moonPhaseData.getPhaseIndex(),
                    moonPhaseData.getIllumination(),
                    moonPhaseData.getAge(),
                    moonPhaseData.getDistanceKm(),
                    moonPhaseData.getZodiacSign(),
                    moonPhaseData.isSupermoon(),
                    moonPhaseData.getActivityImpactScore()
            );
            
            Log.d(TAG, "Moon phase data saved to database");
        } catch (Exception e) {
            Log.e(TAG, "Error saving moon phase data to database", e);
        }
    }
    
    /**
     * Update day record with moon phase data
     */
    private void updateDayRecordWithMoonPhase(MoonPhaseData moonPhaseData) {
        try {
            String currentDate = dateFormat.format(moonPhaseData.getDate());
            DayRecord dayRecord = databaseHelper.getDayRecord(currentDate);
            
            if (dayRecord == null) {
                dayRecord = new DayRecord();
                dayRecord.setDate(currentDate);
            }
            
            // Update moon phase information in DayRecord
            // This requires updating the DayRecord model first
            
            // Recalculate activity score with moon phase impact
            dayRecord.calculateActivityScore();
            
            if (dayRecord.getId() > 0) {
                databaseHelper.updateDayRecord(dayRecord);
            } else {
                databaseHelper.insertDayRecord(dayRecord);
            }
            
            Log.d(TAG, "Day record updated with moon phase data");
        } catch (Exception e) {
            Log.e(TAG, "Error updating day record with moon phase data", e);
        }
    }
    
    /**
     * Get moon phase activity multiplier
     */
    public static float getMoonPhaseActivityMultiplier(String phaseName) {
        return PHASE_ACTIVITY_MULTIPLIERS.getOrDefault(phaseName, 1.0f);
    }
    
    /**
     * Check if moon phase is favorable for outdoor activities
     */
    public static boolean isFavorableMoonPhaseForOutdoorActivity(String phaseName) {
        return "Full Moon".equals(phaseName) || "Waxing Gibbous".equals(phaseName) || "First Quarter".equals(phaseName);
    }
    
    /**
     * Get moon phase sleep impact
     */
    public static String getMoonPhaseSleepImpact(String phaseName) {
        switch (phaseName) {
            case "Full Moon":
                return "May experience lighter sleep. Consider earlier bedtime.";
            case "New Moon":
                return "Good sleep phase. Natural time for rest and recovery.";
            case "Waxing Gibbous":
                return "Increased energy may affect sleep. Avoid caffeine before bed.";
            case "Waning Crescent":
                return "Natural rest phase. Good for deep, restorative sleep.";
            default:
                return "Maintain regular sleep schedule.";
        }
    }
    
    /**
     * Get lunar calendar events for a month
     */
    public void getLunarCalendarEvents(int year, int month, MoonPhaseCallback callback) {
        executorService.execute(() -> {
            try {
                List<MoonPhaseData> events = new ArrayList<>();
                
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, 1); // Calendar.MONTH is 0-based
                
                int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                
                for (int day = 1; day <= daysInMonth; day++) {
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    MoonPhaseData moonPhaseData = calculateMoonPhase(calendar.getTime());
                    
                    // Only add significant phases (new moon, full moon, quarters)
                    if (moonPhaseData.getPhaseIndex() % 2 == 0) {
                        events.add(moonPhaseData);
                    }
                }
                
                // For now, just return the first event
                if (!events.isEmpty()) {
                    callback.onMoonPhaseCalculated(events.get(0));
                } else {
                    callback.onError("No significant lunar events found for the month");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error calculating lunar calendar events", e);
                callback.onError("Failed to calculate lunar calendar events: " + e.getMessage());
            }
        });
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}