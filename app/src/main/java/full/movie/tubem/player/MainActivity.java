package full.movie.tubem.player;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import full.movie.tubem.player.download.DownloadActivity;
import full.movie.tubem.player.settings.SettingsActivity;
import full.movie.tubem.player.util.PermissionHelper;
import org.acra.ACRAConstants;

public class MainActivity extends ThemableActivity {
    private static final String TAG = MainActivity.class.toString();
    /* access modifiers changed from: private */
    public InterstitialAd interstitial;
    private Fragment mainFragment = null;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        setRequestedOrientation(14);
        setVolumeControlStream(3);
        this.mainFragment = getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        this.interstitial = new InterstitialAd(this);
        this.interstitial.setAdUnitId(getString(R.string.interstitial_full_screen));
        ((AdView) findViewById(R.id.adView)).loadAd(new Builder().addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB").build());
        this.interstitial.loadAd(new Builder().addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB").build());
        this.interstitial.setAdListener(new AdListener() {
            public void onAdClosed() {
                MainActivity.this.interstitial.loadAd(new Builder().addTestDevice("B3EEABB8EE11C2BE770B684D95219ECB").build());
            }
        });
    }

    public void displayInterstitial() {
        if (this.interstitial.isLoaded()) {
            this.interstitial.show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(67108864);
                NavUtils.navigateUpTo(this, intent);
                displayInterstitial();
                return true;
            case R.id.action_settings /*2131689771*/:
                startActivity(new Intent(this, SettingsActivity.class));
                displayInterstitial();
                return true;
            case R.id.action_show_downloads /*2131689773*/:
                if (!PermissionHelper.checkStoragePermissions(this)) {
                    return false;
                }
                startActivity(new Intent(this, DownloadActivity.class));
                displayInterstitial();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed() {
        /*new AlertDialog.Builder(this).setIcon((int) ACRAConstants.DEFAULT_DIALOG_ICON).setTitle((CharSequence) "Exit").setMessage((CharSequence) "Are you sure you want to exit?").setPositiveButton((CharSequence) "Yes", (OnClickListener) new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
                System.exit(0);
            }
        }).setNegativeButton((CharSequence) "No", null).show();
        displayInterstitial();*/
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .threshold(4)
                .session(1)
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        finish();
                    }
                }).build();

        ratingDialog.show();
    }
}
