package full.movie.tubem.player.settings;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.FilePickerActivity;
import full.movie.tubem.player.App;
import full.movie.tubem.player.R;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import java.util.ArrayList;
import java.util.Iterator;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    public static final int REQUEST_INSTALL_ORBOT = 4660;
    String DEFAULT_AUDIO_FORMAT_PREFERENCE;
    String DEFAULT_RESOLUTION_PREFERENCE;
    String DOWNLOAD_PATH_AUDIO_PREFERENCE;
    String DOWNLOAD_PATH_PREFERENCE;
    String SEARCH_LANGUAGE_PREFERENCE;
    String THEME;
    String USE_TOR_KEY;
    private ListPreference defaultAudioFormatPreference;
    /* access modifiers changed from: private */
    public SharedPreferences defaultPreferences;
    private ListPreference defaultResolutionPreference;
    /* access modifiers changed from: private */
    public Preference downloadPathAudioPreference;
    /* access modifiers changed from: private */
    public Preference downloadPathPreference;
    OnSharedPreferenceChangeListener prefListener;
    private ListPreference searchLanguagePreference;
    /* access modifiers changed from: private */
    public Preference themePreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        this.defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.DEFAULT_RESOLUTION_PREFERENCE = getString(R.string.default_resolution_key);
        this.DEFAULT_AUDIO_FORMAT_PREFERENCE = getString(R.string.default_audio_format_key);
        this.SEARCH_LANGUAGE_PREFERENCE = getString(R.string.search_language_key);
        this.DOWNLOAD_PATH_PREFERENCE = getString(R.string.download_path_key);
        this.DOWNLOAD_PATH_AUDIO_PREFERENCE = getString(R.string.download_path_audio_key);
        this.THEME = getString(R.string.theme_key);
        this.USE_TOR_KEY = getString(R.string.use_tor_key);
        this.defaultResolutionPreference = (ListPreference) findPreference(this.DEFAULT_RESOLUTION_PREFERENCE);
        this.defaultAudioFormatPreference = (ListPreference) findPreference(this.DEFAULT_AUDIO_FORMAT_PREFERENCE);
        this.searchLanguagePreference = (ListPreference) findPreference(this.SEARCH_LANGUAGE_PREFERENCE);
        this.downloadPathPreference = findPreference(this.DOWNLOAD_PATH_PREFERENCE);
        this.downloadPathAudioPreference = findPreference(this.DOWNLOAD_PATH_AUDIO_PREFERENCE);
        this.themePreference = findPreference(this.THEME);
        this.prefListener = new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Activity a = SettingsFragment.this.getActivity();
                if (a != null) {
                    if (key == SettingsFragment.this.USE_TOR_KEY) {
                        if (!SettingsFragment.this.defaultPreferences.getBoolean(SettingsFragment.this.USE_TOR_KEY, false)) {
                            App.configureTor(false);
                        } else if (OrbotHelper.isOrbotInstalled(a)) {
                            App.configureTor(true);
                            OrbotHelper.requestStartTor(a);
                        } else {
                            a.startActivityForResult(OrbotHelper.getOrbotInstallIntent(a), SettingsFragment.REQUEST_INSTALL_ORBOT);
                        }
                    } else if (key == SettingsFragment.this.DOWNLOAD_PATH_PREFERENCE) {
                        SettingsFragment.this.downloadPathPreference.setSummary(sharedPreferences.getString(SettingsFragment.this.DOWNLOAD_PATH_PREFERENCE, SettingsFragment.this.getString(R.string.download_path_summary)));
                    } else if (key == SettingsFragment.this.DOWNLOAD_PATH_AUDIO_PREFERENCE) {
                        SettingsFragment.this.downloadPathAudioPreference.setSummary(sharedPreferences.getString(SettingsFragment.this.DOWNLOAD_PATH_AUDIO_PREFERENCE, SettingsFragment.this.getString(R.string.download_path_audio_summary)));
                    } else if (key == SettingsFragment.this.THEME) {
                        SettingsFragment.this.themePreference.setSummary(sharedPreferences.getString(SettingsFragment.this.THEME, "Light"));
                    }
                    SettingsFragment.this.updateSummary();
                }
            }
        };
        this.defaultPreferences.registerOnSharedPreferenceChangeListener(this.prefListener);
        updateSummary();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(this.downloadPathPreference.getKey()) || preference.getKey().equals(this.downloadPathAudioPreference.getKey())) {
            Activity activity = getActivity();
            Intent i = new Intent(activity, FilePickerActivity.class);
            i.putExtra(AbstractFilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(AbstractFilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(AbstractFilePickerActivity.EXTRA_MODE, 1);
            if (preference.getKey().equals(this.downloadPathPreference.getKey())) {
                activity.startActivityForResult(i, R.string.download_path_key);
            } else if (preference.getKey().equals(this.downloadPathAudioPreference.getKey())) {
                activity.startActivityForResult(i, R.string.download_path_audio_key);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean z = false;
        super.onActivityResult(requestCode, resultCode, data);
        Activity a = getActivity();
        if ((requestCode == R.string.download_path_audio_key || requestCode == R.string.download_path_key) && resultCode == -1) {
            if (!data.getBooleanExtra(AbstractFilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                Uri uri = data.getData();
            } else if (VERSION.SDK_INT >= 16) {
                ClipData clip = data.getClipData();
                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        Uri uri2 = clip.getItemAt(i).getUri();
                    }
                }
            } else {
                ArrayList<String> paths = data.getStringArrayListExtra(AbstractFilePickerActivity.EXTRA_PATHS);
                if (paths != null) {
                    Iterator it = paths.iterator();
                    while (it.hasNext()) {
                        Uri uri3 = Uri.parse((String) it.next());
                    }
                }
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(a);
            prefs.edit().putString(getString(requestCode), data.getData().toString().substring(7)).apply();
        } else if (requestCode == 4660) {
            if (requestCode == 4660 && OrbotHelper.requestStartTor(a)) {
                z = true;
            }
            App.configureTor(z);
        }
        updateSummary();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* access modifiers changed from: private */
    public void updateSummary() {
        this.defaultResolutionPreference.setSummary(this.defaultPreferences.getString(this.DEFAULT_RESOLUTION_PREFERENCE, getString(R.string.default_resolution_value)));
        this.defaultAudioFormatPreference.setSummary(this.defaultPreferences.getString(this.DEFAULT_AUDIO_FORMAT_PREFERENCE, getString(R.string.default_audio_format_value)));
        this.searchLanguagePreference.setSummary(this.defaultPreferences.getString(this.SEARCH_LANGUAGE_PREFERENCE, getString(R.string.default_language_value)));
        this.downloadPathPreference.setSummary(this.defaultPreferences.getString(this.DOWNLOAD_PATH_PREFERENCE, getString(R.string.download_path_summary)));
        this.downloadPathAudioPreference.setSummary(this.defaultPreferences.getString(this.DOWNLOAD_PATH_AUDIO_PREFERENCE, getString(R.string.download_path_audio_summary)));
        this.themePreference.setSummary(this.defaultPreferences.getString(this.THEME, getString(R.string.light_theme_title)));
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }
}
