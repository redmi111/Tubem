package full.movie.tubem.player.extractor;

public abstract class AbstractStreamInfo {
    public String id = "";
    public int service_id = -1;
    public StreamType stream_type;
    public String thumbnail_url = "";
    public String title = "";
    public String upload_date = "";
    public String uploader = "";
    public long view_count = -1;
    public String webpage_url = "";

    public enum StreamType {
        NONE,
        VIDEO_STREAM,
        AUDIO_STREAM,
        LIVE_STREAM,
        AUDIO_LIVE_STREAM,
        FILE
    }
}
