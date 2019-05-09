package full.movie.tubem.player.extractor.stream_info;

public class VideoStream {
    public int format = -1;
    public String resolution = "";
    public String url = "";

    public VideoStream(String url2, int format2, String res) {
        this.url = url2;
        this.format = format2;
        this.resolution = res;
    }

    public boolean equalStats(VideoStream cmp) {
        return this.format == cmp.format && this.resolution == cmp.resolution;
    }

    public boolean equals(VideoStream cmp) {
        return cmp != null && equalStats(cmp) && this.url == cmp.url;
    }
}
