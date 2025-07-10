package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Comprehensive chart view that can display multiple metrics in various formats
 * Supports line charts, bar charts, area charts, and combined visualizations
 */
public class ComprehensiveChartView extends View {
    private static final String TAG = "ComprehensiveChartView";
    
    public enum ChartType {
        LINE, BAR, AREA, COMBINED
    }
    
    public enum TimeRange {
        HOUR, DAY, WEEK, MONTH
    }
    
    // Colors
    private static final int BACKGROUND_COLOR = Color.parseColor("#1A1A1A");
    private static final int GRID_COLOR = Color.parseColor("#2A2A2A");
    private static final int TEXT_COLOR = Color.WHITE;
    private static final int TEXT_SECONDARY_COLOR = Color.parseColor("#AAAAAA");
    
    // Chart colors
    private static final int[] CHART_COLORS = {
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#795548"), // Brown
        Color.parseColor("#607D8B")  // Blue Grey
    };
    
    private Paint linePaint;
    private Paint barPaint;
    private Paint areaPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint axisTextPaint;
    private RectF chartBounds;
    private Path areaPath;
    
    private List<ChartSeries> seriesList;
    private ChartType chartType = ChartType.LINE;
    private TimeRange timeRange = TimeRange.DAY;
    private String title = "";
    private String xAxisLabel = "";
    private String yAxisLabel = "";
    private boolean showGrid = true;
    private boolean showLegend = true;
    private boolean showAnimation = true;
    
    private float animationProgress = 0f;
    private long animationStartTime = 0;
    private long animationDuration = 1500;
    private boolean isAnimating = false;
    
    private SimpleDateFormat timeFormatter;
    
    public ComprehensiveChartView(Context context) {
        super(context);
        init();
    }
    
    public ComprehensiveChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ComprehensiveChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        
        areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(1f);
        
        axisTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTextPaint.setColor(TEXT_SECONDARY_COLOR);
        axisTextPaint.setTextSize(20f);
        axisTextPaint.setTextAlign(Paint.Align.CENTER);
        
        chartBounds = new RectF();
        areaPath = new Path();
        seriesList = new ArrayList<>();
        
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate chart bounds with margins for labels
        float margin = 60f;
        float topMargin = showLegend ? 120f : 80f;
        float bottomMargin = 80f;
        
        chartBounds.set(margin, topMargin, w - margin, h - bottomMargin);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawColor(BACKGROUND_COLOR);
        
        if (seriesList.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }
        
        // Update animation
        if (isAnimating) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - animationStartTime;
            animationProgress = Math.min(1f, elapsed / (float) animationDuration);
            
            // Easing function
            animationProgress = easeInOutQuart(animationProgress);
            
            if (elapsed >= animationDuration) {
                isAnimating = false;
                animationProgress = 1f;
            }
            
