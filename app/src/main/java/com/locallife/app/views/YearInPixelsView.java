package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Year in Pixels view - displays a full year of activity data as a heat map
 * Similar to GitHub's contribution graph but for personal activity metrics
 */
public class YearInPixelsView extends View {
    private static final String TAG = "YearInPixelsView";
    
    // Constants
    private static final int WEEKS_IN_YEAR = 53;
    private static final int DAYS_IN_WEEK = 7;
    private static final float CELL_SIZE = 12f;
    private static final float CELL_SPACING = 2f;
    private static final float MONTH_LABEL_HEIGHT = 30f;
    private static final float DAY_LABEL_WIDTH = 25f;
    
    // Colors for activity levels (0-4)
    private static final int[] ACTIVITY_COLORS = {
        Color.parseColor("#EBEDF0"), // Level 0 - No activity
        Color.parseColor("#C6E48B"), // Level 1 - Low activity
        Color.parseColor("#7BC96F"), // Level 2 - Moderate activity
        Color.parseColor("#239A3B"), // Level 3 - High activity
        Color.parseColor("#196127")  // Level 4 - Very high activity
    };
    
    private static final int BACKGROUND_COLOR = Color.parseColor("#1A1A1A");
    private static final int TEXT_COLOR = Color.WHITE;
    private static final int TEXT_SECONDARY_COLOR = Color.parseColor("#AAAAAA");
    private static final int BORDER_COLOR = Color.parseColor("#30363D");
    
    // Paints
    private Paint cellPaint;
    private Paint textPaint;
    private Paint monthTextPaint;
    private Paint dayTextPaint;
    private Paint tooltipPaint;
    private Paint tooltipTextPaint;
    private Paint legendPaint;
    
    // Data
    private Map<String, DayData> dayDataMap;
    private Calendar calendar;
    private int selectedYear;
    private String selectedMetric = "activity_score";
    
    // Layout
    private float chartStartX;
    private float chartStartY;
    private float chartWidth;
    private float chartHeight;
    
    // Interaction
    private GestureDetector gestureDetector;
    private String hoveredDate = null;
    private float hoveredX = 0f;
    private float hoveredY = 0f;
    private boolean showTooltip = false;
    
    // Date formatters
    private SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
    private SimpleDateFormat tooltipFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    
    // Metrics
    private static final String[] METRICS = {
        "activity_score", "steps", "places", "screen_time", "media_consumption"
    };
    
    private static final String[] METRIC_LABELS = {
        "Activity Score", "Steps", "Places Visited", "Screen Time", "Media Consumption"
    };
    
    public YearInPixelsView(Context context) {
        super(context);
        init();
    }
    
    public YearInPixelsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public YearInPixelsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Initialize paints
        cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        monthTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        monthTextPaint.setColor(TEXT_SECONDARY_COLOR);
        monthTextPaint.setTextSize(20f);
        monthTextPaint.setTextAlign(Paint.Align.CENTER);
        
        dayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayTextPaint.setColor(TEXT_SECONDARY_COLOR);
        dayTextPaint.setTextSize(16f);
        dayTextPaint.setTextAlign(Paint.Align.CENTER);
        
        tooltipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipPaint.setColor(Color.parseColor("#21262D"));
        tooltipPaint.setStyle(Paint.Style.FILL);
        
        tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setColor(TEXT_COLOR);
        tooltipTextPaint.setTextSize(18f);
        tooltipTextPaint.setTextAlign(Paint.Align.LEFT);
        
        legendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        legendPaint.setStyle(Paint.Style.FILL);
        
