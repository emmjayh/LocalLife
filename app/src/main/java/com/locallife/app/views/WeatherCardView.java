package com.locallife.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.locallife.app.R;

public class WeatherCardView extends LinearLayout {
    private TextView tvTemperature;
    private TextView tvCondition;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvFeelsLike;
    private ImageView ivWeatherIcon;
    private View backgroundView;
    
    private WeatherData weatherData;
    
    public static class WeatherData {
        public String temperature;
        public String condition;
        public String humidity;
        public String windSpeed;
        public String feelsLike;
        public int iconResId;
        public int backgroundColorResId;
        
        public WeatherData(String temperature, String condition, String humidity, 
                          String windSpeed, String feelsLike, int iconResId, int backgroundColorResId) {
            this.temperature = temperature;
            this.condition = condition;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.feelsLike = feelsLike;
            this.iconResId = iconResId;
            this.backgroundColorResId = backgroundColorResId;
        }
    }
    
    public WeatherCardView(Context context) {
        super(context);
        init();
    }
    
    public WeatherCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WeatherCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        inflate(getContext(), R.layout.view_weather_card, this);
        
        tvTemperature = findViewById(R.id.tv_temperature);
        tvCondition = findViewById(R.id.tv_condition);
        tvHumidity = findViewById(R.id.tv_humidity);
        tvWind = findViewById(R.id.tv_wind);
        tvFeelsLike = findViewById(R.id.tv_feels_like);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);
        backgroundView = findViewById(R.id.weather_background);
        
        // Set default values
        setWeatherData(new WeatherData("--°", "Loading...", "--", "--", "--", 
                                     R.drawable.ic_weather, R.color.primary_light));
    }
    
    public void setWeatherData(WeatherData data) {
        this.weatherData = data;
        updateDisplay();
    }
    
    private void updateDisplay() {
        if (weatherData == null) return;
        
        tvTemperature.setText(weatherData.temperature);
        tvCondition.setText(weatherData.condition);
        tvHumidity.setText("Humidity: " + weatherData.humidity);
        tvWind.setText("Wind: " + weatherData.windSpeed);
        tvFeelsLike.setText("Feels like " + weatherData.feelsLike);
        
        ivWeatherIcon.setImageResource(weatherData.iconResId);
        backgroundView.setBackgroundColor(ContextCompat.getColor(getContext(), weatherData.backgroundColorResId));
        
        // Add subtle animation
        animateWeatherUpdate();
    }
    
    private void animateWeatherUpdate() {
        setAlpha(0.7f);
        animate()
            .alpha(1.0f)
            .setDuration(300)
            .start();
    }
    
    public void showLoading() {
        setWeatherData(new WeatherData("--°", "Loading...", "--", "--", "--", 
                                     R.drawable.ic_weather, R.color.primary_light));
    }
    
    public void showError() {
        setWeatherData(new WeatherData("--°", "Error loading weather", "--", "--", "--", 
                                     R.drawable.ic_weather, R.color.error));
    }
    
    // Custom background drawing for weather effects
    public static class WeatherEffectsView extends View {
        private Paint paintRain;
        private Paint paintSnow;
        private Paint paintSun;
        private Path sunPath;
        
        private String weatherType = "clear";
        private float animationProgress = 0f;
        
        public WeatherEffectsView(Context context) {
            super(context);
            init();
        }
        
        public WeatherEffectsView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }
        
        private void init() {
            paintRain = new Paint();
            paintRain.setColor(ContextCompat.getColor(getContext(), R.color.primary_light));
            paintRain.setStrokeWidth(2);
            paintRain.setAntiAlias(true);
            
            paintSnow = new Paint();
            paintSnow.setColor(ContextCompat.getColor(getContext(), R.color.white));
            paintSnow.setAntiAlias(true);
            
            paintSun = new Paint();
            paintSun.setColor(ContextCompat.getColor(getContext(), R.color.warning));
            paintSun.setAntiAlias(true);
            
            sunPath = new Path();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            switch (weatherType) {
                case "rain":
                    drawRain(canvas);
                    break;
                case "snow":
                    drawSnow(canvas);
                    break;
                case "sunny":
                    drawSunRays(canvas);
                    break;
            }
        }
        
        private void drawRain(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            
            for (int i = 0; i < 20; i++) {
                float x = (width / 20f) * i;
                float y = (animationProgress * height) % height;
                canvas.drawLine(x, y, x + 5, y + 20, paintRain);
            }
        }
        
        private void drawSnow(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            
            for (int i = 0; i < 15; i++) {
                float x = (width / 15f) * i;
                float y = (animationProgress * height) % height;
                canvas.drawCircle(x, y, 3, paintSnow);
            }
        }
        
        private void drawSunRays(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            
            // Draw sun rays
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2 * i) / 8;
                float startX = centerX + (float) Math.cos(angle) * 30;
                float startY = centerY + (float) Math.sin(angle) * 30;
                float endX = centerX + (float) Math.cos(angle) * 50;
                float endY = centerY + (float) Math.sin(angle) * 50;
                
                canvas.drawLine(startX, startY, endX, endY, paintSun);
            }
        }
        
        public void setWeatherType(String type) {
            this.weatherType = type;
            invalidate();
        }
        
        public void startAnimation() {
            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(2000);
            animator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            animator.addUpdateListener(animation -> {
                animationProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }
    }
}