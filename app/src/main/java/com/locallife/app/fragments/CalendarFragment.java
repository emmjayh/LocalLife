package com.locallife.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.locallife.app.R;
import com.locallife.app.adapters.EventAdapter;
import com.locallife.app.models.Event;
import com.locallife.app.views.ActivityHeatMapView;
import com.locallife.app.views.StatsCardView;
import com.locallife.model.DayRecord;
import com.locallife.database.DatabaseHelper;
import com.locallife.service.EnvironmentalInsightsService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment implements EventAdapter.OnEventClickListener, ActivityHeatMapView.OnDateClickListener {

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private TextView tvEventCount;
    private RecyclerView rvDayEvents;
    private FloatingActionButton fabAddEvent;
    private ActivityHeatMapView heatMapView;
    private StatsCardView statsSteps;
    private StatsCardView statsPlaces;
    private StatsCardView statsActivities;
    private TextView tvDayStats;
    private View dayStatsContainer;
    private long selectedDateMillis;
    private EventAdapter eventAdapter;
    private List<Event> allEvents = new ArrayList<>();
    private Map<String, Integer> activityData = new HashMap<>();
    private DatabaseHelper databaseHelper;
    private EnvironmentalInsightsService environmentalInsightsService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize services
        databaseHelper = DatabaseHelper.getInstance(getContext());
        environmentalInsightsService = new EnvironmentalInsightsService(getContext());
        
        initializeViews(view);
        setupCalendar();
        setupRecyclerView();
        setupFab();
        setupHeatMap();
        setupStatsCards();
        loadActivityData();
    }

    private void initializeViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvEventCount = view.findViewById(R.id.tv_event_count);
        rvDayEvents = view.findViewById(R.id.rv_day_events);
        fabAddEvent = view.findViewById(R.id.fab_add_event);
        heatMapView = view.findViewById(R.id.heat_map_view);
        statsSteps = view.findViewById(R.id.stats_steps);
        statsPlaces = view.findViewById(R.id.stats_places);
        statsActivities = view.findViewById(R.id.stats_activities);
        tvDayStats = view.findViewById(R.id.tv_day_stats);
        dayStatsContainer = view.findViewById(R.id.day_stats_container);
    }

    private void setupCalendar() {
        // Set current date
        selectedDateMillis = System.currentTimeMillis();
        updateSelectedDateDisplay();
        
        // Set calendar change listener
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                selectedDateMillis = calendar.getTimeInMillis();
                updateSelectedDateDisplay();
                loadEventsForDate();
            }
        });
    }

    private void setupRecyclerView() {
        rvDayEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDayEvents.setNestedScrollingEnabled(false);
        
        // Initialize adapter
        eventAdapter = new EventAdapter(this);
        rvDayEvents.setAdapter(eventAdapter);
        
        // Load events for current date
        loadEventsForDate();
    }

    private void setupFab() {
        fabAddEvent.setOnClickListener(v -> {
            // Open add event dialog or activity
            openAddEventDialog();
        });
    }

    private void updateSelectedDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String dateString = dateFormat.format(selectedDateMillis);
        tvSelectedDate.setText(dateString);
    }

    private void loadEventsForDate() {
        // Filter events for selected date
        List<Event> dayEvents = new ArrayList<>();
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDateMillis);
        
        for (Event event : allEvents) {
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTimeInMillis(event.getStartTime());
            
            if (selectedCal.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                selectedCal.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)) {
                dayEvents.add(event);
            }
        }
        
        eventAdapter.setEvents(dayEvents);
        tvEventCount.setText(String.format(Locale.getDefault(), "%d events", dayEvents.size()));
        
        // Update day statistics
        updateDayStats();
    }

    private void openAddEventDialog() {
        // TODO: Implement add event dialog
        // For now, add sample event
        Event sampleEvent = new Event("Sample Event", "This is a sample event", 
            System.currentTimeMillis(), System.currentTimeMillis() + 3600000, 
            "Sample Location", "meeting");
        allEvents.add(sampleEvent);
        loadEventsForDate();
    }
    
    private void setupHeatMap() {
        heatMapView.setOnDateClickListener(this);
        
        // Generate sample activity data
        generateSampleActivityData();
        heatMapView.setActivityData(activityData);
    }
    
    private void setupStatsCards() {
        statsSteps.setTitle("Steps");
        statsSteps.setIcon(R.drawable.ic_dashboard);
        statsSteps.setProgressColor(getResources().getColor(R.color.success));
        
        statsPlaces.setTitle("Places");
        statsPlaces.setIcon(R.drawable.ic_location);
        statsPlaces.setProgressColor(getResources().getColor(R.color.primary));
        
        statsActivities.setTitle("Activities");
        statsActivities.setIcon(R.drawable.ic_event);
        statsActivities.setProgressColor(getResources().getColor(R.color.accent));
    }
    
    private void loadActivityData() {
        // Load activity data from database
        loadHeatMapData();
        updateStatsCards();
    }
    
    private void loadHeatMapData() {
        try {
            // Get the last 365 days of data
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -365);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Map<String, Integer> heatMapData = new HashMap<>();
            
            // Load data for each day
            for (int i = 0; i < 365; i++) {
                String date = dateFormat.format(calendar.getTime());
                DayRecord dayRecord = databaseHelper.getDayRecord(date);
                
                if (dayRecord != null) {
                    // Load environmental data for this day
                    databaseHelper.loadEnvironmentalData(dayRecord, date);
                    
                    // Convert activity score to heat map intensity (0-4)
                    int intensity = (int) (dayRecord.getActivityScore() / 25); // 0-100 -> 0-4
                    intensity = Math.max(0, Math.min(4, intensity));
                    heatMapData.put(date, intensity);
                } else {
                    heatMapData.put(date, 0); // No data = no activity
                }
                
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            // Update heat map view
            if (heatMapView != null) {
                heatMapView.setActivityData(heatMapData);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample data
            generateSampleHeatMapData();
        }
    }
    
    private void generateSampleHeatMapData() {
        // Generate sample data for demonstration
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -365);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, Integer> sampleData = new HashMap<>();
        
        for (int i = 0; i < 365; i++) {
            String date = dateFormat.format(calendar.getTime());
            int intensity = (int) (Math.random() * 5); // Random intensity 0-4
            sampleData.put(date, intensity);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        if (heatMapView != null) {
            heatMapView.setActivityData(sampleData);
        }
    }
    
    private void updateStatsCards() {
        try {
            // Get today's data
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(new java.util.Date());
            DayRecord todayRecord = databaseHelper.getDayRecord(today);
            
            if (todayRecord != null) {
                // Load environmental data
                databaseHelper.loadEnvironmentalData(todayRecord, today);
                
                // Update steps
                int steps = todayRecord.getStepCount();
                statsSteps.setValue(String.format("%,d", steps));
                statsSteps.setSubtitle("Daily Goal: 10,000");
                statsSteps.setProgress(Math.min(100, (steps / 10000.0f) * 100));
                
                // Update places
                int places = todayRecord.getPlacesVisited();
                statsPlaces.setValue(String.valueOf(places));
                statsPlaces.setSubtitle("Unique locations");
                statsPlaces.setProgress(Math.min(100, places * 8.33f)); // 12 places = 100%
                
                // Update activities (use overall activity score)
                float activityScore = todayRecord.getActivityScore();
                statsActivities.setValue(String.format("%.0f", activityScore));
                statsActivities.setSubtitle("Activity score");
                statsActivities.setProgress(activityScore);
                
            } else {
                // No data available - use defaults
                statsSteps.setValue("0");
                statsSteps.setSubtitle("Daily Goal: 10,000");
                statsSteps.setProgress(0);
                
                statsPlaces.setValue("0");
                statsPlaces.setSubtitle("Unique locations");
                statsPlaces.setProgress(0);
                
                statsActivities.setValue("0");
                statsActivities.setSubtitle("Activity score");
                statsActivities.setProgress(0);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to sample data
            statsSteps.setValue("8,543");
            statsSteps.setSubtitle("Daily Goal: 10,000");
            statsSteps.setProgress(85.4f);
            
            statsPlaces.setValue("12");
            statsPlaces.setSubtitle("Unique locations");
            statsPlaces.setProgress(60.0f);
            
            statsActivities.setValue("24");
            statsActivities.setSubtitle("Events tracked");
            statsActivities.setProgress(75.0f);
        }
    }
    
    private void updateDayStats() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);
        String dateKey = String.format("%04d-%02d-%02d", 
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH));
        
        int activityLevel = activityData.getOrDefault(dateKey, 0);
        
        StringBuilder stats = new StringBuilder();
        stats.append("Activity Level: ").append(activityLevel).append("/4\n");
        stats.append("Events: ").append(eventAdapter.getItemCount()).append("\n");
        
        // Add more stats based on activity level
        switch (activityLevel) {
            case 0:
                stats.append("Quiet day - low activity");
                break;
            case 1:
                stats.append("Light activity day");
                break;
            case 2:
                stats.append("Moderate activity day");
                break;
            case 3:
                stats.append("Active day");
                break;
            case 4:
                stats.append("Very active day");
                break;
        }
        
        tvDayStats.setText(stats.toString());
        dayStatsContainer.setVisibility(View.VISIBLE);
    }
    
    private void generateSampleActivityData() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -365); // Go back 1 year
        
        for (int i = 0; i < 365; i++) {
            String dateKey = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
            
            // Generate random activity level (0-4)
            int activityLevel = (int) (Math.random() * 5);
            activityData.put(dateKey, activityLevel);
            
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }
    
    @Override
    public void onEventClick(Event event) {
        // TODO: Show event details
    }
    
    @Override
    public void onEventLongClick(Event event) {
        // TODO: Show event options (edit, delete)
    }
    
    @Override
    public void onDateClick(String date, int activityLevel) {
        // Parse date and update calendar
        String[] parts = date.split("-");
        if (parts.length == 3) {
            try {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Calendar months are 0-based
                int day = Integer.parseInt(parts[2]);
                
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);
                selectedDateMillis = cal.getTimeInMillis();
                
                calendarView.setDate(selectedDateMillis);
                updateSelectedDateDisplay();
                loadEventsForDate();
            } catch (NumberFormatException e) {
                // Handle parsing error
            }
        }
    }
}