        // Initialize data structures
        dayDataMap = new HashMap<>();
        calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        
        // Initialize gesture detector
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        
        // Generate sample data
        generateSampleData();
    }
    
    private void generateSampleData() {
        dayDataMap.clear();
        
        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear, 0, 1); // January 1st
        
        // Generate data for entire year
        for (int day = 0; day < 365; day++) {
            String dateKey = dateKeyFormat.format(cal.getTime());
            
            // Generate realistic activity patterns
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            
            // Simulate seasonal patterns and weekday/weekend differences
            float baseActivity = 0.5f + 0.3f * (float) Math.sin(dayOfYear * 2 * Math.PI / 365);
            
            // Weekend boost
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                baseActivity += 0.2f;
            }
            
            // Add some randomness
            baseActivity += (Math.random() - 0.5) * 0.4;
            baseActivity = Math.max(0, Math.min(1, baseActivity));
            
            // Create day data
            DayData dayData = new DayData();
            dayData.activityScore = baseActivity * 100;
            dayData.steps = (int) (baseActivity * 15000);
            dayData.placesVisited = (int) (baseActivity * 10);
            dayData.screenTimeMinutes = (int) (300 + (1 - baseActivity) * 200);
            dayData.mediaMinutes = (int) (baseActivity * 180);
            
            dayDataMap.put(dateKey, dayData);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate chart dimensions
        chartStartX = DAY_LABEL_WIDTH + 20f;
        chartStartY = MONTH_LABEL_HEIGHT + 40f;
        chartWidth = WEEKS_IN_YEAR * (CELL_SIZE + CELL_SPACING) - CELL_SPACING;
        chartHeight = DAYS_IN_WEEK * (CELL_SIZE + CELL_SPACING) - CELL_SPACING;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawColor(BACKGROUND_COLOR);
        
        // Draw title
        drawTitle(canvas);
        
        // Draw month labels
        drawMonthLabels(canvas);
        
        // Draw day labels
        drawDayLabels(canvas);
        
        // Draw heat map
        drawHeatMap(canvas);
        
        // Draw legend
        drawLegend(canvas);
        
        // Draw statistics
        drawStatistics(canvas);
        
        // Draw tooltip
        if (showTooltip) {
            drawTooltip(canvas);
        }
    }
    
    private void drawTitle(Canvas canvas) {
        String title = selectedYear + " - " + getMetricLabel(selectedMetric);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, getWidth() / 2f, 30f, textPaint);
    }
    
    private void drawMonthLabels(Canvas canvas) {
        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear, 0, 1);
        
        // Find the first Monday of the year
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        int currentMonth = -1;
        for (int week = 0; week < WEEKS_IN_YEAR; week++) {
            int month = cal.get(Calendar.MONTH);
            
            if (month != currentMonth) {
                currentMonth = month;
                float x = chartStartX + week * (CELL_SIZE + CELL_SPACING) + CELL_SIZE / 2;
                String monthLabel = monthFormat.format(cal.getTime());
                canvas.drawText(monthLabel, x, chartStartY - 10f, monthTextPaint);
            }
            
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
    }
    
    private void drawDayLabels(Canvas canvas) {
        String[] dayLabels = {"", "M", "", "W", "", "F", ""};
        
        for (int day = 0; day < DAYS_IN_WEEK; day++) {
            if (!dayLabels[day].isEmpty()) {
                float y = chartStartY + day * (CELL_SIZE + CELL_SPACING) + CELL_SIZE / 2 + 5f;
                canvas.drawText(dayLabels[day], chartStartX - 15f, y, dayTextPaint);
            }
        }
    }
    
    private void drawHeatMap(Canvas canvas) {
        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear, 0, 1);
        
        // Find the first Monday of the year
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        for (int week = 0; week < WEEKS_IN_YEAR; week++) {
            for (int day = 0; day < DAYS_IN_WEEK; day++) {
                if (cal.get(Calendar.YEAR) == selectedYear || 
                    (week == 0 && cal.get(Calendar.YEAR) == selectedYear - 1) ||
                    (week == WEEKS_IN_YEAR - 1 && cal.get(Calendar.YEAR) == selectedYear + 1)) {
                    
                    String dateKey = dateKeyFormat.format(cal.getTime());
                    DayData dayData = dayDataMap.get(dateKey);
                    
                    float x = chartStartX + week * (CELL_SIZE + CELL_SPACING);
                    float y = chartStartY + day * (CELL_SIZE + CELL_SPACING);
                    
                    // Get activity level for current metric
                    int activityLevel = getActivityLevel(dayData);
                    
                    // Set cell color
                    cellPaint.setColor(ACTIVITY_COLORS[activityLevel]);
                    
                    // Draw cell
                    RectF cellRect = new RectF(x, y, x + CELL_SIZE, y + CELL_SIZE);
                    canvas.drawRoundRect(cellRect, 2f, 2f, cellPaint);
                    
                    // Highlight hovered cell
                    if (dateKey.equals(hoveredDate)) {
                        cellPaint.setColor(Color.parseColor("#FFFFFF"));
                        cellPaint.setStyle(Paint.Style.STROKE);
                        cellPaint.setStrokeWidth(2f);
                        canvas.drawRoundRect(cellRect, 2f, 2f, cellPaint);
                        cellPaint.setStyle(Paint.Style.FILL);
                    }
                }
                
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
    }
    
    private void drawLegend(Canvas canvas) {
        float legendY = chartStartY + chartHeight + 50f;
        float legendStartX = chartStartX + chartWidth - (ACTIVITY_COLORS.length * 20f);
        
        // Legend label
        monthTextPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Less", legendStartX - 10f, legendY + 8f, monthTextPaint);
        
        // Legend squares
        for (int i = 0; i < ACTIVITY_COLORS.length; i++) {
            float x = legendStartX + i * 16f;
            
            legendPaint.setColor(ACTIVITY_COLORS[i]);
            RectF legendRect = new RectF(x, legendY, x + 12f, legendY + 12f);
            canvas.drawRoundRect(legendRect, 2f, 2f, legendPaint);
        }
        
        // Legend label
        monthTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("More", legendStartX + ACTIVITY_COLORS.length * 16f + 5f, legendY + 8f, monthTextPaint);
    }
    
    private void drawStatistics(Canvas canvas) {
        float statsY = chartStartY + chartHeight + 90f;
        
        // Calculate statistics
        int totalDays = 0;
        int activeDays = 0;
        float totalActivity = 0f;
        
        for (DayData dayData : dayDataMap.values()) {
            if (dayData != null) {
                totalDays++;
                float value = getMetricValue(dayData);
                if (value > 0) {
                    activeDays++;
                    totalActivity += value;
                }
            }
        }
        
        float averageActivity = totalDays > 0 ? totalActivity / totalDays : 0f;
        
        // Draw statistics
        monthTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format(Locale.getDefault(), 
            "Total days: %d  |  Active days: %d  |  Average %s: %.1f",
            totalDays, activeDays, getMetricLabel(selectedMetric), averageActivity),
            chartStartX, statsY, monthTextPaint);
        
        // Draw current streak
        int currentStreak = calculateCurrentStreak();
        canvas.drawText(String.format(Locale.getDefault(), 
            "Current streak: %d days", currentStreak),
            chartStartX, statsY + 25f, monthTextPaint);
    }
    
    private void drawTooltip(Canvas canvas) {
        if (hoveredDate == null || !dayDataMap.containsKey(hoveredDate)) {
            return;
        }
        
        DayData dayData = dayDataMap.get(hoveredDate);
        
        // Parse date for tooltip
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateKeyFormat.parse(hoveredDate));
        } catch (Exception e) {
            return;
        }
        
        // Prepare tooltip text
        String dateText = tooltipFormat.format(cal.getTime());
        String valueText = String.format(Locale.getDefault(), 
            "%s: %.1f", getMetricLabel(selectedMetric), getMetricValue(dayData));
        
        // Calculate tooltip size
        float tooltipWidth = Math.max(
            tooltipTextPaint.measureText(dateText),
            tooltipTextPaint.measureText(valueText)
        ) + 20f;
        float tooltipHeight = 60f;
        
        // Position tooltip
        float tooltipX = Math.min(hoveredX, getWidth() - tooltipWidth - 10f);
        float tooltipY = hoveredY - tooltipHeight - 10f;
        
        if (tooltipY < 0) {
            tooltipY = hoveredY + 20f;
        }
        
        // Draw tooltip background
        RectF tooltipRect = new RectF(tooltipX, tooltipY, 
            tooltipX + tooltipWidth, tooltipY + tooltipHeight);
        canvas.drawRoundRect(tooltipRect, 8f, 8f, tooltipPaint);
        
        // Draw tooltip border
        Paint borderPaint = new Paint(tooltipPaint);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);
        borderPaint.setColor(BORDER_COLOR);
        canvas.drawRoundRect(tooltipRect, 8f, 8f, borderPaint);
        
        // Draw tooltip text
        canvas.drawText(dateText, tooltipX + 10f, tooltipY + 20f, tooltipTextPaint);
        canvas.drawText(valueText, tooltipX + 10f, tooltipY + 45f, tooltipTextPaint);
    }
    
    private int getActivityLevel(DayData dayData) {
        if (dayData == null) return 0;
        
        float value = getMetricValue(dayData);
        float maxValue = getMaxValueForMetric();
        
        if (value <= 0) return 0;
        
        float normalized = value / maxValue;
        
        if (normalized < 0.2f) return 1;
        if (normalized < 0.4f) return 2;
        if (normalized < 0.7f) return 3;
        return 4;
    }
    
    private float getMetricValue(DayData dayData) {
        if (dayData == null) return 0f;
        
        switch (selectedMetric) {
            case "activity_score":
                return dayData.activityScore;
            case "steps":
                return dayData.steps;
            case "places":
                return dayData.placesVisited;
            case "screen_time":
                return dayData.screenTimeMinutes;
            case "media_consumption":
                return dayData.mediaMinutes;
            default:
                return dayData.activityScore;
        }
    }
    
    private float getMaxValueForMetric() {
        switch (selectedMetric) {
            case "activity_score":
                return 100f;
            case "steps":
                return 15000f;
            case "places":
                return 10f;
            case "screen_time":
                return 600f;
            case "media_consumption":
                return 300f;
            default:
                return 100f;
        }
    }
    
    private String getMetricLabel(String metric) {
        for (int i = 0; i < METRICS.length; i++) {
            if (METRICS[i].equals(metric)) {
                return METRIC_LABELS[i];
            }
        }
        return "Activity Score";
    }
    
    private int calculateCurrentStreak() {
        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear, 11, 31); // December 31st
        
        int streak = 0;
        while (cal.get(Calendar.YEAR) == selectedYear) {
            String dateKey = dateKeyFormat.format(cal.getTime());
            DayData dayData = dayDataMap.get(dateKey);
            
            if (dayData != null && getMetricValue(dayData) > 0) {
                streak++;
            } else {
                break;
            }
            
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        return streak;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        
        if (event.getAction() == MotionEvent.ACTION_UP) {
            showTooltip = false;
            hoveredDate = null;
            invalidate();
        }
        
        return true;
    }
    
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            handleTouch(e.getX(), e.getY());
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
            handleTouch(e.getX(), e.getY());
        }
    }
    
    private void handleTouch(float x, float y) {
        // Check if touch is within chart area
        if (x < chartStartX || x > chartStartX + chartWidth ||
            y < chartStartY || y > chartStartY + chartHeight) {
            return;
        }
        
        // Calculate which cell was touched
        int week = (int) ((x - chartStartX) / (CELL_SIZE + CELL_SPACING));
        int day = (int) ((y - chartStartY) / (CELL_SIZE + CELL_SPACING));
        
        if (week >= 0 && week < WEEKS_IN_YEAR && day >= 0 && day < DAYS_IN_WEEK) {
            // Calculate date
            Calendar cal = Calendar.getInstance();
            cal.set(selectedYear, 0, 1);
            
            // Find first Monday
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }
            
            cal.add(Calendar.WEEK_OF_YEAR, week);
            cal.add(Calendar.DAY_OF_YEAR, day);
            
            String dateKey = dateKeyFormat.format(cal.getTime());
            
            if (dayDataMap.containsKey(dateKey)) {
                hoveredDate = dateKey;
                hoveredX = x;
                hoveredY = y;
                showTooltip = true;
                invalidate();
            }
        }
    }
    
    public void setSelectedYear(int year) {
        this.selectedYear = year;
        generateSampleData();
        invalidate();
    }
    
    public void setSelectedMetric(String metric) {
        this.selectedMetric = metric;
        invalidate();
    }
    
    public void setDayData(Map<String, DayData> dayData) {
        this.dayDataMap = new HashMap<>(dayData);
        invalidate();
    }
    
    public String[] getAvailableMetrics() {
        return METRICS.clone();
    }
    
    public String[] getMetricLabels() {
        return METRIC_LABELS.clone();
    }
    
    // Data class for day information
    public static class DayData {
        public float activityScore;
        public int steps;
        public int placesVisited;
        public int screenTimeMinutes;
        public int mediaMinutes;
        
        public DayData() {
            this.activityScore = 0f;
            this.steps = 0;
            this.placesVisited = 0;
            this.screenTimeMinutes = 0;
            this.mediaMinutes = 0;
        }
        
        public DayData(float activityScore, int steps, int placesVisited, int screenTimeMinutes, int mediaMinutes) {
            this.activityScore = activityScore;
            this.steps = steps;
            this.placesVisited = placesVisited;
            this.screenTimeMinutes = screenTimeMinutes;
            this.mediaMinutes = mediaMinutes;
        }
    }
}