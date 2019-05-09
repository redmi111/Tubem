package full.movie.tubem.player.detail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import full.movie.tubem.player.R;
import full.movie.tubem.player.download.DownloadActivity;
import full.movie.tubem.player.extractor.MediaFormat;
import full.movie.tubem.player.extractor.stream_info.VideoStream;
import full.movie.tubem.player.settings.SettingsActivity;
import java.util.List;

class ActionBarHandler {
    static final /* synthetic */ boolean $assertionsDisabled = (!ActionBarHandler.class.desiredAssertionStatus());
    private static final String TAG = ActionBarHandler.class.toString();
    private AppCompatActivity activity;
    private SharedPreferences defaultPreferences;
    private Menu menu;
    private OnActionListener onDownloadListener;
    private OnActionListener onOpenInBrowserListener;
    private OnActionListener onPlayAudioListener;
    private OnActionListener onPlayWithKodiListener;
    private OnActionListener onShareListener;
    /* access modifiers changed from: private */
    public int selectedVideoStream = -1;

    public interface OnActionListener {
        void onActionSelected(int i);
    }

    public ActionBarHandler(AppCompatActivity activity2) {
        this.activity = activity2;
    }

    public void setupNavMenu(AppCompatActivity activity2) {
        this.activity = activity2;
        try {
            activity2.getSupportActionBar().setNavigationMode(1);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setupStreamList(List<VideoStream> videoStreams) {
        if (this.activity != null) {
            this.selectedVideoStream = 0;
            String[] itemArray = new String[videoStreams.size()];
            for (int i = 0; i < videoStreams.size(); i++) {
                VideoStream item = (VideoStream) videoStreams.get(i);
                itemArray[i] = MediaFormat.getNameById(item.format) + " " + item.resolution;
            }
            int defaultResolution = getDefaultResolution(videoStreams);
            ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this.activity.getBaseContext(), 17367049, itemArray);
            ActionBar ab = this.activity.getSupportActionBar();
            if ($assertionsDisabled || ab != null) {
                ab.setListNavigationCallbacks(itemAdapter, new OnNavigationListener() {
                    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                        ActionBarHandler.this.selectedVideoStream = (int) itemId;
                        return true;
                    }
                });
                ab.setSelectedNavigationItem(defaultResolution);
                return;
            }
            throw new AssertionError("Could not get actionbar");
        }
    }

    private int getDefaultResolution(List<VideoStream> videoStreams) {
        if (this.defaultPreferences == null) {
            return 0;
        }
        String defaultResolution = this.defaultPreferences.getString(this.activity.getString(R.string.default_resolution_key), this.activity.getString(R.string.default_resolution_value));
        for (int i = 0; i < videoStreams.size(); i++) {
            if (defaultResolution.equals(((VideoStream) videoStreams.get(i)).resolution)) {
                return i;
            }
        }
        return 0;
    }

    public void setupMenu(Menu menu2, MenuInflater inflater) {
        this.menu = menu2;
        this.defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity);
        inflater.inflate(R.menu.videoitem_detail, menu2);
        showPlayWithKodiAction(this.defaultPreferences.getBoolean(this.activity.getString(R.string.show_play_with_kodi_key), false));
    }

    public boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings /*2131689771*/:
                this.activity.startActivity(new Intent(this.activity, SettingsActivity.class));
                return true;
            case R.id.menu_item_play_audio /*2131689787*/:
                if (this.onPlayAudioListener == null) {
                    return true;
                }
                this.onPlayAudioListener.onActionSelected(this.selectedVideoStream);
                return true;
            case R.id.menu_item_download /*2131689788*/:
                if (this.onDownloadListener == null) {
                    return true;
                }
                this.onDownloadListener.onActionSelected(this.selectedVideoStream);
                return true;
            case R.id.action_play_with_kodi /*2131689789*/:
                if (this.onPlayWithKodiListener == null) {
                    return true;
                }
                this.onPlayWithKodiListener.onActionSelected(this.selectedVideoStream);
                return true;
            case R.id.menu_item_downloads /*2131689790*/:
                this.activity.startActivity(new Intent(this.activity, DownloadActivity.class));
                return true;
            default:
                Log.e(TAG, "Menu Item not known");
                return false;
        }
    }

    public int getSelectedVideoStream() {
        return this.selectedVideoStream;
    }

    public void setOnShareListener(OnActionListener listener) {
        this.onShareListener = listener;
    }

    public void setOnOpenInBrowserListener(OnActionListener listener) {
        this.onOpenInBrowserListener = listener;
    }

    public void setOnDownloadListener(OnActionListener listener) {
        this.onDownloadListener = listener;
    }

    public void setOnPlayWithKodiListener(OnActionListener listener) {
        this.onPlayWithKodiListener = listener;
    }

    public void setOnPlayAudioListener(OnActionListener listener) {
        this.onPlayAudioListener = listener;
    }

    public void showAudioAction(boolean visible) {
        this.menu.findItem(R.id.menu_item_play_audio).setVisible(visible);
    }

    public void showDownloadAction(boolean visible) {
        this.menu.findItem(R.id.menu_item_download).setVisible(visible);
    }

    public void showPlayWithKodiAction(boolean visible) {
        this.menu.findItem(R.id.action_play_with_kodi).setVisible(visible);
    }
}
