package full.movie.tubem.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;

public class PanicResponderActivity extends Activity {
    public static final String PANIC_TRIGGER_ACTION = "info.guardianproject.panic.action.TRIGGER";

    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && PANIC_TRIGGER_ACTION.equals(intent.getAction())) {
            ExitActivity.exitAndRemoveFromRecentApps(this);
        }
        if (VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }
}
