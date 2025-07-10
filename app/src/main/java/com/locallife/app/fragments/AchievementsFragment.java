package com.locallife.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.R;
import com.locallife.model.Achievement;
import com.locallife.model.UserLevel;
import com.locallife.service.GamificationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying achievements and user level progression
 */
public class AchievementsFragment extends Fragment {
    
    private GamificationService gamificationService;
    private RecyclerView rvAchievements;
    private TextView tvLevelBadge;
    private TextView tvLevelTitle;
    private TextView tvLevelXP;
    private TextView tvRank;
    private TextView tvStatsSummary;
    private TextView tvTotalAchievements;
    private TextView tvUnlockedAchievements;
    private TextView tvCompletionPercentage;
    private TextView tvRecentActivity;
    private ProgressBar progressXP;
    private ProgressBar progressBar;
    private Button btnCheckAchievements;
    private Button btnFilterAll;
    private Button btnFilterUnlocked;
    private Button btnFilterCategory;
    private AchievementAdapter achievementAdapter;
    private Achievement.AchievementCategory currentCategory = Achievement.AchievementCategory.FITNESS;
    private FilterType currentFilter = FilterType.ALL;
    
    private enum FilterType {
        ALL, UNLOCKED, CATEGORY
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);
        
        initializeViews(view);
        initializeService();
        setupRecyclerView();
        setupClickListeners();
        
        loadUserLevel();
        loadAchievements();
        
