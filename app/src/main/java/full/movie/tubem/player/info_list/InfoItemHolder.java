package full.movie.tubem.player.info_list;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import full.movie.tubem.player.extractor.InfoItem.InfoType;

public abstract class InfoItemHolder extends ViewHolder {
    public abstract InfoType infoType();

    public InfoItemHolder(View v) {
        super(v);
    }
}
