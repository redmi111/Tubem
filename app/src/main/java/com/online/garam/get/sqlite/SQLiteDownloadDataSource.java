package com.online.garam.get.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.online.garam.get.DownloadDataSource;
import com.online.garam.get.DownloadMission;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDownloadDataSource implements DownloadDataSource {
    private static final String TAG = "DownloadDataSourceImpl";
    private final DownloadMissionSQLiteHelper downloadMissionSQLiteHelper;

    public SQLiteDownloadDataSource(Context context) {
        this.downloadMissionSQLiteHelper = new DownloadMissionSQLiteHelper(context);
    }

    public List<DownloadMission> loadMissions() {
        Cursor cursor = this.downloadMissionSQLiteHelper.getReadableDatabase().query("download_missions", null, null, null, null, null, "timestamp");
        int count = cursor.getCount();
        if (count == 0) {
            return new ArrayList();
        }
        ArrayList<DownloadMission> result = new ArrayList<>(count);
        while (cursor.moveToNext()) {
            result.add(DownloadMissionSQLiteHelper.getMissionFromCursor(cursor));
        }
        return result;
    }

    public void addMission(DownloadMission downloadMission) {
        if (downloadMission == null) {
            throw new NullPointerException("downloadMission is null");
        }
        this.downloadMissionSQLiteHelper.getWritableDatabase().insert("download_missions", null, DownloadMissionSQLiteHelper.getValuesOfMission(downloadMission));
    }

    public void updateMission(DownloadMission downloadMission) {
        if (downloadMission == null) {
            throw new NullPointerException("downloadMission is null");
        }
        int rowsAffected = this.downloadMissionSQLiteHelper.getWritableDatabase().update("download_missions", DownloadMissionSQLiteHelper.getValuesOfMission(downloadMission), "location = ? AND name = ?", new String[]{downloadMission.location, downloadMission.name});
        if (rowsAffected != 1) {
            Log.e(TAG, "Expected 1 row to be affected by update but got " + rowsAffected);
        }
    }

    public void deleteMission(DownloadMission downloadMission) {
        if (downloadMission == null) {
            throw new NullPointerException("downloadMission is null");
        }
        this.downloadMissionSQLiteHelper.getWritableDatabase().delete("download_missions", "location = ? AND name = ?", new String[]{downloadMission.location, downloadMission.name});
    }
}
