package com.locallife.app;

import android.app.Application;
import com.locallife.app.database.DatabaseHelper;
import com.locallife.app.utils.PreferenceManager;

public class LocalLifeApplication extends Application {

    private static LocalLifeApplication instance;
    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize database
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize preferences
        preferenceManager = new PreferenceManager(this);
    }

    public static LocalLifeApplication getInstance() {
        return instance;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }
}