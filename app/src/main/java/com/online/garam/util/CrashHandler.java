package com.online.garam.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import java.io.File;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import static android.os.Build.VERSION.RELEASE;

public class CrashHandler implements UncaughtExceptionHandler {
    private static String ANDROID = RELEASE;
    public static final String CRASH_DIR = (Environment.getExternalStorageDirectory().getPath() + "/GigaCrash/");
    public static final String CRASH_LOG = (CRASH_DIR + "last_crash.log");
    public static final String CRASH_TAG = (CRASH_DIR + ".crashed");
    private static String MANUFACTURER = Build.MANUFACTURER;
    private static String MODEL = Build.MODEL;
    public static String VERSION = "Unknown";
    private UncaughtExceptionHandler mPrevious = Thread.currentThread().getUncaughtExceptionHandler();

    public static void init(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            VERSION = info.versionName + info.versionCode;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register() {
        new CrashHandler();
    }

    private CrashHandler() {
        Thread.currentThread().setUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable throwable) {
        File f = new File(CRASH_LOG);
        if (f.exists()) {
            f.delete();
        } else {
            try {
                new File(CRASH_DIR).mkdirs();
                f.createNewFile();
            } catch (Exception e) {
                return;
            }
        }
        try {
            PrintWriter p = new PrintWriter(f);
            p.write("Android Version: " + ANDROID + "\n");
            p.write("Device Model: " + MODEL + "\n");
            p.write("Device Manufacturer: " + MANUFACTURER + "\n");
            p.write("App Version: " + VERSION + "\n");
            p.write("*********************\n");
            throwable.printStackTrace(p);
            p.close();
            try {
                new File(CRASH_TAG).createNewFile();
                if (this.mPrevious != null) {
                    this.mPrevious.uncaughtException(thread, throwable);
                }
            } catch (Exception e2) {
            }
        } catch (Exception e3) {
        }
    }
}
