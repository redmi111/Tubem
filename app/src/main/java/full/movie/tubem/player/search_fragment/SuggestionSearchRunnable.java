package full.movie.tubem.player.search_fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;
import full.movie.tubem.player.R;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import java.io.IOException;
import java.util.List;

public class SuggestionSearchRunnable implements Runnable {
    /* access modifiers changed from: private|final */
    public final Activity a;
    /* access modifiers changed from: private|final */
    public final SuggestionListAdapter adapter;
    private final Handler h = new Handler();
    private final String query;
    private final int serviceId;

    private class SuggestionResultRunnable implements Runnable {
        private final List<String> suggestions;

        private SuggestionResultRunnable(List<String> suggestions2) {
            this.suggestions = suggestions2;
        }

        public void run() {
            SuggestionSearchRunnable.this.adapter.updateAdapter(this.suggestions);
        }
    }

    public SuggestionSearchRunnable(int serviceId2, String query2, Activity activity, SuggestionListAdapter adapter2) {
        this.serviceId = serviceId2;
        this.query = query2;
        this.a = activity;
        this.adapter = adapter2;
    }

    public void run() {
        try {
            this.h.post(new SuggestionResultRunnable(Newapp.getService(this.serviceId).getSuggestionExtractorInstance().suggestionList(this.query, PreferenceManager.getDefaultSharedPreferences(this.a).getString(this.a.getString(R.string.search_language_key), this.a.getString(R.string.default_language_value)))));
        } catch (ExtractionException e) {
            ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e, null, this.a.findViewById(16908290), ErrorInfo.make(0, Newapp.getNameOfService(this.serviceId), this.query, R.string.parsing_error));
            e.printStackTrace();
        } catch (IOException e2) {
            postNewErrorToast(this.h, R.string.network_error);
            e2.printStackTrace();
        } catch (Exception e3) {
            ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e3, null, this.a.findViewById(16908290), ErrorInfo.make(0, Newapp.getNameOfService(this.serviceId), this.query, R.string.general_error));
        }
    }

    private void postNewErrorToast(Handler h2, final int stringResource) {
        h2.post(new Runnable() {
            public void run() {
                Toast.makeText(SuggestionSearchRunnable.this.a, SuggestionSearchRunnable.this.a.getString(stringResource), 0).show();
            }
        });
    }
}
