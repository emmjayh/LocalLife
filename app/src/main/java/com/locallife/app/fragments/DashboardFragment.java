package com.locallife.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.locallife.app.R;
import com.locallife.app.adapters.ActivityAdapter;
import com.locallife.app.adapters.InsightAdapter;
import com.locallife.app.views.WeatherCardView;
import com.locallife.app.views.StatsCardView;
import com.locallife.app.views.CircularProgressView;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.PhotoMetadata;
import com.locallife.service.PhotoMetadataService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment implements ActivityAdapter.OnActivityClickListener, InsightAdapter.OnInsightClickListener {

    private TextView tvWelcome;
    private TextView tvDate;
    private WeatherCardView weatherCard;
    private CardView cardUpcomingEvents;
    private CardView cardLocalNews;
    private CardView cardCommunityPosts;
    private RecyclerView rvUpcomingEvents;
    private RecyclerView rvRecentActivities;
    private RecyclerView rvInsights;
    private StatsCardView statsSteps;
    private StatsCardView statsPlaces;
    private StatsCardView statsScreenTime;
    private StatsCardView statsBattery;
    private StatsCardView statsPhotos;
    private CircularProgressView dailyGoalProgress;
    private TextView tvDailyGoalText;
    private ActivityAdapter activityAdapter;
    private InsightAdapter insightAdapter;
    private Handler updateHandler;
    private Runnable updateRunnable;
    private DatabaseHelper databaseHelper;
    private PhotoMetadataService photoMetadataService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize services
        databaseHelper = DatabaseHelper.getInstance(getContext());
        photoMetadataService = new PhotoMetadataService(getContext());
        
        initializeViews(view);
        setupRecyclerViews();
        setupStatsCards();
        setupRealtimeUpdates();
        updateUI();
        loadRecentActivities();
        loadInsights();
    }

    private void initializeViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvDate = view.findViewById(R.id.tv_date);
        weatherCard = view.findViewById(R.id.weather_card);
        cardUpcomingEvents = view.findViewById(R.id.card_upcoming_events);
        cardLocalNews = view.findViewById(R.id.card_local_news);
        cardCommunityPosts = view.findViewById(R.id.card_community_posts);
        rvUpcomingEvents = view.findViewById(R.id.rv_upcoming_events);
        rvRecentActivities = view.findViewById(R.id.rv_recent_activities);
        rvInsights = view.findViewById(R.id.rv_insights);
        statsSteps = view.findViewById(R.id.stats_steps);
        statsPlaces = view.findViewById(R.id.stats_places);
        statsScreenTime = view.findViewById(R.id.stats_screen_time);
        statsBattery = view.findViewById(R.id.stats_battery);
        statsPhotos = view.findViewById(R.id.stats_photos);
        dailyGoalProgress = view.findViewById(R.id.daily_goal_progress);
        tvDailyGoalText = view.findViewById(R.id.tv_daily_goal_text);
    }

    private void setupRecyclerViews() {
        // Setup upcoming events RecyclerView
        rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcomingEvents.setNestedScrollingEnabled(false);
        
        // Setup recent activities RecyclerView
        rvRecentActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentActivities.setNestedScrollingEnabled(false);
        activityAdapter = new ActivityAdapter(this);
        rvRecentActivities.setAdapter(activityAdapter);
        
        // Setup insights RecyclerView
        rvInsights.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInsights.setNestedScrollingEnabled(false);
        insightAdapter = new InsightAdapter(this);
        rvInsights.setAdapter(insightAdapter);
    }

    private void updateUI() {
        // Set welcome message with time-based greeting
        String greeting = getTimeBasedGreeting();
        tvWelcome.setText(greeting + "!");
        
        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        tvDate.setText(dateFormat.format(new Date()));
        
        // Update weather card
        updateWeatherCard();
        
        // Update stats cards
        updateStatsCards();
        
        // Update daily goal progress
        updateDailyGoalProgress();
        
        // Setup click listeners for cards
        cardUpcomingEvents.setOnClickListener(v -> {
            // Navigate to events section
        });
        
        cardLocalNews.setOnClickListener(v -> {
            // Navigate to news section
        });
        
        cardCommunityPosts.setOnClickListener(v -> {
            // Navigate to community section
        });
    }
    
    private void setupStatsCards() {
        statsSteps.setTitle("Steps");
        statsSteps.setIcon(R.drawable.ic_dashboard);
        statsSteps.setProgressColor(getResources().getColor(R.color.success));
        statsSteps.setShowCircularProgress(true);
        
        statsPlaces.setTitle("Places");
        statsPlaces.setIcon(R.drawable.ic_location);
        statsPlaces.setProgressColor(getResources().getColor(R.color.primary));
        
        statsScreenTime.setTitle("Screen Time");
        statsScreenTime.setIcon(R.drawable.ic_settings);
        statsScreenTime.setProgressColor(getResources().getColor(R.color.warning));
        
        statsBattery.setTitle("Battery");
        statsBattery.setIcon(R.drawable.ic_info);
        statsBattery.setProgressColor(getResources().getColor(R.color.accent));
        
        statsPhotos.setTitle("Photos");
        statsPhotos.setIcon(R.drawable.ic_camera);
        statsPhotos.setProgressColor(getResources().getColor(R.color.secondary));
    }
    
    private void setupRealtimeUpdates() {
        updateHandler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateStatsCards();
                updateDailyGoalProgress();
                updateHandler.postDelayed(this, 30000); // Update every 30 seconds
            }
        };
        updateHandler.postDelayed(updateRunnable, 1000); // Start after 1 second
    }
    
    private String getTimeBasedGreeting() {
        int hour = new Date().getHours();
        if (hour < 12) {
            return "Good Morning";
        } else if (hour < 17) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }
    
    private void updateWeatherCard() {
        // TODO: Get actual weather data
        WeatherCardView.WeatherData weatherData = new WeatherCardView.WeatherData(
            "72°F", "Sunny", "65%", "5 mph", "75°F", 
            R.drawable.ic_weather, R.color.primary_light);
        weatherCard.setWeatherData(weatherData);
    }
    
    private void updateStatsCards() {
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        
        // Update steps (sample data)
        statsSteps.setValue("6,543");
        statsSteps.setSubtitle("Goal: 10,000");
        statsSteps.setProgress(65.4f);
        
        // Update places (sample data)
        statsPlaces.setValue("8");
        statsPlaces.setSubtitle("Locations visited");
        statsPlaces.setProgress(40.0f);
        
        // Update screen time (sample data)
        statsScreenTime.setValue("4h 23m");
        statsScreenTime.setSubtitle("Today");
        statsScreenTime.setProgress(87.5f);
        
        // Update battery (sample data)
        statsBattery.setValue("76%");
        statsBattery.setSubtitle("Good health");
        statsBattery.setProgress(76.0f);
        
        // Update photos with actual data
        updatePhotoStats(today);
    }
    
    private void updatePhotoStats(String date) {
        // Get photo count for today
        int photoCount = databaseHelper.getPhotoCountForDate(date);
        
        // Get photo activity score
        photoMetadataService.getPhotoActivityScore(date, new PhotoMetadataService.ActivityScoreCallback() {
            @Override
            public void onSuccess(int score) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        statsPhotos.setValue(String.valueOf(photoCount));
                        statsPhotos.setSubtitle("Photos taken today");
                        statsPhotos.setProgress(score);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        statsPhotos.setValue(String.valueOf(photoCount));
                        statsPhotos.setSubtitle("Photos taken today");
                        statsPhotos.setProgress(0);
                    });
                }
            }
        });
    }
    
    private void updateDailyGoalProgress() {
        // Calculate overall daily goal progress
        float stepsProgress = 65.4f;
        float placesProgress = 40.0f;
        float averageProgress = (stepsProgress + placesProgress) / 2;
        
        dailyGoalProgress.setProgress(averageProgress);
        dailyGoalProgress.setCenterText(String.format(Locale.getDefault(), "%.0f%%", averageProgress));
        
        String goalText = "Daily Goal Progress";
        if (averageProgress >= 100) {
            goalText += " - Complete!";
        } else if (averageProgress >= 75) {
            goalText += " - Almost there!";
        } else if (averageProgress >= 50) {
            goalText += " - Halfway there";
        } else {
            goalText += " - Keep going!";
        }
        tvDailyGoalText.setText(goalText);
    }
    
    private void loadRecentActivities() {
        List<ActivityAdapter.ActivityRecord> activities = new ArrayList<>();
        
        // Add sample activities
        activities.add(new ActivityAdapter.ActivityRecord(
            "steps", "Steps Recorded", "Walked around the neighborhood",
            System.currentTimeMillis() - 1800000, "Home", R.drawable.ic_dashboard,
            "2,156", "steps"));
        activities.add(new ActivityAdapter.ActivityRecord(
            "location", "Location Updated", "Arrived at new location",
            System.currentTimeMillis() - 3600000, "Coffee Shop", R.drawable.ic_location,
            "", ""));
        activities.add(new ActivityAdapter.ActivityRecord(
            "weather", "Weather Updated", "Sunny conditions detected",
            System.currentTimeMillis() - 7200000, "Current Location", R.drawable.ic_weather,
            "72°F", ""));
        
        // Load recent photo activities
        loadPhotoActivities(activities);
    }
    
    private void loadPhotoActivities(List<ActivityAdapter.ActivityRecord> activities) {
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        
        // Get photo metadata for today
        photoMetadataService.getPhotoMetadataForDate(today, new PhotoMetadataService.PhotoMetadataCallback() {
            @Override
            public void onSuccess(List<PhotoMetadata> metadata) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Add recent photo activities
                        int count = 0;
                        for (PhotoMetadata photo : metadata) {
                            if (count >= 3) break; // Only show 3 recent photos
                            
                            String description = "Photo taken";
                            if (photo.hasLocationData()) {
                                description += " at " + photo.getFormattedLocation();
                            }
                            
                            activities.add(new ActivityAdapter.ActivityRecord(
                                "photo", "Photo Captured", description,
                                photo.getDateTaken().getTime(),
                                photo.getLocationName() != null ? photo.getLocationName() : "Unknown location",
                                R.drawable.ic_camera,
                                photo.getFormattedDimensions(),
                                photo.getActivityType()
                            ));
                            count++;
                        }
                        
                        activityAdapter.setActivities(activities);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        activityAdapter.setActivities(activities);
                    });
                }
            }
        });
    }
    
    private void loadInsights() {
        List<InsightAdapter.Insight> insights = new ArrayList<>();
        
        // Add sample insights
        insights.add(new InsightAdapter.Insight(
            "Step Goal Progress", "You're 65% towards your daily step goal",
            "6,543 steps", "↑ 12% from yesterday", R.drawable.ic_dashboard, 2, "health",
            System.currentTimeMillis()));
        insights.add(new InsightAdapter.Insight(
            "New Location Discovered", "You visited 2 new places today",
            "8 locations", "↑ 2 new places", R.drawable.ic_location, 1, "activity",
            System.currentTimeMillis()));
        insights.add(new InsightAdapter.Insight(
            "Weather Impact", "Sunny weather increased your activity by 23%",
            "Active day", "↑ Weather correlation", R.drawable.ic_weather, 3, "weather",
            System.currentTimeMillis()));
        
        // Load photo-based insights
        loadPhotoInsights(insights);
    }
    
    private void loadPhotoInsights(List<InsightAdapter.Insight> insights) {
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        
        // Get photo frequency patterns
        photoMetadataService.getPhotoFrequencyPatterns(new PhotoMetadataService.PhotoFrequencyCallback() {
            @Override
            public void onSuccess(java.util.Map<String, Integer> patterns) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Analyze patterns and create insights
                        int outdoorPhotos = patterns.getOrDefault("outdoor", 0);
                        int socialPhotos = patterns.getOrDefault("social", 0);
                        int morningPhotos = patterns.getOrDefault("morning", 0);
                        int eveningPhotos = patterns.getOrDefault("evening", 0);
                        
                        // Create photo activity insight
                        int totalPhotos = databaseHelper.getPhotoCountForDate(today);
                        if (totalPhotos > 0) {
                            String insightText = "You captured " + totalPhotos + " photos today";
                            String change = "↑ Photo activity";
                            
                            if (outdoorPhotos > totalPhotos / 2) {
                                insightText += ", mostly outdoors";
                                change = "↑ Outdoor activity";
                            } else if (socialPhotos > totalPhotos / 3) {
                                insightText += ", with social moments";
                                change = "↑ Social activity";
                            }
                            
                            insights.add(new InsightAdapter.Insight(
                                "Photo Activity", insightText,
                                totalPhotos + " photos", change, R.drawable.ic_camera, 2, "photos",
                                System.currentTimeMillis()));
                        }
                        
                        // Create time-based photo insight
                        if (morningPhotos > 0 && eveningPhotos > 0) {
                            insights.add(new InsightAdapter.Insight(
                                "Golden Hour Photography", "You're capturing great light at sunrise and sunset",
                                "Morning & evening", "↑ Great timing", R.drawable.ic_camera, 3, "photos",
                                System.currentTimeMillis()));
                        }
                        
                        insightAdapter.setInsights(insights);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        insightAdapter.setInsights(insights);
                    });
                }
            }
        });
    }
    
    @Override
    public void onActivityClick(ActivityAdapter.ActivityRecord activity) {
        // TODO: Show activity details
    }
    
    @Override
    public void onInsightClick(InsightAdapter.Insight insight) {
        // TODO: Show insight details
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
        if (photoMetadataService != null) {
            photoMetadataService.shutdown();
        }
    }
}