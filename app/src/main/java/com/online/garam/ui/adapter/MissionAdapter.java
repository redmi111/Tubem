package com.online.garam.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import com.google.android.gms.common.util.AndroidUtilsLight;
import com.online.garam.get.DownloadManager;
import com.online.garam.get.DownloadMission;
import com.online.garam.get.DownloadMission.MissionListener;
import com.online.garam.service.DownloadManagerService.DMBinder;
import com.online.garam.ui.common.ProgressDrawable;
import com.online.garam.util.Utility;
import com.online.garam.util.Utility.FileType;
import full.movie.tubem.player.R;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MissionAdapter extends Adapter<MissionAdapter.ViewHolder> {
    /* access modifiers changed from: private|static|final */
    public static final Map<Integer, String> ALGORITHMS = new HashMap();
    private static final String TAG = "MissionAdapter";
    /* access modifiers changed from: private */
    public DMBinder mBinder;
    /* access modifiers changed from: private */
    public Context mContext;
    private LayoutInflater mInflater ;
    private int mLayout;
    /* access modifiers changed from: private */
    public DownloadManager mManager;

    private class ChecksumTask extends AsyncTask<String, Void, String> {
        ProgressDialog prog;

        private ChecksumTask() {
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            super.onPreExecute();
            this.prog = new ProgressDialog(MissionAdapter.this.mContext);
            this.prog.setCancelable(false);
            this.prog.setMessage(MissionAdapter.this.mContext.getString(R.string.msg_wait));
            this.prog.show();
        }

        /* access modifiers changed from: protected|varargs */
        public String doInBackground(String... params) {
            return Utility.checksum(params[0], params[1]);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            this.prog.dismiss();
            Utility.copyToClipboard(MissionAdapter.this.mContext, result);
        }
    }

    static class MissionObserver implements MissionListener {
        private MissionAdapter mAdapter;
        private ViewHolder mHolder;

        public MissionObserver(MissionAdapter adapter, ViewHolder holder) {
            this.mAdapter = adapter;
            this.mHolder = holder;
        }

        public void onProgressUpdate(DownloadMission downloadMission, long done, long total) {
            this.mAdapter.updateProgress(this.mHolder);
        }

        public void onFinish(DownloadMission downloadMission) {
            if (this.mHolder.mission != null) {
                this.mHolder.size.setText(Utility.formatBytes(this.mHolder.mission.length));
                this.mAdapter.updateProgress(this.mHolder, true);
            }
        }

        public void onError(DownloadMission downloadMission, int errCode) {
            this.mAdapter.updateProgress(this.mHolder);
        }
    }

    static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public View bkg;
        public int colorId;
        public ImageView icon;
        public long lastDone = -1;
        public long lastTimeStamp = -1;
        public ImageView menu;
        public DownloadMission mission;
        public TextView name;
        public MissionObserver observer;
        public int position;
        public ProgressDrawable progress;
        public TextView size;
        public TextView status;

        public ViewHolder(View v) {
            super(v);
            this.status = (TextView) Utility.findViewById(v, (int) R.id.item_status);
            this.icon = (ImageView) Utility.findViewById(v, (int) R.id.item_icon);
            this.name = (TextView) Utility.findViewById(v, (int) R.id.item_name);
            this.size = (TextView) Utility.findViewById(v, (int) R.id.item_size);
            this.bkg = (View) Utility.findViewById(v, (int) R.id.item_bkg);
            this.menu = (ImageView) Utility.findViewById(v, (int) R.id.item_more);
        }
    }

    static {
        ALGORITHMS.put(Integer.valueOf(R.id.md5), "MD5");
        ALGORITHMS.put(Integer.valueOf(R.id.sha1), "SHA1");
    }

    public MissionAdapter(Context context, DMBinder binder, DownloadManager manager, boolean isLinear) {
        this.mContext = context;
        this.mManager = manager;
        this.mBinder = binder;
        this.mLayout = isLinear ? R.layout.mission_item_linear : R.layout.mission_item;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mInflater = ((LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        final ViewHolder h = new ViewHolder(this.mInflater.inflate(this.mLayout, parent, false));
        h.menu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MissionAdapter.this.buildPopup(h);
            }
        });
        return h;
    }

    public void onViewRecycled(ViewHolder h) {
        super.onViewRecycled(h);
        h.mission.removeListener(h.observer);
        h.mission = null;
        h.observer = null;
        h.progress = null;
        h.position = -1;
        h.lastTimeStamp = -1;
        h.lastDone = -1;
        h.colorId = 0;
    }

    public void onBindViewHolder(ViewHolder h, int pos) {
        DownloadMission ms = this.mManager.getMission(pos);
        h.mission = ms;
        h.position = pos;
        FileType type = Utility.getFileType(ms.name);
        h.icon.setImageResource(Utility.getIconForFileType(type));
        h.name.setText(ms.name);
        h.size.setText(Utility.formatBytes(ms.length));
        h.progress = new ProgressDrawable(this.mContext, Utility.getBackgroundForFileType(type), Utility.getForegroundForFileType(type));
        h.bkg.setBackgroundDrawable(h.progress);
        h.observer = new MissionObserver(this, h);
        ms.addListener(h.observer);
        updateProgress(h);
    }

    public int getItemCount() {
        return this.mManager.getCount();
    }

    public long getItemId(int position) {
        return (long) position;
    }

    /* access modifiers changed from: private */
    public void updateProgress(ViewHolder h) {
        updateProgress(h, false);
    }

    /* access modifiers changed from: private */
    public void updateProgress(ViewHolder h, boolean finished) {
        if (h.mission != null) {
            long now = System.currentTimeMillis();
            if (h.lastTimeStamp == -1) {
                h.lastTimeStamp = now;
            }
            if (h.lastDone == -1) {
                h.lastDone = h.mission.done;
            }
            long deltaTime = now - h.lastTimeStamp;
            long deltaDone = h.mission.done - h.lastDone;
            if (deltaTime == 0 || deltaTime > 1000 || finished) {
                if (h.mission.errCode > 0) {
                    h.status.setText(R.string.msg_error);
                } else {
                    float progress = ((float) h.mission.done) / ((float) h.mission.length);
                    h.status.setText(String.format(Locale.US, "%.2f%%", new Object[]{Float.valueOf(100.0f * progress)}));
                    h.progress.setProgress(progress);
                }
            }
            if (deltaTime > 1000 && deltaDone > 0) {
                h.size.setText(Utility.formatBytes(h.mission.length) + " " + Utility.formatSpeed(1000.0f * (((float) deltaDone) / ((float) deltaTime))));
                h.lastTimeStamp = now;
                h.lastDone = h.mission.done;
            }
        }
    }

    /* access modifiers changed from: private */
    public void buildPopup(final ViewHolder h) {
        PopupMenu popup = new PopupMenu(this.mContext, h.menu);
        popup.inflate(R.menu.mission);
        Menu menu = popup.getMenu();
        MenuItem start = menu.findItem(R.id.start);
        MenuItem pause = menu.findItem(R.id.pause);
        MenuItem view = menu.findItem(R.id.view);
        MenuItem delete = menu.findItem(R.id.delete);
        MenuItem checksum = menu.findItem(R.id.checksum);
        start.setVisible(false);
        pause.setVisible(false);
        view.setVisible(false);
        delete.setVisible(false);
        checksum.setVisible(false);
        if (h.mission.finished) {
            view.setVisible(true);
            delete.setVisible(true);
            checksum.setVisible(true);
        } else if (!h.mission.running) {
            if (h.mission.errCode == -1) {
                start.setVisible(true);
            }
            delete.setVisible(true);
        } else {
            pause.setVisible(true);
        }
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.start /*2131689532*/:
                        MissionAdapter.this.mManager.resumeMission(h.position);
                        MissionAdapter.this.mBinder.onMissionAdded(MissionAdapter.this.mManager.getMission(h.position));
                        return true;
                    case R.id.pause /*2131689774*/:
                        MissionAdapter.this.mManager.pauseMission(h.position);
                        MissionAdapter.this.mBinder.onMissionRemoved(MissionAdapter.this.mManager.getMission(h.position));
                        h.lastTimeStamp = -1;
                        h.lastDone = -1;
                        return true;
                    case R.id.view /*2131689775*/:
                        File f = new File(h.mission.location, h.mission.name);
                        String ext = Utility.getFileExt(h.mission.name);
                        Log.d(MissionAdapter.TAG, "Viewing file: " + f.getAbsolutePath() + " ext: " + ext);
                        if (ext == null) {
                            Log.w(MissionAdapter.TAG, "Can't view file because it has no extension: " + h.mission.name);
                            return false;
                        }
                        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.substring(1));
                        Log.v(MissionAdapter.TAG, "Mime: " + mime + " package: " + MissionAdapter.this.mContext.getApplicationContext().getPackageName() + ".provider");
                        if (f.exists()) {
                            MissionAdapter.this.viewFileWithFileProvider(f, mime);
                        } else {
                            Log.w(MissionAdapter.TAG, "File doesn't exist");
                        }
                        return true;
                    case R.id.delete /*2131689776*/:
                        MissionAdapter.this.mManager.deleteMission(h.position);
                        MissionAdapter.this.notifyDataSetChanged();
                        return true;
                    case R.id.md5 /*2131689778*/:
                    case R.id.sha1 /*2131689779*/:
                        DownloadMission mission = MissionAdapter.this.mManager.getMission(h.position);
                        new ChecksumTask().execute(new String[]{mission.location + "/" + mission.name, (String) MissionAdapter.ALGORITHMS.get(Integer.valueOf(id))});
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    private void viewFile(File file, String mimetype) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(file), mimetype);
        intent.addFlags(1);
        if (VERSION.SDK_INT >= 21) {
            intent.addFlags(128);
        }
        Log.v(TAG, "Starting intent: " + intent);
        this.mContext.startActivity(intent);
    }

    /* access modifiers changed from: private */
    public void viewFileWithFileProvider(File file, String mimetype) {
        Uri uri = FileProvider.getUriForFile(this.mContext, this.mContext.getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(uri, mimetype);
        intent.addFlags(1);
        if (VERSION.SDK_INT >= 21) {
            intent.addFlags(128);
        }
        Log.v(TAG, "Starting intent: " + intent);
        this.mContext.startActivity(intent);
    }
}
