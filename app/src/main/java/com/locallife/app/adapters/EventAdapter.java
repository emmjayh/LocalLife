package com.locallife.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.locallife.app.R;
import com.locallife.app.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    
    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onEventLongClick(Event event);
    }
    
    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void addEvent(Event event) {
        events.add(event);
        notifyItemInserted(events.size() - 1);
    }
    
    public void removeEvent(int position) {
        if (position >= 0 && position < events.size()) {
            events.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void updateEvent(int position, Event event) {
        if (position >= 0 && position < events.size()) {
            events.set(position, event);
            notifyItemChanged(position);
        }
    }
    
    public Event getEvent(int position) {
        if (position >= 0 && position < events.size()) {
            return events.get(position);
        }
        return null;
    }
    
    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTime;
        private TextView tvDate;
        private TextView tvLocation;
        private TextView tvDescription;
        private ImageView ivEventIcon;
        private View eventColorIndicator;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvTime = itemView.findViewById(R.id.tv_event_time);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            tvLocation = itemView.findViewById(R.id.tv_event_location);
            tvDescription = itemView.findViewById(R.id.tv_event_description);
            ivEventIcon = itemView.findViewById(R.id.iv_event_icon);
            eventColorIndicator = itemView.findViewById(R.id.event_color_indicator);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventLongClick(events.get(position));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Event event) {
            tvTitle.setText(event.getTitle());
            tvTime.setText(timeFormat.format(event.getStartTime()));
            tvDate.setText(dateFormat.format(event.getStartTime()));
            
            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                tvLocation.setText(event.getLocation());
                tvLocation.setVisibility(View.VISIBLE);
            } else {
                tvLocation.setVisibility(View.GONE);
            }
            
            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                tvDescription.setText(event.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            // Set event icon based on type
            setEventIcon(event.getType());
            
            // Set color indicator
            if (event.getColor() != 0) {
                eventColorIndicator.setBackgroundColor(event.getColor());
                eventColorIndicator.setVisibility(View.VISIBLE);
            } else {
                eventColorIndicator.setVisibility(View.GONE);
            }
            
            // Add animation for new items
            animateItem();
        }
        
        private void setEventIcon(String eventType) {
            int iconResId;
            switch (eventType != null ? eventType.toLowerCase() : "") {
                case "meeting":
                    iconResId = R.drawable.ic_event;
                    break;
                case "appointment":
                    iconResId = R.drawable.ic_calendar;
                    break;
                case "social":
                    iconResId = R.drawable.ic_community;
                    break;
                case "travel":
                    iconResId = R.drawable.ic_location;
                    break;
                default:
                    iconResId = R.drawable.ic_event;
                    break;
            }
            ivEventIcon.setImageResource(iconResId);
        }
        
        private void animateItem() {
            itemView.setAlpha(0.6f);
            itemView.setScaleX(0.9f);
            itemView.setScaleY(0.9f);
            
            itemView.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200)
                .start();
        }
    }
}