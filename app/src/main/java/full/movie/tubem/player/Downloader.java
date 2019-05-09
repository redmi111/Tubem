package full.movie.tubem.player;

import full.movie.tubem.player.extractor.exceptions.ReCaptchaException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;

public class Downloader implements full.movie.tubem.player.extractor.Downloader {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
    private static Downloader instance = null;
    private static String mCookies = "";

    private Downloader() {
    }

    public static Downloader getInstance() {
        if (instance == null) {
            synchronized (Downloader.class) {
                if (instance == null) {
                    instance = new Downloader();
                }
            }
        }
        return instance;
    }

    public static synchronized void setCookies(String cookies) {
        synchronized (Downloader.class) {
            mCookies = cookies;
        }
    }

    public static synchronized String getCookies() {
        String str;
        synchronized (Downloader.class) {
            str = mCookies;
        }
        return str;
    }

    public String download(String siteUrl, String language) throws IOException, ReCaptchaException {
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Accept-Language", language);
        return download(siteUrl, requestProperties);
    }

    public String download(String siteUrl, Map<String, String> customProperties) throws IOException, ReCaptchaException {
        HttpsURLConnection con = (HttpsURLConnection) new URL(siteUrl).openConnection();
        for (Entry pair : customProperties.entrySet()) {
            con.setRequestProperty((String) pair.getKey(), (String) pair.getValue());
        }
        return dl(con);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String dl(HttpsURLConnection con) throws IOException, ReCaptchaException {
        StringBuilder response = new StringBuilder();
        BufferedReader in = null;
        try {
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            if (getCookies().length() > 0) {
                con.setRequestProperty("Cookie", getCookies());
            }
            BufferedReader in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while (true) {
                try {
                    String inputLine = in2.readLine();
                    if (inputLine == null) {
                        break;
                    }
                    response.append(inputLine);
                } catch (UnknownHostException e) {
                   // uhe = e;
                    in = in2;
                    try {
                        throw new IOException("unknown host or no network", e);
                    } catch (Throwable th) {
                        th = th;
                        if (in != null) {
                            in.close();
                        }
                        try {
                            throw th;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                } catch (Exception e2) {
                    //e = e2;
                    in = in2;
                    if (con.getResponseCode() != 429) {
                    }
                } catch (Throwable th2) {
                   // th = th2;
                    in = in2;
                    if (in != null) {
                    }
                    throw th2;
                }
            }
            if (in2 != null) {
                in2.close();
            }
            return response.toString();
        } catch (UnknownHostException e3) {
            //uhe = e3;
            throw new IOException("unknown host or no network", e3);
        } catch (Exception e4) {
           // e = e4;
            if (con.getResponseCode() != 429) {
                throw new ReCaptchaException("reCaptcha Challenge requested");
            }
            throw new IOException(e4);
        }
    }

    public String download(String siteUrl) throws IOException, ReCaptchaException {
        return dl((HttpsURLConnection) new URL(siteUrl).openConnection());
    }
}
