package full.movie.tubem.player.extractor.channel;

import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemCollector;
import java.util.List;
import java.util.Vector;

public class ChannelInfo {
    public String avatar_url = "";
    public String banner_url = "";
    public String channel_name = "";
    public List<Throwable> errors = new Vector();
    public String feed_url = "";
    public boolean hasNextPage = false;
    public List<InfoItem> related_streams = null;
    public int service_id = -1;

    public void addException(Exception e) {
        this.errors.add(e);
    }

    public static ChannelInfo getInfo(ChannelExtractor extractor) throws ParsingException {
        ChannelInfo info = new ChannelInfo();
        info.service_id = extractor.getServiceId();
        info.channel_name = extractor.getChannelName();
        info.hasNextPage = extractor.hasNextPage();
        try {
            info.avatar_url = extractor.getAvatarUrl();
        } catch (Exception e) {
            info.errors.add(e);
        }
        try {
            info.banner_url = extractor.getBannerUrl();
        } catch (Exception e2) {
            info.errors.add(e2);
        }
        try {
            info.feed_url = extractor.getFeedUrl();
        } catch (Exception e3) {
            info.errors.add(e3);
        }
        try {
            StreamInfoItemCollector c = extractor.getStreams();
            info.related_streams = c.getItemList();
            info.errors.addAll(c.getErrors());
        } catch (Exception e4) {
            info.errors.add(e4);
        }
        return info;
    }
}
