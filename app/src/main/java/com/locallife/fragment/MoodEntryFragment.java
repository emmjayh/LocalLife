package com.locallife.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.locallife.model.MoodEntry;
import com.locallife.service.MoodTrackingService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment for mood entry with emoji selection and detailed tracking
 */
public class MoodEntryFragment extends Fragment {
    private static final String TAG = "MoodEntryFragment";
    
    private MoodTrackingService moodTrackingService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // UI Components
    private LinearLayout moodSelectionLayout;
    private TextView selectedMoodText;
    private EditText notesEditText;
    private EditText triggersEditText;
    private EditText activitiesEditText;
    private SeekBar energySeekBar;
    private SeekBar stressSeekBar;
    private SeekBar socialSeekBar;
    private TextView energyLevelText;
    private TextView stressLevelText;
    private TextView socialLevelText;
    private EditText sleepHoursEditText;
    private Button exerciseButton;
    private EditText medicationsEditText;
    private Button saveMoodButton;
    
    // State
    private MoodEntry.MoodLevel selectedMoodLevel;
    private String selectedMoodEmoji;
    private boolean hasExercised = false;
    private MoodEntry existingEntry;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            moodTrackingService = new MoodTrackingService(getContext());
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(android.R.layout.activity_list_item, container, false);
        
        // Since we don't have access to custom layouts, we'll create views programmatically
        LinearLayout mainLayout = new LinearLayout(getContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);
        
        setupMoodSelection(mainLayout);
        setupDetailsSection(mainLayout);
        setupLevelsSection(mainLayout);
        setupContextSection(mainLayout);
        setupSaveButton(mainLayout);
        
        if (container != null) {
            container.removeAllViews();
            container.addView(mainLayout);
        }
        
        loadExistingMoodForToday();
        
