package com.online.garam.get.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.online.garam.get.DownloadMission;

public class DownloadMissionSQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "downloads.db";
    private static final int DATABASE_VERSION = 2;
    static final String KEY_DONE = "bytes_downloaded";
    static final String KEY_LOCATION = "location";
    static final String KEY_NAME = "name";
    static final String KEY_TIMESTAMP = "timestamp";
    static final String KEY_URL = "url";
    private static final String MISSIONS_CREATE_TABLE = "CREATE TABLE download_missions (location TEXT NOT NULL, name TEXT NOT NULL, url TEXT NOT NULL, bytes_downloaded INTEGER NOT NULL, timestamp INTEGER NOT NULL,  UNIQUE(location, name));";
    static final String MISSIONS_TABLE_NAME = "download_missions";
    private final String TAG = "DownloadMissionHelper";

    DownloadMissionSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    public static ContentValues getValuesOfMission(DownloadMission downloadMission) {
        ContentValues values = new ContentValues();
        values.put("url", downloadMission.url);
        values.put(KEY_LOCATION, downloadMission.location);
        values.put("name", downloadMission.name);
        values.put(KEY_DONE, Long.valueOf(downloadMission.done));
        values.put(KEY_TIMESTAMP, Long.valueOf(downloadMission.timestamp));
        return values;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MISSIONS_CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static DownloadMission getMissionFromCursor(Cursor cursor) {
        if (cursor == null) {
            throw new NullPointerException("cursor is null");
        }
        DownloadMission mission = new DownloadMission(cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getString(cursor.getColumnIndexOrThrow("url")), cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)));
        mission.done = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DONE));
        mission.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIMESTAMP));
        mission.finished = true;
        return mission;
    }
}
