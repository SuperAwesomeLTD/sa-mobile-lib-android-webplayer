package tv.superawesome.lib.sawebplayer;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SAWebClient extends WebViewClient {

    private Listener listener;

    SAWebClient(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        listener.onPageStarted(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        listener.onPageFinished(view);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return listener.shouldOverrideUrlLoading(view, url);
    }

    interface Listener {
        void onPageStarted (WebView view, String url);
        void onPageFinished (WebView view);
        boolean shouldOverrideUrlLoading (WebView webView, String url);
    }
}
