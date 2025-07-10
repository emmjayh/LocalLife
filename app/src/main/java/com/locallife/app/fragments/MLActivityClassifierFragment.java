package com.locallife.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.R;
import com.locallife.service.MLActivityClassifierService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying ML activity classification results
 */
public class MLActivityClassifierFragment extends Fragment {
    
    private MLActivityClassifierService classifierService;
    private ProgressBar progressBar;
    private TextView tvClassificationSummary;
    private TextView tvPredictionSummary;
    private TextView tvProfileSummary;
    private Button btnClassify;
    private Button btnPredict;
    private Button btnAnalyzeWeather;
    private Button btnCreateProfile;
    private RecyclerView rvClassifications;
    private RecyclerView rvPredictions;
    private LinearLayout layoutResults;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ml_activity_classifier, container, false);
        
        initializeViews(view);
        initializeService();
        setupClickListeners();
        
        return view;
    }
    
    private void initializeViews(View view) {
        progressBar = view.findViewById(R.id.progress_bar);
        tvClassificationSummary = view.findViewById(R.id.tv_classification_summary);
        tvPredictionSummary = view.findViewById(R.id.tv_prediction_summary);
        tvProfileSummary = view.findViewById(R.id.tv_profile_summary);
        btnClassify = view.findViewById(R.id.btn_classify);
        btnPredict = view.findViewById(R.id.btn_predict);
        btnAnalyzeWeather = view.findViewById(R.id.btn_analyze_weather);
        btnCreateProfile = view.findViewById(R.id.btn_create_profile);
        rvClassifications = view.findViewById(R.id.rv_classifications);
        rvPredictions = view.findViewById(R.id.rv_predictions);
        layoutResults = view.findViewById(R.id.layout_results);
        
        rvClassifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPredictions.setLayoutManager(new LinearLayoutManager(getContext()));
        
        layoutResults.setVisibility(View.GONE);
    }
    
    private void initializeService() {
        classifierService = new MLActivityClassifierService(getContext());
    }
    
    private void setupClickListeners() {
        btnClassify.setOnClickListener(v -> classifyActivity());
        btnPredict.setOnClickListener(v -> predictOptimalTimes());
        btnAnalyzeWeather.setOnClickListener(v -> analyzeWeatherImpact());
        btnCreateProfile.setOnClickListener(v -> createActivityProfile());
    }
    
    private void classifyActivity() {
        showProgress(true);
        
        classifierService.classifyActivityLevel(new MLActivityClassifierService.ClassificationCallback() {
            @Override
            public void onActivityClassified(List<MLActivityClassifierService.ActivityClassification> classifications, 
                                           MLActivityClassifierService.ActivityInsights insights) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayClassificationResults(classifications, insights);
                });
            }
            
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayError("Classification Error: " + error);
                });
            }
        });
    }
    
    private void predictOptimalTimes() {
        showProgress(true);
        
        classifierService.predictOptimalActivityTimes(new MLActivityClassifierService.PredictionCallback() {
            @Override
            public void onPredictionsGenerated(List<MLActivityClassifierService.ActivityPrediction> predictions) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayPredictionResults(predictions);
                });
            }
            
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayError("Prediction Error: " + error);
                });
            }
        });
    }
    
    private void analyzeWeatherImpact() {
        showProgress(true);
        
        classifierService.analyzeWeatherImpact(new MLActivityClassifierService.WeatherImpactCallback() {
            @Override
            public void onWeatherImpactAnalyzed(MLActivityClassifierService.WeatherImpactAnalysis analysis) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayWeatherImpactResults(analysis);
                });
            }
            
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayError("Weather Analysis Error: " + error);
                });
            }
        });
    }
    
    private void createActivityProfile() {
        showProgress(true);
        
        classifierService.createActivityProfile(new MLActivityClassifierService.ProfileCallback() {
            @Override
            public void onActivityProfileCreated(MLActivityClassifierService.ActivityProfile profile) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayProfileResults(profile);
                });
            }
            
            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    showProgress(false);
                    displayError("Profile Creation Error: " + error);
                });
            }
        });
    }
    
    private void displayClassificationResults(List<MLActivityClassifierService.ActivityClassification> classifications, 
                                            MLActivityClassifierService.ActivityInsights insights) {
        layoutResults.setVisibility(View.VISIBLE);
        
        StringBuilder summary = new StringBuilder();
        summary.append("🎯 ACTIVITY CLASSIFICATION RESULTS\\n\\n");
        
        summary.append("📊 OVERALL INSIGHTS:\\n");
        summary.append("• Average Activity Score: ").append(String.format("%.1f/100", insights.averageActivityScore)).append("\\n");
        summary.append("• Dominant Activity Level: ").append(insights.dominantActivityLevel).append("\\n");
        summary.append("• Activity Trend: ").append(insights.trend).append("\\n\\n");
        
        summary.append("📈 ACTIVITY LEVEL DISTRIBUTION:\\n");
        for (Map.Entry<MLActivityClassifierService.ActivityLevel, Integer> entry : insights.activityLevelDistribution.entrySet()) {
            summary.append("• ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" days\\n");
        }
        
        summary.append("\\n🗓️ RECENT CLASSIFICATIONS:\\n");
        for (int i = 0; i < Math.min(5, classifications.size()); i++) {
            MLActivityClassifierService.ActivityClassification classification = classifications.get(i);
            summary.append("• ").append(classification.date).append(": ")
                   .append(classification.activityLevel).append(" (")
                   .append(classification.activityScore).append("/100)\\n");
        }
        
        tvClassificationSummary.setText(summary.toString());
        tvClassificationSummary.setVisibility(View.VISIBLE);
        
        // Setup RecyclerView adapter for detailed classifications
        ClassificationAdapter adapter = new ClassificationAdapter(classifications);
        rvClassifications.setAdapter(adapter);
        rvClassifications.setVisibility(View.VISIBLE);
    }
    
    private void displayPredictionResults(List<MLActivityClassifierService.ActivityPrediction> predictions) {
        layoutResults.setVisibility(View.VISIBLE);
        
        StringBuilder summary = new StringBuilder();
        summary.append("🔮 OPTIMAL ACTIVITY TIME PREDICTIONS\\n\\n");
        
        summary.append("📅 WEEKLY PREDICTIONS:\\n");
        for (MLActivityClassifierService.ActivityPrediction prediction : predictions) {
            summary.append("• ").append(prediction.dayName).append(":\\n");
            summary.append("  - Predicted Steps: ").append(prediction.predictedSteps).append("\\n");
            summary.append("  - Screen Time: ").append(prediction.predictedScreenTime).append(" min\\n");
            summary.append("  - Places: ").append(prediction.predictedPlaces).append("\\n");
            summary.append("  - Optimal Temp: ").append(String.format("%.1f°C", prediction.optimalTemperature)).append("\\n");
            summary.append("  - Confidence: ").append(String.format("%.0f%%", prediction.confidence * 100)).append("\\n\\n");
        }
        
        tvPredictionSummary.setText(summary.toString());
        tvPredictionSummary.setVisibility(View.VISIBLE);
        
        // Setup RecyclerView adapter for detailed predictions
        PredictionAdapter adapter = new PredictionAdapter(predictions);
        rvPredictions.setAdapter(adapter);
        rvPredictions.setVisibility(View.VISIBLE);
    }
    
    private void displayWeatherImpactResults(MLActivityClassifierService.WeatherImpactAnalysis analysis) {
        layoutResults.setVisibility(View.VISIBLE);
        
        StringBuilder summary = new StringBuilder();
        summary.append("🌤️ WEATHER IMPACT ANALYSIS\\n\\n");
        
        summary.append("🌡️ TEMPERATURE IMPACT:\\n");
        summary.append(analysis.temperatureImpact).append("\\n\\n");
        
        summary.append("☁️ WEATHER CONDITIONS:\\n");
        summary.append(analysis.weatherConditionImpact).append("\\n\\n");
        
        summary.append("☀️ UV INDEX IMPACT:\\n");
        summary.append(analysis.uvIndexImpact).append("\\n\\n");
        
        summary.append("🌬️ AIR QUALITY IMPACT:\\n");
        summary.append(analysis.airQualityImpact).append("\\n\\n");
        
        summary.append("💡 RECOMMENDATIONS:\\n");
        for (String recommendation : analysis.recommendations) {
            summary.append("• ").append(recommendation).append("\\n");
        }
        
        tvClassificationSummary.setText(summary.toString());
        tvClassificationSummary.setVisibility(View.VISIBLE);
    }
    
    private void displayProfileResults(MLActivityClassifierService.ActivityProfile profile) {
        layoutResults.setVisibility(View.VISIBLE);
        
        StringBuilder summary = new StringBuilder();
        summary.append("👤 ACTIVITY PROFILE\\n\\n");
        
        summary.append("📊 AVERAGE METRICS:\\n");
        summary.append("• Daily Steps: ").append(profile.averageSteps).append("\\n");
        summary.append("• Screen Time: ").append(profile.averageScreenTime).append(" min\\n");
        summary.append("• Places Visited: ").append(profile.averagePlacesVisited).append("\\n");
        summary.append("• Fitness Level: ").append(profile.fitnessLevelEstimation).append("\\n\\n");
        
        summary.append("⏰ PEAK ACTIVITY HOURS:\\n");
        for (Integer hour : profile.peakActivityHours) {
            summary.append("• ").append(String.format("%02d:00", hour)).append("\\n");
        }
        
        summary.append("\\n🎯 CONSISTENCY SCORE:\\n");
        summary.append(String.format("%.1f/1.0 (%.0f%%)", profile.consistencyScore, profile.consistencyScore * 100)).append("\\n\\n");
        
        summary.append("🌤️ PREFERRED WEATHER:\\n");
        for (String condition : profile.preferredWeatherConditions) {
            summary.append("• ").append(condition).append("\\n");
        }
        
        summary.append("\\n🏃 ACTIVITY TYPE PREFERENCES:\\n");
        for (Map.Entry<String, Float> entry : profile.activityTypePreferences.entrySet()) {
            summary.append("• ").append(entry.getKey()).append(": ")
                   .append(String.format("%.0f%%", entry.getValue() * 100)).append("\\n");
        }
        
        tvProfileSummary.setText(summary.toString());
        tvProfileSummary.setVisibility(View.VISIBLE);
    }
    
    private void displayError(String error) {
        layoutResults.setVisibility(View.VISIBLE);
        tvClassificationSummary.setText("❌ " + error);
        tvClassificationSummary.setVisibility(View.VISIBLE);
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnClassify.setEnabled(!show);
        btnPredict.setEnabled(!show);
        btnAnalyzeWeather.setEnabled(!show);
        btnCreateProfile.setEnabled(!show);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (classifierService != null) {
            classifierService.shutdown();
        }
    }
    
    // Adapter classes for RecyclerViews
    private class ClassificationAdapter extends RecyclerView.Adapter<ClassificationAdapter.ViewHolder> {
        private List<MLActivityClassifierService.ActivityClassification> classifications;
        
        public ClassificationAdapter(List<MLActivityClassifierService.ActivityClassification> classifications) {
            this.classifications = classifications;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_classification, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MLActivityClassifierService.ActivityClassification classification = classifications.get(position);
            
            holder.tvDate.setText(classification.date);
            holder.tvActivityLevel.setText(classification.activityLevel.toString());
            holder.tvActivityScore.setText(String.format("%d/100", classification.activityScore));
            holder.tvConfidence.setText(String.format("%.0f%%", classification.confidence * 100));
            
            // Set color based on activity level
            int color = getActivityLevelColor(classification.activityLevel);
            holder.tvActivityLevel.setTextColor(color);
            
            // Display primary factors
            if (classification.primaryFactors != null && !classification.primaryFactors.isEmpty()) {
                StringBuilder factors = new StringBuilder();
                for (String factor : classification.primaryFactors) {
                    factors.append("• ").append(factor).append("\\n");
                }
                holder.tvPrimaryFactors.setText(factors.toString());
                holder.tvPrimaryFactors.setVisibility(View.VISIBLE);
            } else {
                holder.tvPrimaryFactors.setVisibility(View.GONE);
            }
        }
        
        @Override
        public int getItemCount() {
            return classifications.size();
        }
        
        private int getActivityLevelColor(MLActivityClassifierService.ActivityLevel level) {
            switch (level) {
                case VERY_HIGH: return Color.parseColor("#2E7D32"); // Dark green
                case HIGH: return Color.parseColor("#388E3C"); // Green
                case MODERATE: return Color.parseColor("#FBC02D"); // Yellow
                case LOW: return Color.parseColor("#F57C00"); // Orange
                case VERY_LOW: return Color.parseColor("#D32F2F"); // Red
                default: return Color.GRAY;
            }
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvActivityLevel, tvActivityScore, tvConfidence, tvPrimaryFactors;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvActivityLevel = itemView.findViewById(R.id.tv_activity_level);
                tvActivityScore = itemView.findViewById(R.id.tv_activity_score);
                tvConfidence = itemView.findViewById(R.id.tv_confidence);
                tvPrimaryFactors = itemView.findViewById(R.id.tv_primary_factors);
            }
        }
    }
    
    private class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {
        private List<MLActivityClassifierService.ActivityPrediction> predictions;
        
        public PredictionAdapter(List<MLActivityClassifierService.ActivityPrediction> predictions) {
            this.predictions = predictions;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_prediction, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MLActivityClassifierService.ActivityPrediction prediction = predictions.get(position);
            
            holder.tvDayName.setText(prediction.dayName);
            holder.tvPredictedSteps.setText(String.format("%d steps", prediction.predictedSteps));
            holder.tvScreenTime.setText(String.format("%d min", prediction.predictedScreenTime));
            holder.tvPlaces.setText(String.format("%d places", prediction.predictedPlaces));
            holder.tvOptimalTemp.setText(String.format("%.1f°C", prediction.optimalTemperature));
            holder.tvConfidence.setText(String.format("%.0f%%", prediction.confidence * 100));
            
            // Display recommendations
            if (prediction.recommendations != null && !prediction.recommendations.isEmpty()) {
                StringBuilder recommendations = new StringBuilder();
                for (String rec : prediction.recommendations) {
                    recommendations.append("• ").append(rec).append("\\n");
                }
                holder.tvRecommendations.setText(recommendations.toString());
                holder.tvRecommendations.setVisibility(View.VISIBLE);
            } else {
                holder.tvRecommendations.setVisibility(View.GONE);
            }
        }
        
        @Override
        public int getItemCount() {
            return predictions.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDayName, tvPredictedSteps, tvScreenTime, tvPlaces, tvOptimalTemp, tvConfidence, tvRecommendations;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvDayName = itemView.findViewById(R.id.tv_day_name);
                tvPredictedSteps = itemView.findViewById(R.id.tv_predicted_steps);
                tvScreenTime = itemView.findViewById(R.id.tv_screen_time);
                tvPlaces = itemView.findViewById(R.id.tv_places);
                tvOptimalTemp = itemView.findViewById(R.id.tv_optimal_temp);
                tvConfidence = itemView.findViewById(R.id.tv_confidence);
                tvRecommendations = itemView.findViewById(R.id.tv_recommendations);
            }
        }
    }
}