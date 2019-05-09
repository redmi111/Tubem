package full.movie.tubem.player.report;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.InputDeviceCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/*import com.google.android.gms.common.util.CrashUtils.ErrorDialogData;
import com.google.android.gms.dynamite.ProviderConstants;*/
import full.movie.tubem.player.ActivityCommunicator;
import full.movie.tubem.player.Downloader;
import full.movie.tubem.player.MainActivity;
import full.movie.tubem.player.R;
import full.movie.tubem.player.ThemableActivity;
import full.movie.tubem.player.extractor.Parser;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.json.JSONArray;
import org.json.JSONObject;

public class ErrorActivity extends ThemableActivity {
    public static final String ERROR_EMAIL_ADDRESS = "";
    public static final String ERROR_EMAIL_SUBJECT = "Exception in Newapp 1.0";
    public static final String ERROR_INFO = "error_info";
    public static final String ERROR_LIST = "error_list";
    public static final int GET_SUGGESTIONS = 2;
    public static final String GET_SUGGESTIONS_STRING = "get suggestions";
    public static final int LOAD_IMAGE = 5;
    public static final String LOAD_IMAGE_STRING = "load image";
    public static final int REQUESTED_CHANNEL = 7;
    public static final String REQUESTED_CHANNEL_STRING = "requested channel";
    public static final int REQUESTED_STREAM = 1;
    public static final String REQUESTED_STREAM_STRING = "requested stream";
    public static final int SEARCHED = 0;
    public static final String SEARCHED_STRING = "searched";
    public static final int SOMETHING_ELSE = 3;
    public static final String SOMETHING_ELSE_STRING = "something";
    public static final String TAG = ErrorActivity.class.toString();
    public static final int UI_ERROR = 6;
    public static final String UI_ERROR_STRING = "ui error";
    public static final int USER_REPORT = 4;
    public static final String USER_REPORT_STRING = "user report";
    private String currentTimeStamp;
    private ErrorInfo errorInfo;
    private String[] errorList;
    private TextView errorMessageView;
    private TextView errorView;
    /* access modifiers changed from: private */
    public String globIpRange;
    Thread globIpRangeThread;
    /* access modifiers changed from: private */
    public TextView infoView;
    /* access modifiers changed from: private */
    public Button reportButton;
    private Class returnActivity;
    private EditText userCommentBox;

    public static class ErrorInfo implements Parcelable {
        public static final Creator<ErrorInfo> CREATOR = new Creator<ErrorInfo>() {
            public ErrorInfo createFromParcel(Parcel source) {
                return new ErrorInfo(source);
            }

            public ErrorInfo[] newArray(int size) {
                return new ErrorInfo[size];
            }
        };
        public int message;
        public String request;
        public String serviceName;
        public int userAction;

        public static ErrorInfo make(int userAction2, String serviceName2, String request2, int message2) {
            ErrorInfo info = new ErrorInfo();
            info.userAction = userAction2;
            info.serviceName = serviceName2;
            info.request = request2;
            info.message = message2;
            return info;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.userAction);
            dest.writeString(this.request);
            dest.writeString(this.serviceName);
            dest.writeInt(this.message);
        }

        public ErrorInfo() {
        }

