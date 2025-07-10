package com.locallife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.R;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.ActivityType;
import com.locallife.model.DayRecord;
import com.locallife.model.PredictionResult;
import com.locallife.model.Recommendation;
import com.locallife.service.ActivityPredictionEngine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fragment for displaying activity recommendations and predictions
 */
public class ActivityRecommendationFragment extends Fragment {
    
    private ActivityPredictionEngine predictionEngine;
    private DatabaseHelper databaseHelper;
    
    // UI Components
    private LinearLayout llPredictionStatus;
    private ImageView ivStatusIcon;
    private TextView tvStatusTitle;
    private TextView tvStatusDescription;
    private TextView tvAccuracy;
    
    private LinearLayout llWeatherContext;
    private TextView tvWeatherTitle;
    private TextView tvWeatherDescription;
    private TextView tvTemperature;
    private TextView tvHumidity;
    
    private Button btnPersonalize;
    private Button btnViewStats;
    
    private LinearLayout llHighPriority;
    private LinearLayout llMediumPriority;
    private LinearLayout llLowPriority;
    
    private RecyclerView rvHighPriorityRecommendations;
    private RecyclerView rvMediumPriorityRecommendations;
    private RecyclerView rvLowPriorityRecommendations;
    
    private LinearLayout llWeeklyPredictions;
    private RecyclerView rvWeeklyPredictions;
    private TextView tvViewAllPredictions;
    
    private TextView tvPredictionAccuracy;
    private TextView tvTotalPredictions;
    private TextView tvBestActivity;
    private TextView tvConfidence;
    
    private LinearLayout llLoadingState;
    private LinearLayout llEmptyState;
    
    private ImageView ivRefresh;
    
    // Adapters
    private RecommendationAdapter highPriorityAdapter;
    private RecommendationAdapter mediumPriorityAdapter;
    private RecommendationAdapter lowPriorityAdapter;
    private WeeklyPredictionAdapter weeklyPredictionAdapter;
    
    // Data
    private List<Recommendation> allRecommendations;
    private Map<Date, List<PredictionResult>> weeklyPredictions;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize services
        predictionEngine = new ActivityPredictionEngine(getContext());
        databaseHelper = new DatabaseHelper(getContext());
        
        // Initialize data
        allRecommendations = new ArrayList<>();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_recommendations, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerViews();
        setupClickListeners();
        
        // Load initial data
        loadRecommendations();
        updateEngineStatus();
        updateWeatherContext();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews(View view) {
        // Status section
        llPredictionStatus = view.findViewById(R.id.llPredictionStatus);
        ivStatusIcon = view.findViewById(R.id.ivStatusIcon);
        tvStatusTitle = view.findViewById(R.id.tvStatusTitle);
        tvStatusDescription = view.findViewById(R.id.tvStatusDescription);
        tvAccuracy = view.findViewById(R.id.tvAccuracy);
        
        // Weather context
        llWeatherContext = view.findViewById(R.id.llWeatherContext);
        tvWeatherTitle = view.findViewById(R.id.tvWeatherTitle);
        tvWeatherDescription = view.findViewById(R.id.tvWeatherDescription);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        
        // Quick actions
        btnPersonalize = view.findViewById(R.id.btnPersonalize);
        btnViewStats = view.findViewById(R.id.btnViewStats);
        
        // Recommendation sections
        llHighPriority = view.findViewById(R.id.llHighPriority);
        llMediumPriority = view.findViewById(R.id.llMediumPriority);
        llLowPriority = view.findViewById(R.id.llLowPriority);
        
        rvHighPriorityRecommendations = view.findViewById(R.id.rvHighPriorityRecommendations);
        rvMediumPriorityRecommendations = view.findViewById(R.id.rvMediumPriorityRecommendations);
        rvLowPriorityRecommendations = view.findViewById(R.id.rvLowPriorityRecommendations);
        
        // Weekly predictions
        llWeeklyPredictions = view.findViewById(R.id.llWeeklyPredictions);
        rvWeeklyPredictions = view.findViewById(R.id.rvWeeklyPredictions);
        tvViewAllPredictions = view.findViewById(R.id.tvViewAllPredictions);
        
        // Insights
        tvPredictionAccuracy = view.findViewById(R.id.tvPredictionAccuracy);
        tvTotalPredictions = view.findViewById(R.id.tvTotalPredictions);
        tvBestActivity = view.findViewById(R.id.tvBestActivity);
        tvConfidence = view.findViewById(R.id.tvConfidence);
        
        // States
        llLoadingState = view.findViewById(R.id.llLoadingState);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        
        // Actions
        ivRefresh = view.findViewById(R.id.ivRefresh);
    }
    
