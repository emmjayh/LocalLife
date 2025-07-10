package com.locallife.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.locallife.fragments.ShareDialogFragment;
import com.locallife.model.ShareableContent;
import com.locallife.model.ShareableContent.SharePlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for creating platform-specific sharing intents and handling platform detection
 */
public class PlatformIntentHelper {
    private static final String TAG = "PlatformIntentHelper";
    
    private Context context;
    private PackageManager packageManager;
    
    // Platform configuration
    private static final Map<SharePlatform, PlatformConfig> PLATFORM_CONFIGS = new HashMap<>();
    
    static {
        // Twitter configuration
        PLATFORM_CONFIGS.put(SharePlatform.TWITTER, new PlatformConfig(
            "com.twitter.android",
            new String[]{"com.twitter.android"},
            280, // Character limit
            true, // Supports images
            true, // Supports text
            "https://twitter.com/intent/tweet?text="
        ));
        
        // Facebook configuration
        PLATFORM_CONFIGS.put(SharePlatform.FACEBOOK, new PlatformConfig(
            "com.facebook.katana",
            new String[]{"com.facebook.katana", "com.facebook.lite"},
            63206, // Character limit
            true, // Supports images
            true, // Supports text
            "https://www.facebook.com/sharer/sharer.php?u="
        ));
        
        // Instagram configuration
        PLATFORM_CONFIGS.put(SharePlatform.INSTAGRAM, new PlatformConfig(
            "com.instagram.android",
            new String[]{"com.instagram.android"},
            2200, // Character limit
            true, // Supports images
            true, // Supports text
            null // No web fallback
        ));
        
        // LinkedIn configuration
        PLATFORM_CONFIGS.put(SharePlatform.LINKEDIN, new PlatformConfig(
            "com.linkedin.android",
            new String[]{"com.linkedin.android"},
            1300, // Character limit
            true, // Supports images
            true, // Supports text
            "https://www.linkedin.com/sharing/share-offsite/?url="
        ));
        
        // WhatsApp configuration
        PLATFORM_CONFIGS.put(SharePlatform.WHATSAPP, new PlatformConfig(
            "com.whatsapp",
            new String[]{"com.whatsapp", "com.whatsapp.w4b"},
            65536, // Character limit
            true, // Supports images
            true, // Supports text
            "https://api.whatsapp.com/send?text="
        ));
        
        // Telegram configuration
        PLATFORM_CONFIGS.put(SharePlatform.TELEGRAM, new PlatformConfig(
            "org.telegram.messenger",
            new String[]{"org.telegram.messenger", "org.telegram.plus", "org.thunderdog.challegram"},
            4096, // Character limit
            true, // Supports images
            true, // Supports text
            "https://t.me/share/url?url="
        ));
    }
    
    public PlatformIntentHelper(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }
    
    /**
     * Show share dialog with available platforms
     */
    public void showShareDialog(Activity activity, ShareableContent content, 
                              SocialSharingService.ShareResultCallback callback) {
        ShareDialogFragment dialog = ShareDialogFragment.newInstance(content);
        dialog.setShareResultListener(new ShareDialogFragment.ShareResultListener() {
            @Override
            public void onShareSuccess(SharePlatform platform, String shareId) {
                callback.onSuccess(platform, shareId);
            }
            
            @Override
            public void onShareError(SharePlatform platform, Exception error) {
                callback.onError(platform, error);
            }
            
            @Override
            public void onShareCancel() {
                callback.onCancel(SharePlatform.GENERIC);
            }
        });
        
        dialog.show(activity.getSupportFragmentManager(), "share_dialog");
    }
    
