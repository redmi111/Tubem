package full.movie.tubem.player.extractor.stream_info;

public class AudioStream {
    public int bandwidth = -1;
    public int format = -1;
    public int sampling_rate = -1;
    public String url = "";

    public AudioStream(String url2, int format2, int bandwidth2, int samplingRate) {
        this.url = url2;
        this.format = format2;
        this.bandwidth = bandwidth2;
        this.sampling_rate = samplingRate;
    }

    public boolean equalStats(AudioStream cmp) {
        return this.format == cmp.format && this.bandwidth == cmp.bandwidth && this.sampling_rate == cmp.sampling_rate;
    }

    public boolean equals(AudioStream cmp) {
        return cmp != null && equalStats(cmp) && this.url == cmp.url;
    }
}