    /**
     * Setup RecyclerViews with adapters
     */
    private void setupRecyclerViews() {
        // High priority recommendations
        highPriorityAdapter = new RecommendationAdapter(new ArrayList<>(), this::onRecommendationClick);
        rvHighPriorityRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHighPriorityRecommendations.setAdapter(highPriorityAdapter);
        
        // Medium priority recommendations
        mediumPriorityAdapter = new RecommendationAdapter(new ArrayList<>(), this::onRecommendationClick);
        rvMediumPriorityRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMediumPriorityRecommendations.setAdapter(mediumPriorityAdapter);
        
        // Low priority recommendations
        lowPriorityAdapter = new RecommendationAdapter(new ArrayList<>(), this::onRecommendationClick);
        rvLowPriorityRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLowPriorityRecommendations.setAdapter(lowPriorityAdapter);
        
        // Weekly predictions
        weeklyPredictionAdapter = new WeeklyPredictionAdapter(new ArrayList<>(), this::onPredictionClick);
        rvWeeklyPredictions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvWeeklyPredictions.setAdapter(weeklyPredictionAdapter);
    }
    
    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        ivRefresh.setOnClickListener(v -> refreshRecommendations());
        
        btnPersonalize.setOnClickListener(v -> showPersonalizationDialog());
        
        btnViewStats.setOnClickListener(v -> showStatsDialog());
        
        tvViewAllPredictions.setOnClickListener(v -> showAllPredictions());
        
