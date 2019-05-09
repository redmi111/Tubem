package full.movie.tubem.player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ReCaptchaActivity extends AppCompatActivity {
    public static final int RECAPTCHA_REQUEST = 10;
    public static final String TAG = ReCaptchaActivity.class.toString();
    public static final String YT_URL = "https://www.youtube.com";

    private class ReCaptchaWebViewClient extends WebViewClient {
        private Activity context;
        private String mCookies;

        ReCaptchaWebViewClient(Activity ctx) {
            this.context = ctx;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            if (find_access_cookies(CookieManager.getInstance().getCookie(url))) {
                Downloader.setCookies(this.mCookies);
                ReCaptchaActivity.this.setResult(-1);
                ReCaptchaActivity.this.finish();
            }
        }

        private boolean find_access_cookies(String cookies) {
            String[] parts;
            String c_s_gl = "";
            String c_goojf = "";
            for (String part : cookies.split("; ")) {
                if (part.trim().startsWith("s_gl")) {
                    c_s_gl = part.trim();
                }
                if (part.trim().startsWith("goojf")) {
                    c_goojf = part.trim();
                }
            }
            if (c_s_gl.length() <= 0 || c_goojf.length() <= 0) {
                return false;
            }
            this.mCookies = cookies;
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_recaptcha);
        setResult(0);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle((int) R.string.reCaptcha_title);
        actionBar.setDisplayShowTitleEnabled(true);
        WebView myWebView = (WebView) findViewById(R.id.reCaptchaWebView);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new ReCaptchaWebViewClient(this));
        myWebView.clearCache(true);
        myWebView.clearHistory();
        CookieManager cookieManager = CookieManager.getInstance();
        if (VERSION.SDK_INT >= 21) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                public void onReceiveValue(Boolean aBoolean) {
                }
            });
        } else {
            cookieManager.removeAllCookie();
        }
        myWebView.loadUrl(YT_URL);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(67108864);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return false;
        }
    }
}
