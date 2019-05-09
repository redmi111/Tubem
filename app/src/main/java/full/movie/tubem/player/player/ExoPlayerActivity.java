package full.movie.tubem.player.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.accessibility.CaptioningManager;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver.Listener;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.GeobMetadata;
import com.google.android.exoplayer.metadata.PrivMetadata;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.Util;
import full.movie.tubem.player.R;
import full.movie.tubem.player.player.exoplayer.EventLogger;
import full.movie.tubem.player.player.exoplayer.ExtractorRendererBuilder;
import full.movie.tubem.player.player.exoplayer.HlsRendererBuilder;
import full.movie.tubem.player.player.exoplayer.NPExoPlayer;
import full.movie.tubem.player.player.exoplayer.NPExoPlayer.CaptionListener;
import full.movie.tubem.player.player.exoplayer.NPExoPlayer.Id3MetadataListener;
import full.movie.tubem.player.player.exoplayer.NPExoPlayer.RendererBuilder;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Fixme: player package have classes with errors
public class ExoPlayerActivity extends Activity {
    private static final String CONTENT_EXT_EXTRA = "type";
    public static final String CONTENT_ID_EXTRA = "content_id";
    public static final String CONTENT_TYPE_EXTRA = "content_type";
    private static final int ID_OFFSET = 2;
    private static final int MENU_GROUP_TRACKS = 1;
    public static final String PROVIDER_EXTRA = "provider";
    private static final String TAG = "PlayerActivity";
    private static final CookieManager defaultCookieManager = new CookieManager();
    Listener audioCapabilitiesListener = new Listener() {
        public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
            if (ExoPlayerActivity.this.player != null) {
                boolean backgrounded = ExoPlayerActivity.this.player.getBackgrounded();
                boolean playWhenReady = ExoPlayerActivity.this.player.getPlayWhenReady();
                ExoPlayerActivity.this.releasePlayer();
                ExoPlayerActivity.this.preparePlayer(playWhenReady);
                ExoPlayerActivity.this.player.setBackgrounded(backgrounded);
            }
        }
    };
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    CaptionListener captionListener = new CaptionListener() {
        public void onCues(List<Cue> cues) {
            ExoPlayerActivity.this.subtitleLayout.setCues(cues);
        }
    };
    private String contentId;
    private int contentType;
    private Uri contentUri;
    private boolean enableBackgroundAudio = true;
    private EventLogger eventLogger;
    NPExoPlayer.Listener exoPlayerListener = new NPExoPlayer.Listener() {
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == 5) {
                ExoPlayerActivity.this.showControls();
            }
            String text = "playWhenReady=" + playWhenReady + ", playbackState=";
            switch (playbackState) {
                case 1:
                    String text2 = text + "idle";
                    return;
                case 2:
                    String text3 = text + "preparing";
                    return;
                case 3:
                    String text4 = text + "buffering";
                    return;
                case 4:
                    String text5 = text + "ready";
                    return;
                case 5:
                    String text6 = text + "ended";
                    return;
                default:
                    String text7 = text + EnvironmentCompat.MEDIA_UNKNOWN;
                    return;
            }
        }

        public void onError(Exception e) {
            String errorString = null;
            if (e instanceof UnsupportedDrmException) {
                UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
                ExoPlayerActivity exoPlayerActivity = ExoPlayerActivity.this;
                int i = Util.SDK_INT < 18 ? R.string.error_drm_not_supported : unsupportedDrmException.reason == 1 ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
                errorString = exoPlayerActivity.getString(i);
            } else if ((e instanceof ExoPlaybackException) && (e.getCause() instanceof DecoderInitializationException)) {
                DecoderInitializationException decoderInitializationException = (DecoderInitializationException) e.getCause();
                errorString = decoderInitializationException.decoderName == null ? decoderInitializationException.getCause() instanceof DecoderQueryException ? ExoPlayerActivity.this.getString(R.string.error_querying_decoders) : decoderInitializationException.secureDecoderRequired ? ExoPlayerActivity.this.getString(R.string.error_no_secure_decoder, new Object[]{decoderInitializationException.mimeType}) : ExoPlayerActivity.this.getString(R.string.error_no_decoder, new Object[]{decoderInitializationException.mimeType}) : ExoPlayerActivity.this.getString(R.string.error_instantiating_decoder, new Object[]{decoderInitializationException.decoderName});
            }
            if (errorString != null) {
                Toast.makeText(ExoPlayerActivity.this.getApplicationContext(), errorString, 1).show();
            }
            ExoPlayerActivity.this.playerNeedsPrepare = true;
            ExoPlayerActivity.this.showControls();
        }

        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {
            ExoPlayerActivity.this.shutterView.setVisibility(8);
            ExoPlayerActivity.this.videoFrame.setAspectRatio(height == 0 ? 1.0f : (((float) width) * pixelWidthAspectRatio) / ((float) height));
        }
    };
    Id3MetadataListener id3MetadataListener = new Id3MetadataListener() {
        public void onId3Metadata(Map<String, Object> metadata) {
            for (Entry<String, Object> entry : metadata.entrySet()) {
                if (TxxxMetadata.TYPE.equals(entry.getKey())) {
                    TxxxMetadata txxxMetadata = (TxxxMetadata) entry.getValue();
                    Log.i(ExoPlayerActivity.TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", new Object[]{TxxxMetadata.TYPE, txxxMetadata.description, txxxMetadata.value}));
                } else if (PrivMetadata.TYPE.equals(entry.getKey())) {
                    PrivMetadata privMetadata = (PrivMetadata) entry.getValue();
                    Log.i(ExoPlayerActivity.TAG, String.format("ID3 TimedMetadata %s: owner=%s", new Object[]{PrivMetadata.TYPE, privMetadata.owner}));
                } else if (GeobMetadata.TYPE.equals(entry.getKey())) {
                    GeobMetadata geobMetadata = (GeobMetadata) entry.getValue();
                    Log.i(ExoPlayerActivity.TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s", new Object[]{GeobMetadata.TYPE, geobMetadata.mimeType, geobMetadata.filename, geobMetadata.description}));
                } else {
                    Log.i(ExoPlayerActivity.TAG, String.format("ID3 TimedMetadata %s", new Object[]{entry.getKey()}));
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public MediaController mediaController;
    /* access modifiers changed from: private */
    public NPExoPlayer player;
    /* access modifiers changed from: private */
    public boolean playerNeedsPrepare;
    private long playerPosition;
    private String provider;
    /* access modifiers changed from: private */
    public View shutterView;
    /* access modifiers changed from: private */
    public SubtitleLayout subtitleLayout;
    Callback surfaceHolderCallback = new Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            if (ExoPlayerActivity.this.player != null) {
                ExoPlayerActivity.this.player.setSurface(holder.getSurface());
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (ExoPlayerActivity.this.player != null) {
                ExoPlayerActivity.this.player.blockingClearSurface();
            }
        }
    };
    private SurfaceView surfaceView;
    /* access modifiers changed from: private */
    public AspectRatioFrameLayout videoFrame;

    private static final class KeyCompatibleMediaController extends MediaController {
        private MediaPlayerControl playerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        public void setMediaPlayer(MediaPlayerControl playerControl2) {
            super.setMediaPlayer(playerControl2);
            this.playerControl = playerControl2;
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (!this.playerControl.canSeekForward() || keyCode != 90) {
                if (!this.playerControl.canSeekBackward() || keyCode != 89) {
                    return super.dispatchKeyEvent(event);
                }
                if (event.getAction() != 0) {
                    return true;
                }
                this.playerControl.seekTo(this.playerControl.getCurrentPosition() - 5000);
                show();
                return true;
            } else if (event.getAction() != 0) {
                return true;
            } else {
                this.playerControl.seekTo(this.playerControl.getCurrentPosition() + DefaultLoadControl.DEFAULT_LOW_WATERMARK_MS);
                show();
                return true;
            }
        }
    }

    static {
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exo_player_activity);
        View root = findViewById(R.id.root);
        root.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    ExoPlayerActivity.this.toggleControlsVisibility();
                } else if (motionEvent.getAction() == 1) {
                    view.performClick();
                }
                return true;
            }
        });
        root.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 4 || keyCode == 111 || keyCode == 82) {
                    return false;
                }
                return ExoPlayerActivity.this.mediaController.dispatchKeyEvent(event);
            }
        });
        this.shutterView = findViewById(R.id.shutter);
        this.videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        this.surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        this.surfaceView.getHolder().addCallback(this.surfaceHolderCallback);
        this.subtitleLayout = (SubtitleLayout) findViewById(R.id.subtitles);
        this.mediaController = new KeyCompatibleMediaController(this);
        this.mediaController.setAnchorView(root);
        if (CookieHandler.getDefault() != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
        this.audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this.audioCapabilitiesListener);
        this.audioCapabilitiesReceiver.register();
    }

    public void onNewIntent(Intent intent) {
        releasePlayer();
        this.playerPosition = 0;
        setIntent(intent);
    }

    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        this.contentUri = intent.getData();
        this.contentType = intent.getIntExtra(CONTENT_TYPE_EXTRA, inferContentType(this.contentUri, intent.getStringExtra(CONTENT_EXT_EXTRA)));
        this.contentId = intent.getStringExtra(CONTENT_ID_EXTRA);
        this.provider = intent.getStringExtra(PROVIDER_EXTRA);
        configureSubtitleView();
        if (this.player != null) {
            this.player.setBackgrounded(false);
        } else if (!maybeRequestPermission()) {
            preparePlayer(true);
        }
    }

    public void onPause() {
        super.onPause();
        if (!this.enableBackgroundAudio) {
            releasePlayer();
        } else {
            this.player.setBackgrounded(true);
        }
        this.shutterView.setVisibility(0);
    }

    public void onDestroy() {
        super.onDestroy();
        this.audioCapabilitiesReceiver.unregister();
        releasePlayer();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length <= 0 || grantResults[0] != 0) {
            Toast.makeText(getApplicationContext(), R.string.storage_permission_denied, 1).show();
            finish();
            return;
        }
        preparePlayer(true);
    }

    @TargetApi(23)
    private boolean maybeRequestPermission() {
        if (!requiresPermission(this.contentUri)) {
            return false;
        }
        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 0);
        return true;
    }

    @TargetApi(23)
    private boolean requiresPermission(Uri uri) {
        return Util.SDK_INT >= 23 && Util.isLocalFileUri(uri) && checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0;
    }

    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "NewExoPlayer");
        switch (this.contentType) {
            case 0:
            case 1:
            case 2:
                return new HlsRendererBuilder(this, userAgent, this.contentUri.toString());
            case 3:
                return new ExtractorRendererBuilder(this, userAgent, this.contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + this.contentType);
        }
    }

    /* access modifiers changed from: private */
    public void preparePlayer(boolean playWhenReady) {
        if (this.player == null) {
            this.player = new NPExoPlayer(getRendererBuilder());
            this.player.addListener(this.exoPlayerListener);
            this.player.setCaptionListener(this.captionListener);
            this.player.setMetadataListener(this.id3MetadataListener);
            this.player.seekTo(this.playerPosition);
            this.playerNeedsPrepare = true;
            this.mediaController.setMediaPlayer(this.player.getPlayerControl());
            this.mediaController.setEnabled(true);
            this.eventLogger = new EventLogger();
            this.eventLogger.startSession();
            this.player.addListener(this.eventLogger);
            this.player.setInfoListener(this.eventLogger);
            this.player.setInternalErrorListener(this.eventLogger);
        }
        if (this.playerNeedsPrepare) {
            this.player.prepare();
            this.playerNeedsPrepare = false;
        }
        this.player.setSurface(this.surfaceView.getHolder().getSurface());
        this.player.setPlayWhenReady(playWhenReady);
    }

    /* access modifiers changed from: private */
    public void releasePlayer() {
        if (this.player != null) {
            this.playerPosition = this.player.getCurrentPosition();
            this.player.release();
            this.player = null;
            this.eventLogger.endSession();
            this.eventLogger = null;
        }
    }

    /* access modifiers changed from: private */
    public void toggleControlsVisibility() {
        if (this.mediaController.isShowing()) {
            this.mediaController.hide();
        } else {
            showControls();
        }
    }

    /* access modifiers changed from: private */
    public void showControls() {
        this.mediaController.show(0);
    }

    private void configureSubtitleView() {
        CaptionStyleCompat style;
        float fontScale;
        if (Util.SDK_INT >= 19) {
            style = getUserCaptionStyleV19();
            fontScale = getUserCaptionFontScaleV19();
        } else {
            style = CaptionStyleCompat.DEFAULT;
            fontScale = 1.0f;
        }
        this.subtitleLayout.setStyle(style);
        this.subtitleLayout.setFractionalTextSize(0.0533f * fontScale);
    }

    @TargetApi(19)
    private float getUserCaptionFontScaleV19() {
        return ((CaptioningManager) getSystemService("captioning")).getFontScale();
    }

    @TargetApi(19)
    private CaptionStyleCompat getUserCaptionStyleV19() {
        return CaptionStyleCompat.createFromCaptionStyle(((CaptioningManager) getSystemService("captioning")).getUserStyle());
    }

    private static int inferContentType(Uri uri, String fileExtension) {
        String lastPathSegment;
        if (!TextUtils.isEmpty(fileExtension)) {
            lastPathSegment = "." + fileExtension;
        } else {
            lastPathSegment = uri.getLastPathSegment();
        }
        return Util.inferContentType(lastPathSegment);
    }
}
