package full.movie.tubem.player.detail;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import full.movie.tubem.player.ActivityCommunicator;
import full.movie.tubem.player.ImageErrorLoadingListener;
import full.movie.tubem.player.Localization;
import full.movie.tubem.player.R;
import full.movie.tubem.player.ReCaptchaActivity;
import full.movie.tubem.player.detail.ActionBarHandler.OnActionListener;
import full.movie.tubem.player.detail.StreamInfoWorker.OnStreamInfoReceivedListener;
import full.movie.tubem.player.download.DownloadDialog;
import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.extractor.MediaFormat;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.stream_info.AudioStream;
import full.movie.tubem.player.extractor.stream_info.StreamInfo;
import full.movie.tubem.player.extractor.stream_info.VideoStream;
import full.movie.tubem.player.info_list.InfoItemBuilder;
import full.movie.tubem.player.info_list.InfoItemBuilder.OnInfoItemSelectedListener;
import full.movie.tubem.player.player.BackgroundPlayer;
import full.movie.tubem.player.player.ExoPlayerActivity;
import full.movie.tubem.player.player.PlayVideoActivity;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import full.movie.tubem.player.util.NavStack;
import full.movie.tubem.player.util.PermissionHelper;
import info.guardianproject.netcipher.client.StrongHttpsClient;
import java.util.Iterator;
import java.util.Vector;

public class VideoItemDetailFragment extends Fragment {
    public static final String AUTO_PLAY = "auto_play";
    private static final String KORE_PACKET = "org.xbmc.kore";
    /* access modifiers changed from: private|static|final */
    public static final String TAG = VideoItemDetailFragment.class.toString();
    private ActionBarHandler actionBarHandler;
    /* access modifiers changed from: private */
    public AppCompatActivity activity;
    private boolean autoPlayEnabled;
    private DisplayImageOptions displayImageOptions = new Builder().cacheInMemory(true).build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private InfoItemBuilder infoItemBuilder = null;
    /* access modifiers changed from: private|final */
    public final Point initialThumbnailPos = new Point(0, 0);
    private OnInvokeCreateOptionsMenuListener onInvokeCreateOptionsMenuListener;
    private FloatingActionButton playVideoButton;
    private ProgressBar progressBar;
    /* access modifiers changed from: private */
    public View rootView = null;
    private boolean showNextStreamItem;
    /* access modifiers changed from: private */
    public Bitmap streamThumbnail = null;
    /* access modifiers changed from: private */
    public int streamingServiceId = -1;
    /* access modifiers changed from: private */
    public View thumbnailWindowLayout;
    private InterstitialAd mInterstitialAd;

    public interface OnInvokeCreateOptionsMenuListener {
        void createOptionsMenu();
    }

