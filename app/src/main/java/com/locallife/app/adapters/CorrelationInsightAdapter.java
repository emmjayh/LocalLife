package com.locallife.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.app.R;
import com.locallife.service.CorrelationAnalysisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying correlation insights
 */
public class CorrelationInsightAdapter extends RecyclerView.Adapter<CorrelationInsightAdapter.ViewHolder> {
    
    private List<CorrelationAnalysisService.CorrelationInsight> insights = new ArrayList<>();
    
    public void setInsights(List<CorrelationAnalysisService.CorrelationInsight> insights) {
        this.insights = new ArrayList<>(insights);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_correlation_insight, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CorrelationAnalysisService.CorrelationInsight insight = insights.get(position);
        
        holder.tvTitle.setText(insight.title);
        holder.tvDescription.setText(insight.description);
        holder.tvCorrelation.setText(String.format(Locale.getDefault(), 
            "r = %.3f", insight.correlation));
        holder.tvStrength.setText(insight.strength.name());
        
        // Set strength color
        int strengthColor = getStrengthColor(insight.strength);
        holder.tvStrength.setTextColor(strengthColor);
        
        // Set category icon/color
        int categoryColor = getCategoryColor(insight.category);
        holder.cardView.setCardBackgroundColor(categoryColor);
    }
    
    @Override
    public int getItemCount() {
        return insights.size();
    }
    
    private int getStrengthColor(CorrelationAnalysisService.CorrelationInsight.Strength strength) {
        switch (strength) {
            case WEAK:
                return Color.parseColor("#FFC107"); // Yellow
            case MODERATE:
                return Color.parseColor("#FF9800"); // Orange
            case STRONG:
                return Color.parseColor("#FF5722"); // Deep Orange
            case VERY_STRONG:
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.GRAY;
        }
    }
    
    private int getCategoryColor(String category) {
        switch (category) {
            case "temperature":
                return Color.parseColor("#2A2A2A"); // Dark gray base
            case "uv":
                return Color.parseColor("#2A2A2A");
            case "air_quality":
                return Color.parseColor("#2A2A2A");
            case "screen_weather":
                return Color.parseColor("#2A2A2A");
            case "media_weather":
                return Color.parseColor("#2A2A2A");
            default:
                return Color.parseColor("#2A2A2A");
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvCorrelation;
        TextView tvStrength;
        
        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCorrelation = itemView.findViewById(R.id.tv_correlation);
            tvStrength = itemView.findViewById(R.id.tv_strength);
        }
    }
}