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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Custom view that displays environmental data over time
 * Shows temperature, UV index, air quality, and weather conditions
 */
public class EnvironmentalTimelineView extends View {
    private static final String TAG = "EnvironmentalTimelineView";
    
    // Colors
    private static final int BACKGROUND_COLOR = Color.parseColor("#1A1A1A");
    private static final int GRID_COLOR = Color.parseColor("#2A2A2A");
    private static final int TEXT_COLOR = Color.WHITE;
    private static final int TEXT_SECONDARY_COLOR = Color.parseColor("#AAAAAA");
    
    // Temperature colors
    private static final int TEMP_COLD_COLOR = Color.parseColor("#4FC3F7");
    private static final int TEMP_MODERATE_COLOR = Color.parseColor("#66BB6A");
    private static final int TEMP_HOT_COLOR = Color.parseColor("#FF7043");
    
    // UV colors
    private static final int UV_LOW_COLOR = Color.parseColor("#4CAF50");
    private static final int UV_MODERATE_COLOR = Color.parseColor("#FF9800");
    private static final int UV_HIGH_COLOR = Color.parseColor("#F44336");
    
    // Air quality colors
    private static final int AQ_GOOD_COLOR = Color.parseColor("#4CAF50");
    private static final int AQ_MODERATE_COLOR = Color.parseColor("#FF9800");
    private static final int AQ_POOR_COLOR = Color.parseColor("#F44336");
    
    private Paint linePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint backgroundPaint;
    private Path temperaturePath;
    private Path uvPath;
    private Path aqPath;
    
    private List<EnvironmentalData> timelineData;
    private String selectedMetric = "temperature";
    private int viewWidth;
    private int viewHeight;
    private float chartTop;
    private float chartBottom;
    private float chartLeft;
    private float chartRight;
    
    private SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    
    public EnvironmentalTimelineView(Context context) {
        super(context);
        init();
    }
    
    public EnvironmentalTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public EnvironmentalTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(1f);
        
        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        
        temperaturePath = new Path();
        uvPath = new Path();
        aqPath = new Path();
        
        timelineData = new ArrayList<>();
        
