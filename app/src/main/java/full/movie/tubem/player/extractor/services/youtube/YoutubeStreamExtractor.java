package full.movie.tubem.player.extractor.services.youtube;

import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import full.movie.tubem.player.extractor.AbstractStreamInfo.StreamType;
import full.movie.tubem.player.extractor.Downloader;
import full.movie.tubem.player.extractor.MediaFormat;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.Parser;
import full.movie.tubem.player.extractor.Parser.RegexException;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import full.movie.tubem.player.extractor.stream_info.AudioStream;
import full.movie.tubem.player.extractor.stream_info.StreamExtractor;
import full.movie.tubem.player.extractor.stream_info.StreamExtractor.ContentNotAvailableException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemCollector;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemExtractor;
import full.movie.tubem.player.extractor.stream_info.VideoStream;
import full.movie.tubem.player.player.BackgroundPlayer;
import info.guardianproject.netcipher.proxy.TorServiceUtils;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mozilla.classfile.ByteCode;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class YoutubeStreamExtractor extends StreamExtractor {
    public static final String CONTENT = "content";
    private static final String DECRYPTION_FUNC_NAME = "decrypt";
    private static final String EL_INFO = "el=info";
    private static final String GET_VIDEO_INFO_URL = "https://www.youtube.com/get_video_info?video_id=%%video_id%%$$el_type$$&ps=default&eurl=&gl=US&hl=en";
    public static final String HTTPS = "https:";
    public static final String REGEX_INT = "[^\\d]";
    private static final String TAG = YoutubeStreamExtractor.class.toString();
    public static final String URL_ENCODED_FMT_STREAM_MAP = "url_encoded_fmt_stream_map";
    private static volatile String decryptionCode = "";
    private static final ItagItem[] itagList = {new ItagItem(17, ItagType.VIDEO, MediaFormat.v3GPP, "144p", 12), new ItagItem(18, ItagType.VIDEO, MediaFormat.MPEG_4, "360p", 24), new ItagItem(22, ItagType.VIDEO, MediaFormat.MPEG_4, "720p", 24), new ItagItem(36, ItagType.VIDEO, MediaFormat.v3GPP, "240p", 24), new ItagItem(37, ItagType.VIDEO, MediaFormat.MPEG_4, "1080p", 24), new ItagItem(38, ItagType.VIDEO, MediaFormat.MPEG_4, "1080p", 24), new ItagItem(43, ItagType.VIDEO, MediaFormat.WEBM, "360p", 24), new ItagItem(44, ItagType.VIDEO, MediaFormat.WEBM, "480p", 24), new ItagItem(45, ItagType.VIDEO, MediaFormat.WEBM, "720p", 24), new ItagItem(46, ItagType.VIDEO, MediaFormat.WEBM, "1080p", 24), new ItagItem(249, ItagType.AUDIO, MediaFormat.WEBMA, 0, 0), new ItagItem((int) Callback.DEFAULT_SWIPE_ANIMATION_DURATION, ItagType.AUDIO, MediaFormat.WEBMA, 0, 0), new ItagItem((int) ByteCode.LOOKUPSWITCH, ItagType.AUDIO, MediaFormat.WEBMA, 0, 0), new ItagItem(140, ItagType.AUDIO, MediaFormat.M4A, 0, 0), new ItagItem(251, ItagType.AUDIO, MediaFormat.WEBMA, 0, 0), new ItagItem(160, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "144p", 24), new ItagItem(133, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "240p", 24), new ItagItem(134, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "360p", 24), new ItagItem(135, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "480p", 24), new ItagItem(136, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "720p", 24), new ItagItem(137, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "1080p", 24)};
    private final Document doc;
    private boolean isAgeRestricted;
    String pageUrl = "";
    private JSONObject playerArgs;
    UrlIdHandler urlidhandler = YoutubeStreamUrlIdHandler.getInstance();
    private Map<String, String> videoInfoPage;

    public class DecryptException extends ParsingException {
        DecryptException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public class GemaException extends ContentNotAvailableException {
        GemaException(String message) {
            super(message);
        }
    }

    private static class ItagItem {
        public int bandWidth = -1;
        public int fps = -1;
        public int id;
        public ItagType itagType;
        public int mediaFormatId;
        public String resolutionString;
        public int samplingRate = -1;

        public ItagItem(int id2, ItagType type, MediaFormat format, String res, int fps2) {
            this.id = id2;
            this.itagType = type;
            this.mediaFormatId = format.id;
            this.resolutionString = res;
            this.fps = fps2;
        }

        public ItagItem(int id2, ItagType type, MediaFormat format, int samplingRate2, int bandWidth2) {
            this.id = id2;
            this.itagType = type;
            this.mediaFormatId = format.id;
            this.samplingRate = samplingRate2;
            this.bandWidth = bandWidth2;
        }
    }

    public enum ItagType {
        AUDIO,
        VIDEO,
        VIDEO_ONLY
    }

    public class LiveStreamException extends ContentNotAvailableException {
        LiveStreamException(String message) {
            super(message);
        }
    }

    public static boolean itagIsSupported(int itag) {
        for (ItagItem item : itagList) {
            if (itag == item.id) {
                return true;
            }
        }
        return false;
    }

    public static ItagItem getItagItem(int itag) throws ParsingException {
        ItagItem[] itagItemArr;
        for (ItagItem item : itagList) {
            if (itag == item.id) {
                return item;
            }
        }
        throw new ParsingException("itag=" + Integer.toString(itag) + " not supported");
    }

    public YoutubeStreamExtractor(UrlIdHandler urlIdHandler, String pageUrl2, int serviceId) throws ExtractionException, IOException {
        super(urlIdHandler, pageUrl2, serviceId);
        String playerUrl;
        this.pageUrl = pageUrl2;
        Downloader downloader = Newapp.getDownloader();
        String pageContent = downloader.download(this.urlidhandler.cleanUrl(pageUrl2));
        this.doc = Jsoup.parse(pageContent, pageUrl2);
        if (pageContent.contains("<meta property=\"og:restrictions:age")) {
            this.videoInfoPage = Parser.compatParseMap(downloader.download(GET_VIDEO_INFO_URL.replace("%%video_id%%", this.urlidhandler.getId(pageUrl2)).replace("$$el_type$$", "&el=info")));
            playerUrl = getPlayerUrlFromRestrictedVideo(pageUrl2);
            this.isAgeRestricted = true;
        } else {
            JSONObject ytPlayerConfig = getPlayerConfig(pageContent);
            this.playerArgs = getPlayerArgs(ytPlayerConfig);
            playerUrl = getPlayerUrl(ytPlayerConfig);
            this.isAgeRestricted = false;
        }
        if (decryptionCode.isEmpty()) {
            decryptionCode = loadDecryptionCode(playerUrl);
        }
    }

    private JSONObject getPlayerConfig(String pageContent) throws ParsingException {
        try {
            return new JSONObject(Parser.matchGroup1("ytplayer.config\\s*=\\s*(\\{.*?\\});", pageContent));
        } catch (RegexException e) {
            String errorReason = findErrorReason(this.doc);
            char c = 65535;
            switch (errorReason.hashCode()) {
                case 0:
                    if (errorReason.equals("")) {
                        c = 1;
                        break;
                    }
                    break;
                case 2183922:
                    if (errorReason.equals("GEMA")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    throw new GemaException(errorReason);
                case 1:
                    throw new ContentNotAvailableException("Content not available: player config empty", e);
                default:
                    throw new ContentNotAvailableException("Content not available", e);
            }
        } catch (JSONException e2) {
            throw new ParsingException("Could not parse yt player config", e2);
        }
    }

    private JSONObject getPlayerArgs(JSONObject playerConfig) throws ParsingException {
        boolean isLiveStream = false;
        try {
            JSONObject playerArgs2 = playerConfig.getJSONObject("args");
            if ((playerArgs2.has(TorServiceUtils.SHELL_CMD_PS) && playerArgs2.get(TorServiceUtils.SHELL_CMD_PS).toString().equals("live")) || playerArgs2.get(URL_ENCODED_FMT_STREAM_MAP).toString().isEmpty()) {
                isLiveStream = true;
            }
            if (!isLiveStream) {
                return playerArgs2;
            }
            throw new LiveStreamException("This is a Life stream. Can't use those right now.");
        } catch (JSONException e) {
            throw new ParsingException("Could not parse yt player config", e);
        }
    }

    private String getPlayerUrl(JSONObject playerConfig) throws ParsingException {
        String str = "";
        try {
            String playerUrl = playerConfig.getJSONObject("assets").getString("js");
            if (playerUrl.startsWith("//")) {
                return HTTPS + playerUrl;
            }
            return playerUrl;
        } catch (JSONException e) {
            throw new ParsingException("Could not load decryption code for the Youtube service.", e);
        }
    }

    private String getPlayerUrlFromRestrictedVideo(String pageUrl2) throws ParsingException, ReCaptchaException {
        try {
            String playerUrl = "";
            Matcher patternMatcher = Pattern.compile("\"assets\":.+?\"js\":\\s*(\"[^\"]+\")").matcher(Newapp.getDownloader().download("https://www.youtube.com/embed/" + this.urlidhandler.getId(pageUrl2)));
            while (patternMatcher.find()) {
                playerUrl = patternMatcher.group(1);
            }
            String playerUrl2 = playerUrl.replace("\\", "").replace("\"", "");
            if (playerUrl2.startsWith("//")) {
                return HTTPS + playerUrl2;
            }
            return playerUrl2;
        } catch (IOException e) {
            throw new ParsingException("Could load decryption code form restricted video for the Youtube service.", e);
        } catch (ReCaptchaException e2) {
            throw new ReCaptchaException("reCaptcha Challenge requested");
        }
    }

    public String getTitle() throws ParsingException {
        try {
            if (this.playerArgs == null) {
                return (String) this.videoInfoPage.get(BackgroundPlayer.TITLE);
            }
            return this.playerArgs.getString(BackgroundPlayer.TITLE);
        } catch (JSONException je) {
            je.printStackTrace();
            System.err.println("failed to load title from JSON args; trying to extract it from HTML");
            try {
                return this.doc.select("meta[name=title]").attr(CONTENT);
            } catch (Exception e) {
                throw new ParsingException("failed permanently to load title.", e);
            }
        }
    }

    public String getDescription() throws ParsingException {
        try {
            return this.doc.select("p[id=\"eow-description\"]").first().html();
        } catch (Exception e) {
            throw new ParsingException("failed to load description.", e);
        }
    }

    public String getUploader() throws ParsingException {
        try {
            if (this.playerArgs == null) {
                return (String) this.videoInfoPage.get("author");
            }
            return this.playerArgs.getString("author");
        } catch (JSONException je) {
            je.printStackTrace();
            System.err.println("failed to load uploader name from JSON args; trying to extract it from HTML");
            try {
                return this.doc.select("div.yt-user-info").first().text();
            } catch (Exception e) {
                throw new ParsingException("failed permanently to load uploader name.", e);
            }
        }
    }

    public int getLength() throws ParsingException {
        try {
            if (this.playerArgs == null) {
                return Integer.valueOf((String) this.videoInfoPage.get("length_seconds")).intValue();
            }
            return this.playerArgs.getInt("length_seconds");
        } catch (JSONException e) {
            throw new ParsingException("failed to load video duration from JSON args", e);
        }
    }

    public long getViewCount() throws ParsingException {
        try {
            return Long.parseLong(this.doc.select("meta[itemprop=interactionCount]").attr(CONTENT));
        } catch (Exception e) {
            throw new ParsingException("failed to get number of views", e);
        }
    }

    public String getUploadDate() throws ParsingException {
        try {
            return this.doc.select("meta[itemprop=datePublished]").attr(CONTENT);
        } catch (Exception e) {
            throw new ParsingException("failed to get upload date.", e);
        }
    }

    public String getThumbnailUrl() throws ParsingException {
        try {
            return this.doc.select("link[itemprop=\"thumbnailUrl\"]").first().attr("abs:href");
        } catch (Exception e) {
            System.err.println("Could not find high res Thumbnail. Using low res instead");
            try {
                return this.playerArgs.getString("thumbnail_url");
            } catch (JSONException je) {
                throw new ParsingException("failed to extract thumbnail URL from JSON args; trying to extract it from HTML", je);
            } catch (NullPointerException e2) {
                return (String) this.videoInfoPage.get("thumbnail_url");
            }
        }
    }

    public String getUploaderThumbnailUrl() throws ParsingException {
        try {
            return this.doc.select("a[class*=\"yt-user-photo\"]").first().select("img").first().attr("abs:data-thumb");
        } catch (Exception e) {
            throw new ParsingException("failed to get uploader thumbnail URL.", e);
        }
    }

    public String getDashMpdUrl() throws ParsingException {
        return "";
    }

    public List<AudioStream> getAudioStreams() throws ParsingException {
        String encodedUrlMap;
        Vector<AudioStream> audioStreams = new Vector<>();
        try {
            if (this.playerArgs == null) {
                encodedUrlMap = (String) this.videoInfoPage.get("adaptive_fmts");
            } else {
                encodedUrlMap = this.playerArgs.getString("adaptive_fmts");
            }
            for (String url_data_str : encodedUrlMap.split(",")) {
                Map<String, String> tags = Parser.compatParseMap(org.jsoup.parser.Parser.unescapeEntities(url_data_str, true));
                int itag = Integer.parseInt((String) tags.get("itag"));
                if (itagIsSupported(itag)) {
                    ItagItem itagItem = getItagItem(itag);
                    if (itagItem.itagType == ItagType.AUDIO) {
                        String streamUrl = (String) tags.get("url");
                        if (tags.get("s") != null) {
                            streamUrl = streamUrl + "&signature=" + decryptSignature((String) tags.get("s"), decryptionCode);
                        }
                        audioStreams.add(new AudioStream(streamUrl, itagItem.mediaFormatId, itagItem.bandWidth, itagItem.samplingRate));
                    }
                }
            }
            return audioStreams;
        } catch (Exception e) {
            throw new ParsingException("Could not get audiostreams", e);
        }
    }

    public List<VideoStream> getVideoStreams() throws ParsingException {
        String encodedUrlMap;
        Vector<VideoStream> videoStreams = new Vector<>();
        try {
            if (this.playerArgs == null) {
                encodedUrlMap = (String) this.videoInfoPage.get(URL_ENCODED_FMT_STREAM_MAP);
            } else {
                encodedUrlMap = this.playerArgs.getString(URL_ENCODED_FMT_STREAM_MAP);
            }
            for (String url_data_str : encodedUrlMap.split(",")) {
                try {
                    Map<String, String> tags = Parser.compatParseMap(org.jsoup.parser.Parser.unescapeEntities(url_data_str, true));
                    int itag = Integer.parseInt((String) tags.get("itag"));
                    if (itagIsSupported(itag)) {
                        ItagItem itagItem = getItagItem(itag);
                        if (itagItem.itagType == ItagType.VIDEO) {
                            String streamUrl = (String) tags.get("url");
                            if (tags.get("s") != null) {
                                streamUrl = streamUrl + "&signature=" + decryptSignature((String) tags.get("s"), decryptionCode);
                            }
                            videoStreams.add(new VideoStream(streamUrl, itagItem.mediaFormatId, itagItem.resolutionString));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Could not get Video stream.");
                    e.printStackTrace();
                }
            }
            if (!videoStreams.isEmpty()) {
                return videoStreams;
            }
            throw new ParsingException("Failed to get any video stream");
        } catch (Exception e2) {
            throw new ParsingException("Failed to get video streams", e2);
        }
    }

    public List<VideoStream> getVideoOnlyStreams() throws ParsingException {
        return null;
    }

    public int getTimeStamp() throws ParsingException {
        //ToDo: Initialized hoursString as null
        String hoursString = null;
        int hours = 0;
        try {
            String timeStamp = Parser.matchGroup1("((#|&|\\?)t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)", this.pageUrl);
            if (timeStamp.isEmpty()) {
                return 0;
            }
            String secondsString = "";
            String minutesString = "";
            String str = "";
            try {
                secondsString = Parser.matchGroup1("(\\d{1,3})s", timeStamp);
                minutesString = Parser.matchGroup1("(\\d{1,3})m", timeStamp);
                hoursString = Parser.matchGroup1("(\\d{1,3})h", timeStamp);
            } catch (Exception e) {
                if (secondsString.isEmpty() && minutesString.isEmpty() && hoursString.isEmpty()) {
                    secondsString = Parser.matchGroup1("t=(\\d+)", timeStamp);
                }
            }
            int seconds = secondsString.isEmpty() ? 0 : Integer.parseInt(secondsString);
            int minutes = minutesString.isEmpty() ? 0 : Integer.parseInt(minutesString);
            if (!hoursString.isEmpty()) {
                hours = Integer.parseInt(hoursString);
            }
            return (minutes * 60) + seconds + (hours * 3600);
        } catch (RegexException e3) {
            return -2;
        }
    }

    public int getAgeLimit() throws ParsingException {
        if (!this.isAgeRestricted) {
            return 0;
        }
        try {
            return Integer.valueOf(this.doc.head().getElementsByAttributeValue("property", "og:restrictions:age").attr(CONTENT).replace("+", "")).intValue();
        } catch (Exception e) {
            throw new ParsingException("Could not get age restriction");
        }
    }

    public String getAverageRating() throws ParsingException {
        try {
            if (this.playerArgs == null) {
                return (String) this.videoInfoPage.get("avg_rating");
            }
            return this.playerArgs.getString("avg_rating");
        } catch (JSONException e) {
            throw new ParsingException("Could not get Average rating", e);
        }
    }

    public int getLikeCount() throws ParsingException {
        String likesString = "";
        try {
            try {
                return Integer.parseInt(this.doc.select("button.like-button-renderer-like-button").first().select("span.yt-uix-button-content").first().text().replaceAll(REGEX_INT, ""));
            } catch (NullPointerException e) {
                return -1;
            }
        } catch (NumberFormatException nfe) {
            throw new ParsingException("failed to parse likesString \"" + likesString + "\" as integers", nfe);
        } catch (Exception e2) {
            throw new ParsingException("Could not get like count", e2);
        }
    }

    public int getDislikeCount() throws ParsingException {
        String dislikesString = "";
        try {
            try {
                return Integer.parseInt(this.doc.select("button.like-button-renderer-dislike-button").first().select("span.yt-uix-button-content").first().text().replaceAll(REGEX_INT, ""));
            } catch (NullPointerException e) {
                return -1;
            }
        } catch (NumberFormatException nfe) {
            throw new ParsingException("failed to parse dislikesString \"" + dislikesString + "\" as integers", nfe);
        } catch (Exception e2) {
            throw new ParsingException("Could not get dislike count", e2);
        }
    }

    public StreamInfoItemExtractor getNextVideo() throws ParsingException {
        try {
            return extractVideoPreviewInfo(this.doc.select("div[class=\"watch-sidebar-section\"]").first().select("li").first());
        } catch (Exception e) {
            throw new ParsingException("Could not get next video", e);
        }
    }

    public StreamInfoItemCollector getRelatedVideos() throws ParsingException {
        try {
            StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
            Element ul = this.doc.select("ul[id=\"watch-related\"]").first();
            if (ul != null) {
                Iterator it = ul.children().iterator();
                while (it.hasNext()) {
                    Element li = (Element) it.next();
                    if (li.select("a[class*=\"content-link\"]").first() != null) {
                        collector.commit(extractVideoPreviewInfo(li));
                    }
                }
            }
            return collector;
        } catch (Exception e) {
            throw new ParsingException("Could not get related videos", e);
        }
    }

    public String getPageUrl() {
        return this.pageUrl;
    }

    public String getChannelUrl() throws ParsingException {
        try {
            return this.doc.select("div[class=\"yt-user-info\"]").first().children().select("a").first().attr("abs:href");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel link", e);
        }
    }

    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    private StreamInfoItemExtractor extractVideoPreviewInfo(final Element li) {
        return new StreamInfoItemExtractor() {
            public StreamType getStreamType() throws ParsingException {
                return null;
            }

            public String getWebPageUrl() throws ParsingException {
                return li.select("a.content-link").first().attr("abs:href");
            }

            public String getTitle() throws ParsingException {
                return li.select("span.title").first().text();
            }

            public int getDuration() throws ParsingException {
                return YoutubeParsingHelper.parseDurationString(li.select("span.video-time").first().text());
            }

            public String getUploader() throws ParsingException {
                return li.select("span.g-hovercard").first().text();
            }

            public String getUploadDate() throws ParsingException {
                return null;
            }

            public long getViewCount() throws ParsingException {
                try {
                    return Long.parseLong(li.select("span.view-count").first().text().replaceAll(YoutubeStreamExtractor.REGEX_INT, ""));
                } catch (Exception e) {
                    return 0;
                }
            }

            public String getThumbnailUrl() throws ParsingException {
                Element img = li.select("img").first();
                String thumbnailUrl = img.attr("abs:src");
                if (thumbnailUrl.contains(".gif")) {
                    thumbnailUrl = img.attr("data-thumb");
                }
                if (thumbnailUrl.startsWith("//")) {
                    return YoutubeStreamExtractor.HTTPS + thumbnailUrl;
                }
                return thumbnailUrl;
            }
        };
    }

    private String loadDecryptionCode(String playerUrl) throws DecryptException {
        String callerFunc = "function decrypt(a){return %%(a);}";
        try {
            Downloader downloader = Newapp.getDownloader();
            if (!playerUrl.contains("https://youtube.com")) {
                playerUrl = "https://youtube.com" + playerUrl;
            }
            String playerCode = downloader.download(playerUrl);
            String decryptionFuncName = Parser.matchGroup("(\\w+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;", playerCode, 1);
            String decryptionFunc = "var " + Parser.matchGroup1("(" + decryptionFuncName.replace("$", "\\$") + "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})", playerCode) + ";";
            String helperObject = Parser.matchGroup1("(var " + Parser.matchGroup1(";([A-Za-z0-9_\\$]{2})\\...\\(", decryptionFunc).replace("$", "\\$") + "=\\{.+?\\}\\};)", playerCode);
            return helperObject + decryptionFunc + callerFunc.replace("%%", decryptionFuncName);
        } catch (IOException ioe) {
            throw new DecryptException("Could not load decrypt function", ioe);
        } catch (Exception e) {
            throw new DecryptException("Could not parse decrypt function ", e);
        }
    }

    private String decryptSignature(String encryptedSig, String decryptionCode2) throws DecryptException {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        try {
            ScriptableObject scope = context.initStandardObjects();
            context.evaluateString(scope, decryptionCode2, "decryptionCode", 1, null);
            Object result = ((Function) scope.get(DECRYPTION_FUNC_NAME, (Scriptable) scope)).call(context, scope, scope, new Object[]{encryptedSig});
            Context.exit();
            if (result == null) {
                return "";
            }
            return result.toString();
        } catch (Exception e) {
            throw new DecryptException("could not get decrypt signature", e);
        } catch (Throwable th) {
            Context.exit();
            throw th;
        }
    }

    private String findErrorReason(Document doc2) {
        if (doc2.select("h1[id=\"unavailable-message\"]").first().text().contains("GEMA")) {
            return "GEMA";
        }
        return "";
    }
}
