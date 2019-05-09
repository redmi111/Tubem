package full.movie.tubem.player.extractor.services.youtube;

import full.movie.tubem.player.extractor.channel.ChannelInfoItemExtractor;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import org.jsoup.nodes.Element;

public class YoutubeChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    private Element el;

    public YoutubeChannelInfoItemExtractor(Element el2) {
        this.el = el2;
    }

    public String getThumbnailUrl() throws ParsingException {
        Element img = this.el.select("span[class*=\"yt-thumb-simple\"]").first().select("img").first();
        String url = img.attr("abs:src");
        if (url.contains("gif")) {
            return img.attr("abs:data-thumb");
        }
        return url;
    }

    public String getChannelName() throws ParsingException {
        return this.el.select("a[class*=\"yt-uix-tile-link\"]").first().text();
    }

    public String getWebPageUrl() throws ParsingException {
        return this.el.select("a[class*=\"yt-uix-tile-link\"]").first().attr("abs:href");
    }

    public long getSubscriberCount() throws ParsingException {
        Element subsEl = this.el.select("span[class*=\"yt-subscriber-count\"]").first();
        if (subsEl == null) {
            return 0;
        }
        return (long) Integer.parseInt(subsEl.text().replaceAll("\\D+", ""));
    }

    public int getVideoAmount() throws ParsingException {
        Element metaEl = this.el.select("ul[class*=\"yt-lockup-meta-info\"]").first();
        if (metaEl == null) {
            return 0;
        }
        return Integer.parseInt(metaEl.text().replaceAll("\\D+", ""));
    }

    public String getDescription() throws ParsingException {
        Element desEl = this.el.select("div[class*=\"yt-lockup-description\"]").first();
        if (desEl == null) {
            return "";
        }
        return desEl.text();
    }
}
