package full.movie.tubem.player.info_list;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.exoplayer.C;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import full.movie.tubem.player.ImageErrorLoadingListener;
import full.movie.tubem.player.R;
import full.movie.tubem.player.extractor.AbstractStreamInfo.StreamType;
import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.extractor.InfoItem.InfoType;
import full.movie.tubem.player.extractor.channel.ChannelInfoItem;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItem;

public class InfoItemBuilder {
    private static final String TAG = InfoItemBuilder.class.toString();
    private Activity activity = null;
    final String billion;
    private DisplayImageOptions displayImageOptions = new Builder().cacheInMemory(true).build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    final String million;
    /* access modifiers changed from: private */
    public OnInfoItemSelectedListener onChannelInfoItemSelectedListener;
    /* access modifiers changed from: private */
    public OnInfoItemSelectedListener onStreamInfoItemSelectedListener;
    private View rootView = null;
    final String subsS;
    final String thousand;
    final String videosS;
    final String viewsS;

    public interface OnInfoItemSelectedListener {
        void selected(String str, int i);
    }

    public InfoItemBuilder(Activity a, View rootView2) {
        this.activity = a;
        this.rootView = rootView2;
        this.viewsS = a.getString(R.string.views);
        this.videosS = a.getString(R.string.videos);
        this.subsS = a.getString(R.string.subscriber);
        this.thousand = a.getString(R.string.short_thousand);
        this.million = a.getString(R.string.short_million);
        this.billion = a.getString(R.string.short_billion);
    }

    public void setOnStreamInfoItemSelectedListener(OnInfoItemSelectedListener listener) {
        this.onStreamInfoItemSelectedListener = listener;
    }

    public void setOnChannelInfoItemSelectedListener(OnInfoItemSelectedListener listener) {
        this.onChannelInfoItemSelectedListener = listener;
    }

    public void buildByHolder(InfoItemHolder holder, InfoItem i) {
        if (i.infoType() == holder.infoType()) {
            switch (i.infoType()) {
                case STREAM:
                    buildStreamInfoItem((StreamInfoItemHolder) holder, (StreamInfoItem) i);
                    return;
                case CHANNEL:
                    buildChannelInfoItem((ChannelInfoItemHolder) holder, (ChannelInfoItem) i);
                    return;
                case PLAYLIST:
                    Log.e(TAG, "Not yet implemented");
                    return;
                default:
                    Log.e(TAG, "Trollolo");
                    return;
            }
        }
    }

