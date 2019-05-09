package full.movie.tubem.player;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class ThemableActivity extends AppCompatActivity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("theme", getResources().getString(R.string.light_theme_title)).equals(getResources().getString(R.string.dark_theme_title))) {
            setTheme(R.style.DarkTheme);
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("theme", getResources().getString(R.string.light_theme_title)).equals(getResources().getString(R.string.dark_theme_title))) {
            setTheme(R.style.DarkTheme);
        }
    }
}