        llPredictionStatus.setOnClickListener(v -> showEngineStatusDialog());
    }
    
    /**
     * Load recommendations from prediction engine
     */
    private void loadRecommendations() {
        showLoadingState();
        
        // Load recommendations in background thread
        new Thread(() -> {
            try {
                List<Recommendation> recommendations = predictionEngine.getRecommendations(10);
                
                // Update UI on main thread
                getActivity().runOnUiThread(() -> {
                    allRecommendations.clear();
                    allRecommendations.addAll(recommendations);
                    
                    updateRecommendationLists();
                    updateInsights();
                    hideLoadingState();
                    
                    if (recommendations.isEmpty()) {
                        showEmptyState();
                    }
                });
                
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    hideLoadingState();
                    showError("Error loading recommendations: " + e.getMessage());
                });
            }
        }).start();
        
        // Load weekly predictions
        loadWeeklyPredictions();
    }
    
    /**
     * Load weekly predictions
     */
    private void loadWeeklyPredictions() {
        new Thread(() -> {
            try {
                weeklyPredictions = predictionEngine.predictWeeklyPatterns();
                
                getActivity().runOnUiThread(() -> {
                    if (weeklyPredictions != null && !weeklyPredictions.isEmpty()) {
                        weeklyPredictionAdapter.updatePredictions(weeklyPredictions);
                        llWeeklyPredictions.setVisibility(View.VISIBLE);
                    } else {
                        llWeeklyPredictions.setVisibility(View.GONE);
                    }
                });
                
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    llWeeklyPredictions.setVisibility(View.GONE);
                });
            }
        }).start();
    }
    
    /**
     * Update recommendation lists by priority
     */
    private void updateRecommendationLists() {
        List<Recommendation> highPriority = allRecommendations.stream()
            .filter(rec -> rec.getPriorityLevel().equals("HIGH"))
            .collect(Collectors.toList());
        
        List<Recommendation> mediumPriority = allRecommendations.stream()
            .filter(rec -> rec.getPriorityLevel().equals("MEDIUM"))
            .collect(Collectors.toList());
        
        List<Recommendation> lowPriority = allRecommendations.stream()
            .filter(rec -> rec.getPriorityLevel().equals("LOW"))
            .collect(Collectors.toList());
        
        // Update adapters
        highPriorityAdapter.updateRecommendations(highPriority);
        mediumPriorityAdapter.updateRecommendations(mediumPriority);
        lowPriorityAdapter.updateRecommendations(lowPriority);
        
        // Show/hide sections based on content
        llHighPriority.setVisibility(highPriority.isEmpty() ? View.GONE : View.VISIBLE);
        llMediumPriority.setVisibility(mediumPriority.isEmpty() ? View.GONE : View.VISIBLE);
        llLowPriority.setVisibility(lowPriority.isEmpty() ? View.GONE : View.VISIBLE);
    }
    
    /**
     * Update engine status display
     */
    private void updateEngineStatus() {
        new Thread(() -> {
            try {
                Map<String, Double> accuracyStats = predictionEngine.getPredictionAccuracyStats();
                
                getActivity().runOnUiThread(() -> {
                    double overallAccuracy = accuracyStats.getOrDefault("overall_accuracy", 0.0);
                    double totalPredictions = accuracyStats.getOrDefault("total_predictions", 0.0);
                    
                    tvAccuracy.setText(String.format("%.0f%%", overallAccuracy * 100));
                    
                    if (totalPredictions > 50) {
                        tvStatusTitle.setText("Engine Ready");
                        tvStatusDescription.setText("High confidence predictions available");
                        ivStatusIcon.setImageResource(R.drawable.ic_info);
                    } else if (totalPredictions > 10) {
                        tvStatusTitle.setText("Engine Learning");
                        tvStatusDescription.setText("Improving accuracy with more data");
                        ivStatusIcon.setImageResource(R.drawable.ic_info);
                    } else {
                        tvStatusTitle.setText("Engine Initializing");
                        tvStatusDescription.setText("Collecting data for better predictions");
                        ivStatusIcon.setImageResource(R.drawable.ic_info);
                    }
                });
                
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    tvStatusTitle.setText("Engine Error");
                    tvStatusDescription.setText("Unable to load engine status");
                    tvAccuracy.setText("--");
                });
            }
        }).start();
    }
    
    /**
     * Update weather context display
     */
    private void updateWeatherContext() {
        new Thread(() -> {
            try {
                DayRecord today = databaseHelper.getTodayRecord();
                
                getActivity().runOnUiThread(() -> {
                    if (today != null) {
                        tvTemperature.setText(String.format("%.0f°C", today.getTemperature()));
                        tvHumidity.setText(String.format("%.0f%% humidity", today.getHumidity()));
                        
                        String weatherCondition = today.getWeatherCondition();
                        if (weatherCondition != null) {
                            tvWeatherDescription.setText(String.format("%.0f°C, %s", 
                                today.getTemperature(), weatherCondition));
                        } else {
                            tvWeatherDescription.setText(String.format("%.0f°C", today.getTemperature()));
                        }
                        
                        llWeatherContext.setVisibility(View.VISIBLE);
                    } else {
                        llWeatherContext.setVisibility(View.GONE);
                    }
                });
                
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    llWeatherContext.setVisibility(View.GONE);
                });
            }
        }).start();
    }
    
    /**
     * Update insights display
     */
    private void updateInsights() {
        new Thread(() -> {
            try {
                Map<String, Double> accuracyStats = predictionEngine.getPredictionAccuracyStats();
                
                getActivity().runOnUiThread(() -> {
                    double overallAccuracy = accuracyStats.getOrDefault("overall_accuracy", 0.0);
                    double totalPredictions = accuracyStats.getOrDefault("total_predictions", 0.0);
                    
                    tvPredictionAccuracy.setText(String.format("%.0f%%", overallAccuracy * 100));
                    tvTotalPredictions.setText(String.format("%.0f", totalPredictions));
                    
                    // Find best activity
                    if (!allRecommendations.isEmpty()) {
                        Recommendation bestRecommendation = allRecommendations.stream()
                            .max((r1, r2) -> Double.compare(r1.getConfidenceScore(), r2.getConfidenceScore()))
                            .orElse(null);
                        
                        if (bestRecommendation != null) {
                            tvBestActivity.setText(bestRecommendation.getActivityType().getDisplayName());
                            tvConfidence.setText(String.format("%.0f%%", bestRecommendation.getConfidenceScore() * 100));
                        }
                    }
                });
                
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    tvPredictionAccuracy.setText("--");
                    tvTotalPredictions.setText("--");
                    tvBestActivity.setText("--");
                    tvConfidence.setText("--");
                });
            }
        }).start();
    }
    
    /**
     * Handle recommendation click
     */
    private void onRecommendationClick(Recommendation recommendation, String action) {
        switch (action) {
            case "accept":
                acceptRecommendation(recommendation);
                break;
            case "dismiss":
                dismissRecommendation(recommendation);
                break;
            case "view":
                showRecommendationDetails(recommendation);
                break;
        }
    }
    
    /**
     * Handle prediction click
     */
    private void onPredictionClick(Date date, List<PredictionResult> predictions) {
        showPredictionDetails(date, predictions);
    }
    
    /**
     * Accept recommendation
     */
    private void acceptRecommendation(Recommendation recommendation) {
        recommendation.setActedUpon(true);
        recommendation.setActionDate(new Date());
        
        Toast.makeText(getContext(), "Great! Enjoy your " + 
            recommendation.getActivityType().getDisplayName().toLowerCase(), Toast.LENGTH_SHORT).show();
        
        // Update prediction engine with positive feedback
        // In a real implementation, you would track the actual outcome
        new Thread(() -> {
            try {
                // Simulate positive outcome for demonstration
                predictionEngine.updatePredictionAccuracy(recommendation.getId(), recommendation.getActivityType());
            } catch (Exception e) {
                // Log error
            }
        }).start();
        
        // Remove from current recommendations
        allRecommendations.remove(recommendation);
        updateRecommendationLists();
    }
    
    /**
     * Dismiss recommendation
     */
    private void dismissRecommendation(Recommendation recommendation) {
        allRecommendations.remove(recommendation);
        updateRecommendationLists();
        
        Toast.makeText(getContext(), "Recommendation dismissed", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show recommendation details
     */
    private void showRecommendationDetails(Recommendation recommendation) {
        // Create and show detailed dialog
        RecommendationDetailDialog dialog = new RecommendationDetailDialog(getContext(), recommendation);
        dialog.show();
    }
    
    /**
     * Show prediction details
     */
    private void showPredictionDetails(Date date, List<PredictionResult> predictions) {
        // Create and show prediction details dialog
        PredictionDetailDialog dialog = new PredictionDetailDialog(getContext(), date, predictions);
        dialog.show();
    }
    
    /**
     * Refresh recommendations
     */
    private void refreshRecommendations() {
        predictionEngine.clearCache();
        loadRecommendations();
        updateEngineStatus();
        updateWeatherContext();
        
        Toast.makeText(getContext(), "Recommendations refreshed", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show personalization dialog
     */
    private void showPersonalizationDialog() {
        PersonalizationDialog dialog = new PersonalizationDialog(getContext(), this::onPersonalizationChanged);
        dialog.show();
    }
    
    /**
     * Show stats dialog
     */
    private void showStatsDialog() {
        StatsDialog dialog = new StatsDialog(getContext(), predictionEngine);
        dialog.show();
    }
    
    /**
     * Show all predictions
     */
    private void showAllPredictions() {
        AllPredictionsDialog dialog = new AllPredictionsDialog(getContext(), weeklyPredictions);
        dialog.show();
    }
    
    /**
     * Show engine status dialog
     */
    private void showEngineStatusDialog() {
        EngineStatusDialog dialog = new EngineStatusDialog(getContext(), predictionEngine);
        dialog.show();
    }
    
    /**
     * Handle personalization changes
     */
    private void onPersonalizationChanged(List<ActivityType> preferredActivities) {
        // Reload recommendations with new preferences
        new Thread(() -> {
            try {
                List<Recommendation> personalizedRecommendations = predictionEngine.getPersonalizedRecommendations(
                    preferredActivities, "current_location", 10);
                
                getActivity().runOnUiThread(() -> {
                    allRecommendations.clear();
                    allRecommendations.addAll(personalizedRecommendations);
                    updateRecommendationLists();
                    
                    Toast.makeText(getContext(), "Recommendations updated with your preferences", 
                        Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    showError("Error updating recommendations: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Show loading state
     */
    private void showLoadingState() {
        llLoadingState.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        
        llHighPriority.setVisibility(View.GONE);
        llMediumPriority.setVisibility(View.GONE);
        llLowPriority.setVisibility(View.GONE);
    }
    
    /**
     * Hide loading state
     */
    private void hideLoadingState() {
        llLoadingState.setVisibility(View.GONE);
    }
    
    /**
     * Show empty state
     */
    private void showEmptyState() {
        llEmptyState.setVisibility(View.VISIBLE);
        
        llHighPriority.setVisibility(View.GONE);
        llMediumPriority.setVisibility(View.GONE);
        llLowPriority.setVisibility(View.GONE);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        updateEngineStatus();
        updateWeatherContext();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (predictionEngine != null) {
            predictionEngine.clearCache();
        }
    }
}