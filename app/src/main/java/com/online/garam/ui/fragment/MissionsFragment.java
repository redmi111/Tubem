package com.online.garam.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.online.garam.get.DownloadManager;
import com.online.garam.service.DownloadManagerService;
import com.online.garam.service.DownloadManagerService.DMBinder;
import com.online.garam.ui.adapter.MissionAdapter;
import com.online.garam.util.Utility;
import full.movie.tubem.player.R;

public abstract class MissionsFragment extends Fragment {
    private Context mActivity;
    private MissionAdapter mAdapter;
    /* access modifiers changed from: private */
    public DMBinder mBinder;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            MissionsFragment.this.mBinder = (DMBinder) binder;
            MissionsFragment.this.mManager = MissionsFragment.this.setupDownloadManager(MissionsFragment.this.mBinder);
            MissionsFragment.this.updateList();
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private GridLayoutManager mGridManager;
    private boolean mLinear;
    private LinearLayoutManager mLinearManager;
    private RecyclerView mList;
    /* access modifiers changed from: private */
    public DownloadManager mManager;
    private SharedPreferences mPrefs;
    private MenuItem mSwitch;

    public abstract DownloadManager setupDownloadManager(DMBinder dMBinder);

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.missions, container, false);
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.mLinear = this.mPrefs.getBoolean("linear", false);
        Intent i = new Intent();
        i.setClass(getActivity(), DownloadManagerService.class);
        getActivity().bindService(i, this.mConnection, 1);
        this.mList = (RecyclerView) Utility.findViewById(v, (int) R.id.mission_recycler);
        this.mGridManager = new GridLayoutManager(getActivity(), 2);
        this.mLinearManager = new LinearLayoutManager(getActivity());
        this.mList.setLayoutManager(this.mGridManager);
        setHasOptionsMenu(true);
        return v;
    }

    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unbindService(this.mConnection);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_mode /*2131689772*/:
                this.mLinear = !this.mLinear;
                updateList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void notifyChange() {
        this.mAdapter.notifyDataSetChanged();
    }

    /* access modifiers changed from: private */
    public void updateList() {
        this.mAdapter = new MissionAdapter(this.mActivity, this.mBinder, this.mManager, this.mLinear);
        if (this.mLinear) {
            this.mList.setLayoutManager(this.mLinearManager);
        } else {
            this.mList.setLayoutManager(this.mGridManager);
        }
        this.mList.setAdapter(this.mAdapter);
        if (this.mSwitch != null) {
            this.mSwitch.setIcon(this.mLinear ? R.drawable.grid : R.drawable.list);
        }
        this.mPrefs.edit().putBoolean("linear", this.mLinear).commit();
    }
}
