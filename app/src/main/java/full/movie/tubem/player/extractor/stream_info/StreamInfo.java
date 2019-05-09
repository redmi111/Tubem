package full.movie.tubem.player.extractor.stream_info;

import full.movie.tubem.player.extractor.AbstractStreamInfo;
import full.movie.tubem.player.extractor.AbstractStreamInfo.StreamType;
import full.movie.tubem.player.extractor.DashMpdParser;
import full.movie.tubem.player.extractor.InfoItem;
import full.movie.tubem.player.extractor.UrlIdHandler;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class StreamInfo extends AbstractStreamInfo {
    public int age_limit = -1;
    public List<AudioStream> audio_streams = null;
    public String average_rating = "";
    public String channel_url = "";
    public String dashMpdUrl = "";
    public String description = "";
    public int dislike_count = -1;
    public int duration = -1;
    public List<Throwable> errors = new Vector();
    public int like_count = -1;
    public StreamInfoItem next_video = null;
    public List<InfoItem> related_streams = null;
    public int start_position = 0;
    public String uploader_thumbnail_url = "";
    public List<VideoStream> video_only_streams = null;
    public List<VideoStream> video_streams = null;

    public static class StreamExctractException extends ExtractionException {
        StreamExctractException(String message) {
            super(message);
        }
    }

    public StreamInfo() {
    }

    public StreamInfo(AbstractStreamInfo avi) {
        this.id = avi.id;
        this.title = avi.title;
        this.uploader = avi.uploader;
        this.thumbnail_url = avi.thumbnail_url;
        this.webpage_url = avi.webpage_url;
        this.upload_date = avi.upload_date;
        this.upload_date = avi.upload_date;
        this.view_count = avi.view_count;
        if (avi instanceof StreamInfoItem) {
            this.duration = ((StreamInfoItem) avi).duration;
        }
    }

    public void addException(Exception e) {
        this.errors.add(e);
    }

    public static StreamInfo getVideoInfo(StreamExtractor extractor) throws ExtractionException, IOException {
        return extractOptionalData(extractStreams(extractImportantData(new StreamInfo(), extractor), extractor), extractor);
    }

    private static StreamInfo extractImportantData(StreamInfo streamInfo, StreamExtractor extractor) throws ExtractionException, IOException {
        UrlIdHandler uiconv = extractor.getUrlIdHandler();
        streamInfo.service_id = extractor.getServiceId();
        streamInfo.webpage_url = extractor.getPageUrl();
        streamInfo.stream_type = extractor.getStreamType();
        streamInfo.id = uiconv.getId(extractor.getPageUrl());
        streamInfo.title = extractor.getTitle();
        streamInfo.age_limit = extractor.getAgeLimit();
        if (streamInfo.stream_type != StreamType.NONE && streamInfo.webpage_url != null && !streamInfo.webpage_url.isEmpty() && streamInfo.id != null && !streamInfo.id.isEmpty() && streamInfo.title != null && streamInfo.age_limit != -1) {
            return streamInfo;
        }
        throw new ExtractionException("Some importand stream information was not given.");
    }

    private static StreamInfo extractStreams(StreamInfo streamInfo, StreamExtractor extractor) throws ExtractionException, IOException {
        try {
            streamInfo.dashMpdUrl = extractor.getDashMpdUrl();
        } catch (Exception e) {
            streamInfo.addException(new ExtractionException("Couldn't get Dash manifest", e));
        }
        try {
            streamInfo.audio_streams = extractor.getAudioStreams();
        } catch (Exception e2) {
            streamInfo.addException(new ExtractionException("Couldn't get audio streams", e2));
        }
        if (streamInfo.dashMpdUrl != null && !streamInfo.dashMpdUrl.isEmpty()) {
            if (streamInfo.audio_streams == null) {
                streamInfo.audio_streams = new Vector();
            }
            try {
                streamInfo.audio_streams.addAll(DashMpdParser.getAudioStreams(streamInfo.dashMpdUrl));
            } catch (Exception e3) {
                streamInfo.addException(new ExtractionException("Couldn't get audio streams from dash mpd", e3));
            }
        }
        try {
            streamInfo.video_streams = extractor.getVideoStreams();
        } catch (Exception e4) {
            streamInfo.addException(new ExtractionException("Couldn't get video streams", e4));
        }
        try {
            streamInfo.video_only_streams = extractor.getVideoOnlyStreams();
        } catch (Exception e5) {
            streamInfo.addException(new ExtractionException("Couldn't get video only streams", e5));
        }
        if ((streamInfo.video_streams != null && !streamInfo.video_streams.isEmpty()) || ((streamInfo.audio_streams != null && !streamInfo.audio_streams.isEmpty()) || (streamInfo.dashMpdUrl != null && !streamInfo.dashMpdUrl.isEmpty()))) {
            return streamInfo;
        }
        throw new StreamExctractException("Could not get any stream. See error variable to get further details.");
    }

    private static StreamInfo extractOptionalData(StreamInfo streamInfo, StreamExtractor extractor) {
        try {
            streamInfo.thumbnail_url = extractor.getThumbnailUrl();
        } catch (Exception e) {
            streamInfo.addException(e);
        }
        try {
            streamInfo.duration = extractor.getLength();
        } catch (Exception e2) {
            streamInfo.addException(e2);
        }
        try {
            streamInfo.uploader = extractor.getUploader();
        } catch (Exception e3) {
            streamInfo.addException(e3);
        }
        try {
            streamInfo.channel_url = extractor.getChannelUrl();
        } catch (Exception e4) {
            streamInfo.addException(e4);
        }
        try {
            streamInfo.description = extractor.getDescription();
        } catch (Exception e5) {
            streamInfo.addException(e5);
        }
        try {
            streamInfo.view_count = extractor.getViewCount();
        } catch (Exception e6) {
            streamInfo.addException(e6);
        }
        try {
            streamInfo.upload_date = extractor.getUploadDate();
        } catch (Exception e7) {
            streamInfo.addException(e7);
        }
        try {
            streamInfo.uploader_thumbnail_url = extractor.getUploaderThumbnailUrl();
        } catch (Exception e8) {
            streamInfo.addException(e8);
        }
        try {
            streamInfo.start_position = extractor.getTimeStamp();
        } catch (Exception e9) {
            streamInfo.addException(e9);
        }
        try {
            streamInfo.average_rating = extractor.getAverageRating();
        } catch (Exception e10) {
            streamInfo.addException(e10);
        }
        try {
            streamInfo.like_count = extractor.getLikeCount();
        } catch (Exception e11) {
            streamInfo.addException(e11);
        }
        try {
            streamInfo.dislike_count = extractor.getDislikeCount();
        } catch (Exception e12) {
            streamInfo.addException(e12);
        }
        try {
            if (streamInfo.next_video != null) {
                StreamInfoItemCollector c = new StreamInfoItemCollector(extractor.getUrlIdHandler(), extractor.getServiceId());
                c.commit(extractor.getNextVideo());
                if (c.getItemList().size() != 0) {
                    streamInfo.next_video = (StreamInfoItem) c.getItemList().get(0);
                }
                streamInfo.errors.addAll(c.getErrors());
            }
        } catch (Exception e13) {
            streamInfo.addException(e13);
        }
        try {
            StreamInfoItemCollector c2 = extractor.getRelatedVideos();
            streamInfo.related_streams = c2.getItemList();
            streamInfo.errors.addAll(c2.getErrors());
        } catch (Exception e14) {
            streamInfo.addException(e14);
        }
        return streamInfo;
    }
}
