package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.exceptions.ParsingException;

public interface UrlIdHandler {
    boolean acceptUrl(String str);

    String cleanUrl(String str) throws ParsingException;

    String getId(String str) throws ParsingException;

    String getUrl(String str);
}
