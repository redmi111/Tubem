package full.movie.tubem.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;

public class ExitActivity extends Activity {
    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
        System.exit(0);
    }

    public static void exitAndRemoveFromRecentApps(Activity activity) {
        Intent intent = new Intent(activity, ExitActivity.class);
        intent.addFlags(276922368);
        activity.startActivity(intent);
    }
}
