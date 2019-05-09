package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import java.io.IOException;
import java.util.List;

public abstract class SuggestionExtractor {
    private int serviceId;

    public abstract List<String> suggestionList(String str, String str2) throws ExtractionException, IOException;

    public SuggestionExtractor(int serviceId2) {
        this.serviceId = serviceId2;
    }

    public int getServiceId() {
        return this.serviceId;
    }
}
