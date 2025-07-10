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
 * Dialog for displaying prediction engine statistics
 */
public class StatsDialog extends Dialog {
    
    private ActivityPredictionEngine predictionEngine;
    
    public StatsDialog(@NonNull Context context, ActivityPredictionEngine predictionEngine) {
        super(context);
        this.predictionEngine = predictionEngine;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_stats);
        
        setupViews();
        setupClickListeners();
        loadStats();
    }
    
    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Prediction Engine Statistics");
    }
    
    private void setupClickListeners() {
        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());
    }
    
    private void loadStats() {
        TextView tvStatsContent = findViewById(R.id.tvStatsContent);
        
        new Thread(() -> {
            try {
                Map<String, Double> stats = predictionEngine.getPredictionAccuracyStats();
                Map<String, Integer> cacheStats = predictionEngine.getCacheStats();
                
                post(() -> {
                    StringBuilder content = new StringBuilder();
                    content.append("=== Accuracy Statistics ===\n");
                    content.append(String.format("Overall Accuracy: %.1f%%\n", stats.getOrDefault("overall_accuracy", 0.0) * 100));
                    content.append(String.format("Total Predictions: %.0f\n", stats.getOrDefault("total_predictions", 0.0)));
                    content.append(String.format("Recent Accuracy: %.1f%%\n\n", stats.getOrDefault("recent_accuracy", 0.0) * 100));
                    
                    content.append("=== Cache Statistics ===\n");
                    content.append(String.format("Prediction Cache: %d items\n", cacheStats.getOrDefault("prediction_cache_size", 0)));
                    content.append(String.format("Recommendation Cache: %d items\n\n", cacheStats.getOrDefault("recommendation_cache_size", 0)));
                    
                    content.append("=== Method Performance ===\n");
                    for (Map.Entry<String, Double> entry : stats.entrySet()) {
                        if (entry.getKey().contains("_accuracy") && !entry.getKey().contains("overall") && !entry.getKey().contains("recent")) {
                            content.append(String.format("%s: %.1f%%\n", 
                                entry.getKey().replace("_accuracy", "").replace("_", " "), 
                                entry.getValue() * 100));
                        }
                    }
                    
                    tvStatsContent.setText(content.toString());
                });
                
            } catch (Exception e) {
                post(() -> {
                    tvStatsContent.setText("Error loading statistics: " + e.getMessage());
                });
            }
        }).start();
    }
}