package full.movie.tubem.player.download;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.InterstitialAd;
import com.online.garam.service.DownloadManagerService;
import com.online.garam.ui.fragment.AllMissionsFragment;
import com.online.garam.ui.fragment.MissionsFragment;
import com.online.garam.util.CrashHandler;
import com.online.garam.util.Utility;
import full.movie.tubem.player.R;
import full.movie.tubem.player.ThemableActivity;
import full.movie.tubem.player.settings.NewSettings;
import full.movie.tubem.player.settings.SettingsActivity;
import java.io.File;

public class DownloadActivity extends ThemableActivity implements OnItemClickListener {
    public static final String INTENT_DOWNLOAD = "org.dfor.downloader.intent.DOWNLOAD";
    public static final String INTENT_LIST = "org.dfor.downloader.intent.LIST";
    private static final String TAG = DownloadActivity.class.toString();
    public static final String THREADS = "threads";
    /* access modifiers changed from: private */
    public MissionsFragment mFragment;
    InterstitialAd mInterstitialAd;
    /* access modifiers changed from: private */
    public String mPendingUrl;
    /* access modifiers changed from: private */
    public SharedPreferences mPrefs;

    /* access modifiers changed from: protected */
    @TargetApi(21)
    public void onCreate(Bundle savedInstanceState) {
        CrashHandler.init(this);
        CrashHandler.register();
        this.mInterstitialAd = new InterstitialAd(this);
        this.mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        this.mInterstitialAd.loadAd(new Builder().build());
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_downloader);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle((int) R.string.downloads_title);
        actionBar.setDisplayShowTitleEnabled(true);
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                DownloadActivity.this.updateFragments();
                DownloadActivity.this.getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        if (getIntent().getAction() != null && getIntent().getAction().equals(INTENT_DOWNLOAD)) {
            this.mPendingUrl = getIntent().getData().toString();
        }
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction().equals(INTENT_DOWNLOAD)) {
            this.mPendingUrl = intent.getData().toString();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (this.mPendingUrl != null) {
            showUrlDialog();
            this.mPendingUrl = null;
        }
    }

    /* access modifiers changed from: private */
    public void updateFragments() {
        this.mFragment = new AllMissionsFragment();
        getFragmentManager().beginTransaction().replace(R.id.frame, this.mFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

    private void showUrlDialog() {
        View v = ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.dialog_url, null);
        final EditText name = (EditText) Utility.findViewById(v, (int) R.id.file_name);
        final TextView tCount = (TextView) Utility.findViewById(v, (int) R.id.threads_count);
        final SeekBar threads = (SeekBar) Utility.findViewById(v, (int) R.id.threads);
        Toolbar toolbar = (Toolbar) Utility.findViewById(v, (int) R.id.toolbar);
        final RadioButton audioButton = (RadioButton) Utility.findViewById(v, (int) R.id.audio_button);
        threads.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                tCount.setText(String.valueOf(progress + 1));
            }

            public void onStartTrackingTouch(SeekBar p1) {
            }

            public void onStopTrackingTouch(SeekBar p1) {
            }
        });
        int def = this.mPrefs.getInt(THREADS, 4);
        threads.setProgress(def - 1);
        tCount.setText(String.valueOf(def));
        name.setText(getIntent().getStringExtra("fileName"));
        toolbar.setTitle((int) R.string.add);
        toolbar.setNavigationIcon((int) R.drawable.ic_arrow_back_black_24dp);
        toolbar.inflateMenu(R.menu.dialog_url);
        final AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(true).setView(v).create();
        dialog.show();
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String location;
                if (item.getItemId() != R.id.okay) {
                    return false;
                }
                if (audioButton.isChecked()) {
                    location = NewSettings.getAudioDownloadPath(DownloadActivity.this);
                } else {
                    location = NewSettings.getVideoDownloadPath(DownloadActivity.this);
                }
                String fName = name.getText().toString().trim();
                if (new File(location, fName).exists()) {
                    Toast.makeText(DownloadActivity.this, R.string.msg_exists, 0).show();
                } else {
                    DownloadManagerService.startMission(DownloadActivity.this, DownloadActivity.this.getIntent().getData().toString(), location, fName, audioButton.isChecked(), threads.getProgress() + 1);
                    DownloadActivity.this.mFragment.notifyChange();
                    DownloadActivity.this.mPrefs.edit().putInt(DownloadActivity.THREADS, threads.getProgress() + 1).commit();
                    DownloadActivity.this.mPendingUrl = null;
                    dialog.dismiss();
                }
                return true;
            }
        });
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        showInterstitial();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.download_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                return true;
            case R.id.action_settings /*2131689771*/:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInterstitial() {
        if (this.mInterstitialAd.isLoaded()) {
            this.mInterstitialAd.show();
        }
    }
}
