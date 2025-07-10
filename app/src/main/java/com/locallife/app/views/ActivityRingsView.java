package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view that displays activity data as Apple Watch-style rings
 * Shows multiple metrics in concentric circles with smooth animations
 */
public class ActivityRingsView extends View {
    private static final String TAG = "ActivityRingsView";
    
    // Ring configuration
    private static final int RING_COUNT = 4;
    private static final float RING_STROKE_WIDTH = 12f;
    private static final float RING_SPACING = 20f;
    private static final float START_ANGLE = -90f; // Start at top
    
    // Colors for different rings
    private static final int[] RING_COLORS = {
        Color.parseColor("#FF6B35"), // Steps - Orange
        Color.parseColor("#4ECDC4"), // Places - Teal
        Color.parseColor("#45B7D1"), // Screen Time - Blue
        Color.parseColor("#96CEB4")  // Media - Green
    };
    
    private static final int BACKGROUND_COLOR = Color.parseColor("#1A1A1A");
    private static final int BACKGROUND_RING_COLOR = Color.parseColor("#2A2A2A");
    
    private Paint ringPaint;
    private Paint backgroundRingPaint;
    private Paint textPaint;
    private Paint centerTextPaint;
    private RectF ringRect;
    
    private List<RingData> rings;
    private String centerText = "85%";
    private String centerSubtext = "Overall";
    
    // Animation properties
    private float[] animationProgress;
    private long animationDuration = 2000; // 2 seconds
    private long animationStartTime = 0;
    private boolean isAnimating = false;
    
    public ActivityRingsView(Context context) {
        super(context);
        init();
    }
    
    public ActivityRingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ActivityRingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(RING_STROKE_WIDTH);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        
        backgroundRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundRingPaint.setStyle(Paint.Style.STROKE);
        backgroundRingPaint.setStrokeWidth(RING_STROKE_WIDTH);
        backgroundRingPaint.setColor(BACKGROUND_RING_COLOR);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextPaint.setColor(Color.WHITE);
        centerTextPaint.setTextSize(36f);
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        
        ringRect = new RectF();
        rings = new ArrayList<>();
        animationProgress = new float[RING_COUNT];
        
