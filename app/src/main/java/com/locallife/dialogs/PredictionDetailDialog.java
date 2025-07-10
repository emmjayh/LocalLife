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

/**
 * Dialog for displaying detailed prediction information
 */
public class PredictionDetailDialog extends Dialog {
    
    private Date date;
    private List<PredictionResult> predictions;
    
    public PredictionDetailDialog(@NonNull Context context, Date date, List<PredictionResult> predictions) {
        super(context);
        this.date = date;
        this.predictions = predictions;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_prediction_detail);
        
        setupViews();
        setupClickListeners();
    }
    
    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvPredictionsContent = findViewById(R.id.tvPredictionsContent);
        
        // Set title and date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        tvTitle.setText("Predictions for " + dateFormat.format(date));
        tvDate.setText(dateFormat.format(date));
        
        // Set predictions content
        if (predictions != null && !predictions.isEmpty()) {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < predictions.size(); i++) {
                PredictionResult prediction = predictions.get(i);
                content.append(String.format("%d. %s (%.0f%% confidence)\n", 
                    i + 1, 
                    prediction.getPredictedActivity().getDisplayName(),
                    prediction.getConfidenceScore() * 100));
                
                if (prediction.getReasoning() != null) {
                    content.append("   ").append(prediction.getReasoning()).append("\n");
                }
                content.append("\n");
            }
            tvPredictionsContent.setText(content.toString().trim());
        } else {
            tvPredictionsContent.setText("No predictions available for this date.");
        }
    }
    
    private void setupClickListeners() {
        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());
    }
}