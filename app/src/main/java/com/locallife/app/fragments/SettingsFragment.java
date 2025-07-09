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
}