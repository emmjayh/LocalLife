package com.locallife.service;

import android.content.Context;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;
import com.locallife.model.MoodEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Service for generating contextually relevant notification content
 * based on user patterns, weather, mood, and activities
 */
public class ContextualNotificationGenerator {
    private static final String TAG = "ContextualNotificationGen";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Random random = new Random();
    
    // Content templates and personalization
    private Map<String, List<String>> motivationalQuotes;
    private Map<String, List<String>> weatherBasedContent;
    private Map<String, List<String>> moodBasedContent;
    private Map<String, List<String>> activitySuggestions;
    
    public ContextualNotificationGenerator(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        initializeContentTemplates();
    }
    
    /**
     * Generate contextual notification content
     */
    public NotificationContent generateContent(NotificationType type, String baseContent, 
                                             Map<String, Object> context, UserPattern userPattern) {
        try {
            NotificationContent content = new NotificationContent();
            content.type = type;
            content.baseContent = baseContent;
            
            // Get current context data
            String today = dateFormat.format(Calendar.getInstance().getTime());
            DayRecord todayRecord = databaseHelper.getDayRecord(today);
            MoodEntry recentMood = getRecentMoodEntry();
            
            // Generate personalized content based on type
            switch (type) {
                case ACTIVITY_REMINDER:
                    content = generateActivityReminderContent(context, todayRecord, userPattern);
                    break;
                    
                case MOOD_CHECK:
                    content = generateMoodCheckContent(context, recentMood, todayRecord);
                    break;
                    
                case GOAL_PROGRESS:
                    content = generateGoalProgressContent(context, todayRecord, userPattern);
                    break;
                    
                case WEATHER_ALERT:
                    content = generateWeatherAlertContent(context, todayRecord);
                    break;
                    
                case MOTIVATION_QUOTE:
                    content = generateMotivationalContent(context, recentMood, userPattern);
                    break;
                    
                case HEALTH_TIP:
                    content = generateHealthTipContent(context, todayRecord, recentMood);
                    break;
                    
                case INSIGHT_AVAILABLE:
                    content = generateInsightContent(context, userPattern);
                    break;
                    
                case ACHIEVEMENT_EARNED:
                    content = generateAchievementContent(context, todayRecord);
                    break;
                    
                default:
                    content.title = "LocalLife";
                    content.message = baseContent != null ? baseContent : "Check your activity progress";
                    break;
            }
            
            // Add personalization and context
            personalizeContent(content, context, userPattern, todayRecord, recentMood);
            
            return content;
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating notification content", e);
            return createFallbackContent(type, baseContent);
        }
    }
    
    /**
     * Generate activity reminder content
     */
    private NotificationContent generateActivityReminderContent(Map<String, Object> context, 
                                                              DayRecord todayRecord, 
                                                              UserPattern userPattern) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.ACTIVITY_REMINDER;
        
        // Get current hour and determine appropriate activity
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        // Check weather conditions for outdoor activity suggestions
        boolean isGoodWeather = todayRecord != null && isGoodWeatherForOutdoor(todayRecord.getWeatherCondition());
        
        if (hour >= 6 && hour <= 10) {
            // Morning activities
            if (isGoodWeather) {
                content.title = "Good morning! ☀️";
                content.message = getRandomFromList(activitySuggestions.get("morning_outdoor"));
            } else {
                content.title = "Rise and shine! ✨";
                content.message = getRandomFromList(activitySuggestions.get("morning_indoor"));
            }
        } else if (hour >= 17 && hour <= 20) {
            // Evening activities
            if (isGoodWeather) {
                content.title = "Perfect evening for activity! 🌅";
                content.message = getRandomFromList(activitySuggestions.get("evening_outdoor"));
            } else {
                content.title = "Evening wind-down time 🏠";
                content.message = getRandomFromList(activitySuggestions.get("evening_indoor"));
            }
        } else {
            // General activity reminder
            content.title = "Time to move! 🚶‍♂️";
            content.message = "Take a break and do some light activity";
        }
        
        // Add step goal context if available
        if (todayRecord != null && todayRecord.getSteps() > 0) {
            int stepsRemaining = Math.max(0, 10000 - todayRecord.getSteps());
            if (stepsRemaining > 0) {
                content.message += String.format(" You're %d steps away from your daily goal!", stepsRemaining);
            } else {
                content.message += " You've already hit your step goal today! 🎉";
            }
        }
        
