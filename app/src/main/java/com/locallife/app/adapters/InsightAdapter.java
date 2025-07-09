package com.locallife.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.locallife.app.R;

import java.util.ArrayList;
import java.util.List;

public class InsightAdapter extends RecyclerView.Adapter<InsightAdapter.InsightViewHolder> {
    private List<Insight> insights = new ArrayList<>();
    private OnInsightClickListener listener;
    
    public interface OnInsightClickListener {
        void onInsightClick(Insight insight);
    }
    
    public static class Insight {
        private String title;
        private String description;
        private String value;
        private String trend;
        private int iconResId;
        private int priority; // 1 = high, 2 = medium, 3 = low
        private String category;
        private long timestamp;
        
        public Insight(String title, String description, String value, String trend, 
                      int iconResId, int priority, String category, long timestamp) {
            this.title = title;
            this.description = description;
            this.value = value;
            this.trend = trend;
            this.iconResId = iconResId;
            this.priority = priority;
            this.category = category;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getValue() { return value; }
        public String getTrend() { return trend; }
        public int getIconResId() { return iconResId; }
        public int getPriority() { return priority; }
        public String getCategory() { return category; }
        public long getTimestamp() { return timestamp; }
    }
    
    public InsightAdapter(OnInsightClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public InsightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_insight, parent, false);
        return new InsightViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull InsightViewHolder holder, int position) {
        Insight insight = insights.get(position);
        holder.bind(insight);
    }
    
    @Override
    public int getItemCount() {
        return insights.size();
    }
    
    public void setInsights(List<Insight> insights) {
        this.insights = insights != null ? insights : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void addInsight(Insight insight) {
        insights.add(0, insight);
        notifyItemInserted(0);
    }
    
    public void clearInsights() {
        insights.clear();
        notifyDataSetChanged();
    }
    
    class InsightViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvValue;
        private TextView tvTrend;
        private TextView tvCategory;
        private ImageView ivInsightIcon;
        private ImageView ivTrendIcon;
        private View priorityIndicator;
        
        public InsightViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTitle = itemView.findViewById(R.id.tv_insight_title);
            tvDescription = itemView.findViewById(R.id.tv_insight_description);
            tvValue = itemView.findViewById(R.id.tv_insight_value);
            tvTrend = itemView.findViewById(R.id.tv_insight_trend);
            tvCategory = itemView.findViewById(R.id.tv_insight_category);
            ivInsightIcon = itemView.findViewById(R.id.iv_insight_icon);
            ivTrendIcon = itemView.findViewById(R.id.iv_trend_icon);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onInsightClick(insights.get(position));
                }
            });
        }
        
        public void bind(Insight insight) {
            tvTitle.setText(insight.getTitle());
            tvDescription.setText(insight.getDescription());
            tvCategory.setText(insight.getCategory());
            
            if (insight.getValue() != null && !insight.getValue().isEmpty()) {
                tvValue.setText(insight.getValue());
                tvValue.setVisibility(View.VISIBLE);
            } else {
                tvValue.setVisibility(View.GONE);
            }
            
            if (insight.getTrend() != null && !insight.getTrend().isEmpty()) {
                tvTrend.setText(insight.getTrend());
                tvTrend.setVisibility(View.VISIBLE);
                
                // Set trend icon and color
                setTrendIcon(insight.getTrend());
            } else {
                tvTrend.setVisibility(View.GONE);
                ivTrendIcon.setVisibility(View.GONE);
            }
            
            ivInsightIcon.setImageResource(insight.getIconResId());
            
            // Set priority indicator color
            setPriorityIndicator(insight.getPriority());
            
            // Set category-specific styling
            setCategoryStyle(insight.getCategory());
            
            // Add fade-in animation
            animateItem();
        }
        
        private void setTrendIcon(String trend) {
            if (trend.contains("↑") || trend.toLowerCase().contains("up") || trend.toLowerCase().contains("increase")) {
                ivTrendIcon.setImageResource(R.drawable.ic_arrow_up);
                ivTrendIcon.setColorFilter(itemView.getContext().getColor(R.color.success));
                tvTrend.setTextColor(itemView.getContext().getColor(R.color.success));
            } else if (trend.contains("↓") || trend.toLowerCase().contains("down") || trend.toLowerCase().contains("decrease")) {
                ivTrendIcon.setImageResource(R.drawable.ic_arrow_down);
                ivTrendIcon.setColorFilter(itemView.getContext().getColor(R.color.error));
                tvTrend.setTextColor(itemView.getContext().getColor(R.color.error));
            } else {
                ivTrendIcon.setImageResource(R.drawable.ic_arrow_right);
                ivTrendIcon.setColorFilter(itemView.getContext().getColor(R.color.secondary_text));
                tvTrend.setTextColor(itemView.getContext().getColor(R.color.secondary_text));
            }
            ivTrendIcon.setVisibility(View.VISIBLE);
        }
        
        private void setPriorityIndicator(int priority) {
            int color;
            switch (priority) {
                case 1: // High priority
                    color = itemView.getContext().getColor(R.color.error);
                    break;
                case 2: // Medium priority
                    color = itemView.getContext().getColor(R.color.warning);
                    break;
                case 3: // Low priority
                    color = itemView.getContext().getColor(R.color.success);
                    break;
                default:
                    color = itemView.getContext().getColor(R.color.secondary_text);
                    break;
            }
            priorityIndicator.setBackgroundColor(color);
        }
        
        private void setCategoryStyle(String category) {
            int backgroundColor;
            switch (category != null ? category.toLowerCase() : "") {
                case "health":
                    backgroundColor = itemView.getContext().getColor(R.color.success);
                    break;
                case "activity":
                    backgroundColor = itemView.getContext().getColor(R.color.primary);
                    break;
                case "location":
                    backgroundColor = itemView.getContext().getColor(R.color.accent);
                    break;
                case "weather":
                    backgroundColor = itemView.getContext().getColor(R.color.warning);
                    break;
                default:
                    backgroundColor = itemView.getContext().getColor(R.color.primary_light);
                    break;
            }
            
            // Set subtle background tint
            itemView.setBackgroundColor(backgroundColor);
            itemView.getBackground().setAlpha(20); // Very subtle tint
        }
        
        private void animateItem() {
            itemView.setAlpha(0.3f);
            itemView.setTranslationY(50f);
            
            itemView.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(getAdapterPosition() * 50L)
                .start();
        }
    }
}