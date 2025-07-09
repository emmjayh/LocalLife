package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.locallife.app.R;

import java.util.List;

public class ChartView extends View {
    private Paint linePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Path linePath;
    private Path fillPath;
    private RectF chartRect;
    
    private List<Float> dataPoints;
    private List<String> labels;
    private String chartTitle = "";
    private ChartType chartType = ChartType.LINE;
    private int primaryColor;
    private int secondaryColor;
    private int textColor;
    private int gridColor;
    
    public enum ChartType {
        LINE, BAR, PIE
    }
    
    public ChartView(Context context) {
        super(context);
        init();
    }
    
    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        secondaryColor = ContextCompat.getColor(getContext(), R.color.primary_light);
        textColor = ContextCompat.getColor(getContext(), R.color.primary_text);
        gridColor = ContextCompat.getColor(getContext(), R.color.divider);
        
        linePaint = new Paint();
        linePaint.setColor(primaryColor);
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        
        fillPaint = new Paint();
        fillPaint.setColor(secondaryColor);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
        fillPaint.setAlpha(100);
        
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(dpToPx(12));
        textPaint.setAntiAlias(true);
        
        gridPaint = new Paint();
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        
        linePath = new Path();
        fillPath = new Path();
        chartRect = new RectF();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (dataPoints == null || dataPoints.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }
        
        setupChartRect();
        drawTitle(canvas);
        drawGrid(canvas);
        
        switch (chartType) {
            case LINE:
                drawLineChart(canvas);
                break;
            case BAR:
                drawBarChart(canvas);
                break;
            case PIE:
                drawPieChart(canvas);
                break;
        }
        
        drawLabels(canvas);
    }
    
    private void setupChartRect() {
        int padding = dpToPx(20);
        int titleSpace = dpToPx(40);
        int labelSpace = dpToPx(30);
        
        chartRect.set(
            padding,
            padding + titleSpace,
            getWidth() - padding,
            getHeight() - padding - labelSpace
        );
    }
    
    private void drawTitle(Canvas canvas) {
        if (!chartTitle.isEmpty()) {
            textPaint.setTextSize(dpToPx(16));
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(chartTitle, getWidth() / 2f, dpToPx(25), textPaint);
        }
    }
    
    private void drawGrid(Canvas canvas) {
        // Horizontal grid lines
        for (int i = 0; i <= 5; i++) {
            float y = chartRect.top + (chartRect.height() / 5) * i;
            canvas.drawLine(chartRect.left, y, chartRect.right, y, gridPaint);
        }
        
        // Vertical grid lines
        for (int i = 0; i <= 4; i++) {
            float x = chartRect.left + (chartRect.width() / 4) * i;
            canvas.drawLine(x, chartRect.top, x, chartRect.bottom, gridPaint);
        }
    }
    
    private void drawLineChart(Canvas canvas) {
        if (dataPoints.size() < 2) return;
        
        float maxValue = getMaxValue();
        float minValue = getMinValue();
        float valueRange = maxValue - minValue;
        
        linePath.reset();
        fillPath.reset();
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float x = chartRect.left + (chartRect.width() / (dataPoints.size() - 1)) * i;
            float y = chartRect.bottom - ((dataPoints.get(i) - minValue) / valueRange) * chartRect.height();
            
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, chartRect.bottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        
        // Close fill path
        fillPath.lineTo(chartRect.right, chartRect.bottom);
        fillPath.close();
        
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
    
    private void drawBarChart(Canvas canvas) {
        float maxValue = getMaxValue();
        float barWidth = chartRect.width() / dataPoints.size() * 0.8f;
        float barSpacing = chartRect.width() / dataPoints.size() * 0.2f;
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float x = chartRect.left + (chartRect.width() / dataPoints.size()) * i + barSpacing / 2;
            float barHeight = (dataPoints.get(i) / maxValue) * chartRect.height();
            float y = chartRect.bottom - barHeight;
            
            RectF barRect = new RectF(x, y, x + barWidth, chartRect.bottom);
            canvas.drawRect(barRect, fillPaint);
            canvas.drawRect(barRect, linePaint);
        }
    }
    
    private void drawPieChart(Canvas canvas) {
        float total = 0;
        for (Float value : dataPoints) {
            total += value;
        }
        
        float centerX = chartRect.centerX();
        float centerY = chartRect.centerY();
        float radius = Math.min(chartRect.width(), chartRect.height()) / 2 * 0.8f;
        
        float startAngle = -90f; // Start from top
        
        for (int i = 0; i < dataPoints.size(); i++) {
            float sweepAngle = (dataPoints.get(i) / total) * 360f;
            
            // Alternate colors
            Paint paint = i % 2 == 0 ? fillPaint : linePaint;
            
            canvas.drawArc(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                startAngle, sweepAngle, true, paint
            );
            
            startAngle += sweepAngle;
        }
    }
    
    private void drawLabels(Canvas canvas) {
        if (labels == null || labels.isEmpty()) return;
        
        textPaint.setTextSize(dpToPx(10));
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        for (int i = 0; i < Math.min(labels.size(), dataPoints.size()); i++) {
            float x = chartRect.left + (chartRect.width() / dataPoints.size()) * i + 
                     (chartRect.width() / dataPoints.size()) / 2;
            float y = chartRect.bottom + dpToPx(20);
            
            canvas.drawText(labels.get(i), x, y, textPaint);
        }
    }
    
    private void drawEmptyState(Canvas canvas) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dpToPx(14));
        canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
    }
    
    private float getMaxValue() {
        float max = Float.MIN_VALUE;
        for (Float value : dataPoints) {
            max = Math.max(max, value);
        }
        return max;
    }
    
    private float getMinValue() {
        float min = Float.MAX_VALUE;
        for (Float value : dataPoints) {
            min = Math.min(min, value);
        }
        return min;
    }
    
    private int dpToPx(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }
    
    // Public methods
    public void setDataPoints(List<Float> dataPoints) {
        this.dataPoints = dataPoints;
        invalidate();
    }
    
    public void setLabels(List<String> labels) {
        this.labels = labels;
        invalidate();
    }
    
    public void setChartTitle(String title) {
        this.chartTitle = title;
        invalidate();
    }
    
    public void setChartType(ChartType type) {
        this.chartType = type;
        invalidate();
    }
    
    public void setPrimaryColor(int color) {
        this.primaryColor = color;
        linePaint.setColor(color);
        invalidate();
    }
    
    public void setSecondaryColor(int color) {
        this.secondaryColor = color;
        fillPaint.setColor(color);
        invalidate();
    }
}