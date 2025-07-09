package com.locallife.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.locallife.model.DayRecord;
import com.locallife.model.PhotoMetadata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Enhanced database helper with all necessary tables for LocalLife app
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "locallife.db";
    private static final int DATABASE_VERSION = 4;
    
    // Date format for database storage
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    
    // Table names
    private static final String TABLE_DAY_RECORDS = "day_records";
    private static final String TABLE_LOCATION_VISITS = "location_visits";
    private static final String TABLE_STEP_DATA = "step_data";
    private static final String TABLE_WEATHER_DATA = "weather_data";
    private static final String TABLE_BATTERY_DATA = "battery_data";
    private static final String TABLE_SCREEN_TIME = "screen_time";
    private static final String TABLE_PHOTO_METADATA = "photo_metadata";
    
    // Day Records table columns
    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_STEP_COUNT = "step_count";
    private static final String KEY_TOTAL_DISTANCE = "total_distance";
    private static final String KEY_ACTIVE_MINUTES = "active_minutes";
    private static final String KEY_ACTIVITY_SCORE = "activity_score";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_HUMIDITY = "humidity";
    private static final String KEY_WEATHER_CONDITION = "weather_condition";
    private static final String KEY_WIND_SPEED = "wind_speed";
    private static final String KEY_PLACES_VISITED = "places_visited";
    private static final String KEY_PRIMARY_LOCATION = "primary_location";
    private static final String KEY_TOTAL_TRAVEL_DISTANCE = "total_travel_distance";
    private static final String KEY_SCREEN_TIME_MINUTES = "screen_time_minutes";
    private static final String KEY_BATTERY_USAGE_PERCENT = "battery_usage_percent";
    private static final String KEY_PHONE_UNLOCKS = "phone_unlocks";
    private static final String KEY_PHYSICAL_ACTIVITY_SCORE = "physical_activity_score";
    private static final String KEY_SOCIAL_ACTIVITY_SCORE = "social_activity_score";
    private static final String KEY_PRODUCTIVITY_SCORE = "productivity_score";
    private static final String KEY_OVERALL_WELLBEING_SCORE = "overall_wellbeing_score";
    private static final String KEY_PHOTO_COUNT = "photo_count";
    private static final String KEY_PHOTO_ACTIVITY_SCORE = "photo_activity_score";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";
    
    // Location Visits table columns
    private static final String KEY_RECORD_ID = "record_id";
    private static final String KEY_PLACE_NAME = "place_name";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ARRIVAL_TIME = "arrival_time";
    private static final String KEY_DEPARTURE_TIME = "departure_time";
    private static final String KEY_DURATION_MINUTES = "duration_minutes";
    private static final String KEY_PLACE_CATEGORY = "place_category";
    
    // Step Data table columns
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_STEP_TYPE = "step_type"; // daily, hourly
    
    // Weather Data table columns
    private static final String KEY_LOCATION = "location";
    private static final String KEY_WEATHER_CODE = "weather_code";
    private static final String KEY_PRECIPITATION = "precipitation";
    private static final String KEY_CLOUD_COVER = "cloud_cover";
    
    // Battery Data table columns
    private static final String KEY_BATTERY_LEVEL = "battery_level";
    private static final String KEY_IS_CHARGING = "is_charging";
    private static final String KEY_BATTERY_HEALTH = "battery_health";
    
    // Screen Time table columns
    private static final String KEY_APP_NAME = "app_name";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_USAGE_TIME = "usage_time";
    private static final String KEY_LAST_USED = "last_used";
    
    // Photo Metadata table columns
    private static final String KEY_PHOTO_URI = "photo_uri";
    private static final String KEY_PHOTO_PATH = "photo_path";
    private static final String KEY_DATE_TAKEN = "date_taken";
    private static final String KEY_DATE_MODIFIED = "date_modified";
    private static final String KEY_CAMERA_MAKE = "camera_make";
    private static final String KEY_CAMERA_MODEL = "camera_model";
    private static final String KEY_IMAGE_WIDTH = "image_width";
    private static final String KEY_IMAGE_HEIGHT = "image_height";
    private static final String KEY_ORIENTATION = "orientation";
    private static final String KEY_FLASH_MODE = "flash_mode";
    private static final String KEY_FOCAL_LENGTH = "focal_length";
    private static final String KEY_APERTURE = "aperture";
    private static final String KEY_SHUTTER_SPEED = "shutter_speed";
    private static final String KEY_ISO = "iso";
    private static final String KEY_WHITE_BALANCE = "white_balance";
    private static final String KEY_FILE_SIZE = "file_size";
    private static final String KEY_MIME_TYPE = "mime_type";
    private static final String KEY_HAS_LOCATION_DATA = "has_location_data";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_ACTIVITY_TYPE = "activity_type";
    private static final String KEY_ACTIVITY_SCORE = "activity_score";
    private static final String KEY_TIME_OF_DAY = "time_of_day";
    private static final String KEY_SEASON = "season";
    private static final String KEY_IS_OUTDOOR = "is_outdoor";
    private static final String KEY_HAS_PEOPLE = "has_people";
    private static final String KEY_DOMINANT_COLORS = "dominant_colors";
    private static final String KEY_IS_PROCESSED = "is_processed";
    private static final String KEY_PROCESSING_ERROR = "processing_error";
    
    private static DatabaseHelper instance;
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        createIndexes(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // Drop all tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAY_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_VISITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEP_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEATHER_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BATTERY_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCREEN_TIME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO_METADATA);
        
        onCreate(db);
    }
    
    private void createTables(SQLiteDatabase db) {
        // Day Records table
        String CREATE_DAY_RECORDS_TABLE = "CREATE TABLE " + TABLE_DAY_RECORDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT UNIQUE NOT NULL,"
                + KEY_STEP_COUNT + " INTEGER DEFAULT 0,"
                + KEY_TOTAL_DISTANCE + " REAL DEFAULT 0,"
                + KEY_ACTIVE_MINUTES + " INTEGER DEFAULT 0,"
                + KEY_ACTIVITY_SCORE + " REAL DEFAULT 0,"
                + KEY_TEMPERATURE + " REAL DEFAULT 0,"
                + KEY_HUMIDITY + " REAL DEFAULT 0,"
                + KEY_WEATHER_CONDITION + " TEXT,"
                + KEY_WIND_SPEED + " REAL DEFAULT 0,"
                + KEY_PLACES_VISITED + " INTEGER DEFAULT 0,"
                + KEY_PRIMARY_LOCATION + " TEXT,"
                + KEY_TOTAL_TRAVEL_DISTANCE + " REAL DEFAULT 0,"
                + KEY_SCREEN_TIME_MINUTES + " INTEGER DEFAULT 0,"
                + KEY_BATTERY_USAGE_PERCENT + " REAL DEFAULT 0,"
                + KEY_PHONE_UNLOCKS + " INTEGER DEFAULT 0,"
                + KEY_PHYSICAL_ACTIVITY_SCORE + " REAL DEFAULT 0,"
                + KEY_SOCIAL_ACTIVITY_SCORE + " REAL DEFAULT 0,"
                + KEY_PRODUCTIVITY_SCORE + " REAL DEFAULT 0,"
                + KEY_OVERALL_WELLBEING_SCORE + " REAL DEFAULT 0,"
                + KEY_PHOTO_COUNT + " INTEGER DEFAULT 0,"
                + KEY_PHOTO_ACTIVITY_SCORE + " REAL DEFAULT 0,"
                + KEY_CREATED_AT + " TEXT NOT NULL,"
                + KEY_UPDATED_AT + " TEXT NOT NULL"
                + ")";
        
        // Location Visits table
        String CREATE_LOCATION_VISITS_TABLE = "CREATE TABLE " + TABLE_LOCATION_VISITS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_RECORD_ID + " INTEGER NOT NULL,"
                + KEY_PLACE_NAME + " TEXT,"
                + KEY_LATITUDE + " REAL NOT NULL,"
                + KEY_LONGITUDE + " REAL NOT NULL,"
                + KEY_ARRIVAL_TIME + " TEXT NOT NULL,"
                + KEY_DEPARTURE_TIME + " TEXT,"
                + KEY_DURATION_MINUTES + " INTEGER DEFAULT 0,"
                + KEY_PLACE_CATEGORY + " TEXT,"
                + "FOREIGN KEY(" + KEY_RECORD_ID + ") REFERENCES " + TABLE_DAY_RECORDS + "(" + KEY_ID + ")"
                + ")";
        
        // Step Data table
        String CREATE_STEP_DATA_TABLE = "CREATE TABLE " + TABLE_STEP_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " TEXT NOT NULL,"
                + KEY_STEPS + " INTEGER NOT NULL,"
                + KEY_STEP_TYPE + " TEXT DEFAULT 'hourly'"
                + ")";
        
        // Weather Data table
        String CREATE_WEATHER_DATA_TABLE = "CREATE TABLE " + TABLE_WEATHER_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " TEXT NOT NULL,"
                + KEY_LOCATION + " TEXT,"
                + KEY_TEMPERATURE + " REAL,"
                + KEY_HUMIDITY + " REAL,"
                + KEY_WEATHER_CONDITION + " TEXT,"
                + KEY_WEATHER_CODE + " INTEGER,"
                + KEY_WIND_SPEED + " REAL,"
                + KEY_PRECIPITATION + " REAL,"
                + KEY_CLOUD_COVER + " REAL"
                + ")";
        
        // Battery Data table
        String CREATE_BATTERY_DATA_TABLE = "CREATE TABLE " + TABLE_BATTERY_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " TEXT NOT NULL,"
                + KEY_BATTERY_LEVEL + " INTEGER,"
                + KEY_IS_CHARGING + " INTEGER,"
                + KEY_BATTERY_HEALTH + " TEXT"
                + ")";
        
        // Screen Time table
        String CREATE_SCREEN_TIME_TABLE = "CREATE TABLE " + TABLE_SCREEN_TIME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_APP_NAME + " TEXT,"
                + KEY_PACKAGE_NAME + " TEXT,"
                + KEY_USAGE_TIME + " INTEGER,"
                + KEY_LAST_USED + " TEXT"
                + ")";
        
        // Photo Metadata table
        String CREATE_PHOTO_METADATA_TABLE = "CREATE TABLE " + TABLE_PHOTO_METADATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PHOTO_URI + " TEXT NOT NULL,"
                + KEY_PHOTO_PATH + " TEXT UNIQUE NOT NULL,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_DATE_TAKEN + " TEXT,"
                + KEY_DATE_MODIFIED + " TEXT,"
                + KEY_LATITUDE + " REAL DEFAULT 0,"
                + KEY_LONGITUDE + " REAL DEFAULT 0,"
                + KEY_ALTITUDE + " REAL DEFAULT 0,"
                + KEY_LOCATION_NAME + " TEXT,"
                + KEY_CAMERA_MAKE + " TEXT,"
                + KEY_CAMERA_MODEL + " TEXT,"
                + KEY_IMAGE_WIDTH + " INTEGER DEFAULT 0,"
                + KEY_IMAGE_HEIGHT + " INTEGER DEFAULT 0,"
                + KEY_ORIENTATION + " INTEGER DEFAULT 0,"
                + KEY_FLASH_MODE + " TEXT,"
                + KEY_FOCAL_LENGTH + " TEXT,"
                + KEY_APERTURE + " TEXT,"
                + KEY_SHUTTER_SPEED + " TEXT,"
                + KEY_ISO + " TEXT,"
                + KEY_WHITE_BALANCE + " TEXT,"
                + KEY_FILE_SIZE + " INTEGER DEFAULT 0,"
                + KEY_MIME_TYPE + " TEXT,"
                + KEY_HAS_LOCATION_DATA + " INTEGER DEFAULT 0,"
                + KEY_ACTIVITY_TYPE + " TEXT,"
                + KEY_ACTIVITY_SCORE + " INTEGER DEFAULT 0,"
                + KEY_TIME_OF_DAY + " TEXT,"
                + KEY_SEASON + " TEXT,"
                + KEY_IS_OUTDOOR + " INTEGER DEFAULT 0,"
                + KEY_HAS_PEOPLE + " INTEGER DEFAULT 0,"
                + KEY_DOMINANT_COLORS + " TEXT,"
                + KEY_IS_PROCESSED + " INTEGER DEFAULT 0,"
                + KEY_PROCESSING_ERROR + " TEXT,"
                + KEY_CREATED_AT + " TEXT NOT NULL,"
                + KEY_UPDATED_AT + " TEXT NOT NULL"
                + ")";
        
        db.execSQL(CREATE_DAY_RECORDS_TABLE);
        db.execSQL(CREATE_LOCATION_VISITS_TABLE);
        db.execSQL(CREATE_STEP_DATA_TABLE);
        db.execSQL(CREATE_WEATHER_DATA_TABLE);
        db.execSQL(CREATE_BATTERY_DATA_TABLE);
        db.execSQL(CREATE_SCREEN_TIME_TABLE);
        db.execSQL(CREATE_PHOTO_METADATA_TABLE);
    }
    
    private void createIndexes(SQLiteDatabase db) {
        // Create indexes for better performance
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_day_records_date ON " + TABLE_DAY_RECORDS + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_location_visits_record_id ON " + TABLE_LOCATION_VISITS + "(" + KEY_RECORD_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_step_data_date ON " + TABLE_STEP_DATA + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weather_data_date ON " + TABLE_WEATHER_DATA + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_battery_data_date ON " + TABLE_BATTERY_DATA + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_screen_time_date ON " + TABLE_SCREEN_TIME + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_photo_metadata_date ON " + TABLE_PHOTO_METADATA + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_photo_metadata_path ON " + TABLE_PHOTO_METADATA + "(" + KEY_PHOTO_PATH + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_photo_metadata_date_taken ON " + TABLE_PHOTO_METADATA + "(" + KEY_DATE_TAKEN + ")");
    }
    
    // Day Records CRUD operations
    public long insertDayRecord(DayRecord dayRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, dayRecord.getDate());
        values.put(KEY_STEP_COUNT, dayRecord.getStepCount());
        values.put(KEY_TOTAL_DISTANCE, dayRecord.getTotalDistance());
        values.put(KEY_ACTIVE_MINUTES, dayRecord.getActiveMinutes());
        values.put(KEY_ACTIVITY_SCORE, dayRecord.getActivityScore());
        values.put(KEY_TEMPERATURE, dayRecord.getTemperature());
        values.put(KEY_HUMIDITY, dayRecord.getHumidity());
        values.put(KEY_WEATHER_CONDITION, dayRecord.getWeatherCondition());
        values.put(KEY_WIND_SPEED, dayRecord.getWindSpeed());
        values.put(KEY_PLACES_VISITED, dayRecord.getPlacesVisited());
        values.put(KEY_PRIMARY_LOCATION, dayRecord.getPrimaryLocation());
        values.put(KEY_TOTAL_TRAVEL_DISTANCE, dayRecord.getTotalTravelDistance());
        values.put(KEY_SCREEN_TIME_MINUTES, dayRecord.getScreenTimeMinutes());
        values.put(KEY_BATTERY_USAGE_PERCENT, dayRecord.getBatteryUsagePercent());
        values.put(KEY_PHONE_UNLOCKS, dayRecord.getPhoneUnlocks());
        values.put(KEY_PHYSICAL_ACTIVITY_SCORE, dayRecord.getPhysicalActivityScore());
        values.put(KEY_SOCIAL_ACTIVITY_SCORE, dayRecord.getSocialActivityScore());
        values.put(KEY_PRODUCTIVITY_SCORE, dayRecord.getProductivityScore());
        values.put(KEY_OVERALL_WELLBEING_SCORE, dayRecord.getOverallWellbeingScore());
        values.put(KEY_PHOTO_COUNT, dayRecord.getPhotoCount());
        values.put(KEY_PHOTO_ACTIVITY_SCORE, dayRecord.getPhotoActivityScore());
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(dayRecord.getCreatedAt()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(dayRecord.getUpdatedAt()));
        
        long id = db.insertOrThrow(TABLE_DAY_RECORDS, null, values);
        dayRecord.setId(id);
        
        // Insert location visits
        for (DayRecord.LocationVisit visit : dayRecord.getLocationVisits()) {
            insertLocationVisit(id, visit);
        }
        
        return id;
    }
    
    public DayRecord getDayRecord(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAY_RECORDS, null, KEY_DATE + "=?", 
                new String[]{date}, null, null, null);
        
        DayRecord dayRecord = null;
        if (cursor.moveToFirst()) {
            dayRecord = cursorToDayRecord(cursor);
            // Load location visits
            dayRecord.setLocationVisits(getLocationVisits(dayRecord.getId()));
        }
        cursor.close();
        return dayRecord;
    }
    
    public List<DayRecord> getAllDayRecords() {
        List<DayRecord> dayRecords = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAY_RECORDS, null, null, null, null, null, KEY_DATE + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                DayRecord dayRecord = cursorToDayRecord(cursor);
                dayRecord.setLocationVisits(getLocationVisits(dayRecord.getId()));
                dayRecords.add(dayRecord);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dayRecords;
    }
    
    public int updateDayRecord(DayRecord dayRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_STEP_COUNT, dayRecord.getStepCount());
        values.put(KEY_TOTAL_DISTANCE, dayRecord.getTotalDistance());
        values.put(KEY_ACTIVE_MINUTES, dayRecord.getActiveMinutes());
        values.put(KEY_ACTIVITY_SCORE, dayRecord.getActivityScore());
        values.put(KEY_TEMPERATURE, dayRecord.getTemperature());
        values.put(KEY_HUMIDITY, dayRecord.getHumidity());
        values.put(KEY_WEATHER_CONDITION, dayRecord.getWeatherCondition());
        values.put(KEY_WIND_SPEED, dayRecord.getWindSpeed());
        values.put(KEY_PLACES_VISITED, dayRecord.getPlacesVisited());
        values.put(KEY_PRIMARY_LOCATION, dayRecord.getPrimaryLocation());
        values.put(KEY_TOTAL_TRAVEL_DISTANCE, dayRecord.getTotalTravelDistance());
        values.put(KEY_SCREEN_TIME_MINUTES, dayRecord.getScreenTimeMinutes());
        values.put(KEY_BATTERY_USAGE_PERCENT, dayRecord.getBatteryUsagePercent());
        values.put(KEY_PHONE_UNLOCKS, dayRecord.getPhoneUnlocks());
        values.put(KEY_PHYSICAL_ACTIVITY_SCORE, dayRecord.getPhysicalActivityScore());
        values.put(KEY_SOCIAL_ACTIVITY_SCORE, dayRecord.getSocialActivityScore());
        values.put(KEY_PRODUCTIVITY_SCORE, dayRecord.getProductivityScore());
        values.put(KEY_OVERALL_WELLBEING_SCORE, dayRecord.getOverallWellbeingScore());
        values.put(KEY_PHOTO_COUNT, dayRecord.getPhotoCount());
        values.put(KEY_PHOTO_ACTIVITY_SCORE, dayRecord.getPhotoActivityScore());
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.update(TABLE_DAY_RECORDS, values, KEY_ID + "=?", 
                new String[]{String.valueOf(dayRecord.getId())});
    }
    
    private DayRecord cursorToDayRecord(Cursor cursor) {
        DayRecord dayRecord = new DayRecord();
        dayRecord.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
        dayRecord.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
        dayRecord.setStepCount(cursor.getInt(cursor.getColumnIndex(KEY_STEP_COUNT)));
        dayRecord.setTotalDistance(cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_DISTANCE)));
        dayRecord.setActiveMinutes(cursor.getInt(cursor.getColumnIndex(KEY_ACTIVE_MINUTES)));
        dayRecord.setActivityScore(cursor.getFloat(cursor.getColumnIndex(KEY_ACTIVITY_SCORE)));
        dayRecord.setTemperature(cursor.getFloat(cursor.getColumnIndex(KEY_TEMPERATURE)));
        dayRecord.setHumidity(cursor.getFloat(cursor.getColumnIndex(KEY_HUMIDITY)));
        dayRecord.setWeatherCondition(cursor.getString(cursor.getColumnIndex(KEY_WEATHER_CONDITION)));
        dayRecord.setWindSpeed(cursor.getFloat(cursor.getColumnIndex(KEY_WIND_SPEED)));
        dayRecord.setPlacesVisited(cursor.getInt(cursor.getColumnIndex(KEY_PLACES_VISITED)));
        dayRecord.setPrimaryLocation(cursor.getString(cursor.getColumnIndex(KEY_PRIMARY_LOCATION)));
        dayRecord.setTotalTravelDistance(cursor.getFloat(cursor.getColumnIndex(KEY_TOTAL_TRAVEL_DISTANCE)));
        dayRecord.setScreenTimeMinutes(cursor.getInt(cursor.getColumnIndex(KEY_SCREEN_TIME_MINUTES)));
        dayRecord.setBatteryUsagePercent(cursor.getFloat(cursor.getColumnIndex(KEY_BATTERY_USAGE_PERCENT)));
        dayRecord.setPhoneUnlocks(cursor.getInt(cursor.getColumnIndex(KEY_PHONE_UNLOCKS)));
        dayRecord.setPhysicalActivityScore(cursor.getFloat(cursor.getColumnIndex(KEY_PHYSICAL_ACTIVITY_SCORE)));
        dayRecord.setSocialActivityScore(cursor.getFloat(cursor.getColumnIndex(KEY_SOCIAL_ACTIVITY_SCORE)));
        dayRecord.setProductivityScore(cursor.getFloat(cursor.getColumnIndex(KEY_PRODUCTIVITY_SCORE)));
        dayRecord.setOverallWellbeingScore(cursor.getFloat(cursor.getColumnIndex(KEY_OVERALL_WELLBEING_SCORE)));
        dayRecord.setPhotoCount(cursor.getInt(cursor.getColumnIndex(KEY_PHOTO_COUNT)));
        dayRecord.setPhotoActivityScore(cursor.getFloat(cursor.getColumnIndex(KEY_PHOTO_ACTIVITY_SCORE)));
        
        try {
            dayRecord.setCreatedAt(DATETIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT))));
            dayRecord.setUpdatedAt(DATETIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex(KEY_UPDATED_AT))));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e);
        }
        
        return dayRecord;
    }
    
    // Location Visits operations
    private void insertLocationVisit(long recordId, DayRecord.LocationVisit visit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_RECORD_ID, recordId);
        values.put(KEY_PLACE_NAME, visit.getPlaceName());
        values.put(KEY_LATITUDE, visit.getLatitude());
        values.put(KEY_LONGITUDE, visit.getLongitude());
        values.put(KEY_ARRIVAL_TIME, DATETIME_FORMAT.format(visit.getArrivalTime()));
        if (visit.getDepartureTime() != null) {
            values.put(KEY_DEPARTURE_TIME, DATETIME_FORMAT.format(visit.getDepartureTime()));
        }
        values.put(KEY_DURATION_MINUTES, visit.getDurationMinutes());
        values.put(KEY_PLACE_CATEGORY, visit.getPlaceCategory());
        
        db.insert(TABLE_LOCATION_VISITS, null, values);
    }
    
    private List<DayRecord.LocationVisit> getLocationVisits(long recordId) {
        List<DayRecord.LocationVisit> visits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOCATION_VISITS, null, KEY_RECORD_ID + "=?", 
                new String[]{String.valueOf(recordId)}, null, null, KEY_ARRIVAL_TIME + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                DayRecord.LocationVisit visit = new DayRecord.LocationVisit(
                        cursor.getString(cursor.getColumnIndex(KEY_PLACE_NAME)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                );
                
                try {
                    visit.setArrivalTime(DATETIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex(KEY_ARRIVAL_TIME))));
                    String departureTime = cursor.getString(cursor.getColumnIndex(KEY_DEPARTURE_TIME));
                    if (departureTime != null) {
                        visit.setDepartureTime(DATETIME_FORMAT.parse(departureTime));
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing visit times", e);
                }
                
                visit.setDurationMinutes(cursor.getInt(cursor.getColumnIndex(KEY_DURATION_MINUTES)));
                visit.setPlaceCategory(cursor.getString(cursor.getColumnIndex(KEY_PLACE_CATEGORY)));
                visits.add(visit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }
    
    // Step data operations
    public void insertStepData(String date, int steps, String stepType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put(KEY_TIMESTAMP, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_STEPS, steps);
        values.put(KEY_STEP_TYPE, stepType);
        
        db.insert(TABLE_STEP_DATA, null, values);
    }
    
    // Weather data operations
    public void insertWeatherData(String date, String location, float temperature, float humidity, 
                                  String condition, int weatherCode, float windSpeed, float precipitation, float cloudCover) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put(KEY_TIMESTAMP, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_LOCATION, location);
        values.put(KEY_TEMPERATURE, temperature);
        values.put(KEY_HUMIDITY, humidity);
        values.put(KEY_WEATHER_CONDITION, condition);
        values.put(KEY_WEATHER_CODE, weatherCode);
        values.put(KEY_WIND_SPEED, windSpeed);
        values.put(KEY_PRECIPITATION, precipitation);
        values.put(KEY_CLOUD_COVER, cloudCover);
        
        db.insert(TABLE_WEATHER_DATA, null, values);
    }
    
    // Battery data operations
    public void insertBatteryData(String date, int batteryLevel, boolean isCharging, String batteryHealth) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put(KEY_TIMESTAMP, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_BATTERY_LEVEL, batteryLevel);
        values.put(KEY_IS_CHARGING, isCharging ? 1 : 0);
        values.put(KEY_BATTERY_HEALTH, batteryHealth);
        
        db.insert(TABLE_BATTERY_DATA, null, values);
    }
    
    // Screen time operations
    public void insertScreenTimeData(String date, String appName, String packageName, int usageTime, String lastUsed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put(KEY_APP_NAME, appName);
        values.put(KEY_PACKAGE_NAME, packageName);
        values.put(KEY_USAGE_TIME, usageTime);
        values.put(KEY_LAST_USED, lastUsed);
        
        db.insert(TABLE_SCREEN_TIME, null, values);
    }
    
    // Utility methods
    public void deleteOldRecords(int daysToKeep) {
        SQLiteDatabase db = this.getWritableDatabase();
        String dateLimit = DATE_FORMAT.format(new Date(System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000)));
        
        db.delete(TABLE_DAY_RECORDS, KEY_DATE + " < ?", new String[]{dateLimit});
        db.delete(TABLE_STEP_DATA, KEY_DATE + " < ?", new String[]{dateLimit});
        db.delete(TABLE_WEATHER_DATA, KEY_DATE + " < ?", new String[]{dateLimit});
        db.delete(TABLE_BATTERY_DATA, KEY_DATE + " < ?", new String[]{dateLimit});
        db.delete(TABLE_SCREEN_TIME, KEY_DATE + " < ?", new String[]{dateLimit});
        db.delete(TABLE_PHOTO_METADATA, KEY_DATE + " < ?", new String[]{dateLimit});
    }
    
    // Photo Metadata CRUD operations
    public long insertPhotoMetadata(PhotoMetadata photoMetadata) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_PHOTO_URI, photoMetadata.getPhotoUri());
        values.put(KEY_PHOTO_PATH, photoMetadata.getPhotoPath());
        values.put(KEY_DATE, DATE_FORMAT.format(photoMetadata.getDateTaken()));
        values.put(KEY_DATE_TAKEN, photoMetadata.getDateTaken() != null ? 
                DATETIME_FORMAT.format(photoMetadata.getDateTaken()) : null);
        values.put(KEY_DATE_MODIFIED, photoMetadata.getDateModified() != null ? 
                DATETIME_FORMAT.format(photoMetadata.getDateModified()) : null);
        values.put(KEY_LATITUDE, photoMetadata.getLatitude());
        values.put(KEY_LONGITUDE, photoMetadata.getLongitude());
        values.put(KEY_ALTITUDE, photoMetadata.getAltitude());
        values.put(KEY_LOCATION_NAME, photoMetadata.getLocationName());
        values.put(KEY_CAMERA_MAKE, photoMetadata.getCameraMake());
        values.put(KEY_CAMERA_MODEL, photoMetadata.getCameraModel());
        values.put(KEY_IMAGE_WIDTH, photoMetadata.getImageWidth());
        values.put(KEY_IMAGE_HEIGHT, photoMetadata.getImageHeight());
        values.put(KEY_ORIENTATION, photoMetadata.getOrientation());
        values.put(KEY_FLASH_MODE, photoMetadata.getFlashMode());
        values.put(KEY_FOCAL_LENGTH, photoMetadata.getFocalLength());
        values.put(KEY_APERTURE, photoMetadata.getAperture());
        values.put(KEY_SHUTTER_SPEED, photoMetadata.getShutterSpeed());
        values.put(KEY_ISO, photoMetadata.getIso());
        values.put(KEY_WHITE_BALANCE, photoMetadata.getWhiteBalance());
        values.put(KEY_FILE_SIZE, photoMetadata.getFileSize());
        values.put(KEY_MIME_TYPE, photoMetadata.getMimeType());
        values.put(KEY_HAS_LOCATION_DATA, photoMetadata.hasLocationData() ? 1 : 0);
        values.put(KEY_ACTIVITY_TYPE, photoMetadata.getActivityType());
        values.put(KEY_ACTIVITY_SCORE, photoMetadata.getActivityScore());
        values.put(KEY_TIME_OF_DAY, photoMetadata.getTimeOfDay());
        values.put(KEY_SEASON, photoMetadata.getSeason());
        values.put(KEY_IS_OUTDOOR, photoMetadata.isOutdoor() ? 1 : 0);
        values.put(KEY_HAS_PEOPLE, photoMetadata.hasPeople() ? 1 : 0);
        values.put(KEY_DOMINANT_COLORS, photoMetadata.getDominantColors());
        values.put(KEY_IS_PROCESSED, photoMetadata.isProcessed() ? 1 : 0);
        values.put(KEY_PROCESSING_ERROR, photoMetadata.getProcessingError());
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(photoMetadata.getCreatedAt()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(photoMetadata.getUpdatedAt()));
        
        long id = db.insertOrThrow(TABLE_PHOTO_METADATA, null, values);
        photoMetadata.setId(id);
        return id;
    }
    
    public List<PhotoMetadata> getPhotoMetadataForDate(String date) {
        List<PhotoMetadata> photoMetadataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PHOTO_METADATA, null, KEY_DATE + "=?", 
                new String[]{date}, null, null, KEY_DATE_TAKEN + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                PhotoMetadata photoMetadata = cursorToPhotoMetadata(cursor);
                photoMetadataList.add(photoMetadata);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return photoMetadataList;
    }
    
    public boolean isPhotoMetadataExists(String photoPath) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PHOTO_METADATA, new String[]{KEY_ID}, 
                KEY_PHOTO_PATH + "=?", new String[]{photoPath}, null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    
    public int getPhotoCountForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PHOTO_METADATA, new String[]{"COUNT(*)"}, 
                KEY_DATE + "=?", new String[]{date}, null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    public Map<String, Integer> getPhotoCountsByTimeOfDay() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PHOTO_METADATA, 
                new String[]{KEY_TIME_OF_DAY, "COUNT(*)"}, 
                KEY_TIME_OF_DAY + " IS NOT NULL", null, 
                KEY_TIME_OF_DAY, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                String timeOfDay = cursor.getString(0);
                int count = cursor.getInt(1);
                counts.put(timeOfDay, count);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return counts;
    }
    
    public Map<String, Integer> getPhotoCountsByDayOfWeek() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // This would need to be implemented with date manipulation
        // For now, returning empty map
        return counts;
    }
    
    public Map<String, Integer> getPhotoCountsByActivityType() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PHOTO_METADATA, 
                new String[]{KEY_ACTIVITY_TYPE, "COUNT(*)"}, 
                KEY_ACTIVITY_TYPE + " IS NOT NULL", null, 
                KEY_ACTIVITY_TYPE, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                String activityType = cursor.getString(0);
                int count = cursor.getInt(1);
                counts.put(activityType, count);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return counts;
    }
    
    public void deleteOldPhotoMetadata(int daysToKeep) {
        SQLiteDatabase db = this.getWritableDatabase();
        String dateLimit = DATE_FORMAT.format(new Date(System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000)));
        
        db.delete(TABLE_PHOTO_METADATA, KEY_DATE + " < ?", new String[]{dateLimit});
    }
    
    private PhotoMetadata cursorToPhotoMetadata(Cursor cursor) {
        PhotoMetadata photoMetadata = new PhotoMetadata();
        photoMetadata.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
        photoMetadata.setPhotoUri(cursor.getString(cursor.getColumnIndex(KEY_PHOTO_URI)));
        photoMetadata.setPhotoPath(cursor.getString(cursor.getColumnIndex(KEY_PHOTO_PATH)));
        
        try {
            String dateTaken = cursor.getString(cursor.getColumnIndex(KEY_DATE_TAKEN));
            if (dateTaken != null) {
                photoMetadata.setDateTaken(DATETIME_FORMAT.parse(dateTaken));
            }
            
            String dateModified = cursor.getString(cursor.getColumnIndex(KEY_DATE_MODIFIED));
            if (dateModified != null) {
                photoMetadata.setDateModified(DATETIME_FORMAT.parse(dateModified));
            }
            
            photoMetadata.setCreatedAt(DATETIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT))));
            photoMetadata.setUpdatedAt(DATETIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex(KEY_UPDATED_AT))));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing photo metadata dates", e);
        }
        
        photoMetadata.setLatitude(cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)));
        photoMetadata.setLongitude(cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)));
        photoMetadata.setAltitude(cursor.getFloat(cursor.getColumnIndex(KEY_ALTITUDE)));
        photoMetadata.setLocationName(cursor.getString(cursor.getColumnIndex(KEY_LOCATION_NAME)));
        photoMetadata.setCameraMake(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MAKE)));
        photoMetadata.setCameraModel(cursor.getString(cursor.getColumnIndex(KEY_CAMERA_MODEL)));
        photoMetadata.setImageWidth(cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_WIDTH)));
        photoMetadata.setImageHeight(cursor.getInt(cursor.getColumnIndex(KEY_IMAGE_HEIGHT)));
        photoMetadata.setOrientation(cursor.getInt(cursor.getColumnIndex(KEY_ORIENTATION)));
        photoMetadata.setFlashMode(cursor.getString(cursor.getColumnIndex(KEY_FLASH_MODE)));
        photoMetadata.setFocalLength(cursor.getString(cursor.getColumnIndex(KEY_FOCAL_LENGTH)));
        photoMetadata.setAperture(cursor.getString(cursor.getColumnIndex(KEY_APERTURE)));
        photoMetadata.setShutterSpeed(cursor.getString(cursor.getColumnIndex(KEY_SHUTTER_SPEED)));
        photoMetadata.setIso(cursor.getString(cursor.getColumnIndex(KEY_ISO)));
        photoMetadata.setWhiteBalance(cursor.getString(cursor.getColumnIndex(KEY_WHITE_BALANCE)));
        photoMetadata.setFileSize(cursor.getLong(cursor.getColumnIndex(KEY_FILE_SIZE)));
        photoMetadata.setMimeType(cursor.getString(cursor.getColumnIndex(KEY_MIME_TYPE)));
        photoMetadata.setHasLocationData(cursor.getInt(cursor.getColumnIndex(KEY_HAS_LOCATION_DATA)) == 1);
        photoMetadata.setActivityType(cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY_TYPE)));
        photoMetadata.setActivityScore(cursor.getInt(cursor.getColumnIndex(KEY_ACTIVITY_SCORE)));
        photoMetadata.setTimeOfDay(cursor.getString(cursor.getColumnIndex(KEY_TIME_OF_DAY)));
        photoMetadata.setSeason(cursor.getString(cursor.getColumnIndex(KEY_SEASON)));
        photoMetadata.setOutdoor(cursor.getInt(cursor.getColumnIndex(KEY_IS_OUTDOOR)) == 1);
        photoMetadata.setHasPeople(cursor.getInt(cursor.getColumnIndex(KEY_HAS_PEOPLE)) == 1);
        photoMetadata.setDominantColors(cursor.getString(cursor.getColumnIndex(KEY_DOMINANT_COLORS)));
        photoMetadata.setProcessed(cursor.getInt(cursor.getColumnIndex(KEY_IS_PROCESSED)) == 1);
        photoMetadata.setProcessingError(cursor.getString(cursor.getColumnIndex(KEY_PROCESSING_ERROR)));
        
        return photoMetadata;
    }
    
    public void close() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}