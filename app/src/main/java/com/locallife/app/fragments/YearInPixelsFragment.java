package com.locallife.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.locallife.app.R;
import com.locallife.app.views.YearInPixelsView;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment for displaying the Year in Pixels visualization
 */
public class YearInPixelsFragment extends Fragment {
    
    private YearInPixelsView yearInPixelsView;
    private Spinner spinnerYear;
    private Spinner spinnerMetric;
    private Button btnRefresh;
    private TextView tvYearStats;
    private TextView tvDescription;
    private CardView cardYearPixels;
    private CardView cardControls;
    private CardView cardStatistics;
    
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private int selectedYear;
    private String selectedMetric = "activity_score";
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_year_in_pixels, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        databaseHelper = DatabaseHelper.getInstance(getContext());
        selectedYear = Calendar.getInstance().get(Calendar.YEAR);
        
        initializeViews(view);
        setupSpinners();
        setupClickListeners();
        loadYearData();
    }
    
    private void initializeViews(View view) {
        yearInPixelsView = view.findViewById(R.id.year_in_pixels_view);
        spinnerYear = view.findViewById(R.id.spinner_year);
        spinnerMetric = view.findViewById(R.id.spinner_metric);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        tvYearStats = view.findViewById(R.id.tv_year_stats);
        tvDescription = view.findViewById(R.id.tv_description);
        cardYearPixels = view.findViewById(R.id.card_year_pixels);
        cardControls = view.findViewById(R.id.card_controls);
        cardStatistics = view.findViewById(R.id.card_statistics);
    }
    
    private void setupSpinners() {
        // Setup year spinner
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = currentYear - 5; year <= currentYear + 1; year++) {
            years.add(String.valueOf(year));
        }
        
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        
        // Set current year as default
        int currentYearIndex = years.indexOf(String.valueOf(currentYear));
        if (currentYearIndex >= 0) {
            spinnerYear.setSelection(currentYearIndex);
        }
        
        // Setup metric spinner
        String[] metricLabels = yearInPixelsView.getMetricLabels();
        ArrayAdapter<String> metricAdapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, metricLabels);
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMetric.setAdapter(metricAdapter);
        
        // Setup listeners
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(years.get(position));
                yearInPixelsView.setSelectedYear(selectedYear);
                loadYearData();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        spinnerMetric.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] metrics = yearInPixelsView.getAvailableMetrics();
                selectedMetric = metrics[position];
                yearInPixelsView.setSelectedMetric(selectedMetric);
                updateDescription();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> loadYearData());
        
        // Add click listener to year pixels view for interaction feedback
        yearInPixelsView.setOnClickListener(v -> {
            // Could add additional interaction here if needed
        });
    }
    
    private void loadYearData() {
        // Load actual data from database
        Map<String, YearInPixelsView.DayData> yearData = loadYearDataFromDatabase();
        
        if (yearData.isEmpty()) {
            // If no data available, the view will show sample data
            updateStatistics(null);
        } else {
            yearInPixelsView.setDayData(yearData);
            updateStatistics(yearData);
        }
        
        updateDescription();
    }
    
    private Map<String, YearInPixelsView.DayData> loadYearDataFromDatabase() {
        Map<String, YearInPixelsView.DayData> yearData = new HashMap<>();
        
        // Get all day records for the selected year
        List<DayRecord> dayRecords = databaseHelper.getAllDayRecords();
        
        for (DayRecord record : dayRecords) {
            if (record.getDate().startsWith(String.valueOf(selectedYear))) {
                YearInPixelsView.DayData dayData = new YearInPixelsView.DayData(
                    record.getActivityScore(),
                    record.getStepCount(),
                    record.getPlacesVisited(),
                    record.getScreenTimeMinutes(),
                    record.getTotalMediaMinutes()
                );
                
                yearData.put(record.getDate(), dayData);
            }
        }
        
        return yearData;
    }
    
    private void updateStatistics(Map<String, YearInPixelsView.DayData> yearData) {
        if (yearData == null || yearData.isEmpty()) {
            tvYearStats.setText(String.format(Locale.getDefault(), 
                "No data available for %d\\nShowing sample data patterns", selectedYear));
            return;
        }
        
        // Calculate statistics
        int totalDays = yearData.size();
        int activeDays = 0;
        float totalValue = 0f;
        float maxValue = 0f;
        
        for (YearInPixelsView.DayData dayData : yearData.values()) {
            float value = getMetricValue(dayData, selectedMetric);
            totalValue += value;
            maxValue = Math.max(maxValue, value);
            
            if (value > 0) {
                activeDays++;
            }
        }
        
        float averageValue = totalDays > 0 ? totalValue / totalDays : 0f;
        
        // Calculate percentage of year with data
        float dataPercentage = (totalDays / 365f) * 100f;
        
        // Format statistics text
        String statsText = String.format(Locale.getDefault(),
            "Year %d Statistics:\\n" +
            "• Data Coverage: %.1f%% of year (%d days)\\n" +
            "• Active Days: %d (%.1f%%)\\n" +
            "• Average %s: %.1f\\n" +
            "• Peak %s: %.1f",
            selectedYear, dataPercentage, totalDays,
            activeDays, (activeDays / (float) totalDays) * 100f,
            getMetricLabel(selectedMetric), averageValue,
            getMetricLabel(selectedMetric), maxValue);
        
        tvYearStats.setText(statsText);
    }
    
    private void updateDescription() {
        String description = getMetricDescription(selectedMetric);
        tvDescription.setText(description);
    }
    
    private String getMetricDescription(String metric) {
        switch (metric) {
            case "activity_score":
                return "Overall activity score combining steps, places visited, screen time, and other factors. Higher scores indicate more active and balanced days.";
            case "steps":
                return "Daily step count from your device's step counter. Darker colors represent days with more steps taken.";
            case "places":
                return "Number of unique places visited each day. Shows your exploration and mobility patterns throughout the year.";
            case "screen_time":
                return "Daily screen time in minutes. Darker colors indicate more screen usage. Moderate usage is generally healthier.";
            case "media_consumption":
                return "Time spent consuming media (TV, movies, music, etc.) in minutes. Shows your entertainment consumption patterns.";
            default:
                return "Activity data visualization showing patterns throughout the year.";
        }
    }
    
    private float getMetricValue(YearInPixelsView.DayData dayData, String metric) {
        switch (metric) {
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
    
    private String getMetricLabel(String metric) {
        String[] metrics = yearInPixelsView.getAvailableMetrics();
        String[] labels = yearInPixelsView.getMetricLabels();
        
        for (int i = 0; i < metrics.length; i++) {
            if (metrics[i].equals(metric)) {
                return labels[i];
            }
        }
        
        return "Activity Score";
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadYearData();
    }
}