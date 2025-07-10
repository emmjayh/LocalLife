package com.locallife.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.locallife.app.R;
import com.locallife.app.views.ActivityRingsView;
import com.locallife.app.views.EnvironmentalTimelineView;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment showcasing advanced visualizations including Activity Rings and Environmental Timeline
 */
public class VisualizationsFragment extends Fragment {
    
    private ActivityRingsView activityRingsView;
    private EnvironmentalTimelineView environmentalTimelineView;
    private Button btnRefreshData;
    private Button btnTimelineTemperature;
    private Button btnTimelineUV;
    private Button btnTimelineAQ;
    private Button btnTimelineAll;
    private TextView tvVisualizationTitle;
    private CardView cardActivityRings;
    private CardView cardEnvironmentalTimeline;
    
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visualizations, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        databaseHelper = DatabaseHelper.getInstance(getContext());
        
        initializeViews(view);
        setupClickListeners();
        loadVisualizationData();
    }
    
    private void initializeViews(View view) {
        activityRingsView = view.findViewById(R.id.activity_rings_view);
        environmentalTimelineView = view.findViewById(R.id.environmental_timeline_view);
        btnRefreshData = view.findViewById(R.id.btn_refresh_data);
        btnTimelineTemperature = view.findViewById(R.id.btn_timeline_temperature);
        btnTimelineUV = view.findViewById(R.id.btn_timeline_uv);
        btnTimelineAQ = view.findViewById(R.id.btn_timeline_aq);
        btnTimelineAll = view.findViewById(R.id.btn_timeline_all);
        tvVisualizationTitle = view.findViewById(R.id.tv_visualization_title);
        cardActivityRings = view.findViewById(R.id.card_activity_rings);
        cardEnvironmentalTimeline = view.findViewById(R.id.card_environmental_timeline);
    }
    
    private void setupClickListeners() {
        btnRefreshData.setOnClickListener(v -> loadVisualizationData());
        
        btnTimelineTemperature.setOnClickListener(v -> {
            environmentalTimelineView.setSelectedMetric("temperature");
            updateTimelineButtonSelection("temperature");
        });
        
        btnTimelineUV.setOnClickListener(v -> {
            environmentalTimelineView.setSelectedMetric("uv");
            updateTimelineButtonSelection("uv");
        });
        
        btnTimelineAQ.setOnClickListener(v -> {
            environmentalTimelineView.setSelectedMetric("air_quality");
            updateTimelineButtonSelection("air_quality");
        });
        
        btnTimelineAll.setOnClickListener(v -> {
            environmentalTimelineView.setSelectedMetric("all");
            updateTimelineButtonSelection("all");
        });
        
        // Add click listeners for activity rings interaction
        activityRingsView.setOnClickListener(v -> {
            // Start animation on click
            activityRingsView.startAnimation();
        });
    }
    
    private void updateTimelineButtonSelection(String selectedMetric) {
        // Reset all buttons
        btnTimelineTemperature.setSelected(false);
        btnTimelineUV.setSelected(false);
        btnTimelineAQ.setSelected(false);
        btnTimelineAll.setSelected(false);
        
        // Set selected button
        switch (selectedMetric) {
            case "temperature":
                btnTimelineTemperature.setSelected(true);
                break;
            case "uv":
                btnTimelineUV.setSelected(true);
                break;
            case "air_quality":
                btnTimelineAQ.setSelected(true);
                break;
            case "all":
                btnTimelineAll.setSelected(true);
                break;
        }
    }
    
    private void loadVisualizationData() {
        loadActivityRingsData();
        loadEnvironmentalTimelineData();
    }
    
    private void loadActivityRingsData() {
        // Get today's record
        String today = dateFormat.format(new Date());
        DayRecord todayRecord = databaseHelper.getDayRecord(today);
        
        List<ActivityRingsView.RingData> ringData = new ArrayList<>();
        
        if (todayRecord != null) {
            // Steps ring
            float stepsProgress = Math.min(1.0f, todayRecord.getStepCount() / 10000.0f);
            ringData.add(new ActivityRingsView.RingData(
                "Steps", 
                stepsProgress, 
                String.format(Locale.getDefault(), "%,d", todayRecord.getStepCount()),
                "of 10,000 goal"
            ));
            
            // Places ring
            float placesProgress = Math.min(1.0f, todayRecord.getPlacesVisited() / 20.0f);
            ringData.add(new ActivityRingsView.RingData(
                "Places", 
                placesProgress, 
                String.valueOf(todayRecord.getPlacesVisited()),
                "locations visited"
            ));
            
            // Screen time ring (inverted - less is better)
            float screenTimeHours = todayRecord.getScreenTimeMinutes() / 60.0f;
            float screenTimeProgress = Math.max(0.0f, Math.min(1.0f, (8.0f - screenTimeHours) / 8.0f));
            ringData.add(new ActivityRingsView.RingData(
                "Screen Time", 
                screenTimeProgress, 
                String.format(Locale.getDefault(), "%.1fh", screenTimeHours),
                "digital wellness"
            ));
            
            // Media consumption ring
            float mediaHours = todayRecord.getTotalMediaMinutes() / 60.0f;
            float mediaProgress = todayRecord.getMediaConsumptionScore() / 100.0f;
            ringData.add(new ActivityRingsView.RingData(
                "Media", 
                mediaProgress, 
                String.format(Locale.getDefault(), "%.1fh", mediaHours),
                "consumed today"
            ));
            
            // Calculate overall progress for center text
            float totalProgress = 0f;
            for (ActivityRingsView.RingData ring : ringData) {
                totalProgress += ring.progress;
            }
            float averageProgress = totalProgress / ringData.size();
            
            activityRingsView.setCenterText(
                String.format(Locale.getDefault(), "%.0f%%", averageProgress * 100),
                "Overall Score"
            );
        } else {
            // No data available - show sample data
            ringData.add(new ActivityRingsView.RingData("Steps", 0.0f, "0", "no data"));
            ringData.add(new ActivityRingsView.RingData("Places", 0.0f, "0", "no data"));
            ringData.add(new ActivityRingsView.RingData("Screen Time", 0.0f, "0h", "no data"));
            ringData.add(new ActivityRingsView.RingData("Media", 0.0f, "0h", "no data"));
            
            activityRingsView.setCenterText("0%", "No Data");
        }
        
        activityRingsView.setRingData(ringData);
    }
    
    private void loadEnvironmentalTimelineData() {
        List<EnvironmentalTimelineView.EnvironmentalData> timelineData = new ArrayList<>();
        
        // Get environmental data for the last 24 hours
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -24);
        
        for (int i = 0; i < 24; i++) {
            Date timestamp = calendar.getTime();
            String dateString = dateFormat.format(timestamp);
            
            // Try to get real environmental data
            DayRecord record = databaseHelper.getDayRecord(dateString);
            
            if (record != null) {
                timelineData.add(new EnvironmentalTimelineView.EnvironmentalData(
                    timestamp,
                    record.getTemperature(),
                    (float) record.getUvIndex(),
                    record.getAirQualityIndex(),
                    record.getHumidity(),
                    record.getWindSpeed(),
                    record.getWeatherCondition() != null ? record.getWeatherCondition() : "Unknown"
                ));
            } else {
                // Generate sample data if no real data available
                float temperature = 15f + (float) (Math.sin(i * Math.PI / 12) * 10) + (float) (Math.random() * 5);
                float uvIndex = Math.max(0, (float) (Math.sin((i - 6) * Math.PI / 12) * 8) + (float) (Math.random() * 2));
                int airQuality = 50 + (int) (Math.random() * 100);
                float humidity = 40f + (float) (Math.random() * 40);
                float windSpeed = (float) (Math.random() * 20);
                String condition = (i >= 6 && i <= 18) ? "Sunny" : "Clear";
                
                timelineData.add(new EnvironmentalTimelineView.EnvironmentalData(
                    timestamp, temperature, uvIndex, airQuality, humidity, windSpeed, condition
                ));
            }
            
            calendar.add(Calendar.HOUR, 1);
        }
        
        environmentalTimelineView.setTimelineData(timelineData);
        
        // Set default selection
        environmentalTimelineView.setSelectedMetric("temperature");
        updateTimelineButtonSelection("temperature");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadVisualizationData();
    }
}