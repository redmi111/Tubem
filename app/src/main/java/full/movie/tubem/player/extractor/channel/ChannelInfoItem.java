package full.movie.tubem.player.extractor.channel;

import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.extractor.InfoItem.InfoType;

public class ChannelInfoItem implements InfoItem {
    public String channelName = "";
    public String description = "";
    public int serviceId = -1;
    public long subscriberCount = -1;
    public String thumbnailUrl = "";
    public int videoAmount = -1;
    public String webPageUrl = "";

    public InfoType infoType() {
        return InfoType.CHANNEL;
    }

    public String getTitle() {
        return this.channelName;
    }

    public String getLink() {
        return this.webPageUrl;
    }
}
