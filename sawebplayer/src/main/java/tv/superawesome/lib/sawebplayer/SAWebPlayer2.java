package tv.superawesome.lib.sawebplayer;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SAWebPlayer2 extends Fragment {

    private SAWebView2 webView = null;
    private int contentWidth = 0;
    private int contentHeight = 0;
    private String html = null;

    // interface objects used for the web player callback mechanism
    private SAWebPlayerEventInterface eventListener;
    private SAWebPlayerClickInterface clickListener;

    public SAWebPlayer2 () {
        // empty constructor
    }

    public SAWebPlayer2 (int width, int height, String html) {
        contentWidth = width;
        contentHeight = height;
        this.html = html;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        if (webView == null) {
            webView = new SAWebView2(getActivity());
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setClickListener(clickListener);
            webView.setEventListener(eventListener);
            webView.loadHTML(html);
        } else {
            container.removeView(webView);
        }

        webView.setLayoutParams(new ViewGroup.LayoutParams(contentWidth, contentHeight));

        ViewTreeObserver observer = container.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // create sizes for the scaled view
                Rect sizes = mapSourceSizeIntoBoundingSize(contentWidth, contentHeight, container.getMeasuredWidth(), container.getMeasuredHeight());

                webView.setPivotX(0);
                webView.setPivotY(0);
                webView.setScaleX(sizes.right / (float) (contentWidth));
                webView.setScaleY(sizes.bottom / (float) (contentHeight));
                webView.setTranslationX(sizes.left);
                webView.setTranslationY(sizes.top);
            }
        });

        return webView;
    }

    private Rect mapSourceSizeIntoBoundingSize(float sourceW, float sourceH, float boundingW, float boundingH) {
        float sourceRatio = sourceW / sourceH;
        float boundingRatio = boundingW / boundingH;
        float X, Y, W, H;
        if(sourceRatio > boundingRatio) {
            W = boundingW;
            H = W / sourceRatio;
            X = 0.0F;
            Y = (boundingH - H) / 2.0F;
        } else {
            H = boundingH;
            W = sourceRatio * H;
            Y = 0.0F;
            X = (boundingW - W) / 2.0F;
        }

        return new Rect((int)X, (int)Y, (int)W, (int)H);
    }

    public SAWebView2 getWebView () {
        return webView;
    }

    /**
     * Setter method that adds an actual event listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setEventListener(SAWebPlayerEventInterface listener) {
        eventListener = listener;
    }

    /**
     * Setter method that adds an actual click listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setClickListener(SAWebPlayerClickInterface listener) {
        clickListener = listener;
    }
}

class SAWebView2 extends WebView {

    private boolean finishedLoading = false;

    // interface objects used for the web player callback mechanism
    private SAWebPlayerEventInterface eventListener;
    private SAWebPlayerClickInterface clickListener;

    public SAWebView2(Context context) {
        this(context, null, 0);
    }

    public SAWebView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SAWebView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //setup the default event listener, so as it's never null
        eventListener = new SAWebPlayerEventInterface() {@Override public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event) {}};
        // setup the default click listener, so as it's never null
        clickListener = new SAWebPlayerClickInterface() {@Override public void saWebPlayerDidReceiveClick(String url) {}};

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
                Log.d("SuperAwesome", "Click lstn " + clickListener);
                if (finishedLoading) {
                    clickListener.saWebPlayerDidReceiveClick(url);
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
        eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Start);
    }

    /**
     * Setter method that adds an actual event listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setEventListener(SAWebPlayerEventInterface listener) {
        eventListener = listener != null ? listener : eventListener;
    }

    /**
     * Setter method that adds an actual click listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setClickListener(SAWebPlayerClickInterface listener) {
        clickListener = listener != null ? listener : clickListener;
    }
}