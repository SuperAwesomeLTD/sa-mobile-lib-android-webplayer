package tv.superawesome.lib.sawebplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SAWebView extends WebView {

    private boolean finishedLoading = false;

    // interface objects used for the web player callback mechanism
    SAWebPlayerEventInterface eventListener;

    public SAWebView(Context context) {
        this(context, null, 0);
    }

    public SAWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SAWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // set bg color transparent
        this.setBackgroundColor(Color.TRANSPARENT);

        // enable javascript
        this.setInitialScale(100);
        this.getSettings().setJavaScriptEnabled(true);

        // setup a custom WebView client to catch events
        this.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (shouldOverrideUrlLoading(view, url)) {
                    view.stopLoading();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                finishedLoading = true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (finishedLoading) {
                    eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Click, url);
                    return true;
                } else {
                    return false;
                }
            }

        });
    }

    public void loadHTML(String html) {

        // if the HTML is null, just return by default and don't do anything
        if (html == null) return;

        // replace HTML template parameters with their corresponding actual values,
        // before loading the final HTML into the web player
        String baseHtml = "<html><header><style>html, body, div { margin: 0px; padding: 0px; width: 100%; height: 100%; }</style></header><body>_CONTENT_</body></html>";
        String fullHtml = baseHtml.replace("_CONTENT_", html);

        // load data directly, not from file as before
        this.loadData(fullHtml, "text/html", "UTF-8");

        // call success listener
        eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Loaded, null);
    }
}
