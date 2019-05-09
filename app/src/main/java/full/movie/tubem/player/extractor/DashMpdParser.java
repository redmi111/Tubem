package full.movie.tubem.player.extractor;

import android.util.Xml;
import com.google.android.exoplayer.util.MimeTypes;
import full.movie.tubem.player.extractor.exceptions.ParsingException;
import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import full.movie.tubem.player.extractor.stream_info.AudioStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;

public class DashMpdParser {

    static class DashMpdParsingException extends ParsingException {
        DashMpdParsingException(String message, Exception e) {
            super(message, e);
        }
    }

    private DashMpdParser() {
    }

    public static List<AudioStream> getAudioStreams(String dashManifestUrl) throws DashMpdParsingException, ReCaptchaException {
        try {
            String dashDoc = Newapp.getDownloader().download(dashManifestUrl);
            Vector<AudioStream> audioStreams = new Vector<>();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(new StringReader(dashDoc));
                String tagName = "";
                String currentMimeType = "";
                int currentBandwidth = -1;
                int currentSamplingRate = -1;
                boolean currentTagIsBaseUrl = false;
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    switch (eventType) {
                        case 2:
                            tagName = parser.getName();
                            if (!tagName.equals("AdaptationSet")) {
                                if (!tagName.equals("Representation") || !currentMimeType.contains(MimeTypes.BASE_TYPE_AUDIO)) {
                                    if (tagName.equals("BaseURL")) {
                                        currentTagIsBaseUrl = true;
                                        break;
                                    }
                                } else {
                                    currentBandwidth = Integer.parseInt(parser.getAttributeValue("", "bandwidth"));
                                    currentSamplingRate = Integer.parseInt(parser.getAttributeValue("", "audioSamplingRate"));
                                    break;
                                }
                            } else {
                                currentMimeType = parser.getAttributeValue("", "mimeType");
                                break;
                            }
                            break;
                        case 3:
                            if (!tagName.equals("AdaptationSet")) {
                                if (tagName.equals("BaseURL")) {
                                    currentTagIsBaseUrl = false;
                                    break;
                                }
                            } else {
                                currentMimeType = "";
                                break;
                            }
                            break;
                        case 4:
                            if (currentTagIsBaseUrl && currentMimeType.contains(MimeTypes.BASE_TYPE_AUDIO)) {
                                int format = -1;
                                if (currentMimeType.equals(MediaFormat.WEBMA.mimeType)) {
                                    format = MediaFormat.WEBMA.id;
                                } else if (currentMimeType.equals(MediaFormat.M4A.mimeType)) {
                                    format = MediaFormat.M4A.id;
                                }
                                audioStreams.add(new AudioStream(parser.getText(), format, currentBandwidth, currentSamplingRate));
                                break;
                            }
                    }
                }
                return audioStreams;
            } catch (Exception e) {
                throw new DashMpdParsingException("Could not parse Dash mpd", e);
            }
        } catch (IOException ioe) {
            throw new DashMpdParsingException("Could not get dash mpd: " + dashManifestUrl, ioe);
        } catch (ReCaptchaException e2) {
            throw new ReCaptchaException("reCaptcha Challenge needed");
        }
    }
}
