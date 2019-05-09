/*
package full.movie.tubem.player.extractor.services.youtube;

import full.movie.tubem.player.extractor.Downloader;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.channel.ChannelInfoItemExtractor;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.search.InfoItemSearchCollector;
import full.movie.tubem.player.extractor.search.SearchEngine;
import full.movie.tubem.player.extractor.search.SearchEngine.Filter;
import full.movie.tubem.player.extractor.search.SearchEngine.NothingFoundException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemExtractor;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class YoutubeSearchEngine extends SearchEngine {
    public static final String CHARSET_UTF_8 = "UTF-8";
    private static final String TAG = YoutubeSearchEngine.class.toString();

    public YoutubeSearchEngine(UrlIdHandler urlIdHandler, int serviceId) {
        super(urlIdHandler, serviceId);
    }

    public InfoItemSearchCollector search(String query, int page, String languageCode, EnumSet<Filter> filter) throws IOException, ExtractionException {
        String site;
        InfoItemSearchCollector collector = getInfoItemSearchCollector();
        Downloader downloader = Newapp.getDownloader();
        String url = "https://www.youtube.com/results?q=" + URLEncoder.encode(query, "UTF-8") + "&page=" + Integer.toString(page + 1);
        if (filter.contains(Filter.STREAM) && !filter.contains(Filter.CHANNEL)) {
            url = url + "&sp=EgIQAQ%253D%253D";
        } else if (!filter.contains(Filter.STREAM) && filter.contains(Filter.CHANNEL)) {
            url = url + "&sp=EgIQAg%253D%253D";
        }
        if (!languageCode.isEmpty()) {
            site = downloader.download(url, languageCode);
        } else {
            site = downloader.download(url);
        }
        Element list = Jsoup.parse(site, url).select("ol[class=\"item-section\"]").first();
        Iterator it = list.children().iterator();
        while (it.hasNext()) {
            Element item = (Element) it.next();
            Element el = item.select("div[class*=\"spell-correction\"]").first();
            if (el != null) {
                collector.setSuggestion(el.select("a").first().text());
                if (list.children().size() == 1) {
                    throw new NothingFoundException("Did you mean: " + el.select("a").first().text());
                }
            } else {
                Element el2 = item.select("div[class*=\"search-message\"]").first();
                if (el2 != null) {
                    throw new NothingFoundException(el2.text());
                }
                Element el3 = item.select("div[class*=\"yt-lockup-video\"]").first();
                if (el3 != null) {
                    collector.commit((StreamInfoItemExtractor) new YoutubeStreamInfoItemExtractor(el3));
                } else {
                    Element el4 = item.select("div[class*=\"yt-lockup-channel\"]").first();
                    if (el4 != null) {
                        collector.commit((ChannelInfoItemExtractor) new YoutubeChannelInfoItemExtractor(el4));
                    }
                }
            }
        }
        return collector;
    }
}
*/

package full.movie.tubem.player.extractor.services.youtube;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import full.movie.tubem.player.extractor.Downloader;
import full.movie.tubem.player.extractor.channel.ChannelInfoItemExtractor;
import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemExtractor;
import full.movie.tubem.player.extractor.search.SearchEngine.NothingFoundException;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import full.movie.tubem.player.extractor.search.SearchEngine.Filter;
import java.net.URLEncoder;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.search.InfoItemSearchCollector;
import java.util.EnumSet;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.search.SearchEngine;

public class YoutubeSearchEngine extends SearchEngine
{
    public static final String CHARSET_UTF_8 = "UTF-8";
    private static final String TAG;

    static {
        TAG = YoutubeSearchEngine.class.toString();
    }

    public YoutubeSearchEngine(final UrlIdHandler urlIdHandler, final int n) {
        super(urlIdHandler, n);
    }

    @Override
    public InfoItemSearchCollector search(final String s, final int n, final String s2, final EnumSet set) {
        final InfoItemSearchCollector infoItemSearchCollector = this.getInfoItemSearchCollector();
        final Downloader downloader = Newapp.getDownloader();
        String s3 = null;
        try {
            s3 = "https://www.youtube.com/results?q=" + URLEncoder.encode(s, "UTF-8") + "&page=" + Integer.toString(n + 1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (set.contains(SearchEngine.Filter.STREAM) && !set.contains(SearchEngine.Filter.CHANNEL)) {
            s3 += "&sp=EgIQAQ%253D%253D";
        }
        else if (!set.contains(SearchEngine.Filter.STREAM) && set.contains(SearchEngine.Filter.CHANNEL)) {
            s3 += "&sp=EgIQAg%253D%253D";
        }
        //ToDo: Initialized s4 as null.
        String s4 = null;
        if (!s2.isEmpty()) {
            try {
                s4 = downloader.download(s3, s2);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ReCaptchaException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                s4 = downloader.download(s3);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ReCaptchaException e) {
                e.printStackTrace();
            }
        }
        final Element first = Jsoup.parse(s4, s3).select("ol[class=\"item-section\"]").first();
        for (final Element element : first.children()) {
            final Element first2 = element.select("div[class*=\"spell-correction\"]").first();
            if (first2 != null) {
                infoItemSearchCollector.setSuggestion(first2.select("a").first().text());
                if (first.children().size() == 1) {
                    try {
                        throw new NothingFoundException("Did you mean: " + first2.select("a").first().text());
                    } catch (NothingFoundException e) {
                        e.printStackTrace();
                    }
                }
                continue;
            }
            else {
                final Element first3 = element.select("div[class*=\"search-message\"]").first();
                if (first3 != null) {
                    try {
                        throw new NothingFoundException(first3.text());
                    } catch (NothingFoundException e) {
                        e.printStackTrace();
                    }
                }
                final Element first4 = element.select("div[class*=\"yt-lockup-video\"]").first();
                if (first4 != null) {
                    infoItemSearchCollector.commit(new YoutubeStreamInfoItemExtractor(first4));
                }
                else {
                    final Element first5 = element.select("div[class*=\"yt-lockup-channel\"]").first();
                    if (first5 == null) {
                        continue;
                    }
                    infoItemSearchCollector.commit(new YoutubeChannelInfoItemExtractor(first5));
                }
            }
        }
        return infoItemSearchCollector;
    }
}
