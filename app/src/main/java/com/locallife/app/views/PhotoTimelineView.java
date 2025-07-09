package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.locallife.app.R;
import com.locallife.model.PhotoMetadata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Custom view that displays a timeline of photos taken throughout the day
 * Shows photo activity patterns and highlights peak photography hours
 */
public class PhotoTimelineView extends View {
    
    private Paint timelinePaint;
    private Paint photoPointPaint;
    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint gridPaint;
    private Paint activityBandPaint;
    
    private List<PhotoMetadata> photoMetadata;
    private int viewWidth;
    private int viewHeight;
    private int timelineMargin;
    private int timelineHeight;
    private int photoPointRadius;
    
    // Timeline configuration
    private static final int HOURS_IN_DAY = 24;
    private static final int TIMELINE_PADDING = 40;
    private static final int TEXT_SIZE = 24;
    private static final int POINT_RADIUS = 8;
    private static final int ACTIVITY_BAND_HEIGHT = 20;
    
    // Colors
    private int primaryColor;
    private int secondaryColor;
    private int backgroundColor;
    private int textColor;
    private int gridColor;
    private int photoPointColor;
    private int activityBandColor;
    
    public PhotoTimelineView(Context context) {
        super(context);
        init();
    }
    
    public PhotoTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PhotoTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        photoMetadata = new ArrayList<>();
        
        // Initialize colors
        primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        secondaryColor = ContextCompat.getColor(getContext(), R.color.secondary);
        backgroundColor = ContextCompat.getColor(getContext(), R.color.background);
        textColor = ContextCompat.getColor(getContext(), R.color.text);
        gridColor = ContextCompat.getColor(getContext(), R.color.surface);
        photoPointColor = ContextCompat.getColor(getContext(), R.color.accent);
        activityBandColor = ContextCompat.getColor(getContext(), R.color.success);
        
        // Initialize paints
        timelinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timelinePaint.setColor(primaryColor);
        timelinePaint.setStrokeWidth(4);
        timelinePaint.setStyle(Paint.Style.STROKE);
        
        photoPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        photoPointPaint.setColor(photoPointColor);
        photoPointPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(1);
        gridPaint.setStyle(Paint.Style.STROKE);
        
        activityBandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activityBandPaint.setColor(activityBandColor);
        activityBandPaint.setStyle(Paint.Style.FILL);
        