        return container;
    }
    
    private void setupMoodSelection(LinearLayout parent) {
        TextView title = new TextView(getContext());
        title.setText("How are you feeling today?");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 24);
        parent.addView(title);
        
        moodSelectionLayout = new LinearLayout(getContext());
        moodSelectionLayout.setOrientation(LinearLayout.VERTICAL);
        parent.addView(moodSelectionLayout);
        
        // Create mood level buttons
        for (MoodEntry.MoodLevel level : MoodEntry.MoodLevel.values()) {
            Button moodButton = new Button(getContext());
            moodButton.setText(level.getEmoji() + " " + level.getDisplayName());
            moodButton.setOnClickListener(v -> selectMood(level));
            moodSelectionLayout.addView(moodButton);
        }
        
        selectedMoodText = new TextView(getContext());
        selectedMoodText.setText("No mood selected");
        selectedMoodText.setTextSize(16);
        selectedMoodText.setPadding(0, 16, 0, 24);
        parent.addView(selectedMoodText);
    }
    
    private void setupDetailsSection(LinearLayout parent) {
        TextView detailsTitle = new TextView(getContext());
        detailsTitle.setText("Additional Details");
        detailsTitle.setTextSize(18);
        detailsTitle.setPadding(0, 16, 0, 16);
        parent.addView(detailsTitle);
        
        // Notes
        TextView notesLabel = new TextView(getContext());
        notesLabel.setText("Notes:");
        parent.addView(notesLabel);
        
        notesEditText = new EditText(getContext());
        notesEditText.setHint("How was your day? What happened?");
        notesEditText.setMinLines(2);
        parent.addView(notesEditText);
        
        // Triggers
        TextView triggersLabel = new TextView(getContext());
        triggersLabel.setText("Mood Triggers:");
        triggersLabel.setPadding(0, 16, 0, 0);
        parent.addView(triggersLabel);
        
        triggersEditText = new EditText(getContext());
        triggersEditText.setHint("What affected your mood? (work, family, weather...)");
        parent.addView(triggersEditText);
        
        // Activities
        TextView activitiesLabel = new TextView(getContext());
        activitiesLabel.setText("Activities:");
        activitiesLabel.setPadding(0, 16, 0, 0);
        parent.addView(activitiesLabel);
        
        activitiesEditText = new EditText(getContext());
        activitiesEditText.setHint("What did you do today?");
        parent.addView(activitiesEditText);
    }
    
    private void setupLevelsSection(LinearLayout parent) {
        TextView levelsTitle = new TextView(getContext());
        levelsTitle.setText("Energy & Stress Levels");
        levelsTitle.setTextSize(18);
        levelsTitle.setPadding(0, 24, 0, 16);
        parent.addView(levelsTitle);
        
        // Energy Level
        TextView energyLabel = new TextView(getContext());
        energyLabel.setText("Energy Level:");
        parent.addView(energyLabel);
        
        energyLevelText = new TextView(getContext());
        energyLevelText.setText("5/10");
        parent.addView(energyLevelText);
        
        energySeekBar = new SeekBar(getContext());
        energySeekBar.setMax(10);
        energySeekBar.setProgress(5);
        energySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                energyLevelText.setText(progress + "/10");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        parent.addView(energySeekBar);
        
        // Stress Level
        TextView stressLabel = new TextView(getContext());
        stressLabel.setText("Stress Level:");
        stressLabel.setPadding(0, 16, 0, 0);
        parent.addView(stressLabel);
        
        stressLevelText = new TextView(getContext());
        stressLevelText.setText("5/10");
        parent.addView(stressLevelText);
        
        stressSeekBar = new SeekBar(getContext());
        stressSeekBar.setMax(10);
        stressSeekBar.setProgress(5);
        stressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                stressLevelText.setText(progress + "/10");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        parent.addView(stressSeekBar);
        
        // Social Level
        TextView socialLabel = new TextView(getContext());
        socialLabel.setText("Social Level:");
        socialLabel.setPadding(0, 16, 0, 0);
        parent.addView(socialLabel);
        
        socialLevelText = new TextView(getContext());
        socialLevelText.setText("5/10");
        parent.addView(socialLevelText);
        
        socialSeekBar = new SeekBar(getContext());
        socialSeekBar.setMax(10);
        socialSeekBar.setProgress(5);
        socialSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                socialLevelText.setText(progress + "/10");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        parent.addView(socialSeekBar);
    }
    
    private void setupContextSection(LinearLayout parent) {
        TextView contextTitle = new TextView(getContext());
        contextTitle.setText("Additional Context");
        contextTitle.setTextSize(18);
        contextTitle.setPadding(0, 24, 0, 16);
        parent.addView(contextTitle);
        
        // Sleep Hours
        TextView sleepLabel = new TextView(getContext());
        sleepLabel.setText("Hours of Sleep:");
        parent.addView(sleepLabel);
        
        sleepHoursEditText = new EditText(getContext());
        sleepHoursEditText.setHint("8");
        sleepHoursEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        parent.addView(sleepHoursEditText);
        
        // Exercise
        exerciseButton = new Button(getContext());
        exerciseButton.setText("Did you exercise today? No");
        exerciseButton.setOnClickListener(v -> {
            hasExercised = !hasExercised;
            exerciseButton.setText("Did you exercise today? " + (hasExercised ? "Yes" : "No"));
        });
        exerciseButton.setPadding(0, 16, 0, 0);
        parent.addView(exerciseButton);
        
        // Medications
        TextView medicationsLabel = new TextView(getContext());
        medicationsLabel.setText("Medications/Supplements:");
        medicationsLabel.setPadding(0, 16, 0, 0);
        parent.addView(medicationsLabel);
        
        medicationsEditText = new EditText(getContext());
        medicationsEditText.setHint("List any medications or supplements");
        parent.addView(medicationsEditText);
    }
    
    private void setupSaveButton(LinearLayout parent) {
        saveMoodButton = new Button(getContext());
        saveMoodButton.setText("Save Mood Entry");
        saveMoodButton.setOnClickListener(v -> saveMoodEntry());
        saveMoodButton.setPadding(0, 32, 0, 0);
        parent.addView(saveMoodButton);
    }
    
    private void selectMood(MoodEntry.MoodLevel level) {
        selectedMoodLevel = level;
        selectedMoodEmoji = level.getEmoji();
        selectedMoodText.setText("Selected: " + level.getEmoji() + " " + level.getDisplayName());
        
        // Update UI styling to show selection
        for (int i = 0; i < moodSelectionLayout.getChildCount(); i++) {
            View child = moodSelectionLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                if (button.getText().toString().contains(level.getDisplayName())) {
                    button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                } else {
                    button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                }
            }
        }
    }
    
    private void loadExistingMoodForToday() {
        String today = dateFormat.format(new Date());
        moodTrackingService.getMoodForDate(today, new MoodTrackingService.MoodCallback() {
            @Override
            public void onMoodReceived(MoodEntry moodEntry) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (moodEntry != null) {
                            populateFormWithExistingEntry(moodEntry);
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                Log.d(TAG, "No existing mood entry for today: " + error);
            }
        });
    }
    
    private void populateFormWithExistingEntry(MoodEntry entry) {
        existingEntry = entry;
        
        if (entry.getMoodLevel() != null) {
            selectMood(entry.getMoodLevel());
        }
        
        if (entry.getNotes() != null) {
            notesEditText.setText(entry.getNotes());
        }
        
        if (entry.getTriggers() != null) {
            triggersEditText.setText(entry.getTriggers());
        }
        
        if (entry.getActivities() != null) {
            activitiesEditText.setText(entry.getActivities());
        }
        
        energySeekBar.setProgress(entry.getEnergyLevel());
        stressSeekBar.setProgress(entry.getStressLevel());
        socialSeekBar.setProgress(entry.getSocialLevel());
        
        if (entry.getSleepHours() > 0) {
            sleepHoursEditText.setText(String.valueOf(entry.getSleepHours()));
        }
        
        hasExercised = entry.isHasExercised();
        exerciseButton.setText("Did you exercise today? " + (hasExercised ? "Yes" : "No"));
        
        if (entry.getMedications() != null) {
            medicationsEditText.setText(entry.getMedications());
        }
        
        saveMoodButton.setText("Update Mood Entry");
    }
    
    private void saveMoodEntry() {
        if (selectedMoodLevel == null) {
            Toast.makeText(getContext(), "Please select a mood level", Toast.LENGTH_SHORT).show();
            return;
        }
        
        MoodEntry moodEntry = new MoodEntry();
        moodEntry.setDate(dateFormat.format(new Date()));
        moodEntry.setMoodLevel(selectedMoodLevel);
        moodEntry.setMoodEmoji(selectedMoodEmoji);
        moodEntry.setNotes(notesEditText.getText().toString().trim());
        moodEntry.setTriggers(triggersEditText.getText().toString().trim());
        moodEntry.setActivities(activitiesEditText.getText().toString().trim());
        moodEntry.setEnergyLevel(energySeekBar.getProgress());
        moodEntry.setStressLevel(stressSeekBar.getProgress());
        moodEntry.setSocialLevel(socialSeekBar.getProgress());
        moodEntry.setHasExercised(hasExercised);
        
        // Parse sleep hours
        String sleepHoursText = sleepHoursEditText.getText().toString().trim();
        if (!sleepHoursText.isEmpty()) {
            try {
                int sleepHours = Integer.parseInt(sleepHoursText);
                moodEntry.setSleepHours(sleepHours);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid sleep hours format: " + sleepHoursText);
            }
        }
        
        moodEntry.setMedications(medicationsEditText.getText().toString().trim());
        
        // Save mood entry
        moodTrackingService.saveMoodEntry(moodEntry, new MoodTrackingService.MoodSaveCallback() {
            @Override
            public void onMoodSaved(MoodEntry savedEntry) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Mood entry saved successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Optional: Navigate back or clear form
                        clearForm();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error saving mood: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    
    private void clearForm() {
        selectedMoodLevel = null;
        selectedMoodEmoji = null;
        selectedMoodText.setText("No mood selected");
        notesEditText.setText("");
        triggersEditText.setText("");
        activitiesEditText.setText("");
        energySeekBar.setProgress(5);
        stressSeekBar.setProgress(5);
        socialSeekBar.setProgress(5);
        sleepHoursEditText.setText("");
        hasExercised = false;
        exerciseButton.setText("Did you exercise today? No");
        medicationsEditText.setText("");
        saveMoodButton.setText("Save Mood Entry");
        existingEntry = null;
        
        // Reset button colors
        for (int i = 0; i < moodSelectionLayout.getChildCount(); i++) {
            View child = moodSelectionLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (moodTrackingService != null) {
            moodTrackingService.shutdown();
        }
    }
}