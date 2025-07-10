package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view for displaying scatter plots with correlation analysis
 */
public class CorrelationScatterPlotView extends View {
    private static final String TAG = "CorrelationScatterPlotView";
    
    // Colors
    private static final int BACKGROUND_COLOR = Color.parseColor("#1A1A1A");
    private static final int GRID_COLOR = Color.parseColor("#2A2A2A");
    private static final int AXIS_COLOR = Color.parseColor("#FFFFFF");
    private static final int POINT_COLOR = Color.parseColor("#4CAF50");
    private static final int TREND_LINE_COLOR = Color.parseColor("#FF9800");
    private static final int TEXT_COLOR = Color.WHITE;
    
    // Paints
    private Paint pointPaint;
    private Paint trendLinePaint;
    private Paint axisPaint;
    private Paint gridPaint;
    private Paint textPaint;
    
    // Data
    private List<DataPoint> dataPoints;
    private String xLabel = "X Axis";
    private String yLabel = "Y Axis";
    private String title = "Correlation Analysis";
    private float correlation = 0f;
    
    // Chart bounds
    private float chartLeft;
    private float chartRight;
    private float chartTop;
    private float chartBottom;
    
    // Data ranges
    private float minX, maxX, minY, maxY;
    
    public CorrelationScatterPlotView(Context context) {
        super(context);
        init();
    }
    
    public CorrelationScatterPlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CorrelationScatterPlotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(POINT_COLOR);
        pointPaint.setStyle(Paint.Style.FILL);
        
        trendLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trendLinePaint.setColor(TREND_LINE_COLOR);
        trendLinePaint.setStrokeWidth(3f);
        trendLinePaint.setStyle(Paint.Style.STROKE);
        
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(AXIS_COLOR);
        axisPaint.setStrokeWidth(2f);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(1f);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        dataPoints = new ArrayList<>();
        generateSampleData();
    }
    
    private void generateSampleData() {
        dataPoints.clear();
        
        // Generate sample correlation data
        for (int i = 0; i < 50; i++) {
            float x = (float) (Math.random() * 100);
            float y = x * 0.8f + (float) (Math.random() * 20 - 10); // Positive correlation with noise
            dataPoints.add(new DataPoint(x, y));
        }
        
        correlation = 0.75f; // Sample correlation
        updateDataRanges();
    }
    
    private void updateDataRanges() {
        if (dataPoints.isEmpty()) return;
        
        minX = maxX = dataPoints.get(0).x;
        minY = maxY = dataPoints.get(0).y;
        
        for (DataPoint point : dataPoints) {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        
        // Add padding
        float xPadding = (maxX - minX) * 0.1f;
        float yPadding = (maxY - minY) * 0.1f;
        
        minX -= xPadding;
        maxX += xPadding;
        minY -= yPadding;
        maxY += yPadding;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate chart bounds
        chartLeft = 80f;
        chartRight = w - 40f;
        chartTop = 80f;
        chartBottom = h - 80f;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawColor(BACKGROUND_COLOR);
        
        if (dataPoints.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }
        
        // Draw title
        drawTitle(canvas);
        
        // Draw grid
        drawGrid(canvas);
        
        // Draw axes
        drawAxes(canvas);
        
        // Draw data points
        drawDataPoints(canvas);
        
        // Draw trend line
        drawTrendLine(canvas);
        
        // Draw correlation info
        drawCorrelationInfo(canvas);
    }
    
    private void drawEmptyState(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
    }
    
    private void drawTitle(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(28f);
        canvas.drawText(title, getWidth() / 2f, 40f, textPaint);
    }
    
    private void drawGrid(Canvas canvas) {
        // Vertical grid lines
        int verticalLines = 5;
        for (int i = 0; i <= verticalLines; i++) {
            float x = chartLeft + (i / (float) verticalLines) * (chartRight - chartLeft);
            canvas.drawLine(x, chartTop, x, chartBottom, gridPaint);
        }
        
        // Horizontal grid lines
        int horizontalLines = 5;
        for (int i = 0; i <= horizontalLines; i++) {
            float y = chartTop + (i / (float) horizontalLines) * (chartBottom - chartTop);
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
        }
    }
    
    private void drawAxes(Canvas canvas) {
        // Draw X axis
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);
        
        // Draw Y axis
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint);
        
        // Draw axis labels
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(20f);
        
        // X axis label
        canvas.drawText(xLabel, (chartLeft + chartRight) / 2f, chartBottom + 50f, textPaint);
        
        // Y axis label
        canvas.save();
        canvas.rotate(-90f, chartLeft - 50f, (chartTop + chartBottom) / 2f);
        canvas.drawText(yLabel, chartLeft - 50f, (chartTop + chartBottom) / 2f + 8f, textPaint);
        canvas.restore();
        
        // Draw axis values
        textPaint.setTextSize(16f);
        
        // X axis values
        for (int i = 0; i <= 5; i++) {
            float x = chartLeft + (i / 5f) * (chartRight - chartLeft);
            float value = minX + (i / 5f) * (maxX - minX);
            canvas.drawText(String.format("%.0f", value), x, chartBottom + 25f, textPaint);
        }
        
        // Y axis values
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 5; i++) {
            float y = chartBottom - (i / 5f) * (chartBottom - chartTop);
            float value = minY + (i / 5f) * (maxY - minY);
            canvas.drawText(String.format("%.0f", value), chartLeft - 10f, y + 6f, textPaint);
        }
    }
    
    private void drawDataPoints(Canvas canvas) {
        for (DataPoint point : dataPoints) {
            float x = chartLeft + ((point.x - minX) / (maxX - minX)) * (chartRight - chartLeft);
            float y = chartBottom - ((point.y - minY) / (maxY - minY)) * (chartBottom - chartTop);
            
            // Draw point with some transparency based on density
            int alpha = (int) (255 * 0.7f);
            pointPaint.setAlpha(alpha);
            canvas.drawCircle(x, y, 6f, pointPaint);
        }
    }
    
    private void drawTrendLine(Canvas canvas) {
        if (dataPoints.size() < 2) return;
        
        // Calculate linear regression
        LinearRegression regression = calculateLinearRegression();
        
        // Draw trend line
        float x1 = chartLeft;
        float y1 = chartBottom - ((regression.predict(minX) - minY) / (maxY - minY)) * (chartBottom - chartTop);
        
        float x2 = chartRight;
        float y2 = chartBottom - ((regression.predict(maxX) - minY) / (maxY - minY)) * (chartBottom - chartTop);
        
        // Clamp y values to chart bounds
        y1 = Math.max(chartTop, Math.min(chartBottom, y1));
        y2 = Math.max(chartTop, Math.min(chartBottom, y2));
        
        canvas.drawLine(x1, y1, x2, y2, trendLinePaint);
    }
    
    private void drawCorrelationInfo(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(18f);
        
        String corrText = String.format("Correlation: %.3f", correlation);
        String strengthText = "Strength: " + getCorrelationStrength(correlation);
        
        canvas.drawText(corrText, chartLeft, chartTop - 40f, textPaint);
        canvas.drawText(strengthText, chartLeft, chartTop - 20f, textPaint);
        
        // Draw interpretation
        textPaint.setTextSize(14f);
        String interpretation = getCorrelationInterpretation(correlation);
        canvas.drawText(interpretation, chartLeft, chartTop - 5f, textPaint);
    }
    
    private LinearRegression calculateLinearRegression() {
        if (dataPoints.isEmpty()) return new LinearRegression(0, 0);
        
        int n = dataPoints.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (DataPoint point : dataPoints) {
            sumX += point.x;
            sumY += point.y;
            sumXY += point.x * point.y;
            sumXX += point.x * point.x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        return new LinearRegression(slope, intercept);
    }
    
    private String getCorrelationStrength(float correlation) {
        float abs = Math.abs(correlation);
        if (abs < 0.3f) return "Weak";
        if (abs < 0.5f) return "Moderate";
        if (abs < 0.7f) return "Strong";
        return "Very Strong";
    }
    
    private String getCorrelationInterpretation(float correlation) {
        if (correlation > 0.5f) return "Strong positive relationship";
        if (correlation > 0.3f) return "Moderate positive relationship";
        if (correlation > 0.1f) return "Weak positive relationship";
        if (correlation > -0.1f) return "Little to no relationship";
        if (correlation > -0.3f) return "Weak negative relationship";
        if (correlation > -0.5f) return "Moderate negative relationship";
        return "Strong negative relationship";
    }
    
    public void setData(List<DataPoint> dataPoints, float correlation) {
        this.dataPoints = new ArrayList<>(dataPoints);
        this.correlation = correlation;
        updateDataRanges();
        invalidate();
    }
    
    public void setLabels(String title, String xLabel, String yLabel) {
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        invalidate();
    }
    
    // Data classes
    public static class DataPoint {
        public final float x;
        public final float y;
        
        public DataPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private static class LinearRegression {
        private final double slope;
        private final double intercept;
        
        public LinearRegression(double slope, double intercept) {
            this.slope = slope;
            this.intercept = intercept;
        }
        
        public float predict(float x) {
            return (float) (slope * x + intercept);
        }
    }
}