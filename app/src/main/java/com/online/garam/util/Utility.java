package com.online.garam.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Toast;
import com.google.android.exoplayer.util.MimeTypes;
import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.online.garam.get.DownloadMission;
import full.movie.tubem.player.R;
import full.movie.tubem.player.settings.NewSettings;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {

    public enum FileType {
        VIDEO,
        MUSIC,
        UNKNOWN
    }

    public static String formatBytes(long bytes) {
        if (bytes < PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
            return String.format("%d B", new Object[]{Long.valueOf(bytes)});
        } else if (bytes < PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED) {
            return String.format("%.2f kB", new Object[]{Float.valueOf(((float) bytes) / 1024.0f)});
        } else if (bytes < 1073741824) {
            return String.format("%.2f MB", new Object[]{Float.valueOf((((float) bytes) / 1024.0f) / 1024.0f)});
        } else {
            return String.format("%.2f GB", new Object[]{Float.valueOf(((((float) bytes) / 1024.0f) / 1024.0f) / 1024.0f)});
        }
    }

    public static String formatSpeed(float speed) {
        if (speed < 1024.0f) {
            return String.format("%.2f B/s", new Object[]{Float.valueOf(speed)});
        } else if (speed < 1048576.0f) {
            return String.format("%.2f kB/s", new Object[]{Float.valueOf(speed / 1024.0f)});
        } else if (speed < 1.07374182E9f) {
            return String.format("%.2f MB/s", new Object[]{Float.valueOf((speed / 1024.0f) / 1024.0f)});
        } else {
            return String.format("%.2f GB/s", new Object[]{Float.valueOf(((speed / 1024.0f) / 1024.0f) / 1024.0f)});
        }
    }

    public static void writeToFile(String fileName, String content) {
        try {
            writeToFile(fileName, content.getBytes("UTF-8"));
        } catch (Exception e) {
        }
    }

    public static void writeToFile(String fileName, byte[] content) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
            }
        }
        try {
            FileOutputStream opt = new FileOutputStream(f, false);
            opt.write(content, 0, content.length);
            opt.close();
        } catch (Exception e2) {
        }
    }

    public static String readFromFile(String file) {
        try {
            File f = new File(file);
            if (!f.exists() || !f.canRead()) {
                return null;
            }
            BufferedInputStream ipt = new BufferedInputStream(new FileInputStream(f));
            byte[] buf = new byte[512];
            StringBuilder sb = new StringBuilder();
            while (ipt.available() > 0) {
                sb.append(new String(buf, 0, ipt.read(buf, 0, 512), "UTF-8"));
            }
            ipt.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T findViewById(View v, int id) {
        //Fixme: Cast to T
        return (T) v.findViewById(id);
    }

    public static <T> T findViewById(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    public static String getFileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        }
        String ext = url.substring(url.lastIndexOf("."));
        if (ext.indexOf("%") > -1) {
            ext = ext.substring(0, ext.indexOf("%"));
        }
        if (ext.indexOf("/") > -1) {
            ext = ext.substring(0, ext.indexOf("/"));
        }
        return ext.toLowerCase();
    }

    public static FileType getFileType(String file) {
        if (file.endsWith(".mp3") || file.endsWith(".wav") || file.endsWith(".flac") || file.endsWith(".m4a")) {
            return FileType.MUSIC;
        }
        if (file.endsWith(".mp4") || file.endsWith(".mpeg") || file.endsWith(".rm") || file.endsWith(".rmvb") || file.endsWith(".flv") || file.endsWith(".webp") || file.endsWith(".webm")) {
            return FileType.VIDEO;
        }
        return FileType.UNKNOWN;
    }

    public static Boolean isMusicFile(String file) {
        return Boolean.valueOf(getFileType(file) == FileType.MUSIC);
    }

    public static Boolean isVideoFile(String file) {
        return Boolean.valueOf(getFileType(file) == FileType.VIDEO);
    }

    public static int getBackgroundForFileType(FileType type) {
        switch (type) {
            case MUSIC:
                return R.color.audio_left_to_load_color;
            case VIDEO:
                return R.color.video_left_to_load_color;
            default:
                return R.color.gray;
        }
    }

    public static int getForegroundForFileType(FileType type) {
        switch (type) {
            case MUSIC:
                return R.color.audio_already_load_color;
            case VIDEO:
                return R.color.video_already_load_color;
            default:
                return R.color.gray;
        }
    }

    public static int getIconForFileType(FileType type) {
        switch (type) {
            case MUSIC:
                return R.drawable.music;
            default:
                return R.drawable.video;
        }
    }

    public static boolean isDirectoryAvailble(String path) {
        File dir = new File(path);
        return dir.exists() && dir.isDirectory();
    }

    public static boolean isDownloadDirectoryAvailble(Context context) {
        return isDirectoryAvailble(NewSettings.getVideoDownloadPath(context));
    }

    public static void showDirectoryChooser(Activity activity) {
        Intent i = new Intent(activity, FilePickerActivity.class);
        i.setAction("android.intent.action.GET_CONTENT");
        i.putExtra(AbstractFilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(AbstractFilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(AbstractFilePickerActivity.EXTRA_MODE, 1);
        activity.startActivityForResult(i, DownloadMission.ERROR_UNKNOWN);
    }

    public static void copyToClipboard(Context context, String str) {
        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(MimeTypes.BASE_TYPE_TEXT, str));
        Toast.makeText(context, R.string.msg_copied, 0).show();
    }

    public static String checksum(String path, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            try {
                FileInputStream i = new FileInputStream(path);
                byte[] buf = new byte[1024];
                while (true) {
                    try {
                        int len = i.read(buf);
                        if (len == -1) {
                            break;
                        }
                        md.update(buf, 0, len);
                    } catch (IOException e) {
                    }
                }
                byte[] digest = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(Integer.toString((b & -1) + 0, 16).substring(1));
                }
                return sb.toString();
            } catch (FileNotFoundException e2) {
                throw new RuntimeException(e2);
            }
        } catch (NoSuchAlgorithmException e3) {
            throw new RuntimeException(e3);
        }
    }
}
