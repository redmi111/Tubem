package full.movie.tubem.player.extractor.exceptions;

public class ParsingException extends ExtractionException {
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
