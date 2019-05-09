package full.movie.tubem.player.detail;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import full.movie.tubem.player.App;
import full.movie.tubem.player.R;
import full.movie.tubem.player.ThemableActivity;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.StreamingService;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.util.NavStack;
import java.util.Collection;
import java.util.HashSet;

public class VideoItemDetailActivity extends ThemableActivity {
    private static final String REGEX_REMOVE_FROM_URL = "[\\p{Z}\\p{P}]";
    private static final String TAG = VideoItemDetailActivity.class.toString();
    private int currentStreamingService = -1;
    private VideoItemDetailFragment fragment;
    private String videoUrl;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_videoitem_detail);
        setVolumeControlStream(3);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Log.d(TAG, "Could not get SupportActionBar");
            e.printStackTrace();
        }
        if (savedInstanceState == null) {
            handleIntent(getIntent());
            return;
        }
        this.videoUrl = savedInstanceState.getString("url");
        this.currentStreamingService = savedInstanceState.getInt("service_id");
        NavStack.getInstance().restoreSavedInstanceState(savedInstanceState);
        addFragment(savedInstanceState);
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Bundle arguments = new Bundle();
        boolean autoplay = false;
        if (intent.getData() != null) {
            this.videoUrl = intent.getData().toString();
            this.currentStreamingService = getServiceIdByUrl(this.videoUrl);
            if (this.currentStreamingService == -1) {
                Toast.makeText(this, R.string.url_not_supported_toast, 1).show();
            }
            autoplay = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.autoplay_through_intent_key), false);
        } else if (intent.getStringExtra("android.intent.extra.TEXT") != null) {
            this.videoUrl = getUris(intent.getStringExtra("android.intent.extra.TEXT"))[0];
            this.currentStreamingService = getServiceIdByUrl(this.videoUrl);
        } else {
            this.videoUrl = intent.getStringExtra("url");
            this.currentStreamingService = intent.getIntExtra("service_id", -1);
        }
        arguments.putBoolean(VideoItemDetailFragment.AUTO_PLAY, autoplay);
        arguments.putString("url", this.videoUrl);
        arguments.putInt("service_id", this.currentStreamingService);
        addFragment(arguments);
    }

    private void addFragment(Bundle arguments) {
        this.fragment = new VideoItemDetailFragment();
        this.fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction().replace(R.id.videoitem_detail_container, this.fragment).commit();
    }

    public void onResume() {
        super.onResume();
        App.checkStartTor(this);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", this.videoUrl);
        outState.putInt("service_id", this.currentStreamingService);
        outState.putBoolean(VideoItemDetailFragment.AUTO_PLAY, false);
        NavStack.getInstance().onSaveInstanceState(outState);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        NavStack.getInstance().openMainActivity(this);
        return true;
    }

    public void onBackPressed() {
        try {
            NavStack.getInstance().navBack(this);
        } catch (Exception e) {
            ErrorActivity.reportUiError(this, e);
        }
    }

    private String[] getUris(String sharedText) {
        Collection<String> result = new HashSet<>();
        if (sharedText != null) {
            for (String s : sharedText.split("\\p{Space}")) {
                String s2 = trim(s);
                if (s2.length() != 0) {
                    if (s2.matches(".+://.+")) {
                        result.add(removeHeadingGibberish(s2));
                    } else if (s2.matches(".+\\..+")) {
                        result.add("http://" + s2);
                    }
                }
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    private static String removeHeadingGibberish(String input) {
        int start = 0;
        int i = input.indexOf("://") - 1;
        while (true) {
            if (i < 0) {
                break;
            } else if (!input.substring(i, i + 1).matches("\\p{L}")) {
                start = i + 1;
                break;
            } else {
                i--;
            }
        }
        return input.substring(start, input.length());
    }

    private static String trim(String input) {
        if (input == null || input.length() < 1) {
            return input;
        }
        String output = input;
        while (output.length() > 0 && output.substring(0, 1).matches(REGEX_REMOVE_FROM_URL)) {
            output = output.substring(1);
        }
        while (output.length() > 0 && output.substring(output.length() - 1, output.length()).matches(REGEX_REMOVE_FROM_URL)) {
            output = output.substring(0, output.length() - 1);
        }
        return output;
    }

    private int getServiceIdByUrl(String url) {
        StreamingService[] serviceList = Newapp.getServices();
        for (int i = 0; i < serviceList.length; i++) {
            if (serviceList[i].getStreamUrlIdHandlerInstance().acceptUrl(this.videoUrl)) {
                return i;
            }
        }
        return -1;
    }
}
