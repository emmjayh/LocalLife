package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.locallife.app.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ActivityHeatMapView extends View {
    private Paint cellPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private RectF cellRect;
    
    private static final int CELL_SIZE = 40;
    private static final int CELL_SPACING = 4;
    private static final int WEEKS_TO_SHOW = 52;
    private static final int DAYS_IN_WEEK = 7;
    
    private Map<String, Integer> activityData = new HashMap<>();
    private int[] heatMapColors;
    private OnDateClickListener dateClickListener;
    
    public interface OnDateClickListener {
        void onDateClick(String date, int activityLevel);
    }
    
    public ActivityHeatMapView(Context context) {
        super(context);
        init();
    }
    
    public ActivityHeatMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ActivityHeatMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        cellPaint = new Paint();
        cellPaint.setAntiAlias(true);
        cellPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(dpToPx(10));
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.secondary_text));
        
        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.divider));
        
        cellRect = new RectF();
        
        // Initialize heat map colors (from light to dark)
        heatMapColors = new int[]{
            ContextCompat.getColor(getContext(), R.color.background),
            ContextCompat.getColor(getContext(), R.color.primary_light),
            ContextCompat.getColor(getContext(), R.color.primary),
            ContextCompat.getColor(getContext(), R.color.primary_dark),
            ContextCompat.getColor(getContext(), R.color.accent)
        };
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = WEEKS_TO_SHOW * (CELL_SIZE + CELL_SPACING) + CELL_SPACING;
        int height = DAYS_IN_WEEK * (CELL_SIZE + CELL_SPACING) + CELL_SPACING + dpToPx(40); // Extra space for labels
        
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -WEEKS_TO_SHOW);
        
        // Draw day labels
        String[] dayLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < dayLabels.length; i++) {
            float y = CELL_SPACING + i * (CELL_SIZE + CELL_SPACING) + CELL_SIZE / 2 + dpToPx(4);
            canvas.drawText(dayLabels[i], dpToPx(30), y, textPaint);
        }
        
        // Draw heat map cells
        for (int week = 0; week < WEEKS_TO_SHOW; week++) {
            for (int day = 0; day < DAYS_IN_WEEK; day++) {
                int x = dpToPx(40) + week * (CELL_SIZE + CELL_SPACING);
                int y = CELL_SPACING + day * (CELL_SIZE + CELL_SPACING);
                
                // Get activity level for this date
                String dateKey = getDateKey(calendar);
                int activityLevel = activityData.getOrDefault(dateKey, 0);
                
                // Set cell color based on activity level
                int colorIndex = Math.min(activityLevel, heatMapColors.length - 1);
                cellPaint.setColor(heatMapColors[colorIndex]);
                
                // Draw cell
                cellRect.set(x, y, x + CELL_SIZE, y + CELL_SIZE);
                canvas.drawRoundRect(cellRect, 4, 4, cellPaint);
                
                // Draw border
                canvas.drawRoundRect(cellRect, 4, 4, gridPaint);
                
                // Move to next day
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            // Reset to start of next week
            calendar.add(Calendar.DAY_OF_YEAR, -DAYS_IN_WEEK);
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && dateClickListener != null) {
            float x = event.getX();
            float y = event.getY();
            
            // Calculate which cell was touched
            int cellX = (int) ((x - dpToPx(40)) / (CELL_SIZE + CELL_SPACING));
            int cellY = (int) ((y - CELL_SPACING) / (CELL_SIZE + CELL_SPACING));
            
            if (cellX >= 0 && cellX < WEEKS_TO_SHOW && cellY >= 0 && cellY < DAYS_IN_WEEK) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.WEEK_OF_YEAR, -WEEKS_TO_SHOW + cellX);
                calendar.add(Calendar.DAY_OF_YEAR, cellY);
                
                String dateKey = getDateKey(calendar);
                int activityLevel = activityData.getOrDefault(dateKey, 0);
                
                dateClickListener.onDateClick(dateKey, activityLevel);
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
    
    private String getDateKey(Calendar calendar) {
        return String.format("%04d-%02d-%02d", 
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }
    
    public void setActivityData(Map<String, Integer> data) {
        this.activityData = data;
        invalidate();
    }
    
    public void setOnDateClickListener(OnDateClickListener listener) {
        this.dateClickListener = listener;
    }
    
    public void updateActivityLevel(String date, int level) {
        activityData.put(date, level);
        invalidate();
    }
}