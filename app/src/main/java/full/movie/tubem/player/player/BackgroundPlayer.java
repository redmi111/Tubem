package full.movie.tubem.player.player;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.google.android.exoplayer.C;
//import com.google.android.gms.common.util.CrashUtils.ErrorDialogData;
import full.movie.tubem.player.ActivityCommunicator;
import full.movie.tubem.player.BuildConfig;
import full.movie.tubem.player.R;
import full.movie.tubem.player.detail.VideoItemDetailActivity;
import java.io.IOException;
import java.util.Arrays;

@SuppressLint({"RestrictedApi", "WrongConstant"})

public class BackgroundPlayer extends Service {
    private static final String ACTION_PLAYBACK_STATE = "BackgroundPlayer.PLAYBACK_STATE";
    private static final String ACTION_PLAYPAUSE = "BackgroundPlayer.PLAYPAUSE";
    private static final String ACTION_REWIND = "BackgroundPlayer.REWIND";
    private static final String ACTION_STOP = "BackgroundPlayer.STOP";
    public static final String CHANNEL_NAME = "channel_name";
    private static final String CLASSNAME = "BackgroundPlayer";
    private static final String EXTRA_PLAYBACK_STATE = "BackgroundPlayer.extras.EXTRA_PLAYBACK_STATE";
    public static final String SERVICE_ID = "service_id";
    private static final String TAG = "BackgroundPlayer";
    public static final String TITLE = "title";
    public static final String WEB_URL = "web_url";
    public static volatile boolean isRunning;
    /* access modifiers changed from: private|volatile */
    public volatile String channelName = "";
    /* access modifiers changed from: private|volatile */
    public volatile int serviceId = -1;
    /* access modifiers changed from: private|volatile */
    public volatile String webUrl = "";

    public static class PlaybackState implements Parcelable {
        public static final Creator<PlaybackState> CREATOR = new Creator<PlaybackState>() {
            public PlaybackState createFromParcel(Parcel in) {
                return new PlaybackState(in);
            }

            public PlaybackState[] newArray(int size) {
                return new PlaybackState[size];
            }
        };
        static final PlaybackState FAILED = new PlaybackState(false, false, true);
        private static final int INDEX_HAS_ERROR = 2;
        private static final int INDEX_IS_PLAYING = 0;
        private static final int INDEX_IS_PREPARED = 1;
        static final PlaybackState UNPREPARED = new PlaybackState(false, false, false);
        private final boolean[] booleanValues;
        private final int duration;
        private final int played;

        PlaybackState(Parcel in) {
            this.booleanValues = new boolean[3];
            this.duration = in.readInt();
            this.played = in.readInt();
            in.readBooleanArray(this.booleanValues);
        }

        PlaybackState(int duration2, int played2, boolean isPlaying) {
            this.booleanValues = new boolean[3];
            this.played = played2;
            this.duration = duration2;
            this.booleanValues[0] = isPlaying;
            this.booleanValues[1] = true;
            this.booleanValues[2] = false;
        }

        private PlaybackState(boolean isPlaying, boolean isPrepared, boolean hasErrors) {
            this.booleanValues = new boolean[3];
            this.played = 0;
            this.duration = 0;
            this.booleanValues[0] = isPlaying;
            this.booleanValues[1] = isPrepared;
            this.booleanValues[2] = hasErrors;
        }

        /* access modifiers changed from: 0000 */
        public int getDuration() {
            return this.duration;
        }

        /* access modifiers changed from: 0000 */
        public int getPlayedTime() {
            return this.played;
        }

        /* access modifiers changed from: 0000 */
        public boolean isPlaying() {
            return this.booleanValues[0];
        }

        /* access modifiers changed from: 0000 */
        public boolean isPrepared() {
            return this.booleanValues[1];
        }

        /* access modifiers changed from: 0000 */
        public boolean hasErrors() {
            return this.booleanValues[2];
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.duration);
            dest.writeInt(this.played);
            dest.writeBooleanArray(this.booleanValues);
        }

        public int describeContents() {
            return 0;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PlaybackState that = (PlaybackState) o;
            if (this.duration == that.duration && this.played == that.played) {
                return Arrays.equals(this.booleanValues, that.booleanValues);
            }
            return false;
        }

