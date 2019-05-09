package full.movie.tubem.player.info_list;

import android.app.Activity;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import full.movie.tubem.player.R;
import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.info_list.InfoItemBuilder.OnInfoItemSelectedListener;
import java.util.List;
import java.util.Vector;

public class InfoListAdapter extends Adapter<InfoItemHolder> {
    private static final String TAG = InfoListAdapter.class.toString();
    private final InfoItemBuilder infoItemBuilder;
    private final List<InfoItem> infoItemList = new Vector();

    public InfoListAdapter(Activity a, View rootView) {
        this.infoItemBuilder = new InfoItemBuilder(a, rootView);
    }

    public void setOnStreamInfoItemSelectedListener(OnInfoItemSelectedListener listener) {
        this.infoItemBuilder.setOnStreamInfoItemSelectedListener(listener);
    }

    public void setOnChannelInfoItemSelectedListener(OnInfoItemSelectedListener listener) {
        this.infoItemBuilder.setOnChannelInfoItemSelectedListener(listener);
    }

    public void addInfoItemList(List<InfoItem> videos) {
        if (videos != null) {
            this.infoItemList.addAll(videos);
            notifyDataSetChanged();
        }
    }

    public void clearSteamItemList() {
        this.infoItemList.clear();
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return this.infoItemList.size();
    }

    public int getItemViewType(int position) {
        switch (((InfoItem) this.infoItemList.get(position)).infoType()) {
            case STREAM:
                return 0;
            case CHANNEL:
                return 1;
            case PLAYLIST:
                return 2;
            default:
                Log.e(TAG, "Trollolo");
                return -1;
        }
    }

    public InfoItemHolder onCreateViewHolder(ViewGroup parent, int type) {
        switch (type) {
            case 0:
                return new StreamInfoItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.stream_item, parent, false));
            case 1:
                return new ChannelInfoItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item, parent, false));
            case 2:
                Log.e(TAG, "Playlist is not yet implemented");
                return null;
            default:
                Log.e(TAG, "Trollolo");
                return null;
        }
    }

    public void onBindViewHolder(InfoItemHolder holder, int i) {
        this.infoItemBuilder.buildByHolder(holder, (InfoItem) this.infoItemList.get(i));
    }
}
