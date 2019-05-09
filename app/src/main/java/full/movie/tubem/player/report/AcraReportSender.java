package full.movie.tubem.player.report;

import android.content.Context;
import full.movie.tubem.player.R;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

public class AcraReportSender implements ReportSender {
    public void send(Context context, CrashReportData report) throws ReportSenderException {
        ErrorActivity.reportError(context, report, ErrorInfo.make(6, "none", "App crash, UI failure", R.string.app_ui_crash));
    }
}
