package full.movie.tubem.player;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder;
import com.onesignal.OneSignal;

import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.report.AcraReportSenderFactory;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import full.movie.tubem.player.settings.SettingsActivity;
import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import io.fabric.sdk.android.Fabric;
import org.acra.ACRA;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.ReportSenderFactory;

public class App extends Application {
    private static final String TAG = App.class.toString();
    private static boolean useTor;
    //ToDo: Added Class[]
    final Class<? extends ReportSenderFactory>[] reportSenderFactoryClasses = new Class[]{AcraReportSenderFactory.class};

    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        try {
            ACRA.init((Application) this, new ConfigurationBuilder(this).setReportSenderFactoryClasses(this.reportSenderFactoryClasses).build());
        } catch (ACRAConfigurationException ace) {
            ace.printStackTrace();
            ErrorActivity.reportError((Context) this, (Throwable) ace, null, null, ErrorInfo.make(0, "none", "Could not initialize ACRA crash report", R.string.app_ui_crash));
        }
        Newapp.init(Downloader.getInstance());
        ImageLoader.getInstance().init(new Builder(this).build());
        configureTor(false);
        SettingsActivity.initSettings(this);
        MobileAds.initialize(this, getResources().getString(R.string.appid));
    }

    public static void configureTor(boolean enabled) {
        useTor = enabled;
        if (useTor) {
            NetCipher.useTor();
        } else {
            NetCipher.setProxy(null);
        }
    }

    public static void checkStartTor(Context context) {
        if (useTor) {
            OrbotHelper.requestStartTor(context);
        }
    }

    public static boolean isUsingTor() {
        return useTor;
    }
}
