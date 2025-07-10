package com.locallife.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.locallife.model.Achievement;
import com.locallife.model.DayRecord;
import com.locallife.model.Goal;
import com.locallife.model.ShareableContent;
import com.locallife.model.ShareableContent.SharePlatform;
import com.locallife.model.UserLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Comprehensive service for handling social media sharing across multiple platforms
 * Supports Twitter, Facebook, Instagram, LinkedIn, WhatsApp, and Telegram
 */
public class SocialSharingService {
    private static final String TAG = "SocialSharingService";
    private static final String SHARE_IMAGE_DIR = "LocalLife_Shares";
    
    private Context context;
    private ImageGenerationService imageGenerationService;
    private ShareAnalyticsService analyticsService;
    private PlatformIntentHelper platformIntentHelper;
    
    // Platform package names for intent filtering
    private static final Map<SharePlatform, String> PLATFORM_PACKAGES = new HashMap<>();
    
    static {
        PLATFORM_PACKAGES.put(SharePlatform.TWITTER, "com.twitter.android");
        PLATFORM_PACKAGES.put(SharePlatform.FACEBOOK, "com.facebook.katana");
        PLATFORM_PACKAGES.put(SharePlatform.INSTAGRAM, "com.instagram.android");
        PLATFORM_PACKAGES.put(SharePlatform.LINKEDIN, "com.linkedin.android");
        PLATFORM_PACKAGES.put(SharePlatform.WHATSAPP, "com.whatsapp");
        PLATFORM_PACKAGES.put(SharePlatform.TELEGRAM, "org.telegram.messenger");
    }
    
    // Share result callback interface
    public interface ShareResultCallback {
        void onSuccess(SharePlatform platform, String shareId);
        void onError(SharePlatform platform, Exception error);
        void onCancel(SharePlatform platform);
    }
    
    public SocialSharingService(Context context) {
        this.context = context;
        this.imageGenerationService = new ImageGenerationService(context);
        this.analyticsService = new ShareAnalyticsService(context);
        this.platformIntentHelper = new PlatformIntentHelper(context);
    }
    
    /**
     * Share achievement unlock to specified platform
     */
    public void shareAchievement(Achievement achievement, SharePlatform platform, 
                               ShareResultCallback callback) {
        ShareableContent content = ShareableContent.createAchievementShare(achievement);
        
        // Generate achievement card image
        CompletableFuture<Bitmap> imageFuture = imageGenerationService
            .generateAchievementCard(achievement);
        
        imageFuture.thenAccept(bitmap -> {
            content.setImageBitmap(bitmap);
            shareContent(content, platform, callback);
        }).exceptionally(throwable -> {
            Log.e(TAG, "Error generating achievement image", throwable);
            // Share without image
            shareContent(content, platform, callback);
            return null;
        });
    }
    
    /**
     * Share level up to specified platform
     */
    public void shareLevelUp(UserLevel userLevel, SharePlatform platform, 
                           ShareResultCallback callback) {
        ShareableContent content = ShareableContent.createLevelUpShare(userLevel);
        
        // Generate level up card image
        CompletableFuture<Bitmap> imageFuture = imageGenerationService
            .generateLevelUpCard(userLevel);
        
        imageFuture.thenAccept(bitmap -> {
            content.setImageBitmap(bitmap);
            shareContent(content, platform, callback);
        }).exceptionally(throwable -> {
            Log.e(TAG, "Error generating level up image", throwable);
            // Share without image
            shareContent(content, platform, callback);
            return null;
        });
    }
    
    /**
     * Share goal completion to specified platform
     */
    public void shareGoalCompletion(Goal goal, SharePlatform platform, 
                                  ShareResultCallback callback) {
        ShareableContent content = ShareableContent.createGoalShare(goal);
        
        // Generate goal completion card image
        CompletableFuture<Bitmap> imageFuture = imageGenerationService
            .generateGoalCompletionCard(goal);
        
        imageFuture.thenAccept(bitmap -> {
            content.setImageBitmap(bitmap);
            shareContent(content, platform, callback);
        }).exceptionally(throwable -> {
            Log.e(TAG, "Error generating goal completion image", throwable);
            // Share without image
            shareContent(content, platform, callback);
            return null;
        });
    }
    
