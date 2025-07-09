package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.locallife.app.R;

public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rectF;
    
    private float progress = 0f;
    private int maxProgress = 100;
    private String centerText = "";
    private int strokeWidth = 20;
    private int progressColor;
    private int backgroundColor;
    private int textColor;
    
    public CircularProgressView(Context context) {
        super(context);
        init();
    }
    
    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        progressColor = ContextCompat.getColor(getContext(), R.color.primary);
        backgroundColor = ContextCompat.getColor(getContext(), R.color.divider);
        textColor = ContextCompat.getColor(getContext(), R.color.primary_text);
        
        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        
        progressPaint = new Paint();
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(dpToPx(16));
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        rectF = new RectF();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height);
        
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        
        int contentWidth = width - paddingLeft - paddingRight;
        int contentHeight = height - paddingTop - paddingBottom;
        
        int radius = Math.min(contentWidth, contentHeight) / 2;
        int centerX = paddingLeft + contentWidth / 2;
        int centerY = paddingTop + contentHeight / 2;
        
        // Set up the bounds for the arc
        int arcRadius = radius - strokeWidth / 2;
        rectF.set(centerX - arcRadius, centerY - arcRadius, centerX + arcRadius, centerY + arcRadius);
        
        // Draw background circle
        canvas.drawCircle(centerX, centerY, arcRadius, backgroundPaint);
        
        // Draw progress arc
        float sweepAngle = (progress / maxProgress) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
        
        // Draw center text
        if (!centerText.isEmpty()) {
            float textY = centerY + (textPaint.descent() + textPaint.ascent()) / 2;
            canvas.drawText(centerText, centerX, textY, textPaint);
        }
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }
    
    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(progress, maxProgress));
        invalidate();
    }
    
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }
    
    public void setCenterText(String text) {
        this.centerText = text;
        invalidate();
    }
    
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }
    
    public void setStrokeWidth(int width) {
        this.strokeWidth = dpToPx(width);
        backgroundPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeWidth(strokeWidth);
        invalidate();
    }
    
    public void animateProgress(float toProgress, long duration) {
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(progress, toProgress);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            setProgress(animatedValue);
        });
        animator.start();
    }
}