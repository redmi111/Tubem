package full.movie.tubem.player.extractor.channel;

import full.movie.tubem.player.extractor.exceptions.ParsingException;

public interface ChannelInfoItemExtractor {
    String getChannelName() throws ParsingException;

    String getDescription() throws ParsingException;

    long getSubscriberCount() throws ParsingException;

    String getThumbnailUrl() throws ParsingException;

    int getVideoAmount() throws ParsingException;

    String getWebPageUrl() throws ParsingException;
}
