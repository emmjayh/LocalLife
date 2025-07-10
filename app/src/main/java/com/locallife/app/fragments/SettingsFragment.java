package com.locallife.app.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.locallife.app.R;
import com.locallife.database.DatabaseHelper;
import com.locallife.model.DayRecord;
import com.locallife.model.PhotoMetadata;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "locallife_settings";
    private static final String PREF_NOTIFICATIONS = "notifications_enabled";
    private static final String PREF_LOCATION_SERVICES = "location_services_enabled";
    private static final String PREF_DARK_MODE = "dark_mode_enabled";
    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_USER_EMAIL = "user_email";

    private SharedPreferences sharedPreferences;
    private LinearLayout llProfile;
    private LinearLayout llNotifications;
    private LinearLayout llPrivacy;
    private LinearLayout llLocation;
    private LinearLayout llAbout;
    private LinearLayout llHelp;
    private LinearLayout llLogout;
    
    private Switch switchNotifications;
    private Switch switchLocationServices;
    private Switch switchDarkMode;
    
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvAppVersion;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        
        initializeViews(view);
        setupClickListeners();
        loadUserSettings();
    }

    private void initializeViews(View view) {
        // Layout items
        llProfile = view.findViewById(R.id.ll_profile);
        llNotifications = view.findViewById(R.id.ll_notifications);
        llPrivacy = view.findViewById(R.id.ll_privacy);
        llLocation = view.findViewById(R.id.ll_location);
        llAbout = view.findViewById(R.id.ll_about);
        llHelp = view.findViewById(R.id.ll_help);
        llLogout = view.findViewById(R.id.ll_logout);
        
        // Switches
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchLocationServices = view.findViewById(R.id.switch_location);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        
        // TextViews
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvAppVersion = view.findViewById(R.id.tv_app_version);
    }

    private void setupClickListeners() {
        llProfile.setOnClickListener(v -> openProfileSettings());
        llNotifications.setOnClickListener(v -> openNotificationSettings());
        llPrivacy.setOnClickListener(v -> openPrivacySettings());
        llLocation.setOnClickListener(v -> openLocationSettings());
        llAbout.setOnClickListener(v -> showAboutDialog());
        llHelp.setOnClickListener(v -> openHelpCenter());
        llLogout.setOnClickListener(v -> showLogoutDialog());
        
        // Switch listeners
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationPreference(isChecked);
        });
        
        switchLocationServices.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveLocationPreference(isChecked);
        });
        
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleDarkMode(isChecked);
        });
    }

    private void loadUserSettings() {
        // Load actual user data from preferences
        String defaultUserName = "John Doe";
        String defaultUserEmail = "john.doe@example.com";
        
        tvUserName.setText(sharedPreferences.getString(PREF_USER_NAME, defaultUserName));
        tvUserEmail.setText(sharedPreferences.getString(PREF_USER_EMAIL, defaultUserEmail));
        tvAppVersion.setText("Version 1.0.0");
        
        // Load switch states from preferences
        switchNotifications.setChecked(sharedPreferences.getBoolean(PREF_NOTIFICATIONS, true));
        switchLocationServices.setChecked(sharedPreferences.getBoolean(PREF_LOCATION_SERVICES, true));
        switchDarkMode.setChecked(sharedPreferences.getBoolean(PREF_DARK_MODE, false));
    }

    private void openProfileSettings() {
        // TODO: Navigate to profile settings
    }

    private void openNotificationSettings() {
        // TODO: Navigate to detailed notification settings
        showNotificationSettingsDialog();
    }

    private void openPrivacySettings() {
        // TODO: Navigate to privacy settings
        showPrivacySettingsDialog();
    }

    private void openLocationSettings() {
        // TODO: Navigate to location settings
        showLocationSettingsDialog();
    }
    
    private void showNotificationSettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Notification Settings")
                .setMessage("Configure your notification preferences here.")
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void showPrivacySettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Privacy Settings")
                .setMessage("Your privacy is important. Data is stored locally on your device.")
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void showLocationSettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Location Settings")
                .setMessage("Location data is used to provide weather updates and track visited places.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About LocalLife")
                .setMessage("LocalLife is your community companion app.\n\nVersion: 1.0.0\n\n© 2024 LocalLife Inc.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void openHelpCenter() {
        // TODO: Navigate to help center
        showExportDialog();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // TODO: Implement logout logic
    }

    private void saveNotificationPreference(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_NOTIFICATIONS, enabled);
        editor.apply();
        
        // Here you could also trigger actual notification service changes
        // if (enabled) {
        //     // Enable notifications
        // } else {
        //     // Disable notifications
        // }
    }

    private void saveLocationPreference(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_LOCATION_SERVICES, enabled);
        editor.apply();
        
        // Here you could also trigger location service changes
        // if (enabled) {
        //     // Start location services
        // } else {
        //     // Stop location services
        // }
    }

    private void toggleDarkMode(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_DARK_MODE, enabled);
        editor.apply();
        
        // Apply theme change
        if (enabled) {
            // Apply dark theme
            if (getActivity() != null) {
                getActivity().setTheme(R.style.Theme_LocalLife_Dark);
            }
        } else {
            // Apply light theme
            if (getActivity() != null) {
                getActivity().setTheme(R.style.Theme_LocalLife);
            }
        }
        
        // Restart activity to apply theme change
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }
    
    /**
     * Save user profile information
     */
    public void saveUserProfile(String name, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_USER_NAME, name);
        editor.putString(PREF_USER_EMAIL, email);
        editor.apply();
        
        // Update UI
        tvUserName.setText(name);
        tvUserEmail.setText(email);
    }
    
    /**
     * Get current settings as a bundle for other components
     */
    public Bundle getCurrentSettings() {
        Bundle settings = new Bundle();
        settings.putBoolean(PREF_NOTIFICATIONS, sharedPreferences.getBoolean(PREF_NOTIFICATIONS, true));
        settings.putBoolean(PREF_LOCATION_SERVICES, sharedPreferences.getBoolean(PREF_LOCATION_SERVICES, true));
        settings.putBoolean(PREF_DARK_MODE, sharedPreferences.getBoolean(PREF_DARK_MODE, false));
        settings.putString(PREF_USER_NAME, sharedPreferences.getString(PREF_USER_NAME, "John Doe"));
        settings.putString(PREF_USER_EMAIL, sharedPreferences.getString(PREF_USER_EMAIL, "john.doe@example.com"));
        return settings;
    }
    
    /**
     * Show dialog for data export options
     */
    private void showExportDialog() {
        String[] options = {"Export to CSV", "Export to JSON", "Export All Data"};
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Export Data")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            exportToCSV();
                            break;
                        case 1:
                            exportToJSON();
                            break;
                        case 2:
                            exportAllData();
                            break;
                    }
                })
                .show();
    }
    
    /**
     * Export daily records to CSV format
     */
    private void exportToCSV() {
        try {
            DatabaseHelper db = DatabaseHelper.getInstance(getContext());
            List<DayRecord> records = db.getAllDayRecords();
            
            if (records.isEmpty()) {
                showMessage("No data to export");
                return;
            }
            
            // Create CSV file
            String fileName = "locallife_data_" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".csv";
            File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            FileWriter writer = new FileWriter(file);
            
            // Write CSV header
            writer.append("Date,Steps,Places Visited,Screen Time (min),Battery Usage %,Activity Score,Temperature,Weather,Primary Location\n");
            
            // Write data rows
            for (DayRecord record : records) {
                writer.append(String.valueOf(record.getDate())).append(",");
                writer.append(String.valueOf(record.getStepCount())).append(",");
                writer.append(String.valueOf(record.getPlacesVisited())).append(",");
                writer.append(String.valueOf(record.getScreenTimeMinutes())).append(",");
                writer.append(String.valueOf(record.getBatteryUsagePercent())).append(",");
                writer.append(String.valueOf(record.getActivityScore())).append(",");
                writer.append(String.valueOf(record.getTemperature())).append(",");
                writer.append(String.valueOf(record.getWeatherCondition())).append(",");
                writer.append(String.valueOf(record.getPrimaryLocation())).append("\n");
            }
            
            writer.close();
            
            // Share the file
            shareFile(file, "text/csv");
            
        } catch (IOException e) {
            showMessage("Error exporting data: " + e.getMessage());
        }
    }
    
    /**
     * Export daily records to JSON format
     */
    private void exportToJSON() {
        try {
            DatabaseHelper db = DatabaseHelper.getInstance(getContext());
            List<DayRecord> records = db.getAllDayRecords();
            
            if (records.isEmpty()) {
                showMessage("No data to export");
                return;
            }
            
            // Create JSON file
            String fileName = "locallife_data_" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".json";
            File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            FileWriter writer = new FileWriter(file);
            
            // Write JSON
            writer.append("{\n");
            writer.append("  \"export_date\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append("\",\n");
            writer.append("  \"app_version\": \"1.0.0\",\n");
            writer.append("  \"data_type\": \"daily_records\",\n");
            writer.append("  \"records\": [\n");
            
            for (int i = 0; i < records.size(); i++) {
                DayRecord record = records.get(i);
                writer.append("    {\n");
                writer.append("      \"date\": \"").append(record.getDate()).append("\",\n");
                writer.append("      \"step_count\": ").append(String.valueOf(record.getStepCount())).append(",\n");
                writer.append("      \"places_visited\": ").append(String.valueOf(record.getPlacesVisited())).append(",\n");
                writer.append("      \"screen_time_minutes\": ").append(String.valueOf(record.getScreenTimeMinutes())).append(",\n");
                writer.append("      \"battery_usage_percent\": ").append(String.valueOf(record.getBatteryUsagePercent())).append(",\n");
                writer.append("      \"activity_score\": ").append(String.valueOf(record.getActivityScore())).append(",\n");
                writer.append("      \"temperature\": ").append(String.valueOf(record.getTemperature())).append(",\n");
                writer.append("      \"humidity\": ").append(String.valueOf(record.getHumidity())).append(",\n");
                writer.append("      \"weather_condition\": \"").append(record.getWeatherCondition() != null ? record.getWeatherCondition() : "").append("\",\n");
                writer.append("      \"primary_location\": \"").append(record.getPrimaryLocation() != null ? record.getPrimaryLocation() : "").append("\",\n");
                writer.append("      \"phone_unlocks\": ").append(String.valueOf(record.getPhoneUnlocks())).append(",\n");
                writer.append("      \"photo_count\": ").append(String.valueOf(record.getPhotoCount())).append("\n");
                writer.append("    }");
                
                if (i < records.size() - 1) {
                    writer.append(",");
                }
                writer.append("\n");
            }
            
            writer.append("  ]\n");
            writer.append("}\n");
            
            writer.close();
            
            // Share the file
            shareFile(file, "application/json");
            
        } catch (IOException e) {
            showMessage("Error exporting data: " + e.getMessage());
        }
    }
    
    /**
     * Export all data including photos and detailed records
     */
    private void exportAllData() {
        try {
            DatabaseHelper db = DatabaseHelper.getInstance(getContext());
            List<DayRecord> records = db.getAllDayRecords();
            
            if (records.isEmpty()) {
                showMessage("No data to export");
                return;
            }
            
            // Create comprehensive JSON file
            String fileName = "locallife_full_export_" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".json";
            File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            FileWriter writer = new FileWriter(file);
            
            // Write comprehensive JSON
            writer.append("{\n");
            writer.append("  \"export_info\": {\n");
            writer.append("    \"export_date\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append("\",\n");
            writer.append("    \"app_version\": \"1.0.0\",\n");
            writer.append("    \"total_records\": ").append(String.valueOf(records.size())).append("\n");
            writer.append("  },\n");
            
            // Export daily records
            writer.append("  \"daily_records\": [\n");
            for (int i = 0; i < records.size(); i++) {
                DayRecord record = records.get(i);
                writer.append("    {\n");
                writer.append("      \"date\": \"").append(record.getDate()).append("\",\n");
                writer.append("      \"metrics\": {\n");
                writer.append("        \"step_count\": ").append(String.valueOf(record.getStepCount())).append(",\n");
                writer.append("        \"places_visited\": ").append(String.valueOf(record.getPlacesVisited())).append(",\n");
                writer.append("        \"screen_time_minutes\": ").append(String.valueOf(record.getScreenTimeMinutes())).append(",\n");
                writer.append("        \"battery_usage_percent\": ").append(String.valueOf(record.getBatteryUsagePercent())).append(",\n");
                writer.append("        \"phone_unlocks\": ").append(String.valueOf(record.getPhoneUnlocks())).append(",\n");
                writer.append("        \"photo_count\": ").append(String.valueOf(record.getPhotoCount())).append("\n");
                writer.append("      },\n");
                writer.append("      \"scores\": {\n");
                writer.append("        \"activity_score\": ").append(String.valueOf(record.getActivityScore())).append(",\n");
                writer.append("        \"physical_activity_score\": ").append(String.valueOf(record.getPhysicalActivityScore())).append(",\n");
                writer.append("        \"social_activity_score\": ").append(String.valueOf(record.getSocialActivityScore())).append(",\n");
                writer.append("        \"productivity_score\": ").append(String.valueOf(record.getProductivityScore())).append(",\n");
                writer.append("        \"overall_wellbeing_score\": ").append(String.valueOf(record.getOverallWellbeingScore())).append("\n");
                writer.append("      },\n");
                writer.append("      \"environmental\": {\n");
                writer.append("        \"temperature\": ").append(String.valueOf(record.getTemperature())).append(",\n");
                writer.append("        \"humidity\": ").append(String.valueOf(record.getHumidity())).append(",\n");
                writer.append("        \"weather_condition\": \"").append(record.getWeatherCondition() != null ? record.getWeatherCondition() : "").append("\",\n");
                writer.append("        \"wind_speed\": ").append(String.valueOf(record.getWindSpeed())).append("\n");
                writer.append("      },\n");
                writer.append("      \"location\": {\n");
                writer.append("        \"primary_location\": \"").append(record.getPrimaryLocation() != null ? record.getPrimaryLocation() : "").append("\",\n");
                writer.append("        \"total_travel_distance\": ").append(String.valueOf(record.getTotalTravelDistance())).append("\n");
                writer.append("      }\n");
                writer.append("    }");
                
                if (i < records.size() - 1) {
                    writer.append(",");
                }
                writer.append("\n");
            }
            writer.append("  ],\n");
            
            // Export photo metadata summary
            writer.append("  \"photo_summary\": {\n");
            writer.append("    \"total_photos\": ").append(String.valueOf(getTotalPhotoCount())).append(",\n");
            writer.append("    \"by_time_of_day\": ").append(mapToJson(db.getPhotoCountsByTimeOfDay())).append(",\n");
            writer.append("    \"by_day_of_week\": ").append(mapToJson(db.getPhotoCountsByDayOfWeek())).append(",\n");
            writer.append("    \"by_activity_type\": ").append(mapToJson(db.getPhotoCountsByActivityType())).append("\n");
            writer.append("  }\n");
            
            writer.append("}\n");
            
            writer.close();
            
            // Share the file
            shareFile(file, "application/json");
            
        } catch (IOException e) {
            showMessage("Error exporting data: " + e.getMessage());
        }
    }
    
    /**
     * Share exported file
     */
    private void shareFile(File file, String mimeType) {
        try {
            Uri fileUri = FileProvider.getUriForFile(getContext(), 
                getContext().getPackageName() + ".fileprovider", file);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "LocalLife Data Export");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Exported data from LocalLife app");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share exported data"));
            
        } catch (Exception e) {
            showMessage("Error sharing file: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to convert map to JSON string
     */
    private String mapToJson(java.util.Map<String, Integer> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, Integer> entry : map.entrySet()) {
            if (!first) json.append(", ");
            json.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * Get total photo count across all days
     */
    private int getTotalPhotoCount() {
        try {
            DatabaseHelper db = DatabaseHelper.getInstance(getContext());
            List<DayRecord> records = db.getAllDayRecords();
            int total = 0;
            for (DayRecord record : records) {
                total += record.getPhotoCount();
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Show message to user
     */
    private void showMessage(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Export")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}