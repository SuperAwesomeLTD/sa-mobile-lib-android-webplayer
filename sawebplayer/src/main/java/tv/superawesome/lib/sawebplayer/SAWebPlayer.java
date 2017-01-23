/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sawebplayer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SAWebPlayer extends Fragment {

    // only mmeber variable, a SAWebContainer instance
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

        // get activity
        Activity activity = getActivity();

        // create new layout params
        int match = ViewGroup.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams layoutParams;
        layoutParams = new FrameLayout.LayoutParams(match, match);

        if (webContainer == null) {
            webContainer = new SAWebContainer(activity);
            webContainer.setLayoutParams(layoutParams);
        }
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

        // create new layout params
        int match = ViewGroup.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams layoutParams;
        layoutParams = new FrameLayout.LayoutParams(match, match);

        // force update layout params
        webContainer.setLayoutParams(layoutParams);
    }

    /**
     * Setter method that adds an actual event listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setEventListener(SAWebPlayerEventInterface listener) {
        webContainer.webView.setEventListener(listener);
    }

    /**
     * Setter method that adds an actual click listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setClickListener(SAWebPlayerClickInterface listener) {
        webContainer.webView.setClickListener(listener);
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
}

/**
 * Subclass of FrameLayout that will hold a child SAWebView instance and will handle size change
 * events so that the web view is always scaled correctly
 */
class SAWebContainer extends FrameLayout {

    // members for the SAWebContainer
    public SAWebView webView;
    private int      contentWidth = 0;
    private int      contentHeight = 0;
    private boolean  loadedOnce = false;

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

        // create the web view
        webView = new SAWebView(context);
        addView(webView);
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
     * Overridden method from FrameLayout that notifies this object when it's size has changed
     * (usually because of a rotation or such).
     * If that happens then it's the method's job to recalculate the new scale to be applied
     * on the child webview and position it so that it fits perfectly
     *
     * @param w     new width
     * @param h     new height
     * @param oldw  old width
     * @param oldh  old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // create sizes for the scaled view
        Rect sizes = mapSourceSizeIntoBoundingSize(contentWidth, contentHeight, w, h);
        int scaledX = sizes.left;
        int scaledY = sizes.top;
        int scaledW = sizes.right;
        int scaledH = sizes.bottom;

        float resultX = scaledW / (float) (contentWidth);
        float resultY = scaledH / (float) (contentHeight);

        // create size for the web view
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(contentWidth, contentHeight);
        layoutParams.setMargins(scaledX, scaledY, 0, 0);
        webView.setLayoutParams(layoutParams);

        webView.setPivotX(0);
        webView.setPivotY(0);
        webView.setScaleX(resultX);
        webView.setScaleY(resultY);
    }

    /**
     * Setter method for the size of the Web Content that's going to be displayed
     *
     * @param width     expected content width
     * @param height    expected content height
     */
    void setContentSize (int width, int height) {
        contentWidth = width;
        contentHeight = height;
    }

    /**
     * This method loads HTML into the web view for ad use. It takes into account the source
     * html string, the needed ad with and height as well as the bounding frame width and height
     *
     * @param html        the html to load
     */
    public void loadHTML(String html) {
        if (loadedOnce) return;
        loadedOnce = true;
        webView.loadHTML(html);
    }
}

/**
 * Class that abstracts away the details of loading HTML into an Android WebView
 */
class SAWebView extends WebView {

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
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("file:///")) {
                    return false;
                } else {
                    clickListener.saWebPlayerDidReceiveClick(url);
                    return true;
                }
            }

        });
    }

    /**
     * This method loads HTML into the web view for ad use. It takes into account the source
     * html string, the needed ad with and height as well as the bounding frame width and height
     *
     * @param html        the html to load
     */
    public void loadHTML(String html) {
        // replace HTML template parameters with their corresponding actual values,
        // before loading the final HTML into the web player
        String baseHtml = "<html><header><style>html, body, div { margin: 0px; padding: 0px; overflow: hidden; }</style></header><body>_CONTENT_</body></html>";
        String fullHtml = baseHtml.replace("_CONTENT_", html);

        // get the context
        Context context = this.getContext();

        // start creating a temporary file
        File path = context.getFilesDir();
        File file = new File(path, "tmpHTML.html");

        // write to the tmp file
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file);
            stream.write(fullHtml.getBytes());
            stream.close();

            // load HTML data from a file
            this.loadUrl("file://" + file.getAbsolutePath());

            // call success listener
            eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Start);

        } catch (Exception e) {
            // send error event here
            eventListener.saWebPlayerDidReceiveEvent(SAWebPlayerEvent.Web_Error);
        }
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
