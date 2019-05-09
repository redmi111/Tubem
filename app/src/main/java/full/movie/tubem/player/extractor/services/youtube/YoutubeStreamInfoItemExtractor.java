package full.movie.tubem.player.extractor.services.youtube;

import full.movie.tubem.player.extractor.AbstractStreamInfo.StreamType;
import full.movie.tubem.player.extractor.Parser;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemExtractor;
import org.jsoup.nodes.Element;

public class YoutubeStreamInfoItemExtractor implements StreamInfoItemExtractor {
    private final Element item;

    public YoutubeStreamInfoItemExtractor(Element item2) {
        this.item = item2;
    }

   /* public String getWebPageUrl() throws ParsingException {
        try {
            return this.item.select("div[class*=\"yt-lockup-video\"")
                    .first().select("h3")
                    .first().select("a")
                    .first().attr("abs:href");
        } catch (Exception e) {
            throw new ParsingException("Could not get web page url for the video", e);
        }
    }*/
   public String getWebPageUrl()
           throws ParsingException
   {
       try
       {
           String str = this.item.select("div[class*=\"yt-lockup-video\"")
                   .first().select("h3")
                   .first().select("a")
                   .first().attr("abs:href");
           return str;
       }
       catch (Exception localException)
       {
           throw new ParsingException("Could not get web page url for the video", localException);
       }
   }

    public String getTitle() throws ParsingException {
        try {
            return this.item.select("div[class*=\"yt-lockup-video\"").first().select("h3").first().select("a").first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get title", e);
        }
    }

    public int getDuration() throws ParsingException {
        try {
            return YoutubeParsingHelper.parseDurationString(this.item.select("span[class=\"video-time\"]").first().text());
        } catch (Exception e) {
            if (isLiveStream(this.item)) {
                return -1;
            }
            throw new ParsingException("Could not get Duration: " + getTitle(), e);
        }
    }

    public String getUploader() throws ParsingException {
        try {
            return this.item.select("div[class=\"yt-lockup-byline\"]").first().select("a").first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get uploader", e);
        }
    }

    public String getUploadDate() throws ParsingException {
        try {
            Element div = this.item.select("div[class=\"yt-lockup-meta\"]").first();
            if (div == null) {
                return null;
            }
            return div.select("li").first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get uplaod date", e);
        }
    }

    public long getViewCount() throws ParsingException {
        try {
            Element div = this.item.select("div[class=\"yt-lockup-meta\"]").first();
            if (div == null) {
                return -1;
            }
            String input = ((Element) div.select("li").get(1)).text();
            try {
                return Long.parseLong(Parser.matchGroup1("([0-9,\\. ]*)", input).replace(" ", "").replace(".", "").replace(",", ""));
            } catch (NumberFormatException e) {
                if (!input.isEmpty()) {
                    return 0;
                }
                throw new ParsingException("Could not handle input: " + input, e);
            }
        } catch (IndexOutOfBoundsException e2) {
            if (isLiveStream(this.item)) {
                return -1;
            }
            throw new ParsingException("Could not parse yt-lockup-meta although available: " + getTitle(), e2);
        }
    }

    public String getThumbnailUrl() throws ParsingException {
        try {
            Element te = this.item.select("div[class=\"yt-thumb video-thumb\"]").first().select("img").first();
            String url = te.attr("abs:src");
            if (url.contains(".gif")) {
                return te.attr("abs:data-thumb");
            }
            return url;
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    public StreamType getStreamType() {
        if (isLiveStream(this.item)) {
            return StreamType.LIVE_STREAM;
        }
        return StreamType.VIDEO_STREAM;
    }

    private boolean isLiveStream(Element item2) {
        Element bla = item2.select("span[class*=\"yt-badge-live\"]").first();
        if ((bla != null || item2.select("span[class*=\"video-time\"]").first() != null) && bla == null) {
            return false;
        }
        return true;
    }
}
