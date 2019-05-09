package full.movie.tubem.player;

import android.graphics.Bitmap;

public class ActivityCommunicator {
    private static ActivityCommunicator activityCommunicator;
    public volatile Bitmap backgroundPlayerThumbnail;
    public volatile Class returnActivity;

    public static ActivityCommunicator getCommunicator() {
        if (activityCommunicator == null) {
            activityCommunicator = new ActivityCommunicator();
        }
        return activityCommunicator;
    }
}