    public View buildView(ViewGroup parent, InfoItem info) {
        View itemView = null;
        InfoItemHolder holder = null;
        switch (info.infoType()) {
            case STREAM:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stream_item, parent, false);
                holder = new StreamInfoItemHolder(itemView);
                break;
            case CHANNEL:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false);
                holder = new ChannelInfoItemHolder(itemView);
                break;
            case PLAYLIST:
                Log.e(TAG, "Not yet implemented");
                break;
        }
        Log.e(TAG, "Trollolo");
        buildByHolder(holder, info);
        return itemView;
    }

    private void buildStreamInfoItem(StreamInfoItemHolder holder, final StreamInfoItem info) {
        if (info.infoType() != InfoType.STREAM) {
            Log.e("InfoItemBuilder", "Info type not yet supported");
        }
        holder.itemVideoTitleView.setText(info.title);
        if (info.uploader == null || info.uploader.isEmpty()) {
            holder.itemUploaderView.setVisibility(4);
        } else {
            holder.itemUploaderView.setText(info.uploader);
        }
        if (info.duration > 0) {
            holder.itemDurationView.setText(getDurationString(info.duration));
        } else if (info.stream_type == StreamType.LIVE_STREAM) {
            holder.itemDurationView.setText(R.string.duration_live);
        } else {
            holder.itemDurationView.setVisibility(8);
        }
        if (info.view_count >= 0) {
            holder.itemViewCountView.setText(shortViewCount(Long.valueOf(info.view_count)));
        } else {
            holder.itemViewCountView.setVisibility(8);
        }
        if (info.upload_date != null && !info.upload_date.isEmpty()) {
            holder.itemUploadDateView.setText(info.upload_date + " • ");
        }
        holder.itemThumbnailView.setImageResource(R.drawable.dummy_thumbnail);
        if (info.thumbnail_url != null && !info.thumbnail_url.isEmpty()) {
            this.imageLoader.displayImage(info.thumbnail_url, holder.itemThumbnailView, this.displayImageOptions, (ImageLoadingListener) new ImageErrorLoadingListener(this.activity, this.rootView, info.service_id));
        }
        holder.itemButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                InfoItemBuilder.this.onStreamInfoItemSelectedListener.selected(info.webpage_url, info.service_id);
            }
        });
    }

    private void buildChannelInfoItem(ChannelInfoItemHolder holder, final ChannelInfoItem info) {
        holder.itemChannelTitleView.setText(info.getTitle());
        holder.itemSubscriberCountView.setText(shortSubscriber(Long.valueOf(info.subscriberCount)) + " • ");
        holder.itemVideoCountView.setText(info.videoAmount + " " + this.videosS);
        holder.itemChannelDescriptionView.setText(info.description);
        holder.itemThumbnailView.setImageResource(R.drawable.buddy_channel_item);
        if (info.thumbnailUrl != null && !info.thumbnailUrl.isEmpty()) {
            this.imageLoader.displayImage(info.thumbnailUrl, (ImageView) holder.itemThumbnailView, this.displayImageOptions, (ImageLoadingListener) new ImageErrorLoadingListener(this.activity, this.rootView, info.serviceId));
        }
        holder.itemButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                InfoItemBuilder.this.onChannelInfoItemSelectedListener.selected(info.getLink(), info.serviceId);
            }
        });
    }

    public String shortViewCount(Long viewCount) {
        if (viewCount.longValue() >= 1000000000) {
            return Long.toString(viewCount.longValue() / 1000000000) + this.billion + " " + this.viewsS;
        }
        if (viewCount.longValue() >= C.MICROS_PER_SECOND) {
            return Long.toString(viewCount.longValue() / C.MICROS_PER_SECOND) + this.million + " " + this.viewsS;
        }
        if (viewCount.longValue() >= 1000) {
            return Long.toString(viewCount.longValue() / 1000) + this.thousand + " " + this.viewsS;
        }
        return Long.toString(viewCount.longValue()) + " " + this.viewsS;
    }

    public String shortSubscriber(Long viewCount) {
        if (viewCount.longValue() >= 1000000000) {
            return Long.toString(viewCount.longValue() / 1000000000) + this.billion + " " + this.subsS;
        }
        if (viewCount.longValue() >= C.MICROS_PER_SECOND) {
            return Long.toString(viewCount.longValue() / C.MICROS_PER_SECOND) + this.million + " " + this.subsS;
        }
        if (viewCount.longValue() >= 1000) {
            return Long.toString(viewCount.longValue() / 1000) + this.thousand + " " + this.subsS;
        }
        return Long.toString(viewCount.longValue()) + " " + this.subsS;
    }

    public static String getDurationString(int duration) {
        String output;
        String output2;
        String output3 = "";
        int days = duration / 86400;
        int duration2 = duration % 86400;
        int hours = duration2 / 3600;
        int duration3 = duration2 % 3600;
        int minutes = duration3 / 60;
        int seconds = duration3 % 60;
        if (days > 0) {
            output3 = Integer.toString(days) + ":";
        }
        if (hours > 0 || !output3.isEmpty()) {
            if (hours <= 0) {
                output2 = output3 + "00";
            } else if (hours >= 10 || output3.isEmpty()) {
                output2 = output3 + Integer.toString(hours);
            } else {
                output2 = output3 + "0" + Integer.toString(hours);
            }
            output3 = output2 + ":";
        }
        if (minutes > 0 || !output3.isEmpty()) {
            if (minutes <= 0) {
                output = output3 + "00";
            } else if (minutes >= 10 || output3.isEmpty()) {
                output = output3 + Integer.toString(minutes);
            } else {
                output = output3 + "0" + Integer.toString(minutes);
            }
            output3 = output + ":";
        }
        if (output3.isEmpty()) {
            output3 = output3 + "0:";
        }
        if (seconds >= 10) {
            return output3 + Integer.toString(seconds);
        }
        return output3 + "0" + Integer.toString(seconds);
    }
}
