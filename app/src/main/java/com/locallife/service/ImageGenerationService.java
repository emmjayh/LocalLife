package com.locallife.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.Log;

import com.locallife.model.Achievement;
import com.locallife.model.DayRecord;
import com.locallife.model.Goal;
import com.locallife.model.UserLevel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service for generating visual content for social sharing
 * Creates achievement cards, level up cards, and other visual content
 */
public class ImageGenerationService {
    private static final String TAG = "ImageGenerationService";
    
    // Image dimensions
    private static final int CARD_WIDTH = 1080;
    private static final int CARD_HEIGHT = 1080;
    private static final int SUMMARY_CARD_HEIGHT = 720;
    
    // Colors
    private static final int BACKGROUND_COLOR = Color.parseColor("#1E1E1E");
    private static final int CARD_BACKGROUND_COLOR = Color.parseColor("#2D2D2D");
    private static final int TEXT_PRIMARY_COLOR = Color.parseColor("#FFFFFF");
    private static final int TEXT_SECONDARY_COLOR = Color.parseColor("#B0B0B0");
    private static final int ACCENT_COLOR = Color.parseColor("#4CAF50");
    
    // Fonts and sizes
    private static final float TITLE_TEXT_SIZE = 72f;
    private static final float SUBTITLE_TEXT_SIZE = 48f;
    private static final float BODY_TEXT_SIZE = 36f;
    private static final float LABEL_TEXT_SIZE = 28f;
    
    private Context context;
    
    public ImageGenerationService(Context context) {
        this.context = context;
    }
    
