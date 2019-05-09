package full.movie.tubem.player.download;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import full.movie.tubem.player.R;
import full.movie.tubem.player.settings.SettingsFragment;
import info.guardianproject.netcipher.NetCipher;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;

public class FileDownloader extends AsyncTask<Void, Integer, Void> {
    public static final String TAG = "FileDownloader";
    private Builder builder;
    private final Context context;
    private final String debugContext;
    private int fileSize = -1;
    private final String fileURL;
    private NotificationManager nm;
    private int notifyId = SettingsFragment.REQUEST_INSTALL_ORBOT;
    private final File saveFilePath;
    private final String title;

    public FileDownloader(Context context2, String fileURL2, File saveFilePath2, String title2) {
        this.context = context2;
        this.fileURL = fileURL2;
        this.saveFilePath = saveFilePath2;
        this.title = title2;
        this.debugContext = "'" + fileURL2 + "' => '" + saveFilePath2 + "'";
    }

    public static void downloadFile(Context context2, String fileURL2, File saveFilePath2, String title2) {
        new FileDownloader(context2, fileURL2, saveFilePath2, title2).execute(new Void[0]);
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        super.onPreExecute();
        this.nm = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.builder = new Builder(this.context).setSmallIcon(17301633).setLargeIcon(((BitmapDrawable) this.context.getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap()).setContentTitle(this.saveFilePath.getName()).setContentText(this.saveFilePath.getAbsolutePath()).setProgress(this.fileSize, 0, false);
        this.nm.notify(this.notifyId, this.builder.build());
    }


    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Void doInBackground(Void... voids) {
        HttpsURLConnection con = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            con = NetCipher.getHttpsURLConnection(this.fileURL);
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                this.fileSize = con.getContentLength();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(con.getInputStream());
                try {
                    FileOutputStream outputStream2 = new FileOutputStream(this.saveFilePath);
                    int downloaded = 0;
                    try {
                        byte[] buffer = new byte[8192];
                        while (true) {
                            int bytesRead = bufferedInputStream.read(buffer);
                            if (bytesRead == -1) {
                                break;
                            }
                            outputStream2.write(buffer, 0, bytesRead);
                            downloaded += bytesRead;
                            if (downloaded % 50000 < 8192) {
                                publishProgress(new Integer[]{Integer.valueOf(downloaded)});
                            }
                        }
                        publishProgress(new Integer[]{Integer.valueOf(8192)});
                        outputStream = outputStream2;
                        inputStream = bufferedInputStream;
                    } catch (IOException e) {
                        e = e;
                        FileOutputStream outputStream3 = outputStream2;
                        inputStream = bufferedInputStream;
                        try {
                            Log.e(TAG, "No file to download. Server replied HTTP code: ", e);
                            e.printStackTrace();
                            if (outputStream != null) {
                            }
                            if (inputStream != null) {
                            }
                            if (con != null) {
                            }
                            return null;
                        } catch (Throwable th) {
                            th = th;
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                    if (con != null) {
                                    }
                                    throw th;
                                }
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (con != null) {
                                con.disconnect();
                            }
                            throw th;
                        }
                    } catch (Throwable th2) {
                        //th = th2;
                        outputStream = outputStream2;
                        inputStream = bufferedInputStream;
                        if (outputStream != null) {
                        }
                        if (inputStream != null) {
                        }
                        if (con != null) {
                        }
                        throw th2;
                    }
                } catch (IOException e3) {
                  //  e = e3;
                    inputStream = bufferedInputStream;
                    Log.e(TAG, "No file to download. Server replied HTTP code: ", e3);
                    e3.printStackTrace();
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                            if (con != null) {
                                con.disconnect();
                            }
                            return null;
                        }
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (con != null) {
                    }
                    return null;
                } catch (Throwable th3) {
                    //th = th3;
                    inputStream = bufferedInputStream;
                    if (outputStream != null) {
                    }
                    if (inputStream != null) {
                    }
                    if (con != null) {
                    }
                    try {
                        throw th3;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            } else {
                Log.i(TAG, "No file to download. Server replied HTTP code: " + responseCode);
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (con != null) {
                con.disconnect();
            }
        } catch (IOException e6) {
          //  e = e6;
        }
        return null;
    }

    /* access modifiers changed from: protected|varargs */
    public void onProgressUpdate(Integer... progress) {
        this.builder.setProgress(this.fileSize, progress[0].intValue(), false);
        this.nm.notify(this.notifyId, this.builder.build());
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.nm.cancel(this.notifyId);
    }
}