    /**
     * Check if a platform is available on the device
     */
    public boolean isPlatformAvailable(SharePlatform platform) {
        PlatformConfig config = PLATFORM_CONFIGS.get(platform);
        if (config == null) return false;
        
        for (String packageName : config.packageNames) {
            if (isPackageInstalled(packageName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all available platforms on the device
     */
    public List<SharePlatform> getAvailablePlatforms() {
        List<SharePlatform> availablePlatforms = new ArrayList<>();
        
        for (SharePlatform platform : SharePlatform.values()) {
            if (platform != SharePlatform.GENERIC && isPlatformAvailable(platform)) {
                availablePlatforms.add(platform);
            }
        }
        
        // Always add generic as fallback
        availablePlatforms.add(SharePlatform.GENERIC);
        
        return availablePlatforms;
    }
    
    /**
     * Create optimized share intent for platform
     */
    public Intent createOptimizedShareIntent(ShareableContent content, SharePlatform platform) {
        PlatformConfig config = PLATFORM_CONFIGS.get(platform);
        if (config == null) {
            return createGenericShareIntent(content);
        }
        
        String shareText = content.getFormattedShareText(platform);
        
        // Trim text if necessary
        if (shareText.length() > config.characterLimit) {
            shareText = shareText.substring(0, config.characterLimit - 3) + "...";
        }
        
        // Create platform-specific intent
        switch (platform) {
            case TWITTER:
                return createTwitterOptimizedIntent(shareText, content);
            case FACEBOOK:
                return createFacebookOptimizedIntent(shareText, content);
            case INSTAGRAM:
                return createInstagramOptimizedIntent(shareText, content);
            case LINKEDIN:
                return createLinkedInOptimizedIntent(shareText, content);
            case WHATSAPP:
                return createWhatsAppOptimizedIntent(shareText, content);
            case TELEGRAM:
                return createTelegramOptimizedIntent(shareText, content);
            default:
                return createGenericShareIntent(content);
        }
    }
    
    /**
     * Create Twitter optimized intent
     */
    private Intent createTwitterOptimizedIntent(String text, ShareableContent content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage(getPreferredPackage(SharePlatform.TWITTER));
        intent.putExtra(Intent.EXTRA_TEXT, text);
        
        // Twitter-specific extras
        intent.putExtra("twitter_text", text);
        
        if (content.getImageBitmap() != null) {
            Uri imageUri = saveImageForSharing(content.getImageBitmap(), "twitter");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        
        return intent;
    }
    
    /**
     * Create Facebook optimized intent
     */
    private Intent createFacebookOptimizedIntent(String text, ShareableContent content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(getPreferredPackage(SharePlatform.FACEBOOK));
        
        if (content.getImageBitmap() != null) {
            Uri imageUri = saveImageForSharing(content.getImageBitmap(), "facebook");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }
        
        return intent;
    }
    
    /**
     * Create Instagram optimized intent
     */
    private Intent createInstagramOptimizedIntent(String text, ShareableContent content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(getPreferredPackage(SharePlatform.INSTAGRAM));
        
        if (content.getImageBitmap() != null) {
            Uri imageUri = saveImageForSharing(content.getImageBitmap(), "instagram");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            // Instagram Stories for text-only
            intent.setAction("com.instagram.share.ADD_TO_STORY");
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }
        
        return intent;
    }
    
    /**
     * Create LinkedIn optimized intent
     */
    private Intent createLinkedInOptimizedIntent(String text, ShareableContent content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(getPreferredPackage(SharePlatform.LINKEDIN));
        
        if (content.getImageBitmap() != null) {
            Uri imageUri = saveImageForSharing(content.getImageBitmap(), "linkedin");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }
        
        return intent;
    }
    
    /**
     * Create WhatsApp optimized intent
     */
    private Intent createWhatsAppOptimizedIntent(String text, ShareableContent content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(getPreferredPackage(SharePlatform.WHATSAPP));
        
        if (content.getImageBitmap() != null) {
            Uri imageUri = saveImageForSharing(content.getImageBitmap(), "whatsapp");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }
        
        return intent;
    }
    
    /**
     * Create Telegram optimized intent
     */
    private Intent createTelegramOptimizedIntent(String text, ShareableContent content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(getPreferredPackage(SharePlatform.TELEGRAM));
        
        if (content.getImageBitmap() != null) {
            Uri imageUri = saveImageForSharing(content.getImageBitmap(), "telegram");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }
        
        return intent;
    }
    
    /**
     * Create generic share intent
     */
    private Intent createGenericShareIntent(ShareableContent content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String shareText = content.getFormattedShareText(SharePlatform.GENERIC);
        
        if (content.getImageBitmap() != null) {
            Uri imageUri = saveImageForSharing(content.getImageBitmap(), "generic");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.putExtra(Intent.EXTRA_TEXT, shareText);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
        }
        
        return Intent.createChooser(intent, "Share via");
    }
    
    /**
     * Create web fallback intent for platforms without app
     */
    public Intent createWebFallbackIntent(ShareableContent content, SharePlatform platform) {
        PlatformConfig config = PLATFORM_CONFIGS.get(platform);
        if (config == null || config.webFallbackUrl == null) {
            return null;
        }
        
        String shareText = content.getFormattedShareText(platform);
        String url = config.webFallbackUrl + Uri.encode(shareText);
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        
        return intent;
    }
    
    /**
     * Get preferred package name for platform
     */
    private String getPreferredPackage(SharePlatform platform) {
        PlatformConfig config = PLATFORM_CONFIGS.get(platform);
        if (config == null) return null;
        
        // Return first available package
        for (String packageName : config.packageNames) {
            if (isPackageInstalled(packageName)) {
                return packageName;
            }
        }
        
        return config.primaryPackage;
    }
    
    /**
     * Check if package is installed
     */
    private boolean isPackageInstalled(String packageName) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Save image for sharing
     */
    private Uri saveImageForSharing(android.graphics.Bitmap bitmap, String prefix) {
        // This would typically use FileProvider or similar
        // For now, return null as a placeholder
        // In a real implementation, you'd save to cache and return the URI
        return null;
    }
    
    /**
     * Get platform configuration
     */
    public PlatformConfig getPlatformConfig(SharePlatform platform) {
        return PLATFORM_CONFIGS.get(platform);
    }
    
    /**
     * Get optimal text length for platform
     */
    public int getOptimalTextLength(SharePlatform platform) {
        PlatformConfig config = PLATFORM_CONFIGS.get(platform);
        return config != null ? config.characterLimit : 280;
    }
    
    /**
     * Check if platform supports images
     */
    public boolean supportsImages(SharePlatform platform) {
        PlatformConfig config = PLATFORM_CONFIGS.get(platform);
        return config != null && config.supportsImages;
    }
    
    /**
     * Check if platform supports text
     */
    public boolean supportsText(SharePlatform platform) {
        PlatformConfig config = PLATFORM_CONFIGS.get(platform);
        return config != null && config.supportsText;
    }
    
    /**
     * Get sharing recommendations for content
     */
    public List<SharePlatform> getRecommendedPlatforms(ShareableContent content) {
        List<SharePlatform> recommendations = new ArrayList<>();
        
        // Recommend based on content type
        switch (content.getShareType()) {
            case ACHIEVEMENT:
                recommendations.add(SharePlatform.TWITTER);
                recommendations.add(SharePlatform.LINKEDIN);
                recommendations.add(SharePlatform.INSTAGRAM);
                break;
            case LEVEL_UP:
                recommendations.add(SharePlatform.INSTAGRAM);
                recommendations.add(SharePlatform.TWITTER);
                recommendations.add(SharePlatform.FACEBOOK);
                break;
            case GOAL_COMPLETION:
                recommendations.add(SharePlatform.LINKEDIN);
                recommendations.add(SharePlatform.TWITTER);
                recommendations.add(SharePlatform.FACEBOOK);
                break;
            case STREAK_MILESTONE:
                recommendations.add(SharePlatform.INSTAGRAM);
                recommendations.add(SharePlatform.TWITTER);
                break;
            case DAILY_SUMMARY:
                recommendations.add(SharePlatform.TWITTER);
                recommendations.add(SharePlatform.INSTAGRAM);
                break;
            default:
                recommendations.add(SharePlatform.GENERIC);
                break;
        }
        
        // Filter by availability
        List<SharePlatform> availableRecommendations = new ArrayList<>();
        for (SharePlatform platform : recommendations) {
            if (isPlatformAvailable(platform)) {
                availableRecommendations.add(platform);
            }
        }
        
        return availableRecommendations;
    }
    
    /**
     * Platform configuration class
     */
    public static class PlatformConfig {
        public final String primaryPackage;
        public final String[] packageNames;
        public final int characterLimit;
        public final boolean supportsImages;
        public final boolean supportsText;
        public final String webFallbackUrl;
        
        public PlatformConfig(String primaryPackage, String[] packageNames, int characterLimit,
                            boolean supportsImages, boolean supportsText, String webFallbackUrl) {
            this.primaryPackage = primaryPackage;
            this.packageNames = packageNames;
            this.characterLimit = characterLimit;
            this.supportsImages = supportsImages;
            this.supportsText = supportsText;
            this.webFallbackUrl = webFallbackUrl;
        }
    }
    
    /**
     * Get platform analytics data
     */
    public Map<String, Object> getPlatformAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        for (SharePlatform platform : SharePlatform.values()) {
            if (platform == SharePlatform.GENERIC) continue;
            
            Map<String, Object> platformData = new HashMap<>();
            platformData.put("available", isPlatformAvailable(platform));
            platformData.put("supports_images", supportsImages(platform));
            platformData.put("supports_text", supportsText(platform));
            platformData.put("character_limit", getOptimalTextLength(platform));
            
            analytics.put(platform.name(), platformData);
        }
        
        return analytics;
    }
}