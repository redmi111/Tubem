package com.online.garam.ui.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.online.garam.util.Utility;
import full.movie.tubem.player.R;

public abstract class ToolbarActivity extends AppCompatActivity {
    protected Toolbar mToolbar;

    public abstract int getLayoutResource();

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        this.mToolbar = (Toolbar) Utility.findViewById((Activity) this, (int) R.id.toolbar);
        setSupportActionBar(this.mToolbar);
    }
}
