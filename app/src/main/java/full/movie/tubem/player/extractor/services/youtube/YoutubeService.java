package full.movie.tubem.player.extractor.services.youtube;

import full.movie.tubem.player.extractor.StreamingService;
import full.movie.tubem.player.extractor.StreamingService.ServiceInfo;
import full.movie.tubem.player.extractor.SuggestionExtractor;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.channel.ChannelExtractor;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.search.SearchEngine;
import full.movie.tubem.player.extractor.stream_info.StreamExtractor;
import full.movie.tubem.player.search_fragment.SearchWorker;

import java.io.IOException;

public class YoutubeService extends StreamingService {
    public YoutubeService(int id) {
        super(id);
    }

    public ServiceInfo getServiceInfo() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.name = SearchWorker.SearchRunnable.YOUTUBE;
        return serviceInfo;
    }

    public StreamExtractor getExtractorInstance(String url) throws ExtractionException, IOException {
        UrlIdHandler urlIdHandler = YoutubeStreamUrlIdHandler.getInstance();
        if (urlIdHandler.acceptUrl(url)) {
            return new YoutubeStreamExtractor(urlIdHandler, url, getServiceId());
        }
        throw new IllegalArgumentException("supplied String is not a valid Youtube URL");
    }

    public SearchEngine getSearchEngineInstance() {
        return new YoutubeSearchEngine(getStreamUrlIdHandlerInstance(), getServiceId());
    }

    public UrlIdHandler getStreamUrlIdHandlerInstance() {
        return YoutubeStreamUrlIdHandler.getInstance();
    }

    public UrlIdHandler getChannelUrlIdHandlerInstance() {
        return new YoutubeChannelUrlIdHandler();
    }

    public ChannelExtractor getChannelExtractorInstance(String url, int page) throws ExtractionException, IOException {
        return new YoutubeChannelExtractor(getChannelUrlIdHandlerInstance(), url, page, getServiceId());
    }

    public SuggestionExtractor getSuggestionExtractorInstance() {
        return new YoutubeSuggestionExtractor(getServiceId());
    }
}
