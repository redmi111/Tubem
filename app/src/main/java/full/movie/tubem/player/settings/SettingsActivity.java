package full.movie.tubem.player.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import full.movie.tubem.player.R;

public class SettingsActivity extends PreferenceActivity {
    SettingsFragment f = new SettingsFragment();
    private AppCompatDelegate mDelegate = null;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceBundle) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("theme", getResources().getString(R.string.light_theme_title)).equals(getResources().getString(R.string.dark_theme_title))) {
            setTheme(R.style.DarkTheme);
        }
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceBundle);
        super.onCreate(savedInstanceBundle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle((int) R.string.settings_title);
        actionBar.setDisplayShowTitleEnabled(true);
        getFragmentManager().beginTransaction().replace(16908290, this.f).commit();
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.f.onActivityResult(requestCode, resultCode, data);
    }

    /* access modifiers changed from: protected */
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    private ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    @NonNull
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    public void setContentView(View view, LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    public void addContentView(View view, LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    /* access modifiers changed from: protected */
    public void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    /* access modifiers changed from: protected */
    public void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getDelegate() {
        if (this.mDelegate == null) {
            this.mDelegate = AppCompatDelegate.create((Activity) this, null);
        }
        return this.mDelegate;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return true;
    }

    public static void initSettings(Context context) {
        NewSettings.initSettings(context);
    }
}
