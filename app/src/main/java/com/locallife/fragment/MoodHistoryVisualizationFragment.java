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
import com.locallife.service.MoodWeatherCorrelationService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying comprehensive mood history visualizations
 */
public class MoodHistoryVisualizationFragment extends Fragment {
    private static final String TAG = "MoodHistoryVisualization";
    
    private MoodTrackingService moodTrackingService;
    private MoodVisualizationService visualizationService;
    private MoodWeatherCorrelationService correlationService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    // UI Components
    private ScrollView mainScrollView;
    private LinearLayout mainLayout;
    private Button timeRangeButton;
    private LinearLayout chartContainer;
    private LinearLayout correlationContainer;
    private LinearLayout patternContainer;
    
    // State
    private int currentTimeRange = 30; // Days
    private String[] timeRangeOptions = {"7 days", "2 weeks", "1 month", "3 months", "6 months", "1 year"};
    private int[] timeRangeValues = {7, 14, 30, 90, 180, 365};
    private int currentRangeIndex = 2; // Default to 1 month
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            moodTrackingService = new MoodTrackingService(getContext());
            visualizationService = new MoodVisualizationService(getContext());
            correlationService = new MoodWeatherCorrelationService(getContext());
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
        setupChartContainer();
        setupCorrelationContainer();
        setupPatternContainer();
        
        mainScrollView.addView(mainLayout);
        
        loadMoodHistory();
        
