package com.locallife.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.locallife.R;
import com.locallife.model.Recommendation;

/**
 * Dialog for displaying detailed information about a recommendation
 */
public class RecommendationDetailDialog extends Dialog {
    
    private Recommendation recommendation;
    
    public RecommendationDetailDialog(@NonNull Context context, Recommendation recommendation) {
        super(context);
        this.recommendation = recommendation;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_recommendation_detail);
        
        setupViews();
        setupClickListeners();
    }
    
    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvReasoning = findViewById(R.id.tvReasoning);
        TextView tvConfidence = findViewById(R.id.tvConfidence);
        TextView tvWeatherSuitability = findViewById(R.id.tvWeatherSuitability);
        TextView tvRequirements = findViewById(R.id.tvRequirements);
        TextView tvBenefits = findViewById(R.id.tvBenefits);
        
        // Set content
        tvTitle.setText(recommendation.getTitle());
        tvDescription.setText(recommendation.getDescription());
        tvReasoning.setText(recommendation.getReasoning());
        tvConfidence.setText(String.format("%.0f%%", recommendation.getConfidenceScore() * 100));
        
        // Weather suitability
        if (recommendation.getWeatherSuitability() != null) {
            tvWeatherSuitability.setText(String.format("%.0f%%", 
                recommendation.getWeatherSuitability().getOverallScore() * 100));
        } else {
            tvWeatherSuitability.setText("N/A");
        }
        
        // Requirements
        if (recommendation.getRequirements() != null && !recommendation.getRequirements().isEmpty()) {
            StringBuilder requirements = new StringBuilder();
            for (String req : recommendation.getRequirements()) {
                requirements.append("• ").append(req).append("\n");
            }
            tvRequirements.setText(requirements.toString().trim());
        } else {
            tvRequirements.setText("No special requirements");
        }
        
        // Benefits
        if (recommendation.getBenefits() != null && !recommendation.getBenefits().isEmpty()) {
            StringBuilder benefits = new StringBuilder();
            for (String benefit : recommendation.getBenefits()) {
                benefits.append("• ").append(benefit).append("\n");
            }
            tvBenefits.setText(benefits.toString().trim());
        } else {
            tvBenefits.setText("General wellbeing benefits");
        }
    }
    
    private void setupClickListeners() {
        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());
    }
}