            invalidate();
        }
        
        // Draw title
        if (!title.isEmpty()) {
            drawTitle(canvas);
        }
        
        // Draw grid
        if (showGrid) {
            drawGrid(canvas);
        }
        
        // Draw axes
        drawAxes(canvas);
        
        // Draw chart based on type
        switch (chartType) {
            case LINE:
                drawLineChart(canvas);
                break;
            case BAR:
                drawBarChart(canvas);
                break;
            case AREA:
                drawAreaChart(canvas);
                break;
            case COMBINED:
                drawCombinedChart(canvas);
                break;
        }
        
        // Draw legend
        if (showLegend) {
            drawLegend(canvas);
        }
    }
    
    private void drawEmptyState(Canvas canvas) {
        textPaint.setColor(TEXT_SECONDARY_COLOR);
        textPaint.setTextSize(24f);
        canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
    }
    
    private void drawTitle(Canvas canvas) {
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, getWidth() / 2f, 40f, textPaint);
    }
    
    private void drawGrid(Canvas canvas) {
        // Horizontal grid lines
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            float y = chartBounds.top + (i / (float) gridLines) * chartBounds.height();
            canvas.drawLine(chartBounds.left, y, chartBounds.right, y, gridPaint);
        }
        
        // Vertical grid lines
        for (int i = 0; i <= gridLines; i++) {
            float x = chartBounds.left + (i / (float) gridLines) * chartBounds.width();
            canvas.drawLine(x, chartBounds.top, x, chartBounds.bottom, gridPaint);
        }
    }
    
    private void drawAxes(Canvas canvas) {
        // Draw axis labels
        if (!xAxisLabel.isEmpty()) {
            axisTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(xAxisLabel, chartBounds.centerX(), 
                           chartBounds.bottom + 50f, axisTextPaint);
        }
        
        if (!yAxisLabel.isEmpty()) {
            canvas.save();
            canvas.rotate(-90f, chartBounds.left - 40f, chartBounds.centerY());
            axisTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(yAxisLabel, chartBounds.left - 40f, 
                           chartBounds.centerY() + 8f, axisTextPaint);
            canvas.restore();
        }
    }
    
    private void drawLineChart(Canvas canvas) {
        for (int seriesIndex = 0; seriesIndex < seriesList.size(); seriesIndex++) {
            ChartSeries series = seriesList.get(seriesIndex);
            if (series.dataPoints.isEmpty()) continue;
            
            linePaint.setColor(series.color);
            
            Path linePath = new Path();
            List<DataPoint> points = series.dataPoints;
            
            // Find data range
            float minValue = Float.MAX_VALUE;
            float maxValue = Float.MIN_VALUE;
            
            for (DataPoint point : points) {
                minValue = Math.min(minValue, point.value);
                maxValue = Math.max(maxValue, point.value);
            }
            
            // Add padding
            float range = maxValue - minValue;
            if (range == 0) range = 1;
            minValue -= range * 0.1f;
            maxValue += range * 0.1f;
            
            // Draw line
            for (int i = 0; i < points.size(); i++) {
                DataPoint point = points.get(i);
                
                float x = chartBounds.left + (i / (float) (points.size() - 1)) * chartBounds.width();
                float y = chartBounds.bottom - ((point.value - minValue) / (maxValue - minValue)) * chartBounds.height();
                
                // Apply animation
                if (showAnimation && isAnimating) {
                    float progress = Math.min(1f, (i + 1) / (float) points.size() * animationProgress);
                    if (progress < 1f) {
                        y = chartBounds.bottom + (y - chartBounds.bottom) * progress;
                    }
                }
                
                if (i == 0) {
                    linePath.moveTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                }
                
                // Draw data points
                canvas.drawCircle(x, y, 4f, linePaint);
            }
            
            canvas.drawPath(linePath, linePaint);
        }
    }
    
    private void drawBarChart(Canvas canvas) {
        if (seriesList.isEmpty()) return;
        
        int totalBars = seriesList.get(0).dataPoints.size() * seriesList.size();
        float barWidth = chartBounds.width() / (totalBars + seriesList.size());
        float groupWidth = barWidth * seriesList.size();
        float groupSpacing = barWidth;
        
        for (int pointIndex = 0; pointIndex < seriesList.get(0).dataPoints.size(); pointIndex++) {
            float groupX = chartBounds.left + pointIndex * (groupWidth + groupSpacing);
            
            for (int seriesIndex = 0; seriesIndex < seriesList.size(); seriesIndex++) {
                ChartSeries series = seriesList.get(seriesIndex);
                if (pointIndex >= series.dataPoints.size()) continue;
                
                DataPoint point = series.dataPoints.get(pointIndex);
                
                barPaint.setColor(series.color);
                
                float barX = groupX + seriesIndex * barWidth;
                float barHeight = (point.value / getMaxValue()) * chartBounds.height();
                
                // Apply animation
                if (showAnimation && isAnimating) {
                    barHeight *= animationProgress;
                }
                
                RectF barRect = new RectF(
                    barX,
                    chartBounds.bottom - barHeight,
                    barX + barWidth,
                    chartBounds.bottom
                );
                
                canvas.drawRect(barRect, barPaint);
            }
        }
    }
    
    private void drawAreaChart(Canvas canvas) {
        for (int seriesIndex = 0; seriesIndex < seriesList.size(); seriesIndex++) {
            ChartSeries series = seriesList.get(seriesIndex);
            if (series.dataPoints.isEmpty()) continue;
            
            // Create gradient for area fill
            LinearGradient gradient = new LinearGradient(
                0, chartBounds.top,
                0, chartBounds.bottom,
                series.color,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            );
            
            areaPaint.setShader(gradient);
            
            areaPath.reset();
            List<DataPoint> points = series.dataPoints;
            
            // Find data range
            float maxValue = getMaxValue();
            
            // Start from bottom left
            areaPath.moveTo(chartBounds.left, chartBounds.bottom);
            
            // Draw area path
            for (int i = 0; i < points.size(); i++) {
                DataPoint point = points.get(i);
                
                float x = chartBounds.left + (i / (float) (points.size() - 1)) * chartBounds.width();
                float y = chartBounds.bottom - (point.value / maxValue) * chartBounds.height();
                
                // Apply animation
                if (showAnimation && isAnimating) {
                    float progress = Math.min(1f, (i + 1) / (float) points.size() * animationProgress);
                    if (progress < 1f) {
                        y = chartBounds.bottom + (y - chartBounds.bottom) * progress;
                    }
                }
                
                if (i == 0) {
                    areaPath.lineTo(x, y);
                } else {
                    areaPath.lineTo(x, y);
                }
            }
            
            // Close the path
            areaPath.lineTo(chartBounds.right, chartBounds.bottom);
            areaPath.close();
            
            canvas.drawPath(areaPath, areaPaint);
            
            // Draw line on top
            linePaint.setColor(series.color);
            drawLineChart(canvas);
        }
    }
    
    private void drawCombinedChart(Canvas canvas) {
        // Draw areas first (background)
        drawAreaChart(canvas);
        
        // Then draw lines on top
        drawLineChart(canvas);
    }
    
    private void drawLegend(Canvas canvas) {
        if (seriesList.isEmpty()) return;
        
        float legendY = 80f;
        float legendSpacing = 120f;
        float legendStartX = chartBounds.left;
        
        axisTextPaint.setTextAlign(Paint.Align.LEFT);
        
        for (int i = 0; i < seriesList.size(); i++) {
            ChartSeries series = seriesList.get(i);
            
            float legendX = legendStartX + i * legendSpacing;
            
            // Draw color indicator
            barPaint.setColor(series.color);
            canvas.drawCircle(legendX, legendY - 5f, 8f, barPaint);
            
            // Draw label
            canvas.drawText(series.label, legendX + 15f, legendY, axisTextPaint);
        }
    }
    
    private float getMaxValue() {
        float maxValue = 0f;
        for (ChartSeries series : seriesList) {
            for (DataPoint point : series.dataPoints) {
                maxValue = Math.max(maxValue, point.value);
            }
        }
        return maxValue == 0f ? 1f : maxValue;
    }
    
    private float easeInOutQuart(float t) {
        if (t < 0.5f) {
            return 8f * t * t * t * t;
        } else {
            float p = 2f * t - 2f;
            return 1f - 8f * p * p * p * p;
        }
    }
    
    public void setChartType(ChartType type) {
        this.chartType = type;
        invalidate();
    }
    
    public void setTimeRange(TimeRange range) {
        this.timeRange = range;
        updateTimeFormatter();
        invalidate();
    }
    
    private void updateTimeFormatter() {
        switch (timeRange) {
            case HOUR:
                timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                break;
            case DAY:
                timeFormatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
                break;
            case WEEK:
                timeFormatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
                break;
            case MONTH:
                timeFormatter = new SimpleDateFormat("MMM", Locale.getDefault());
                break;
        }
    }
    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    public void setAxisLabels(String xLabel, String yLabel) {
        this.xAxisLabel = xLabel;
        this.yAxisLabel = yLabel;
        invalidate();
    }
    
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        invalidate();
    }
    
    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
        invalidate();
    }
    
    public void setSeriesData(List<ChartSeries> series) {
        this.seriesList = new ArrayList<>(series);
        startAnimation();
    }
    
    public void startAnimation() {
        if (showAnimation) {
            isAnimating = true;
            animationStartTime = System.currentTimeMillis();
            animationProgress = 0f;
            invalidate();
        }
    }
    
    public void setAnimationDuration(long duration) {
        this.animationDuration = duration;
    }
    
    // Data classes
    public static class ChartSeries {
        public final String label;
        public final List<DataPoint> dataPoints;
        public final int color;
        
        public ChartSeries(String label, List<DataPoint> dataPoints, int color) {
            this.label = label;
            this.dataPoints = new ArrayList<>(dataPoints);
            this.color = color;
        }
    }
    
    public static class DataPoint {
        public final Date timestamp;
        public final float value;
        public final String label;
        
        public DataPoint(Date timestamp, float value, String label) {
            this.timestamp = timestamp;
            this.value = value;
            this.label = label;
        }
        
        public DataPoint(Date timestamp, float value) {
            this(timestamp, value, "");
        }
    }
}