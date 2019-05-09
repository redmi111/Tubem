package full.movie.tubem.player.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import full.movie.tubem.player.R;
import java.io.File;

public class NewSettings {
    private NewSettings() {
    }

    public static void initSettings(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.settings, false);
        getVideoDownloadFolder(context);
        getAudioDownloadFolder(context);
    }

    public static File getVideoDownloadFolder(Context context) {
        return getFolder(context, R.string.download_path_key, Environment.DIRECTORY_MOVIES);
    }

    public static String getVideoDownloadPath(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.download_path_key), Environment.DIRECTORY_MOVIES);
    }

    public static File getAudioDownloadFolder(Context context) {
        return getFolder(context, R.string.download_path_audio_key, Environment.DIRECTORY_MUSIC);
    }

    public static String getAudioDownloadPath(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.download_path_audio_key), Environment.DIRECTORY_MUSIC);
    }

    private static File getFolder(Context context, int keyID, String defaultDirectoryName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(keyID);
        String downloadPath = prefs.getString(key, null);
        if (downloadPath != null && !downloadPath.isEmpty()) {
            return new File(downloadPath.trim());
        }
        File folder = getFolder(defaultDirectoryName);
        Editor spEditor = prefs.edit();
        spEditor.putString(key, new File(folder, "tubem").getAbsolutePath());
        spEditor.apply();
        return folder;
    }

    @NonNull
    private static File getFolder(String defaultDirectoryName) {
        return new File(Environment.getExternalStorageDirectory(), defaultDirectoryName);
    }
}
