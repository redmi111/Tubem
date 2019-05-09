package com.online.garam.get;

import java.util.List;

public interface DownloadDataSource {
    void addMission(DownloadMission downloadMission);

    void deleteMission(DownloadMission downloadMission);

    List<DownloadMission> loadMissions();

    void updateMission(DownloadMission downloadMission);
}
