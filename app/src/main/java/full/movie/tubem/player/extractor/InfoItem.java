package full.movie.tubem.player.extractor;

public interface InfoItem {

    public enum InfoType {
        STREAM,
        PLAYLIST,
        CHANNEL
    }

    String getLink();

    String getTitle();

    InfoType infoType();
}