    /**
     * Generate achievement card image
     */
    public CompletableFuture<Bitmap> generateAchievementCard(Achievement achievement) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createAchievementCard(achievement);
            } catch (Exception e) {
                Log.e(TAG, "Error generating achievement card", e);
                throw new CompletionException(e);
            }
        });
    }
    
    /**
     * Generate level up card image
     */
    public CompletableFuture<Bitmap> generateLevelUpCard(UserLevel userLevel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createLevelUpCard(userLevel);
            } catch (Exception e) {
                Log.e(TAG, "Error generating level up card", e);
                throw new CompletionException(e);
            }
        });
    }
    
    /**
     * Generate goal completion card image
     */
    public CompletableFuture<Bitmap> generateGoalCompletionCard(Goal goal) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createGoalCompletionCard(goal);
            } catch (Exception e) {
                Log.e(TAG, "Error generating goal completion card", e);
                throw new CompletionException(e);
            }
        });
    }
    
    /**
     * Generate streak milestone card image
     */
    public CompletableFuture<Bitmap> generateStreakMilestoneCard(Goal goal) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createStreakMilestoneCard(goal);
            } catch (Exception e) {
                Log.e(TAG, "Error generating streak milestone card", e);
                throw new CompletionException(e);
            }
        });
    }
    
    /**
     * Generate daily summary card image
     */
    public CompletableFuture<Bitmap> generateDailySummaryCard(DayRecord dayRecord) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createDailySummaryCard(dayRecord);
            } catch (Exception e) {
                Log.e(TAG, "Error generating daily summary card", e);
                throw new CompletionException(e);
            }
        });
    }
    
    /**
     * Create achievement card bitmap
     */
    private Bitmap createAchievementCard(Achievement achievement) {
        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawCardBackground(canvas, achievement.getTier().name());
        
        // Draw achievement badge
        drawAchievementBadge(canvas, achievement);
        
        // Draw title
        drawCenteredText(canvas, achievement.getTitle(), TITLE_TEXT_SIZE, 
            TEXT_PRIMARY_COLOR, CARD_HEIGHT * 0.35f);
        
        // Draw tier
        drawCenteredText(canvas, achievement.getTierDisplayName() + " Tier", 
            SUBTITLE_TEXT_SIZE, getTierColor(achievement.getTier()), CARD_HEIGHT * 0.45f);
        
        // Draw description
        drawMultilineText(canvas, achievement.getDescription(), BODY_TEXT_SIZE, 
            TEXT_SECONDARY_COLOR, CARD_HEIGHT * 0.55f, CARD_WIDTH * 0.8f);
        
        // Draw achievement date
        String dateText = "Unlocked " + formatDate(achievement.getUnlockedAt());
        drawCenteredText(canvas, dateText, LABEL_TEXT_SIZE, TEXT_SECONDARY_COLOR, 
            CARD_HEIGHT * 0.85f);
        
        // Draw LocalLife branding
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Create level up card bitmap
     */
    private Bitmap createLevelUpCard(UserLevel userLevel) {
        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background with level-based gradient
        drawLevelUpBackground(canvas, userLevel.getCurrentLevel());
        
        // Draw level badge
        drawLevelBadge(canvas, userLevel);
        
        // Draw "LEVEL UP!" text
        drawCenteredText(canvas, "LEVEL UP!", TITLE_TEXT_SIZE, TEXT_PRIMARY_COLOR, 
            CARD_HEIGHT * 0.25f);
        
        // Draw level info
        String levelText = "Level " + userLevel.getCurrentLevel();
        drawCenteredText(canvas, levelText, SUBTITLE_TEXT_SIZE * 1.2f, 
            Color.parseColor(userLevel.getLevelColor()), CARD_HEIGHT * 0.4f);
        
        // Draw title
        drawCenteredText(canvas, userLevel.getCurrentTitle(), SUBTITLE_TEXT_SIZE, 
            TEXT_PRIMARY_COLOR, CARD_HEIGHT * 0.5f);
        
        // Draw stats
        drawStatsSection(canvas, userLevel);
        
        // Draw branding
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Create goal completion card bitmap
     */
    private Bitmap createGoalCompletionCard(Goal goal) {
        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawCardBackground(canvas, "GOAL");
        
        // Draw checkmark
        drawCheckmark(canvas);
        
        // Draw "GOAL ACHIEVED!" text
        drawCenteredText(canvas, "GOAL ACHIEVED!", TITLE_TEXT_SIZE, ACCENT_COLOR, 
            CARD_HEIGHT * 0.35f);
        
        // Draw goal title
        drawCenteredText(canvas, goal.getTitle(), SUBTITLE_TEXT_SIZE, TEXT_PRIMARY_COLOR, 
            CARD_HEIGHT * 0.5f);
        
        // Draw progress
        String progressText = goal.getFormattedProgress();
        drawCenteredText(canvas, progressText, BODY_TEXT_SIZE, TEXT_SECONDARY_COLOR, 
            CARD_HEIGHT * 0.6f);
        
        // Draw completion date
        String dateText = "Completed " + formatDate(goal.getLastCompletedDate());
        drawCenteredText(canvas, dateText, LABEL_TEXT_SIZE, TEXT_SECONDARY_COLOR, 
            CARD_HEIGHT * 0.85f);
        
        // Draw branding
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Create streak milestone card bitmap
     */
    private Bitmap createStreakMilestoneCard(Goal goal) {
        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background with fire theme
        drawStreakBackground(canvas);
        
        // Draw fire emoji or icon
        drawFireIcon(canvas);
        
        // Draw streak count
        String streakText = goal.getStreakCount() + " DAY";
        drawCenteredText(canvas, streakText, TITLE_TEXT_SIZE, Color.parseColor("#FF6B35"), 
            CARD_HEIGHT * 0.35f);
        
        // Draw "STREAK" text
        drawCenteredText(canvas, "STREAK!", SUBTITLE_TEXT_SIZE, TEXT_PRIMARY_COLOR, 
            CARD_HEIGHT * 0.45f);
        
        // Draw goal title
        drawMultilineText(canvas, goal.getTitle(), BODY_TEXT_SIZE, TEXT_SECONDARY_COLOR, 
            CARD_HEIGHT * 0.6f, CARD_WIDTH * 0.8f);
        
        // Draw branding
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Create daily summary card bitmap
     */
    private Bitmap createDailySummaryCard(DayRecord dayRecord) {
        Bitmap bitmap = Bitmap.createBitmap(CARD_WIDTH, SUMMARY_CARD_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw background
        drawSummaryBackground(canvas);
        
        // Draw title
        drawCenteredText(canvas, "Daily Summary", TITLE_TEXT_SIZE, TEXT_PRIMARY_COLOR, 
            SUMMARY_CARD_HEIGHT * 0.15f);
        
        // Draw date
        String dateText = formatDate(dayRecord.getDate());
        drawCenteredText(canvas, dateText, SUBTITLE_TEXT_SIZE, TEXT_SECONDARY_COLOR, 
            SUMMARY_CARD_HEIGHT * 0.25f);
        
        // Draw stats grid
        drawSummaryStats(canvas, dayRecord);
        
        // Draw branding
        drawBranding(canvas);
        
        return bitmap;
    }
    
    /**
     * Draw card background with gradient
     */
    private void drawCardBackground(Canvas canvas, String theme) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Create gradient based on theme
        LinearGradient gradient = createThemeGradient(theme);
        paint.setShader(gradient);
        
        // Draw rounded rectangle background
        RectF rect = new RectF(0, 0, CARD_WIDTH, CARD_HEIGHT);
        canvas.drawRoundRect(rect, 40, 40, paint);
        
        // Draw overlay
        paint.setColor(Color.parseColor("#AA000000"));
        paint.setShader(null);
        canvas.drawRoundRect(rect, 40, 40, paint);
    }
    
    /**
     * Create theme-based gradient
     */
    private LinearGradient createThemeGradient(String theme) {
        int[] colors;
        
        switch (theme) {
            case "BRONZE":
                colors = new int[]{Color.parseColor("#CD7F32"), Color.parseColor("#8B4513")};
                break;
            case "SILVER":
                colors = new int[]{Color.parseColor("#C0C0C0"), Color.parseColor("#808080")};
                break;
            case "GOLD":
                colors = new int[]{Color.parseColor("#FFD700"), Color.parseColor("#B8860B")};
                break;
            case "PLATINUM":
                colors = new int[]{Color.parseColor("#E5E4E2"), Color.parseColor("#A8A8A8")};
                break;
            case "DIAMOND":
                colors = new int[]{Color.parseColor("#B9F2FF"), Color.parseColor("#5DADE2")};
                break;
            case "LEGENDARY":
                colors = new int[]{Color.parseColor("#FF6B35"), Color.parseColor("#E74C3C")};
                break;
            default:
                colors = new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#2E7D32")};
                break;
        }
        
        return new LinearGradient(0, 0, 0, CARD_HEIGHT, colors, null, Shader.TileMode.CLAMP);
    }
    
    /**
     * Draw achievement badge
     */
    private void drawAchievementBadge(Canvas canvas, Achievement achievement) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        float centerX = CARD_WIDTH / 2f;
        float centerY = CARD_HEIGHT * 0.2f;
        float radius = 80f;
        
        // Draw badge background
        paint.setColor(Color.parseColor(achievement.getBadgeColor()));
        canvas.drawCircle(centerX, centerY, radius, paint);
        
        // Draw badge border
        paint.setColor(TEXT_PRIMARY_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        canvas.drawCircle(centerX, centerY, radius, paint);
        
        // Draw trophy icon (simplified)
        drawTrophyIcon(canvas, centerX, centerY, radius * 0.6f);
    }
    
    /**
     * Draw level badge
     */
    private void drawLevelBadge(Canvas canvas, UserLevel userLevel) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        float centerX = CARD_WIDTH / 2f;
        float centerY = CARD_HEIGHT * 0.15f;
        float radius = 100f;
        
        // Draw badge background
        paint.setColor(Color.parseColor(userLevel.getLevelColor()));
        canvas.drawCircle(centerX, centerY, radius, paint);
        
        // Draw level number
        paint.setColor(TEXT_PRIMARY_COLOR);
        paint.setTextSize(SUBTITLE_TEXT_SIZE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        
        String levelText = String.valueOf(userLevel.getCurrentLevel());
        canvas.drawText(levelText, centerX, centerY + paint.getTextSize() / 3, paint);
    }
    
    /**
     * Draw checkmark for goal completion
     */
    private void drawCheckmark(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(ACCENT_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(16f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        
        float centerX = CARD_WIDTH / 2f;
        float centerY = CARD_HEIGHT * 0.2f;
        float size = 60f;
        
        // Draw checkmark path
        Path checkPath = new Path();
        checkPath.moveTo(centerX - size, centerY);
        checkPath.lineTo(centerX - size/3, centerY + size/2);
        checkPath.lineTo(centerX + size, centerY - size/2);
        
        canvas.drawPath(checkPath, paint);
    }
    
    /**
     * Draw fire icon for streak
     */
    private void drawFireIcon(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        float centerX = CARD_WIDTH / 2f;
        float centerY = CARD_HEIGHT * 0.2f;
        
        // Draw flame shape (simplified)
        paint.setColor(Color.parseColor("#FF6B35"));
        Path flamePath = new Path();
        flamePath.moveTo(centerX, centerY + 60);
        flamePath.quadTo(centerX - 30, centerY + 20, centerX - 20, centerY - 20);
        flamePath.quadTo(centerX - 10, centerY - 40, centerX, centerY - 60);
        flamePath.quadTo(centerX + 10, centerY - 40, centerX + 20, centerY - 20);
        flamePath.quadTo(centerX + 30, centerY + 20, centerX, centerY + 60);
        
        canvas.drawPath(flamePath, paint);
    }
    
    /**
     * Draw trophy icon (simplified)
     */
    private void drawTrophyIcon(Canvas canvas, float centerX, float centerY, float size) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(TEXT_PRIMARY_COLOR);
        
        // Draw trophy cup (simplified rectangle)
        RectF cupRect = new RectF(centerX - size/2, centerY - size/3, 
            centerX + size/2, centerY + size/3);
        canvas.drawRoundRect(cupRect, 10, 10, paint);
        
        // Draw trophy base
        RectF baseRect = new RectF(centerX - size/3, centerY + size/3, 
            centerX + size/3, centerY + size/2);
        canvas.drawRect(baseRect, paint);
    }
    
    /**
     * Draw centered text
     */
    private void drawCenteredText(Canvas canvas, String text, float textSize, 
                                int color, float y) {
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText(text, CARD_WIDTH / 2f, y, paint);
    }
    
    /**
     * Draw multiline text
     */
    private void drawMultilineText(Canvas canvas, String text, float textSize, 
                                 int color, float y, float maxWidth) {
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        
        // Simple text wrapping
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        float currentY = y;
        
        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            } else {
                if (currentLine.length() > 0) {
                    canvas.drawText(currentLine.toString(), CARD_WIDTH / 2f, currentY, paint);
                    currentY += textSize * 1.2f;
                    currentLine = new StringBuilder(word);
                } else {
                    canvas.drawText(word, CARD_WIDTH / 2f, currentY, paint);
                    currentY += textSize * 1.2f;
                }
            }
        }
        
        if (currentLine.length() > 0) {
            canvas.drawText(currentLine.toString(), CARD_WIDTH / 2f, currentY, paint);
        }
    }
    
    /**
     * Draw stats section for level up
     */
    private void drawStatsSection(Canvas canvas, UserLevel userLevel) {
        float startY = CARD_HEIGHT * 0.65f;
        float spacing = 60f;
        
        // Draw stats
        drawStatItem(canvas, "Total XP", String.format(Locale.getDefault(), "%,d", userLevel.getTotalXP()), 
            startY);
        drawStatItem(canvas, "Achievements", String.valueOf(userLevel.getAchievementsUnlocked()), 
            startY + spacing);
        drawStatItem(canvas, "Goals Completed", String.valueOf(userLevel.getGoalsCompleted()), 
            startY + spacing * 2);
    }
    
    /**
     * Draw individual stat item
     */
    private void drawStatItem(Canvas canvas, String label, String value, float y) {
        TextPaint labelPaint = new TextPaint();
        labelPaint.setAntiAlias(true);
        labelPaint.setColor(TEXT_SECONDARY_COLOR);
        labelPaint.setTextSize(LABEL_TEXT_SIZE);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        
        TextPaint valuePaint = new TextPaint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(TEXT_PRIMARY_COLOR);
        valuePaint.setTextSize(BODY_TEXT_SIZE);
        valuePaint.setTextAlign(Paint.Align.RIGHT);
        valuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        
        float margin = CARD_WIDTH * 0.1f;
        canvas.drawText(label, margin, y, labelPaint);
        canvas.drawText(value, CARD_WIDTH - margin, y, valuePaint);
    }
    
    /**
     * Draw summary stats for daily summary
     */
    private void drawSummaryStats(Canvas canvas, DayRecord dayRecord) {
        float startY = SUMMARY_CARD_HEIGHT * 0.4f;
        float spacing = 70f;
        
        // Draw key stats
        drawStatItem(canvas, "Steps", String.format(Locale.getDefault(), "%,d", dayRecord.getStepCount()), 
            startY);
        drawStatItem(canvas, "Places Visited", String.valueOf(dayRecord.getPlacesVisited()), 
            startY + spacing);
        drawStatItem(canvas, "Photos Taken", String.valueOf(dayRecord.getPhotoCount()), 
            startY + spacing * 2);
        drawStatItem(canvas, "Activity Score", String.valueOf(Math.round(dayRecord.getActivityScore())), 
            startY + spacing * 3);
    }
    
    /**
     * Draw branding
     */
    private void drawBranding(Canvas canvas) {
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setColor(TEXT_SECONDARY_COLOR);
        paint.setTextSize(LABEL_TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);
        
        float y = canvas.getHeight() - 40f;
        canvas.drawText("LocalLife", CARD_WIDTH / 2f, y, paint);
    }
    
    /**
     * Draw level up background
     */
    private void drawLevelUpBackground(Canvas canvas, int level) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Create level-based gradient
        String levelColor = UserLevel.getTitleForLevel(level);
        LinearGradient gradient = createThemeGradient(levelColor);
        paint.setShader(gradient);
        
        RectF rect = new RectF(0, 0, CARD_WIDTH, CARD_HEIGHT);
        canvas.drawRoundRect(rect, 40, 40, paint);
        
        // Draw overlay
        paint.setColor(Color.parseColor("#AA000000"));
        paint.setShader(null);
        canvas.drawRoundRect(rect, 40, 40, paint);
    }
    
    /**
     * Draw streak background
     */
    private void drawStreakBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Fire gradient
        int[] colors = {Color.parseColor("#FF6B35"), Color.parseColor("#E74C3C"), 
            Color.parseColor("#8B0000")};
        LinearGradient gradient = new LinearGradient(0, 0, 0, CARD_HEIGHT, colors, null, 
            Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        
        RectF rect = new RectF(0, 0, CARD_WIDTH, CARD_HEIGHT);
        canvas.drawRoundRect(rect, 40, 40, paint);
        
        // Draw overlay
        paint.setColor(Color.parseColor("#AA000000"));
        paint.setShader(null);
        canvas.drawRoundRect(rect, 40, 40, paint);
    }
    
    /**
     * Draw summary background
     */
    private void drawSummaryBackground(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        
        // Simple gradient
        int[] colors = {Color.parseColor("#2E7D32"), Color.parseColor("#1B5E20")};
        LinearGradient gradient = new LinearGradient(0, 0, 0, SUMMARY_CARD_HEIGHT, colors, null, 
            Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        
        RectF rect = new RectF(0, 0, CARD_WIDTH, SUMMARY_CARD_HEIGHT);
        canvas.drawRoundRect(rect, 40, 40, paint);
        
        // Draw overlay
        paint.setColor(Color.parseColor("#AA000000"));
        paint.setShader(null);
        canvas.drawRoundRect(rect, 40, 40, paint);
    }
    
    /**
     * Get tier color
     */
    private int getTierColor(Achievement.AchievementTier tier) {
        switch (tier) {
            case BRONZE: return Color.parseColor("#CD7F32");
            case SILVER: return Color.parseColor("#C0C0C0");
            case GOLD: return Color.parseColor("#FFD700");
            case PLATINUM: return Color.parseColor("#E5E4E2");
            case DIAMOND: return Color.parseColor("#B9F2FF");
            case LEGENDARY: return Color.parseColor("#FF6B35");
            default: return ACCENT_COLOR;
        }
    }
    
    /**
     * Format date for display
     */
    private String formatDate(Date date) {
        if (date == null) return "Today";
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
    }
    
    /**
     * Format date string
     */
    private String formatDate(String dateString) {
        if (dateString == null) return "Today";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }
}