package full.movie.tubem.player.extractor.stream_info;

import full.movie.tubem.player.extractor.AbstractStreamInfo;
import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.extractor.InfoItem.InfoType;

public class StreamInfoItem extends AbstractStreamInfo implements InfoItem {
    public int duration;

    public InfoType infoType() {
        return InfoType.STREAM;
    }

    public String getTitle() {
        return this.title;
    }

    public String getLink() {
        return this.webpage_url;
    }
}
