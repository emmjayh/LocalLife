package com.locallife.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.locallife.app.R;

public class SettingsFragment extends Fragment {

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
        // TODO: Load actual user data from preferences/database
        tvUserName.setText("John Doe");
        tvUserEmail.setText("john.doe@example.com");
        tvAppVersion.setText("Version 1.0.0");
        
        // Load switch states
        switchNotifications.setChecked(true);
        switchLocationServices.setChecked(true);
        switchDarkMode.setChecked(false);
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
        // TODO: Save to SharedPreferences
    }

    private void saveLocationPreference(boolean enabled) {
        // TODO: Save to SharedPreferences
    }

    private void toggleDarkMode(boolean enabled) {
        // TODO: Apply dark mode theme
    }
}