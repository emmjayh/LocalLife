package com.locallife.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.locallife.app.R;
import com.locallife.app.fragments.CalendarFragment;
import com.locallife.app.fragments.DashboardFragment;
import com.locallife.app.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBottomNavigation();
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment = null;
                    
                    switch (item.getItemId()) {
                        case R.id.navigation_dashboard:
                            fragment = new DashboardFragment();
                            break;
                        case R.id.navigation_calendar:
                            fragment = new CalendarFragment();
                            break;
                        case R.id.navigation_settings:
                            fragment = new SettingsFragment();
                            break;
                    }
                    
                    if (fragment != null) {
                        loadFragment(fragment);
                        return true;
                    }
                    return false;
                }
            });
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            currentFragment = fragment;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}