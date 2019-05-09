package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.channel.ChannelExtractor;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.search.SearchEngine;
import full.movie.tubem.player.extractor.stream_info.StreamExtractor;
import java.io.IOException;

public abstract class StreamingService {
    private int serviceId;

    public enum LinkType {
        NONE,
        STREAM,
        CHANNEL,
        PLAYLIST
    }

    public class ServiceInfo {
        public String name = "";

        public ServiceInfo() {
        }
    }

    public abstract ChannelExtractor getChannelExtractorInstance(String str, int i) throws ExtractionException, IOException;

    public abstract UrlIdHandler getChannelUrlIdHandlerInstance();

    public abstract StreamExtractor getExtractorInstance(String str) throws IOException, ExtractionException;

    public abstract SearchEngine getSearchEngineInstance();

    public abstract ServiceInfo getServiceInfo();

    public abstract UrlIdHandler getStreamUrlIdHandlerInstance();

    public abstract SuggestionExtractor getSuggestionExtractorInstance();

    public StreamingService(int id) {
        this.serviceId = id;
    }

    public final int getServiceId() {
        return this.serviceId;
    }

    public final LinkType getLinkTypeByUrl(String url) {
        UrlIdHandler sH = getStreamUrlIdHandlerInstance();
        UrlIdHandler cH = getChannelUrlIdHandlerInstance();
        if (sH.acceptUrl(url)) {
            return LinkType.STREAM;
        }
        if (cH.acceptUrl(url)) {
            return LinkType.CHANNEL;
        }
        return LinkType.NONE;
    }
}
