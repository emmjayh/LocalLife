package com.locallife.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locallife.db";
    private static final int DATABASE_VERSION = 1;

    // Events table
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_EVENT_ID = "id";
    public static final String COLUMN_EVENT_TITLE = "title";
    public static final String COLUMN_EVENT_DESCRIPTION = "description";
    public static final String COLUMN_EVENT_START_DATE = "start_date";
    public static final String COLUMN_EVENT_END_DATE = "end_date";
    public static final String COLUMN_EVENT_LOCATION = "location";
    public static final String COLUMN_EVENT_CATEGORY = "category";

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PROFILE_IMAGE = "profile_image";

    // Create tables SQL
    private static final String CREATE_EVENTS_TABLE = 
        "CREATE TABLE " + TABLE_EVENTS + " (" +
        COLUMN_EVENT_ID + " TEXT PRIMARY KEY, " +
        COLUMN_EVENT_TITLE + " TEXT NOT NULL, " +
        COLUMN_EVENT_DESCRIPTION + " TEXT, " +
        COLUMN_EVENT_START_DATE + " INTEGER, " +
        COLUMN_EVENT_END_DATE + " INTEGER, " +
        COLUMN_EVENT_LOCATION + " TEXT, " +
        COLUMN_EVENT_CATEGORY + " TEXT" +
        ")";

    private static final String CREATE_USERS_TABLE = 
        "CREATE TABLE " + TABLE_USERS + " (" +
        COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
        COLUMN_USER_NAME + " TEXT NOT NULL, " +
        COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
        COLUMN_USER_PROFILE_IMAGE + " TEXT" +
        ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}