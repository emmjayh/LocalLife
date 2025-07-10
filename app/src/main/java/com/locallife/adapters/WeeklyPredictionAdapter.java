package com.locallife.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.R;
import com.locallife.model.PredictionResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for displaying weekly predictions in horizontal RecyclerView
 */
public class WeeklyPredictionAdapter extends RecyclerView.Adapter<WeeklyPredictionAdapter.PredictionViewHolder> {
    
    private List<DayPrediction> dayPredictions;
    private PredictionClickListener clickListener;
    private Context context;
    
    public interface PredictionClickListener {
        void onPredictionClick(Date date, List<PredictionResult> predictions);
    }
    
    public WeeklyPredictionAdapter(List<DayPrediction> dayPredictions, PredictionClickListener clickListener) {
        this.dayPredictions = dayPredictions;
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public PredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_weekly_prediction, parent, false);
        return new PredictionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PredictionViewHolder holder, int position) {
        DayPrediction dayPrediction = dayPredictions.get(position);
        holder.bind(dayPrediction);
    }
    
    @Override
    public int getItemCount() {
        return dayPredictions.size();
    }
    
    /**
     * Update predictions with new data
     */
    public void updatePredictions(Map<Date, List<PredictionResult>> weeklyPredictions) {
        this.dayPredictions.clear();
        
        for (Map.Entry<Date, List<PredictionResult>> entry : weeklyPredictions.entrySet()) {
            this.dayPredictions.add(new DayPrediction(entry.getKey(), entry.getValue()));
        }
        
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for prediction items
     */
    public class PredictionViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvDayName;
        private TextView tvDate;
        private TextView tvTopActivity;
        private TextView tvConfidence;
        private View viewConfidenceBar;
        
        public PredictionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTopActivity = itemView.findViewById(R.id.tvTopActivity);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            viewConfidenceBar = itemView.findViewById(R.id.viewConfidenceBar);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    DayPrediction dayPrediction = dayPredictions.get(position);
                    clickListener.onPredictionClick(dayPrediction.getDate(), dayPrediction.getPredictions());
                }
            });
        }
        
        /**
         * Bind prediction data to views
         */
        public void bind(DayPrediction dayPrediction) {
            Date date = dayPrediction.getDate();
            List<PredictionResult> predictions = dayPrediction.getPredictions();
            
            // Format date
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d", Locale.getDefault());
            
            tvDayName.setText(dayFormat.format(date));
            tvDate.setText(dateFormat.format(date));
            
            // Get top prediction
            if (predictions != null && !predictions.isEmpty()) {
                PredictionResult topPrediction = predictions.stream()
                    .max((p1, p2) -> Double.compare(p1.getConfidenceScore(), p2.getConfidenceScore()))
                    .orElse(predictions.get(0));
                
                // Display top activity
                String activityName = topPrediction.getPredictedActivity().getDisplayName();
                tvTopActivity.setText(shortenActivityName(activityName));
                
                // Display confidence
                double confidence = topPrediction.getConfidenceScore();
                tvConfidence.setText(String.format("%.0f%%", confidence * 100));
                
                // Update confidence bar
                updateConfidenceBar(confidence);
                
                // Set colors based on confidence
                updateColors(confidence);
            } else {
                tvTopActivity.setText("No data");
                tvConfidence.setText("--");
                viewConfidenceBar.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        }
        
        /**
         * Shorten activity name for display
         */
        private String shortenActivityName(String activityName) {
            if (activityName.length() <= 12) {
                return activityName;
            }
            
            // Create abbreviations for common activities
            switch (activityName) {
                case "Outdoor Exercise":
                    return "Outdoor";
                case "Indoor Exercise":
                    return "Indoor Ex";
                case "Social Activity":
                    return "Social";
                case "Work/Productivity":
                    return "Work";
                case "Recreational":
                    return "Recreation";
                case "Relaxation":
                    return "Relax";
                case "Travel":
                    return "Travel";
                case "Photography":
                    return "Photo";
                case "Indoor Activities":
                    return "Indoor";
                case "Outdoor Leisure":
                    return "Outdoor";
                default:
                    return activityName.substring(0, Math.min(10, activityName.length()));
            }
        }
        
        /**
         * Update confidence bar appearance
         */
        private void updateConfidenceBar(double confidence) {
            // Set color based on confidence
            int color;
            if (confidence >= 0.8) {
                color = context.getResources().getColor(android.R.color.holo_green_dark);
            } else if (confidence >= 0.6) {
                color = context.getResources().getColor(android.R.color.holo_orange_dark);
            } else if (confidence >= 0.4) {
                color = context.getResources().getColor(android.R.color.holo_orange_light);
            } else {
                color = context.getResources().getColor(android.R.color.holo_red_light);
            }
            
            viewConfidenceBar.setBackgroundColor(color);
            
            // Set height based on confidence
            ViewGroup.LayoutParams params = viewConfidenceBar.getLayoutParams();
            if (params != null) {
                params.height = (int) (confidence * 20 + 4); // 4-24dp range
                viewConfidenceBar.setLayoutParams(params);
            }
        }
        
        /**
         * Update colors based on confidence
         */
        private void updateColors(double confidence) {
            int textColor;
            if (confidence >= 0.7) {
                textColor = context.getResources().getColor(android.R.color.black);
            } else if (confidence >= 0.5) {
                textColor = context.getResources().getColor(android.R.color.darker_gray);
            } else {
                textColor = context.getResources().getColor(android.R.color.darker_gray);
            }
            
            tvTopActivity.setTextColor(textColor);
            tvConfidence.setTextColor(textColor);
        }
    }
    
    /**
     * Helper class to represent a day's predictions
     */
    public static class DayPrediction {
        private Date date;
        private List<PredictionResult> predictions;
        
        public DayPrediction(Date date, List<PredictionResult> predictions) {
            this.date = date;
            this.predictions = predictions;
        }
        
        public Date getDate() {
            return date;
        }
        
        public List<PredictionResult> getPredictions() {
            return predictions;
        }
    }
}