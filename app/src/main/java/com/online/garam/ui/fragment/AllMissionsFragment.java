package com.online.garam.ui.fragment;

import com.online.garam.get.DownloadManager;
import com.online.garam.service.DownloadManagerService.DMBinder;

public class AllMissionsFragment extends MissionsFragment {
    /* access modifiers changed from: protected */
    public DownloadManager setupDownloadManager(DMBinder binder) {
        return binder.getDownloadManager();
    }
}
