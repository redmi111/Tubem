package full.movie.tubem.player.player.exoplayer;

import android.content.Context;
import android.os.Handler;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
//import com.google.android.exoplayer.metadata.Id3Parser;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.metadata.id3.Id3Parser;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;
import full.movie.tubem.player.player.exoplayer.NPExoPlayer.RendererBuilder;
import java.io.IOException;

public class HlsRendererBuilder implements RendererBuilder {
    private static final int BUFFER_SEGMENT_SIZE = 65536;
    private static final int MAIN_BUFFER_SEGMENTS = 256;
    private static final int TEXT_BUFFER_SEGMENTS = 2;
    private final Context context;
    private AsyncRendererBuilder currentAsyncBuilder;
    private final String url;
    private final String userAgent;

    private static final class AsyncRendererBuilder implements ManifestCallback<HlsPlaylist> {
        private boolean canceled;
        private final Context context;
        private final NPExoPlayer player;
        private final ManifestFetcher<HlsPlaylist> playlistFetcher;
        private final String url;
        private final String userAgent;

        public AsyncRendererBuilder(Context context2, String userAgent2, String url2, NPExoPlayer player2) {
            this.context = context2;
            this.userAgent = userAgent2;
            this.url = url2;
            this.player = player2;
            this.playlistFetcher = new ManifestFetcher<>(url2, new DefaultUriDataSource(context2, userAgent2), new HlsPlaylistParser());
        }

        public void init() {
            this.playlistFetcher.singleLoad(this.player.getMainHandler().getLooper(), this);
        }

        public void cancel() {
            this.canceled = true;
        }

        public void onSingleManifestError(IOException e) {
            if (!this.canceled) {
                this.player.onRenderersError(e);
            }
        }


