package full.movie.tubem.player.info_list;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import full.movie.tubem.player.R;
import full.movie.tubem.player.extractor.InfoItem.InfoType;

public class ChannelInfoItemHolder extends InfoItemHolder {
    public final Button itemButton;
    public final TextView itemChannelDescriptionView;
    public final TextView itemChannelTitleView;
    public final TextView itemSubscriberCountView;
    public final CircleImageView itemThumbnailView;
    public final TextView itemVideoCountView;

    ChannelInfoItemHolder(View v) {
        super(v);
        this.itemThumbnailView = (CircleImageView) v.findViewById(R.id.itemThumbnailView);
        this.itemChannelTitleView = (TextView) v.findViewById(R.id.itemChannelTitleView);
        this.itemSubscriberCountView = (TextView) v.findViewById(R.id.itemSubscriberCountView);
        this.itemVideoCountView = (TextView) v.findViewById(R.id.itemVideoCountView);
        this.itemChannelDescriptionView = (TextView) v.findViewById(R.id.itemChannelDescriptionView);
        this.itemButton = (Button) v.findViewById(R.id.item_button);
    }

    public InfoType infoType() {
        return InfoType.CHANNEL;
    }
}
