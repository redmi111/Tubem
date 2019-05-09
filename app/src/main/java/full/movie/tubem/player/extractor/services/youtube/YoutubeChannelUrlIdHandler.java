package full.movie.tubem.player.extractor.services.youtube;

import full.movie.tubem.player.extractor.Parser;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.ParsingException;

public class YoutubeChannelUrlIdHandler implements UrlIdHandler {
    public String getUrl(String channelId) {
        return "https://www.youtube.com/" + channelId;
    }

    public String getId(String siteUrl) throws ParsingException {
        return Parser.matchGroup1("/(user/[A-Za-z0-9_-]*|channel/[A-Za-z0-9_-]*)", siteUrl);
    }

    public String cleanUrl(String siteUrl) throws ParsingException {
        return getUrl(getId(siteUrl));
    }

    public boolean acceptUrl(String videoUrl) {
        return (videoUrl.contains("youtube") || videoUrl.contains("youtu.be")) && (videoUrl.contains("/user/") || videoUrl.contains("/channel/"));
    }
}
