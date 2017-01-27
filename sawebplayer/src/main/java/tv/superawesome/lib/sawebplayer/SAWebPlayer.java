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
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

public class SAWebPlayer extends Fragment implements SAWebPlayerEventInterface {

    private FrameLayout holder = null;
    private SAWebView webView = null;
    private int contentWidth = 0;
    private int contentHeight = 0;

    // interface objects used for the web player callback mechanism
    private SAWebPlayerEventInterface eventListener;

    public SAWebPlayer() {
        //setup the default event listener, so as it's never null
        eventListener = new SAWebPlayerEventInterface() {@Override public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event, String destination) {}};
    }

    public SAWebPlayer(int width, int height) {
        this();
        contentWidth = width;
        contentHeight = height;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        if (holder == null) {
            holder = new FrameLayout(getActivity());
            holder.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            holder.setClipChildren(false);
            holder.setClipToPadding(false);

            webView = new SAWebView(getActivity());
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.eventListener = this;
            saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Prepared, null);
        }
        else {
            holder.removeView(webView);
            if (container != null) {
                container.removeView(holder);
            }
        }

        webView.setLayoutParams(new ViewGroup.LayoutParams(contentWidth, contentHeight));
        holder.addView(webView);

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

        return holder;
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

    public void loadHTML (String html) {
        if (webView != null) {
            webView.loadHTML(html);
        }
    }

    public FrameLayout getHolder () {
        return holder;
    }

    public SAWebView getWebView () {
        return webView;
    }

    @Override
    public void onStart() {
        super.onStart();
        saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Started, null);
    }

    /**
     * Setter method that adds an actual event listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setEventListener(SAWebPlayerEventInterface listener) {
        eventListener = listener != null ? listener : eventListener;
    }

    @Override
    public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event, String destination) {
        eventListener.saWebPlayerDidReceiveEvent(event, destination);
    }
}

class SAWebView extends WebView {

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