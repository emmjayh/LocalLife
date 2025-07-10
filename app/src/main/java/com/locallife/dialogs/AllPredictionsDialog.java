package com.locallife.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.locallife.R;
import com.locallife.model.PredictionResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dialog for displaying all weekly predictions
 */
public class AllPredictionsDialog extends Dialog {
    
    private Map<Date, List<PredictionResult>> weeklyPredictions;
    
    public AllPredictionsDialog(@NonNull Context context, Map<Date, List<PredictionResult>> weeklyPredictions) {
        super(context);
        this.weeklyPredictions = weeklyPredictions;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_all_predictions);
        
        setupViews();
        setupClickListeners();
    }
    
    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvPredictionsContent = findViewById(R.id.tvPredictionsContent);
        
        tvTitle.setText("Weekly Predictions");
        
        if (weeklyPredictions != null && !weeklyPredictions.isEmpty()) {
            StringBuilder content = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            
            for (Map.Entry<Date, List<PredictionResult>> entry : weeklyPredictions.entrySet()) {
                content.append(dateFormat.format(entry.getKey())).append("\n");
                content.append("────────────────────\n");
                
                List<PredictionResult> predictions = entry.getValue();
                for (PredictionResult prediction : predictions) {
                    content.append(String.format("• %s (%.0f%% confidence)\n", 
                        prediction.getPredictedActivity().getDisplayName(),
                        prediction.getConfidenceScore() * 100));
                }
                content.append("\n");
            }
            
            tvPredictionsContent.setText(content.toString().trim());
        } else {
            tvPredictionsContent.setText("No predictions available.");
        }
    }
    
    private void setupClickListeners() {
        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());
    }
}