package com.online.garam.get;

public interface DownloadManager {
    public static final int BLOCK_SIZE = 524288;

    void deleteMission(int i);

    int getCount();

    DownloadMission getMission(int i);

    void pauseMission(int i);

    void resumeMission(int i);

    int startMission(String str, String str2, String str3, boolean z, int i);
}