        /* Code decompiled incorrectly, please refer to instructions dump. */
        /*public void onSingleManifest(HlsPlaylist manifest) {
            //? r30;
            if (!this.canceled) {
                Handler mainHandler = this.player.getMainHandler();
                DefaultLoadControl defaultLoadControl = new DefaultLoadControl(new DefaultAllocator(65536));
                DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                PtsTimestampAdjusterProvider timestampAdjusterProvider = new PtsTimestampAdjusterProvider();
                HlsSampleSource sampleSource = new HlsSampleSource(new HlsChunkSource(true, new DefaultUriDataSource(this.context, (TransferListener) bandwidthMeter, this.userAgent), this.url, manifest, DefaultHlsTrackSelector.newDefaultInstance(this.context), bandwidthMeter, timestampAdjusterProvider, 1), defaultLoadControl, 16777216, mainHandler, this.player, 0);
                MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(this.context, sampleSource, MediaCodecSelector.DEFAULT, 1, HlsChunkSource.DEFAULT_MIN_BUFFER_TO_SWITCH_UP_MS, mainHandler, this.player, 50);
                MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT, null, true, this.player.getMainHandler(), this.player, AudioCapabilities.getCapabilities(this.context), 3);
                MetadataTrackRenderer metadataTrackRenderer = new MetadataTrackRenderer(sampleSource, new Id3Parser(), this.player, mainHandler.getLooper());
                boolean preferWebvtt = false;
                if (manifest instanceof HlsMasterPlaylist) {
                    preferWebvtt = !((HlsMasterPlaylist) manifest).subtitles.isEmpty();
                }
                Object r30;
                if (preferWebvtt) {
                    DefaultUriDataSource defaultUriDataSource = new DefaultUriDataSource(this.context, (TransferListener) bandwidthMeter, this.userAgent);
                    TextTrackRenderer textTrackRenderer = new TextTrackRenderer((SampleSource) new HlsSampleSource(new HlsChunkSource(false, defaultUriDataSource, this.url, manifest, DefaultHlsTrackSelector.newVttInstance(), bandwidthMeter, timestampAdjusterProvider, 1), defaultLoadControl, 131072, mainHandler, this.player, 2), (TextRenderer) this.player, mainHandler.getLooper(), new SubtitleParser[0]);
                    r30 = textTrackRenderer;
                } else {
                    Eia608TrackRenderer eia608TrackRenderer = new Eia608TrackRenderer(sampleSource, this.player, mainHandler.getLooper());
                    r30 = eia608TrackRenderer;
                }
                TrackRenderer[] r29 = new TrackRenderer[4];
                r29[0] = videoRenderer;
                r29[1] = audioRenderer;
                r29[3] = metadataTrackRenderer;
                r29[2] = (TrackRenderer) r30;
                this.player.onRenderers(r29, bandwidthMeter);
            }
        }*/
        public void onSingleManifest(HlsPlaylist paramHlsPlaylist)
        {
            if (this.canceled) {
                return;
            }
            Handler localHandler = this.player.getMainHandler();
            DefaultLoadControl localDefaultLoadControl = new DefaultLoadControl(new DefaultAllocator(65536));
            DefaultBandwidthMeter localDefaultBandwidthMeter = new DefaultBandwidthMeter();
            PtsTimestampAdjusterProvider localPtsTimestampAdjusterProvider = new PtsTimestampAdjusterProvider();
            //Fixme: Arguments issue.
            HlsSampleSource localHlsSampleSource = new HlsSampleSource(new HlsChunkSource(true, new DefaultUriDataSource(this.context, localDefaultBandwidthMeter, this.userAgent),  paramHlsPlaylist,DefaultHlsTrackSelector.newDefaultInstance(this.context), localDefaultBandwidthMeter, localPtsTimestampAdjusterProvider,/* this.url*/ 1,  1), localDefaultLoadControl, 16777216, localHandler, this.player, 0);
            MediaCodecVideoTrackRenderer localMediaCodecVideoTrackRenderer = new MediaCodecVideoTrackRenderer(this.context, localHlsSampleSource, MediaCodecSelector.DEFAULT, 1, 5000L, localHandler, this.player, 50);
            MediaCodecAudioTrackRenderer localMediaCodecAudioTrackRenderer = new MediaCodecAudioTrackRenderer(localHlsSampleSource, MediaCodecSelector.DEFAULT, null, true, this.player.getMainHandler(), this.player, AudioCapabilities.getCapabilities(this.context), 3);
            MetadataTrackRenderer localMetadataTrackRenderer = new MetadataTrackRenderer(localHlsSampleSource, new Id3Parser(), this.player, localHandler.getLooper());
            int i = 0;
            if ((paramHlsPlaylist instanceof HlsMasterPlaylist))
            {
                if (!((HlsMasterPlaylist)paramHlsPlaylist).subtitles.isEmpty()) {
                    i = 1;
                }
            }
            else {
                if (i == 0) {
                    //break label346;
                }
            }
            //label346:
            //Fixme: Before delivery.
            /*for (TrackRenderer paramHlsPlaylist1 = new TextTrackRenderer(new HlsSampleSource(new HlsChunkSource(false, new DefaultUriDataSource(this.context, localDefaultBandwidthMeter, this.userAgent), this.url, paramHlsPlaylist, DefaultHlsTrackSelector.newSubtitleInstance(), localDefaultBandwidthMeter, localPtsTimestampAdjusterProvider, 1), localDefaultLoadControl, 131072, localHandler, this.player, 2), this.player, localHandler.getLooper(), new SubtitleParser[0]);; paramHlsPlaylist = new Eia608TrackRenderer(localHlsSampleSource, this.player, localHandler.getLooper()))
            {
                this.player.onRenderers(new TrackRenderer[] { localMediaCodecVideoTrackRenderer, localMediaCodecAudioTrackRenderer, paramHlsPlaylist1, localMetadataTrackRenderer }, localDefaultBandwidthMeter);
                return;
                i = 0;
                break;
            }*/
        }
    }

    public HlsRendererBuilder(Context context2, String userAgent2, String url2) {
        this.context = context2;
        this.userAgent = userAgent2;
        this.url = url2;
    }

    public void buildRenderers(NPExoPlayer player) {
        this.currentAsyncBuilder = new AsyncRendererBuilder(this.context, this.userAgent, this.url, player);
        this.currentAsyncBuilder.init();
    }

    public void cancel() {
        if (this.currentAsyncBuilder != null) {
            this.currentAsyncBuilder.cancel();
            this.currentAsyncBuilder = null;
        }
    }
}