    /**
     * Share streak milestone to specified platform
     */
    public void shareStreakMilestone(Goal goal, SharePlatform platform, 
                                   ShareResultCallback callback) {
        ShareableContent content = ShareableContent.createStreakShare(goal);
        
        // Generate streak milestone card image
        CompletableFuture<Bitmap> imageFuture = imageGenerationService
            .generateStreakMilestoneCard(goal);
        
        imageFuture.thenAccept(bitmap -> {
            content.setImageBitmap(bitmap);
            shareContent(content, platform, callback);
        }).exceptionally(throwable -> {
            Log.e(TAG, "Error generating streak milestone image", throwable);
            // Share without image
            shareContent(content, platform, callback);
            return null;
        });
    }
    
    /**
     * Share daily summary to specified platform
     */
    public void shareDailySummary(DayRecord dayRecord, SharePlatform platform, 
                                ShareResultCallback callback) {
        ShareableContent content = ShareableContent.createDailySummaryShare(dayRecord);
        
        // Generate daily summary card image
        CompletableFuture<Bitmap> imageFuture = imageGenerationService
            .generateDailySummaryCard(dayRecord);
        
        imageFuture.thenAccept(bitmap -> {
            content.setImageBitmap(bitmap);
            shareContent(content, platform, callback);
        }).exceptionally(throwable -> {
            Log.e(TAG, "Error generating daily summary image", throwable);
            // Share without image
            shareContent(content, platform, callback);
            return null;
        });
    }
    
    /**
     * Share generic content to specified platform
     */
    public void shareContent(ShareableContent content, SharePlatform platform, 
                           ShareResultCallback callback) {
        if (!isPlatformAvailable(platform)) {
            callback.onError(platform, 
                new Exception("Platform not installed: " + platform.name()));
            return;
        }
        
        try {
            // Get platform-specific formatted text
            String shareText = content.getFormattedShareText(platform);
            
            // Create and execute share intent
            Intent shareIntent = createShareIntent(content, platform, shareText);
            
            if (shareIntent != null) {
                // Track share attempt
                analyticsService.trackShareAttempt(content, platform);
                
                // Start activity for result if possible
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(shareIntent, 
                        getRequestCodeForPlatform(platform));
                } else {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(shareIntent);
                }
                
                // Consider share as successful if intent was created
                String shareId = generateShareId(content, platform);
                callback.onSuccess(platform, shareId);
                
                // Track successful share
                analyticsService.trackShareSuccess(content, platform, shareId);
                
            } else {
                callback.onError(platform, 
                    new Exception("Failed to create share intent"));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error sharing content to " + platform.name(), e);
            callback.onError(platform, e);
            analyticsService.trackShareError(content, platform, e);
        }
    }
    
    /**
     * Create platform-specific share intent
     */
    private Intent createShareIntent(ShareableContent content, SharePlatform platform, 
                                   String shareText) {
        switch (platform) {
            case TWITTER:
                return createTwitterIntent(shareText, content.getImageBitmap());
            case FACEBOOK:
                return createFacebookIntent(shareText, content.getImageBitmap());
            case INSTAGRAM:
                return createInstagramIntent(shareText, content.getImageBitmap());
            case LINKEDIN:
                return createLinkedInIntent(shareText, content.getImageBitmap());
            case WHATSAPP:
                return createWhatsAppIntent(shareText, content.getImageBitmap());
            case TELEGRAM:
                return createTelegramIntent(shareText, content.getImageBitmap());
            default:
                return createGenericIntent(shareText, content.getImageBitmap());
        }
    }
    