        return mainScrollView;
    }
    
    private void setupHeader() {
        TextView title = new TextView(getContext());
        title.setText("Mood History & Patterns");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 16);
        mainLayout.addView(title);
        
        timeRangeButton = new Button(getContext());
        updateTimeRangeButton();
        timeRangeButton.setOnClickListener(v -> cycleTimeRange());
        mainLayout.addView(timeRangeButton);
    }
    
    private void setupChartContainer() {
        TextView sectionTitle = new TextView(getContext());
        sectionTitle.setText("Mood Visualizations");
        sectionTitle.setTextSize(20);
        sectionTitle.setPadding(0, 24, 0, 16);
        mainLayout.addView(sectionTitle);
        
        chartContainer = new LinearLayout(getContext());
        chartContainer.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(chartContainer);
    }
    
    private void setupCorrelationContainer() {
        TextView sectionTitle = new TextView(getContext());
        sectionTitle.setText("Weather & Environmental Correlations");
        sectionTitle.setTextSize(20);
        sectionTitle.setPadding(0, 24, 0, 16);
        mainLayout.addView(sectionTitle);
        
        correlationContainer = new LinearLayout(getContext());
        correlationContainer.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(correlationContainer);
    }
    
    private void setupPatternContainer() {
        TextView sectionTitle = new TextView(getContext());
        sectionTitle.setText("Mood Patterns & Insights");
        sectionTitle.setTextSize(20);
        sectionTitle.setPadding(0, 24, 0, 16);
        mainLayout.addView(sectionTitle);
        
        patternContainer = new LinearLayout(getContext());
        patternContainer.setOrientation(LinearLayout.VERTICAL);
        mainLayout.addView(patternContainer);
    }
    
    private void loadMoodHistory() {
        // Clear existing content
        chartContainer.removeAllViews();
        correlationContainer.removeAllViews();
        patternContainer.removeAllViews();
        
        // Load mood statistics
        moodTrackingService.getMoodStatistics(new MoodTrackingService.MoodStatsCallback() {
            @Override
            public void onStatsReceived(MoodTrackingService.MoodStatistics statistics) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (statistics.totalEntries > 0) {
                            createMoodCharts();
                            loadCorrelationAnalysis();
                            loadPatternAnalysis();
                        } else {
                            showNoDataMessage();
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading mood statistics: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> showErrorMessage(error));
                }
            }
        });
    }
    
    private void createMoodCharts() {
        // Mood Trend Line Chart
        createMoodTrendChart();
        
        // Mood Distribution Chart
        createMoodDistributionChart();
        
        // Weekly Pattern Chart
        createWeeklyPatternChart();
        
        // Mood Heatmap Calendar
        createMoodHeatmapChart();
        
        // Energy vs Stress Scatter Plot
        createEnergyStressChart();
    }
    
    private void createMoodTrendChart() {
        LinearLayout chartView = createChartContainer("📈 Mood Trend Over Time");
        
        MoodVisualizationService.MoodLineChartData lineData = 
            visualizationService.createMoodLineChart(currentTimeRange);
        
        TextView chartContent = new TextView(getContext());
        chartContent.setTypeface(android.graphics.Typeface.MONOSPACE);
        chartContent.setPadding(16, 16, 16, 16);
        
        StringBuilder trendText = new StringBuilder();
        trendText.append("Date Range: Last ").append(currentTimeRange).append(" days\n");
        trendText.append("Average Mood: ").append(String.format("%.1f/9", lineData.getAverageMood())).append("\n");
        trendText.append("Range: ").append(String.format("%.1f - %.1f", lineData.getMinMood(), lineData.getMaxMood())).append("\n\n");
        
        // Create text-based trend visualization
        List<String> labels = lineData.getLabels();
        List<Float> values = lineData.getMoodValues();
        
        for (int i = Math.max(0, labels.size() - 14); i < labels.size(); i++) {
            String label = labels.get(i);
            Float value = values.get(i);
            
            trendText.append(label).append(": ");
            if (value != null) {
                trendText.append(createMoodBar(value, 9));
                trendText.append(" ").append(String.format("%.1f", value));
                trendText.append(" ").append(getMoodEmoji(value));
            } else {
                trendText.append("No data");
            }
            trendText.append("\n");
        }
        
        chartContent.setText(trendText.toString());
        chartView.addView(chartContent);
        chartContainer.addView(chartView);
    }
    
    private void createMoodDistributionChart() {
        LinearLayout chartView = createChartContainer("🥧 Mood Distribution");
        
        MoodVisualizationService.MoodDistributionChartData distributionData = 
            visualizationService.createMoodDistributionChart(currentTimeRange);
        
        TextView chartContent = new TextView(getContext());
        chartContent.setPadding(16, 16, 16, 16);
        
        StringBuilder distText = new StringBuilder();
        distText.append("Total Entries: ").append(distributionData.getTotalEntries()).append("\n\n");
        
        List<String> labels = distributionData.getLabels();
        List<Integer> values = distributionData.getValues();
        
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            Integer count = values.get(i);
            float percentage = (float) count / distributionData.getTotalEntries() * 100;
            
            distText.append(label).append(": ").append(count)
                   .append(" (").append(String.format("%.1f%%", percentage)).append(")\n");
            
            // Create visual bar
            int barLength = (int) (percentage / 5); // Scale for display
            for (int j = 0; j < barLength; j++) {
                distText.append("█");
            }
            distText.append("\n\n");
        }
        
        chartContent.setText(distText.toString());
        chartView.addView(chartContent);
        chartContainer.addView(chartView);
    }
    
    private void createWeeklyPatternChart() {
        LinearLayout chartView = createChartContainer("📅 Weekly Mood Pattern");
        
        MoodVisualizationService.WeeklyMoodChartData weeklyData = 
            visualizationService.createWeeklyMoodChart(4);
        
        TextView chartContent = new TextView(getContext());
        chartContent.setTypeface(android.graphics.Typeface.MONOSPACE);
        chartContent.setPadding(16, 16, 16, 16);
        
        StringBuilder weeklyText = new StringBuilder();
        List<String> dayLabels = weeklyData.getDayLabels();
        List<Float> moodValues = weeklyData.getMoodValues();
        
        for (int i = 0; i < dayLabels.size(); i++) {
            String day = dayLabels.get(i);
            Float mood = moodValues.get(i);
            
            weeklyText.append(String.format("%-3s: ", day));
            if (mood != null && mood > 0) {
                weeklyText.append(createMoodBar(mood, 9));
                weeklyText.append(" ").append(String.format("%.1f", mood));
                weeklyText.append(" ").append(getMoodEmoji(mood));
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
        
        chartContent.setText(weeklyText.toString());
        chartView.addView(chartContent);
        chartContainer.addView(chartView);
    }
    
    private void createMoodHeatmapChart() {
        LinearLayout chartView = createChartContainer("🗓️ Mood Calendar Heatmap");
        
        MoodVisualizationService.MoodHeatmapData heatmapData = 
            visualizationService.createMoodHeatmap(Math.min(currentTimeRange, 28));
        
        TextView chartContent = new TextView(getContext());
        chartContent.setTypeface(android.graphics.Typeface.MONOSPACE);
        chartContent.setPadding(16, 16, 16, 16);
        
        StringBuilder heatmapText = new StringBuilder();
        heatmapText.append("Recent Mood Calendar:\n\n");
        
        List<String> dateLabels = heatmapData.getDateLabels();
        List<Float> intensities = heatmapData.getMoodIntensities();
        
        // Group by weeks for better visualization
        int daysToShow = Math.min(28, dateLabels.size());
        for (int i = dateLabels.size() - daysToShow; i < dateLabels.size(); i++) {
            String date = dateLabels.get(i);
            Float intensity = intensities.get(i);
            
            String[] dateParts = date.split("-");
            String shortDate = dateParts[2] + "/" + dateParts[1];
            
            heatmapText.append(String.format("%-5s: ", shortDate));
            if (intensity != null && intensity > 0) {
                float moodValue = intensity * 10;
                heatmapText.append(getMoodEmoji(moodValue));
                heatmapText.append(" ").append(String.format("%.1f", moodValue));
            } else {
                heatmapText.append("⚪ No data");
            }
            heatmapText.append("\n");
            
            // Add week separator
            if ((i + 1) % 7 == 0) {
                heatmapText.append("\n");
            }
        }
        
        chartContent.setText(heatmapText.toString());
        chartView.addView(chartContent);
        chartContainer.addView(chartView);
    }
    
    private void createEnergyStressChart() {
        LinearLayout chartView = createChartContainer("⚡ Energy vs Stress Analysis");
        
        TextView chartContent = new TextView(getContext());
        chartContent.setPadding(16, 16, 16, 16);
        
        // Get recent mood entries for analysis
        moodTrackingService.getRecentMoodEntries(new MoodTrackingService.MoodListCallback() {
            @Override
            public void onMoodEntriesReceived(List<MoodEntry> moodEntries) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        StringBuilder analysisText = new StringBuilder();
                        
                        if (!moodEntries.isEmpty()) {
                            float avgEnergy = 0, avgStress = 0, avgMood = 0;
                            int count = 0;
                            
                            for (MoodEntry entry : moodEntries) {
                                avgEnergy += entry.getEnergyLevel();
                                avgStress += entry.getStressLevel();
                                avgMood += entry.getMoodScore();
                                count++;
                            }
                            
                            avgEnergy /= count;
                            avgStress /= count;
                            avgMood /= count;
                            
                            analysisText.append("Energy-Stress-Mood Analysis:\n\n");
                            analysisText.append("Average Energy: ").append(String.format("%.1f/10", avgEnergy)).append("\n");
                            analysisText.append("Average Stress: ").append(String.format("%.1f/10", avgStress)).append("\n");
                            analysisText.append("Average Mood: ").append(String.format("%.1f/9", avgMood)).append("\n\n");
                            
                            // Energy vs Stress correlation
                            if (avgEnergy > 7 && avgStress < 4) {
                                analysisText.append("✅ Great energy-stress balance\n");
                            } else if (avgEnergy < 4 && avgStress > 7) {
                                analysisText.append("⚠️ Low energy, high stress - consider relaxation\n");
                            } else if (avgEnergy > 6 && avgStress > 7) {
                                analysisText.append("🔄 High energy but stressed - channel energy positively\n");
                            } else {
                                analysisText.append("📊 Balanced energy-stress levels\n");
                            }
                            
                            // Show recent patterns
                            analysisText.append("\nRecent Energy Levels:\n");
                            for (int i = Math.max(0, moodEntries.size() - 7); i < moodEntries.size(); i++) {
                                MoodEntry entry = moodEntries.get(i);
                                analysisText.append(entry.getDate()).append(": ");
                                analysisText.append("Energy ").append(entry.getEnergyLevel());
                                analysisText.append(" | Stress ").append(entry.getStressLevel()).append("\n");
                            }
                        } else {
                            analysisText.append("No data available for energy-stress analysis");
                        }
                        
                        chartContent.setText(analysisText.toString());
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading mood entries for energy analysis: " + error);
            }
        });
        
        chartView.addView(chartContent);
        chartContainer.addView(chartView);
    }
    
    private void loadCorrelationAnalysis() {
        // Weather-Mood Correlation
        correlationService.analyzeMoodWeatherCorrelation(currentTimeRange, 
            new MoodWeatherCorrelationService.CorrelationCallback() {
                @Override
                public void onCorrelationAnalyzed(MoodWeatherCorrelationService.WeatherMoodCorrelation correlation) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> createWeatherCorrelationView(correlation));
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error analyzing weather correlation: " + error);
                }
            });
        
        // Seasonal Pattern Analysis
        correlationService.analyzeSeasonalMoodPatterns(
            new MoodWeatherCorrelationService.SeasonalAnalysisCallback() {
                @Override
                public void onSeasonalAnalysisComplete(MoodWeatherCorrelationService.SeasonalMoodAnalysis analysis) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> createSeasonalAnalysisView(analysis));
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error analyzing seasonal patterns: " + error);
                }
            });
    }
    
    private void createWeatherCorrelationView(MoodWeatherCorrelationService.WeatherMoodCorrelation correlation) {
        LinearLayout correlationView = createChartContainer("🌤️ Weather-Mood Correlation");
        
        TextView correlationContent = new TextView(getContext());
        correlationContent.setPadding(16, 16, 16, 16);
        
        StringBuilder corrText = new StringBuilder();
        corrText.append("Data Points: ").append(correlation.totalDataPoints).append("\n");
        corrText.append("Correlation Strength: ").append(String.format("%.2f", correlation.correlationStrength)).append("\n\n");
        
        if (correlation.bestWeatherCondition != null) {
            corrText.append("🌟 Best Weather for Mood: ").append(correlation.bestWeatherCondition).append("\n");
        }
        
        if (correlation.worstWeatherCondition != null) {
            corrText.append("😔 Challenging Weather: ").append(correlation.worstWeatherCondition).append("\n");
        }
        
        if (correlation.optimalTemperatureRange != null) {
            corrText.append("🌡️ Optimal Temperature: ").append(correlation.optimalTemperatureRange).append("\n");
        }
        
        corrText.append("\nWeather Impact Analysis:\n");
        for (String weather : correlation.weatherMoodAverages.keySet()) {
            Float avgMood = correlation.weatherMoodAverages.get(weather);
            if (avgMood != null) {
                corrText.append(weather).append(": ").append(String.format("%.1f/9", avgMood)).append("\n");
            }
        }
        
        correlationContent.setText(corrText.toString());
        correlationView.addView(correlationContent);
        correlationContainer.addView(correlationView);
    }
    
    private void createSeasonalAnalysisView(MoodWeatherCorrelationService.SeasonalMoodAnalysis analysis) {
        LinearLayout seasonalView = createChartContainer("🍂 Seasonal Mood Patterns");
        
        TextView seasonalContent = new TextView(getContext());
        seasonalContent.setPadding(16, 16, 16, 16);
        
        StringBuilder seasonText = new StringBuilder();
        seasonText.append("Seasonal Mood Analysis:\n\n");
        seasonText.append("🌸 Spring: ").append(String.format("%.1f/9", analysis.springAverage)).append("\n");
        seasonText.append("☀️ Summer: ").append(String.format("%.1f/9", analysis.summerAverage)).append("\n");
        seasonText.append("🍂 Fall: ").append(String.format("%.1f/9", analysis.fallAverage)).append("\n");
        seasonText.append("❄️ Winter: ").append(String.format("%.1f/9", analysis.winterAverage)).append("\n");
        
        seasonalContent.setText(seasonText.toString());
        seasonalView.addView(seasonalContent);
        correlationContainer.addView(seasonalView);
    }
    
    private void loadPatternAnalysis() {
        // Get mood insights
        moodTrackingService.getMoodInsights(new MoodTrackingService.MoodInsightsCallback() {
            @Override
            public void onInsightsReceived(List<String> insights) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> createInsightsView(insights));
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading insights: " + error);
            }
        });
    }
    
    private void createInsightsView(List<String> insights) {
        LinearLayout insightsView = createChartContainer("🧠 Mood Insights & Patterns");
        
        TextView insightsContent = new TextView(getContext());
        insightsContent.setPadding(16, 16, 16, 16);
        
        StringBuilder insightsText = new StringBuilder();
        if (insights.isEmpty()) {
            insightsText.append("Keep tracking your mood to discover personalized insights!");
        } else {
            for (int i = 0; i < insights.size(); i++) {
                insightsText.append("💡 ").append(insights.get(i)).append("\n");
                if (i < insights.size() - 1) {
                    insightsText.append("\n");
                }
            }
        }
        
        insightsContent.setText(insightsText.toString());
        insightsView.addView(insightsContent);
        patternContainer.addView(insightsView);
    }
    
    private LinearLayout createChartContainer(String title) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(16, 16, 16, 16);
        container.setBackgroundColor(Color.parseColor("#F9F9F9"));
        
        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setPadding(0, 0, 0, 16);
        container.addView(titleView);
        
        // Add margin between charts
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        container.setLayoutParams(params);
        
        return container;
    }
    
    private void cycleTimeRange() {
        currentRangeIndex = (currentRangeIndex + 1) % timeRangeOptions.length;
        currentTimeRange = timeRangeValues[currentRangeIndex];
        updateTimeRangeButton();
        loadMoodHistory();
    }
    
    private void updateTimeRangeButton() {
        timeRangeButton.setText("Time Range: " + timeRangeOptions[currentRangeIndex]);
    }
    
    private void showNoDataMessage() {
        TextView noDataText = new TextView(getContext());
        noDataText.setText("No mood data available for visualization.\nStart tracking your mood to see beautiful charts and insights!");
        noDataText.setPadding(32, 32, 32, 32);
        noDataText.setTextSize(16);
        chartContainer.addView(noDataText);
    }
    
    private void showErrorMessage(String error) {
        TextView errorText = new TextView(getContext());
        errorText.setText("Error loading mood data: " + error);
        errorText.setPadding(32, 32, 32, 32);
        errorText.setTextColor(Color.RED);
        chartContainer.addView(errorText);
    }
    
    private String createMoodBar(float value, int maxValue) {
        int bars = Math.round((value / maxValue) * 10);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
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
        if (value <= 3) return "😢";
        if (value <= 4) return "😞";
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
        if (correlationService != null) {
            correlationService.shutdown();
        }
    }
}