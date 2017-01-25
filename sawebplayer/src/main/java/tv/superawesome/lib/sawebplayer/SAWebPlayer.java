/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
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
import android.view.ViewParent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;

public class SAWebPlayer extends Fragment {

    // private interface objects used for the web player callback mechanism
    private SAWebPlayerEventInterface eventListener;
    private SAWebPlayerClickInterface clickListener;

    // only member variable, a SAWebContainer instance
    private SAWebContainer webContainer;

    /**
     * Overridden Fragment "onCreate" method
     *
     * @param savedInstanceState current saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Overridden Fragment "onCreateView" method that returns the view that will be displayed in
     * this fragment
     *
     * @param inflater           current inflater instance
     * @param container          current container
     * @param savedInstanceState current saved instance state
     * @return                   the view to be rendered
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // if it's the first time the Fragment is being created, also create a web container
        // to return as view
        if (webContainer == null) {
            webContainer = new SAWebContainer(getActivity());
            int match = ViewGroup.LayoutParams.MATCH_PARENT;
            webContainer.setLayoutParams(new FrameLayout.LayoutParams(match, match));
        }
        // if the fragment is being recreated, the "webContainer" object should still be there
        // on account of #setRetainInstance(true) in #onCreate, so just remove it from the
        // superview so that when return it's going to be re-added by Android
        else {

            // get parent
            ViewParent parent = webContainer.getParent();

            // try removing the web container from its parent
            if (parent instanceof FrameLayout) {
                ((FrameLayout) parent).removeView(webContainer);
            } else if (parent instanceof RelativeLayout) {
                ((RelativeLayout) parent).removeView(webContainer);
            } else if (parent instanceof LinearLayout) {
                ((LinearLayout) parent).removeView(webContainer);
            }
        }

        // return the web container
        return webContainer;
    }

    /**
     * Method that passes on the expected content width & height to the fragment's only child,
     * the web container object, and sets new layout parameters for it
     *
     * @param width  expected width
     * @param height expected height
     */
    public void setContentSize (int width, int height) {
        // set the size of the content to be rendered
        webContainer.setContentSize(width, height);
        // and set listener to be added to the SAWebView instance, when it's created
        webContainer.setEventListener(eventListener);
        webContainer.setClickListener(clickListener);
    }

    /**
     * This method loads HTML into the web view for ad use. It takes into account the source
     * html string, the needed ad with and height as well as the bounding frame width and height
     *
     * @param html        the html to load
     */
    public void loadHTML(String html) {
        webContainer.loadHTML(html);
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

    /**
     * Getter for the internal web view
     *
     * @return the used web view instance
     */
    public WebView getWebView () {
        return webContainer != null ? webContainer.webView : null;
    }
}

/**
 * Subclass of FrameLayout that will hold a child SAWebView instance and will handle size change
 * events so that the web view is always scaled correctly
 */
class SAWebContainer extends FrameLayout {

    // members for the SAWebContainer
    public SAWebView webView;
    private boolean forceResize = false;

    /**
     * Main constructor
     *
     * @param context current context (activity or fragment)
     */
    public SAWebContainer(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor with context and attribute sets
     *
     * @param context current context (activity or fragment)
     * @param attrs   attributes for the web player
     */
    public SAWebContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Main constructor with context, attribute sets and default style
     *
     * @param context   current context (activity or fragment)
     * @param attrs     attributes for the web player
     * @param defStyle  default style (usually 0)
     */
    public SAWebContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // setup web container view attributes
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setClipChildren(false);
        this.setClipToPadding(false);
    }

    /**
     * Aux method that maps a "box" width given width & height into another "bounding box", and
     * spits out the size of the rectangle that should do the job
     *
     * @param sourceW    source box width
     * @param sourceH    source box height
     * @param boundingW  bounding box width
     * @param boundingH  bounding box height
     * @return           a rectangle with X, Y, W, H that will map the source coords into the
     *                   bounding box
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
     * Overridden onLayout method
     *
     * @param changed   whether the current layout has changed
     * @param left      X pos
     * @param top       Y pos
     * @param right     Width
     * @param bottom    Height
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed && !forceResize) return;

        forceResize = false;

        if (webView != null) {

            int contentWidth = webView.getMeasuredWidth();
            int contentHeight = webView.getMeasuredHeight();

            // create sizes for the scaled view
            Rect sizes = mapSourceSizeIntoBoundingSize(contentWidth, contentHeight, right, bottom);
            int scaledX = sizes.left;
            int scaledY = sizes.top;
            int scaledW = sizes.right;
            int scaledH = sizes.bottom;

            float resultX = scaledW / (float) (contentWidth);
            float resultY = scaledH / (float) (contentHeight);

            webView.setPivotX(0);
            webView.setPivotY(0);
            webView.setScaleX(resultX);
            webView.setScaleY(resultY);
            webView.setTranslationX(scaledX);
            webView.setTranslationY(scaledY);
        }
    }

    /**
     * Setter method for the size of the Web Content that's going to be displayed
     *
     * @param width     expected content width
     * @param height    expected content height
     */
    void setContentSize (int width, int height) {

        // force resize of the
        forceResize = true;

        // remove previous web view
        if (webView != null) {
            this.removeView(webView);
            webView = null;
        }

        // recreate web view
        webView = new SAWebView(getContext());
        webView.clearCache(true);
        addView(webView);

        // make it as big as the width & height of the content
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        webView.setLayoutParams(layoutParams);
    }

    /**
     * This method loads HTML into the web view for ad use. It takes into account the source
     * html string, the needed ad with and height as well as the bounding frame width and height
     *
     * @param html        the html to load
     */
    public void loadHTML(String html) {
        if (webView != null) {
            webView.loadHTML(html);
        }
    }

    /**
     * Setter method that adds an actual event listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setEventListener(SAWebPlayerEventInterface listener) {
        if (webView != null) {
            webView.setEventListener(listener);
        }
    }

    /**
     * Setter method that adds an actual click listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setClickListener(SAWebPlayerClickInterface listener) {
        if (webView != null) {
            webView.setClickListener(listener);
        }
    }
}

/**
 * Class that abstracts away the details of loading HTML into an Android WebView
 */
class SAWebView extends WebView {

    private boolean finishedLoading = false;

    // private interface objects used for the web player callback mechanism
    private SAWebPlayerEventInterface eventListener;
    private SAWebPlayerClickInterface clickListener;

    /**
     * Main constructor
     *
     * @param context current context (activity or fragment)
     */
    public SAWebView(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor with context and attribute sets
     *
     * @param context current context (activity or fragment)
     * @param attrs   attributes for the web player
     */
    public SAWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Main constructor with context, attribute sets and default style
     *
     * @param context   current context (activity or fragment)
     * @param attrs     attributes for the web player
     * @param defStyle  default style (usually 0)
     */
    public SAWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

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
                if (finishedLoading) {
                    clickListener.saWebPlayerDidReceiveClick(url);
                    return true;
                } else {
                    return false;
                }
            }

        });
    }

    /**
     * This method loads HTML into the web view for ad use. It takes into account the source
     * html string, the needed ad with and height as well as the bounding frame width and height
     *
     * @param html the html to load
     */
    public void loadHTML(String html) {
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
     * Overridden onLayout method
     *
     * @param changed   whether the current layout has changed
     * @param left      X pos
     * @param top       Y pos
     * @param right     Width
     * @param bottom    Height
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
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
