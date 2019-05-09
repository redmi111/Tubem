package full.movie.tubem.player.report;

import android.content.Context;
import org.acra.config.ACRAConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

public class AcraReportSenderFactory implements ReportSenderFactory {
    public ReportSender create(Context context, ACRAConfiguration config) {
        return new AcraReportSender();
    }
}
