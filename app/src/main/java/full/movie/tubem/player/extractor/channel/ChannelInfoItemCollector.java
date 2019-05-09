package full.movie.tubem.player.extractor.channel;

import full.movie.tubem.player.extractor.InfoItemCollector;
import full.movie.tubem.player.extractor.exceptions.ParsingException;

public class ChannelInfoItemCollector extends InfoItemCollector {
    public ChannelInfoItemCollector(int serviceId) {
        super(serviceId);
    }

    public ChannelInfoItem extract(ChannelInfoItemExtractor extractor) throws ParsingException {
        ChannelInfoItem resultItem = new ChannelInfoItem();
        resultItem.channelName = extractor.getChannelName();
        resultItem.serviceId = getServiceId();
        resultItem.webPageUrl = extractor.getWebPageUrl();
        try {
            resultItem.subscriberCount = extractor.getSubscriberCount();
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.videoAmount = extractor.getVideoAmount();
        } catch (Exception e2) {
            addError(e2);
        }
        try {
            resultItem.thumbnailUrl = extractor.getThumbnailUrl();
        } catch (Exception e3) {
            addError(e3);
        }
        try {
            resultItem.description = extractor.getDescription();
        } catch (Exception e4) {
            addError(e4);
        }
        return resultItem;
    }

    public void commit(ChannelInfoItemExtractor extractor) throws ParsingException {
        try {
            addItem(extract(extractor));
        } catch (Exception e) {
            addError(e);
        }
    }
}
