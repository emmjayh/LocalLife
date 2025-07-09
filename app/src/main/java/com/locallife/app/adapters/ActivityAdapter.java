package com.locallife.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.locallife.app.R;
import com.locallife.app.models.ActivityRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private List<ActivityRecord> activities = new ArrayList<>();
    private OnActivityClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    public interface OnActivityClickListener {
        void onActivityClick(ActivityRecord activity);
    }
    
    public static class ActivityRecord {
        private String type;
        private String title;
        private String description;
        private long timestamp;
        private String location;
        private int iconResId;
        private String value;
        private String unit;
        
        public ActivityRecord(String type, String title, String description, 
                            long timestamp, String location, int iconResId, String value, String unit) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.location = location;
            this.iconResId = iconResId;
            this.value = value;
            this.unit = unit;
        }
        
        // Getters
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
        public String getLocation() { return location; }
        public int getIconResId() { return iconResId; }
        public String getValue() { return value; }
        public String getUnit() { return unit; }
    }
    
    public ActivityAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityRecord activity = activities.get(position);
        holder.bind(activity);
    }
    
    @Override
    public int getItemCount() {
        return activities.size();
    }
    
    public void setActivities(List<ActivityRecord> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void addActivity(ActivityRecord activity) {
        activities.add(0, activity); // Add to top
        notifyItemInserted(0);
    }
    
    public void clearActivities() {
        activities.clear();
        notifyDataSetChanged();
    }
    
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvTime;
        private TextView tvLocation;
        private TextView tvValue;
        private ImageView ivActivityIcon;
        private View timelineDot;
        private View timelineLine;
        
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvDescription = itemView.findViewById(R.id.tv_activity_description);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
            tvLocation = itemView.findViewById(R.id.tv_activity_location);
            tvValue = itemView.findViewById(R.id.tv_activity_value);
            ivActivityIcon = itemView.findViewById(R.id.iv_activity_icon);
            timelineDot = itemView.findViewById(R.id.timeline_dot);
            timelineLine = itemView.findViewById(R.id.timeline_line);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onActivityClick(activities.get(position));
                }
            });
        }
        
        public void bind(ActivityRecord activity) {
            tvTitle.setText(activity.getTitle());
            tvDescription.setText(activity.getDescription());
            tvTime.setText(timeFormat.format(activity.getTimestamp()));
            
            if (activity.getLocation() != null && !activity.getLocation().isEmpty()) {
                tvLocation.setText(activity.getLocation());
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }
            
            if (activity.getValue() != null && !activity.getValue().isEmpty()) {
                String valueText = activity.getValue();
                if (activity.getUnit() != null && !activity.getUnit().isEmpty()) {
                    valueText += " " + activity.getUnit();
                }
                tvValue.setText(valueText);
                tvValue.setVisibility(View.VISIBLE);
            } else {
                tvValue.setVisibility(View.GONE);
            }
            
            ivActivityIcon.setImageResource(activity.getIconResId());
            
            // Hide timeline line for last item
            if (getAdapterPosition() == activities.size() - 1) {
                timelineLine.setVisibility(View.INVISIBLE);
            } else {
                timelineLine.setVisibility(View.VISIBLE);
            }
            
            // Set different colors for different activity types
            setActivityStyle(activity.getType());
            
            // Add slide-in animation
            animateItem();
        }
        
        private void setActivityStyle(String type) {
            int dotColor;
            switch (type != null ? type.toLowerCase() : "") {
                case "steps":
                    dotColor = itemView.getContext().getColor(R.color.success);
                    break;
                case "location":
                    dotColor = itemView.getContext().getColor(R.color.primary);
                    break;
                case "weather":
                    dotColor = itemView.getContext().getColor(R.color.warning);
                    break;
                case "battery":
                    dotColor = itemView.getContext().getColor(R.color.accent);
                    break;
                case "screen":
                    dotColor = itemView.getContext().getColor(R.color.secondary_text);
                    break;
                default:
                    dotColor = itemView.getContext().getColor(R.color.primary);
                    break;
            }
            timelineDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dotColor));
        }
        
        private void animateItem() {
            itemView.setTranslationX(300f);
            itemView.setAlpha(0.3f);
            
            itemView.animate()
                .translationX(0f)
                .alpha(1.0f)
                .setDuration(300)
                .setStartDelay(getAdapterPosition() * 50L)
                .start();
        }
    }
}