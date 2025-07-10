package com.locallife.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.locallife.model.MoodEntry;
import com.locallife.service.MoodTrackingService;
import com.locallife.service.MoodVisualizationService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying comprehensive mood analytics and insights
 */
public class MoodAnalyticsDashboardFragment extends Fragment {
    private static final String TAG = "MoodAnalyticsDashboard";
    
    private MoodTrackingService moodTrackingService;
    private MoodVisualizationService visualizationService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    
    // UI Components
    private ScrollView mainScrollView;
    private LinearLayout mainLayout;
    private TextView summaryStatsLayout;
    private LinearLayout chartsLayout;
    private LinearLayout insightsLayout;
    private Button timeRangeButton;
    
    // State
    private int currentTimeRange = 30; // Days
    private MoodTrackingService.MoodStatistics currentStats;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            moodTrackingService = new MoodTrackingService(getContext());
            visualizationService = new MoodVisualizationService(getContext());
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Create main layout programmatically
        mainScrollView = new ScrollView(getContext());
        mainLayout = new LinearLayout(getContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(24, 24, 24, 24);
        
        setupHeader();
        setupSummaryStats();
        setupCharts();
        setupInsights();
        
        mainScrollView.addView(mainLayout);
        
        loadMoodAnalytics();
        
        return mainScrollView;
    }
    
    private void setupHeader() {
        TextView title = new TextView(getContext());
        title.setText("Mood Analytics Dashboard");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 16);
        mainLayout.addView(title);
        
