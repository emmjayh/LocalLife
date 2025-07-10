package com.locallife.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.R;
import com.locallife.model.ActivityType;
import com.locallife.model.Recommendation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying activity recommendations in RecyclerView
 */
public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {
    
    private List<Recommendation> recommendations;
    private RecommendationClickListener clickListener;
    private Context context;
    
    public interface RecommendationClickListener {
        void onRecommendationClick(Recommendation recommendation, String action);
    }
    
    public RecommendationAdapter(List<Recommendation> recommendations, RecommendationClickListener clickListener) {
        this.recommendations = recommendations;
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity_recommendation, parent, false);
        return new RecommendationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);
        holder.bind(recommendation);
    }
    
    @Override
    public int getItemCount() {
        return recommendations.size();
    }
    
    /**
     * Update recommendations list
     */
    public void updateRecommendations(List<Recommendation> newRecommendations) {
        this.recommendations = newRecommendations;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for recommendation items
     */
    public class RecommendationViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView ivActivityIcon;
        private TextView tvActivityTitle;
        private TextView tvActivityDescription;
        private TextView tvConfidenceScore;
        private TextView tvPriorityLevel;
        private TextView tvTiming;
        private TextView tvLocation;
        private View viewWeatherBar;
        private TextView tvWeatherScore;
        private TextView tvReasoning;
        private TextView tvRequirements;
        private TextView tvBenefits;
        private Button btnDismiss;
        private Button btnAccept;
        
        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivActivityIcon = itemView.findViewById(R.id.ivActivityIcon);
            tvActivityTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvActivityDescription = itemView.findViewById(R.id.tvActivityDescription);
            tvConfidenceScore = itemView.findViewById(R.id.tvConfidenceScore);
            tvPriorityLevel = itemView.findViewById(R.id.tvPriorityLevel);
            tvTiming = itemView.findViewById(R.id.tvTiming);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            viewWeatherBar = itemView.findViewById(R.id.viewWeatherBar);
            tvWeatherScore = itemView.findViewById(R.id.tvWeatherScore);
            tvReasoning = itemView.findViewById(R.id.tvReasoning);
            tvRequirements = itemView.findViewById(R.id.tvRequirements);
            tvBenefits = itemView.findViewById(R.id.tvBenefits);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            
            // Set click listeners
            btnAccept.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onRecommendationClick(recommendations.get(position), "accept");
                }
            });
            
            btnDismiss.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onRecommendationClick(recommendations.get(position), "dismiss");
                }
            });
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onRecommendationClick(recommendations.get(position), "view");
                }
            });
        }
        
        /**
         * Bind recommendation data to views
         */
        public void bind(Recommendation recommendation) {
            // Activity info
            tvActivityTitle.setText(recommendation.getTitle());
            tvActivityDescription.setText(recommendation.getDescription());
            
            // Confidence score
            tvConfidenceScore.setText(String.format("%.0f%%", recommendation.getConfidenceScore() * 100));
            
            // Priority level
            String priority = recommendation.getPriorityLevel();
            tvPriorityLevel.setText(priority);
            tvPriorityLevel.setTextColor(getPriorityColor(priority));
            
            // Activity icon
            ivActivityIcon.setImageResource(getActivityIcon(recommendation.getActivityType()));
            
            // Timing
            String timing = formatTiming(recommendation);
            tvTiming.setText(timing);
            
            // Location
            String location = recommendation.getLocation();
            tvLocation.setText(location != null ? location : "Flexible location");
            
            // Weather suitability
            if (recommendation.getWeatherSuitability() != null) {
                double weatherScore = recommendation.getWeatherSuitability().getOverallScore();
                tvWeatherScore.setText(String.format("%.0f%%", weatherScore * 100));
                
                // Update weather bar color and width
                updateWeatherBar(weatherScore);
            } else {
                tvWeatherScore.setText("N/A");
                viewWeatherBar.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            }
            
            // Reasoning
            String reasoning = recommendation.getReasoning();
            tvReasoning.setText(reasoning != null ? reasoning : "Recommended based on current conditions");
            
            // Requirements
            if (recommendation.getRequirements() != null && !recommendation.getRequirements().isEmpty()) {
                StringBuilder requirements = new StringBuilder();
                for (String requirement : recommendation.getRequirements()) {
                    requirements.append("• ").append(requirement).append("\n");
                }
                tvRequirements.setText(requirements.toString().trim());
            } else {
                tvRequirements.setText("• No special requirements");
            }
            
            // Benefits
            if (recommendation.getBenefits() != null && !recommendation.getBenefits().isEmpty()) {
                StringBuilder benefits = new StringBuilder();
                for (String benefit : recommendation.getBenefits()) {
                    benefits.append("• ").append(benefit).append("\n");
                }
                tvBenefits.setText(benefits.toString().trim());
            } else {
                tvBenefits.setText("• General wellbeing benefits");
            }
            
            // Update button states
            updateButtonStates(recommendation);
        }
        
        /**
         * Get priority color
         */
        private int getPriorityColor(String priority) {
            switch (priority) {
                case "HIGH":
                    return context.getResources().getColor(android.R.color.holo_red_dark);
                case "MEDIUM":
                    return context.getResources().getColor(android.R.color.holo_orange_dark);
                case "LOW":
                    return context.getResources().getColor(android.R.color.darker_gray);
                default:
                    return context.getResources().getColor(android.R.color.black);
            }
        }
        
        /**
         * Get activity icon resource
         */
        private int getActivityIcon(ActivityType activityType) {
            switch (activityType) {
                case OUTDOOR_EXERCISE:
                case INDOOR_EXERCISE:
                    return R.drawable.ic_health;
                case SOCIAL_ACTIVITY:
                    return R.drawable.ic_community;
                case WORK_PRODUCTIVITY:
                    return R.drawable.ic_work;
                case RECREATIONAL:
                    return R.drawable.ic_entertainment;
                case RELAXATION:
                    return R.drawable.ic_health;
                case TRAVEL:
                    return R.drawable.ic_transport;
                case PHOTOGRAPHY:
                    return R.drawable.ic_camera;
                case INDOOR_ACTIVITIES:
                    return R.drawable.ic_home;
                case OUTDOOR_LEISURE:
                    return R.drawable.ic_location;
                default:
                    return R.drawable.ic_info;
            }
        }
        
        /**
         * Format timing information
         */
        private String formatTiming(Recommendation recommendation) {
            StringBuilder timing = new StringBuilder();
            
            if (recommendation.isTimeSensitive()) {
                timing.append("Now");
            } else {
                timing.append("Anytime");
            }
            
            if (recommendation.getDurationMinutes() > 0) {
                timing.append(" • ").append(recommendation.getDurationMinutes()).append(" min");
            }
            
            return timing.toString();
        }
        
        /**
         * Update weather bar appearance
         */
        private void updateWeatherBar(double weatherScore) {
            // Set color based on score
            int color;
            if (weatherScore >= 0.8) {
                color = context.getResources().getColor(android.R.color.holo_green_dark);
            } else if (weatherScore >= 0.6) {
                color = context.getResources().getColor(android.R.color.holo_orange_dark);
            } else if (weatherScore >= 0.4) {
                color = context.getResources().getColor(android.R.color.holo_orange_light);
            } else {
                color = context.getResources().getColor(android.R.color.holo_red_light);
            }
            
            viewWeatherBar.setBackgroundColor(color);
            
            // Set width based on score (animate if needed)
            ViewGroup.LayoutParams params = viewWeatherBar.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                // Adjust width based on score (this is a simplified approach)
                viewWeatherBar.setScaleX((float) Math.max(0.1, weatherScore));
            }
        }
        
        /**
         * Update button states
         */
        private void updateButtonStates(Recommendation recommendation) {
            if (recommendation.isActedUpon()) {
                btnAccept.setEnabled(false);
                btnAccept.setText("Completed");
                btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            } else {
                btnAccept.setEnabled(true);
                btnAccept.setText("I'll Do This");
                btnAccept.setBackgroundResource(R.drawable.button_primary);
            }
            
            // Check if recommendation is stale
            if ("STALE".equals(recommendation.getFreshness())) {
                btnAccept.setEnabled(false);
                btnAccept.setText("Outdated");
                btnAccept.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        }
    }
}