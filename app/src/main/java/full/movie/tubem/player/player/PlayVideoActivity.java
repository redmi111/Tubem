package full.movie.tubem.player.player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;
import full.movie.tubem.player.R;

public class PlayVideoActivity extends AppCompatActivity {
    static final /* synthetic */ boolean $assertionsDisabled = (!PlayVideoActivity.class.desiredAssertionStatus());
    private static final long HIDING_DELAY = 3000;
    private static final String POSITION = "position";
    private static final String PREF_IS_LANDSCAPE = "is_landscape";
    public static final String START_POSITION = "start_position";
    public static final String STREAM_URL = "stream_url";
    private static final String TAG = PlayVideoActivity.class.toString();
    public static final String VIDEO_TITLE = "video_title";
    public static final String VIDEO_URL = "video_url";
    /* access modifiers changed from: private|static */
    public static long lastUiShowTime;
    private ActionBar actionBar;
    private View decorView;
    private boolean hasSoftKeys;
    private boolean isLandscape = true;
    private MediaController mediaController;
    /* access modifiers changed from: private */
    public int position;
    private SharedPreferences prefs;
    /* access modifiers changed from: private */
    public ProgressBar progressBar;
    /* access modifiers changed from: private */
    public boolean uiIsHidden;
    private String videoUrl = "";
    /* access modifiers changed from: private */
    public VideoView videoView;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_play_video);
        setVolumeControlStream(3);
        getSupportActionBar().setHomeAsUpIndicator((int) R.drawable.ic_arrow_back_white_24dp);
        this.isLandscape = checkIfLandscape();
        this.hasSoftKeys = checkIfHasSoftKeys();
        this.actionBar = getSupportActionBar();
        if ($assertionsDisabled || this.actionBar != null) {
            this.actionBar.setDisplayHomeAsUpEnabled(true);
            Intent intent = getIntent();
            if (this.mediaController == null) {
                this.mediaController = new MediaController(this) {
                    public boolean dispatchKeyEvent(KeyEvent event) {
                        int keyCode = event.getKeyCode();
                        boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == 0;
                        if (keyCode != 4) {
                            return super.dispatchKeyEvent(event);
                        }
                        if (!uniqueDown) {
                            return true;
                        }
                        if (isShowing()) {
                            PlayVideoActivity.this.finish();
                            return true;
                        }
                        hide();
                        return true;
                    }
                };
            }
            this.position = intent.getIntExtra(START_POSITION, 0) * 1000;
            this.videoView = (VideoView) findViewById(R.id.video_view);
            this.progressBar = (ProgressBar) findViewById(R.id.play_video_progress_bar);
            try {
                this.videoView.setMediaController(this.mediaController);
                this.videoView.setVideoURI(Uri.parse(intent.getStringExtra(STREAM_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.videoView.requestFocus();
            this.videoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    PlayVideoActivity.this.progressBar.setVisibility(8);
                    PlayVideoActivity.this.videoView.seekTo(PlayVideoActivity.this.position);
                    if (PlayVideoActivity.this.position <= 0) {
                        PlayVideoActivity.this.videoView.start();
                        PlayVideoActivity.this.showUi();
                        return;
                    }
                    PlayVideoActivity.this.videoView.pause();
                }
            });
            this.videoUrl = intent.getStringExtra("video_url");
            ((Button) findViewById(R.id.content_button)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (PlayVideoActivity.this.uiIsHidden) {
                        PlayVideoActivity.this.showUi();
                    } else {
                        PlayVideoActivity.this.hideUi();
                    }
                }
            });
            this.decorView = getWindow().getDecorView();
            this.decorView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == 0 && PlayVideoActivity.this.uiIsHidden) {
                        PlayVideoActivity.this.showUi();
                    }
                }
            });
            if (VERSION.SDK_INT >= 17) {
                this.decorView.setSystemUiVisibility(1792);
            }
            this.prefs = getPreferences(0);
            if (this.prefs.getBoolean(PREF_IS_LANDSCAPE, false) && !this.isLandscape) {
                toggleOrientation();
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    public boolean onCreatePanelMenu(int featured, Menu menu) {
        super.onCreatePanelMenu(featured, menu);
        getMenuInflater().inflate(R.menu.video_player, menu);
        return true;
    }

    public void onPause() {
        super.onPause();
        this.videoView.pause();
    }

    public void onResume() {
        super.onResume();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.prefs = getPreferences(0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
            case R.id.menu_item_screen_rotation /*2131689786*/:
                toggleOrientation();
                break;
            default:
                Log.e(TAG, "Error: MenuItem not known");
                return false;
        }
        return true;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (config.orientation == 2) {
            this.isLandscape = true;
            adjustMediaControlMetrics();
        } else if (config.orientation == 1) {
            this.isLandscape = false;
            adjustMediaControlMetrics();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.position = savedInstanceState.getInt(POSITION);
    }

    /* access modifiers changed from: private */
    public void showUi() {
        try {
            this.uiIsHidden = false;
            this.mediaController.show(100000);
            this.actionBar.show();
            adjustMediaControlMetrics();
            getWindow().clearFlags(1024);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (System.currentTimeMillis() - PlayVideoActivity.lastUiShowTime >= PlayVideoActivity.HIDING_DELAY) {
                        PlayVideoActivity.this.hideUi();
                    }
                }
            }, HIDING_DELAY);
            lastUiShowTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void hideUi() {
        this.uiIsHidden = true;
        this.actionBar.hide();
        this.mediaController.hide();
        if (VERSION.SDK_INT >= 17) {
            this.decorView.setSystemUiVisibility(1798);
        }
        getWindow().setFlags(1024, 1024);
    }

    private void adjustMediaControlMetrics() {
        LayoutParams mediaControllerLayout = new LayoutParams(-1, -2);
        if (!this.hasSoftKeys) {
            mediaControllerLayout.setMargins(20, 0, 20, 20);
        } else {
            int width = getNavigationBarWidth();
            mediaControllerLayout.setMargins(width + 20, 0, width + 20, getNavigationBarHeight() + 20);
        }
        this.mediaController.setLayoutParams(mediaControllerLayout);
    }

    private boolean checkIfHasSoftKeys() {
        return (VERSION.SDK_INT < 17 && getNavigationBarHeight() == 0 && getNavigationBarWidth() == 0) ? false : true;
    }

    private int getNavigationBarHeight() {
        if (VERSION.SDK_INT < 17) {
            return 50;
        }
        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        return realDisplayMetrics.heightPixels - displayMetrics.heightPixels;
    }

    private int getNavigationBarWidth() {
        if (VERSION.SDK_INT < 17) {
            return 50;
        }
        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        return realDisplayMetrics.widthPixels - displayMetrics.widthPixels;
    }

    private boolean checkIfLandscape() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels < displayMetrics.widthPixels;
    }

    private void toggleOrientation() {
        if (this.isLandscape) {
            this.isLandscape = false;
            setRequestedOrientation(1);
        } else {
            this.isLandscape = true;
            setRequestedOrientation(6);
        }
        Editor editor = this.prefs.edit();
        editor.putBoolean(PREF_IS_LANDSCAPE, this.isLandscape);
        editor.apply();
    }
}
