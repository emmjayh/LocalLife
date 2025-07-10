package com.locallife.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.locallife.R;
import com.locallife.service.ActivityPredictionEngine;

import java.util.Map;

/**
 * Dialog for displaying engine status and insights
 */
public class EngineStatusDialog extends Dialog {
    
    private ActivityPredictionEngine predictionEngine;
    
    public EngineStatusDialog(@NonNull Context context, ActivityPredictionEngine predictionEngine) {
        super(context);
        this.predictionEngine = predictionEngine;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_engine_status);
        
        setupViews();
        setupClickListeners();
        loadEngineStatus();
    }
    
    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Engine Status");
    }
    
    private void setupClickListeners() {
        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());
    }
    
    private void loadEngineStatus() {
        TextView tvStatusContent = findViewById(R.id.tvStatusContent);
        
        new Thread(() -> {
            try {
                Map<String, Double> accuracyStats = predictionEngine.getPredictionAccuracyStats();
                Map<String, Object> correlationInsights = predictionEngine.getActivityCorrelationInsights();
                
                post(() -> {
                    StringBuilder content = new StringBuilder();
                    
                    content.append("=== Engine Status ===\n");
                    double totalPredictions = accuracyStats.getOrDefault("total_predictions", 0.0);
                    
                    if (totalPredictions > 50) {
                        content.append("Status: READY ✓\n");
                        content.append("The engine has sufficient data for high-confidence predictions.\n\n");
                    } else if (totalPredictions > 10) {
                        content.append("Status: LEARNING ⚡\n");
                        content.append("The engine is learning and improving with more data.\n\n");
                    } else {
                        content.append("Status: INITIALIZING ⏳\n");
                        content.append("The engine is collecting data for better predictions.\n\n");
                    }
                    
                    content.append("=== Performance Metrics ===\n");
                    content.append(String.format("Overall Accuracy: %.1f%%\n", accuracyStats.getOrDefault("overall_accuracy", 0.0) * 100));
                    content.append(String.format("Total Predictions: %.0f\n", totalPredictions));
                    content.append(String.format("Recent Accuracy: %.1f%%\n\n", accuracyStats.getOrDefault("recent_accuracy", 0.0) * 100));
                    
                    content.append("=== Correlation Insights ===\n");
                    if (correlationInsights.containsKey("cached_correlations")) {
                        content.append(String.format("Weather Correlations: %s\n", correlationInsights.get("cached_correlations")));
                    }
                    
                    if (correlationInsights.containsKey("top_correlated_activities")) {
                        content.append("Top Correlated Activities:\n");
                        @SuppressWarnings("unchecked")
                        java.util.List<String> topActivities = (java.util.List<String>) correlationInsights.get("top_correlated_activities");
                        for (String activity : topActivities) {
                            content.append("• ").append(activity).append("\n");
                        }
                    }
                    
                    tvStatusContent.setText(content.toString());
                });
                
            } catch (Exception e) {
                post(() -> {
                    tvStatusContent.setText("Error loading engine status: " + e.getMessage());
                });
            }
        }).start();
    }
}