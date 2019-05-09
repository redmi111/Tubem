package full.movie.tubem.player.extractor.search;

import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import java.io.IOException;
import java.util.EnumSet;

public abstract class SearchEngine {
    private InfoItemSearchCollector collector;

    public enum Filter {
        STREAM,
        CHANNEL,
        PLAY_LIST
    }

    public static class NothingFoundException extends ExtractionException {
        public NothingFoundException(String message) {
            super(message);
        }
    }

    public abstract InfoItemSearchCollector search(String str, int i, String str2, EnumSet<Filter> enumSet) throws ExtractionException, IOException;

    public SearchEngine(UrlIdHandler urlIdHandler, int serviceId) {
        this.collector = new InfoItemSearchCollector(urlIdHandler, serviceId);
    }

    /* access modifiers changed from: protected */
    public InfoItemSearchCollector getInfoItemSearchCollector() {
        return this.collector;
    }
}