        public int hashCode() {
            if (this == UNPREPARED) {
                return 1;
            }
            if (this == FAILED) {
                return 2;
            }
            return (((this.duration * 31) + this.played) * 31) + Arrays.hashCode(this.booleanValues) + 2;
        }
    }

    private class PlayerThread extends Thread {
        private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                char c = 65535;
                switch (action.hashCode()) {
                    case -855999295:
                        if (action.equals(BackgroundPlayer.ACTION_STOP)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 279088972:
                        if (action.equals(BackgroundPlayer.ACTION_PLAYBACK_STATE)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1425944003:
                        if (action.equals(BackgroundPlayer.ACTION_PLAYPAUSE)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1976150458:
                        if (action.equals(BackgroundPlayer.ACTION_REWIND)) {
                            c = 1;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        if (PlayerThread.this.mediaPlayer.isPlaying()) {
                            PlayerThread.this.mediaPlayer.pause();
                        } else {
                            PlayerThread.this.mediaPlayer.setWakeMode(BackgroundPlayer.this.getApplicationContext(), 1);
                            PlayerThread.this.mediaPlayer.start();
                        }
                        synchronized (PlayerThread.this) {
                            PlayerThread.this.notifyAll();
                        }
                        return;
                    case 1:
                        PlayerThread.this.mediaPlayer.seekTo(0);
                        synchronized (PlayerThread.this) {
                            PlayerThread.this.notifyAll();
                        }
                        return;
                    case 2:
                        PlayerThread.this.mediaPlayer.stop();
                        PlayerThread.this.afterPlayCleanup();
                        return;
                    case 3:
                        PlaybackState playbackState = (PlaybackState) intent.getParcelableExtra(BackgroundPlayer.EXTRA_PLAYBACK_STATE);
                        if (!playbackState.equals(PlaybackState.UNPREPARED)) {
                            PlayerThread.this.noteBuilder.setProgress(playbackState.getDuration(), playbackState.getPlayedTime(), false);
                            PlayerThread.this.noteBuilder.setIsPlaying(playbackState.isPlaying());
                        } else {
                            PlayerThread.this.noteBuilder.setProgress(0, 0, true);
                        }
                        PlayerThread.this.noteMgr.notify(PlayerThread.this.noteID, PlayerThread.this.noteBuilder.build());
                        return;
                    default:
                        return;
                }
            }
        };
        private volatile boolean donePlaying = false;
        MediaPlayer mediaPlayer;
        /* access modifiers changed from: private */
        public NoteBuilder noteBuilder;
        /* access modifiers changed from: private */
        public int noteID = "BackgroundPlayer".hashCode();
        /* access modifiers changed from: private */
        public NotificationManager noteMgr;
        private BackgroundPlayer owner;
        private String source;
        private String title;
        /* access modifiers changed from: private */
        public Bitmap videoThumbnail;
        private WifiLock wifiLock;

        private class EndListener implements OnCompletionListener {
            private WifiLock wl;

            public EndListener(WifiLock wifiLock) {
                this.wl = wifiLock;
            }

            public void onCompletion(MediaPlayer mp) {
                PlayerThread.this.afterPlayCleanup();
            }
        }

        class NoteBuilder extends Builder {
            public NoteBuilder(Context context, PendingIntent playPI, PendingIntent stopPI, PendingIntent rewindPI, PendingIntent openDetailView) {
                super(context);
                setCustomContentView(createCustomContentView(playPI, stopPI, rewindPI, openDetailView));
                setCustomBigContentView(createCustomBigContentView(playPI, stopPI, rewindPI, openDetailView));
            }

            private RemoteViews createCustomBigContentView(PendingIntent playPI, PendingIntent stopPI, PendingIntent rewindPI, PendingIntent openDetailView) {
                RemoteViews expandedView = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.player_notification_expanded);
                expandedView.setImageViewBitmap(R.id.notificationCover, PlayerThread.this.videoThumbnail);
                expandedView.setOnClickPendingIntent(R.id.notificationStop, stopPI);
                expandedView.setOnClickPendingIntent(R.id.notificationPlayPause, playPI);
                expandedView.setOnClickPendingIntent(R.id.notificationRewind, rewindPI);
                expandedView.setOnClickPendingIntent(R.id.notificationContent, openDetailView);
                return expandedView;
            }

            private RemoteViews createCustomContentView(PendingIntent playPI, PendingIntent stopPI, PendingIntent rewindPI, PendingIntent openDetailView) {
                RemoteViews view = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.player_notification);
                view.setImageViewBitmap(R.id.notificationCover, PlayerThread.this.videoThumbnail);
                view.setOnClickPendingIntent(R.id.notificationStop, stopPI);
                view.setOnClickPendingIntent(R.id.notificationPlayPause, playPI);
                view.setOnClickPendingIntent(R.id.notificationRewind, rewindPI);
                view.setOnClickPendingIntent(R.id.notificationContent, openDetailView);
                return view;
            }

            /* access modifiers changed from: 0000 */
            public NoteBuilder setTitle(String title) {
                setContentTitle(title);
                getContentView().setTextViewText(R.id.notificationSongName, title);
                getBigContentView().setTextViewText(R.id.notificationSongName, title);
                setTicker(String.format(BackgroundPlayer.this.getBaseContext().getString(R.string.background_player_time_text), new Object[]{title}));
                return this;
            }

            /* access modifiers changed from: 0000 */
            public NoteBuilder setArtist(String artist) {
                setSubText(artist);
                getContentView().setTextViewText(R.id.notificationArtist, artist);
                getBigContentView().setTextViewText(R.id.notificationArtist, artist);
                return this;
            }

            public NotificationCompat.Builder setProgress(int max, int progress, boolean indeterminate) {
                super.setProgress(max, progress, indeterminate);
                getBigContentView().setProgressBar(R.id.playbackProgress, max, progress, indeterminate);
                return this;
            }

            public void setIsPlaying(boolean isPlaying) {
                int imageSrc;
                RemoteViews views = getContentView();
                RemoteViews bigViews = getBigContentView();
                if (isPlaying) {
                    imageSrc = R.drawable.ic_pause_white_24dp;
                } else {
                    imageSrc = R.drawable.ic_play_circle_filled_white_24dp;
                }
                views.setImageViewResource(R.id.notificationPlayPause, imageSrc);
                bigViews.setImageViewResource(R.id.notificationPlayPause, imageSrc);
            }
        }

        public PlayerThread(String src, String title2, BackgroundPlayer owner2) {
            this.source = src;
            this.title = title2;
            this.owner = owner2;
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setAudioStreamType(3);
        }

        public boolean isDonePlaying() {
            return this.donePlaying;
        }

        private boolean isPlaying() {
            try {
                return this.mediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                Log.w("BackgroundPlayer", "Unable to retrieve playing state", e);
                return false;
            }
        }

        private void setDonePlaying() {
            this.donePlaying = true;
            synchronized (this) {
                notifyAll();
            }
        }

        private synchronized PlaybackState getPlaybackState() {
            PlaybackState playbackState;
            try {
                playbackState = new PlaybackState(this.mediaPlayer.getDuration(), this.mediaPlayer.getCurrentPosition(), isPlaying());
            } catch (IllegalStateException e) {
                Log.w("BackgroundPlayer", this + ": Got illegal state exception while creating playback state", e);
                playbackState = PlaybackState.UNPREPARED;
            }
            return playbackState;
        }

        private void broadcastState() {
            PlaybackState state = getPlaybackState();
            if (state != null) {
                Intent intent = new Intent(BackgroundPlayer.ACTION_PLAYBACK_STATE);
                intent.putExtra(BackgroundPlayer.EXTRA_PLAYBACK_STATE, state);
                BackgroundPlayer.this.sendBroadcast(intent);
            }
        }

        public void run() {
            this.mediaPlayer.setWakeMode(BackgroundPlayer.this.getApplicationContext(), 1);
            try {
                this.mediaPlayer.setDataSource(this.source);
                this.mediaPlayer.prepare();
                try {
                    this.videoThumbnail = ActivityCommunicator.getCommunicator().backgroundPlayerThumbnail;
                } catch (Exception e) {
                    Log.e("BackgroundPlayer", "Could not get video thumbnail from ActivityCommunicator");
                    e.printStackTrace();
                }
                this.wifiLock = ((WifiManager) BackgroundPlayer.this.getSystemService(WIFI_SERVICE)).createWifiLock(1, "BackgroundPlayer");
                this.mediaPlayer.setOnCompletionListener(new EndListener(this.wifiLock));
                this.wifiLock.acquire();
                this.mediaPlayer.start();
                IntentFilter filter = new IntentFilter();
                filter.setPriority(Integer.MAX_VALUE);
                filter.addAction(BackgroundPlayer.ACTION_PLAYPAUSE);
                filter.addAction(BackgroundPlayer.ACTION_STOP);
                filter.addAction(BackgroundPlayer.ACTION_REWIND);
                filter.addAction(BackgroundPlayer.ACTION_PLAYBACK_STATE);
                BackgroundPlayer.this.registerReceiver(this.broadcastReceiver, filter);
                initNotificationBuilder();
                BackgroundPlayer.this.startForeground(this.noteID, this.noteBuilder.build());
                this.noteMgr = (NotificationManager) BackgroundPlayer.this.getSystemService(NOTIFICATION_SERVICE);
                int sleepTime = Math.min(2000, this.mediaPlayer.getDuration() / 4);
                while (!isDonePlaying()) {
                    broadcastState();
                    try {
                        synchronized (this) {
                            wait((long) sleepTime);
                        }
                    } catch (InterruptedException e2) {
                        Log.e("BackgroundPlayer", "sleep failure", e2);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.e("BackgroundPlayer", "video source:" + this.source);
                Log.e("BackgroundPlayer", "video title:" + this.title);
            }
        }

        /* access modifiers changed from: private */
        public void afterPlayCleanup() {
            setDonePlaying();
            this.noteMgr.cancel(this.noteID);
            BackgroundPlayer.this.unregisterReceiver(this.broadcastReceiver);
            this.mediaPlayer.release();
            this.wifiLock.release();
            BackgroundPlayer.this.stopForeground(true);
            try {
                join();
            } catch (InterruptedException e) {
                Log.e("BackgroundPlayer", "unable to join player thread", e);
            }
            BackgroundPlayer.this.stopSelf();
        }

        private void initNotificationBuilder() {
            Resources resources = BackgroundPlayer.this.getApplicationContext().getResources();
            PendingIntent playPI = PendingIntent.getBroadcast(this.owner, this.noteID, new Intent(BackgroundPlayer.ACTION_PLAYPAUSE), C.SAMPLE_FLAG_DECODE_ONLY);
            PendingIntent stopPI = PendingIntent.getBroadcast(this.owner, this.noteID, new Intent(BackgroundPlayer.ACTION_STOP), C.SAMPLE_FLAG_DECODE_ONLY);
            PendingIntent rewindPI = PendingIntent.getBroadcast(this.owner, this.noteID, new Intent(BackgroundPlayer.ACTION_REWIND), C.SAMPLE_FLAG_DECODE_ONLY);
            Intent openDetailViewIntent = new Intent(BackgroundPlayer.this.getApplicationContext(), VideoItemDetailActivity.class);
            openDetailViewIntent.putExtra("service_id", BackgroundPlayer.this.serviceId);
            openDetailViewIntent.putExtra("url", BackgroundPlayer.this.webUrl);
            //ToDo: Must fix
            //openDetailViewIntent.addFlags(ErrorDialogData.BINDER_CRASH);
            PendingIntent openDetailView = PendingIntent.getActivity(this.owner, this.noteID, openDetailViewIntent, C.SAMPLE_FLAG_DECODE_ONLY);
            this.noteBuilder = new NoteBuilder(this.owner, playPI, stopPI, rewindPI, openDetailView);
            this.noteBuilder.setTitle(this.title).setArtist(BackgroundPlayer.this.channelName).setOngoing(true).setDeleteIntent(stopPI).setSmallIcon(R.drawable.ic_play_circle_filled_white_24dp).setContentIntent(PendingIntent.getActivity(BackgroundPlayer.this.getApplicationContext(), this.noteID, openDetailViewIntent, C.SAMPLE_FLAG_DECODE_ONLY)).setContentIntent(openDetailView).setCategory(NotificationCompat.CATEGORY_TRANSPORT).setVisibility(1);
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, R.string.background_player_playing_toast, 0).show();
        String source = intent.getDataString();
        String videoTitle = intent.getStringExtra(TITLE);
        this.webUrl = intent.getStringExtra(WEB_URL);
        this.serviceId = intent.getIntExtra("service_id", -1);
        this.channelName = intent.getStringExtra(CHANNEL_NAME);
        new PlayerThread(source, videoTitle, this).start();
        isRunning = true;
        return 2;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        isRunning = false;
    }
}
