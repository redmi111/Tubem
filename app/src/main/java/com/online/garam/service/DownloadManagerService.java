package com.online.garam.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;
import com.google.android.exoplayer.C;
import com.online.garam.get.DownloadDataSource;
import com.online.garam.get.DownloadManager;
import com.online.garam.get.DownloadManagerImpl;
import com.online.garam.get.DownloadMission;
import com.online.garam.get.sqlite.SQLiteDownloadDataSource;
import full.movie.tubem.player.R;
import full.movie.tubem.player.download.DownloadActivity;
import full.movie.tubem.player.settings.NewSettings;
import java.util.ArrayList;

public class DownloadManagerService extends Service {
    private static final String EXTRA_IS_AUDIO = "DownloadManagerService.extra.is_audio";
    private static final String EXTRA_LOCATION = "DownloadManagerService.extra.location";
    private static final String EXTRA_NAME = "DownloadManagerService.extra.name";
    private static final String EXTRA_THREADS = "DownloadManagerService.extra.threads";
    private static final int NOTIFICATION_ID = 1000;
    private static final String TAG = DownloadManagerService.class.getSimpleName();
    private static final int UPDATE_MESSAGE = 0;
    /* access modifiers changed from: private */
    public DMBinder mBinder;
    private DownloadDataSource mDataSource;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public long mLastTimeStamp = System.currentTimeMillis();
    /* access modifiers changed from: private */
    public DownloadManager mManager;
    private Notification mNotification;
    /* access modifiers changed from: private */
    public MissionListener missionListener = new MissionListener();

    public class DMBinder extends Binder {
        public DMBinder() {
        }

        public DownloadManager getDownloadManager() {
            return DownloadManagerService.this.mManager;
        }

        public void onMissionAdded(DownloadMission mission) {
            mission.addListener(DownloadManagerService.this.missionListener);
            DownloadManagerService.this.postUpdateMessage();
        }

        public void onMissionRemoved(DownloadMission mission) {
            mission.removeListener(DownloadManagerService.this.missionListener);
            DownloadManagerService.this.postUpdateMessage();
        }
    }

    class MissionListener implements DownloadMission.MissionListener {
        MissionListener() {
        }

        public void onProgressUpdate(DownloadMission downloadMission, long done, long total) {
            long now = System.currentTimeMillis();
            if (now - DownloadManagerService.this.mLastTimeStamp > 2000) {
                DownloadManagerService.this.postUpdateMessage();
                DownloadManagerService.this.mLastTimeStamp = now;
            }
        }

        public void onFinish(DownloadMission downloadMission) {
            DownloadManagerService.this.postUpdateMessage();
            DownloadManagerService.this.notifyMediaScanner(downloadMission);
        }

        public void onError(DownloadMission downloadMission, int errCode) {
            DownloadManagerService.this.postUpdateMessage();
        }
    }

    /* access modifiers changed from: private */
    public void notifyMediaScanner(DownloadMission mission) {
        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse("file://" + mission.location + "/" + mission.name)));
    }

    public void onCreate() {
        super.onCreate();
        this.mBinder = new DMBinder();
        if (this.mDataSource == null) {
            this.mDataSource = new SQLiteDownloadDataSource(this);
        }
        if (this.mManager == null) {
            ArrayList<String> paths = new ArrayList<>(2);
            paths.add(NewSettings.getVideoDownloadPath(this));
            paths.add(NewSettings.getAudioDownloadPath(this));
            this.mManager = new DownloadManagerImpl(paths, this.mDataSource);
        }
        Intent i = new Intent();
        i.setAction("android.intent.action.MAIN");
        i.setClass(this, DownloadActivity.class);
        Builder builder = new Builder(this).setContentIntent(PendingIntent.getActivity(this, 0, i, 0)).setSmallIcon(17301633).setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap()).setContentTitle(getString(R.string.msg_running)).setContentText(getString(R.string.msg_running_detail));
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, DownloadActivity.class).setAction(DownloadActivity.INTENT_LIST), C.SAMPLE_FLAG_DECODE_ONLY));
        this.mNotification = builder.build();
        HandlerThread thread = new HandlerThread("ServiceMessenger");
        thread.start();
        this.mHandler = new Handler(thread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        int runningCount = 0;
                        for (int i = 0; i < DownloadManagerService.this.mManager.getCount(); i++) {
                            if (DownloadManagerService.this.mManager.getMission(i).running) {
                                runningCount++;
                            }
                        }
                        DownloadManagerService.this.updateState(runningCount);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void startMissionAsync(String url, String location, String name, boolean isAudio, int threads) {
        final String str = url;
        final String str2 = location;
        final String str3 = name;
        final boolean z = isAudio;
        final int i = threads;
        this.mHandler.post(new Runnable() {
            public void run() {
                DownloadManagerService.this.mBinder.onMissionAdded(DownloadManagerService.this.mManager.getMission(DownloadManagerService.this.mManager.startMission(str, str2, str3, z, i)));
            }
        });
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Got intent: " + intent);
        String action = intent.getAction();
        if (action != null && action.equals("android.intent.action.RUN")) {
            String name = intent.getStringExtra(EXTRA_NAME);
            String location = intent.getStringExtra(EXTRA_LOCATION);
            int threads = intent.getIntExtra(EXTRA_THREADS, 1);
            startMissionAsync(intent.getDataString(), location, name, intent.getBooleanExtra(EXTRA_IS_AUDIO, false), threads);
        }
        return 2;
    }

    public void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < this.mManager.getCount(); i++) {
            this.mManager.pauseMission(i);
        }
        stopForeground(true);
    }

    public IBinder onBind(Intent intent) {
        if (VERSION.SDK_INT >= 16 && PermissionChecker.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == -1) {
            Toast.makeText(this, "Permission denied (read)", 0).show();
        }
        if (PermissionChecker.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == -1) {
            Toast.makeText(this, "Permission denied (write)", 0).show();
        }
        return this.mBinder;
    }

    /* access modifiers changed from: private */
    public void postUpdateMessage() {
        this.mHandler.sendEmptyMessage(0);
    }

    /* access modifiers changed from: private */
    public void updateState(int runningCount) {
        if (runningCount == 0) {
            stopForeground(true);
        } else {
            startForeground(1000, this.mNotification);
        }
    }

    public static void startMission(Context context, String url, String location, String name, boolean isAudio, int threads) {
        Intent intent = new Intent(context, DownloadManagerService.class);
        intent.setAction("android.intent.action.RUN");
        intent.setData(Uri.parse(url));
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_LOCATION, location);
        intent.putExtra(EXTRA_IS_AUDIO, isAudio);
        intent.putExtra(EXTRA_THREADS, threads);
        context.startService(intent);
    }
}
