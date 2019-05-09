package full.movie.tubem.player.extractor.channel;

import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemCollector;
import java.io.IOException;

public abstract class ChannelExtractor {
    private int page = -1;
    private StreamInfoItemCollector previewInfoCollector;
    private int serviceId;
    private String url;
    private UrlIdHandler urlIdHandler;

    public abstract String getAvatarUrl() throws ParsingException;

    public abstract String getBannerUrl() throws ParsingException;

    public abstract String getChannelName() throws ParsingException;

    public abstract String getFeedUrl() throws ParsingException;

    public abstract StreamInfoItemCollector getStreams() throws ParsingException;

    public abstract boolean hasNextPage() throws ParsingException;

    public ChannelExtractor(UrlIdHandler urlIdHandler2, String url2, int page2, int serviceId2) throws ExtractionException, IOException {
        this.url = url2;
        this.page = page2;
        this.serviceId = serviceId2;
        this.urlIdHandler = urlIdHandler2;
        this.previewInfoCollector = new StreamInfoItemCollector(urlIdHandler2, serviceId2);
    }

    public String getUrl() {
        return this.url;
    }

    public UrlIdHandler getUrlIdHandler() {
        return this.urlIdHandler;
    }

    public StreamInfoItemCollector getStreamPreviewInfoCollector() {
        return this.previewInfoCollector;
    }

    public int getServiceId() {
        return this.serviceId;
    }
}