        timelineMargin = TIMELINE_PADDING;
        photoPointRadius = POINT_RADIUS;
    }
    
    /**
     * Set the photo metadata to display on the timeline
     */
    public void setPhotoMetadata(List<PhotoMetadata> metadata) {
        this.photoMetadata = metadata != null ? metadata : new ArrayList<>();
        invalidate();
    }
    
    /**
     * Add a single photo metadata entry to the timeline
     */
    public void addPhotoMetadata(PhotoMetadata metadata) {
        if (metadata != null) {
            photoMetadata.add(metadata);
            invalidate();
        }
    }
    
    /**
     * Clear all photo metadata from the timeline
     */
    public void clearPhotoMetadata() {
        photoMetadata.clear();
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        timelineHeight = h - (timelineMargin * 2) - 40; // Leave space for text
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (viewWidth == 0 || viewHeight == 0) {
            return;
        }
        
        // Draw background
        canvas.drawRect(0, 0, viewWidth, viewHeight, backgroundPaint);
        
        // Draw grid lines for hours
        drawTimeGrid(canvas);
        
        // Draw activity bands
        drawActivityBands(canvas);
        
        // Draw main timeline
        drawTimeline(canvas);
        
        // Draw photo points
        drawPhotoPoints(canvas);
        
        // Draw hour labels
        drawHourLabels(canvas);
        
        // Draw statistics
        drawStatistics(canvas);
    }
    
    private void drawTimeGrid(Canvas canvas) {
        int timelineWidth = viewWidth - (timelineMargin * 2);
        float hourWidth = timelineWidth / (float) HOURS_IN_DAY;
        
        // Draw vertical grid lines for each hour
        for (int hour = 0; hour <= HOURS_IN_DAY; hour++) {
            float x = timelineMargin + (hour * hourWidth);
            canvas.drawLine(x, timelineMargin, x, timelineMargin + timelineHeight, gridPaint);
        }
        
        // Draw horizontal grid lines
        canvas.drawLine(timelineMargin, timelineMargin, viewWidth - timelineMargin, timelineMargin, gridPaint);
        canvas.drawLine(timelineMargin, timelineMargin + timelineHeight, viewWidth - timelineMargin, timelineMargin + timelineHeight, gridPaint);
    }
    
    private void drawActivityBands(Canvas canvas) {
        if (photoMetadata.isEmpty()) {
            return;
        }
        
        // Calculate photo activity by hour
        int[] hourCounts = new int[HOURS_IN_DAY];
        for (PhotoMetadata photo : photoMetadata) {
            if (photo.getDateTaken() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(photo.getDateTaken());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                hourCounts[hour]++;
            }
        }
        
        // Find maximum count for normalization
        int maxCount = 0;
        for (int count : hourCounts) {
            maxCount = Math.max(maxCount, count);
        }
        
        if (maxCount == 0) {
            return;
        }
        
        // Draw activity bands
        int timelineWidth = viewWidth - (timelineMargin * 2);
        float hourWidth = timelineWidth / (float) HOURS_IN_DAY;
        
        for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
            if (hourCounts[hour] > 0) {
                float intensity = hourCounts[hour] / (float) maxCount;
                int alpha = (int) (255 * intensity * 0.6f); // Make it semi-transparent
                
                activityBandPaint.setAlpha(alpha);
                
                float x = timelineMargin + (hour * hourWidth);
                float bandHeight = ACTIVITY_BAND_HEIGHT * intensity;
                
                RectF bandRect = new RectF(
                    x + 2, 
                    timelineMargin + timelineHeight - bandHeight,
                    x + hourWidth - 2,
                    timelineMargin + timelineHeight
                );
                
                canvas.drawRoundRect(bandRect, 4, 4, activityBandPaint);
            }
        }
    }
    
    private void drawTimeline(Canvas canvas) {
        float y = timelineMargin + (timelineHeight / 2);
        canvas.drawLine(timelineMargin, y, viewWidth - timelineMargin, y, timelinePaint);
    }
    
    private void drawPhotoPoints(Canvas canvas) {
        if (photoMetadata.isEmpty()) {
            return;
        }
        
        int timelineWidth = viewWidth - (timelineMargin * 2);
        float timelineY = timelineMargin + (timelineHeight / 2);
        
        for (PhotoMetadata photo : photoMetadata) {
            if (photo.getDateTaken() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(photo.getDateTaken());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                
                // Calculate position on timeline
                float hourFraction = (hour + minute / 60f) / HOURS_IN_DAY;
                float x = timelineMargin + (hourFraction * timelineWidth);
                
                // Vary point size based on activity score
                float pointSize = photoPointRadius + (photo.getActivityScore() / 100f) * photoPointRadius;
                
                // Set color based on activity type
                if (\"outdoor\".equals(photo.getActivityType())) {
                    photoPointPaint.setColor(ContextCompat.getColor(getContext(), R.color.success));
                } else if (\"social\".equals(photo.getActivityType())) {
                    photoPointPaint.setColor(ContextCompat.getColor(getContext(), R.color.warning));
                } else if (\"travel\".equals(photo.getActivityType())) {
                    photoPointPaint.setColor(ContextCompat.getColor(getContext(), R.color.info));
                } else {
                    photoPointPaint.setColor(photoPointColor);
                }
                
                // Draw photo point
                canvas.drawCircle(x, timelineY, pointSize, photoPointPaint);
                
                // Draw location indicator if available
                if (photo.hasLocationData()) {
                    photoPointPaint.setColor(primaryColor);
                    canvas.drawCircle(x, timelineY, pointSize / 2, photoPointPaint);
                }
            }
        }
    }
    
    private void drawHourLabels(Canvas canvas) {
        int timelineWidth = viewWidth - (timelineMargin * 2);
        float hourWidth = timelineWidth / (float) HOURS_IN_DAY;
        float labelY = timelineMargin + timelineHeight + 30;
        
        // Draw labels for every 3 hours
        for (int hour = 0; hour < HOURS_IN_DAY; hour += 3) {
            float x = timelineMargin + (hour * hourWidth);
            String label = String.format(Locale.getDefault(), \"%02d:00\", hour);
            canvas.drawText(label, x, labelY, textPaint);
        }
    }
    
    private void drawStatistics(Canvas canvas) {
        if (photoMetadata.isEmpty()) {
            return;
        }
        
        // Calculate statistics
        int totalPhotos = photoMetadata.size();
        int photosWithLocation = 0;
        int outdoorPhotos = 0;
        int socialPhotos = 0;
        
        for (PhotoMetadata photo : photoMetadata) {
            if (photo.hasLocationData()) {
                photosWithLocation++;
            }
            if (photo.isOutdoor()) {
                outdoorPhotos++;
            }
            if (photo.hasPeople()) {
                socialPhotos++;
            }
        }
        
        // Draw statistics text
        textPaint.setTextSize(18);
        textPaint.setTextAlign(Paint.Align.LEFT);
        
        String statsText = String.format(Locale.getDefault(), 
            \"Photos: %d | With Location: %d | Outdoor: %d | Social: %d\",
            totalPhotos, photosWithLocation, outdoorPhotos, socialPhotos);
        
        canvas.drawText(statsText, timelineMargin, 20, textPaint);
        
        // Reset text paint
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    /**
     * Get the time range (in hours) that has the most photo activity
     */
    public String getPeakActivityHours() {
        if (photoMetadata.isEmpty()) {
            return \"No photos\";
        }
        
        int[] hourCounts = new int[HOURS_IN_DAY];
        for (PhotoMetadata photo : photoMetadata) {
            if (photo.getDateTaken() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(photo.getDateTaken());
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                hourCounts[hour]++;
            }
        }
        
        // Find peak hours
        int maxCount = 0;
        int peakHour = 0;
        for (int hour = 0; hour < HOURS_IN_DAY; hour++) {
            if (hourCounts[hour] > maxCount) {
                maxCount = hourCounts[hour];
                peakHour = hour;
            }
        }
        
        if (maxCount == 0) {
            return \"No photos\";
        }
        
        return String.format(Locale.getDefault(), \"%02d:00 - %02d:00 (%d photos)\", 
            peakHour, (peakHour + 1) % 24, maxCount);
    }
    
    /**
     * Get the percentage of photos that have location data
     */
    public float getLocationDataPercentage() {
        if (photoMetadata.isEmpty()) {
            return 0f;
        }
        
        int photosWithLocation = 0;
        for (PhotoMetadata photo : photoMetadata) {
            if (photo.hasLocationData()) {
                photosWithLocation++;
            }
        }
        
        return (photosWithLocation / (float) photoMetadata.size()) * 100f;
    }
    
    /**
     * Get the average activity score of all photos
     */
    public float getAverageActivityScore() {
        if (photoMetadata.isEmpty()) {
            return 0f;
        }
        
        int totalScore = 0;
        for (PhotoMetadata photo : photoMetadata) {
            totalScore += photo.getActivityScore();
        }
        
        return totalScore / (float) photoMetadata.size();
    }
}