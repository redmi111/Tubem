package full.movie.tubem.player.extractor;

import com.google.android.exoplayer.util.MimeTypes;

public enum MediaFormat {
    MPEG_4(0, "MPEG-4", "mp4", MimeTypes.VIDEO_MP4),
    v3GPP(1, "3GPP", "3gp", MimeTypes.VIDEO_H263),
    WEBM(2, "WebM", "webm", MimeTypes.VIDEO_WEBM),
    M4A(3, "m4a", "m4a", MimeTypes.AUDIO_MP4),
    WEBMA(4, "WebM", "webm", MimeTypes.AUDIO_WEBM);
    
    public final int id;
    public final String mimeType;
    public final String name;
    public final String suffix;

    private MediaFormat(int id2, String name2, String suffix2, String mimeType2) {
        this.id = id2;
        this.name = name2;
        this.suffix = suffix2;
        this.mimeType = mimeType2;
    }

    public static String getNameById(int ident) {
        MediaFormat[] values;
        for (MediaFormat vf : values()) {
            if (vf.id == ident) {
                return vf.name;
            }
        }
        return "";
    }

    public static String getSuffixById(int ident) {
        MediaFormat[] values;
        for (MediaFormat vf : values()) {
            if (vf.id == ident) {
                return vf.suffix;
            }
        }
        return "";
    }

    public static String getMimeById(int ident) {
        MediaFormat[] values;
        for (MediaFormat vf : values()) {
            if (vf.id == ident) {
                return vf.mimeType;
            }
        }
        return "";
    }
}
