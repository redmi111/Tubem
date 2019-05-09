package com.online.garam.get;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.online.garam.util.Utility;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DownloadMission {
    public static final int ERROR_SERVER_UNSUPPORTED = 206;
    public static final int ERROR_UNKNOWN = 233;
    private static final int NO_IDENTIFIER = -1;
    private static final String TAG = DownloadMission.class.getSimpleName();
    public final Map<Long, Boolean> blockState = new HashMap();
    public long blocks;
    private long db_identifier = -1;
    public long done;
    public int errCode = -1;
    public boolean fallback;
    public int finishCount;
    public boolean finished;
    public long length;
    public String location;
    private transient ArrayList<WeakReference<MissionListener>> mListeners = new ArrayList<>();
    /* access modifiers changed from: private|transient */
    public transient boolean mWritingToFile;
    public String name;
    public transient boolean recovered;
    public boolean running;
    public int threadCount = 3;
    private List<Long> threadPositions = new ArrayList();
    public long timestamp;
    public String url;

    public interface MissionListener {
        public static final HashMap<MissionListener, Handler> handlerStore = new HashMap<>();

        void onError(DownloadMission downloadMission, int i);

        void onFinish(DownloadMission downloadMission);

        void onProgressUpdate(DownloadMission downloadMission, long j, long j2);
    }

    public DownloadMission() {
    }

    public DownloadMission(String name2, String url2, String location2) {
        if (name2 == null) {
            throw new NullPointerException("name is null");
        } else if (name2.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        } else if (url2 == null) {
            throw new NullPointerException("url is null");
        } else if (url2.isEmpty()) {
            throw new IllegalArgumentException("url is empty");
        } else if (location2 == null) {
            throw new NullPointerException("location is null");
        } else if (location2.isEmpty()) {
            throw new IllegalArgumentException("location is empty");
        } else {
            this.url = url2;
            this.name = name2;
            this.location = location2;
        }
    }

    private void checkBlock(long block) {
        if (block < 0 || block >= this.blocks) {
            throw new IllegalArgumentException("illegal block identifier");
        }
    }

    public boolean isBlockPreserved(long block) {
        checkBlock(block);
        if (this.blockState.containsKey(Long.valueOf(block))) {
            return ((Boolean) this.blockState.get(Long.valueOf(block))).booleanValue();
        }
        return false;
    }

    public void preserveBlock(long block) {
        checkBlock(block);
        synchronized (this.blockState) {
            this.blockState.put(Long.valueOf(block), Boolean.valueOf(true));
        }
    }

    public void setPosition(int threadId, long position) {
        this.threadPositions.set(threadId, Long.valueOf(position));
    }

    public long getPosition(int threadId) {
        return ((Long) this.threadPositions.get(threadId)).longValue();
    }

    public synchronized void notifyProgress(long deltaLen) {
        if (this.running) {
            if (this.recovered) {
                this.recovered = false;
            }
            this.done += deltaLen;
            if (this.done > this.length) {
                this.done = this.length;
            }
            if (this.done != this.length) {
                writeThisToFile();
            }
            Iterator it = this.mListeners.iterator();
            while (it.hasNext()) {
                final MissionListener listener = (MissionListener) ((WeakReference) it.next()).get();
                if (listener != null) {
                    ((Handler) MissionListener.handlerStore.get(listener)).post(new Runnable() {
                        public void run() {
                            listener.onProgressUpdate(DownloadMission.this, DownloadMission.this.done, DownloadMission.this.length);
                        }
                    });
                }
            }
        }
    }

    public synchronized void notifyFinished() {
        if (this.errCode <= 0) {
            this.finishCount++;
            if (this.finishCount == this.threadCount) {
                onFinish();
            }
        }
    }

    private void onFinish() {
        if (this.errCode <= 0) {
            this.running = false;
            this.finished = true;
            deleteThisFromFile();
            Iterator it = this.mListeners.iterator();
            while (it.hasNext()) {
                final MissionListener listener = (MissionListener) ((WeakReference) it.next()).get();
                if (listener != null) {
                    ((Handler) MissionListener.handlerStore.get(listener)).post(new Runnable() {
                        public void run() {
                            listener.onFinish(DownloadMission.this);
                        }
                    });
                }
            }
        }
    }

    public synchronized void notifyError(int err) {
        this.errCode = err;
        writeThisToFile();
        Iterator it = this.mListeners.iterator();
        while (it.hasNext()) {
            final MissionListener listener = (MissionListener) ((WeakReference) it.next()).get();
            ((Handler) MissionListener.handlerStore.get(listener)).post(new Runnable() {
                public void run() {
                    listener.onError(DownloadMission.this, DownloadMission.this.errCode);
                }
            });
        }
    }

    public synchronized void addListener(MissionListener listener) {
        MissionListener.handlerStore.put(listener, new Handler(Looper.getMainLooper()));
        this.mListeners.add(new WeakReference(listener));
    }

    public synchronized void removeListener(MissionListener listener) {
        Iterator<WeakReference<MissionListener>> iterator = this.mListeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<MissionListener> weakRef = (WeakReference) iterator.next();
            if (listener != null && listener == weakRef.get()) {
                iterator.remove();
            }
        }
    }

    public void start() {
        if (!this.running && !this.finished) {
            this.running = true;
            if (!this.fallback) {
                for (int i = 0; i < this.threadCount; i++) {
                    if (this.threadPositions.size() <= i && !this.recovered) {
                        this.threadPositions.add(Long.valueOf((long) i));
                    }
                    new Thread(new DownloadRunnable(this, i)).start();
                }
                return;
            }
            this.threadCount = 1;
            this.done = 0;
            this.blocks = 0;
            new Thread(new DownloadRunnableFallback(this)).start();
        }
    }

    public void pause() {
        if (this.running) {
            this.running = false;
            this.recovered = true;
        }
    }

    public void delete() {
        deleteThisFromFile();
        new File(this.location, this.name).delete();
    }

    public void writeThisToFile() {
        if (!this.mWritingToFile) {
            this.mWritingToFile = true;
            new Thread() {
                public void run() {
                    DownloadMission.this.doWriteThisToFile();
                    DownloadMission.this.mWritingToFile = false;
                }
            }.start();
        }
    }

    /* access modifiers changed from: private */
    public void doWriteThisToFile() {
        synchronized (this.blockState) {
            Utility.writeToFile(getMetaFilename(), new Gson().toJson((Object) this));
        }
    }

    private void deleteThisFromFile() {
        new File(getMetaFilename()).delete();
    }

    private String getMetaFilename() {
        return this.location + "/" + this.name + ".downloader";
    }

    public File getDownloadedFile() {
        return new File(this.location, this.name);
    }
}
