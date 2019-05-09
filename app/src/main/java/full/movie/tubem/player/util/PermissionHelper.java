package full.movie.tubem.player.util;

import android.app.Activity;
import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {
    public static final int PERMISSION_READ_STORAGE = 777;
    public static final int PERMISSION_WRITE_STORAGE = 778;

    public static boolean checkStoragePermissions(Activity activity) {
        if (VERSION.SDK_INT < 16 || checkReadStoragePermissions(activity)) {
            return checkWriteStoragePermissions(activity);
        }
        return false;
    }

    @RequiresApi(api = 16)
    public static boolean checkReadStoragePermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, "android.permission.READ_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        ActivityCompat.requestPermissions(activity, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_READ_STORAGE);
        return false;
    }

    public static boolean checkWriteStoragePermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        ActivityCompat.requestPermissions(activity, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSION_WRITE_STORAGE);
        return false;
    }
}