        return content;
    }
    
    /**
     * Generate mood check content
     */
    private NotificationContent generateMoodCheckContent(Map<String, Object> context, 
                                                       MoodEntry recentMood, 
                                                       DayRecord todayRecord) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.MOOD_CHECK;
        
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 6 && hour <= 11) {
            content.title = "Morning check-in ☀️";
            content.message = "How are you feeling this morning? Let's track your mood!";
        } else if (hour >= 12 && hour <= 17) {
            content.title = "Afternoon reflection 🌤️";
            content.message = "How's your day going so far? Take a moment to check in with yourself.";
        } else if (hour >= 18 && hour <= 22) {
            content.title = "Evening wind-down 🌙";
            content.message = "As your day winds down, how are you feeling? Share your mood with us.";
        } else {
            content.title = "Quick mood check 💭";
            content.message = "Take a moment to reflect on how you're feeling right now.";
        }
        
        // Add context based on recent mood
        if (recentMood != null) {
            if (recentMood.isPositiveMood()) {
                content.message += " Keep up that positive energy!";
            } else if (recentMood.isNegativeMood()) {
                content.message += " Remember, every feeling is valid and temporary.";
            }
        }
        
        // Add weather context
        if (todayRecord != null && todayRecord.getWeatherCondition() != null) {
            String weather = todayRecord.getWeatherCondition().toLowerCase();
            if (weather.contains("rain")) {
                content.message += " The rain might be affecting how you feel today.";
            } else if (weather.contains("sunny") || weather.contains("clear")) {
                content.message += " This beautiful weather might be lifting your spirits!";
            }
        }
        
        return content;
    }
    
    /**
     * Generate goal progress content
     */
    private NotificationContent generateGoalProgressContent(Map<String, Object> context, 
                                                          DayRecord todayRecord, 
                                                          UserPattern userPattern) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.GOAL_PROGRESS;
        
        if (todayRecord != null) {
            int steps = todayRecord.getSteps();
            int goalSteps = 10000; // Default goal
            
            float progress = (float) steps / goalSteps * 100;
            
            if (progress >= 100) {
                content.title = "Goal Crushed! 🎉";
                content.message = String.format("Amazing! You've completed %d steps today! That's %d%% of your goal!", 
                    steps, (int) progress);
            } else if (progress >= 75) {
                content.title = "Almost There! 💪";
                content.message = String.format("You're so close! %d steps down, just %d to go!", 
                    steps, goalSteps - steps);
            } else if (progress >= 50) {
                content.title = "Halfway Point! 🚶‍♂️";
                content.message = String.format("Great progress! You've completed %d steps. Keep it up!", steps);
            } else if (progress >= 25) {
                content.title = "Building Momentum 📈";
                content.message = String.format("Good start with %d steps! Every step counts towards your goal.", steps);
            } else {
                content.title = "Let's Get Moving! ⭐";
                content.message = "Your journey to 10,000 steps starts with the first one. Ready to begin?";
            }
        } else {
            content.title = "Goal Check-in 📊";
            content.message = "How are you progressing towards your daily goals? Let's see your stats!";
        }
        
        return content;
    }
    
    /**
     * Generate weather alert content
     */
    private NotificationContent generateWeatherAlertContent(Map<String, Object> context, 
                                                          DayRecord todayRecord) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.WEATHER_ALERT;
        
        if (todayRecord != null) {
            String weather = todayRecord.getWeatherCondition();
            float temperature = todayRecord.getTemperature();
            
            if (weather != null) {
                String weatherLower = weather.toLowerCase();
                
                if (weatherLower.contains("rain")) {
                    content.title = "Rain Alert! ☔";
                    content.message = "It's raining today. Perfect time for indoor activities or cozy reading!";
                } else if (weatherLower.contains("snow")) {
                    content.title = "Snow Day! ❄️";
                    content.message = "Snow is falling! Bundle up if you're heading out or enjoy the view from inside.";
                } else if (weatherLower.contains("sunny") || weatherLower.contains("clear")) {
                    content.title = "Beautiful Day! ☀️";
                    content.message = "Perfect weather for outdoor activities! Get some vitamin D and fresh air.";
                } else if (weatherLower.contains("cloudy") || weatherLower.contains("overcast")) {
                    content.title = "Cloudy Skies 🌤️";
                    content.message = "Mild weather today. Great for a gentle walk or outdoor exercise.";
                } else {
                    content.title = "Weather Update 🌤️";
                    content.message = String.format("Today's weather: %s. Plan your activities accordingly!", weather);
                }
                
                // Add temperature context
                if (temperature < 0) {
                    content.message += " Bundle up, it's freezing out there!";
                } else if (temperature > 30) {
                    content.message += " Stay cool and hydrated in this heat!";
                } else if (temperature >= 20 && temperature <= 25) {
                    content.message += " Perfect temperature for any outdoor activity!";
                }
            }
        } else {
            content.title = "Weather Check 🌤️";
            content.message = "Check today's weather to plan your perfect day!";
        }
        
        return content;
    }
    
    /**
     * Generate motivational content
     */
    private NotificationContent generateMotivationalContent(Map<String, Object> context, 
                                                          MoodEntry recentMood, 
                                                          UserPattern userPattern) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.MOTIVATION_QUOTE;
        
        List<String> quotes;
        
        if (recentMood != null) {
            if (recentMood.isNegativeMood()) {
                quotes = motivationalQuotes.get("uplifting");
                content.title = "You've Got This! 💪";
            } else if (recentMood.isPositiveMood()) {
                quotes = motivationalQuotes.get("encouraging");
                content.title = "Keep Shining! ✨";
            } else {
                quotes = motivationalQuotes.get("general");
                content.title = "Daily Inspiration 🌟";
            }
        } else {
            quotes = motivationalQuotes.get("general");
            content.title = "Daily Inspiration 🌟";
        }
        
        content.message = getRandomFromList(quotes);
        
        return content;
    }
    
    /**
     * Generate health tip content
     */
    private NotificationContent generateHealthTipContent(Map<String, Object> context, 
                                                       DayRecord todayRecord, 
                                                       MoodEntry recentMood) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.HEALTH_TIP;
        content.title = "Health Tip 🍃";
        
        // Contextual health tips based on current data
        List<String> tips = new ArrayList<>();
        
        if (todayRecord != null && todayRecord.getSteps() < 5000) {
            tips.add("Take the stairs instead of elevators to boost your daily steps!");
            tips.add("Try a 10-minute walk break every hour to stay active.");
            tips.add("Park further away or get off one bus stop early for extra steps.");
        }
        
        if (recentMood != null && recentMood.getStressLevel() > 7) {
            tips.add("Try deep breathing: inhale for 4, hold for 4, exhale for 6.");
            tips.add("Take 5 minutes for mindfulness or meditation.");
            tips.add("Listen to calming music or nature sounds to reduce stress.");
        }
        
        if (recentMood != null && recentMood.getEnergyLevel() < 4) {
            tips.add("Stay hydrated! Dehydration can cause fatigue.");
            tips.add("Get some natural light exposure to boost energy.");
            tips.add("Try light stretching or gentle movement to energize.");
        }
        
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 6 && hour <= 10) {
            tips.add("Start your day with a glass of water to kickstart hydration.");
            tips.add("Morning sunlight helps regulate your circadian rhythm.");
        } else if (hour >= 20) {
            tips.add("Avoid screens 1 hour before bed for better sleep quality.");
            tips.add("Try gentle stretching or reading before bedtime.");
        }
        
        // Always have general tips available
        tips.add("Remember to take breaks from sitting every 30-60 minutes.");
        tips.add("Aim for 7-9 hours of quality sleep each night.");
        tips.add("Practice gratitude - think of 3 things you're thankful for today.");
        
        content.message = getRandomFromList(tips);
        
        return content;
    }
    
    /**
     * Generate insight content
     */
    private NotificationContent generateInsightContent(Map<String, Object> context, 
                                                     UserPattern userPattern) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.INSIGHT_AVAILABLE;
        content.title = "New Insight Available! 📊";
        
        List<String> insightMessages = new ArrayList<>();
        insightMessages.add("We've discovered patterns in your activity data!");
        insightMessages.add("Your mood and weather correlation analysis is ready.");
        insightMessages.add("Check out your weekly activity summary and trends.");
        insightMessages.add("New personalized recommendations based on your habits!");
        insightMessages.add("Your progress report shows interesting patterns.");
        
        content.message = getRandomFromList(insightMessages) + " Tap to explore your insights.";
        
        return content;
    }
    
    /**
     * Generate achievement content
     */
    private NotificationContent generateAchievementContent(Map<String, Object> context, 
                                                         DayRecord todayRecord) {
        NotificationContent content = new NotificationContent();
        content.type = NotificationType.ACHIEVEMENT_EARNED;
        
        // This would normally be populated by the achievement system
        String achievementName = (String) context.get("achievement_name");
        String achievementDescription = (String) context.get("achievement_description");
        
        if (achievementName != null) {
            content.title = "Achievement Unlocked! 🏆";
            content.message = String.format("Congratulations! You've earned '%s': %s", 
                achievementName, achievementDescription);
        } else {
            content.title = "Great Job! 🎉";
            content.message = "You've reached a new milestone! Keep up the excellent work.";
        }
        
        return content;
    }
    
    /**
     * Personalize content based on user patterns and context
     */
    private void personalizeContent(NotificationContent content, Map<String, Object> context, 
                                  UserPattern userPattern, DayRecord todayRecord, 
                                  MoodEntry recentMood) {
        // Add user's name if available
        String userName = (String) context.get("user_name");
        if (userName != null && !userName.isEmpty()) {
            content.message = content.message.replace("you", userName);
        }
        
        // Add time-specific greetings
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 5 && hour <= 11) {
            content.timeOfDayGreeting = "Good morning";
        } else if (hour >= 12 && hour <= 17) {
            content.timeOfDayGreeting = "Good afternoon";
        } else if (hour >= 18 && hour <= 22) {
            content.timeOfDayGreeting = "Good evening";
        } else {
            content.timeOfDayGreeting = "Hello";
        }
        
        // Add action buttons based on notification type
        content.actionButtons = generateActionButtons(content.type, context);
        
        // Set priority based on context
        content.priority = calculateNotificationPriority(content.type, todayRecord, recentMood);
    }
    
    /**
     * Generate action buttons for notifications
     */
    private List<NotificationAction> generateActionButtons(NotificationType type, 
                                                         Map<String, Object> context) {
        List<NotificationAction> actions = new ArrayList<>();
        
        switch (type) {
            case ACTIVITY_REMINDER:
                actions.add(new NotificationAction("Start Now", "start_activity"));
                actions.add(new NotificationAction("Remind Later", "remind_later"));
                break;
                
            case MOOD_CHECK:
                actions.add(new NotificationAction("😊 Good", "quick_mood_good"));
                actions.add(new NotificationAction("😐 Okay", "quick_mood_okay"));
                actions.add(new NotificationAction("😔 Not Great", "quick_mood_bad"));
                break;
                
            case GOAL_PROGRESS:
                actions.add(new NotificationAction("View Stats", "view_progress"));
                actions.add(new NotificationAction("Log Activity", "log_activity"));
                break;
                
            case ACHIEVEMENT_EARNED:
                actions.add(new NotificationAction("Share", "share_achievement"));
                actions.add(new NotificationAction("View", "view_achievement"));
                break;
                
            default:
                actions.add(new NotificationAction("Open App", "open_app"));
                break;
        }
        
        return actions;
    }
    
    /**
     * Calculate notification priority
     */
    private String calculateNotificationPriority(NotificationType type, DayRecord todayRecord, 
                                                MoodEntry recentMood) {
        switch (type) {
            case MOOD_CHECK:
                if (recentMood != null && recentMood.isNegativeMood()) {
                    return "high";
                }
                return "normal";
                
            case WEATHER_ALERT:
                if (todayRecord != null) {
                    String weather = todayRecord.getWeatherCondition();
                    if (weather != null && (weather.contains("storm") || weather.contains("severe"))) {
                        return "high";
                    }
                }
                return "normal";
                
            case ACHIEVEMENT_EARNED:
                return "high";
                
            case ACTIVITY_REMINDER:
                return "normal";
                
            default:
                return "low";
        }
    }
    
    /**
     * Get recent mood entry
     */
    private MoodEntry getRecentMoodEntry() {
        try {
            String today = dateFormat.format(Calendar.getInstance().getTime());
            return databaseHelper.getMoodEntryForDate(today);
        } catch (Exception e) {
            Log.w(TAG, "Could not get recent mood entry", e);
            return null;
        }
    }
    
    /**
     * Check if weather is good for outdoor activities
     */
    private boolean isGoodWeatherForOutdoor(String weatherCondition) {
        if (weatherCondition == null) return true;
        
        String weather = weatherCondition.toLowerCase();
        return !weather.contains("rain") && !weather.contains("storm") && 
               !weather.contains("snow") && !weather.contains("severe");
    }
    
    /**
     * Get random item from list
     */
    private String getRandomFromList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "Stay active and positive!";
        }
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * Create fallback content for errors
     */
    private NotificationContent createFallbackContent(NotificationType type, String baseContent) {
        NotificationContent content = new NotificationContent();
        content.type = type;
        content.title = "LocalLife";
        content.message = baseContent != null ? baseContent : "Check your activity progress";
        content.priority = "normal";
        return content;
    }
    
    /**
     * Initialize content templates
     */
    private void initializeContentTemplates() {
        motivationalQuotes = new HashMap<>();
        weatherBasedContent = new HashMap<>();
        moodBasedContent = new HashMap<>();
        activitySuggestions = new HashMap<>();
        
        // Motivational quotes
        List<String> upliftingQuotes = new ArrayList<>();
        upliftingQuotes.add("Every storm runs out of rain. You've got this! 💪");
        upliftingQuotes.add("Tomorrow is a fresh start. Today is practice for greatness.");
        upliftingQuotes.add("You are stronger than you think and braver than you feel.");
        upliftingQuotes.add("Small progress is still progress. Keep going!");
        motivationalQuotes.put("uplifting", upliftingQuotes);
        
        List<String> encouragingQuotes = new ArrayList<>();
        encouragingQuotes.add("You're radiating positive energy today! Keep shining! ✨");
        encouragingQuotes.add("Your positive attitude is contagious. Spread those good vibes!");
        encouragingQuotes.add("You're proving that consistency creates magic!");
        encouragingQuotes.add("Your energy today could light up a room! 🌟");
        motivationalQuotes.put("encouraging", encouragingQuotes);
        
        List<String> generalQuotes = new ArrayList<>();
        generalQuotes.add("Every step forward is progress, no matter how small.");
        generalQuotes.add("You have the power to make today amazing!");
        generalQuotes.add("Your journey is unique and valuable. Trust the process.");
        generalQuotes.add("Believe in yourself and your ability to grow.");
        motivationalQuotes.put("general", generalQuotes);
        
        // Activity suggestions
        List<String> morningOutdoor = new ArrayList<>();
        morningOutdoor.add("Perfect morning for a refreshing walk in the park!");
        morningOutdoor.add("Beautiful weather for some outdoor stretching or yoga!");
        morningOutdoor.add("Great day to walk or bike to work if possible!");
        activitySuggestions.put("morning_outdoor", morningOutdoor);
        
        List<String> morningIndoor = new ArrayList<>();
        morningIndoor.add("Start your day with some energizing indoor exercises!");
        morningIndoor.add("Perfect time for yoga or stretching by the window!");
        morningIndoor.add("Try some morning meditation to center yourself!");
        activitySuggestions.put("morning_indoor", morningIndoor);
        
        List<String> eveningOutdoor = new ArrayList<>();
        eveningOutdoor.add("Beautiful evening for a peaceful walk!");
        eveningOutdoor.add("Perfect time for outdoor activities or gardening!");
        eveningOutdoor.add("Great weather to enjoy nature and unwind!");
        activitySuggestions.put("evening_outdoor", eveningOutdoor);
        
        List<String> eveningIndoor = new ArrayList<>();
        eveningIndoor.add("Perfect time for gentle stretching or yoga!");
        eveningIndoor.add("Try some relaxing indoor activities to wind down!");
        eveningIndoor.add("Great evening for mindfulness or reading!");
        activitySuggestions.put("evening_indoor", eveningIndoor);
    }
    
    // Data classes
    public static class NotificationContent {
        public NotificationType type;
        public String title;
        public String message;
        public String baseContent;
        public String timeOfDayGreeting;
        public String priority;
        public List<NotificationAction> actionButtons = new ArrayList<>();
        public Map<String, Object> metadata = new HashMap<>();
    }
    
    public static class NotificationAction {
        public String label;
        public String action;
        
        public NotificationAction(String label, String action) {
            this.label = label;
            this.action = action;
        }
    }
    
    // Enum for notification types
    public enum NotificationType {
        ACTIVITY_REMINDER,
        MOOD_CHECK,
        GOAL_PROGRESS,
        WEATHER_ALERT,
        MOTIVATION_QUOTE,
        HEALTH_TIP,
        INSIGHT_AVAILABLE,
        ACHIEVEMENT_EARNED
    }
    
    // User pattern class (simplified)
    public static class UserPattern {
        public Map<Integer, Float> activityByHour = new HashMap<>();
        public Map<Integer, Float> engagementByHour = new HashMap<>();
        public Map<String, Float> contextualFactors = new HashMap<>();
    }
}