        protected ErrorInfo(Parcel in) {
            this.userAction = in.readInt();
            this.request = in.readString();
            this.serviceName = in.readString();
            this.message = in.readInt();
        }
    }

    private class IpRageReturnRunnable implements Runnable {
        String ipRange;

        public IpRageReturnRunnable(String ipRange2) {
            this.ipRange = ipRange2;
        }

        public void run() {
            ErrorActivity.this.globIpRange = this.ipRange;
            if (ErrorActivity.this.infoView != null) {
                ErrorActivity.this.infoView.setText(ErrorActivity.this.infoView.getText().toString() + "\n" + ErrorActivity.this.globIpRange);
                ErrorActivity.this.reportButton.setEnabled(true);
            }
        }
    }

    private class IpRagneRequester implements Runnable {
        Handler h;

        private IpRagneRequester() {
            this.h = new Handler();
        }

        public void run() {
            String ipRange = "none";
            try {
                ipRange = Parser.matchGroup1("([0-9]*\\.[0-9]*\\.)[0-9]*\\.[0-9]*", Downloader.getInstance().download("https://ipv4.icanhazip.com")) + "0.0";
            } catch (Throwable e) {
                Log.d(ErrorActivity.TAG, "Error while error: could not get iprange");
                e.printStackTrace();
            } finally {
                this.h.post(new IpRageReturnRunnable(ipRange));
            }
        }
    }

    public static void reportUiError(AppCompatActivity activity, Throwable el) {
        reportError((Context) activity, el, activity.getClass(), null, ErrorInfo.make(6, "none", "", R.string.app_ui_crash));
    }

    public static void reportError(final Context context, final List<Throwable> el, final Class returnAcitivty, View rootView, final ErrorInfo errorInfo2) {
        if (rootView != null) {
            Snackbar.make(rootView, (int) R.string.error_snackbar_message, 0).setActionTextColor((int) InputDeviceCompat.SOURCE_ANY).setAction((int) R.string.error_snackbar_action, (OnClickListener) new OnClickListener() {
                public void onClick(View v) {
                    ActivityCommunicator.getCommunicator().returnActivity = returnAcitivty;
                    Intent intent = new Intent(context, ErrorActivity.class);
                    intent.putExtra(ErrorActivity.ERROR_INFO, errorInfo2);
                    intent.putExtra(ErrorActivity.ERROR_LIST, ErrorActivity.elToSl(el));
                    context.startActivity(intent);
                }
            });
            return;
        }
        ActivityCommunicator.getCommunicator().returnActivity = returnAcitivty;
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(ERROR_INFO, errorInfo2);
        intent.putExtra(ERROR_LIST, elToSl(el));
        context.startActivity(intent);
    }

    public static void reportError(Context context, Throwable e, Class returnAcitivty, View rootView, ErrorInfo errorInfo2) {
        List<Throwable> el = null;
        if (e != null) {
            el = new Vector<>();
            el.add(e);
        }
        reportError(context, el, returnAcitivty, rootView, errorInfo2);
    }

    public static void reportError(Handler handler, Context context, Throwable e, Class returnAcitivty, View rootView, ErrorInfo errorInfo2) {
        List<Throwable> el = null;
        if (e != null) {
            el = new Vector<>();
            el.add(e);
        }
        reportError(handler, context, el, returnAcitivty, rootView, errorInfo2);
    }

    public static void reportError(Handler handler, Context context, List<Throwable> el, Class returnAcitivty, View rootView, ErrorInfo errorInfo2) {
        final Context context2 = context;
        final List<Throwable> list = el;
        final Class cls = returnAcitivty;
        final View view = rootView;
        final ErrorInfo errorInfo3 = errorInfo2;
        handler.post(new Runnable() {
            public void run() {
                ErrorActivity.reportError(context2, list, cls, view, errorInfo3);
            }
        });
    }

    /*public static void reportError(Context context, CrashReportData report, ErrorInfo errorInfo2) {
        ReportField key = null;
        for (ReportField k : report.keySet()) {
            if (k.toString().equals("STACK_TRACE")) {
                key = k;
            }
        }
        String[] el = {(String) report.get(key)};
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(ERROR_INFO, errorInfo2);
        intent.putExtra(ERROR_LIST, el);
        intent.setFlags(268435456);
        context.startActivity(intent);
    }*/
    public static void reportError(final Context context, final CrashReportData crashReportData, final ErrorActivity.ErrorInfo errorActivity$ErrorInfo) {
        Object o = null;
        for (final ReportField reportField : crashReportData.keySet()) {
            if (reportField.toString().equals("STACK_TRACE")) {
                o = reportField;
            }
        }
        final String[] array = {String.valueOf(crashReportData.get(o))};
        final Intent intent = new Intent(context, (Class)ErrorActivity.class);
        intent.putExtra("error_info", (Parcelable)errorActivity$ErrorInfo);
        intent.putExtra("error_list", array);
        intent.setFlags(268435456);
        context.startActivity(intent);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_error);
        Intent intent = getIntent();
        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle((int) R.string.error_report_title);
            actionBar.setDisplayShowTitleEnabled(true);
        } catch (Throwable e) {
            Log.e(TAG, "Error turing exception handling");
            e.printStackTrace();
        }
        this.reportButton = (Button) findViewById(R.id.errorReportButton);
        this.userCommentBox = (EditText) findViewById(R.id.errorCommentBox);
        this.errorView = (TextView) findViewById(R.id.errorView);
        this.infoView = (TextView) findViewById(R.id.errorInfosView);
        this.errorMessageView = (TextView) findViewById(R.id.errorMessageView);
        this.returnActivity = ActivityCommunicator.getCommunicator().returnActivity;
        this.errorInfo = (ErrorInfo) intent.getParcelableExtra(ERROR_INFO);
        this.errorList = intent.getStringArrayExtra(ERROR_LIST);
        addGuruMeditaion();
        this.currentTimeStamp = getCurrentTimeStamp();
        this.reportButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.SENDTO");
                intent.setData(Uri.parse("mailto:")).putExtra("android.intent.extra.SUBJECT", ErrorActivity.ERROR_EMAIL_SUBJECT).putExtra("android.intent.extra.TEXT", ErrorActivity.this.buildJson());
                ErrorActivity.this.startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
        this.reportButton.setEnabled(false);
        this.globIpRangeThread = new Thread(new IpRagneRequester());
        this.globIpRangeThread.start();
        buildInfo(this.errorInfo);
        if (this.errorInfo.message != 0) {
            this.errorMessageView.setText(this.errorInfo.message);
        } else {
            this.errorMessageView.setVisibility(View.GONE);
            findViewById(R.id.messageWhatHappenedView).setVisibility(View.GONE);
        }
        this.errorView.setText(formErrorText(this.errorList));
        for (String e2 : this.errorList) {
            Log.e(TAG, e2);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.error_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                goToReturnActivity();
                break;
        }
        return false;
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }

    private String formErrorText(String[] el) {
        String text = "";
        if (el != null) {
            int length = el.length;
            for (int i = 0; i < length; i++) {
                text = text + "-------------------------------------\n" + el[i];
            }
        }
        return text + "-------------------------------------";
    }

    private void goToReturnActivity() {
        Intent intent;
        if (this.returnActivity == null) {
            super.onBackPressed();
            return;
        }
        if (this.returnActivity == null || !this.returnActivity.isAssignableFrom(Activity.class)) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, this.returnActivity);
        }
        intent.addFlags(67108864);
        NavUtils.navigateUpTo(this, intent);
    }

    private void buildInfo(ErrorInfo info) {
        TextView infoView2 = (TextView) findViewById(R.id.errorInfosView);
        ((TextView) findViewById(R.id.errorInfoLabelsView)).setText(getString(R.string.info_labels).replace("\\n", "\n"));
        infoView2.setText("" + getUserActionString(info.userAction) + "\n" + info.request + "\n" + getContentLangString() + "\n" + info.serviceName + "\n" + this.currentTimeStamp + "\n" + getPackageName() + "\n" + "1.0" + "\n" + getOsString());
    }

    /* access modifiers changed from: private */
    public String buildJson() {
        JSONObject errorObject = new JSONObject();
        try {
            errorObject.put("user_action", getUserActionString(this.errorInfo.userAction)).put("request", this.errorInfo.request).put("content_language", getContentLangString()).put(NotificationCompat.CATEGORY_SERVICE, this.errorInfo.serviceName).put("package", getPackageName()).put("version", "1.0").put("os", getOsString()).put("time", this.currentTimeStamp).put("ip_range", this.globIpRange);
            JSONArray exceptionArray = new JSONArray();
            if (this.errorList != null) {
                for (String e : this.errorList) {
                    exceptionArray.put(e);
                }
            }
            errorObject.put("exceptions", exceptionArray);
            errorObject.put("user_comment", this.userCommentBox.getText().toString());
            return errorObject.toString(3);
        } catch (Throwable e2) {
            Log.e(TAG, "Error while erroring: Could not build json");
            e2.printStackTrace();
            return "";
        }
    }

    private String getUserActionString(int userAction) {
        switch (userAction) {
            case 0:
                return SEARCHED_STRING;
            case 1:
                return REQUESTED_STREAM_STRING;
            case 2:
                return GET_SUGGESTIONS_STRING;
            case 3:
                return SOMETHING_ELSE_STRING;
            case 4:
                return USER_REPORT_STRING;
            case 5:
                return LOAD_IMAGE_STRING;
            case 6:
                return UI_ERROR_STRING;
            case 7:
                return REQUESTED_CHANNEL_STRING;
            default:
                return "Your description is in another castle.";
        }
    }

    private String getContentLangString() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.search_language_key), "none");
    }

    private String getOsString() {
        String osBase = VERSION.SDK_INT >= 23 ? VERSION.BASE_OS : "Android";
        StringBuilder append = new StringBuilder().append(System.getProperty("os.name")).append(" ");
        if (osBase.isEmpty()) {
            osBase = "Android";
        }
        return append.append(osBase).append(" ").append(VERSION.RELEASE).append(" - ").append(Integer.toString(VERSION.SDK_INT)).toString();
    }

    private void addGuruMeditaion() {
        TextView sorryView = (TextView) findViewById(R.id.errorSorryView);
        sorryView.setText(sorryView.getText().toString() + "\n" + getString(R.string.guru_meditation));
    }

    public void onBackPressed() {
        goToReturnActivity();
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }

    /* access modifiers changed from: private|static */
    public static String[] elToSl(List<Throwable> stackTraces) {
        String[] out = new String[stackTraces.size()];
        for (int i = 0; i < stackTraces.size(); i++) {
            out[i] = getStackTrace((Throwable) stackTraces.get(i));
        }
        return out;
    }
}
