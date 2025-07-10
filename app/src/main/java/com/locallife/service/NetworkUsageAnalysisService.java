package com.locallife.service;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for analyzing network usage patterns and app usage statistics
 */
public class NetworkUsageAnalysisService {
    private static final String TAG = "NetworkUsageAnalysis";
    
    private Context context;
    private NetworkStatsManager networkStatsManager;
    private PackageManager packageManager;
    private ExecutorService backgroundExecutor;
    
    public NetworkUsageAnalysisService(Context context) {
        this.context = context;
        this.networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        this.packageManager = context.getPackageManager();
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Analyze network usage for apps
     */
    public void analyzeNetworkUsage(NetworkUsageCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                long endTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DAY_OF_MONTH, -7); // Last 7 days
                long startTime = calendar.getTimeInMillis();
                
                Map<String, AppNetworkUsage> appUsageMap = new HashMap<>();
                
                // Analyze WiFi usage
                analyzeNetworkTypeUsage(ConnectivityManager.TYPE_WIFI, startTime, endTime, appUsageMap);
                
                // Analyze Mobile data usage
                analyzeNetworkTypeUsage(ConnectivityManager.TYPE_MOBILE, startTime, endTime, appUsageMap);
                
                // Convert to list and add app names
                List<AppNetworkUsage> appUsageList = new ArrayList<>();
                for (Map.Entry<String, AppNetworkUsage> entry : appUsageMap.entrySet()) {
                    AppNetworkUsage usage = entry.getValue();
                    try {
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(entry.getKey(), 0);
                        usage.appName = packageManager.getApplicationLabel(appInfo).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        usage.appName = entry.getKey();
                    }
                    appUsageList.add(usage);
                }
                
                // Sort by total usage
                appUsageList.sort((a, b) -> Long.compare(b.totalBytes, a.totalBytes));
                
                // Calculate network statistics
                NetworkUsageStatistics stats = calculateNetworkStatistics(appUsageList);
                
                if (callback != null) {
                    callback.onNetworkUsageAnalyzed(appUsageList, stats);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing network usage", e);
                if (callback != null) {
                    callback.onError("Network analysis failed: " + e.getMessage());
                }
            }
        });
    }
    
    private void analyzeNetworkTypeUsage(int networkType, long startTime, long endTime, 
                                       Map<String, AppNetworkUsage> appUsageMap) {
        try {
            NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                networkType, null, startTime, endTime, -1);
            
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                
                String packageName = getPackageNameFromUid(bucket.getUid());
                if (packageName != null) {
                    AppNetworkUsage usage = appUsageMap.get(packageName);
                    if (usage == null) {
                        usage = new AppNetworkUsage();
                        usage.packageName = packageName;
                        appUsageMap.put(packageName, usage);
                    }
                    
                    if (networkType == ConnectivityManager.TYPE_WIFI) {
                        usage.wifiRxBytes += bucket.getRxBytes();
                        usage.wifiTxBytes += bucket.getTxBytes();
                    } else if (networkType == ConnectivityManager.TYPE_MOBILE) {
                        usage.mobileRxBytes += bucket.getRxBytes();
                        usage.mobileTxBytes += bucket.getTxBytes();
                    }
                    
                    usage.totalBytes = usage.wifiRxBytes + usage.wifiTxBytes + 
                                     usage.mobileRxBytes + usage.mobileTxBytes;
                }
            }
            
            networkStats.close();
            
        } catch (RemoteException e) {
            Log.e(TAG, "Error querying network stats", e);
        }
    }
    
    private String getPackageNameFromUid(int uid) {
        String[] packages = packageManager.getPackagesForUid(uid);
        return packages != null && packages.length > 0 ? packages[0] : null;
    }
    
    private NetworkUsageStatistics calculateNetworkStatistics(List<AppNetworkUsage> appUsageList) {
        NetworkUsageStatistics stats = new NetworkUsageStatistics();
        
        for (AppNetworkUsage usage : appUsageList) {
            stats.totalWifiUsage += usage.wifiRxBytes + usage.wifiTxBytes;
            stats.totalMobileUsage += usage.mobileRxBytes + usage.mobileTxBytes;
            stats.totalDataUsage += usage.totalBytes;
        }
        
        stats.appCount = appUsageList.size();
        stats.averageAppUsage = stats.appCount > 0 ? stats.totalDataUsage / stats.appCount : 0;
        
        // Find top data consumers
        if (appUsageList.size() > 0) {
            stats.topDataConsumer = appUsageList.get(0);
        }
        
        // Calculate WiFi vs Mobile ratio
        if (stats.totalDataUsage > 0) {
            stats.wifiToMobileRatio = (float) stats.totalWifiUsage / stats.totalMobileUsage;
        }
        
        return stats;
    }
    
    /**
     * Analyze app usage patterns over time
     */
    public void analyzeAppUsagePatterns(int days, AppUsagePatternCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                long endTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DAY_OF_MONTH, -days);
                long startTime = calendar.getTimeInMillis();
                
                Map<String, List<DailyAppUsage>> appPatterns = new HashMap<>();
                
                // Analyze daily patterns
                for (int day = 0; day < days; day++) {
                    Calendar dayStart = Calendar.getInstance();
                    dayStart.setTimeInMillis(startTime);
                    dayStart.add(Calendar.DAY_OF_MONTH, day);
                    dayStart.set(Calendar.HOUR_OF_DAY, 0);
                    dayStart.set(Calendar.MINUTE, 0);
                    dayStart.set(Calendar.SECOND, 0);
                    
                    Calendar dayEnd = Calendar.getInstance();
                    dayEnd.setTimeInMillis(dayStart.getTimeInMillis());
                    dayEnd.add(Calendar.DAY_OF_MONTH, 1);
                    
                    analyzeDailyAppUsage(dayStart.getTimeInMillis(), dayEnd.getTimeInMillis(), 
                                       appPatterns, day);
                }
                
                // Generate pattern insights
                List<AppUsagePattern> patterns = generatePatternInsights(appPatterns);
                
                if (callback != null) {
                    callback.onAppUsagePatternsAnalyzed(patterns);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing app usage patterns", e);
                if (callback != null) {
                    callback.onError("App usage pattern analysis failed: " + e.getMessage());
                }
            }
        });
    }
    
    private void analyzeDailyAppUsage(long startTime, long endTime, 
                                    Map<String, List<DailyAppUsage>> appPatterns, int dayIndex) {
        try {
            // Analyze WiFi usage for the day
            NetworkStats networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI, null, startTime, endTime, -1);
            
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                
                String packageName = getPackageNameFromUid(bucket.getUid());
                if (packageName != null) {
                    List<DailyAppUsage> usageList = appPatterns.get(packageName);
                    if (usageList == null) {
                        usageList = new ArrayList<>();
                        appPatterns.put(packageName, usageList);
                    }
                    
                    // Ensure list has enough entries
                    while (usageList.size() <= dayIndex) {
                        usageList.add(new DailyAppUsage());
                    }
                    
                    DailyAppUsage dailyUsage = usageList.get(dayIndex);
                    dailyUsage.dataUsage += bucket.getRxBytes() + bucket.getTxBytes();
                    dailyUsage.dayIndex = dayIndex;
                }
            }
            
            networkStats.close();
            
        } catch (RemoteException e) {
            Log.e(TAG, "Error analyzing daily app usage", e);
        }
    }
    
    private List<AppUsagePattern> generatePatternInsights(Map<String, List<DailyAppUsage>> appPatterns) {
        List<AppUsagePattern> patterns = new ArrayList<>();
        
        for (Map.Entry<String, List<DailyAppUsage>> entry : appPatterns.entrySet()) {
            String packageName = entry.getKey();
            List<DailyAppUsage> dailyUsages = entry.getValue();
            
            AppUsagePattern pattern = new AppUsagePattern();
            pattern.packageName = packageName;
            
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                pattern.appName = packageManager.getApplicationLabel(appInfo).toString();
            } catch (PackageManager.NameNotFoundException e) {
                pattern.appName = packageName;
            }
            
            // Calculate usage statistics
            long totalUsage = 0;
            long maxUsage = 0;
            int activeDays = 0;
            
            for (DailyAppUsage usage : dailyUsages) {
                totalUsage += usage.dataUsage;
                maxUsage = Math.max(maxUsage, usage.dataUsage);
                if (usage.dataUsage > 0) {
                    activeDays++;
                }
            }
            
            pattern.totalUsage = totalUsage;
            pattern.averageDailyUsage = dailyUsages.size() > 0 ? totalUsage / dailyUsages.size() : 0;
            pattern.peakUsage = maxUsage;
            pattern.activeDays = activeDays;
            pattern.usageConsistency = calculateUsageConsistency(dailyUsages);
            
            // Determine usage category
            if (totalUsage > 1024 * 1024 * 1024) { // > 1GB
                pattern.category = "Heavy";
            } else if (totalUsage > 100 * 1024 * 1024) { // > 100MB
                pattern.category = "Moderate";
            } else {
                pattern.category = "Light";
            }
            
            patterns.add(pattern);
        }
        
        // Sort by total usage
        patterns.sort((a, b) -> Long.compare(b.totalUsage, a.totalUsage));
        
        return patterns;
    }
    
    private float calculateUsageConsistency(List<DailyAppUsage> dailyUsages) {
        if (dailyUsages.size() < 2) return 0f;
        
        long mean = 0;
        for (DailyAppUsage usage : dailyUsages) {
            mean += usage.dataUsage;
        }
        mean /= dailyUsages.size();
        
        long variance = 0;
        for (DailyAppUsage usage : dailyUsages) {
            long diff = usage.dataUsage - mean;
            variance += diff * diff;
        }
        variance /= dailyUsages.size();
        
        double standardDeviation = Math.sqrt(variance);
        
        // Return coefficient of variation (lower = more consistent)
        return mean > 0 ? (float) (standardDeviation / mean) : 0f;
    }
    
    /**
     * Get network usage recommendations
     */
    public void generateNetworkRecommendations(NetworkRecommendationCallback callback) {
        backgroundExecutor.execute(() -> {
            try {
                List<NetworkRecommendation> recommendations = new ArrayList<>();
                
                // Analyze current usage patterns
                analyzeNetworkUsage(new NetworkUsageCallback() {
                    @Override
                    public void onNetworkUsageAnalyzed(List<AppNetworkUsage> appUsages, 
                                                     NetworkUsageStatistics stats) {
                        // Generate recommendations based on usage patterns
                        if (stats.totalMobileUsage > 5L * 1024 * 1024 * 1024) { // > 5GB
                            recommendations.add(new NetworkRecommendation(
                                "High Mobile Data Usage",
                                "Consider using WiFi more frequently to reduce mobile data consumption",
                                NetworkRecommendation.Priority.HIGH
                            ));
                        }
                        
                        if (stats.wifiToMobileRatio < 0.5f) {
                            recommendations.add(new NetworkRecommendation(
                                "Mobile Data Preference",
                                "You're using more mobile data than WiFi. Check WiFi availability",
                                NetworkRecommendation.Priority.MEDIUM
                            ));
                        }
                        
                        // Check for data-hungry apps
                        for (AppNetworkUsage usage : appUsages) {
                            if (usage.totalBytes > 1024 * 1024 * 1024) { // > 1GB
                                recommendations.add(new NetworkRecommendation(
                                    "High Data App: " + usage.appName,
                                    String.format("This app used %s of data. Consider monitoring its usage",
                                        formatBytes(usage.totalBytes)),
                                    NetworkRecommendation.Priority.MEDIUM
                                ));
                                break; // Only show one app recommendation
                            }
                        }
                        
                        if (callback != null) {
                            callback.onNetworkRecommendationsGenerated(recommendations);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating network recommendations", e);
                if (callback != null) {
                    callback.onError("Failed to generate recommendations: " + e.getMessage());
                }
            }
        });
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024f);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024f * 1024f));
        return String.format("%.1f GB", bytes / (1024f * 1024f * 1024f));
    }
    
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }
    
    // Data classes
    public static class AppNetworkUsage {
        public String packageName;
        public String appName;
        public long wifiRxBytes;
        public long wifiTxBytes;
        public long mobileRxBytes;
        public long mobileTxBytes;
        public long totalBytes;
        
        public String getFormattedTotalUsage() {
            if (totalBytes < 1024) return totalBytes + " B";
            if (totalBytes < 1024 * 1024) return String.format("%.1f KB", totalBytes / 1024f);
            if (totalBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", totalBytes / (1024f * 1024f));
            return String.format("%.1f GB", totalBytes / (1024f * 1024f * 1024f));
        }
    }
    
    public static class NetworkUsageStatistics {
        public long totalWifiUsage;
        public long totalMobileUsage;
        public long totalDataUsage;
        public int appCount;
        public long averageAppUsage;
        public AppNetworkUsage topDataConsumer;
        public float wifiToMobileRatio;
    }
    
    public static class DailyAppUsage {
        public long dataUsage;
        public int dayIndex;
    }
    
    public static class AppUsagePattern {
        public String packageName;
        public String appName;
        public long totalUsage;
        public long averageDailyUsage;
        public long peakUsage;
        public int activeDays;
        public float usageConsistency;
        public String category; // Heavy, Moderate, Light
    }
    
    public static class NetworkRecommendation {
        public String title;
        public String description;
        public Priority priority;
        
        public NetworkRecommendation(String title, String description, Priority priority) {
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
        
        public enum Priority {
            LOW, MEDIUM, HIGH
        }
    }
    
    // Callback interfaces
    public interface NetworkUsageCallback {
        void onNetworkUsageAnalyzed(List<AppNetworkUsage> appUsages, NetworkUsageStatistics stats);
        void onError(String error);
    }
    
    public interface AppUsagePatternCallback {
        void onAppUsagePatternsAnalyzed(List<AppUsagePattern> patterns);
        void onError(String error);
    }
    
    public interface NetworkRecommendationCallback {
        void onNetworkRecommendationsGenerated(List<NetworkRecommendation> recommendations);
        void onError(String error);
    }
}