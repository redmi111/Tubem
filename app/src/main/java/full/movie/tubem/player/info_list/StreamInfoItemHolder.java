package full.movie.tubem.player.info_list;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import full.movie.tubem.player.R;
import full.movie.tubem.player.extractor.InfoItem.InfoType;

public class StreamInfoItemHolder extends InfoItemHolder {
    public final Button itemButton;
    public final TextView itemDurationView;
    public final ImageView itemThumbnailView;
    public final TextView itemUploadDateView;
    public final TextView itemUploaderView;
    public final TextView itemVideoTitleView;
    public final TextView itemViewCountView;

    public StreamInfoItemHolder(View v) {
        super(v);
        this.itemThumbnailView = (ImageView) v.findViewById(R.id.itemThumbnailView);
        this.itemVideoTitleView = (TextView) v.findViewById(R.id.itemVideoTitleView);
        this.itemUploaderView = (TextView) v.findViewById(R.id.itemUploaderView);
        this.itemDurationView = (TextView) v.findViewById(R.id.itemDurationView);
        this.itemUploadDateView = (TextView) v.findViewById(R.id.itemUploadDateView);
        this.itemViewCountView = (TextView) v.findViewById(R.id.itemViewCountView);
        this.itemButton = (Button) v.findViewById(R.id.item_button);
    }

    public InfoType infoType() {
        return InfoType.STREAM;
    }
}
