package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.locallife.app.R;

public class StatsCardView extends LinearLayout {
    private TextView tvTitle;
    private TextView tvValue;
    private TextView tvSubtitle;
    private ImageView ivIcon;
    private View progressBar;
    private LinearLayout progressContainer;
    private CircularProgressView circularProgress;
    
    private String title;
    private String value;
    private String subtitle;
    private int iconResId;
    private float progress;
    private int progressColor;
    private boolean showCircularProgress = false;
    
    public StatsCardView(Context context) {
        super(context);
        init();
    }
    
    public StatsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StatsCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        inflate(getContext(), R.layout.view_stats_card, this);
        
        tvTitle = findViewById(R.id.tv_title);
        tvValue = findViewById(R.id.tv_value);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        ivIcon = findViewById(R.id.iv_icon);
        progressBar = findViewById(R.id.progress_bar);
        progressContainer = findViewById(R.id.progress_container);
        circularProgress = findViewById(R.id.circular_progress);
        
        // Set default values
        progressColor = ContextCompat.getColor(getContext(), R.color.primary);
        
        // Set default background
        setBackgroundResource(R.drawable.card_background);
        setElevation(dpToPx(4));
    }
    
    public void setTitle(String title) {
        this.title = title;
        tvTitle.setText(title);
    }
    
    public void setValue(String value) {
        this.value = value;
        tvValue.setText(value);
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        tvSubtitle.setText(subtitle);
        tvSubtitle.setVisibility(subtitle != null && !subtitle.isEmpty() ? VISIBLE : GONE);
    }
    
    public void setIcon(int iconResId) {
        this.iconResId = iconResId;
        ivIcon.setImageResource(iconResId);
    }
    
    public void setProgress(float progress) {
        this.progress = progress;
        updateProgressBar();
    }
    
    public void setProgressColor(int color) {
        this.progressColor = color;
        updateProgressBar();
    }
    
    public void setShowCircularProgress(boolean show) {
        this.showCircularProgress = show;
        if (show) {
            circularProgress.setVisibility(VISIBLE);
            progressContainer.setVisibility(GONE);
        } else {
            circularProgress.setVisibility(GONE);
            progressContainer.setVisibility(VISIBLE);
        }
    }
    
    private void updateProgressBar() {
        if (showCircularProgress) {
            circularProgress.setProgress(progress);
            circularProgress.setProgressColor(progressColor);
        } else {
            // Update linear progress bar
            ProgressBarView progressView = (ProgressBarView) progressBar;
            progressView.setProgress(progress);
            progressView.setProgressColor(progressColor);
        }
    }
    
    public void animateValue(String fromValue, String toValue, long duration) {
        // Simple text animation
        tvValue.setAlpha(0.5f);
        tvValue.animate()
            .alpha(1.0f)
            .setDuration(duration)
            .withEndAction(() -> tvValue.setText(toValue))
            .start();
    }
    
    public void animateProgress(float toProgress, long duration) {
        if (showCircularProgress) {
            circularProgress.animateProgress(toProgress, duration);
        } else {
            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(progress, toProgress);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                float animatedValue = (float) animation.getAnimatedValue();
                setProgress(animatedValue);
            });
            animator.start();
        }
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }
    
    // Custom progress bar view for linear progress
    public static class ProgressBarView extends View {
        private Paint backgroundPaint;
        private Paint progressPaint;
        private RectF rectF;
        
        private float progress = 0f;
        private int progressColor;
        private int backgroundColor;
        private int cornerRadius = 8;
        
        public ProgressBarView(Context context) {
            super(context);
            init();
        }
        
        public ProgressBarView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }
        
        private void init() {
            progressColor = ContextCompat.getColor(getContext(), R.color.primary);
            backgroundColor = ContextCompat.getColor(getContext(), R.color.divider);
            
            backgroundPaint = new Paint();
            backgroundPaint.setColor(backgroundColor);
            backgroundPaint.setAntiAlias(true);
            
            progressPaint = new Paint();
            progressPaint.setColor(progressColor);
            progressPaint.setAntiAlias(true);
            
            rectF = new RectF();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            int width = getWidth();
            int height = getHeight();
            
            // Draw background
            rectF.set(0, 0, width, height);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, backgroundPaint);
            
            // Draw progress
            float progressWidth = (progress / 100f) * width;
            rectF.set(0, 0, progressWidth, height);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, progressPaint);
        }
        
        public void setProgress(float progress) {
            this.progress = Math.max(0, Math.min(progress, 100));
            invalidate();
        }
        
        public void setProgressColor(int color) {
            this.progressColor = color;
            progressPaint.setColor(color);
            invalidate();
        }
    }
}