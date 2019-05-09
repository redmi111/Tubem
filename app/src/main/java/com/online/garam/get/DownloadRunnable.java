package com.online.garam.get;

import android.support.v4.media.session.PlaybackStateCompat;
import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadRunnable implements Runnable {
    private static final String TAG = DownloadRunnable.class.getSimpleName();
    private final int mId;
    private final DownloadMission mMission;

    public DownloadRunnable(DownloadMission mission, int id) {
        if (mission == null) {
            throw new NullPointerException("mission is null");
        }
        this.mMission = mission;
        this.mId = id;
    }

    public void run() {
        boolean retry = this.mMission.recovered;
        long position = this.mMission.getPosition(this.mId);
        while (true) {
            if (this.mMission.errCode != -1 || !this.mMission.running || position >= this.mMission.blocks) {
                break;
            } else if (Thread.currentThread().isInterrupted()) {
                this.mMission.pause();
                return;
            } else {
                while (!retry && position < this.mMission.blocks && this.mMission.isBlockPreserved(position)) {
                    position++;
                }
                retry = false;
                if (position >= this.mMission.blocks) {
                    break;
                }
                this.mMission.preserveBlock(position);
                this.mMission.setPosition(this.mId, position);
                long start = position * PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED;
                long end = (PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED + start) - 1;
                if (end >= this.mMission.length) {
                    end = this.mMission.length - 1;
                }
                int total = 0;
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(this.mMission.url).openConnection();
                    conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                    if (conn.getResponseCode() != 206) {
                        this.mMission.errCode = DownloadMission.ERROR_SERVER_UNSUPPORTED;
                        notifyError(DownloadMission.ERROR_SERVER_UNSUPPORTED);
                        break;
                    }
                    RandomAccessFile f = new RandomAccessFile(this.mMission.location + "/" + this.mMission.name, "rw");
                    f.seek(start);
                    BufferedInputStream ipt = new BufferedInputStream(conn.getInputStream());
                    byte[] buf = new byte[512];
                    while (start < end && this.mMission.running) {
                        int len = ipt.read(buf, 0, 512);
                        if (len == -1) {
                            break;
                        }
                        start += (long) len;
                        total += len;
                        f.write(buf, 0, len);
                        notifyProgress((long) len);
                    }
                    f.close();
                    ipt.close();
                } catch (Exception e) {
                    retry = true;
                    notifyProgress((long) (-0));
                }
            }
        }
        if (this.mMission.errCode == -1 && this.mMission.running) {
            notifyFinished();
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
