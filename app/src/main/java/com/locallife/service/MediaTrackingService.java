package com.locallife.service;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.locallife.database.DatabaseHelper;
import com.locallife.model.MediaConsumption;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for tracking media consumption across various platforms
 */
public class MediaTrackingService {
    private static final String TAG = "MediaTrackingService";
    private static final String PREFS_NAME = "media_tracking_prefs";
    private static final String KEY_LAST_SCAN = "last_scan_time";
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private SharedPreferences preferences;
    private ExecutorService backgroundExecutor;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    // Media app package names
    private static final Map<String, MediaAppInfo> MEDIA_APPS = new HashMap<>();
    static {
        // Video streaming apps
        MEDIA_APPS.put("com.netflix.mediaclient", new MediaAppInfo("Netflix", "tv", "video"));
        MEDIA_APPS.put("com.amazon.avod.thirdpartyclient", new MediaAppInfo("Prime Video", "tv", "video"));
        MEDIA_APPS.put("com.disney.disneyplus", new MediaAppInfo("Disney+", "movie", "video"));
        MEDIA_APPS.put("com.hulu.plus", new MediaAppInfo("Hulu", "tv", "video"));
        MEDIA_APPS.put("com.hbo.hbonow", new MediaAppInfo("HBO Max", "tv", "video"));
        MEDIA_APPS.put("com.google.android.youtube", new MediaAppInfo("YouTube", "youtube", "video"));
        MEDIA_APPS.put("com.google.android.youtube.tv", new MediaAppInfo("YouTube TV", "tv", "video"));
        MEDIA_APPS.put("tv.twitch", new MediaAppInfo("Twitch", "livestream", "video"));
        
        // Music apps
        MEDIA_APPS.put("com.spotify.music", new MediaAppInfo("Spotify", "music", "audio"));
        MEDIA_APPS.put("com.apple.android.music", new MediaAppInfo("Apple Music", "music", "audio"));
        MEDIA_APPS.put("com.google.android.music", new MediaAppInfo("YouTube Music", "music", "audio"));
        MEDIA_APPS.put("com.amazon.mp3", new MediaAppInfo("Amazon Music", "music", "audio"));
        MEDIA_APPS.put("deezer.android.app", new MediaAppInfo("Deezer", "music", "audio"));
        MEDIA_APPS.put("com.pandora.android", new MediaAppInfo("Pandora", "music", "audio"));
        
        // Podcast apps
        MEDIA_APPS.put("com.google.android.apps.podcasts", new MediaAppInfo("Google Podcasts", "podcast", "audio"));
        MEDIA_APPS.put("fm.overcast", new MediaAppInfo("Overcast", "podcast", "audio"));
        MEDIA_APPS.put("com.castbox.podcasts", new MediaAppInfo("Castbox", "podcast", "audio"));
        MEDIA_APPS.put("au.com.shiftyjelly.pocketcasts", new MediaAppInfo("Pocket Casts", "podcast", "audio"));
        
        // Additional platforms
        MEDIA_APPS.put("com.crunchyroll.crunchyroid", new MediaAppInfo("Crunchyroll", "tv", "video"));
        MEDIA_APPS.put("com.funimation.video", new MediaAppInfo("Funimation", "tv", "video"));
        MEDIA_APPS.put("com.plexapp.android", new MediaAppInfo("Plex", "tv", "video"));
        MEDIA_APPS.put("com.google.android.videos", new MediaAppInfo("Google Play Movies", "movie", "video"));
    }
    
    private static class MediaAppInfo {
        String displayName;
        String mediaType;
        String category;
        
        MediaAppInfo(String displayName, String mediaType, String category) {
            this.displayName = displayName;
            this.mediaType = mediaType;
            this.category = category;
        }
    }
    
