package full.movie.tubem.player.extractor.stream_info;

import full.movie.tubem.player.extractor.InfoItemCollector;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.FoundAdException;
import full.movie.tubem.player.extractor.exceptions.ParsingException;

public class StreamInfoItemCollector extends InfoItemCollector {
    private UrlIdHandler urlIdHandler;

    public StreamInfoItemCollector(UrlIdHandler handler, int serviceId) {
        super(serviceId);
        this.urlIdHandler = handler;
    }

    private UrlIdHandler getUrlIdHandler() {
        return this.urlIdHandler;
    }

    public StreamInfoItem extract(StreamInfoItemExtractor extractor) throws Exception {
        StreamInfoItem resultItem = new StreamInfoItem();
        resultItem.service_id = getServiceId();
        resultItem.webpage_url = extractor.getWebPageUrl();
        if (getUrlIdHandler() == null) {
            throw new ParsingException("Error: UrlIdHandler not set");
        }
        if (!resultItem.webpage_url.isEmpty()) {
            resultItem.id = Newapp.getService(getServiceId()).getStreamUrlIdHandlerInstance().getId(resultItem.webpage_url);
        }
        resultItem.title = extractor.getTitle();
        resultItem.stream_type = extractor.getStreamType();
        try {
            resultItem.duration = extractor.getDuration();
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.uploader = extractor.getUploader();
        } catch (Exception e2) {
            addError(e2);
        }
        try {
            resultItem.upload_date = extractor.getUploadDate();
        } catch (Exception e3) {
            addError(e3);
        }
        try {
            resultItem.view_count = extractor.getViewCount();
        } catch (Exception e4) {
            addError(e4);
        }
        try {
            resultItem.thumbnail_url = extractor.getThumbnailUrl();
        } catch (Exception e5) {
            addError(e5);
        }
        return resultItem;
    }

    public void commit(StreamInfoItemExtractor extractor) throws ParsingException {
        try {
            addItem(extract(extractor));
        } catch (FoundAdException ae) {
            System.out.println("AD_WARNING: " + ae.getMessage());
        } catch (Exception e) {
            addError(e);
        }
    }
}