        return view;
    }
    
    private void initializeViews(View view) {
        rvAchievements = view.findViewById(R.id.rv_achievements);
        tvLevelBadge = view.findViewById(R.id.tv_level_badge);
        tvLevelTitle = view.findViewById(R.id.tv_level_title);
        tvLevelXP = view.findViewById(R.id.tv_level_xp);
        tvRank = view.findViewById(R.id.tv_rank);
        tvStatsSummary = view.findViewById(R.id.tv_stats_summary);
        tvTotalAchievements = view.findViewById(R.id.tv_total_achievements);
        tvUnlockedAchievements = view.findViewById(R.id.tv_unlocked_achievements);
        tvCompletionPercentage = view.findViewById(R.id.tv_completion_percentage);
        tvRecentActivity = view.findViewById(R.id.tv_recent_activity);
        progressXP = view.findViewById(R.id.progress_xp);
        progressBar = view.findViewById(R.id.progress_bar);
        btnCheckAchievements = view.findViewById(R.id.btn_check_achievements);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterUnlocked = view.findViewById(R.id.btn_filter_unlocked);
        btnFilterCategory = view.findViewById(R.id.btn_filter_category);
    }
    
    private void initializeService() {
        gamificationService = new GamificationService(getContext());
    }
    
    private void setupRecyclerView() {
        achievementAdapter = new AchievementAdapter(new ArrayList<>());
        rvAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAchievements.setAdapter(achievementAdapter);
    }
    
    private void setupClickListeners() {
        btnCheckAchievements.setOnClickListener(v -> checkAchievements());
        btnFilterAll.setOnClickListener(v -> filterAchievements(FilterType.ALL));
        btnFilterUnlocked.setOnClickListener(v -> filterAchievements(FilterType.UNLOCKED));
        btnFilterCategory.setOnClickListener(v -> filterAchievements(FilterType.CATEGORY));
    }
    
    private void loadUserLevel() {
        gamificationService.getUserLevel(new GamificationService.UserLevelCallback() {
            @Override
            public void onUserLevelReceived(UserLevel userLevel) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        displayUserLevel(userLevel);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                // Handle error silently or show default level
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        displayUserLevel(new UserLevel());
                    });
                }
            }
        });
    }
    
    private void displayUserLevel(UserLevel userLevel) {
        tvLevelBadge.setText(String.valueOf(userLevel.getCurrentLevel()));
        tvLevelBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(userLevel.getLevelColor())));
        
        tvLevelTitle.setText(userLevel.getFormattedLevel());
        tvLevelXP.setText(userLevel.getFormattedXP());
        tvRank.setText(userLevel.getRank());
        tvStatsSummary.setText(userLevel.getStatsSummary());
        
        // Set XP progress
        float progress = userLevel.getProgressToNextLevel();
        progressXP.setProgress((int) progress);
        
        // Set rank color
        String rankColor = getRankColor(userLevel.getRank());
        tvRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(rankColor)));
    }
    
    private String getRankColor(String rank) {
        switch (rank) {
            case "S+":
            case "S": return "#FF6B35"; // Legendary
            case "A+":
            case "A": return "#FFD700"; // Gold
            case "B+":
            case "B": return "#C0C0C0"; // Silver
            case "C+":
            case "C": return "#CD7F32"; // Bronze
            default: return "#9E9E9E"; // Gray
        }
    }
    
    private void loadAchievements() {
        showProgress(true);
        
        gamificationService.getAllAchievements(false, false, new GamificationService.AchievementListCallback() {
            @Override
            public void onAchievementsReceived(List<Achievement> achievements) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        achievementAdapter.updateAchievements(achievements);
                        updateAchievementStatistics(achievements);
                        updateFilterButtons();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        createDefaultAchievements();
                    });
                }
            }
        });
    }
    
    private void createDefaultAchievements() {
        gamificationService.createDefaultAchievements(new GamificationService.AchievementListCallback() {
            @Override
            public void onAchievementsReceived(List<Achievement> achievements) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        achievementAdapter.updateAchievements(achievements);
                        updateAchievementStatistics(achievements);
                        updateFilterButtons();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }
    
    private void updateAchievementStatistics(List<Achievement> achievements) {
        int totalAchievements = achievements.size();
        int unlockedCount = 0;
        
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlockedCount++;
            }
        }
        
        float completionRate = totalAchievements > 0 ? ((float) unlockedCount / totalAchievements) * 100 : 0;
        
        tvTotalAchievements.setText(String.valueOf(totalAchievements));
        tvUnlockedAchievements.setText(String.valueOf(unlockedCount));
        tvCompletionPercentage.setText(String.format("%.0f%%", completionRate));
        
        // Update recent activity
        if (unlockedCount > 0) {
            tvRecentActivity.setText("Great progress! You've unlocked " + unlockedCount + " achievements.");
        } else {
            tvRecentActivity.setText("Complete your first goal to start earning achievements!");
        }
    }
    
    private void filterAchievements(FilterType filterType) {
        currentFilter = filterType;
        showProgress(true);
        
        switch (filterType) {
            case ALL:
                gamificationService.getAllAchievements(false, false, createFilterCallback());
                break;
            case UNLOCKED:
                gamificationService.getAllAchievements(true, false, createFilterCallback());
                break;
            case CATEGORY:
                // Cycle through categories
                currentCategory = getNextCategory(currentCategory);
                btnFilterCategory.setText(currentCategory.name());
                gamificationService.getAchievementsByCategory(currentCategory, createFilterCallback());
                break;
        }
        
        updateFilterButtons();
    }
    
    private Achievement.AchievementCategory getNextCategory(Achievement.AchievementCategory current) {
        Achievement.AchievementCategory[] categories = Achievement.AchievementCategory.values();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] == current) {
                return categories[(i + 1) % categories.length];
            }
        }
        return Achievement.AchievementCategory.FITNESS;
    }
    
    private GamificationService.AchievementListCallback createFilterCallback() {
        return new GamificationService.AchievementListCallback() {
            @Override
            public void onAchievementsReceived(List<Achievement> achievements) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        achievementAdapter.updateAchievements(achievements);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                    });
                }
            }
        };
    }
    
    private void checkAchievements() {
        showProgress(true);
        
        gamificationService.checkAchievements(new GamificationService.AchievementCheckCallback() {
            @Override
            public void onAchievementsChecked(List<Achievement> newlyUnlocked) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        
                        if (!newlyUnlocked.isEmpty()) {
                            showAchievementUnlockedDialog(newlyUnlocked);
                        }
                        
                        // Refresh the list
                        loadAchievements();
                        loadUserLevel();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                    });
                }
            }
        });
    }
    
    private void showAchievementUnlockedDialog(List<Achievement> achievements) {
        if (getActivity() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            builder.setTitle("🎉 Achievement" + (achievements.size() > 1 ? "s" : "") + " Unlocked!");
            
            StringBuilder message = new StringBuilder();
            int totalXP = 0;
            
            for (Achievement achievement : achievements) {
                message.append("🏆 ").append(achievement.getTitle()).append("\n");
                message.append(achievement.getDescription()).append("\n");
                message.append("+").append(achievement.getPointsValue()).append(" XP\n\n");
                totalXP += achievement.getPointsValue();
            }
            
            message.append("Total XP earned: +").append(totalXP);
            
            builder.setMessage(message.toString());
            builder.setPositiveButton("Awesome!", null);
            builder.show();
        }
    }
    
    private void updateFilterButtons() {
        // Reset button states
        btnFilterAll.setSelected(currentFilter == FilterType.ALL);
        btnFilterUnlocked.setSelected(currentFilter == FilterType.UNLOCKED);
        btnFilterCategory.setSelected(currentFilter == FilterType.CATEGORY);
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvAchievements.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gamificationService != null) {
            gamificationService.shutdown();
        }
    }
    
    // RecyclerView Adapter
    private class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
        private List<Achievement> achievements;
        
        public AchievementAdapter(List<Achievement> achievements) {
            this.achievements = achievements;
        }
        
        @Override
        public AchievementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_achievement, parent, false);
            return new AchievementViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(AchievementViewHolder holder, int position) {
            Achievement achievement = achievements.get(position);
            holder.bind(achievement);
        }
        
        @Override
        public int getItemCount() {
            return achievements.size();
        }
        
        public void updateAchievements(List<Achievement> newAchievements) {
            this.achievements = newAchievements;
            notifyDataSetChanged();
        }
        
        class AchievementViewHolder extends RecyclerView.ViewHolder {
            TextView tvBadge;
            TextView tvTier;
            TextView tvTitle;
            TextView tvDescription;
            TextView tvPoints;
            TextView tvProgress;
            TextView tvPercentage;
            TextView tvUnlockedDate;
            TextView tvHiddenMessage;
            ProgressBar progressAchievement;
            LinearLayout layoutProgress;
            LinearLayout layoutUnlocked;
            
            AchievementViewHolder(View itemView) {
                super(itemView);
                tvBadge = itemView.findViewById(R.id.tv_achievement_badge);
                tvTier = itemView.findViewById(R.id.tv_achievement_tier);
                tvTitle = itemView.findViewById(R.id.tv_achievement_title);
                tvDescription = itemView.findViewById(R.id.tv_achievement_description);
                tvPoints = itemView.findViewById(R.id.tv_achievement_points);
                tvProgress = itemView.findViewById(R.id.tv_achievement_progress);
                tvPercentage = itemView.findViewById(R.id.tv_achievement_percentage);
                tvUnlockedDate = itemView.findViewById(R.id.tv_unlocked_date);
                tvHiddenMessage = itemView.findViewById(R.id.tv_hidden_message);
                progressAchievement = itemView.findViewById(R.id.progress_achievement);
                layoutProgress = itemView.findViewById(R.id.layout_progress);
                layoutUnlocked = itemView.findViewById(R.id.layout_unlocked);
            }
            
            void bind(Achievement achievement) {
                // Set basic info
                tvTitle.setText(achievement.getTitle());
                tvDescription.setText(achievement.getDescription());
                tvTier.setText(achievement.getTierDisplayName());
                tvPoints.setText("+" + achievement.getPointsValue() + " XP");
                
                // Set badge icon based on category
                String badgeIcon = getAchievementIcon(achievement.getCategory());
                tvBadge.setText(badgeIcon);
                
                // Set badge background color
                try {
                    int badgeColor = Color.parseColor(achievement.getBadgeColor());
                    tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(badgeColor));
                } catch (Exception e) {
                    tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                }
                
                // Handle different achievement states
                if (achievement.isHidden() && !achievement.isUnlocked()) {
                    // Hidden achievement
                    layoutProgress.setVisibility(View.GONE);
                    layoutUnlocked.setVisibility(View.GONE);
                    tvHiddenMessage.setVisibility(View.VISIBLE);
                    tvTitle.setText("???");
                    tvDescription.setText("Secret Achievement");
                } else if (achievement.isUnlocked()) {
                    // Unlocked achievement
                    layoutProgress.setVisibility(View.GONE);
                    layoutUnlocked.setVisibility(View.VISIBLE);
                    tvHiddenMessage.setVisibility(View.GONE);
                    
                    // Set unlock date
                    if (achievement.getUnlockedAt() != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                        tvUnlockedDate.setText(dateFormat.format(achievement.getUnlockedAt()));
                    } else {
                        tvUnlockedDate.setText("Recently");
                    }
                } else {
                    // In progress achievement
                    layoutProgress.setVisibility(View.VISIBLE);
                    layoutUnlocked.setVisibility(View.GONE);
                    tvHiddenMessage.setVisibility(View.GONE);
                    
                    // Set progress
                    float progress = achievement.getProgressPercentage();
                    progressAchievement.setProgress((int) progress);
                    tvProgress.setText(achievement.getFormattedProgress());
                    tvPercentage.setText(String.format("%.0f%%", progress));
                }
                
                // Handle clicks
                itemView.setOnClickListener(v -> {
                    showAchievementDetails(achievement);
                });
            }
        }
    }
    
    private String getAchievementIcon(Achievement.AchievementCategory category) {
        switch (category) {
            case FITNESS: return "🏃";
            case HEALTH: return "❤️";
            case PRODUCTIVITY: return "⚡";
            case SOCIAL: return "👥";
            case EXPLORATION: return "🗺️";
            case PHOTOGRAPHY: return "📸";
            case CONSISTENCY: return "🔥";
            case ENVIRONMENTAL: return "🌤️";
            case LEARNING: return "🧠";
            case SPECIAL: return "⭐";
            default: return "🏆";
        }
    }
    
    private void showAchievementDetails(Achievement achievement) {
        if (getActivity() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            builder.setTitle(achievement.getTitle());
            
            StringBuilder details = new StringBuilder();
            details.append("Description: ").append(achievement.getDescription()).append("\n\n");
            details.append("Category: ").append(achievement.getCategory().name()).append("\n");
            details.append("Tier: ").append(achievement.getTierDisplayName()).append("\n");
            details.append("Points: ").append(achievement.getPointsValue()).append(" XP\n\n");
            
            if (achievement.isUnlocked()) {
                details.append("Status: Unlocked ✅\n");
                if (achievement.getUnlockedAt() != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    details.append("Unlocked: ").append(dateFormat.format(achievement.getUnlockedAt())).append("\n");
                }
            } else {
                details.append("Progress: ").append(achievement.getFormattedProgress()).append("\n");
                details.append("Completion: ").append(String.format("%.1f%%", achievement.getProgressPercentage())).append("\n");
                details.append("Requirements: ").append(achievement.getFormattedRequirements()).append("\n");
            }
            
            builder.setMessage(details.toString());
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }
}