    public MediaTrackingService(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        this.packageManager = context.getPackageManager();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Scan for media consumption based on app usage
     */
    public void scanMediaConsumption() {
        backgroundExecutor.execute(() -> {
            try {
                Log.d(TAG, "Starting media consumption scan");
                
                if (!hasUsageStatsPermission()) {
                    Log.w(TAG, "Usage stats permission not granted");
                    return;
                }
                
                // Get usage stats for today
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startTime = calendar.getTimeInMillis();
                long endTime = System.currentTimeMillis();
                
                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
                
                if (usageStatsList != null && !usageStatsList.isEmpty()) {
                    processMediaUsage(usageStatsList);
                }
                
                // Update last scan time
                preferences.edit().putLong(KEY_LAST_SCAN, System.currentTimeMillis()).apply();
                
            } catch (Exception e) {
                Log.e(TAG, "Error scanning media consumption", e);
            }
        });
    }
    
    private void processMediaUsage(List<UsageStats> usageStatsList) {
        String today = dateFormat.format(new Date());
        
        for (UsageStats stats : usageStatsList) {
            String packageName = stats.getPackageName();
            
            if (MEDIA_APPS.containsKey(packageName)) {
                MediaAppInfo appInfo = MEDIA_APPS.get(packageName);
                long usageTime = stats.getTotalTimeInForeground();
                
                // Only process if usage time is significant (> 1 minute)
                if (usageTime > 60000) {
                    int usageMinutes = (int) (usageTime / 60000);
                    
                    // Check if we already have a record for this app today
                    MediaConsumption existing = getExistingMediaRecord(today, packageName);
                    
                    if (existing != null) {
                        // Update existing record
                        existing.setDurationMinutes(usageMinutes);
                        existing.setUpdatedAt(new Date());
                        updateMediaConsumption(existing);
                    } else {
                        // Create new record
                        MediaConsumption media = new MediaConsumption(today, appInfo.mediaType, 
                                appInfo.displayName, appInfo.displayName);
                        media.setDurationMinutes(usageMinutes);
                        media.setSource("appUsage");
                        media.setGenre(appInfo.category);
                        media.setMetadata("{\"packageName\":\"" + packageName + "\"}");
                        
                        insertMediaConsumption(media);
                    }
                    
                    Log.d(TAG, "Tracked " + appInfo.displayName + " usage: " + usageMinutes + " minutes");
                }
            }
        }
    }
    
    /**
     * Manual media logging
     */
    public void logMediaManually(String title, String mediaType, String platform, int durationMinutes) {
        backgroundExecutor.execute(() -> {
            try {
                String today = dateFormat.format(new Date());
                
                MediaConsumption media = new MediaConsumption(today, mediaType, title, platform);
                media.setDurationMinutes(durationMinutes);
                media.setSource("manual");
                media.setStartTime(new Date());
                
                // Set end time based on duration
                Calendar endCal = Calendar.getInstance();
                endCal.add(Calendar.MINUTE, durationMinutes);
                media.setEndTime(endCal.getTime());
                
                insertMediaConsumption(media);
                
                Log.d(TAG, "Manually logged: " + title + " (" + durationMinutes + " minutes)");
                
            } catch (Exception e) {
                Log.e(TAG, "Error logging media manually", e);
            }
        });
    }
    
    /**
     * Log TV show episode
     */
    public void logTVEpisode(String showTitle, int season, int episode, String platform, int durationMinutes) {
        backgroundExecutor.execute(() -> {
            try {
                String today = dateFormat.format(new Date());
                
                MediaConsumption media = new MediaConsumption(today, "tv", showTitle, platform);
                media.setSeason(season);
                media.setEpisode(episode);
                media.setDurationMinutes(durationMinutes);
                media.setSource("manual");
                media.setStartTime(new Date());
                
                // Check if this is a rewatch
                boolean isRewatch = checkIfRewatch(showTitle, season, episode);
                media.setRewatch(isRewatch);
                
                insertMediaConsumption(media);
                
                Log.d(TAG, "Logged TV episode: " + showTitle + " S" + season + "E" + episode);
                
            } catch (Exception e) {
                Log.e(TAG, "Error logging TV episode", e);
            }
        });
    }
    
    /**
     * Log movie
     */
    public void logMovie(String title, String platform, int durationMinutes, String genre) {
        backgroundExecutor.execute(() -> {
            try {
                String today = dateFormat.format(new Date());
                
                MediaConsumption media = new MediaConsumption(today, "movie", title, platform);
                media.setDurationMinutes(durationMinutes);
                media.setGenre(genre);
                media.setSource("manual");
                media.setStartTime(new Date());
                
                insertMediaConsumption(media);
                
                Log.d(TAG, "Logged movie: " + title + " (" + durationMinutes + " minutes)");
                
            } catch (Exception e) {
                Log.e(TAG, "Error logging movie", e);
            }
        });
    }
    
    /**
     * Get media consumption for a specific date
     */
    public List<MediaConsumption> getMediaConsumptionForDate(String date) {
        return databaseHelper.getMediaConsumptionForDate(date);
    }
    
    /**
     * Get media consumption statistics
     */
    public MediaStats getMediaStats(String date) {
        List<MediaConsumption> mediaList = getMediaConsumptionForDate(date);
        
        int totalMinutes = 0;
        int videoMinutes = 0;
        int audioMinutes = 0;
        Map<String, Integer> platformUsage = new HashMap<>();
        Map<String, Integer> genreDistribution = new HashMap<>();
        
        for (MediaConsumption media : mediaList) {
            totalMinutes += media.getDurationMinutes();
            
            if (media.isVideo()) {
                videoMinutes += media.getDurationMinutes();
            } else if (media.isAudio()) {
                audioMinutes += media.getDurationMinutes();
            }
            
            // Platform usage
            String platform = media.getPlatform();
            platformUsage.put(platform, platformUsage.getOrDefault(platform, 0) + media.getDurationMinutes());
            
            // Genre distribution
            String genre = media.getGenre();
            if (genre != null) {
                genreDistribution.put(genre, genreDistribution.getOrDefault(genre, 0) + media.getDurationMinutes());
            }
        }
        
        return new MediaStats(totalMinutes, videoMinutes, audioMinutes, platformUsage, genreDistribution);
    }
    
    /**
     * Detect binge watching patterns
     */
    public List<BingeSession> detectBingeSessions(String date) {
        List<MediaConsumption> mediaList = getMediaConsumptionForDate(date);
        List<BingeSession> bingeSessions = new ArrayList<>();
        
        // Group by show title
        Map<String, List<MediaConsumption>> showGroups = new HashMap<>();
        for (MediaConsumption media : mediaList) {
            if ("tv".equals(media.getMediaType())) {
                String key = media.getTitle() + "_" + media.getPlatform();
                showGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(media);
            }
        }
        
        // Detect binge sessions (3+ episodes in sequence)
        for (Map.Entry<String, List<MediaConsumption>> entry : showGroups.entrySet()) {
            List<MediaConsumption> episodes = entry.getValue();
            if (episodes.size() >= 3) {
                // Sort by episode number
                episodes.sort((a, b) -> Integer.compare(a.getEpisode(), b.getEpisode()));
                
                // Check if episodes are sequential
                boolean isSequential = true;
                for (int i = 1; i < episodes.size(); i++) {
                    if (episodes.get(i).getEpisode() != episodes.get(i-1).getEpisode() + 1) {
                        isSequential = false;
                        break;
                    }
                }
                
                if (isSequential) {
                    int totalMinutes = episodes.stream().mapToInt(MediaConsumption::getDurationMinutes).sum();
                    bingeSessions.add(new BingeSession(episodes.get(0).getTitle(), 
                            episodes.get(0).getPlatform(), episodes.size(), totalMinutes));
                }
            }
        }
        
        return bingeSessions;
    }
    
    /**
     * Get trending/popular shows for quick logging
     */
    public List<String> getTrendingShows() {
        // This would normally call an API like TVMaze or TMDB
        // For now, return a curated list
        List<String> trending = new ArrayList<>();
        trending.add("Stranger Things");
        trending.add("The Crown");
        trending.add("Ozark");
        trending.add("The Mandalorian");
        trending.add("Bridgerton");
        trending.add("Squid Game");
        trending.add("The Office");
        trending.add("Friends");
        trending.add("Breaking Bad");
        trending.add("Game of Thrones");
        return trending;
    }
    
    /**
     * Get user's recent/favorite shows
     */
    public List<String> getRecentlyWatchedShows() {
        // Query database for recently watched shows
        return databaseHelper.getRecentlyWatchedShows();
    }
    
    private boolean hasUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                long now = System.currentTimeMillis();
                List<UsageStats> stats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY, now - 1000, now);
                return stats != null && !stats.isEmpty();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    private MediaConsumption getExistingMediaRecord(String date, String packageName) {
        return databaseHelper.getExistingMediaRecord(date, packageName);
    }
    
