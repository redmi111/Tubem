package com.online.garam.get;

import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadRunnableFallback implements Runnable {
    private final DownloadMission mMission;

    public DownloadRunnableFallback(DownloadMission mission) {
        if (mission == null) {
            throw new NullPointerException("mission is null");
        }
        this.mMission = mission;
    }

    public void run() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(this.mMission.url).openConnection();
            if (conn.getResponseCode() == 200 || conn.getResponseCode() == 206) {
                RandomAccessFile f = new RandomAccessFile(this.mMission.location + "/" + this.mMission.name, "rw");
                f.seek(0);
                BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());
                byte[] buf = new byte[512];
                do {
                    int len = ipt.read(buf, 0, 512);
                    if (len == -1 || !this.mMission.running) {
                        f.close();
                        ipt.close();
                    } else {
                        f.write(buf, 0, len);
                        notifyProgress((long) len);
                    }
                } while (!Thread.interrupted());
                f.close();
                ipt.close();
                if (this.mMission.errCode == -1 && this.mMission.running) {
                    notifyFinished();
                    return;
                }
            }
            notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED);
            if (this.mMission.errCode == -1) {
            }
        } catch (Exception e) {
            notifyError(DownloadMission.ERROR_UNKNOWN);
        }
    }

    private void notifyProgress(long len) {
        synchronized (this.mMission) {
            this.mMission.notifyProgress(len);
        }
    }

    private void notifyError(int err) {
        synchronized (this.mMission) {
            this.mMission.notifyError(err);
            this.mMission.pause();
        }
    }

    private void notifyFinished() {
        synchronized (this.mMission) {
            this.mMission.notifyFinished();
        }
    }
}
