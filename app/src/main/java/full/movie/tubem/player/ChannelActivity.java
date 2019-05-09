package full.movie.tubem.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import full.movie.tubem.player.detail.VideoItemDetailFragment;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.StreamingService;
import full.movie.tubem.player.extractor.channel.ChannelInfo;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.info_list.InfoItemBuilder.OnInfoItemSelectedListener;
import full.movie.tubem.player.info_list.InfoListAdapter;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import full.movie.tubem.player.util.NavStack;
import java.io.IOException;

public class ChannelActivity extends AppCompatActivity {
    /* access modifiers changed from: private|static|final */
    public static final String TAG = ChannelActivity.class.toString();
    /* access modifiers changed from: private */
    public String channelUrl = "";
    /* access modifiers changed from: private */
    public boolean hasNextPage = true;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private InfoListAdapter infoListAdapter = null;
    /* access modifiers changed from: private */
    public boolean isLoading = false;
    /* access modifiers changed from: private */
    public int pageNumber = 0;
    private View rootView = null;
    /* access modifiers changed from: private */
    public int serviceId = -1;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("theme", getResources().getString(R.string.light_theme_title)).equals(getResources().getString(R.string.dark_theme_title))) {
            setTheme(R.style.DarkTheme_NoActionBar);
        }
        setTranslucentStatusBar(getWindow());
        setContentView((int) R.layout.activity_channel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.rootView = findViewById(R.id.rootView);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            Intent i = getIntent();
            this.channelUrl = i.getStringExtra("url");
            this.serviceId = i.getIntExtra("service_id", -1);
        } else {
            this.channelUrl = savedInstanceState.getString("url");
            this.serviceId = savedInstanceState.getInt("service_id");
            NavStack.getInstance().restoreSavedInstanceState(savedInstanceState);
        }
        this.infoListAdapter = new InfoListAdapter(this, this.rootView);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.channel_streams_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(this.infoListAdapter);
        this.infoListAdapter.setOnStreamInfoItemSelectedListener(new OnInfoItemSelectedListener() {
            public void selected(String url, int serviceId) {
                NavStack.getInstance().openDetailActivity(ChannelActivity.this, url, serviceId);
            }
        });
        recyclerView.setOnScrollListener(new OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    if (layoutManager.getChildCount() + layoutManager.findFirstVisibleItemPosition() >= layoutManager.getItemCount() && !ChannelActivity.this.isLoading && ChannelActivity.this.hasNextPage) {
                        ChannelActivity.this.pageNumber = ChannelActivity.this.pageNumber + 1;
                        ChannelActivity.this.requestData(true);
                    }
                }
            }
        });
        requestData(false);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", this.channelUrl);
        outState.putInt("service_id", this.serviceId);
        NavStack.getInstance().onSaveInstanceState(outState);
    }

    /* access modifiers changed from: private */
    public void updateUi(final ChannelInfo info) {
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) findViewById(R.id.channel_toolbar_layout);
        ImageView channelBanner = (ImageView) findViewById(R.id.channel_banner_image);
        FloatingActionButton feedButton = (FloatingActionButton) findViewById(R.id.channel_rss_fab);
        ImageView avatarView = (ImageView) findViewById(R.id.channel_avatar_view);
        ImageView haloView = (ImageView) findViewById(R.id.channel_avatar_halo);
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(8);
        if (info.channel_name != null && !info.channel_name.isEmpty()) {
            ctl.setTitle(info.channel_name);
        }
        if (info.banner_url != null && !info.banner_url.isEmpty()) {
            this.imageLoader.displayImage(info.banner_url, channelBanner, (ImageLoadingListener) new ImageErrorLoadingListener(this, this.rootView, info.service_id));
        }
        if (info.avatar_url != null && !info.avatar_url.isEmpty()) {
            avatarView.setVisibility(0);
            haloView.setVisibility(0);
            this.imageLoader.displayImage(info.avatar_url, avatarView, (ImageLoadingListener) new ImageErrorLoadingListener(this, this.rootView, info.service_id));
        }
        if (info.feed_url == null || info.feed_url.isEmpty()) {
            feedButton.setVisibility(8);
        } else {
            feedButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Log.d(ChannelActivity.TAG, info.feed_url);
                    ChannelActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(info.feed_url)));
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void addVideos(ChannelInfo info) {
        this.infoListAdapter.addInfoItemList(info.related_streams);
    }

    /* access modifiers changed from: private */
    public void postNewErrorToast(Handler h, final int stringResource) {
        h.post(new Runnable() {
            public void run() {
                Toast.makeText(ChannelActivity.this, stringResource, 1).show();
            }
        });
    }

    /* access modifiers changed from: private */
    public void requestData(final boolean onlyVideos) {
        this.isLoading = true;
        new Thread(new Runnable() {
            Handler h = new Handler();

            public void run() {
                //Fixme: Initialized service, service2 as null.
                StreamingService service = null;
                StreamingService service2 = null;
                StreamingService service3 = null;
                try {
                    service3 = Newapp.getService(ChannelActivity.this.serviceId);
                    ChannelInfo info = ChannelInfo.getInfo(service3.getChannelExtractorInstance(ChannelActivity.this.channelUrl, ChannelActivity.this.pageNumber));
                    final ChannelInfo channelInfo = info;
                    this.h.post(new Runnable() {
                        public void run() {
                            ChannelActivity.this.isLoading = false;
                            if (!onlyVideos) {
                                ChannelActivity.this.updateUi(channelInfo);
                            }
                            ChannelActivity.this.hasNextPage = channelInfo.hasNextPage;
                            ChannelActivity.this.addVideos(channelInfo);
                        }
                    });
                    if (info != null && !info.errors.isEmpty()) {
                        Log.e(ChannelActivity.TAG, "OCCURRED ERRORS DURING EXTRACTION:");
                        for (Throwable e : info.errors) {
                            e.printStackTrace();
                            Log.e(ChannelActivity.TAG, "------");
                        }
                        ErrorActivity.reportError(this.h, (Context) ChannelActivity.this, info.errors, null, ChannelActivity.this.findViewById(16908290), ErrorInfo.make(7, service3.getServiceInfo().name, ChannelActivity.this.channelUrl, 0));
                    }
                } catch (IOException ioe) {
                    ChannelActivity.this.postNewErrorToast(this.h, R.string.network_error);
                    ioe.printStackTrace();
                } catch (ParsingException pe) {
                    ErrorActivity.reportError(this.h, (Context) ChannelActivity.this, (Throwable) pe, VideoItemDetailFragment.class, null, ErrorInfo.make(7, service2.getServiceInfo().name, ChannelActivity.this.channelUrl, R.string.parsing_error));
                    this.h.post(new Runnable() {
                        public void run() {
                            ChannelActivity.this.finish();
                        }
                    });
                    pe.printStackTrace();
                } catch (ExtractionException ex) {
                    String name = "none";
                    if (service != null) {
                        name = service.getServiceInfo().name;
                    }
                    ErrorActivity.reportError(this.h, (Context) ChannelActivity.this, (Throwable) ex, VideoItemDetailFragment.class, null, ErrorInfo.make(7, name, ChannelActivity.this.channelUrl, R.string.parsing_error));
                    this.h.post(new Runnable() {
                        public void run() {
                            ChannelActivity.this.finish();
                        }
                    });
                    ex.printStackTrace();
                } catch (Exception e2) {
                    ErrorActivity.reportError(this.h, (Context) ChannelActivity.this, (Throwable) e2, VideoItemDetailFragment.class, null, ErrorInfo.make(7, service3.getServiceInfo().name, ChannelActivity.this.channelUrl, R.string.general_error));
                    this.h.post(new Runnable() {
                        public void run() {
                            ChannelActivity.this.finish();
                        }
                    });
                    e2.printStackTrace();
                }
            }
        }).start();
    }

    public static void setTranslucentStatusBar(Window window) {
        if (window != null) {
            int sdkInt = VERSION.SDK_INT;
            if (sdkInt >= 21) {
                setTranslucentStatusBarLollipop(window);
            } else if (sdkInt >= 19) {
                setTranslucentStatusBarKiKat(window);
            }
        }
    }

    @TargetApi(21)
    private static void setTranslucentStatusBarLollipop(Window window) {
        window.setStatusBarColor(ContextCompat.getColor(window.getContext(), 17170445));
    }

    @TargetApi(19)
    private static void setTranslucentStatusBarKiKat(Window window) {
        window.addFlags(67108864);
    }

    public void onBackPressed() {
        try {
            NavStack.getInstance().navBack(this);
        } catch (Exception e) {
            ErrorActivity.reportUiError(this, e);
        }
    }
}
