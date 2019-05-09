/*
package full.movie.tubem.player.extractor.services.youtube;

import full.movie.tubem.player.extractor.AbstractStreamInfo.StreamType;
import full.movie.tubem.player.extractor.Downloader;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.Parser;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.channel.ChannelExtractor;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemCollector;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemExtractor;
import java.io.IOException;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class YoutubeChannelExtractor extends ChannelExtractor {
    private static final String TAG = YoutubeChannelExtractor.class.toString();
    private static String avatarUrl = "";
    private static String bannerUrl = "";
    private static String channelName = "";
    private static String feedUrl = "";
    private static String nextPageUrl = "";
    private static String userUrl = "";
    private Document doc = null;
    private boolean isAjaxPage = false;

    public YoutubeChannelExtractor(UrlIdHandler urlIdHandler, String url, int page, int serviceId) throws ExtractionException, IOException {
        super(urlIdHandler, url, page, serviceId);
        Downloader downloader = Newapp.getDownloader();
        String url2 = urlIdHandler.cleanUrl(url);
        if (page == 0) {
            if (isUserUrl(url2)) {
                userUrl = url2;
            } else {
                userUrl = getUserUrl(Jsoup.parse(downloader.download(url2), url2));
            }
            userUrl += "/videos?veiw=0&flow=list&sort=dd&live_view=10000";
            this.doc = Jsoup.parse(downloader.download(userUrl), userUrl);
            nextPageUrl = getNextPageUrl(this.doc);
            this.isAjaxPage = false;
            return;
        }
        try {
            JSONObject ajaxData = new JSONObject(downloader.download(nextPageUrl));
            this.doc = Jsoup.parse(ajaxData.getString("content_html"), nextPageUrl);
            String nextPageHtmlDataRaw = ajaxData.getString("load_more_widget_html");
            if (!nextPageHtmlDataRaw.isEmpty()) {
                nextPageUrl = getNextPageUrl(Jsoup.parse(nextPageHtmlDataRaw, nextPageUrl));
            } else {
                nextPageUrl = "";
            }
            this.isAjaxPage = true;
        } catch (JSONException e) {
            throw new ParsingException("Could not parse json data for next page", e);
        }
    }

    public String getChannelName() throws ParsingException {
        try {
            if (!this.isAjaxPage) {
                channelName = this.doc.select("span[class=\"qualified-channel-title-text\"]").first().select("a").first().text();
            }
            return channelName;
        } catch (Exception e) {
            throw new ParsingException("Could not get channel name");
        }
    }

    public String getAvatarUrl() throws ParsingException {
        try {
            if (!this.isAjaxPage) {
                avatarUrl = this.doc.select("img[class=\"channel-header-profile-image\"]").first().attr("abs:src");
            }
            return avatarUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get avatar", e);
        }
    }

    public String getBannerUrl() throws ParsingException {
        try {
            if (!this.isAjaxPage) {
                //ToDo: "style" instead of TtmlNode
                String url = YoutubeStreamExtractor.HTTPS + Parser.matchGroup1("url\\(([^)]+)\\)", this.doc.select("div[id=\"gh-banner\"]").first().select("style").first().html());
                if (url.contains("s.ytimg.com") || url.contains("default_banner")) {
                    bannerUrl = null;
                } else {
                    bannerUrl = url;
                }
            }
            return bannerUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get Banner", e);
        }
    }

    public StreamInfoItemCollector getStreams() throws ParsingException {
        Element ul;
        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        if (this.isAjaxPage) {
            //ToDo: "body" instead of TtmlNode
            ul = this.doc.select("body").first();
        } else {
            ul = this.doc.select("ul[id=\"browse-items-primary\"]").first();
        }
        Iterator it = ul.children().iterator();
        while (it.hasNext()) {
            final Element li = (Element) it.next();
            if (li.select("div[class=\"feed-item-dismissable\"]").first() != null) {
                collector.commit(new StreamInfoItemExtractor() {
                    public StreamType getStreamType() throws ParsingException {
                        return StreamType.VIDEO_STREAM;
                    }

                    public String getWebPageUrl() throws ParsingException {
                        try {
                            return li.select("div[class=\"feed-item-dismissable\"]").first().select("h3").first().select("a").first().attr("abs:href");
                        } catch (Exception e) {
                            throw new ParsingException("Could not get web page url for the video", e);
                        }
                    }

                    public String getTitle() throws ParsingException {
                        try {
                            return li.select("div[class=\"feed-item-dismissable\"]").first().select("h3").first().select("a").first().text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get title", e);
                        }
                    }

                    public int getDuration() throws ParsingException {
                        try {
                            return YoutubeParsingHelper.parseDurationString(li.select("span[class=\"video-time\"]").first().text());
                        } catch (Exception e) {
                            if (isLiveStream(li)) {
                                return -1;
                            }
                            throw new ParsingException("Could not get Duration: " + getTitle(), e);
                        }
                    }

                    public String getUploader() throws ParsingException {
                        return YoutubeChannelExtractor.this.getChannelName();
                    }

                    public String getUploadDate() throws ParsingException {
                        try {
                            return li.select("div[class=\"yt-lockup-meta\"]").first().select("li").first().text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get uplaod date", e);
                        }
                    }

                    public long getViewCount() throws ParsingException {
                        try {
                            String input = ((Element) li.select("div[class=\"yt-lockup-meta\"]").first().select("li").get(1)).text();
                            try {
                                return Long.parseLong(Parser.matchGroup1("([0-9,\\. ]*)", input).replace(" ", "").replace(".", "").replace(",", ""));
                            } catch (NumberFormatException e) {
                                if (!input.isEmpty()) {
                                    return 0;
                                }
                                throw new ParsingException("Could not handle input: " + input, e);
                            }
                        } catch (IndexOutOfBoundsException e2) {
                            if (isLiveStream(li)) {
                                return -1;
                            }
                            throw new ParsingException("Could not parse yt-lockup-meta although available: " + getTitle(), e2);
                        }
                    }

                    public String getThumbnailUrl() throws ParsingException {
                        try {
                            Element te = li.select("span[class=\"yt-thumb-clip\"]").first().select("img").first();
                            String url = te.attr("abs:src");
                            if (url.contains(".gif")) {
                                return te.attr("abs:data-thumb");
                            }
                            return url;
                        } catch (Exception e) {
                            throw new ParsingException("Could not get thumbnail url", e);
                        }
                    }

                    private boolean isLiveStream(Element item) {
                        Element bla = item.select("span[class*=\"yt-badge-live\"]").first();
                        if ((bla != null || item.select("span[class*=\"video-time\"]").first() != null) && bla == null) {
                            return false;
                        }
                        return true;
                    }
                });
            }
        }
        return collector;
    }

    public String getFeedUrl() throws ParsingException {
        try {
            if (userUrl.contains("channel")) {
                return "";
            }
            if (!this.isAjaxPage) {
                feedUrl = this.doc.select("link[title=\"RSS\"]").first().attr("abs:href");
            }
            return feedUrl;
        } catch (Exception e) {
            throw new ParsingException("Could not get feed url", e);
        }
    }

    public boolean hasNextPage() throws ParsingException {
        return !nextPageUrl.isEmpty();
    }

    private String getUserUrl(Document d) throws ParsingException {
        return d.select("span[class=\"qualified-channel-title-text\"]").first().select("a").first().attr("abs:href");
    }

    private boolean isUserUrl(String url) throws ParsingException {
        return url.contains("/user/");
    }

    private String getNextPageUrl(Document d) throws ParsingException {
        try {
            Element button = d.select("button[class*=\"yt-uix-load-more\"]").first();
            if (button != null) {
                return button.attr("abs:data-uix-load-more-href");
            }
            return "";
        } catch (Exception e) {
            throw new ParsingException("could not load next page url", e);
        }
    }
}
*/


