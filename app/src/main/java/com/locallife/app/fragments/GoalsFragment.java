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
import com.locallife.model.Goal;
import com.locallife.service.GoalManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying and managing user goals
 */
public class GoalsFragment extends Fragment {
    
    private GoalManagementService goalService;
    private RecyclerView rvGoals;
    private LinearLayout layoutStats;
    private TextView tvTotalGoals;
    private TextView tvActiveGoals;
    private TextView tvCompletedGoals;
    private TextView tvCompletionRate;
    private TextView tvCurrentStreak;
    private Button btnAddGoal;
    private Button btnFilterAll;
    private Button btnFilterActive;
    private Button btnFilterCompleted;
    private ProgressBar progressBar;
    private GoalAdapter goalAdapter;
    private Goal.GoalCategory currentFilter = null;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);
        
        initializeViews(view);
        initializeService();
        setupRecyclerView();
        setupClickListeners();
        
        loadGoals();
        loadStatistics();
        
        return view;
    }
    
    private void initializeViews(View view) {
        rvGoals = view.findViewById(R.id.rv_goals);
        layoutStats = view.findViewById(R.id.layout_stats);
        tvTotalGoals = view.findViewById(R.id.tv_total_goals);
        tvActiveGoals = view.findViewById(R.id.tv_active_goals);
        tvCompletedGoals = view.findViewById(R.id.tv_completed_goals);
        tvCompletionRate = view.findViewById(R.id.tv_completion_rate);
        tvCurrentStreak = view.findViewById(R.id.tv_current_streak);
        btnAddGoal = view.findViewById(R.id.btn_add_goal);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterActive = view.findViewById(R.id.btn_filter_active);
        btnFilterCompleted = view.findViewById(R.id.btn_filter_completed);
        progressBar = view.findViewById(R.id.progress_bar);
    }
    
    private void initializeService() {
        goalService = new GoalManagementService(getContext());
    }
    
    private void setupRecyclerView() {
        goalAdapter = new GoalAdapter(new ArrayList<>());
        rvGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGoals.setAdapter(goalAdapter);
    }
    
    private void setupClickListeners() {
        btnAddGoal.setOnClickListener(v -> showAddGoalDialog());
        btnFilterAll.setOnClickListener(v -> filterGoals(null));
        btnFilterActive.setOnClickListener(v -> filterActiveGoals());
        btnFilterCompleted.setOnClickListener(v -> filterCompletedGoals());
    }
    
    private void loadGoals() {
        showProgress(true);
        
        goalService.getActiveGoals(new GoalManagementService.GoalListCallback() {
            @Override
            public void onGoalsReceived(List<Goal> goals) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        goalAdapter.updateGoals(goals);
                        updateFilterButtons();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        showError(error);
                    });
                }
            }
        });
    }
    
    private void loadStatistics() {
        goalService.getGoalStatistics(new GoalManagementService.GoalStatsCallback() {
            @Override
            public void onStatsReceived(GoalManagementService.GoalStatistics stats) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        displayStatistics(stats);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                // Handle error silently for stats
            }
        });
    }
    
    private void displayStatistics(GoalManagementService.GoalStatistics stats) {
        tvTotalGoals.setText(String.valueOf(stats.totalGoals));
        tvActiveGoals.setText(String.valueOf(stats.activeGoals));
        tvCompletedGoals.setText(String.valueOf(stats.completedGoals));
        tvCompletionRate.setText(String.format("%.1f%%", stats.completionRate));
        tvCurrentStreak.setText(String.valueOf(stats.currentStreak));
    }
    
    private void filterGoals(Goal.GoalCategory category) {
        currentFilter = category;
        
        if (category == null) {
            loadGoals();
        } else {
            showProgress(true);
            
            goalService.getGoalsByCategory(category, new GoalManagementService.GoalListCallback() {
                @Override
                public void onGoalsReceived(List<Goal> goals) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showProgress(false);
                            goalAdapter.updateGoals(goals);
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showProgress(false);
                            showError(error);
                        });
                    }
                }
            });
        }
        
        updateFilterButtons();
    }
    
    private void filterActiveGoals() {
        List<Goal> activeGoals = new ArrayList<>();
        for (Goal goal : goalAdapter.getAllGoals()) {
            if (goal.isActive() && !goal.isCompleted()) {
                activeGoals.add(goal);
            }
        }
        goalAdapter.updateGoals(activeGoals);
        updateFilterButtons();
    }
    
    private void filterCompletedGoals() {
        List<Goal> completedGoals = new ArrayList<>();
        for (Goal goal : goalAdapter.getAllGoals()) {
            if (goal.isCompleted()) {
                completedGoals.add(goal);
            }
        }
        goalAdapter.updateGoals(completedGoals);
        updateFilterButtons();
    }
    
    private void updateFilterButtons() {
        // Reset all button states
        btnFilterAll.setSelected(false);
        btnFilterActive.setSelected(false);
        btnFilterCompleted.setSelected(false);
        
        // Set selected button
        if (currentFilter == null) {
            btnFilterAll.setSelected(true);
        }
    }
    
    private void showAddGoalDialog() {
        // Create default goals for demo
        goalService.createDefaultGoals(new GoalManagementService.GoalListCallback() {
            @Override
            public void onGoalsReceived(List<Goal> goals) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadGoals();
                        loadStatistics();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError(error);
                    });
                }
            }
        });
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvGoals.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String error) {
        // In a real app, you'd show a snackbar or toast
        // For now, just log the error
        android.util.Log.e("GoalsFragment", error);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (goalService != null) {
            goalService.shutdown();
        }
    }
    
    // RecyclerView Adapter
    private class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
        private List<Goal> goals;
        private List<Goal> allGoals;
        
        public GoalAdapter(List<Goal> goals) {
            this.goals = goals;
            this.allGoals = new ArrayList<>(goals);
        }
        
        @Override
        public GoalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_goal, parent, false);
            return new GoalViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(GoalViewHolder holder, int position) {
            Goal goal = goals.get(position);
            holder.bind(goal);
        }
        
        @Override
        public int getItemCount() {
            return goals.size();
        }
        
        public void updateGoals(List<Goal> newGoals) {
            this.goals = newGoals;
            this.allGoals = new ArrayList<>(newGoals);
            notifyDataSetChanged();
        }
        
        public List<Goal> getAllGoals() {
            return allGoals;
        }
        
        class GoalViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvDescription;
            TextView tvProgress;
            TextView tvProgressPercentage;
            TextView tvStatus;
            TextView tvTimeRemaining;
            TextView tvMotivation;
            TextView tvStreak;
            TextView tvPriority;
            ProgressBar progressBar;
            View colorIndicator;
            
            GoalViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvDescription = itemView.findViewById(R.id.tv_description);
                tvProgress = itemView.findViewById(R.id.tv_progress);
                tvProgressPercentage = itemView.findViewById(R.id.tv_progress_percentage);
                tvStatus = itemView.findViewById(R.id.tv_status);
                tvTimeRemaining = itemView.findViewById(R.id.tv_time_remaining);
                tvMotivation = itemView.findViewById(R.id.tv_motivation);
                tvStreak = itemView.findViewById(R.id.tv_streak);
                tvPriority = itemView.findViewById(R.id.tv_priority);
                progressBar = itemView.findViewById(R.id.progress_bar);
                colorIndicator = itemView.findViewById(R.id.color_indicator);
            }
            
            void bind(Goal goal) {
                tvTitle.setText(goal.getTitle());
                tvDescription.setText(goal.getDescription());
                tvProgress.setText(goal.getFormattedProgress());
                tvStatus.setText(goal.getStatusText());
                tvTimeRemaining.setText(goal.getFormattedRemainingTime());
                tvMotivation.setText(goal.getMotivationalMessage());
                tvStreak.setText(goal.getFormattedStreak());
                tvPriority.setText(goal.getPriorityText());
                
                // Set progress
                float progress = goal.getProgressPercentage();
                progressBar.setProgress((int) progress);
                tvProgressPercentage.setText(String.format("%.1f%%", progress));
                
                // Set color indicator
                try {
                    int color = Color.parseColor(goal.getColor());
                    colorIndicator.setBackgroundColor(color);
                    progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
                } catch (IllegalArgumentException e) {
                    colorIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
                    progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                }
                
                // Set status color
                if (goal.isCompleted()) {
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                } else if (goal.isOverdue()) {
                    tvStatus.setTextColor(Color.parseColor("#F44336"));
                } else {
                    tvStatus.setTextColor(Color.parseColor("#2196F3"));
                }
                
                // Set priority color
                switch (goal.getPriority()) {
                    case 5:
                    case 4:
                        tvPriority.setTextColor(Color.parseColor("#F44336")); // Red for high priority
                        break;
                    case 3:
                        tvPriority.setTextColor(Color.parseColor("#FF9800")); // Orange for medium
                        break;
                    default:
                        tvPriority.setTextColor(Color.parseColor("#4CAF50")); // Green for low priority
                        break;
                }
                
                // Handle clicks
                itemView.setOnClickListener(v -> {
                    // Show goal details
                    showGoalDetails(goal);
                });
            }
        }
    }
    
    private void showGoalDetails(Goal goal) {
        // Create a simple dialog showing goal details
        if (getActivity() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            builder.setTitle(goal.getTitle());
            
            StringBuilder details = new StringBuilder();
            details.append("Progress: ").append(goal.getFormattedProgress()).append("\n");
            details.append("Completion: ").append(String.format("%.1f%%", goal.getProgressPercentage())).append("\n");
            details.append("Status: ").append(goal.getStatusText()).append("\n");
            details.append("Streak: ").append(goal.getStreakCount()).append(" days\n");
            details.append("Total Completions: ").append(goal.getTotalCompletions()).append("\n");
            details.append("Time Remaining: ").append(goal.getFormattedRemainingTime()).append("\n");
            
            if (goal.getMotivationalMessage() != null) {
                details.append("\n").append(goal.getMotivationalMessage());
            }
            
            builder.setMessage(details.toString());
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }
}