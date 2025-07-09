package com.locallife.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.locallife.app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
    private List<Place> places = new ArrayList<>();
    private OnPlaceClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
        void onPlaceDirectionsClick(Place place);
    }
    
    public static class Place {
        private String name;
        private String address;
        private String category;
        private double latitude;
        private double longitude;
        private long firstVisit;
        private long lastVisit;
        private int visitCount;
        private String description;
        private int iconResId;
        private float rating;
        private String phoneNumber;
        private String website;
        
        public Place(String name, String address, String category, double latitude, double longitude,
                    long firstVisit, long lastVisit, int visitCount, String description, int iconResId) {
            this.name = name;
            this.address = address;
            this.category = category;
            this.latitude = latitude;
            this.longitude = longitude;
            this.firstVisit = firstVisit;
            this.lastVisit = lastVisit;
            this.visitCount = visitCount;
            this.description = description;
            this.iconResId = iconResId;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getCategory() { return category; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public long getFirstVisit() { return firstVisit; }
        public long getLastVisit() { return lastVisit; }
        public int getVisitCount() { return visitCount; }
        public String getDescription() { return description; }
        public int getIconResId() { return iconResId; }
        public float getRating() { return rating; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getWebsite() { return website; }
        
        public void setRating(float rating) { this.rating = rating; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public void setWebsite(String website) { this.website = website; }
    }
    
    public PlaceAdapter(OnPlaceClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);
    }
    
    @Override
    public int getItemCount() {
        return places.size();
    }
    
    public void setPlaces(List<Place> places) {
        this.places = places != null ? places : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void addPlace(Place place) {
        places.add(place);
        notifyItemInserted(places.size() - 1);
    }
    
    public void updatePlace(int position, Place place) {
        if (position >= 0 && position < places.size()) {
            places.set(position, place);
            notifyItemChanged(position);
        }
    }
    
    public void removePlace(int position) {
        if (position >= 0 && position < places.size()) {
            places.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    class PlaceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvAddress;
        private TextView tvCategory;
        private TextView tvVisitCount;
        private TextView tvLastVisit;
        private TextView tvDescription;
        private ImageView ivPlaceIcon;
        private ImageView ivDirections;
        private View ratingBar;
        private TextView tvRating;
        
        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvName = itemView.findViewById(R.id.tv_place_name);
            tvAddress = itemView.findViewById(R.id.tv_place_address);
            tvCategory = itemView.findViewById(R.id.tv_place_category);
            tvVisitCount = itemView.findViewById(R.id.tv_visit_count);
            tvLastVisit = itemView.findViewById(R.id.tv_last_visit);
            tvDescription = itemView.findViewById(R.id.tv_place_description);
            ivPlaceIcon = itemView.findViewById(R.id.iv_place_icon);
            ivDirections = itemView.findViewById(R.id.iv_directions);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvRating = itemView.findViewById(R.id.tv_rating);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlaceClick(places.get(position));
                }
            });
            
            ivDirections.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlaceDirectionsClick(places.get(position));
                }
            });
        }
        
        public void bind(Place place) {
            tvName.setText(place.getName());
            tvAddress.setText(place.getAddress());
            tvCategory.setText(place.getCategory());
            
            // Format visit count
            String visitText = place.getVisitCount() == 1 ? 
                "1 visit" : place.getVisitCount() + " visits";
            tvVisitCount.setText(visitText);
            
            // Format last visit
            if (place.getLastVisit() > 0) {
                String lastVisitText = "Last visit: " + timeFormat.format(place.getLastVisit());
                tvLastVisit.setText(lastVisitText);
                tvLastVisit.setVisibility(View.VISIBLE);
            } else {
                tvLastVisit.setVisibility(View.GONE);
            }
            
            // Set description
            if (place.getDescription() != null && !place.getDescription().isEmpty()) {
                tvDescription.setText(place.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            // Set place icon based on category
            setPlaceIcon(place.getCategory());
            
            // Set rating
            if (place.getRating() > 0) {
                tvRating.setText(String.format(Locale.getDefault(), "%.1f", place.getRating()));
                ratingBar.setVisibility(View.VISIBLE);
            } else {
                ratingBar.setVisibility(View.GONE);
            }
            
            // Set category-specific styling
            setCategoryStyle(place.getCategory());
            
            // Add entry animation
            animateItem();
        }
        
        private void setPlaceIcon(String category) {
            int iconResId;
            switch (category != null ? category.toLowerCase() : "") {
                case "restaurant":
                case "food":
                    iconResId = R.drawable.ic_restaurant;
                    break;
                case "shopping":
                case "store":
                    iconResId = R.drawable.ic_shopping;
                    break;
                case "work":
                case "office":
                    iconResId = R.drawable.ic_work;
                    break;
                case "home":
                    iconResId = R.drawable.ic_home;
                    break;
                case "entertainment":
                case "leisure":
                    iconResId = R.drawable.ic_entertainment;
                    break;
                case "health":
                case "medical":
                    iconResId = R.drawable.ic_health;
                    break;
                case "education":
                case "school":
                    iconResId = R.drawable.ic_school;
                    break;
                case "transport":
                case "travel":
                    iconResId = R.drawable.ic_transport;
                    break;
                default:
                    iconResId = R.drawable.ic_location;
                    break;
            }
            ivPlaceIcon.setImageResource(iconResId);
        }
        
        private void setCategoryStyle(String category) {
            int categoryColor;
            switch (category != null ? category.toLowerCase() : "") {
                case "restaurant":
                case "food":
                    categoryColor = itemView.getContext().getColor(R.color.accent);
                    break;
                case "shopping":
                case "store":
                    categoryColor = itemView.getContext().getColor(R.color.primary);
                    break;
                case "work":
                case "office":
                    categoryColor = itemView.getContext().getColor(R.color.secondary_text);
                    break;
                case "home":
                    categoryColor = itemView.getContext().getColor(R.color.success);
                    break;
                case "entertainment":
                case "leisure":
                    categoryColor = itemView.getContext().getColor(R.color.warning);
                    break;
                case "health":
                case "medical":
                    categoryColor = itemView.getContext().getColor(R.color.error);
                    break;
                default:
                    categoryColor = itemView.getContext().getColor(R.color.primary_light);
                    break;
            }
            
            tvCategory.setTextColor(categoryColor);
            ivPlaceIcon.setColorFilter(categoryColor);
        }
        
        private void animateItem() {
            itemView.setAlpha(0.4f);
            itemView.setScaleX(0.9f);
            itemView.setScaleY(0.9f);
            
            itemView.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .setStartDelay(getAdapterPosition() * 30L)
                .start();
        }
    }
}