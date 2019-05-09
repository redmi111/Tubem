package full.movie.tubem.player.extractor.search;

import full.movie.tubem.player.extractor.InfoItemCollector;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.channel.ChannelInfoItemCollector;
import full.movie.tubem.player.extractor.channel.ChannelInfoItemExtractor;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.FoundAdException;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemCollector;
import full.movie.tubem.player.extractor.stream_info.StreamInfoItemExtractor;

public class InfoItemSearchCollector extends InfoItemCollector {
    private ChannelInfoItemCollector channelCollector;
    SearchResult result = new SearchResult();
    private StreamInfoItemCollector streamCollector;
    private String suggestion = "";

    InfoItemSearchCollector(UrlIdHandler handler, int serviceId) {
        super(serviceId);
        this.streamCollector = new StreamInfoItemCollector(handler, serviceId);
        this.channelCollector = new ChannelInfoItemCollector(serviceId);
    }

    public void setSuggestion(String suggestion2) {
        this.suggestion = suggestion2;
    }

    public SearchResult getSearchResult() throws ExtractionException {
        addFromCollector(this.channelCollector);
        addFromCollector(this.streamCollector);
        this.result.suggestion = this.suggestion;
        this.result.errors = getErrors();
        return this.result;
    }

    public void commit(StreamInfoItemExtractor extractor) {
        try {
            this.result.resultList.add(this.streamCollector.extract(extractor));
        } catch (FoundAdException e) {
            System.err.println("Found add");
        } catch (Exception e2) {
            addError(e2);
        }
    }

    public void commit(ChannelInfoItemExtractor extractor) {
        try {
            this.result.resultList.add(this.channelCollector.extract(extractor));
        } catch (FoundAdException e) {
            System.err.println("Found add");
        } catch (Exception e2) {
            addError(e2);
        }
    }
}
