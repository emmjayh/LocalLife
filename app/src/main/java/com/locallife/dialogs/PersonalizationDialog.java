package com.locallife.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.locallife.R;
import com.locallife.model.ActivityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for personalizing activity recommendations
 */
public class PersonalizationDialog extends Dialog {
    
    private PersonalizationListener listener;
    private List<CheckBox> activityCheckboxes;
    
    public interface PersonalizationListener {
        void onPersonalizationChanged(List<ActivityType> preferredActivities);
    }
    
    public PersonalizationDialog(@NonNull Context context, PersonalizationListener listener) {
        super(context);
        this.listener = listener;
        this.activityCheckboxes = new ArrayList<>();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_personalization);
        
        setupViews();
        setupClickListeners();
    }
    
    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDescription = findViewById(R.id.tvDescription);
        LinearLayout llActivityList = findViewById(R.id.llActivityList);
        
        tvTitle.setText("Personalize Recommendations");
        tvDescription.setText("Select the activities you enjoy most:");
        
        // Add checkboxes for each activity type
        for (ActivityType activityType : ActivityType.values()) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(activityType.getDisplayName());
            checkBox.setTextSize(14);
            checkBox.setPadding(16, 8, 16, 8);
            
            llActivityList.addView(checkBox);
            activityCheckboxes.add(checkBox);
        }
    }
    
    private void setupClickListeners() {
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);
        
        btnSave.setOnClickListener(v -> {
            List<ActivityType> preferredActivities = new ArrayList<>();
            
            for (int i = 0; i < activityCheckboxes.size(); i++) {
                CheckBox checkBox = activityCheckboxes.get(i);
                if (checkBox.isChecked()) {
                    preferredActivities.add(ActivityType.values()[i]);
                }
            }
            
            if (listener != null) {
                listener.onPersonalizationChanged(preferredActivities);
            }
            
            dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dismiss());
    }
}