        // Generate sample data
        generateSampleData();
    }
    
    private void generateSampleData() {
        timelineData.clear();
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -24); // Start 24 hours ago
        
        for (int i = 0; i < 24; i++) {
            Date time = calendar.getTime();
            
            // Generate realistic environmental data
            float temperature = 15f + (float) (Math.sin(i * Math.PI / 12) * 10) + (float) (Math.random() * 5);
            float uvIndex = Math.max(0, (float) (Math.sin((i - 6) * Math.PI / 12) * 8) + (float) (Math.random() * 2));
            int airQuality = 50 + (int) (Math.random() * 100);
            float humidity = 40f + (float) (Math.random() * 40);
            float windSpeed = (float) (Math.random() * 20);
            
            String condition = getWeatherCondition(i);
            
            timelineData.add(new EnvironmentalData(
                time, temperature, uvIndex, airQuality, humidity, windSpeed, condition
            ));
            
            calendar.add(Calendar.HOUR, 1);
        }
    }
    
    private String getWeatherCondition(int hour) {
        if (hour >= 6 && hour <= 18) {
            return Math.random() > 0.7 ? "Cloudy" : "Sunny";
        } else {
            return Math.random() > 0.8 ? "Cloudy" : "Clear";
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        
        // Set chart bounds
        chartLeft = 80f;
        chartRight = viewWidth - 40f;
        chartTop = 80f;
        chartBottom = viewHeight - 120f;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw background
        canvas.drawColor(BACKGROUND_COLOR);
        
        if (timelineData.isEmpty()) {
            return;
        }
        
        // Draw chart based on selected metric
        drawChart(canvas);
        
        // Draw grid and labels
        drawGrid(canvas);
        
        // Draw time labels
        drawTimeLabels(canvas);
        
        // Draw legend
        drawLegend(canvas);
        
        // Draw current values
        drawCurrentValues(canvas);
    }
    
    private void drawChart(Canvas canvas) {
        if (timelineData.isEmpty()) return;
        
        switch (selectedMetric) {
            case "temperature":
                drawTemperatureChart(canvas);
                break;
            case "uv":
                drawUVChart(canvas);
                break;
            case "air_quality":
                drawAirQualityChart(canvas);
                break;
            case "all":
                drawCombinedChart(canvas);
                break;
        }
    }
    
    private void drawTemperatureChart(Canvas canvas) {
        temperaturePath.reset();
        
        // Find temperature range
        float minTemp = Float.MAX_VALUE;
        float maxTemp = Float.MIN_VALUE;
        
        for (EnvironmentalData data : timelineData) {
            minTemp = Math.min(minTemp, data.temperature);
            maxTemp = Math.max(maxTemp, data.temperature);
        }
        
        // Add padding to range
        float tempRange = maxTemp - minTemp;
        minTemp -= tempRange * 0.1f;
        maxTemp += tempRange * 0.1f;
        
        // Create gradient
        LinearGradient gradient = new LinearGradient(
            0, chartTop, 0, chartBottom,
            new int[]{TEMP_HOT_COLOR, TEMP_MODERATE_COLOR, TEMP_COLD_COLOR},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        
        linePaint.setShader(gradient);
        
        // Draw temperature line
        for (int i = 0; i < timelineData.size(); i++) {
            EnvironmentalData data = timelineData.get(i);
            
            float x = chartLeft + (i / (float) (timelineData.size() - 1)) * (chartRight - chartLeft);
            float y = chartTop + (1f - (data.temperature - minTemp) / (maxTemp - minTemp)) * (chartBottom - chartTop);
            
            if (i == 0) {
                temperaturePath.moveTo(x, y);
            } else {
                temperaturePath.lineTo(x, y);
            }
            
            // Draw temperature points
            fillPaint.setColor(getTemperatureColor(data.temperature));
            canvas.drawCircle(x, y, 6f, fillPaint);
        }
        
        canvas.drawPath(temperaturePath, linePaint);
        
        // Reset shader
        linePaint.setShader(null);
    }
    
    private void drawUVChart(Canvas canvas) {
        uvPath.reset();
        
        float maxUV = 12f; // UV index max
        
        // Create UV gradient
        LinearGradient uvGradient = new LinearGradient(
            0, chartTop, 0, chartBottom,
            new int[]{UV_HIGH_COLOR, UV_MODERATE_COLOR, UV_LOW_COLOR},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        
        linePaint.setShader(uvGradient);
        
        // Draw UV line
        for (int i = 0; i < timelineData.size(); i++) {
            EnvironmentalData data = timelineData.get(i);
            
            float x = chartLeft + (i / (float) (timelineData.size() - 1)) * (chartRight - chartLeft);
            float y = chartTop + (1f - (data.uvIndex / maxUV)) * (chartBottom - chartTop);
            
            if (i == 0) {
                uvPath.moveTo(x, y);
            } else {
                uvPath.lineTo(x, y);
            }
            
            // Draw UV points
            fillPaint.setColor(getUVColor(data.uvIndex));
            canvas.drawCircle(x, y, 6f, fillPaint);
        }
        
        canvas.drawPath(uvPath, linePaint);
        linePaint.setShader(null);
    }
    
    private void drawAirQualityChart(Canvas canvas) {
        aqPath.reset();
        
        float maxAQ = 300f; // AQI max
        
        // Create AQ gradient
        LinearGradient aqGradient = new LinearGradient(
            0, chartTop, 0, chartBottom,
            new int[]{AQ_POOR_COLOR, AQ_MODERATE_COLOR, AQ_GOOD_COLOR},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );
        
        linePaint.setShader(aqGradient);
        
        // Draw AQ line
        for (int i = 0; i < timelineData.size(); i++) {
            EnvironmentalData data = timelineData.get(i);
            
            float x = chartLeft + (i / (float) (timelineData.size() - 1)) * (chartRight - chartLeft);
            float y = chartTop + (1f - (data.airQuality / maxAQ)) * (chartBottom - chartTop);
            
            if (i == 0) {
                aqPath.moveTo(x, y);
            } else {
                aqPath.lineTo(x, y);
            }
            
            // Draw AQ points
            fillPaint.setColor(getAirQualityColor(data.airQuality));
            canvas.drawCircle(x, y, 6f, fillPaint);
        }
        
        canvas.drawPath(aqPath, linePaint);
        linePaint.setShader(null);
    }
    
    private void drawCombinedChart(Canvas canvas) {
        // Draw all three metrics with different alpha values
        linePaint.setAlpha(200);
        drawTemperatureChart(canvas);
        drawUVChart(canvas);
        drawAirQualityChart(canvas);
        linePaint.setAlpha(255);
    }
    
    private void drawGrid(Canvas canvas) {
        // Draw horizontal grid lines
        for (int i = 0; i <= 4; i++) {
            float y = chartTop + (i / 4f) * (chartBottom - chartTop);
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
        }
        
        // Draw vertical grid lines
        for (int i = 0; i <= 6; i++) {
            float x = chartLeft + (i / 6f) * (chartRight - chartLeft);
            canvas.drawLine(x, chartTop, x, chartBottom, gridPaint);
        }
    }
    
    private void drawTimeLabels(Canvas canvas) {
        textPaint.setColor(TEXT_SECONDARY_COLOR);
        textPaint.setTextSize(20f);
        
        // Draw time labels at bottom
        for (int i = 0; i < timelineData.size(); i += 4) {
            EnvironmentalData data = timelineData.get(i);
            float x = chartLeft + (i / (float) (timelineData.size() - 1)) * (chartRight - chartLeft);
            String timeLabel = hourFormat.format(data.timestamp);
            canvas.drawText(timeLabel, x, chartBottom + 40f, textPaint);
        }
    }
    
    private void drawLegend(Canvas canvas) {
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        
        String title = getMetricTitle(selectedMetric);
        canvas.drawText(title, chartLeft, 50f, textPaint);
        
        // Draw metric selector buttons
        drawMetricButtons(canvas);
    }
    
    private void drawMetricButtons(Canvas canvas) {
        String[] metrics = {"Temperature", "UV Index", "Air Quality", "All"};
        String[] metricIds = {"temperature", "uv", "air_quality", "all"};
        
        textPaint.setTextSize(18f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        float buttonWidth = 80f;
        float buttonHeight = 30f;
        float startX = chartRight - (metrics.length * buttonWidth) - 20f;
        
        for (int i = 0; i < metrics.length; i++) {
            float x = startX + (i * buttonWidth);
            float y = 30f;
            
            // Draw button background
            if (metricIds[i].equals(selectedMetric)) {
                fillPaint.setColor(Color.parseColor("#4CAF50"));
            } else {
                fillPaint.setColor(Color.parseColor("#2A2A2A"));
            }
            
            RectF buttonRect = new RectF(x, y, x + buttonWidth - 10f, y + buttonHeight);
            canvas.drawRoundRect(buttonRect, 5f, 5f, fillPaint);
            
            // Draw button text
            textPaint.setColor(TEXT_COLOR);
            canvas.drawText(metrics[i], x + buttonWidth / 2 - 5f, y + 20f, textPaint);
        }
    }
    
    private void drawCurrentValues(Canvas canvas) {
        if (timelineData.isEmpty()) return;
        
        EnvironmentalData current = timelineData.get(timelineData.size() - 1);
        
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(16f);
        textPaint.setColor(TEXT_COLOR);
        
        float startY = chartBottom + 80f;
        
        canvas.drawText("Current Conditions:", chartLeft, startY, textPaint);
        canvas.drawText(String.format("Temperature: %.1f°C", current.temperature), chartLeft, startY + 25f, textPaint);
        canvas.drawText(String.format("UV Index: %.1f", current.uvIndex), chartLeft, startY + 45f, textPaint);
        canvas.drawText(String.format("Air Quality: %d AQI", current.airQuality), chartLeft, startY + 65f, textPaint);
        canvas.drawText(String.format("Humidity: %.0f%%", current.humidity), chartLeft + 200f, startY + 25f, textPaint);
        canvas.drawText(String.format("Wind: %.1f mph", current.windSpeed), chartLeft + 200f, startY + 45f, textPaint);
        canvas.drawText(String.format("Condition: %s", current.condition), chartLeft + 200f, startY + 65f, textPaint);
    }
    
    private String getMetricTitle(String metric) {
        switch (metric) {
            case "temperature": return "Temperature (°C)";
            case "uv": return "UV Index";
            case "air_quality": return "Air Quality Index";
            case "all": return "Environmental Overview";
            default: return "Environmental Data";
        }
    }
    
    private int getTemperatureColor(float temperature) {
        if (temperature < 10) return TEMP_COLD_COLOR;
        if (temperature < 25) return TEMP_MODERATE_COLOR;
        return TEMP_HOT_COLOR;
    }
    
    private int getUVColor(float uvIndex) {
        if (uvIndex < 3) return UV_LOW_COLOR;
        if (uvIndex < 8) return UV_MODERATE_COLOR;
        return UV_HIGH_COLOR;
    }
    
    private int getAirQualityColor(int aqi) {
        if (aqi < 50) return AQ_GOOD_COLOR;
        if (aqi < 150) return AQ_MODERATE_COLOR;
        return AQ_POOR_COLOR;
    }
    
    public void setSelectedMetric(String metric) {
        this.selectedMetric = metric;
        invalidate();
    }
    
    public void setTimelineData(List<EnvironmentalData> data) {
        this.timelineData = new ArrayList<>(data);
        invalidate();
    }
    
    // Data class for environmental information
    public static class EnvironmentalData {
        public final Date timestamp;
        public final float temperature;
        public final float uvIndex;
        public final int airQuality;
        public final float humidity;
        public final float windSpeed;
        public final String condition;
        
        public EnvironmentalData(Date timestamp, float temperature, float uvIndex, 
                               int airQuality, float humidity, float windSpeed, String condition) {
            this.timestamp = timestamp;
            this.temperature = temperature;
            this.uvIndex = uvIndex;
            this.airQuality = airQuality;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.condition = condition;
        }
    }
}