        // Initialize with sample data
        setupSampleData();
    }
    
    private void setupSampleData() {
        rings.clear();
        rings.add(new RingData("Steps", 0.65f, "6,543", "10,000 goal"));
        rings.add(new RingData("Places", 0.40f, "8", "visited"));
        rings.add(new RingData("Screen Time", 0.87f, "4h 23m", "today"));
        rings.add(new RingData("Media", 0.45f, "2h 15m", "consumed"));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Fill background
        canvas.drawColor(BACKGROUND_COLOR);
        
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Calculate ring sizes
        float maxRadius = Math.min(width, height) / 2f - 50f;
        float ringSpacing = (maxRadius - 50f) / RING_COUNT;
        
        // Update animation progress
        if (isAnimating) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - animationStartTime;
            float progress = Math.min(1f, elapsed / (float) animationDuration);
            
            // Easing function for smooth animation
            progress = easeInOutCubic(progress);
            
            for (int i = 0; i < RING_COUNT && i < rings.size(); i++) {
                animationProgress[i] = progress * rings.get(i).progress;
            }
            
            if (elapsed >= animationDuration) {
                isAnimating = false;
            }
            
            invalidate(); // Continue animation
        }
        
        // Draw rings from outside to inside
        for (int i = 0; i < RING_COUNT && i < rings.size(); i++) {
            RingData ring = rings.get(i);
            float radius = maxRadius - (i * ringSpacing);
            
            // Set up ring bounds
            ringRect.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            );
            
            // Draw background ring
            canvas.drawCircle(centerX, centerY, radius, backgroundRingPaint);
            
            // Draw progress ring
            ringPaint.setColor(RING_COLORS[i]);
            
            float sweepAngle = 360f * (isAnimating ? animationProgress[i] : ring.progress);
            canvas.drawArc(ringRect, START_ANGLE, sweepAngle, false, ringPaint);
            
            // Draw progress indicators (dots)
            if (ring.progress > 0) {
                drawProgressDot(canvas, centerX, centerY, radius, sweepAngle, RING_COLORS[i]);
            }
        }
        
        // Draw center text
        float centerTextY = centerY - 10f;
        canvas.drawText(centerText, centerX, centerTextY, centerTextPaint);
        
        Paint subtextPaint = new Paint(textPaint);
        subtextPaint.setTextSize(18f);
        subtextPaint.setColor(Color.GRAY);
        canvas.drawText(centerSubtext, centerX, centerTextY + 30f, subtextPaint);
        
        // Draw ring labels
        drawRingLabels(canvas, centerX, centerY, maxRadius, ringSpacing);
    }
    
    private void drawProgressDot(Canvas canvas, float centerX, float centerY, float radius, float sweepAngle, int color) {
        // Calculate dot position at the end of the arc
        double angleRad = Math.toRadians(START_ANGLE + sweepAngle);
        float dotX = centerX + (float) (radius * Math.cos(angleRad));
        float dotY = centerY + (float) (radius * Math.sin(angleRad));
        
        Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(color);
        dotPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawCircle(dotX, dotY, 8f, dotPaint);
        
        // Add white center
        dotPaint.setColor(Color.WHITE);
        canvas.drawCircle(dotX, dotY, 4f, dotPaint);
    }
    
    private void drawRingLabels(Canvas canvas, float centerX, float centerY, float maxRadius, float ringSpacing) {
        for (int i = 0; i < RING_COUNT && i < rings.size(); i++) {
            RingData ring = rings.get(i);
            float radius = maxRadius - (i * ringSpacing);
            
            // Position label to the right of the ring
            float labelX = centerX + radius + 30f;
            float labelY = centerY + (i * 25f) - (RING_COUNT * 12.5f);
            
            // Draw color indicator
            Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            colorPaint.setColor(RING_COLORS[i]);
            canvas.drawCircle(labelX - 20f, labelY - 5f, 8f, colorPaint);
            
            // Draw label text
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(16f);
            canvas.drawText(ring.label, labelX, labelY, textPaint);
            
            // Draw value
            textPaint.setColor(RING_COLORS[i]);
            textPaint.setTextSize(14f);
            canvas.drawText(ring.value, labelX, labelY + 18f, textPaint);
            
            // Draw subtitle
            textPaint.setColor(Color.GRAY);
            textPaint.setTextSize(12f);
            canvas.drawText(ring.subtitle, labelX, labelY + 32f, textPaint);
        }
    }
    
    private float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4f * t * t * t;
        } else {
            float p = 2f * t - 2f;
            return 1f + p * p * p / 2f;
        }
    }
    
    public void setRingData(List<RingData> rings) {
        this.rings = new ArrayList<>(rings);
        
        // Calculate overall progress for center text
        float totalProgress = 0f;
        for (RingData ring : rings) {
            totalProgress += ring.progress;
        }
        float averageProgress = totalProgress / rings.size();
        centerText = String.format("%.0f%%", averageProgress * 100);
        
        startAnimation();
    }
    
    public void setCenterText(String text, String subtext) {
        this.centerText = text;
        this.centerSubtext = subtext;
        invalidate();
    }
    
    public void startAnimation() {
        isAnimating = true;
        animationStartTime = System.currentTimeMillis();
        
        // Reset animation progress
        for (int i = 0; i < animationProgress.length; i++) {
            animationProgress[i] = 0f;
        }
        
        invalidate();
    }
    
    public void setAnimationDuration(long duration) {
        this.animationDuration = duration;
    }
    
    // Data class for ring information
    public static class RingData {
        public final String label;
        public final float progress; // 0.0 to 1.0
        public final String value;
        public final String subtitle;
        
        public RingData(String label, float progress, String value, String subtitle) {
            this.label = label;
            this.progress = Math.max(0f, Math.min(1f, progress)); // Clamp to 0-1
            this.value = value;
            this.subtitle = subtitle;
        }
    }
}