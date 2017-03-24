package tv.superawesome.lib.sawebplayer;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

public class SAWebPlayer extends Fragment {

    // boolean holding whether the web view has finished loading or not
    private boolean finishedLoading = false;

    // private variables for the web player
    private FrameLayout holder = null;
    private WebView     webView = null;
    private int         contentWidth = 0;
    private int         contentHeight = 0;

    // interface objects used for the web player callback mechanism
    private SAWebPlayerEventInterface eventListener;

    /**
     * Constructor
     */
    public SAWebPlayer() {
        //setup the default event listener, so as it's never null
        eventListener = new SAWebPlayerEventInterface() {@Override public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event, String destination) {}};
    }

    /**
     * Main method that sets the desired content size to be displayed
     *
     * @param width  width of the content size
     * @param height height of the content size
     */
    public void setContentSize (int width, int height) {
        contentWidth = width;
        contentHeight = height;
    }

    /**
     * Overridden fragment "onCreate" method. This just specifies the fragment to be retain.
     * This will be called only once.
     *
     * @param savedInstanceState a saved instance state; can be null.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Overridden fragment "onCreateView" method in which all the subviews needed by the fragment
     * are created or laid out. This will be called every time the configuration of the app
     * changes (e.g. on orientation).
     *
     * @param inflater           current inflater
     * @param container          view that contains the fragment
     * @param savedInstanceState previous saved state
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        if (holder == null) {
            holder = new FrameLayout(getActivity());
            holder.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            holder.setClipChildren(false);
            holder.setClipToPadding(false);

            webView = new WebView(getActivity());
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setInitialScale(100);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
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
            eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Prepared, null);
        }
        else {
            holder.removeView(webView);
            if (container != null) {
                container.removeView(holder);
            }
        }

        webView.setLayoutParams(new ViewGroup.LayoutParams(contentWidth, contentHeight));
        holder.addView(webView);

        if (container != null) {
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

                    // send event
                    eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Layout, null);

                }
            });
        }

        return holder;
    }

    /**
     * Private method that maps a source rect into a bounding rect.
     *
     * @param sourceW   source width
     * @param sourceH   source height
     * @param boundingW bounding width
     * @param boundingH bounding height
     * @return          the resulting correct rect
     */
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

    /**
     * Method that loads the HTML into the underlining web view
     *
     * @param html the html to load
     */
    public void loadHTML (String html) {
        if (webView != null) {

            // if the HTML is null, just return by default and don't do anything
            if (html == null) return;

            // replace HTML template parameters with their corresponding actual values,
            // before loading the final HTML into the web player
            String baseHtml = "<html><header><style>html, body, div { margin: 0px; padding: 0px; width: 100%; height: 100%; }</style></header><body>_CONTENT_</body></html>";
            String fullHtml = baseHtml.replace("_CONTENT_", html);

            // load data directly, not from file as before
            webView.loadData(fullHtml, "text/html", "UTF-8");

            // call success listener
            eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Loaded, null);

            // webView.loadHTML(html);
        }
    }

    /**
     * Getter method for the view holder (frame layout) that acts as first child to the web player
     *
     * @return holder instance
     */
    public FrameLayout getHolder () {
        return holder;
    }

    /**
     * Getter method for the web view
     *
     * @return web view instance
     */
    public WebView getWebView () {
        return webView;
    }

    /**
     * Overridden fragment "onStart" method that sends out an event
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Started, null);
    }

    /**
     * Setter method that adds an actual event listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setEventListener(SAWebPlayerEventInterface listener) {
        eventListener = listener != null ? listener : eventListener;
    }
}