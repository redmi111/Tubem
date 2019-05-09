package full.movie.tubem.player.extractor.services.youtube;

import android.support.annotation.NonNull;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.Parser;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.FoundAdException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

public class YoutubeStreamUrlIdHandler implements UrlIdHandler {
    private static final String ID_PATTERN = "([\\-a-zA-Z0-9_]{11})";
    private static final YoutubeStreamUrlIdHandler instance = new YoutubeStreamUrlIdHandler();

    private YoutubeStreamUrlIdHandler() {
    }

    public static YoutubeStreamUrlIdHandler getInstance() {
        return instance;
    }

    public String getUrl(String videoId) {
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    public String getId(String url) throws ParsingException, IllegalArgumentException {
        String id;
        if (url.isEmpty()) {
            throw new IllegalArgumentException("The url parameter should not be empty");
        }
        String lowercaseUrl = url.toLowerCase();
        if (lowercaseUrl.contains("youtube")) {
            if (url.contains("attribution_link")) {
                try {
                    id = Parser.matchGroup1("v=([\\-a-zA-Z0-9_]{11})", URLDecoder.decode(Parser.matchGroup1("u=(.[^&|$]*)", url), "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    throw new ParsingException("Could not parse attribution_link", uee);
                }
            } else if (lowercaseUrl.contains("youtube.com/shared?ci=")) {
                return getRealIdFromSharedLink(url);
            } else {
                if (url.contains("vnd.youtube")) {
                    id = Parser.matchGroup1(ID_PATTERN, url);
                } else if (url.contains("embed")) {
                    id = Parser.matchGroup1("embed/([\\-a-zA-Z0-9_]{11})", url);
                } else if (url.contains("googleads")) {
                    throw new FoundAdException("Error found add: " + url);
                } else {
                    id = Parser.matchGroup1("[?&]v=([\\-a-zA-Z0-9_]{11})", url);
                }
            }
        } else if (!lowercaseUrl.contains("youtu.be")) {
            throw new ParsingException("Error no suitable url: " + url);
        } else if (url.contains("v=")) {
            id = Parser.matchGroup1("v=([\\-a-zA-Z0-9_]{11})", url);
        } else {
            id = Parser.matchGroup1("[Yy][Oo][Uu][Tt][Uu]\\.[Bb][Ee]/([\\-a-zA-Z0-9_]{11})", url);
        }
        if (!id.isEmpty()) {
            return id;
        }
        throw new ParsingException("Error could not parse url: " + url);
    }

    @NonNull
    private String getRealIdFromSharedLink(String url) throws ParsingException {
        try {
            String sharedId = getSharedId(new URI(url));
            try {
                String realId = Parser.matchGroup1("rel=\"shortlink\" href=\"https://youtu.be/([\\-a-zA-Z0-9_]{11})", Newapp.getDownloader().download("https://www.youtube.com/shared?ci=" + sharedId));
                if (!sharedId.equals(realId)) {
                    return realId;
                }
                throw new ParsingException("Got same id for as shared id: " + sharedId);
            } catch (ReCaptchaException | IOException e) {
                throw new ParsingException("Unable to resolve shared link", e);
            }
        } catch (URISyntaxException e2) {
            throw new ParsingException("Invalid shared link", e2);
        }
    }

    @NonNull
    private String getSharedId(URI uri) throws ParsingException {
        if ("/shared".equals(uri.getPath())) {
            return Parser.matchGroup1("ci=([\\-a-zA-Z0-9_]{11})", uri.getQuery());
        }
        throw new ParsingException("Not a shared link: " + uri.toString() + " (path != " + uri.getPath() + ")");
    }

    public String cleanUrl(String complexUrl) throws ParsingException {
        return getUrl(getId(complexUrl));
    }

    public boolean acceptUrl(String videoUrl) {
        String videoUrl2 = videoUrl.toLowerCase();
        if (!videoUrl2.contains("youtube") && !videoUrl2.contains("youtu.be")) {
            return false;
        }
        try {
            getId(videoUrl2);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
