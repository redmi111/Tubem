package full.movie.tubem.player.extractor;

import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import java.io.IOException;
import java.util.Map;

public interface Downloader {
    String download(String str) throws IOException, ReCaptchaException;

    String download(String str, String str2) throws IOException, ReCaptchaException;

    String download(String str, Map<String, String> map) throws IOException, ReCaptchaException;
}