package full.movie.tubem.player.extractor.services.youtube;

import java.io.IOException;
import java.util.Iterator;

import full.movie.tubem.player.extractor.AbstractStreamInfo;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemExtractor;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemCollector;
import full.movie.tubem.player.extractor.Parser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import full.movie.tubem.player.extractor.Downloader;
import org.json.JSONException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.UrlIdHandler;
import org.jsoup.nodes.Document;
import full.movie.tubem.player.extractor.channel.ChannelExtractor;

public class YoutubeChannelExtractor extends ChannelExtractor
{
    private static final String TAG;
    private static String avatarUrl;
    private static String bannerUrl;
    private static String channelName;
    private static String feedUrl;
    private static String nextPageUrl;
    private static String userUrl;
    private Document doc;
    private boolean isAjaxPage;

    static {
        TAG = YoutubeChannelExtractor.class.toString();
        YoutubeChannelExtractor.userUrl = "";
        YoutubeChannelExtractor.channelName = "";
        YoutubeChannelExtractor.avatarUrl = "";
        YoutubeChannelExtractor.bannerUrl = "";
        YoutubeChannelExtractor.feedUrl = "";
        YoutubeChannelExtractor.nextPageUrl = "";
    }

    public YoutubeChannelExtractor(final UrlIdHandler urlIdHandler, String cleanUrl, final int n, final int n2) throws IOException, ExtractionException {
        super(urlIdHandler, cleanUrl, n, n2);
        this.doc = null;
        this.isAjaxPage = false;
        final Downloader downloader = Newapp.getDownloader();
        try {
            cleanUrl = urlIdHandler.cleanUrl(cleanUrl);
        } catch (ParsingException e) {
            e.printStackTrace();
        }
        if (n == 0) {
            if (this.isUserUrl(cleanUrl)) {
                YoutubeChannelExtractor.userUrl = cleanUrl;
            }
            else {
                try {
                    YoutubeChannelExtractor.userUrl = this.getUserUrl(Jsoup.parse(downloader.download(cleanUrl), cleanUrl));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ReCaptchaException e) {
                    e.printStackTrace();
                }
            }
            YoutubeChannelExtractor.userUrl += "/videos?veiw=0&flow=list&sort=dd&live_view=10000";
            try {
                this.doc = Jsoup.parse(downloader.download(YoutubeChannelExtractor.userUrl), YoutubeChannelExtractor.userUrl);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ReCaptchaException e) {
                e.printStackTrace();
            }
            YoutubeChannelExtractor.nextPageUrl = this.getNextPageUrl(this.doc);
            this.isAjaxPage = false;
        }
        else {
            //ToDo: Initialized as null.
            String download = null;
            try {
                download = downloader.download(YoutubeChannelExtractor.nextPageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ReCaptchaException e) {
                e.printStackTrace();
            }
            try {
                final JSONObject jsonObject = new JSONObject(download);
                final String string = jsonObject.getString("content_html");
                try {
                    this.doc = Jsoup.parse(string, YoutubeChannelExtractor.nextPageUrl);
                    final String string2 = jsonObject.getString("load_more_widget_html");
                    Label_0321: {
                        if (string2.isEmpty()) {
                            break Label_0321;
                        }
                        final String nextPageUrl = this.getNextPageUrl(Jsoup.parse(string2, YoutubeChannelExtractor.nextPageUrl));
                        YoutubeChannelExtractor.nextPageUrl = nextPageUrl;
                        this.isAjaxPage = true;

                        YoutubeChannelExtractor.nextPageUrl = "";
                        return;
                    }
                }
                catch (JSONException ex3) {}
            }
            catch (JSONException ex4) {}
        }
    }

    private String getNextPageUrl(final Document document) {
        final String s = "button[class*=\"yt-uix-load-more\"]";
        try {
            final Elements select = document.select(s);
            try {
                final Element first = select.first();
                String attr;
                if (first != null) {
                    attr = first.attr("abs:data-uix-load-more-href");
                }
                else {
                    attr = "";
                }
                return attr;
            }
            catch (Exception ex) {
                throw new ParsingException("could not load next page url", ex);
            }
        }
        catch (Exception ex2) {}
        return s;
    }

    private String getUserUrl(final Document document) {
        return document.select("span[class=\"qualified-channel-title-text\"]").first().select("a").first().attr("abs:href");
    }

    private boolean isUserUrl(final String s) {
        return s.contains("/user/");
    }

    @Override
    public String getAvatarUrl() {
        try {
            Label_0041: {
                if (this.isAjaxPage) {
                    break Label_0041;
                }
                final Elements select = this.doc.select("img[class=\"channel-header-profile-image\"]");
                try {
                    final String attr = select.first().attr("abs:src");
                    try {
                        YoutubeChannelExtractor.avatarUrl = attr;
                        return YoutubeChannelExtractor.avatarUrl;
                    }
                    catch (Exception ex) {
                        throw new ParsingException("Could not get avatar", ex);
                    }
                }
                catch (Exception ex2) {}
            }
        }
        catch (Exception ex3) {}
        return null;
    }

    @Override
    public String getBannerUrl() {
        try {
            Label_0125: {
                if (this.isAjaxPage) {
                    break Label_0125;
                }
                final Elements select = this.doc.select("div[id=\"gh-banner\"]");
                try {
                    final Elements select2 = select.first().select("style");
                    try {
                        final Element first = select2.first();
                        try {
                            final String html = first.html();
                            try {
                                try {
                                    final StringBuilder append = new StringBuilder().append("https:").append(Parser.matchGroup1("url\\(([^)]+)\\)", html));
                                    try {
                                        final String string = append.toString();
                                        if (string.contains("s.ytimg.com") || string.contains("default_banner")) {
                                            YoutubeChannelExtractor.bannerUrl = null;
                                        }
                                        else {
                                            YoutubeChannelExtractor.bannerUrl = string;
                                        }
                                        return YoutubeChannelExtractor.bannerUrl;
                                    }
                                    catch (Exception ex) {
                                        throw new ParsingException("Could not get Banner", ex);
                                    }
                                }
                                catch (Exception ex2) {}
                            }
                            catch (Exception ex3) {}
                        }
                        catch (Exception ex4) {}
                    }
                    catch (Exception ex5) {}
                }
                catch (Exception ex6) {}
            }
        }
        catch (Exception ex7) {}
        return null;
    }

    @Override
    public String getChannelName() {
        try {
            Label_0051: {
                if (this.isAjaxPage) {
                    break Label_0051;
                }
                final Elements select = this.doc.select("span[class=\"qualified-channel-title-text\"]");
                try {
                    final Elements select2 = select.first().select("a");
                    try {
                        final Element first = select2.first();
                        try {
                            final String text = first.text();
                            try {
                                YoutubeChannelExtractor.channelName = text;
                                return YoutubeChannelExtractor.channelName;
                            }
                            catch (Exception ex) {
                                throw new ParsingException("Could not get channel name");
                            }
                        }
                        catch (Exception ex2) {}
                    }
                    catch (Exception ex3) {}
                }
                catch (Exception ex4) {}
            }
        }
        catch (Exception ex5) {}
        return null;
    }

    @Override
    public String getFeedUrl() {
        try {
            String feedUrl = null;
            if (YoutubeChannelExtractor.userUrl.contains("channel")) {
                feedUrl = "";
            }
            else {
                Label_0063: {
                    if (this.isAjaxPage) {
                        break Label_0063;
                    }
                    final Elements select = this.doc.select("link[title=\"RSS\"]");
                    try {
                        final String attr = select.first().attr("abs:href");
                        try {
                            YoutubeChannelExtractor.feedUrl = attr;
                            feedUrl = YoutubeChannelExtractor.feedUrl;
                        }
                        catch (Exception ex) {
                            throw new ParsingException("Could not get feed url", ex);
                        }
                    }
                    catch (Exception ex2) {}
                }
            }
            return feedUrl;
        }
        catch (Exception ex3) {}
        return null;
    }

    @Override
    /*public StreamInfoItemCollector getStreams() {
        final StreamInfoItemCollector streamPreviewInfoCollector = this.getStreamPreviewInfoCollector();
        Element element;
        if (this.isAjaxPage) {
            element = this.doc.select("body").first();
        }
        else {
            element = this.doc.select("ul[id=\"browse-items-primary\"]").first();
        }
        for (final Element element2 : element.children()) {
            if (element2.select("div[class=\"feed-item-dismissable\"]").first() != null) {
                streamPreviewInfoCollector.commit(new YoutubeChannelExtractor$1(this, element2));
            }
        }
        return streamPreviewInfoCollector;
    }*/
    public StreamInfoItemCollector getStreams() throws ParsingException {
        Element ul;
        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        if (this.isAjaxPage) {
            //ToDo: "body" instead of TtmlNode
            ul = this.doc.select("body").first();
        } else {
            ul = this.doc.select("ul[id=\"browse-items-primary\"]").first();
        }
        Iterator it = ul.children().iterator();
        while (it.hasNext()) {
            final Element li = (Element) it.next();
            if (li.select("div[class=\"feed-item-dismissable\"]").first() != null) {
                collector.commit(new StreamInfoItemExtractor() {
                    public AbstractStreamInfo.StreamType getStreamType() throws ParsingException {
                        return AbstractStreamInfo.StreamType.VIDEO_STREAM;
                    }

                    public String getWebPageUrl() throws ParsingException {
                        try {
                            return li.select("div[class=\"feed-item-dismissable\"]").first().select("h3").first().select("a").first().attr("abs:href");
                        } catch (Exception e) {
                            throw new ParsingException("Could not get web page url for the video", e);
                        }
                    }

                    public String getTitle() throws ParsingException {
                        try {
                            return li.select("div[class=\"feed-item-dismissable\"]").first().select("h3").first().select("a").first().text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get title", e);
                        }
                    }

                    public int getDuration() throws ParsingException {
                        try {
                            return YoutubeParsingHelper.parseDurationString(li.select("span[class=\"video-time\"]").first().text());
                        } catch (Exception e) {
                            if (isLiveStream(li)) {
                                return -1;
                            }
                            throw new ParsingException("Could not get Duration: " + getTitle(), e);
                        }
                    }

                    public String getUploader() throws ParsingException {
                        return YoutubeChannelExtractor.this.getChannelName();
                    }

                    public String getUploadDate() throws ParsingException {
                        try {
                            return li.select("div[class=\"yt-lockup-meta\"]").first().select("li").first().text();
                        } catch (Exception e) {
                            throw new ParsingException("Could not get uplaod date", e);
                        }
                    }

                    public long getViewCount() throws ParsingException {
                        try {
                            String input = ((Element) li.select("div[class=\"yt-lockup-meta\"]").first().select("li").get(1)).text();
                            try {
                                return Long.parseLong(Parser.matchGroup1("([0-9,\\. ]*)", input).replace(" ", "").replace(".", "").replace(",", ""));
                            } catch (NumberFormatException e) {
                                if (!input.isEmpty()) {
                                    return 0;
                                }
                                throw new ParsingException("Could not handle input: " + input, e);
                            }
                        } catch (IndexOutOfBoundsException e2) {
                            if (isLiveStream(li)) {
                                return -1;
                            }
                            throw new ParsingException("Could not parse yt-lockup-meta although available: " + getTitle(), e2);
                        }
                    }

                    public String getThumbnailUrl() throws ParsingException {
                        try {
                            Element te = li.select("span[class=\"yt-thumb-clip\"]").first().select("img").first();
                            String url = te.attr("abs:src");
                            if (url.contains(".gif")) {
                                return te.attr("abs:data-thumb");
                            }
                            return url;
                        } catch (Exception e) {
                            throw new ParsingException("Could not get thumbnail url", e);
                        }
                    }

                    private boolean isLiveStream(Element item) {
                        Element bla = item.select("span[class*=\"yt-badge-live\"]").first();
                        if ((bla != null || item.select("span[class*=\"video-time\"]").first() != null) && bla == null) {
                            return false;
                        }
                        return true;
                    }
                });
            }
        }
        return collector;
    }

    @Override
    public boolean hasNextPage() {
        return !YoutubeChannelExtractor.nextPageUrl.isEmpty();
    }
}
