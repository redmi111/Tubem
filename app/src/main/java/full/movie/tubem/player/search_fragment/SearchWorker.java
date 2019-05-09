package full.movie.tubem.player.search_fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import full.movie.tubem.player.R;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.exceptions.ExtractionException;
import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import full.movie.tubem.player.extractor.search.SearchEngine;
import full.movie.tubem.player.extractor.search.SearchEngine.Filter;
import full.movie.tubem.player.extractor.search.SearchEngine.NothingFoundException;
import full.movie.tubem.player.extractor.search.SearchResult;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import java.io.IOException;
import java.util.EnumSet;

public class SearchWorker {
    /* access modifiers changed from: private|static|final */
    public static final String TAG = SearchWorker.class.toString();
    private static SearchWorker searchWorker = null;
    /* access modifiers changed from: private */
    public int requestId = 0;
    private SearchRunnable runnable = null;
    /* access modifiers changed from: private */
    public SearchWorkerResultListener searchWorkerResultListener = null;

    private class ResultRunnable implements Runnable {
        private int requestId = 0;
        private final SearchResult result;

        public ResultRunnable(SearchResult result2, int requestId2) {
            this.result = result2;
            this.requestId = requestId2;
        }

        public void run() {
            if (this.requestId == SearchWorker.this.requestId) {
                SearchWorker.this.searchWorkerResultListener.onResult(this.result);
            }
        }
    }

    public class SearchRunnable implements Runnable {
        public static final String YOUTUBE = "Youtube";
        private Activity a = null;
        private final EnumSet<Filter> filter;
        final Handler h = new Handler();
        private final int page;
        private final String query;
        private volatile boolean runs = true;
        private int serviceId = -1;

        public SearchRunnable(int serviceId2, String query2, int page2, EnumSet<Filter> filter2, Activity activity, int requestId) {
            this.serviceId = serviceId2;
            this.query = query2;
            this.page = page2;
            this.filter = filter2;
            this.a = activity;
        }

        /* access modifiers changed from: 0000 */
        public void terminate() {
            this.runs = false;
        }

        public void run() {
            String serviceName = Newapp.getNameOfService(this.serviceId);
            try {
                SearchEngine engine = Newapp.getService(this.serviceId).getSearchEngineInstance();
                try {
                    SearchResult result = SearchResult.getSearchResult(engine, this.query, this.page, PreferenceManager.getDefaultSharedPreferences(this.a).getString(this.a.getString(R.string.search_language_key), this.a.getString(R.string.default_language_value)), this.filter);
                    if (this.runs) {
                        this.h.post(new ResultRunnable(result, SearchWorker.this.requestId));
                    }
                    View rootView = this.a.findViewById(16908290);
                    if (result != null && !result.errors.isEmpty()) {
                        Log.e(SearchWorker.TAG, "OCCURRED ERRORS DURING SEARCH EXTRACTION:");
                        for (Throwable e : result.errors) {
                            e.printStackTrace();
                            Log.e(SearchWorker.TAG, "------");
                        }
                        if (!result.resultList.isEmpty() || result.errors.isEmpty()) {
                            ErrorActivity.reportError(this.h, (Context) this.a, result.errors, null, rootView, ErrorInfo.make(0, serviceName, this.query, R.string.light_parsing_error));
                        } else {
                            ErrorActivity.reportError(this.h, (Context) this.a, result.errors, null, null, ErrorInfo.make(0, serviceName, this.query, R.string.parsing_error));
                        }
                    }
                } catch (ReCaptchaException e2) {
                    this.h.post(new Runnable() {
                        public void run() {
                            SearchWorker.this.searchWorkerResultListener.onReCaptchaChallenge();
                        }
                    });
                } catch (IOException e3) {
                    this.h.post(new Runnable() {
                        public void run() {
                            SearchWorker.this.searchWorkerResultListener.onNothingFound(R.string.network_error);
                        }
                    });
                    e3.printStackTrace();
                } catch (final NothingFoundException e4) {
                    this.h.post(new Runnable() {
                        public void run() {
                            SearchWorker.this.searchWorkerResultListener.onError(e4.getMessage());
                        }
                    });
                } catch (ExtractionException e5) {
                    ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e5, null, null, ErrorInfo.make(0, serviceName, this.query, R.string.parsing_error));
                    e5.printStackTrace();
                } catch (Exception e6) {
                    ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e6, null, null, ErrorInfo.make(0, YOUTUBE, this.query, R.string.general_error));
                    e6.printStackTrace();
                }
            } catch (ExtractionException e7) {
                ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e7, null, null, ErrorInfo.make(0, Integer.toString(this.serviceId), this.query, R.string.general_error));
            }
        }
    }

    public interface SearchWorkerResultListener {
        void onError(String str);

        void onNothingFound(int i);

        void onReCaptchaChallenge();

        void onResult(SearchResult searchResult);
    }

    public static SearchWorker getInstance() {
        if (searchWorker != null) {
            return searchWorker;
        }
        SearchWorker searchWorker2 = new SearchWorker();
        searchWorker = searchWorker2;
        return searchWorker2;
    }

    public void setSearchWorkerResultListener(SearchWorkerResultListener listener) {
        this.searchWorkerResultListener = listener;
    }

    /*private SearchWorker() {
    }*/

    public void search(int serviceId, String query, int page, Activity a, EnumSet<Filter> filter) {
        if (this.runnable != null) {
            terminate();
        }
        this.runnable = new SearchRunnable(serviceId, query, page, filter, a, this.requestId);
        new Thread(this.runnable).start();
    }

    public void terminate() {
        this.requestId++;
        this.runnable.terminate();
    }
}