    /* access modifiers changed from: private */
    public void updateInfo(StreamInfo info) {
        Activity a = getActivity();
        RelativeLayout textContentLayout = (RelativeLayout) this.activity.findViewById(R.id.detail_text_content_layout);
        TextView videoTitleView = (TextView) this.activity.findViewById(R.id.detail_video_title_view);
        TextView uploaderView = (TextView) this.activity.findViewById(R.id.detail_uploader_view);
        TextView viewCountView = (TextView) this.activity.findViewById(R.id.detail_view_count_view);
        TextView thumbsUpView = (TextView) this.activity.findViewById(R.id.detail_thumbs_up_count_view);
        TextView thumbsDownView = (TextView) this.activity.findViewById(R.id.detail_thumbs_down_count_view);
        TextView uploadDateView = (TextView) this.activity.findViewById(R.id.detail_upload_date_view);
        TextView descriptionView = (TextView) this.activity.findViewById(R.id.detail_description_view);
        RecyclerView nextStreamView = (RecyclerView) this.activity.findViewById(R.id.detail_next_stream_content);
        RelativeLayout nextVideoRootFrame = (RelativeLayout) this.activity.findViewById(R.id.detail_next_stream_root_layout);
        TextView similarTitle = (TextView) this.activity.findViewById(R.id.detail_similar_title);
        Button backgroundButton = (Button) this.activity.findViewById(R.id.detail_stream_thumbnail_window_background_button);
        View thumbnailView = this.activity.findViewById(R.id.detail_thumbnail_view);
        View topView = this.activity.findViewById(R.id.detailTopView);
        Button channelButton = (Button) this.activity.findViewById(R.id.channel_button);
        if (channelButton != null) {
            this.progressBar.setVisibility(8);
            if (info.next_video != null) {
                nextStreamView.setVisibility(8);
            } else {
                nextStreamView.setVisibility(8);
                this.activity.findViewById(R.id.detail_similar_title).setVisibility(8);
            }
            textContentLayout.setVisibility(View.VISIBLE);
            if (VERSION.SDK_INT < 18) {
                this.playVideoButton.setVisibility(View.VISIBLE);
            } else {
                ((ImageView) this.activity.findViewById(R.id.play_arrow_view)).setVisibility(0);
            }
            if (!this.showNextStreamItem) {
                nextVideoRootFrame.setVisibility(8);
                similarTitle.setVisibility(8);
            }
            videoTitleView.setText(info.title);
            OnTouchListener anonymousClass1 = new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == 1) {
                        ImageView imageView = (ImageView) VideoItemDetailFragment.this.activity.findViewById(R.id.toggle_description_view);
                        View extra = VideoItemDetailFragment.this.activity.findViewById(R.id.detailExtraView);
                        if (extra.getVisibility() == 0) {
                            extra.setVisibility(8);
                        }
                    }
                    return true;
                }
            };
            topView.setOnTouchListener(anonymousClass1);
            videoTitleView.setText(info.title);
            if (!info.uploader.isEmpty()) {
                uploaderView.setText(info.uploader);
            } else {
                this.activity.findViewById(R.id.detail_uploader_view).setVisibility(8);
            }
            if (info.view_count >= 0) {
                viewCountView.setText(Localization.localizeViewCount(info.view_count, a));
            } else {
                viewCountView.setVisibility(8);
            }
            if (info.dislike_count >= 0) {
                thumbsDownView.setText(Localization.localizeNumber((long) info.dislike_count, a));
            } else {
                thumbsDownView.setVisibility(4);
                this.activity.findViewById(R.id.detail_thumbs_down_count_view).setVisibility(8);
            }
            if (info.like_count >= 0) {
                thumbsUpView.setText(Localization.localizeNumber((long) info.like_count, a));
            } else {
                thumbsUpView.setVisibility(View.GONE);
                this.activity.findViewById(R.id.detail_thumbs_up_img_view).setVisibility(8);
                thumbsDownView.setVisibility(8);
                this.activity.findViewById(R.id.detail_thumbs_down_img_view).setVisibility(8);
            }
            if (!info.upload_date.isEmpty()) {
                uploadDateView.setText(Localization.localizeDate(info.upload_date, a));
            } else {
                uploadDateView.setVisibility(8);
            }
            if (!info.description.isEmpty()) {
                descriptionView.setText(Html.fromHtml(info.description));
            } else {
                descriptionView.setVisibility(8);
            }
            descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
            Vector<VideoStream> streamsToUse = new Vector<>();
            for (VideoStream i : info.video_streams) {
                if (useStream(i, streamsToUse)) {
                    streamsToUse.add(i);
                }
            }
            textContentLayout.setVisibility(0);
            if (info.next_video == null) {
                this.activity.findViewById(R.id.detail_next_stream_title).setVisibility(8);
            }
            if (info.related_streams == null || info.related_streams.isEmpty()) {
                this.activity.findViewById(R.id.detail_similar_title).setVisibility(8);
                this.activity.findViewById(R.id.similar_streams_view).setVisibility(8);
            } else {
                initSimilarVideos(info);
            }
            setupActionBarHandler(info);
            if (this.autoPlayEnabled) {
                playVideo(info);
            }
            if (VERSION.SDK_INT < 18) {
                FloatingActionButton floatingActionButton = this.playVideoButton;
                final StreamInfo streamInfo = info;
                OnClickListener anonymousClass2 = new OnClickListener() {
                    public void onClick(View v) {
                        VideoItemDetailFragment.this.playVideo(streamInfo);
                    }
                };
                floatingActionButton.setOnClickListener(anonymousClass2);
            }
            final StreamInfo streamInfo2 = info;
            OnClickListener anonymousClass3 = new OnClickListener() {
                public void onClick(View v) {
                    VideoItemDetailFragment.this.playVideo(streamInfo2);
                }
            };
            backgroundButton.setOnClickListener(anonymousClass3);
            final StreamInfo streamInfo3 = info;
            OnClickListener anonymousClass4 = new OnClickListener() {
                public void onClick(View v) {
                    VideoItemDetailFragment.this.playVideo(streamInfo3);
                }
            };
            thumbnailView.setOnClickListener(anonymousClass4);
            if (info.channel_url == null || info.channel_url == "") {
                channelButton.setVisibility(View.GONE);
            } else {
                final StreamInfo streamInfo4 = info;
                OnClickListener anonymousClass5 = new OnClickListener() {
                    public void onClick(View view) {
                        NavStack.getInstance().openChannelActivity(VideoItemDetailFragment.this.getActivity(), streamInfo4.channel_url, streamInfo4.service_id);
                    }
                };
                channelButton.setOnClickListener(anonymousClass5);
            }
            initThumbnailViews(info);
        }
    }

    private void initThumbnailViews(final StreamInfo info) {
        ImageView videoThumbnailView = (ImageView) this.activity.findViewById(R.id.detail_thumbnail_view);
        ImageView uploaderThumb = (ImageView) this.activity.findViewById(R.id.detail_uploader_thumbnail_view);
        if (info.thumbnail_url == null || info.thumbnail_url.isEmpty()) {
            videoThumbnailView.setImageResource(R.drawable.dummy_thumbnail_dark);
        } else {
            this.imageLoader.displayImage(info.thumbnail_url, videoThumbnailView, this.displayImageOptions, (ImageLoadingListener) new ImageLoadingListener() {
                public void onLoadingStarted(String imageUri, View view) {
                }

                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    ErrorActivity.reportError((Context) VideoItemDetailFragment.this.getActivity(), failReason.getCause(), null, VideoItemDetailFragment.this.rootView, ErrorInfo.make(5, Newapp.getNameOfService(info.service_id), imageUri, R.string.could_not_load_thumbnails));
                }

                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    VideoItemDetailFragment.this.streamThumbnail = loadedImage;
                }

                public void onLoadingCancelled(String imageUri, View view) {
                }
            });
        }
        if (info.uploader_thumbnail_url != null && !info.uploader_thumbnail_url.isEmpty()) {
            this.imageLoader.displayImage(info.uploader_thumbnail_url, uploaderThumb, this.displayImageOptions, (ImageLoadingListener) new ImageErrorLoadingListener(this.activity, this.rootView, info.service_id));
        }
    }

    private void setupActionBarHandler(final StreamInfo info) {
        this.actionBarHandler.setupStreamList(info.video_streams);
        this.actionBarHandler.setOnShareListener(new OnActionListener() {
            public void onActionSelected(int selectedStreamId) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.TEXT", info.webpage_url);
                intent.setType("text/plain");
                VideoItemDetailFragment.this.activity.startActivity(Intent.createChooser(intent, VideoItemDetailFragment.this.activity.getString(R.string.share_dialog_title)));
            }
        });
        this.actionBarHandler.setOnOpenInBrowserListener(new OnActionListener() {
            public void onActionSelected(int selectedStreamId) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(info.webpage_url));
                VideoItemDetailFragment.this.activity.startActivity(Intent.createChooser(intent, VideoItemDetailFragment.this.activity.getString(R.string.choose_browser)));
            }
        });
        this.actionBarHandler.setOnPlayWithKodiListener(new OnActionListener() {
            public void onActionSelected(int selectedStreamId) {
                try {
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.setPackage(VideoItemDetailFragment.KORE_PACKET);
                    intent.setData(Uri.parse(info.webpage_url.replace("https", StrongHttpsClient.TYPE_HTTP)));
                    VideoItemDetailFragment.this.activity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoItemDetailFragment.this.activity);
                    builder.setMessage((int) R.string.kore_not_found).setPositiveButton((int) R.string.install, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setData(Uri.parse(VideoItemDetailFragment.this.activity.getString(R.string.fdroid_kore_url)));
                            VideoItemDetailFragment.this.activity.startActivity(intent);
                        }
                    }).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create().show();
                }
            }
        });
        this.actionBarHandler.setOnDownloadListener(new OnActionListener() {
            public void onActionSelected(int selectedStreamId) {

                if(mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                }

                if (PermissionHelper.checkStoragePermissions(VideoItemDetailFragment.this.getActivity())) {
                    try {
                        Bundle args = new Bundle();
                        if (info.audio_streams != null) {
                            AudioStream audioStream = (AudioStream) info.audio_streams.get(VideoItemDetailFragment.this.getPreferredAudioStreamId(info));
                            String audioSuffix = "." + MediaFormat.getSuffixById(audioStream.format);
                            args.putString(DownloadDialog.AUDIO_URL, audioStream.url);
                            args.putString(DownloadDialog.FILE_SUFFIX_AUDIO, audioSuffix);
                        }
                        if (info.video_streams != null) {
                            VideoStream selectedStreamItem = (VideoStream) info.video_streams.get(selectedStreamId);
                            args.putString(DownloadDialog.FILE_SUFFIX_VIDEO, "." + MediaFormat.getSuffixById(selectedStreamItem.format));
                            args.putString("video_url", selectedStreamItem.url);
                        }
                        args.putString(DownloadDialog.TITLE, info.title);
                        DownloadDialog.newInstance(args).show(VideoItemDetailFragment.this.activity.getSupportFragmentManager(), "downloadDialog");
                    } catch (Exception e) {
                        Toast.makeText(VideoItemDetailFragment.this.getActivity(), R.string.could_not_setup_download_menu, 1).show();
                        e.printStackTrace();
                    }
                }
            }
        });
        if (info.audio_streams == null) {
            this.actionBarHandler.showAudioAction(false);
        } else {
            this.actionBarHandler.setOnPlayAudioListener(new OnActionListener() {
                public void onActionSelected(int selectedStreamId) {
                    boolean z;
                    boolean z2 = true;
                    AudioStream audioStream = (AudioStream) info.audio_streams.get(VideoItemDetailFragment.this.getPreferredAudioStreamId(info));
                    if (PreferenceManager.getDefaultSharedPreferences(VideoItemDetailFragment.this.activity).getBoolean(VideoItemDetailFragment.this.activity.getString(R.string.use_external_audio_player_key), false) || VERSION.SDK_INT < 18) {
                        Intent intent = new Intent();
                        try {
                            intent.setAction("android.intent.action.VIEW");
                            intent.setDataAndType(Uri.parse(audioStream.url), MediaFormat.getMimeById(audioStream.format));
                            intent.putExtra("android.intent.extra.TITLE", info.title);
                            intent.putExtra(BackgroundPlayer.TITLE, info.title);
                            VideoItemDetailFragment.this.activity.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            AlertDialog.Builder builder = new AlertDialog.Builder(VideoItemDetailFragment.this.activity);
                            builder.setMessage((int) R.string.no_player_found).setPositiveButton((int) R.string.install, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    intent.setData(Uri.parse(VideoItemDetailFragment.this.activity.getString(R.string.fdroid_vlc_url)));
                                    VideoItemDetailFragment.this.activity.startActivity(intent);
                                }
                            }).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(VideoItemDetailFragment.TAG, "You unlocked a secret unicorn.");
                                }
                            });
                            builder.create().show();
                            Log.e(VideoItemDetailFragment.TAG, "Either no Streaming player for audio was installed, or something important crashed:");
                            e.printStackTrace();
                        }
                    } else if (!BackgroundPlayer.isRunning && VideoItemDetailFragment.this.streamThumbnail != null) {
                        ActivityCommunicator.getCommunicator().backgroundPlayerThumbnail = VideoItemDetailFragment.this.streamThumbnail;
                        Intent intent2 = new Intent(VideoItemDetailFragment.this.activity, BackgroundPlayer.class);
                        intent2.setAction("android.intent.action.VIEW");
                        String access$400 = VideoItemDetailFragment.TAG;
                        StringBuilder append = new StringBuilder().append("audioStream is null:");
                        if (audioStream == null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        Log.i(access$400, append.append(z).toString());
                        String access$4002 = VideoItemDetailFragment.TAG;
                        StringBuilder append2 = new StringBuilder().append("audioStream.url is null:");
                        if (audioStream.url != null) {
                            z2 = false;
                        }
                        Log.i(access$4002, append2.append(z2).toString());
                        intent2.setDataAndType(Uri.parse(audioStream.url), MediaFormat.getMimeById(audioStream.format));
                        intent2.putExtra(BackgroundPlayer.TITLE, info.title);
                        intent2.putExtra(BackgroundPlayer.WEB_URL, info.webpage_url);
                        intent2.putExtra("service_id", VideoItemDetailFragment.this.streamingServiceId);
                        intent2.putExtra(BackgroundPlayer.CHANNEL_NAME, info.uploader);
                        VideoItemDetailFragment.this.activity.startService(intent2);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public int getPreferredAudioStreamId(StreamInfo info) {
        String preferredFormatString = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(this.activity.getString(R.string.default_audio_format_key), "webm");
        int preferredFormat = MediaFormat.WEBMA.id;
        char c = 65535;
        switch (preferredFormatString.hashCode()) {
            case 106458:
                if (preferredFormatString.equals("m4a")) {
                    c = 1;
                    break;
                }
                break;
            case 3645337:
                if (preferredFormatString.equals("webm")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                preferredFormat = MediaFormat.WEBMA.id;
                break;
            case 1:
                preferredFormat = MediaFormat.M4A.id;
                break;
        }
        for (int i = 0; i < info.audio_streams.size(); i++) {
            if (((AudioStream) info.audio_streams.get(i)).format == preferredFormat) {
                return i;
            }
        }
        Log.e(TAG, "FAILED to set audioStream value!");
        return 0;
    }

    private void initSimilarVideos(StreamInfo info) {
        LinearLayout similarLayout = (LinearLayout) this.activity.findViewById(R.id.similar_streams_view);
        for (InfoItem item : info.related_streams) {
            similarLayout.addView(this.infoItemBuilder.buildView(similarLayout, item));
        }
        this.infoItemBuilder.setOnStreamInfoItemSelectedListener(new OnInfoItemSelectedListener() {
            public void selected(String url, int serviceId) {
                NavStack.getInstance().openDetailActivity(VideoItemDetailFragment.this.getContext(), url, serviceId);
            }
        });
    }

    /* access modifiers changed from: private */
    public void onErrorBlockedByGema() {
        Button backgroundButton = (Button) this.activity.findViewById(R.id.detail_stream_thumbnail_window_background_button);
        ImageView thumbnailView = (ImageView) this.activity.findViewById(R.id.detail_thumbnail_view);
        this.progressBar.setVisibility(8);
        thumbnailView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.gruese_die_gema));
        backgroundButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(VideoItemDetailFragment.this.activity.getString(R.string.c3s_url)));
                VideoItemDetailFragment.this.activity.startActivity(intent);
            }
        });
        Toast.makeText(getActivity(), R.string.blocked_by_gema, 1).show();
    }

    /* access modifiers changed from: private */
    public void onNotSpecifiedContentError() {
        ImageView thumbnailView = (ImageView) this.activity.findViewById(R.id.detail_thumbnail_view);
        this.progressBar.setVisibility(8);
        thumbnailView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.not_available_monkey));
        Toast.makeText(this.activity, R.string.content_not_available, 1).show();
    }

    /* access modifiers changed from: private */
    public void onNotSpecifiedContentErrorWithMessage(int resourceId) {
        ImageView thumbnailView = (ImageView) this.activity.findViewById(R.id.detail_thumbnail_view);
        this.progressBar.setVisibility(8);
        thumbnailView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.not_available_monkey));
        Toast.makeText(this.activity, resourceId, 1).show();
    }

    private boolean useStream(VideoStream stream, Vector<VideoStream> streams) {
        Iterator it = streams.iterator();
        while (it.hasNext()) {
            if (((VideoStream) it.next()).resolution.equals(stream.resolution)) {
                return false;
            }
        }
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = (AppCompatActivity) getActivity();
        this.showNextStreamItem = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(this.activity.getString(R.string.show_next_video_key), true);
        StreamInfoWorker.getInstance().setOnStreamInfoReceivedListener(new OnStreamInfoReceivedListener() {
            public void onReceive(StreamInfo info) {
                VideoItemDetailFragment.this.updateInfo(info);
            }

            public void onError(int messageId) {
                VideoItemDetailFragment.this.postNewErrorToast(messageId);
            }

            public void onReCaptchaException() {
                Toast.makeText(VideoItemDetailFragment.this.getActivity(), R.string.recaptcha_request_toast, 1).show();
                VideoItemDetailFragment.this.startActivityForResult(new Intent(VideoItemDetailFragment.this.getActivity(), ReCaptchaActivity.class), 10);
            }

            public void onBlockedByGemaError() {
                VideoItemDetailFragment.this.onErrorBlockedByGema();
            }

            public void onContentErrorWithMessage(int messageId) {
                VideoItemDetailFragment.this.onNotSpecifiedContentErrorWithMessage(messageId);
            }

            public void onContentError() {
                VideoItemDetailFragment.this.onNotSpecifiedContentError();
            }
        });
        setHasOptionsMenu(true);

        MobileAds.initialize(getContext(),
                "ca-app-pub-3940256099942544~3347511713");

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_videoitem_detail, container, false);
        this.progressBar = (ProgressBar) this.rootView.findViewById(R.id.detail_progress_bar);
        this.actionBarHandler = new ActionBarHandler(this.activity);
        this.actionBarHandler.setupNavMenu(this.activity);
        if (this.onInvokeCreateOptionsMenuListener != null) {
            this.onInvokeCreateOptionsMenuListener.createOptionsMenu();
        }
        return this.rootView;
    }

    public void onStart() {
        super.onStart();
        Activity a = getActivity();
        this.infoItemBuilder = new InfoItemBuilder(a, a.findViewById(16908290));
        if (VERSION.SDK_INT < 18) {
            this.playVideoButton = (FloatingActionButton) a.findViewById(R.id.play_video_button);
        }
        this.thumbnailWindowLayout = a.findViewById(R.id.detail_stream_thumbnail_window_layout);
        if (((Button) a.findViewById(R.id.detail_stream_thumbnail_window_background_button)) != null) {
            this.streamingServiceId = getArguments().getInt("service_id");
            StreamInfoWorker.getInstance().search(this.streamingServiceId, getArguments().getString("url"), getActivity());
            this.autoPlayEnabled = getArguments().getBoolean(AUTO_PLAY);
            if (VERSION.SDK_INT >= 18) {
                ((ImageView) this.activity.findViewById(R.id.detail_thumbnail_view)).addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        LayoutParams newWindowLayoutParams = (LayoutParams) VideoItemDetailFragment.this.thumbnailWindowLayout.getLayoutParams();
                        newWindowLayoutParams.height = bottom - top;
                        VideoItemDetailFragment.this.thumbnailWindowLayout.setLayoutParams(newWindowLayoutParams);
                        VideoItemDetailFragment.this.initialThumbnailPos.set(top, left);
                    }
                });
            }
        }
    }

    public void playVideo(StreamInfo info) {
        VideoStream selectedVideoStream = (VideoStream) info.video_streams.get(this.actionBarHandler.getSelectedVideoStream());
        if (PreferenceManager.getDefaultSharedPreferences(this.activity).getBoolean(this.activity.getString(R.string.use_external_video_player_key), false)) {
            Intent intent = new Intent();
            try {
                intent.setAction("android.intent.action.VIEW").setDataAndType(Uri.parse(selectedVideoStream.url), MediaFormat.getMimeById(selectedVideoStream.format)).putExtra("android.intent.extra.TITLE", info.title).putExtra(BackgroundPlayer.TITLE, info.title);
                this.activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
                builder.setMessage((int) R.string.no_player_found).setPositiveButton((int) R.string.install, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        VideoItemDetailFragment.this.activity.startActivity(new Intent().setAction("android.intent.action.VIEW").setData(Uri.parse(VideoItemDetailFragment.this.activity.getString(R.string.fdroid_vlc_url))));
                    }
                }).setNegativeButton((int) R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
            }
        } else if (!PreferenceManager.getDefaultSharedPreferences(this.activity).getBoolean(this.activity.getString(R.string.use_exoplayer_key), false)) {
            this.activity.startActivity(new Intent(this.activity, PlayVideoActivity.class).putExtra(PlayVideoActivity.VIDEO_TITLE, info.title).putExtra(PlayVideoActivity.STREAM_URL, selectedVideoStream.url).putExtra("video_url", info.webpage_url).putExtra(PlayVideoActivity.START_POSITION, info.start_position));
        } else if (info.dashMpdUrl != null && !info.dashMpdUrl.isEmpty()) {
            startActivity(new Intent(this.activity, ExoPlayerActivity.class).setData(Uri.parse(info.dashMpdUrl)).putExtra(ExoPlayerActivity.CONTENT_TYPE_EXTRA, 0));
        } else if (info.audio_streams == null || info.audio_streams.isEmpty() || info.video_only_streams == null || info.video_only_streams.isEmpty()) {
            this.activity.startActivity(new Intent(this.activity, ExoPlayerActivity.class).setDataAndType(Uri.parse(selectedVideoStream.url), MediaFormat.getMimeById(selectedVideoStream.format)).putExtra(ExoPlayerActivity.CONTENT_TYPE_EXTRA, 3));
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.actionBarHandler.setupMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        return this.actionBarHandler.onItemSelected(item);
    }

    public void setOnInvokeCreateOptionsMenuListener(OnInvokeCreateOptionsMenuListener listener) {
        this.onInvokeCreateOptionsMenuListener = listener;
    }

    /* access modifiers changed from: private */
    public void postNewErrorToast(int stringResource) {
        Toast.makeText(getActivity(), stringResource, 1).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10:
                if (resultCode == -1) {
                    StreamInfoWorker.getInstance().search(this.streamingServiceId, getArguments().getString("url"), getActivity());
                    return;
                }
                Log.d(TAG, "ReCaptcha failed");
                return;
            default:
                Log.e(TAG, "Request code from activity not supported [" + requestCode + "]");
                return;
        }
    }
}
