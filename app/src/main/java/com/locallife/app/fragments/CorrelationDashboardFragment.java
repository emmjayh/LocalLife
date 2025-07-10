package com.locallife.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.locallife.app.R;
import com.locallife.app.adapters.CorrelationInsightAdapter;
import com.locallife.app.views.CorrelationScatterPlotView;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;
import com.locallife.service.CorrelationAnalysisService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying correlation analysis between weather and activity patterns
 */
public class CorrelationDashboardFragment extends Fragment {
    
    private CorrelationScatterPlotView scatterPlot;
    private Button btnAnalyze;
    private Button btnTempActivity;
    private Button btnHumidityActivity;
    private Button btnUVActivity;
    private Button btnAirQualityActivity;
    private TextView tvCorrelationSummary;
    private TextView tvInsightsTitle;
    private RecyclerView rvInsights;
    private ProgressBar progressBar;
    private CardView cardScatterPlot;
    private CardView cardInsights;
    private CardView cardControls;
    
    private DatabaseHelper databaseHelper;
    private CorrelationAnalysisService correlationService;
    private CorrelationInsightAdapter insightAdapter;
    
    private String selectedCorrelation = "temperature_activity";
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_correlation_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        databaseHelper = DatabaseHelper.getInstance(getContext());
        correlationService = new CorrelationAnalysisService(getContext());
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        runCorrelationAnalysis();
    }
    
    private void initializeViews(View view) {
        scatterPlot = view.findViewById(R.id.scatter_plot);
        btnAnalyze = view.findViewById(R.id.btn_analyze);
        btnTempActivity = view.findViewById(R.id.btn_temp_activity);
        btnHumidityActivity = view.findViewById(R.id.btn_humidity_activity);
        btnUVActivity = view.findViewById(R.id.btn_uv_activity);
        btnAirQualityActivity = view.findViewById(R.id.btn_air_quality_activity);
        tvCorrelationSummary = view.findViewById(R.id.tv_correlation_summary);
        tvInsightsTitle = view.findViewById(R.id.tv_insights_title);
        rvInsights = view.findViewById(R.id.rv_insights);
        progressBar = view.findViewById(R.id.progress_bar);
        cardScatterPlot = view.findViewById(R.id.card_scatter_plot);
        cardInsights = view.findViewById(R.id.card_insights);
        cardControls = view.findViewById(R.id.card_controls);
    }
    
    private void setupRecyclerView() {
        insightAdapter = new CorrelationInsightAdapter();
        rvInsights.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInsights.setAdapter(insightAdapter);
    }
    
    private void setupClickListeners() {
        btnAnalyze.setOnClickListener(v -> runCorrelationAnalysis());
        
        btnTempActivity.setOnClickListener(v -> {
            selectedCorrelation = "temperature_activity";
            updateCorrelationView();
            updateButtonSelection();
        });
        
        btnHumidityActivity.setOnClickListener(v -> {
            selectedCorrelation = "humidity_activity";
            updateCorrelationView();
            updateButtonSelection();
        });
        
        btnUVActivity.setOnClickListener(v -> {
            selectedCorrelation = "uv_activity";
            updateCorrelationView();
            updateButtonSelection();
        });
        
        btnAirQualityActivity.setOnClickListener(v -> {
            selectedCorrelation = "air_quality_activity";
            updateCorrelationView();
            updateButtonSelection();
        });
    }
    
    private void updateButtonSelection() {
        // Reset all buttons
        btnTempActivity.setSelected(false);
        btnHumidityActivity.setSelected(false);
        btnUVActivity.setSelected(false);
        btnAirQualityActivity.setSelected(false);
        
        // Set selected button
        switch (selectedCorrelation) {
            case "temperature_activity":
                btnTempActivity.setSelected(true);
                break;
            case "humidity_activity":
                btnHumidityActivity.setSelected(true);
                break;
            case "uv_activity":
                btnUVActivity.setSelected(true);
                break;
            case "air_quality_activity":
                btnAirQualityActivity.setSelected(true);
                break;
        }
    }
    
    private void runCorrelationAnalysis() {
        showLoading(true);
        
        correlationService.analyzeWeatherActivityCorrelation(
            new CorrelationAnalysisService.CorrelationCallback() {
                @Override
                public void onCorrelationResults(CorrelationAnalysisService.CorrelationResults results) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            displayResults(results);
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showError(error);
                        });
                    }
                }
            }
        );
    }
    
    private void displayResults(CorrelationAnalysisService.CorrelationResults results) {
        // Update insights
        insightAdapter.setInsights(results.insights);
        
        // Update scatter plot
        updateCorrelationView(results);
        
        // Update summary
        updateCorrelationSummary(results);
        
        // Update button selection
        updateButtonSelection();
    }
    
    private void updateCorrelationView() {
        // This will be called when user switches between different correlations
        // For now, we'll regenerate the analysis
        runCorrelationAnalysis();
    }
    
    private void updateCorrelationView(CorrelationAnalysisService.CorrelationResults results) {
        List<DayRecord> records = databaseHelper.getAllDayRecords();
        List<CorrelationScatterPlotView.DataPoint> dataPoints = new ArrayList<>();
        
        float correlation = 0f;
        String title = "";
        String xLabel = "";
        String yLabel = "Activity Score";
        
        // Generate data points based on selected correlation
        switch (selectedCorrelation) {
            case "temperature_activity":
                correlation = results.temperatureActivityCorr;
                title = "Temperature vs Activity";
                xLabel = "Temperature (°C)";
                for (DayRecord record : records) {
                    if (record.getTemperature() > 0 && record.getActivityScore() > 0) {
                        dataPoints.add(new CorrelationScatterPlotView.DataPoint(
                            record.getTemperature(), record.getActivityScore()));
                    }
                }
                break;
                
            case "humidity_activity":
                correlation = results.humidityActivityCorr;
                title = "Humidity vs Activity";
                xLabel = "Humidity (%)";
                for (DayRecord record : records) {
                    if (record.getHumidity() > 0 && record.getActivityScore() > 0) {
                        dataPoints.add(new CorrelationScatterPlotView.DataPoint(
                            record.getHumidity(), record.getActivityScore()));
                    }
                }
                break;
                
            case "uv_activity":
                correlation = results.uvActivityCorr;
                title = "UV Index vs Activity";
                xLabel = "UV Index";
                for (DayRecord record : records) {
                    if (record.getUvIndex() > 0 && record.getActivityScore() > 0) {
                        dataPoints.add(new CorrelationScatterPlotView.DataPoint(
                            (float) record.getUvIndex(), record.getActivityScore()));
                    }
                }
                break;
                
            case "air_quality_activity":
                correlation = results.airQualityActivityCorr;
                title = "Air Quality vs Activity";
                xLabel = "Air Quality Index";
                for (DayRecord record : records) {
                    if (record.getAirQualityIndex() > 0 && record.getActivityScore() > 0) {
                        dataPoints.add(new CorrelationScatterPlotView.DataPoint(
                            record.getAirQualityIndex(), record.getActivityScore()));
                    }
                }
                break;
        }
        
        scatterPlot.setData(dataPoints, correlation);
        scatterPlot.setLabels(title, xLabel, yLabel);
    }
    
    private void updateCorrelationSummary(CorrelationAnalysisService.CorrelationResults results) {
        StringBuilder summary = new StringBuilder();
        summary.append("Correlation Analysis Summary:\\n\\n");
        
        summary.append(String.format("• Temperature ↔ Activity: %.3f (%s)\\n", 
            results.temperatureActivityCorr, getStrengthText(results.temperatureActivityCorr)));
        
        summary.append(String.format("• Humidity ↔ Activity: %.3f (%s)\\n", 
            results.humidityActivityCorr, getStrengthText(results.humidityActivityCorr)));
        
        summary.append(String.format("• UV Index ↔ Activity: %.3f (%s)\\n", 
            results.uvActivityCorr, getStrengthText(results.uvActivityCorr)));
        
        summary.append(String.format("• Air Quality ↔ Activity: %.3f (%s)\\n", 
            results.airQualityActivityCorr, getStrengthText(results.airQualityActivityCorr)));
        
        summary.append(String.format("\\n• Weather ↔ Screen Time: %.3f (%s)\\n", 
            results.weatherScreenTimeCorr, getStrengthText(results.weatherScreenTimeCorr)));
        
        summary.append(String.format("• Weather ↔ Media: %.3f (%s)", 
            results.weatherMediaCorr, getStrengthText(results.weatherMediaCorr)));
        
        tvCorrelationSummary.setText(summary.toString());
    }
    
    private String getStrengthText(float correlation) {
        float abs = Math.abs(correlation);
        if (abs < 0.3f) return "Weak";
        if (abs < 0.5f) return "Moderate";
        if (abs < 0.7f) return "Strong";
        return "Very Strong";
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnAnalyze.setEnabled(!show);
    }
    
    private void showError(String error) {
        tvCorrelationSummary.setText("Error: " + error);
        insightAdapter.setInsights(new ArrayList<>());
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (correlationService != null) {
            correlationService.shutdown();
        }
    }
}