package full.movie.tubem.player.detail;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import full.movie.tubem.player.R;
import full.movie.tubem.player.extractor.Newapp;
import full.movie.tubem.player.extractor.StreamingService;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import full.movie.tubem.player.extractor.services.youtube.YoutubeStreamExtractor.DecryptException;
import full.movie.tubem.player.extractor.services.youtube.YoutubeStreamExtractor.GemaException;
import full.movie.tubem.player.extractor.services.youtube.YoutubeStreamExtractor.LiveStreamException;
import full.movie.tubem.player.extractor.stream_info.StreamExtractor;
import full.movie.tubem.player.extractor.stream_info.StreamExtractor.ContentNotAvailableException;
import full.movie.tubem.player.extractor.stream_info.StreamInfo;
import full.movie.tubem.player.extractor.stream_info.StreamInfo.StreamExctractException;
import full.movie.tubem.player.report.ErrorActivity;
import full.movie.tubem.player.report.ErrorActivity.ErrorInfo;
import java.io.IOException;

public class StreamInfoWorker {
    /* access modifiers changed from: private|static|final */
    public static final String TAG = StreamInfoWorker.class.toString();
    private static StreamInfoWorker streamInfoWorker = null;
    /* access modifiers changed from: private */
    public OnStreamInfoReceivedListener onStreamInfoReceivedListener = null;
    private StreamExtractorRunnable runnable = null;

    public interface OnStreamInfoReceivedListener {
        void onBlockedByGemaError();

        void onContentError();

        void onContentErrorWithMessage(int i);

        void onError(int i);

        void onReCaptchaException();

        void onReceive(StreamInfo streamInfo);
    }

    private class StreamExtractorRunnable implements Runnable {
        /* access modifiers changed from: private */
        public Activity a;
        private final Handler h = new Handler();
        private final int serviceId;
        private StreamExtractor streamExtractor;
        private final String videoUrl;

        public StreamExtractorRunnable(Activity a2, String videoUrl2, int serviceId2) {
            this.serviceId = serviceId2;
            this.videoUrl = videoUrl2;
            this.a = a2;
        }

        public void run() {
            StreamInfo streamInfo = null;
            try {
                StreamingService service = Newapp.getService(this.serviceId);
                try {
                    this.streamExtractor = service.getExtractorInstance(this.videoUrl);
                    streamInfo = StreamInfo.getVideoInfo(this.streamExtractor);
                    final StreamInfo streamInfo2 = streamInfo;
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamInfoWorker.this.onStreamInfoReceivedListener.onReceive(streamInfo2);
                        }
                    });
                    if (streamInfo != null && !streamInfo.errors.isEmpty()) {
                        Log.e(StreamInfoWorker.TAG, "OCCURRED ERRORS DURING EXTRACTION:");
                        for (Throwable e : streamInfo.errors) {
                            e.printStackTrace();
                            Log.e(StreamInfoWorker.TAG, "------");
                        }
                        ErrorActivity.reportError(this.h, (Context) this.a, streamInfo.errors, null, this.a != null ? this.a.findViewById(R.id.video_item_detail) : null, ErrorInfo.make(1, service.getServiceInfo().name, this.videoUrl, 0));
                    }
                } catch (ReCaptchaException e2) {
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamInfoWorker.this.onStreamInfoReceivedListener.onReCaptchaException();
                        }
                    });
                } catch (IOException e3) {
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamInfoWorker.this.onStreamInfoReceivedListener.onError(R.string.network_error);
                        }
                    });
                    e3.printStackTrace();
                } catch (DecryptException de) {
                    ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) de, VideoItemDetailFragment.class, null, ErrorInfo.make(1, service.getServiceInfo().name, this.videoUrl, R.string.youtube_signature_decryption_error));
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamExtractorRunnable.this.a.finish();
                        }
                    });
                    de.printStackTrace();
                } catch (GemaException e4) {
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamInfoWorker.this.onStreamInfoReceivedListener.onBlockedByGemaError();
                        }
                    });
                } catch (LiveStreamException e5) {
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamInfoWorker.this.onStreamInfoReceivedListener.onContentErrorWithMessage(R.string.live_streams_not_supported);
                        }
                    });
                } catch (ContentNotAvailableException e6) {
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamInfoWorker.this.onStreamInfoReceivedListener.onContentError();
                        }
                    });
                    e6.printStackTrace();
                } catch (StreamExctractException e7) {
                    if (!streamInfo.errors.isEmpty()) {
                        ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e7, VideoItemDetailFragment.class, null, ErrorInfo.make(1, service.getServiceInfo().name, this.videoUrl, R.string.could_not_get_stream));
                    } else {
                        ErrorActivity.reportError(this.h, (Context) this.a, streamInfo.errors, VideoItemDetailFragment.class, null, ErrorInfo.make(1, service.getServiceInfo().name, this.videoUrl, R.string.could_not_get_stream));
                    }
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamExtractorRunnable.this.a.finish();
                        }
                    });
                    e7.printStackTrace();
                } catch (ParsingException e8) {
                    ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e8, VideoItemDetailFragment.class, null, ErrorInfo.make(1, service.getServiceInfo().name, this.videoUrl, R.string.parsing_error));
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamExtractorRunnable.this.a.finish();
                        }
                    });
                    e8.printStackTrace();
                } catch (Exception e9) {
                    ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e9, VideoItemDetailFragment.class, null, ErrorInfo.make(1, service.getServiceInfo().name, this.videoUrl, R.string.general_error));
                    this.h.post(new Runnable() {
                        public void run() {
                            StreamExtractorRunnable.this.a.finish();
                        }
                    });
                    e9.printStackTrace();
                }
            } catch (Exception e10) {
                e10.printStackTrace();
                ErrorActivity.reportError(this.h, (Context) this.a, (Throwable) e10, VideoItemDetailFragment.class, null, ErrorInfo.make(1, "", this.videoUrl, R.string.could_not_get_stream));
            }
        }
    }

    private StreamInfoWorker() {
    }

    public static StreamInfoWorker getInstance() {
        if (streamInfoWorker != null) {
            return streamInfoWorker;
        }
        StreamInfoWorker streamInfoWorker2 = new StreamInfoWorker();
        streamInfoWorker = streamInfoWorker2;
        return streamInfoWorker2;
    }

    public void search(int serviceId, String url, Activity a) {
        this.runnable = new StreamExtractorRunnable(a, url, serviceId);
        new Thread(this.runnable).start();
    }

    public void setOnStreamInfoReceivedListener(OnStreamInfoReceivedListener onStreamInfoReceivedListener2) {
        this.onStreamInfoReceivedListener = onStreamInfoReceivedListener2;
    }
}
