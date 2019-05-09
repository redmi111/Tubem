package full.movie.tubem.player.extractor.stream_info;

import full.movie.tubem.player.extractor.AbstractStreamInfo.StreamType;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import java.util.List;

public abstract class StreamExtractor {
    private StreamInfoItemCollector previewInfoCollector;
    private int serviceId;
    private String url;
    private UrlIdHandler urlIdHandler;

    public class ContentNotAvailableException extends ParsingException {
        public ContentNotAvailableException(String message) {
            super(message);
        }

        public ContentNotAvailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public class ExtractorInitException extends ExtractionException {
        public ExtractorInitException(String message) {
            super(message);
        }

        public ExtractorInitException(Throwable cause) {
            super(cause);
        }

        public ExtractorInitException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public abstract int getAgeLimit() throws ParsingException;

    public abstract List<AudioStream> getAudioStreams() throws ParsingException;

    public abstract String getAverageRating() throws ParsingException;

    public abstract String getChannelUrl() throws ParsingException;

    public abstract String getDashMpdUrl() throws ParsingException;

    public abstract String getDescription() throws ParsingException;

    public abstract int getDislikeCount() throws ParsingException;

    public abstract int getLength() throws ParsingException;

    public abstract int getLikeCount() throws ParsingException;

    public abstract StreamInfoItemExtractor getNextVideo() throws ParsingException;

    public abstract String getPageUrl();

    public abstract StreamInfoItemCollector getRelatedVideos() throws ParsingException;

    public abstract StreamType getStreamType() throws ParsingException;

    public abstract String getThumbnailUrl() throws ParsingException;

    public abstract int getTimeStamp() throws ParsingException;

    public abstract String getTitle() throws ParsingException;

    public abstract String getUploadDate() throws ParsingException;

    public abstract String getUploader() throws ParsingException;

    public abstract String getUploaderThumbnailUrl() throws ParsingException;

    public abstract List<VideoStream> getVideoOnlyStreams() throws ParsingException;

    public abstract List<VideoStream> getVideoStreams() throws ParsingException;

    public abstract long getViewCount() throws ParsingException;

    public StreamExtractor(UrlIdHandler urlIdHandler2, String url2, int serviceId2) {
        this.serviceId = serviceId2;
        this.urlIdHandler = urlIdHandler2;
        this.previewInfoCollector = new StreamInfoItemCollector(urlIdHandler2, serviceId2);
    }

    /* access modifiers changed from: protected */
    public StreamInfoItemCollector getStreamPreviewInfoCollector() {
        return this.previewInfoCollector;
    }

    public String getUrl() {
        return this.url;
    }

    public UrlIdHandler getUrlIdHandler() {
        return this.urlIdHandler;
    }

    public int getServiceId() {
        return this.serviceId;
    }
}
