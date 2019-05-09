package full.movie.tubem.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;

public class ImageErrorLoadingListener implements ImageLoadingListener {
    private Activity activity = null;
    private View rootView = null;
    private int serviceId = -1;

    public ImageErrorLoadingListener(Activity activity2, View rootView2, int serviceId2) {
        this.activity = activity2;
        this.serviceId = serviceId2;
        this.rootView = rootView2;
    }

    public void onLoadingStarted(String imageUri, View view) {
    }

    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        ErrorActivity.reportError((Context) this.activity, failReason.getCause(), null, this.rootView, ErrorInfo.make(5, Newapp.getNameOfService(this.serviceId), imageUri, R.string.could_not_load_image));
    }

    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
    }

    public void onLoadingCancelled(String imageUri, View view) {
    }
}