        timeRangeButton = new Button(getContext());
        timeRangeButton.setText("Last " + currentTimeRange + " days");
        timeRangeButton.setOnClickListener(v -> showTimeRangeSelector());
        mainLayout.addView(timeRangeButton);
    }
    
    private void setupSummaryStats() {
        TextView summaryTitle = new TextView(getContext());
        summaryTitle.setText("Summary Statistics");
        summaryTitle.setTextSize(20);
        summaryTitle.setPadding(0, 24, 0, 16);
        mainLayout.addView(summaryTitle);
        
        summaryStatsLayout = new TextView(getContext());
        summaryStatsLayout.setPadding(16, 16, 16, 16);
        summaryStatsLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        mainLayout.addView(summaryStatsLayout);
    }
    
    private void setupCharts() {
        TextView chartsTitle = new TextView(getContext());
        chartsTitle.setText("Mood Visualizations");
        chartsTitle.setTextSize(20);
        chartsTitle.setPadding(0, 24, 0, 16);
        mainLayout.addView(chartsTitle);
        
        chartsLayout = new LinearLayout(getContext());
        chartsLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(chartsLayout);
    }
    
    private void setupInsights() {
        TextView insightsTitle = new TextView(getContext());
        insightsTitle.setText("Mood Insights");
        insightsTitle.setTextSize(20);
        insightsTitle.setPadding(0, 24, 0, 16);
        mainLayout.addView(insightsTitle);
        
        insightsLayout = new LinearLayout(getContext());
        insightsLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(insightsLayout);
    }
    
    private void loadMoodAnalytics() {
        // Load mood statistics
        moodTrackingService.getMoodStatistics(new MoodTrackingService.MoodStatsCallback() {
            @Override
            public void onStatsReceived(MoodTrackingService.MoodStatistics statistics) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentStats = statistics;
                        updateSummaryStats(statistics);
                        updateCharts();
                        loadInsights();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading mood statistics: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        summaryStatsLayout.setText("Error loading mood data: " + error);
                    });
                }
            }
        });
    }
    
    private void updateSummaryStats(MoodTrackingService.MoodStatistics stats) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("📊 Total Entries: ").append(stats.totalEntries).append("\n\n");
        
        if (stats.totalEntries > 0) {
            summary.append("😊 Positive Days: ").append(stats.positiveEntries)
                   .append(" (").append(String.format("%.1f%%", stats.positivePercentage)).append(")\n");
            
            summary.append("😐 Neutral Days: ").append(stats.neutralEntries)
                   .append(" (").append(String.format("%.1f%%", stats.neutralPercentage)).append(")\n");
            
            summary.append("😔 Negative Days: ").append(stats.negativeEntries)
                   .append(" (").append(String.format("%.1f%%", stats.negativePercentage)).append(")\n\n");
            
            summary.append("📈 Average Mood: ").append(String.format("%.1f/9", stats.averageMoodScore)).append("\n");
            summary.append("⚡ Average Energy: ").append(String.format("%.1f/10", stats.averageEnergyLevel)).append("\n");
            summary.append("😰 Average Stress: ").append(String.format("%.1f/10", stats.averageStressLevel)).append("\n");
            summary.append("👥 Average Social: ").append(String.format("%.1f/10", stats.averageSocialLevel)).append("\n\n");
            
            summary.append("📊 Trend: ").append(stats.moodTrend).append("\n");
            
            if (stats.mostCommonWeather != null) {
                summary.append("🌤️ Most Common Weather: ").append(stats.mostCommonWeather).append("\n");
            }
            
            if (stats.bestMoodEntry != null) {
                summary.append("\n🌟 Best Day: ").append(stats.bestMoodEntry.getFormattedMood())
                       .append(" on ").append(stats.bestMoodEntry.getDate());
            }
        } else {
            summary.append("No mood entries found for this time period.\n");
            summary.append("Start tracking your mood to see analytics!");
        }
        
        summaryStatsLayout.setText(summary.toString());
    }
    
    private void updateCharts() {
        chartsLayout.removeAllViews();
        
        if (currentStats == null || currentStats.totalEntries == 0) {
            TextView noDataText = new TextView(getContext());
            noDataText.setText("No data available for charts");
            noDataText.setPadding(16, 16, 16, 16);
            chartsLayout.addView(noDataText);
            return;
        }
        
        // Mood Trend Chart
        createMoodTrendChart();
        
        // Mood Distribution Chart
        createMoodDistributionChart();
        
        // Weekly Pattern Chart
        createWeeklyPatternChart();
        
        // Mood Heatmap
        createMoodHeatmap();
    }
    
    private void createMoodTrendChart() {
        LinearLayout chartContainer = new LinearLayout(getContext());
        chartContainer.setOrientation(LinearLayout.VERTICAL);
        chartContainer.setPadding(16, 16, 16, 16);
        chartContainer.setBackgroundColor(Color.parseColor("#F9F9F9"));
        
        TextView chartTitle = new TextView(getContext());
        chartTitle.setText("📈 Mood Trend Over Time");
        chartTitle.setTextSize(16);
        chartTitle.setPadding(0, 0, 0, 16);
        chartContainer.addView(chartTitle);
        
        // Create simple text-based chart
        MoodVisualizationService.MoodLineChartData lineData = 
            visualizationService.createMoodLineChart(currentTimeRange);
        
        StringBuilder chartText = new StringBuilder();
        List<String> labels = lineData.getLabels();
        List<Float> values = lineData.getMoodValues();
        
        for (int i = 0; i < Math.min(labels.size(), 10); i++) {
            String label = labels.get(i);
            Float value = values.get(i);
            
            chartText.append(label).append(": ");
            if (value != null) {
                chartText.append(getMoodBar(value));
                chartText.append(" ").append(String.format("%.1f", value));
            } else {
                chartText.append("No data");
            }
            chartText.append("\n");
        }
        
        TextView chartContent = new TextView(getContext());
        chartContent.setText(chartText.toString());
        chartContent.setTypeface(android.graphics.Typeface.MONOSPACE);
        chartContainer.addView(chartContent);
        
        chartsLayout.addView(chartContainer);
    }
    
    private void createMoodDistributionChart() {
        LinearLayout chartContainer = new LinearLayout(getContext());
        chartContainer.setOrientation(LinearLayout.VERTICAL);
        chartContainer.setPadding(16, 16, 16, 16);
        chartContainer.setBackgroundColor(Color.parseColor("#F9F9F9"));
        chartContainer.setPadding(0, 16, 0, 0);
        
        TextView chartTitle = new TextView(getContext());
        chartTitle.setText("🥧 Mood Distribution");
        chartTitle.setTextSize(16);
        chartTitle.setPadding(0, 0, 0, 16);
        chartContainer.addView(chartTitle);
        
        StringBuilder distText = new StringBuilder();
        distText.append("😊 Positive: ").append(currentStats.positiveEntries)
                .append(" days (").append(String.format("%.1f%%", currentStats.positivePercentage)).append(")\n");
        distText.append("😐 Neutral: ").append(currentStats.neutralEntries)
                .append(" days (").append(String.format("%.1f%%", currentStats.neutralPercentage)).append(")\n");
        distText.append("😔 Negative: ").append(currentStats.negativeEntries)
                .append(" days (").append(String.format("%.1f%%", currentStats.negativePercentage)).append(")\n");
        
        TextView distributionContent = new TextView(getContext());
        distributionContent.setText(distText.toString());
        chartContainer.addView(distributionContent);
        
        chartsLayout.addView(chartContainer);
    }
    
    private void createWeeklyPatternChart() {
        LinearLayout chartContainer = new LinearLayout(getContext());
        chartContainer.setOrientation(LinearLayout.VERTICAL);
        chartContainer.setPadding(16, 16, 16, 16);
        chartContainer.setBackgroundColor(Color.parseColor("#F9F9F9"));
        chartContainer.setPadding(0, 16, 0, 0);
        
        TextView chartTitle = new TextView(getContext());
        chartTitle.setText("📅 Weekly Mood Pattern");
        chartTitle.setTextSize(16);
        chartTitle.setPadding(0, 0, 0, 16);
        chartContainer.addView(chartTitle);
        
        MoodVisualizationService.WeeklyMoodChartData weeklyData = 
            visualizationService.createWeeklyMoodChart(4);
        
        StringBuilder weeklyText = new StringBuilder();
        List<String> dayLabels = weeklyData.getDayLabels();
        List<Float> moodValues = weeklyData.getMoodValues();
        
        for (int i = 0; i < dayLabels.size(); i++) {
            String day = dayLabels.get(i);
            Float mood = moodValues.get(i);
            
            weeklyText.append(day).append(": ");
            if (mood != null && mood > 0) {
                weeklyText.append(getMoodBar(mood));
                weeklyText.append(" ").append(String.format("%.1f", mood));
            } else {
                weeklyText.append("No data");
            }
            weeklyText.append("\n");
        }
        
        if (weeklyData.getBestDay() != null) {
            weeklyText.append("\n🌟 Best Day: ").append(weeklyData.getBestDay());
        }
        if (weeklyData.getWorstDay() != null) {
            weeklyText.append("\n😔 Challenging Day: ").append(weeklyData.getWorstDay());
        }
        
        TextView weeklyContent = new TextView(getContext());
        weeklyContent.setText(weeklyText.toString());
        weeklyContent.setTypeface(android.graphics.Typeface.MONOSPACE);
        chartContainer.addView(weeklyContent);
        
        chartsLayout.addView(chartContainer);
    }
    
    private void createMoodHeatmap() {
        LinearLayout chartContainer = new LinearLayout(getContext());
        chartContainer.setOrientation(LinearLayout.VERTICAL);
        chartContainer.setPadding(16, 16, 16, 16);
        chartContainer.setBackgroundColor(Color.parseColor("#F9F9F9"));
        chartContainer.setPadding(0, 16, 0, 0);
        
        TextView chartTitle = new TextView(getContext());
        chartTitle.setText("🗓️ Mood Calendar (Last 14 Days)");
        chartTitle.setTextSize(16);
        chartTitle.setPadding(0, 0, 0, 16);
        chartContainer.addView(chartTitle);
        
        MoodVisualizationService.MoodHeatmapData heatmapData = 
            visualizationService.createMoodHeatmap(14);
        
        StringBuilder heatmapText = new StringBuilder();
        List<String> dateLabels = heatmapData.getDateLabels();
        List<Float> intensities = heatmapData.getMoodIntensities();
        
        for (int i = Math.max(0, dateLabels.size() - 14); i < dateLabels.size(); i++) {
            String date = dateLabels.get(i);
            Float intensity = intensities.get(i);
            
            String[] dateParts = date.split("-");
            String shortDate = dateParts[1] + "/" + dateParts[2];
            
            heatmapText.append(shortDate).append(": ");
            if (intensity != null && intensity > 0) {
                float moodValue = intensity * 10; // Convert back to 1-10 scale
                heatmapText.append(getMoodEmoji(moodValue));
                heatmapText.append(" ").append(String.format("%.1f", moodValue));
            } else {
                heatmapText.append("⚪ No data");
            }
            heatmapText.append("\n");
        }
        
        TextView heatmapContent = new TextView(getContext());
        heatmapContent.setText(heatmapText.toString());
        heatmapContent.setTypeface(android.graphics.Typeface.MONOSPACE);
        chartContainer.addView(heatmapContent);
        
        chartsLayout.addView(chartContainer);
    }
    
    private void loadInsights() {
        moodTrackingService.getMoodInsights(new MoodTrackingService.MoodInsightsCallback() {
            @Override
            public void onInsightsReceived(List<String> insights) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateInsights(insights));
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading insights: " + error);
            }
        });
    }
    
    private void updateInsights(List<String> insights) {
        insightsLayout.removeAllViews();
        
        if (insights.isEmpty()) {
            TextView noInsights = new TextView(getContext());
            noInsights.setText("No insights available yet. Keep tracking your mood!");
            noInsights.setPadding(16, 16, 16, 16);
            insightsLayout.addView(noInsights);
            return;
        }
        
        for (String insight : insights) {
            LinearLayout insightContainer = new LinearLayout(getContext());
            insightContainer.setOrientation(LinearLayout.HORIZONTAL);
            insightContainer.setPadding(16, 8, 16, 8);
            insightContainer.setBackgroundColor(Color.parseColor("#E8F5E8"));
            
            TextView bulletPoint = new TextView(getContext());
            bulletPoint.setText("💡 ");
            bulletPoint.setTextSize(16);
            insightContainer.addView(bulletPoint);
            
            TextView insightText = new TextView(getContext());
            insightText.setText(insight);
            insightText.setTextSize(14);
            insightContainer.addView(insightText);
            
            insightsLayout.addView(insightContainer);
            
            // Add spacing between insights
            View spacer = new View(getContext());
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 8));
            insightsLayout.addView(spacer);
        }
    }
    
    private void showTimeRangeSelector() {
        // Simple time range selector
        int[] ranges = {7, 14, 30, 60, 90};
        String[] rangeLabels = {"7 days", "2 weeks", "1 month", "2 months", "3 months"};
        
        // For simplicity, cycle through ranges
        int currentIndex = -1;
        for (int i = 0; i < ranges.length; i++) {
            if (ranges[i] == currentTimeRange) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % ranges.length;
        currentTimeRange = ranges[nextIndex];
        timeRangeButton.setText("Last " + rangeLabels[nextIndex]);
        
        // Reload data with new time range
        loadMoodAnalytics();
    }
    
    private String getMoodBar(float value) {
        int bars = Math.round(value);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            if (i < bars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }
    
    private String getMoodEmoji(float value) {
        if (value <= 2) return "😭";
        if (value <= 3) return "😞";
        if (value <= 4) return "🙁";
        if (value <= 5) return "😐";
        if (value <= 6) return "🙂";
        if (value <= 7) return "😊";
        if (value <= 8) return "😄";
        return "🤩";
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (moodTrackingService != null) {
            moodTrackingService.shutdown();
        }
        if (visualizationService != null) {
            visualizationService.close();
        }
    }
}