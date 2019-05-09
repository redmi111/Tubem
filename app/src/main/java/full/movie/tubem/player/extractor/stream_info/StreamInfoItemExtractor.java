package full.movie.tubem.player.extractor.stream_info;

import full.movie.tubem.player.extractor.AbstractStreamInfo.StreamType;
import full.movie.tubem.player.extractor.exceptions.ParsingException;

public interface StreamInfoItemExtractor {
    int getDuration() throws ParsingException;

    StreamType getStreamType() throws ParsingException;

    String getThumbnailUrl() throws ParsingException;

    String getTitle() throws ParsingException;

    String getUploadDate() throws ParsingException;

    String getUploader() throws ParsingException;

    long getViewCount() throws ParsingException;

    String getWebPageUrl() throws ParsingException;
}
