package full.movie.tubem.player.extractor.search;

import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.search.SearchEngine.Filter;
import full.movie.tubem.player.extractor.search.SearchEngine.NothingFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

public class SearchResult {
    public List<Throwable> errors = new Vector();
    public List<InfoItem> resultList = new Vector();
    public String suggestion = "";

    public static SearchResult getSearchResult(SearchEngine engine, String query, int page, String languageCode, EnumSet<Filter> filter) throws ExtractionException, IOException {
        SearchResult result = engine.search(query, page, languageCode, filter).getSearchResult();
        if (result.resultList.isEmpty()) {
            if (!result.suggestion.isEmpty()) {
                throw new NothingFoundException(result.suggestion);
            } else if (result.errors.isEmpty()) {
                throw new ExtractionException("Empty result despite no error");
            }
        }
        return result;
    }
}