    /**
     * Create Twitter share intent
     */
    private Intent createTwitterIntent(String text, Bitmap image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage(PLATFORM_PACKAGES.get(SharePlatform.TWITTER));
        intent.putExtra(Intent.EXTRA_TEXT, text);
        
        if (image != null) {
            Uri imageUri = saveImageToCache(image, "twitter_share");
            if (imageUri != null) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        
        return intent.resolveActivity(context.getPackageManager()) != null ? intent : null;
    }
    
    /**
     * Create Facebook share intent
     */
    private Intent createFacebookIntent(String text, Bitmap image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(PLATFORM_PACKAGES.get(SharePlatform.FACEBOOK));
        
        if (image != null) {
            Uri imageUri = saveImageToCache(image, "facebook_share");
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
        
        return intent.resolveActivity(context.getPackageManager()) != null ? intent : null;
    }
    
    /**
     * Create Instagram share intent
     */
    private Intent createInstagramIntent(String text, Bitmap image) {
        if (image == null) {
            // Instagram requires images, fallback to stories
            return createInstagramStoryIntent(text);
        }
        
        Uri imageUri = saveImageToCache(image, "instagram_share");
        if (imageUri == null) return null;
        
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.setPackage(PLATFORM_PACKAGES.get(SharePlatform.INSTAGRAM));
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        return intent.resolveActivity(context.getPackageManager()) != null ? intent : null;
    }
    
    /**
     * Create Instagram story intent for text-only content
     */
    private Intent createInstagramStoryIntent(String text) {
        Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
        intent.setPackage(PLATFORM_PACKAGES.get(SharePlatform.INSTAGRAM));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        
        return intent.resolveActivity(context.getPackageManager()) != null ? intent : null;
    }
    
    /**
     * Create LinkedIn share intent
     */
    private Intent createLinkedInIntent(String text, Bitmap image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(PLATFORM_PACKAGES.get(SharePlatform.LINKEDIN));
        
        if (image != null) {
            Uri imageUri = saveImageToCache(image, "linkedin_share");
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
        
        return intent.resolveActivity(context.getPackageManager()) != null ? intent : null;
    }
    
    /**
     * Create WhatsApp share intent
     */
    private Intent createWhatsAppIntent(String text, Bitmap image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(PLATFORM_PACKAGES.get(SharePlatform.WHATSAPP));
        
        if (image != null) {
            Uri imageUri = saveImageToCache(image, "whatsapp_share");
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
        
        return intent.resolveActivity(context.getPackageManager()) != null ? intent : null;
    }
    
    /**
     * Create Telegram share intent
     */
    private Intent createTelegramIntent(String text, Bitmap image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(PLATFORM_PACKAGES.get(SharePlatform.TELEGRAM));
        
        if (image != null) {
            Uri imageUri = saveImageToCache(image, "telegram_share");
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
        
        return intent.resolveActivity(context.getPackageManager()) != null ? intent : null;
    }
    
    /**
     * Create generic share intent for unsupported platforms
     */
    private Intent createGenericIntent(String text, Bitmap image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        
        if (image != null) {
            Uri imageUri = saveImageToCache(image, "generic_share");
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
        
        return Intent.createChooser(intent, "Share via");
    }
    
    /**
     * Check if platform is available on device
     */
    public boolean isPlatformAvailable(SharePlatform platform) {
        String packageName = PLATFORM_PACKAGES.get(platform);
        if (packageName == null) return true; // Generic sharing always available
        
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get available platforms on device
     */
    public SharePlatform[] getAvailablePlatforms() {
        return SharePlatform.values();
    }
    
    /**
     * Save image to cache directory for sharing
     */
    private Uri saveImageToCache(Bitmap bitmap, String prefix) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", 
                Locale.getDefault()).format(new Date());
            String filename = prefix + "_" + timeStamp + ".jpg";
            
            File shareDir = new File(context.getCacheDir(), SHARE_IMAGE_DIR);
            if (!shareDir.exists()) {
                shareDir.mkdirs();
            }
            
            File imageFile = new File(shareDir, filename);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            
            return Uri.fromFile(imageFile);
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving image to cache", e);
            return null;
        }
    }
    
    /**
     * Generate unique share ID for tracking
     */
    private String generateShareId(ShareableContent content, SharePlatform platform) {
        return String.format(Locale.getDefault(), "%s_%s_%d", 
            content.getShareType().name().toLowerCase(),
            platform.name().toLowerCase(),
            System.currentTimeMillis());
    }
    
    /**
     * Get request code for platform sharing
     */
    private int getRequestCodeForPlatform(SharePlatform platform) {
        switch (platform) {
            case TWITTER: return 1001;
            case FACEBOOK: return 1002;
            case INSTAGRAM: return 1003;
            case LINKEDIN: return 1004;
            case WHATSAPP: return 1005;
            case TELEGRAM: return 1006;
            default: return 1000;
        }
    }
    
    /**
     * Handle share result from activity
     */
    public void handleShareResult(int requestCode, int resultCode, Intent data, 
                                ShareResultCallback callback) {
        SharePlatform platform = getPlatformFromRequestCode(requestCode);
        
        if (resultCode == Activity.RESULT_OK) {
            // Share was successful
            String shareId = data != null ? data.getStringExtra("share_id") : null;
            if (shareId == null) {
                shareId = "success_" + System.currentTimeMillis();
            }
            
            callback.onSuccess(platform, shareId);
            
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Share was canceled
            callback.onCancel(platform);
            analyticsService.trackShareCancel(platform);
            
        } else {
            // Share failed
            callback.onError(platform, new Exception("Share failed with result code: " + resultCode));
        }
    }
    
    /**
     * Get platform from request code
     */
    private SharePlatform getPlatformFromRequestCode(int requestCode) {
        switch (requestCode) {
            case 1001: return SharePlatform.TWITTER;
            case 1002: return SharePlatform.FACEBOOK;
            case 1003: return SharePlatform.INSTAGRAM;
            case 1004: return SharePlatform.LINKEDIN;
            case 1005: return SharePlatform.WHATSAPP;
            case 1006: return SharePlatform.TELEGRAM;
            default: return SharePlatform.GENERIC;
        }
    }
    
    /**
     * Clear share cache
     */
    public void clearShareCache() {
        try {
            File shareDir = new File(context.getCacheDir(), SHARE_IMAGE_DIR);
            if (shareDir.exists()) {
                File[] files = shareDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing share cache", e);
        }
    }
    
    /**
     * Show platform selection dialog
     */
    public void showShareDialog(Activity activity, ShareableContent content, 
                              ShareResultCallback callback) {
        if (platformIntentHelper != null) {
            platformIntentHelper.showShareDialog(activity, content, callback);
        } else {
            // Fallback to generic sharing
            shareContent(content, SharePlatform.GENERIC, callback);
        }
    }
    
    /**
     * Batch share to multiple platforms
     */
    public void shareToMultiplePlatforms(ShareableContent content, 
                                       SharePlatform[] platforms, 
                                       ShareResultCallback callback) {
        for (SharePlatform platform : platforms) {
            shareContent(content, platform, new ShareResultCallback() {
                @Override
                public void onSuccess(SharePlatform platform, String shareId) {
                    callback.onSuccess(platform, shareId);
                }
                
                @Override
                public void onError(SharePlatform platform, Exception error) {
                    callback.onError(platform, error);
                }
                
                @Override
                public void onCancel(SharePlatform platform) {
                    callback.onCancel(platform);
                }
            });
        }
    }
    
    /**
     * Get share statistics
     */
    public Map<String, Object> getShareStatistics() {
        if (analyticsService != null) {
            return analyticsService.getShareStatistics();
        }
        return new HashMap<>();
    }
    
    /**
     * Get most popular sharing platform
     */
    public SharePlatform getMostPopularPlatform() {
        if (analyticsService != null) {
            return analyticsService.getMostPopularPlatform();
        }
        return SharePlatform.GENERIC;
    }
    
    /**
     * Get share history
     */
    public Map<String, Object> getShareHistory(int limit) {
        if (analyticsService != null) {
            return analyticsService.getShareHistory(limit);
        }
        return new HashMap<>();
    }
}