    private boolean checkIfRewatch(String showTitle, int season, int episode) {
        return databaseHelper.checkIfRewatch(showTitle, season, episode);
    }
    
    private void insertMediaConsumption(MediaConsumption media) {
        databaseHelper.insertMediaConsumption(media);
    }
    
    private void updateMediaConsumption(MediaConsumption media) {
        databaseHelper.updateMediaConsumption(media);
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes for statistics
    public static class MediaStats {
        public final int totalMinutes;
        public final int videoMinutes;
        public final int audioMinutes;
        public final Map<String, Integer> platformUsage;
        public final Map<String, Integer> genreDistribution;
        
        public MediaStats(int totalMinutes, int videoMinutes, int audioMinutes, 
                         Map<String, Integer> platformUsage, Map<String, Integer> genreDistribution) {
            this.totalMinutes = totalMinutes;
            this.videoMinutes = videoMinutes;
            this.audioMinutes = audioMinutes;
            this.platformUsage = platformUsage;
            this.genreDistribution = genreDistribution;
        }
        
        public String getFormattedTotalTime() {
            if (totalMinutes < 60) {
                return totalMinutes + " minutes";
            } else {
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;
                return hours + "h " + minutes + "m";
            }
        }
    }
    
    public static class BingeSession {
        public final String showTitle;
        public final String platform;
        public final int episodeCount;
        public final int totalMinutes;
        
        public BingeSession(String showTitle, String platform, int episodeCount, int totalMinutes) {
            this.showTitle = showTitle;
            this.platform = platform;
            this.episodeCount = episodeCount;
            this.totalMinutes = totalMinutes;
        }
    }
}