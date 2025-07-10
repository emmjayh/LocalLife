package com.locallife.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.locallife.model.DayRecord;
import com.locallife.model.PhotoMetadata;
import com.locallife.model.MediaConsumption;
import com.locallife.model.Goal;
import com.locallife.model.Achievement;
import com.locallife.model.UserLevel;

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
    private static final int DATABASE_VERSION = 8;
    
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
    private static final String TABLE_AIR_QUALITY = "air_quality";
    private static final String TABLE_MOON_PHASE = "moon_phase";
    private static final String TABLE_UV_INDEX = "uv_index";
    private static final String TABLE_DAYLIGHT_DATA = "daylight_data";
    private static final String TABLE_MEDIA_CONSUMPTION = "media_consumption";
    private static final String TABLE_GOALS = "goals";
    private static final String TABLE_ACHIEVEMENTS = "achievements";
    private static final String TABLE_USER_LEVEL = "user_level";
    
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
    
    // Environmental data columns
    // Air quality columns
    private static final String KEY_AIR_QUALITY_INDEX = "air_quality_index";
    private static final String KEY_AIR_QUALITY_LEVEL = "air_quality_level";
    private static final String KEY_PM25_LEVEL = "pm25_level";
    private static final String KEY_PM10_LEVEL = "pm10_level";
    private static final String KEY_NO2_LEVEL = "no2_level";
    private static final String KEY_O3_LEVEL = "o3_level";
    private static final String KEY_CO_LEVEL = "co_level";
    private static final String KEY_AIR_QUALITY_IMPACT = "air_quality_impact";
    
    // Moon phase columns
    private static final String KEY_MOON_PHASE = "moon_phase";
    private static final String KEY_MOON_PHASE_INDEX = "moon_phase_index";
    private static final String KEY_MOON_ILLUMINATION = "moon_illumination";
    private static final String KEY_MOON_AGE = "moon_age";
    private static final String KEY_MOON_DISTANCE = "moon_distance";
    private static final String KEY_ZODIAC_SIGN = "zodiac_sign";
    private static final String KEY_IS_SUPERMOON = "is_supermoon";
    private static final String KEY_MOON_ACTIVITY_IMPACT = "moon_activity_impact";
    
    // UV index columns
    private static final String KEY_UV_INDEX = "uv_index";
    private static final String KEY_UV_MAX = "uv_max";
    private static final String KEY_UV_CATEGORY = "uv_category";
    private static final String KEY_BURN_TIME = "burn_time";
    private static final String KEY_TAN_TIME = "tan_time";
    private static final String KEY_PEAK_UV_TIME = "peak_uv_time";
    private static final String KEY_VITAMIN_D_TIME = "vitamin_d_time";
    private static final String KEY_UV_ACTIVITY_IMPACT = "uv_activity_impact";
    
    // Daylight data columns
    private static final String KEY_SUNRISE_TIME = "sunrise_time";
    private static final String KEY_SUNSET_TIME = "sunset_time";
    private static final String KEY_SOLAR_NOON_TIME = "solar_noon_time";
    private static final String KEY_DAY_LENGTH = "day_length";
    private static final String KEY_NIGHT_LENGTH = "night_length";
    private static final String KEY_DAYLIGHT_CHANGE = "daylight_change";
    private static final String KEY_CIRCADIAN_SCORE = "circadian_score";
    
    // Additional weather columns
    private static final String KEY_ATMOSPHERIC_PRESSURE = "atmospheric_pressure";
    private static final String KEY_CLOUD_COVER_PERCENTAGE = "cloud_cover_percentage";
    private static final String KEY_VISIBILITY = "visibility";
    private static final String KEY_WIND_DIRECTION = "wind_direction";
    private static final String KEY_ALTITUDE = "altitude";
    
    // Goal table columns
    private static final String KEY_GOAL_ID = "goal_id";
    private static final String KEY_GOAL_TITLE = "title";
    private static final String KEY_GOAL_DESCRIPTION = "description";
    private static final String KEY_GOAL_TYPE = "type";
    private static final String KEY_GOAL_CATEGORY = "category";
    private static final String KEY_GOAL_TARGET_VALUE = "target_value";
    private static final String KEY_GOAL_TARGET_UNIT = "target_unit";
    private static final String KEY_GOAL_CURRENT_VALUE = "current_value";
    private static final String KEY_GOAL_FREQUENCY = "frequency";
    private static final String KEY_GOAL_START_DATE = "start_date";
    private static final String KEY_GOAL_END_DATE = "end_date";
    private static final String KEY_GOAL_IS_ACTIVE = "is_active";
    private static final String KEY_GOAL_IS_COMPLETED = "is_completed";
    private static final String KEY_GOAL_STREAK_COUNT = "streak_count";
    private static final String KEY_GOAL_TOTAL_COMPLETIONS = "total_completions";
    private static final String KEY_GOAL_BEST_VALUE = "best_value";
    private static final String KEY_GOAL_LAST_COMPLETED_DATE = "last_completed_date";
    private static final String KEY_GOAL_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String KEY_GOAL_PRIORITY = "priority";
    private static final String KEY_GOAL_COLOR = "color";
    
    // Achievement table columns
    private static final String KEY_ACHIEVEMENT_ID = "achievement_id";
    private static final String KEY_ACHIEVEMENT_TITLE = "title";
    private static final String KEY_ACHIEVEMENT_DESCRIPTION = "description";
    private static final String KEY_ACHIEVEMENT_TYPE = "type";
    private static final String KEY_ACHIEVEMENT_CATEGORY = "category";
    private static final String KEY_ACHIEVEMENT_TIER = "tier";
    private static final String KEY_ACHIEVEMENT_ICON_NAME = "icon_name";
    private static final String KEY_ACHIEVEMENT_BADGE_COLOR = "badge_color";
    private static final String KEY_ACHIEVEMENT_TARGET_VALUE = "target_value";
    private static final String KEY_ACHIEVEMENT_TARGET_UNIT = "target_unit";
    private static final String KEY_ACHIEVEMENT_CURRENT_PROGRESS = "current_progress";
    private static final String KEY_ACHIEVEMENT_IS_UNLOCKED = "is_unlocked";
    private static final String KEY_ACHIEVEMENT_UNLOCKED_AT = "unlocked_at";
    private static final String KEY_ACHIEVEMENT_REQUIREMENTS = "requirements";
    private static final String KEY_ACHIEVEMENT_POINTS_VALUE = "points_value";
    private static final String KEY_ACHIEVEMENT_IS_HIDDEN = "is_hidden";
    private static final String KEY_ACHIEVEMENT_STREAK_REQUIREMENT = "streak_requirement";
    private static final String KEY_ACHIEVEMENT_CUSTOM_CONDITION = "custom_condition";
    
    // UserLevel table columns
    private static final String KEY_USER_LEVEL_ID = "level_id";
    private static final String KEY_USER_CURRENT_LEVEL = "current_level";
    private static final String KEY_USER_CURRENT_XP = "current_xp";
    private static final String KEY_USER_TOTAL_XP = "total_xp";
    private static final String KEY_USER_LAST_LEVEL_UP = "last_level_up";
    private static final String KEY_USER_CURRENT_TITLE = "current_title";
    private static final String KEY_USER_ACHIEVEMENTS_UNLOCKED = "achievements_unlocked";
    private static final String KEY_USER_STREAKS_COMPLETED = "streaks_completed";
    private static final String KEY_USER_GOALS_COMPLETED = "goals_completed";
    
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AIR_QUALITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOON_PHASE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UV_INDEX);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYLIGHT_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA_CONSUMPTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACHIEVEMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_LEVEL);
        
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
                + "total_media_minutes INTEGER DEFAULT 0,"
                + "video_minutes INTEGER DEFAULT 0,"
                + "audio_minutes INTEGER DEFAULT 0,"
                + "binge_watching_minutes INTEGER DEFAULT 0,"
                + "unique_media_platforms INTEGER DEFAULT 0,"
                + "media_consumption_score REAL DEFAULT 0,"
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
        
        // Air Quality table
        String CREATE_AIR_QUALITY_TABLE = "CREATE TABLE " + TABLE_AIR_QUALITY + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " TEXT NOT NULL,"
                + KEY_LATITUDE + " REAL NOT NULL,"
                + KEY_LONGITUDE + " REAL NOT NULL,"
                + KEY_LOCATION + " TEXT,"
                + KEY_AIR_QUALITY_INDEX + " INTEGER,"
                + KEY_AIR_QUALITY_LEVEL + " TEXT,"
                + KEY_PM25_LEVEL + " REAL,"
                + KEY_PM10_LEVEL + " REAL,"
                + KEY_NO2_LEVEL + " REAL,"
                + KEY_O3_LEVEL + " REAL,"
                + KEY_CO_LEVEL + " REAL,"
                + KEY_AIR_QUALITY_IMPACT + " REAL"
                + ")";
        
        // Moon Phase table
        String CREATE_MOON_PHASE_TABLE = "CREATE TABLE " + TABLE_MOON_PHASE + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " TEXT NOT NULL,"
                + KEY_MOON_PHASE + " TEXT,"
                + KEY_MOON_PHASE_INDEX + " INTEGER,"
                + KEY_MOON_ILLUMINATION + " REAL,"
                + KEY_MOON_AGE + " REAL,"
                + KEY_MOON_DISTANCE + " REAL,"
                + KEY_ZODIAC_SIGN + " TEXT,"
                + KEY_IS_SUPERMOON + " INTEGER DEFAULT 0,"
                + KEY_MOON_ACTIVITY_IMPACT + " REAL"
                + ")";
        
        // UV Index table
        String CREATE_UV_INDEX_TABLE = "CREATE TABLE " + TABLE_UV_INDEX + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " TEXT NOT NULL,"
                + KEY_LATITUDE + " REAL NOT NULL,"
                + KEY_LONGITUDE + " REAL NOT NULL,"
                + KEY_UV_INDEX + " REAL,"
                + KEY_UV_MAX + " REAL,"
                + KEY_UV_CATEGORY + " TEXT,"
                + KEY_BURN_TIME + " INTEGER,"
                + KEY_TAN_TIME + " INTEGER,"
                + KEY_PEAK_UV_TIME + " TEXT,"
                + KEY_VITAMIN_D_TIME + " REAL,"
                + KEY_UV_ACTIVITY_IMPACT + " REAL"
                + ")";
        
        // Daylight Data table
        String CREATE_DAYLIGHT_DATA_TABLE = "CREATE TABLE " + TABLE_DAYLIGHT_DATA + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + KEY_TIMESTAMP + " TEXT NOT NULL,"
                + KEY_LATITUDE + " REAL NOT NULL,"
                + KEY_LONGITUDE + " REAL NOT NULL,"
                + KEY_SUNRISE_TIME + " TEXT,"
                + KEY_SUNSET_TIME + " TEXT,"
                + KEY_SOLAR_NOON_TIME + " TEXT,"
                + KEY_DAY_LENGTH + " INTEGER,"
                + KEY_NIGHT_LENGTH + " INTEGER,"
                + KEY_SEASON + " TEXT,"
                + KEY_DAYLIGHT_CHANGE + " REAL,"
                + KEY_CIRCADIAN_SCORE + " REAL"
                + ")";
        
        db.execSQL(CREATE_DAY_RECORDS_TABLE);
        db.execSQL(CREATE_LOCATION_VISITS_TABLE);
        db.execSQL(CREATE_STEP_DATA_TABLE);
        db.execSQL(CREATE_WEATHER_DATA_TABLE);
        db.execSQL(CREATE_BATTERY_DATA_TABLE);
        db.execSQL(CREATE_SCREEN_TIME_TABLE);
        db.execSQL(CREATE_PHOTO_METADATA_TABLE);
        db.execSQL(CREATE_AIR_QUALITY_TABLE);
        db.execSQL(CREATE_MOON_PHASE_TABLE);
        db.execSQL(CREATE_UV_INDEX_TABLE);
        db.execSQL(CREATE_DAYLIGHT_DATA_TABLE);
        
        // Media Consumption table
        String CREATE_MEDIA_CONSUMPTION_TABLE = "CREATE TABLE " + TABLE_MEDIA_CONSUMPTION + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_DATE + " TEXT NOT NULL,"
                + "media_type TEXT NOT NULL,"
                + "title TEXT NOT NULL,"
                + "platform TEXT,"
                + "duration_minutes INTEGER DEFAULT 0,"
                + "genre TEXT,"
                + "source TEXT,"
                + "start_time TEXT,"
                + "end_time TEXT,"
                + "metadata TEXT,"
                + "show_id TEXT,"
                + "season INTEGER DEFAULT 0,"
                + "episode INTEGER DEFAULT 0,"
                + "channel TEXT,"
                + "director TEXT,"
                + "artist TEXT,"
                + "album TEXT,"
                + "is_rewatch INTEGER DEFAULT 0,"
                + "rating INTEGER DEFAULT 0,"
                + "notes TEXT,"
                + KEY_CREATED_AT + " TEXT NOT NULL,"
                + KEY_UPDATED_AT + " TEXT NOT NULL"
                + ")";
        
        db.execSQL(CREATE_MEDIA_CONSUMPTION_TABLE);
        
        // Goals table
        String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                + KEY_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_GOAL_TITLE + " TEXT NOT NULL,"
                + KEY_GOAL_DESCRIPTION + " TEXT,"
                + KEY_GOAL_TYPE + " TEXT NOT NULL,"
                + KEY_GOAL_CATEGORY + " TEXT NOT NULL,"
                + KEY_GOAL_TARGET_VALUE + " REAL NOT NULL,"
                + KEY_GOAL_TARGET_UNIT + " TEXT NOT NULL,"
                + KEY_GOAL_CURRENT_VALUE + " REAL DEFAULT 0,"
                + KEY_GOAL_FREQUENCY + " TEXT NOT NULL,"
                + KEY_GOAL_START_DATE + " TEXT NOT NULL,"
                + KEY_GOAL_END_DATE + " TEXT,"
                + KEY_GOAL_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + KEY_GOAL_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_GOAL_STREAK_COUNT + " INTEGER DEFAULT 0,"
                + KEY_GOAL_TOTAL_COMPLETIONS + " INTEGER DEFAULT 0,"
                + KEY_GOAL_BEST_VALUE + " REAL DEFAULT 0,"
                + KEY_GOAL_LAST_COMPLETED_DATE + " TEXT,"
                + KEY_GOAL_MOTIVATIONAL_MESSAGE + " TEXT,"
                + KEY_GOAL_PRIORITY + " INTEGER DEFAULT 3,"
                + KEY_GOAL_COLOR + " TEXT DEFAULT '#4CAF50',"
                + KEY_CREATED_AT + " TEXT NOT NULL,"
                + KEY_UPDATED_AT + " TEXT NOT NULL"
                + ")";
        
        db.execSQL(CREATE_GOALS_TABLE);
        
        // Achievements table
        String CREATE_ACHIEVEMENTS_TABLE = "CREATE TABLE " + TABLE_ACHIEVEMENTS + "("
                + KEY_ACHIEVEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ACHIEVEMENT_TITLE + " TEXT NOT NULL,"
                + KEY_ACHIEVEMENT_DESCRIPTION + " TEXT,"
                + KEY_ACHIEVEMENT_TYPE + " TEXT NOT NULL,"
                + KEY_ACHIEVEMENT_CATEGORY + " TEXT NOT NULL,"
                + KEY_ACHIEVEMENT_TIER + " TEXT NOT NULL,"
                + KEY_ACHIEVEMENT_ICON_NAME + " TEXT,"
                + KEY_ACHIEVEMENT_BADGE_COLOR + " TEXT,"
                + KEY_ACHIEVEMENT_TARGET_VALUE + " INTEGER DEFAULT 0,"
                + KEY_ACHIEVEMENT_TARGET_UNIT + " TEXT,"
                + KEY_ACHIEVEMENT_CURRENT_PROGRESS + " INTEGER DEFAULT 0,"
                + KEY_ACHIEVEMENT_IS_UNLOCKED + " INTEGER DEFAULT 0,"
                + KEY_ACHIEVEMENT_UNLOCKED_AT + " TEXT,"
                + KEY_ACHIEVEMENT_REQUIREMENTS + " TEXT,"
                + KEY_ACHIEVEMENT_POINTS_VALUE + " INTEGER DEFAULT 10,"
                + KEY_ACHIEVEMENT_IS_HIDDEN + " INTEGER DEFAULT 0,"
                + KEY_ACHIEVEMENT_STREAK_REQUIREMENT + " INTEGER DEFAULT 0,"
                + KEY_ACHIEVEMENT_CUSTOM_CONDITION + " TEXT,"
                + KEY_CREATED_AT + " TEXT NOT NULL"
                + ")";
        
        db.execSQL(CREATE_ACHIEVEMENTS_TABLE);
        
        // UserLevel table
        String CREATE_USER_LEVEL_TABLE = "CREATE TABLE " + TABLE_USER_LEVEL + "("
                + KEY_USER_LEVEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_CURRENT_LEVEL + " INTEGER DEFAULT 1,"
                + KEY_USER_CURRENT_XP + " INTEGER DEFAULT 0,"
                + KEY_USER_TOTAL_XP + " INTEGER DEFAULT 0,"
                + KEY_USER_LAST_LEVEL_UP + " TEXT,"
                + KEY_USER_CURRENT_TITLE + " TEXT DEFAULT 'Beginner',"
                + KEY_USER_ACHIEVEMENTS_UNLOCKED + " INTEGER DEFAULT 0,"
                + KEY_USER_STREAKS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_USER_GOALS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_CREATED_AT + " TEXT NOT NULL,"
                + KEY_UPDATED_AT + " TEXT NOT NULL"
                + ")";
        
        db.execSQL(CREATE_USER_LEVEL_TABLE);
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
        
        // Environmental data indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_air_quality_date ON " + TABLE_AIR_QUALITY + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_moon_phase_date ON " + TABLE_MOON_PHASE + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_uv_index_date ON " + TABLE_UV_INDEX + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_media_consumption_date ON " + TABLE_MEDIA_CONSUMPTION + "(" + KEY_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_daylight_data_date ON " + TABLE_DAYLIGHT_DATA + "(" + KEY_DATE + ")");
        
        // Goal table indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_goals_active ON " + TABLE_GOALS + "(" + KEY_GOAL_IS_ACTIVE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_goals_category ON " + TABLE_GOALS + "(" + KEY_GOAL_CATEGORY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_goals_type ON " + TABLE_GOALS + "(" + KEY_GOAL_TYPE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_goals_frequency ON " + TABLE_GOALS + "(" + KEY_GOAL_FREQUENCY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_goals_start_date ON " + TABLE_GOALS + "(" + KEY_GOAL_START_DATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_goals_end_date ON " + TABLE_GOALS + "(" + KEY_GOAL_END_DATE + ")");
        
        // Achievement table indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_achievements_category ON " + TABLE_ACHIEVEMENTS + "(" + KEY_ACHIEVEMENT_CATEGORY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_achievements_type ON " + TABLE_ACHIEVEMENTS + "(" + KEY_ACHIEVEMENT_TYPE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_achievements_tier ON " + TABLE_ACHIEVEMENTS + "(" + KEY_ACHIEVEMENT_TIER + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_achievements_unlocked ON " + TABLE_ACHIEVEMENTS + "(" + KEY_ACHIEVEMENT_IS_UNLOCKED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_achievements_hidden ON " + TABLE_ACHIEVEMENTS + "(" + KEY_ACHIEVEMENT_IS_HIDDEN + ")");
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
        values.put("total_media_minutes", dayRecord.getTotalMediaMinutes());
        values.put("video_minutes", dayRecord.getVideoMinutes());
        values.put("audio_minutes", dayRecord.getAudioMinutes());
        values.put("binge_watching_minutes", dayRecord.getBingeWatchingMinutes());
        values.put("unique_media_platforms", dayRecord.getUniqueMediaPlatforms());
        values.put("media_consumption_score", dayRecord.getMediaConsumptionScore());
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
        values.put("total_media_minutes", dayRecord.getTotalMediaMinutes());
        values.put("video_minutes", dayRecord.getVideoMinutes());
        values.put("audio_minutes", dayRecord.getAudioMinutes());
        values.put("binge_watching_minutes", dayRecord.getBingeWatchingMinutes());
        values.put("unique_media_platforms", dayRecord.getUniqueMediaPlatforms());
        values.put("media_consumption_score", dayRecord.getMediaConsumptionScore());
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
        
        // Media consumption fields
        dayRecord.setTotalMediaMinutes(cursor.getInt(cursor.getColumnIndex("total_media_minutes")));
        dayRecord.setVideoMinutes(cursor.getInt(cursor.getColumnIndex("video_minutes")));
        dayRecord.setAudioMinutes(cursor.getInt(cursor.getColumnIndex("audio_minutes")));
        dayRecord.setBingeWatchingMinutes(cursor.getInt(cursor.getColumnIndex("binge_watching_minutes")));
        dayRecord.setUniqueMediaPlatforms(cursor.getInt(cursor.getColumnIndex("unique_media_platforms")));
        dayRecord.setMediaConsumptionScore(cursor.getFloat(cursor.getColumnIndex("media_consumption_score")));
        
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
    
    // Environmental Data CRUD Methods
    
    public long insertAirQualityData(String date, double latitude, double longitude, 
                                   int aqi, double pm25, double pm10, double no2, 
                                   double o3, double co, String source) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put("aqi", aqi);
        values.put("pm25", pm25);
        values.put("pm10", pm10);
        values.put("no2", no2);
        values.put("o3", o3);
        values.put("co", co);
        values.put("source", source);
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.insertOrThrow(TABLE_AIR_QUALITY, null, values);
    }
    
    public long insertMoonPhaseData(String date, String phase, double illumination, 
                                  int age, boolean isSupermoon, String activityRecommendation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put("phase", phase);
        values.put("illumination", illumination);
        values.put("age", age);
        values.put("is_supermoon", isSupermoon ? 1 : 0);
        values.put("activity_recommendation", activityRecommendation);
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.insertOrThrow(TABLE_MOON_PHASE, null, values);
    }
    
    public long insertUVIndexData(String date, double latitude, double longitude, 
                                double uvIndex, int burnTime, int tanTime, 
                                int vitaminDTime, String recommendation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put("uv_index", uvIndex);
        values.put("burn_time", burnTime);
        values.put("tan_time", tanTime);
        values.put("vitamin_d_time", vitaminDTime);
        values.put("recommendation", recommendation);
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.insertOrThrow(TABLE_UV_INDEX, null, values);
    }
    
    public long insertSunriseSunsetData(String date, double latitude, double longitude, 
                                      String sunrise, String sunset, int daylightDuration, 
                                      String civilTwilight, String nauticalTwilight, 
                                      String astronomicalTwilight, String circadianPhase) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, date);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put("sunrise", sunrise);
        values.put("sunset", sunset);
        values.put("daylight_duration", daylightDuration);
        values.put("civil_twilight", civilTwilight);
        values.put("nautical_twilight", nauticalTwilight);
        values.put("astronomical_twilight", astronomicalTwilight);
        values.put("circadian_phase", circadianPhase);
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.insertOrThrow(TABLE_DAYLIGHT_DATA, null, values);
    }
    
    // Environmental Data Retrieval Methods
    
    public void loadEnvironmentalData(DayRecord dayRecord, String date) {
        loadAirQualityData(dayRecord, date);
        loadMoonPhaseData(dayRecord, date);
        loadUVIndexData(dayRecord, date);
        loadDaylightData(dayRecord, date);
    }
    
    private void loadAirQualityData(DayRecord dayRecord, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_AIR_QUALITY, null, KEY_DATE + "=?", 
                new String[]{date}, null, null, null);
        
        if (cursor.moveToFirst()) {
            dayRecord.setAqi(cursor.getInt(cursor.getColumnIndex("aqi")));
            dayRecord.setPm25Level(cursor.getFloat(cursor.getColumnIndex("pm25")));
            dayRecord.setPm10Level(cursor.getFloat(cursor.getColumnIndex("pm10")));
            dayRecord.setNo2Level(cursor.getFloat(cursor.getColumnIndex("no2")));
            dayRecord.setO3Level(cursor.getFloat(cursor.getColumnIndex("o3")));
            dayRecord.setCoLevel(cursor.getFloat(cursor.getColumnIndex("co")));
            // Calculate activity impact based on AQI
            dayRecord.setAirQualityActivityImpact(calculateAirQualityImpact(dayRecord.getAqi()));
        }
        cursor.close();
    }
    
    private void loadMoonPhaseData(DayRecord dayRecord, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MOON_PHASE, null, KEY_DATE + "=?", 
                new String[]{date}, null, null, null);
        
        if (cursor.moveToFirst()) {
            dayRecord.setMoonPhase(cursor.getString(cursor.getColumnIndex("phase")));
            dayRecord.setMoonIllumination(cursor.getDouble(cursor.getColumnIndex("illumination")));
            dayRecord.setMoonAge(cursor.getInt(cursor.getColumnIndex("age")));
            dayRecord.setSupermoon(cursor.getInt(cursor.getColumnIndex("is_supermoon")) == 1);
            // Calculate activity impact based on moon phase
            dayRecord.setMoonPhaseActivityImpact(calculateMoonPhaseImpact(dayRecord.getMoonPhase()));
        }
        cursor.close();
    }
    
    private void loadUVIndexData(DayRecord dayRecord, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_UV_INDEX, null, KEY_DATE + "=?", 
                new String[]{date}, null, null, null);
        
        if (cursor.moveToFirst()) {
            dayRecord.setUvIndex(cursor.getDouble(cursor.getColumnIndex("uv_index")));
            dayRecord.setBurnTimeMinutes(cursor.getInt(cursor.getColumnIndex("burn_time")));
            dayRecord.setTanTimeMinutes(cursor.getInt(cursor.getColumnIndex("tan_time")));
            dayRecord.setVitaminDTimeMinutes(cursor.getInt(cursor.getColumnIndex("vitamin_d_time")));
            // Calculate activity impact based on UV index
            dayRecord.setUvActivityImpact(calculateUVImpact(dayRecord.getUvIndex()));
        }
        cursor.close();
    }
    
    private void loadDaylightData(DayRecord dayRecord, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAYLIGHT_DATA, null, KEY_DATE + "=?", 
                new String[]{date}, null, null, null);
        
        if (cursor.moveToFirst()) {
            dayRecord.setSunriseTime(cursor.getString(cursor.getColumnIndex("sunrise")));
            dayRecord.setSunsetTime(cursor.getString(cursor.getColumnIndex("sunset")));
            dayRecord.setDayLengthMinutes(cursor.getLong(cursor.getColumnIndex("daylight_duration")));
            dayRecord.setCurrentCircadianPhase(cursor.getString(cursor.getColumnIndex("circadian_phase")));
            // Calculate activity impact based on circadian rhythm
            dayRecord.setCircadianActivityScore(calculateCircadianImpact(dayRecord.getCurrentCircadianPhase()));
        }
        cursor.close();
    }
    
    // Helper methods for calculating environmental impacts
    private float calculateAirQualityImpact(int aqi) {
        if (aqi <= 50) return 1.0f;      // Good
        if (aqi <= 100) return 0.9f;     // Moderate
        if (aqi <= 150) return 0.8f;     // Unhealthy for sensitive groups
        if (aqi <= 200) return 0.7f;     // Unhealthy
        if (aqi <= 300) return 0.5f;     // Very unhealthy
        return 0.3f;                     // Hazardous
    }
    
    private float calculateMoonPhaseImpact(String moonPhase) {
        if (moonPhase == null) return 1.0f;
        switch (moonPhase.toLowerCase()) {
            case "new moon": return 0.9f;
            case "waxing crescent": return 0.95f;
            case "first quarter": return 1.0f;
            case "waxing gibbous": return 1.05f;
            case "full moon": return 1.1f;
            case "waning gibbous": return 1.05f;
            case "last quarter": return 1.0f;
            case "waning crescent": return 0.95f;
            default: return 1.0f;
        }
    }
    
    private float calculateUVImpact(double uvIndex) {
        if (uvIndex <= 2) return 0.8f;   // Low
        if (uvIndex <= 5) return 0.9f;   // Moderate
        if (uvIndex <= 7) return 1.0f;   // High (optimal)
        if (uvIndex <= 10) return 0.8f;  // Very high
        return 0.6f;                     // Extreme
    }
    
    private float calculateCircadianImpact(String phase) {
        if (phase == null) return 1.0f;
        switch (phase.toLowerCase()) {
            case "dawn": return 1.1f;
            case "morning": return 1.2f;
            case "midday": return 1.0f;
            case "afternoon": return 1.1f;
            case "evening": return 0.9f;
            case "night": return 0.7f;
            default: return 1.0f;
        }
    }
    
    // Media Consumption CRUD operations
    public long insertMediaConsumption(MediaConsumption media) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_DATE, media.getDate());
        values.put("media_type", media.getMediaType());
        values.put("title", media.getTitle());
        values.put("platform", media.getPlatform());
        values.put("duration_minutes", media.getDurationMinutes());
        values.put("genre", media.getGenre());
        values.put("source", media.getSource());
        values.put("start_time", media.getStartTime() != null ? DATETIME_FORMAT.format(media.getStartTime()) : null);
        values.put("end_time", media.getEndTime() != null ? DATETIME_FORMAT.format(media.getEndTime()) : null);
        values.put("metadata", media.getMetadata());
        values.put("show_id", media.getShowId());
        values.put("season", media.getSeason());
        values.put("episode", media.getEpisode());
        values.put("channel", media.getChannel());
        values.put("director", media.getDirector());
        values.put("artist", media.getArtist());
        values.put("album", media.getAlbum());
        values.put("is_rewatch", media.isRewatch() ? 1 : 0);
        values.put("rating", media.getRating());
        values.put("notes", media.getNotes());
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(media.getCreatedAt()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(media.getUpdatedAt()));
        
        long id = db.insert(TABLE_MEDIA_CONSUMPTION, null, values);
        media.setId((int) id);
        
        return id;
    }
    
    public int updateMediaConsumption(MediaConsumption media) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("duration_minutes", media.getDurationMinutes());
        values.put("genre", media.getGenre());
        values.put("end_time", media.getEndTime() != null ? DATETIME_FORMAT.format(media.getEndTime()) : null);
        values.put("metadata", media.getMetadata());
        values.put("is_rewatch", media.isRewatch() ? 1 : 0);
        values.put("rating", media.getRating());
        values.put("notes", media.getNotes());
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(media.getUpdatedAt()));
        
        return db.update(TABLE_MEDIA_CONSUMPTION, values, KEY_ID + " = ?", 
                new String[]{String.valueOf(media.getId())});
    }
    
    public List<MediaConsumption> getMediaConsumptionForDate(String date) {
        List<MediaConsumption> mediaList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MEDIA_CONSUMPTION, null, KEY_DATE + " = ?", 
                new String[]{date}, null, null, "start_time DESC");
        
        if (cursor.moveToFirst()) {
            do {
                MediaConsumption media = new MediaConsumption();
                media.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                media.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                media.setMediaType(cursor.getString(cursor.getColumnIndex("media_type")));
                media.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                media.setPlatform(cursor.getString(cursor.getColumnIndex("platform")));
                media.setDurationMinutes(cursor.getInt(cursor.getColumnIndex("duration_minutes")));
                media.setGenre(cursor.getString(cursor.getColumnIndex("genre")));
                media.setSource(cursor.getString(cursor.getColumnIndex("source")));
                
                // Parse dates
                try {
                    String startTimeStr = cursor.getString(cursor.getColumnIndex("start_time"));
                    if (startTimeStr != null) {
                        media.setStartTime(DATETIME_FORMAT.parse(startTimeStr));
                    }
                    
                    String endTimeStr = cursor.getString(cursor.getColumnIndex("end_time"));
                    if (endTimeStr != null) {
                        media.setEndTime(DATETIME_FORMAT.parse(endTimeStr));
                    }
                    
                    String createdAtStr = cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT));
                    if (createdAtStr != null) {
                        media.setCreatedAt(DATETIME_FORMAT.parse(createdAtStr));
                    }
                    
                    String updatedAtStr = cursor.getString(cursor.getColumnIndex(KEY_UPDATED_AT));
                    if (updatedAtStr != null) {
                        media.setUpdatedAt(DATETIME_FORMAT.parse(updatedAtStr));
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing media consumption dates", e);
                }
                
                media.setMetadata(cursor.getString(cursor.getColumnIndex("metadata")));
                media.setShowId(cursor.getString(cursor.getColumnIndex("show_id")));
                media.setSeason(cursor.getInt(cursor.getColumnIndex("season")));
                media.setEpisode(cursor.getInt(cursor.getColumnIndex("episode")));
                media.setChannel(cursor.getString(cursor.getColumnIndex("channel")));
                media.setDirector(cursor.getString(cursor.getColumnIndex("director")));
                media.setArtist(cursor.getString(cursor.getColumnIndex("artist")));
                media.setAlbum(cursor.getString(cursor.getColumnIndex("album")));
                media.setRewatch(cursor.getInt(cursor.getColumnIndex("is_rewatch")) == 1);
                media.setRating(cursor.getInt(cursor.getColumnIndex("rating")));
                media.setNotes(cursor.getString(cursor.getColumnIndex("notes")));
                
                mediaList.add(media);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return mediaList;
    }
    
    public MediaConsumption getExistingMediaRecord(String date, String packageName) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MEDIA_CONSUMPTION, null, 
                KEY_DATE + " = ? AND metadata LIKE ?", 
                new String[]{date, "%" + packageName + "%"}, 
                null, null, null);
        
        MediaConsumption media = null;
        if (cursor.moveToFirst()) {
            media = new MediaConsumption();
            media.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
            media.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
            media.setMediaType(cursor.getString(cursor.getColumnIndex("media_type")));
            media.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            media.setPlatform(cursor.getString(cursor.getColumnIndex("platform")));
            media.setDurationMinutes(cursor.getInt(cursor.getColumnIndex("duration_minutes")));
            media.setGenre(cursor.getString(cursor.getColumnIndex("genre")));
            media.setSource(cursor.getString(cursor.getColumnIndex("source")));
            media.setMetadata(cursor.getString(cursor.getColumnIndex("metadata")));
            
            try {
                String updatedAtStr = cursor.getString(cursor.getColumnIndex(KEY_UPDATED_AT));
                if (updatedAtStr != null) {
                    media.setUpdatedAt(DATETIME_FORMAT.parse(updatedAtStr));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing media consumption date", e);
            }
        }
        
        cursor.close();
        return media;
    }
    
    public boolean checkIfRewatch(String showTitle, int season, int episode) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MEDIA_CONSUMPTION, null, 
                "title = ? AND season = ? AND episode = ?", 
                new String[]{showTitle, String.valueOf(season), String.valueOf(episode)}, 
                null, null, null);
        
        boolean isRewatch = cursor.getCount() > 0;
        cursor.close();
        
        return isRewatch;
    }
    
    public List<String> getRecentlyWatchedShows() {
        List<String> shows = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MEDIA_CONSUMPTION, 
                new String[]{"DISTINCT title"}, 
                "media_type = ? AND " + KEY_CREATED_AT + " >= date('now', '-30 days')", 
                new String[]{"tv"}, 
                null, null, 
                KEY_CREATED_AT + " DESC", 
                "10");
        
        if (cursor.moveToFirst()) {
            do {
                shows.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return shows;
    }
    
    public int getTotalMediaMinutesForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MEDIA_CONSUMPTION, 
                new String[]{"SUM(duration_minutes) as total"}, 
                KEY_DATE + " = ?", 
                new String[]{date}, 
                null, null, null);
        
        int totalMinutes = 0;
        if (cursor.moveToFirst()) {
            totalMinutes = cursor.getInt(0);
        }
        
        cursor.close();
        return totalMinutes;
    }
    
    public void deleteOldMediaConsumption(int daysToKeep) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Calculate cutoff date
        Calendar cutoffDate = Calendar.getInstance();
        cutoffDate.add(Calendar.DAY_OF_YEAR, -daysToKeep);
        String cutoffDateString = DATE_FORMAT.format(cutoffDate.getTime());
        
        int deletedRows = db.delete(TABLE_MEDIA_CONSUMPTION, 
                KEY_DATE + " < ?", 
                new String[]{cutoffDateString});
        
        Log.d(TAG, "Deleted " + deletedRows + " old media consumption records");
    }
    
    public void close() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
    
    // Goal CRUD operations
    public long insertGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_GOAL_TITLE, goal.getTitle());
        values.put(KEY_GOAL_DESCRIPTION, goal.getDescription());
        values.put(KEY_GOAL_TYPE, goal.getType().name());
        values.put(KEY_GOAL_CATEGORY, goal.getCategory().name());
        values.put(KEY_GOAL_TARGET_VALUE, goal.getTargetValue());
        values.put(KEY_GOAL_TARGET_UNIT, goal.getTargetUnit());
        values.put(KEY_GOAL_CURRENT_VALUE, goal.getCurrentValue());
        values.put(KEY_GOAL_FREQUENCY, goal.getFrequency().name());
        values.put(KEY_GOAL_START_DATE, goal.getStartDate() != null ? DATETIME_FORMAT.format(goal.getStartDate()) : null);
        values.put(KEY_GOAL_END_DATE, goal.getEndDate() != null ? DATETIME_FORMAT.format(goal.getEndDate()) : null);
        values.put(KEY_GOAL_IS_ACTIVE, goal.isActive() ? 1 : 0);
        values.put(KEY_GOAL_IS_COMPLETED, goal.isCompleted() ? 1 : 0);
        values.put(KEY_GOAL_STREAK_COUNT, goal.getStreakCount());
        values.put(KEY_GOAL_TOTAL_COMPLETIONS, goal.getTotalCompletions());
        values.put(KEY_GOAL_BEST_VALUE, goal.getBestValue());
        values.put(KEY_GOAL_LAST_COMPLETED_DATE, goal.getLastCompletedDate() != null ? DATETIME_FORMAT.format(goal.getLastCompletedDate()) : null);
        values.put(KEY_GOAL_MOTIVATIONAL_MESSAGE, goal.getMotivationalMessage());
        values.put(KEY_GOAL_PRIORITY, goal.getPriority());
        values.put(KEY_GOAL_COLOR, goal.getColor());
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.insert(TABLE_GOALS, null, values);
    }
    
    public int updateGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_GOAL_TITLE, goal.getTitle());
        values.put(KEY_GOAL_DESCRIPTION, goal.getDescription());
        values.put(KEY_GOAL_TYPE, goal.getType().name());
        values.put(KEY_GOAL_CATEGORY, goal.getCategory().name());
        values.put(KEY_GOAL_TARGET_VALUE, goal.getTargetValue());
        values.put(KEY_GOAL_TARGET_UNIT, goal.getTargetUnit());
        values.put(KEY_GOAL_CURRENT_VALUE, goal.getCurrentValue());
        values.put(KEY_GOAL_FREQUENCY, goal.getFrequency().name());
        values.put(KEY_GOAL_START_DATE, goal.getStartDate() != null ? DATETIME_FORMAT.format(goal.getStartDate()) : null);
        values.put(KEY_GOAL_END_DATE, goal.getEndDate() != null ? DATETIME_FORMAT.format(goal.getEndDate()) : null);
        values.put(KEY_GOAL_IS_ACTIVE, goal.isActive() ? 1 : 0);
        values.put(KEY_GOAL_IS_COMPLETED, goal.isCompleted() ? 1 : 0);
        values.put(KEY_GOAL_STREAK_COUNT, goal.getStreakCount());
        values.put(KEY_GOAL_TOTAL_COMPLETIONS, goal.getTotalCompletions());
        values.put(KEY_GOAL_BEST_VALUE, goal.getBestValue());
        values.put(KEY_GOAL_LAST_COMPLETED_DATE, goal.getLastCompletedDate() != null ? DATETIME_FORMAT.format(goal.getLastCompletedDate()) : null);
        values.put(KEY_GOAL_MOTIVATIONAL_MESSAGE, goal.getMotivationalMessage());
        values.put(KEY_GOAL_PRIORITY, goal.getPriority());
        values.put(KEY_GOAL_COLOR, goal.getColor());
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.update(TABLE_GOALS, values, KEY_GOAL_ID + " = ?", new String[]{String.valueOf(goal.getId())});
    }
    
    public int deleteGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_GOALS, KEY_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
    }
    
    public List<Goal> getActiveGoals() {
        List<Goal> goals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GOALS + " WHERE " + KEY_GOAL_IS_ACTIVE + " = 1 ORDER BY " + KEY_GOAL_PRIORITY + " DESC, " + KEY_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Goal goal = cursorToGoal(cursor);
                goals.add(goal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return goals;
    }
    
    public List<Goal> getAllGoals() {
        List<Goal> goals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GOALS + " ORDER BY " + KEY_GOAL_PRIORITY + " DESC, " + KEY_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Goal goal = cursorToGoal(cursor);
                goals.add(goal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return goals;
    }
    
    public List<Goal> getGoalsByCategory(Goal.GoalCategory category) {
        List<Goal> goals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GOALS + " WHERE " + KEY_GOAL_CATEGORY + " = ? ORDER BY " + KEY_GOAL_PRIORITY + " DESC, " + KEY_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{category.name()});
        
        if (cursor.moveToFirst()) {
            do {
                Goal goal = cursorToGoal(cursor);
                goals.add(goal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return goals;
    }
    
    public List<Goal> getGoalsByFrequency(Goal.GoalFrequency frequency) {
        List<Goal> goals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GOALS + " WHERE " + KEY_GOAL_FREQUENCY + " = ? ORDER BY " + KEY_GOAL_PRIORITY + " DESC, " + KEY_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{frequency.name()});
        
        if (cursor.moveToFirst()) {
            do {
                Goal goal = cursorToGoal(cursor);
                goals.add(goal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return goals;
    }
    
    public Goal getGoalById(int goalId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, null, KEY_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            Goal goal = cursorToGoal(cursor);
            cursor.close();
            return goal;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
    
    private Goal cursorToGoal(Cursor cursor) {
        Goal goal = new Goal();
        
        goal.setId(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_ID)));
        goal.setTitle(cursor.getString(cursor.getColumnIndex(KEY_GOAL_TITLE)));
        goal.setDescription(cursor.getString(cursor.getColumnIndex(KEY_GOAL_DESCRIPTION)));
        goal.setType(Goal.GoalType.valueOf(cursor.getString(cursor.getColumnIndex(KEY_GOAL_TYPE))));
        goal.setCategory(Goal.GoalCategory.valueOf(cursor.getString(cursor.getColumnIndex(KEY_GOAL_CATEGORY))));
        goal.setTargetValue(cursor.getFloat(cursor.getColumnIndex(KEY_GOAL_TARGET_VALUE)));
        goal.setTargetUnit(cursor.getString(cursor.getColumnIndex(KEY_GOAL_TARGET_UNIT)));
        goal.setCurrentValue(cursor.getFloat(cursor.getColumnIndex(KEY_GOAL_CURRENT_VALUE)));
        goal.setFrequency(Goal.GoalFrequency.valueOf(cursor.getString(cursor.getColumnIndex(KEY_GOAL_FREQUENCY))));
        
        // Parse dates
        try {
            String startDateStr = cursor.getString(cursor.getColumnIndex(KEY_GOAL_START_DATE));
            if (startDateStr != null) {
                goal.setStartDate(DATETIME_FORMAT.parse(startDateStr));
            }
            
            String endDateStr = cursor.getString(cursor.getColumnIndex(KEY_GOAL_END_DATE));
            if (endDateStr != null) {
                goal.setEndDate(DATETIME_FORMAT.parse(endDateStr));
            }
            
            String lastCompletedStr = cursor.getString(cursor.getColumnIndex(KEY_GOAL_LAST_COMPLETED_DATE));
            if (lastCompletedStr != null) {
                goal.setLastCompletedDate(DATETIME_FORMAT.parse(lastCompletedStr));
            }
            
            String createdAtStr = cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT));
            if (createdAtStr != null) {
                goal.setCreatedAt(DATETIME_FORMAT.parse(createdAtStr));
            }
            
            String updatedAtStr = cursor.getString(cursor.getColumnIndex(KEY_UPDATED_AT));
            if (updatedAtStr != null) {
                goal.setUpdatedAt(DATETIME_FORMAT.parse(updatedAtStr));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing goal dates", e);
        }
        
        goal.setActive(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_IS_ACTIVE)) == 1);
        goal.setCompleted(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_IS_COMPLETED)) == 1);
        goal.setStreakCount(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_STREAK_COUNT)));
        goal.setTotalCompletions(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_TOTAL_COMPLETIONS)));
        goal.setBestValue(cursor.getFloat(cursor.getColumnIndex(KEY_GOAL_BEST_VALUE)));
        goal.setMotivationalMessage(cursor.getString(cursor.getColumnIndex(KEY_GOAL_MOTIVATIONAL_MESSAGE)));
        goal.setPriority(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_PRIORITY)));
        goal.setColor(cursor.getString(cursor.getColumnIndex(KEY_GOAL_COLOR)));
        
        return goal;
    }
    
    // Achievement CRUD operations
    public long insertAchievement(Achievement achievement) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_ACHIEVEMENT_TITLE, achievement.getTitle());
        values.put(KEY_ACHIEVEMENT_DESCRIPTION, achievement.getDescription());
        values.put(KEY_ACHIEVEMENT_TYPE, achievement.getType().name());
        values.put(KEY_ACHIEVEMENT_CATEGORY, achievement.getCategory().name());
        values.put(KEY_ACHIEVEMENT_TIER, achievement.getTier().name());
        values.put(KEY_ACHIEVEMENT_ICON_NAME, achievement.getIconName());
        values.put(KEY_ACHIEVEMENT_BADGE_COLOR, achievement.getBadgeColor());
        values.put(KEY_ACHIEVEMENT_TARGET_VALUE, achievement.getTargetValue());
        values.put(KEY_ACHIEVEMENT_TARGET_UNIT, achievement.getTargetUnit());
        values.put(KEY_ACHIEVEMENT_CURRENT_PROGRESS, achievement.getCurrentProgress());
        values.put(KEY_ACHIEVEMENT_IS_UNLOCKED, achievement.isUnlocked() ? 1 : 0);
        values.put(KEY_ACHIEVEMENT_UNLOCKED_AT, achievement.getUnlockedAt() != null ? DATETIME_FORMAT.format(achievement.getUnlockedAt()) : null);
        values.put(KEY_ACHIEVEMENT_REQUIREMENTS, achievement.getRequirements());
        values.put(KEY_ACHIEVEMENT_POINTS_VALUE, achievement.getPointsValue());
        values.put(KEY_ACHIEVEMENT_IS_HIDDEN, achievement.isHidden() ? 1 : 0);
        values.put(KEY_ACHIEVEMENT_STREAK_REQUIREMENT, achievement.getStreakRequirement());
        values.put(KEY_ACHIEVEMENT_CUSTOM_CONDITION, achievement.getCustomCondition());
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.insert(TABLE_ACHIEVEMENTS, null, values);
    }
    
    public int updateAchievement(Achievement achievement) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_ACHIEVEMENT_TITLE, achievement.getTitle());
        values.put(KEY_ACHIEVEMENT_DESCRIPTION, achievement.getDescription());
        values.put(KEY_ACHIEVEMENT_TYPE, achievement.getType().name());
        values.put(KEY_ACHIEVEMENT_CATEGORY, achievement.getCategory().name());
        values.put(KEY_ACHIEVEMENT_TIER, achievement.getTier().name());
        values.put(KEY_ACHIEVEMENT_ICON_NAME, achievement.getIconName());
        values.put(KEY_ACHIEVEMENT_BADGE_COLOR, achievement.getBadgeColor());
        values.put(KEY_ACHIEVEMENT_TARGET_VALUE, achievement.getTargetValue());
        values.put(KEY_ACHIEVEMENT_TARGET_UNIT, achievement.getTargetUnit());
        values.put(KEY_ACHIEVEMENT_CURRENT_PROGRESS, achievement.getCurrentProgress());
        values.put(KEY_ACHIEVEMENT_IS_UNLOCKED, achievement.isUnlocked() ? 1 : 0);
        values.put(KEY_ACHIEVEMENT_UNLOCKED_AT, achievement.getUnlockedAt() != null ? DATETIME_FORMAT.format(achievement.getUnlockedAt()) : null);
        values.put(KEY_ACHIEVEMENT_REQUIREMENTS, achievement.getRequirements());
        values.put(KEY_ACHIEVEMENT_POINTS_VALUE, achievement.getPointsValue());
        values.put(KEY_ACHIEVEMENT_IS_HIDDEN, achievement.isHidden() ? 1 : 0);
        values.put(KEY_ACHIEVEMENT_STREAK_REQUIREMENT, achievement.getStreakRequirement());
        values.put(KEY_ACHIEVEMENT_CUSTOM_CONDITION, achievement.getCustomCondition());
        
        return db.update(TABLE_ACHIEVEMENTS, values, KEY_ACHIEVEMENT_ID + " = ?", new String[]{String.valueOf(achievement.getId())});
    }
    
    public List<Achievement> getAllAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ACHIEVEMENTS + " ORDER BY " + KEY_ACHIEVEMENT_TIER + ", " + KEY_ACHIEVEMENT_CATEGORY;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Achievement achievement = cursorToAchievement(cursor);
                achievements.add(achievement);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return achievements;
    }
    
    public List<Achievement> getAchievementsByCategory(Achievement.AchievementCategory category) {
        List<Achievement> achievements = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ACHIEVEMENTS + " WHERE " + KEY_ACHIEVEMENT_CATEGORY + " = ? ORDER BY " + KEY_ACHIEVEMENT_TIER;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{category.name()});
        
        if (cursor.moveToFirst()) {
            do {
                Achievement achievement = cursorToAchievement(cursor);
                achievements.add(achievement);
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        return achievements;
    }
    
    public int getUnlockedAchievementsCount() {
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_ACHIEVEMENTS + " WHERE " + KEY_ACHIEVEMENT_IS_UNLOCKED + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        
        return count;
    }
    
    private Achievement cursorToAchievement(Cursor cursor) {
        Achievement achievement = new Achievement();
        
        achievement.setId(cursor.getInt(cursor.getColumnIndex(KEY_ACHIEVEMENT_ID)));
        achievement.setTitle(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_TITLE)));
        achievement.setDescription(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_DESCRIPTION)));
        achievement.setType(Achievement.AchievementType.valueOf(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_TYPE))));
        achievement.setCategory(Achievement.AchievementCategory.valueOf(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_CATEGORY))));
        achievement.setTier(Achievement.AchievementTier.valueOf(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_TIER))));
        achievement.setIconName(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_ICON_NAME)));
        achievement.setBadgeColor(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_BADGE_COLOR)));
        achievement.setTargetValue(cursor.getInt(cursor.getColumnIndex(KEY_ACHIEVEMENT_TARGET_VALUE)));
        achievement.setTargetUnit(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_TARGET_UNIT)));
        achievement.setCurrentProgress(cursor.getInt(cursor.getColumnIndex(KEY_ACHIEVEMENT_CURRENT_PROGRESS)));
        achievement.setUnlocked(cursor.getInt(cursor.getColumnIndex(KEY_ACHIEVEMENT_IS_UNLOCKED)) == 1);
        achievement.setRequirements(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_REQUIREMENTS)));
        achievement.setPointsValue(cursor.getInt(cursor.getColumnIndex(KEY_ACHIEVEMENT_POINTS_VALUE)));
        achievement.setHidden(cursor.getInt(cursor.getColumnIndex(KEY_ACHIEVEMENT_IS_HIDDEN)) == 1);
        achievement.setStreakRequirement(cursor.getInt(cursor.getColumnIndex(KEY_ACHIEVEMENT_STREAK_REQUIREMENT)));
        achievement.setCustomCondition(cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_CUSTOM_CONDITION)));
        
        // Parse dates
        try {
            String unlockedAtStr = cursor.getString(cursor.getColumnIndex(KEY_ACHIEVEMENT_UNLOCKED_AT));
            if (unlockedAtStr != null) {
                achievement.setUnlockedAt(DATETIME_FORMAT.parse(unlockedAtStr));
            }
            
            String createdAtStr = cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT));
            if (createdAtStr != null) {
                achievement.setCreatedAt(DATETIME_FORMAT.parse(createdAtStr));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing achievement dates", e);
        }
        
        return achievement;
    }
    
    // UserLevel CRUD operations
    public long insertUserLevel(UserLevel userLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_USER_CURRENT_LEVEL, userLevel.getCurrentLevel());
        values.put(KEY_USER_CURRENT_XP, userLevel.getCurrentXP());
        values.put(KEY_USER_TOTAL_XP, userLevel.getTotalXP());
        values.put(KEY_USER_LAST_LEVEL_UP, userLevel.getLastLevelUp() != null ? DATETIME_FORMAT.format(userLevel.getLastLevelUp()) : null);
        values.put(KEY_USER_CURRENT_TITLE, userLevel.getCurrentTitle());
        values.put(KEY_USER_ACHIEVEMENTS_UNLOCKED, userLevel.getAchievementsUnlocked());
        values.put(KEY_USER_STREAKS_COMPLETED, userLevel.getStreaksCompleted());
        values.put(KEY_USER_GOALS_COMPLETED, userLevel.getGoalsCompleted());
        values.put(KEY_CREATED_AT, DATETIME_FORMAT.format(new Date()));
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.insert(TABLE_USER_LEVEL, null, values);
    }
    
    public int updateUserLevel(UserLevel userLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_USER_CURRENT_LEVEL, userLevel.getCurrentLevel());
        values.put(KEY_USER_CURRENT_XP, userLevel.getCurrentXP());
        values.put(KEY_USER_TOTAL_XP, userLevel.getTotalXP());
        values.put(KEY_USER_LAST_LEVEL_UP, userLevel.getLastLevelUp() != null ? DATETIME_FORMAT.format(userLevel.getLastLevelUp()) : null);
        values.put(KEY_USER_CURRENT_TITLE, userLevel.getCurrentTitle());
        values.put(KEY_USER_ACHIEVEMENTS_UNLOCKED, userLevel.getAchievementsUnlocked());
        values.put(KEY_USER_STREAKS_COMPLETED, userLevel.getStreaksCompleted());
        values.put(KEY_USER_GOALS_COMPLETED, userLevel.getGoalsCompleted());
        values.put(KEY_UPDATED_AT, DATETIME_FORMAT.format(new Date()));
        
        return db.update(TABLE_USER_LEVEL, values, KEY_USER_LEVEL_ID + " = ?", new String[]{String.valueOf(userLevel.getId())});
    }
    
    public UserLevel getUserLevel() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_LEVEL, null, null, null, null, null, KEY_USER_LEVEL_ID + " DESC", "1");
        
        if (cursor != null && cursor.moveToFirst()) {
            UserLevel userLevel = cursorToUserLevel(cursor);
            cursor.close();
            return userLevel;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
    
    private UserLevel cursorToUserLevel(Cursor cursor) {
        UserLevel userLevel = new UserLevel();
        
        userLevel.setId(cursor.getInt(cursor.getColumnIndex(KEY_USER_LEVEL_ID)));
        userLevel.setCurrentLevel(cursor.getInt(cursor.getColumnIndex(KEY_USER_CURRENT_LEVEL)));
        userLevel.setCurrentXP(cursor.getInt(cursor.getColumnIndex(KEY_USER_CURRENT_XP)));
        userLevel.setTotalXP(cursor.getInt(cursor.getColumnIndex(KEY_USER_TOTAL_XP)));
        userLevel.setCurrentTitle(cursor.getString(cursor.getColumnIndex(KEY_USER_CURRENT_TITLE)));
        userLevel.setAchievementsUnlocked(cursor.getInt(cursor.getColumnIndex(KEY_USER_ACHIEVEMENTS_UNLOCKED)));
        userLevel.setStreaksCompleted(cursor.getInt(cursor.getColumnIndex(KEY_USER_STREAKS_COMPLETED)));
        userLevel.setGoalsCompleted(cursor.getInt(cursor.getColumnIndex(KEY_USER_GOALS_COMPLETED)));
        
        // Parse dates
        try {
            String lastLevelUpStr = cursor.getString(cursor.getColumnIndex(KEY_USER_LAST_LEVEL_UP));
            if (lastLevelUpStr != null) {
                userLevel.setLastLevelUp(DATETIME_FORMAT.parse(lastLevelUpStr));
            }
            
            String createdAtStr = cursor.getString(cursor.getColumnIndex(KEY_CREATED_AT));
            if (createdAtStr != null) {
                userLevel.setCreatedAt(DATETIME_FORMAT.parse(createdAtStr));
            }
            
            String updatedAtStr = cursor.getString(cursor.getColumnIndex(KEY_UPDATED_AT));
            if (updatedAtStr != null) {
                userLevel.setUpdatedAt(DATETIME_FORMAT.parse(updatedAtStr));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing user level dates", e);
        }
        
        return userLevel;
    }
}