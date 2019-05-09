package com.online.garam.get;

import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.google.gson.Gson;
import com.online.garam.util.Utility;
import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DownloadManagerImpl implements DownloadManager {
    private static final String TAG = DownloadManagerImpl.class.getSimpleName();
    /* access modifiers changed from: private|final */
    public final DownloadDataSource mDownloadDataSource;
    private final ArrayList<DownloadMission> mMissions = new ArrayList<>();

    private class Initializer extends Thread {
        private DownloadMission mission;

        public Initializer(DownloadMission mission2) {
            this.mission = mission2;
        }

        public void run() {
            try {
                URL url = new URL(this.mission.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                this.mission.length = (long) conn.getContentLength();
                if (this.mission.length <= 0) {
                    this.mission.errCode = DownloadMission.ERROR_SERVER_UNSUPPORTED;
                    return;
                }
                HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();
                conn2.setRequestProperty("Range", "bytes=" + (this.mission.length - 10) + "-" + this.mission.length);
                if (conn2.getResponseCode() != 206) {
                    this.mission.fallback = true;
                }
                this.mission.blocks = this.mission.length / PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED;
                if (((long) this.mission.threadCount) > this.mission.blocks) {
                    this.mission.threadCount = (int) this.mission.blocks;
                }
                if (this.mission.threadCount <= 0) {
                    this.mission.threadCount = 1;
                }
                if (this.mission.blocks * PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED < this.mission.length) {
                    this.mission.blocks++;
                }
                new File(this.mission.location).mkdirs();
                new File(this.mission.location + "/" + this.mission.name).createNewFile();
                RandomAccessFile af = new RandomAccessFile(this.mission.location + "/" + this.mission.name, "rw");
                af.setLength(this.mission.length);
                af.close();
                this.mission.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class MissionListener implements DownloadMission.MissionListener {
        private final DownloadMission mMission;

        private MissionListener(DownloadMission mission) {
            if (mission == null) {
                throw new NullPointerException("mission is null");
            }
            this.mMission = mission;
        }

        public void onProgressUpdate(DownloadMission downloadMission, long done, long total) {
        }

        public void onFinish(DownloadMission downloadMission) {
            DownloadManagerImpl.this.mDownloadDataSource.addMission(this.mMission);
        }

        public void onError(DownloadMission downloadMission, int errCode) {
        }
    }

    public DownloadManagerImpl(Collection<String> searchLocations, DownloadDataSource downloadDataSource) {
        this.mDownloadDataSource = downloadDataSource;
        loadMissions((Iterable<String>) searchLocations);
    }

    public int startMission(String url, String location, String name, boolean isAudio, int threads) {
        DownloadMission existingMission = getMissionByLocation(location, name);
        if (existingMission != null) {
            if (existingMission.finished) {
                deleteMission(this.mMissions.indexOf(existingMission));
            } else {
                try {
                    name = generateUniqueName(location, name);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to generate unique name", e);
                    name = System.currentTimeMillis() + name;
                    Log.i(TAG, "Using " + name);
                }
            }
        }
        DownloadMission mission = new DownloadMission(name, url, location);
        mission.timestamp = System.currentTimeMillis();
        mission.threadCount = threads;
        mission.addListener(new MissionListener(mission));
        new Initializer(mission).start();
        return insertMission(mission);
    }

    public void resumeMission(int i) {
        DownloadMission d = getMission(i);
        if (!d.running && d.errCode == -1) {
            d.start();
        }
    }

    public void pauseMission(int i) {
        DownloadMission d = getMission(i);
        if (d.running) {
            d.pause();
        }
    }

    public void deleteMission(int i) {
        DownloadMission mission = getMission(i);
        if (mission.finished) {
            this.mDownloadDataSource.deleteMission(mission);
        }
        mission.delete();
        this.mMissions.remove(i);
    }

    private void loadMissions(Iterable<String> searchLocations) {
        this.mMissions.clear();
        loadFinishedMissions();
        for (String location : searchLocations) {
            loadMissions(location);
        }
    }

    private void loadFinishedMissions() {
        List<DownloadMission> finishedMissions = this.mDownloadDataSource.loadMissions();
        if (finishedMissions == null) {
            finishedMissions = new ArrayList<>();
        }
        Collections.sort(finishedMissions, new Comparator<DownloadMission>() {
            public int compare(DownloadMission o1, DownloadMission o2) {
                return (int) (o1.timestamp - o2.timestamp);
            }
        });
        this.mMissions.ensureCapacity(this.mMissions.size() + finishedMissions.size());
        for (DownloadMission mission : finishedMissions) {
            File downloadedFile = mission.getDownloadedFile();
            if (!downloadedFile.isFile()) {
                this.mDownloadDataSource.deleteMission(mission);
            } else {
                mission.length = downloadedFile.length();
                mission.finished = true;
                mission.running = false;
                this.mMissions.add(mission);
            }
        }
    }

    private void loadMissions(String location) {
        File f = new File(location);
        if (f.exists() && f.isDirectory()) {
            File[] subs = f.listFiles();
            if (subs == null) {
                Log.e(TAG, "listFiles() returned null");
                return;
            }
            int length = subs.length;
            for (int i = 0; i < length; i++) {
                File sub = subs[i];
                if (sub.isFile() && sub.getName().endsWith(".downloader")) {
                    String str = Utility.readFromFile(sub.getAbsolutePath());
                    if (str != null && !str.trim().equals("")) {
                        DownloadMission mis = (DownloadMission) new Gson().fromJson(str, DownloadMission.class);
                        if (!mis.finished) {
                            mis.running = false;
                            mis.recovered = true;
                            insertMission(mis);
                        } else if (!sub.delete()) {
                            Log.w(TAG, "Unable to delete .downloader file: " + sub.getPath());
                        }
                    }
                }
            }
        }
    }

    public DownloadMission getMission(int i) {
        return (DownloadMission) this.mMissions.get(i);
    }

    public int getCount() {
        return this.mMissions.size();
    }

    private int insertMission(DownloadMission mission) {
        int i = -1;
        if (this.mMissions.size() > 0) {
            do {
                i++;
                if (((DownloadMission) this.mMissions.get(i)).timestamp <= mission.timestamp) {
                    break;
                }
            } while (i < this.mMissions.size() - 1);
        } else {
            i = 0;
        }
        this.mMissions.add(i, mission);
        return i;
    }

    @Nullable
    private DownloadMission getMissionByLocation(String location, String name) {
        Iterator it = this.mMissions.iterator();
        while (it.hasNext()) {
            DownloadMission mission = (DownloadMission) it.next();
            if (location.equals(mission.location) && name.equals(mission.name)) {
                return mission;
            }
        }
        return null;
    }

    private static String[] splitName(String name) {
        int dotIndex = name.lastIndexOf(46);
        if (dotIndex <= 0 || dotIndex == name.length() - 1) {
            return new String[]{name, ""};
        }
        return new String[]{name.substring(0, dotIndex), name.substring(dotIndex + 1)};
    }

    private static String generateUniqueName(String location, String name) {
        String newName;
        if (location == null) {
            throw new NullPointerException("location is null");
        } else if (name == null) {
            throw new NullPointerException("name is null");
        } else {
            File destination = new File(location);
            if (!destination.isDirectory()) {
                throw new IllegalArgumentException("location is not a directory: " + location);
            }
            final String[] nameParts = splitName(name);
            String[] existingName = destination.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(nameParts[0]);
                }
            });
            Arrays.sort(existingName);
            int downloadIndex = 0;
            do {
                newName = nameParts[0] + " (" + downloadIndex + ")." + nameParts[1];
                downloadIndex++;
                if (downloadIndex == 1000) {
                    throw new RuntimeException("Too many existing files");
                }
            } while (Arrays.binarySearch(existingName, newName) >= 0);
            return newName;
        }
    }
}
