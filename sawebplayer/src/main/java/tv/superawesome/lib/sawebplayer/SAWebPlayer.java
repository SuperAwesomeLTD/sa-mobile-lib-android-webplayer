/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sawebplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class that abstracts away the details of loading HTML into an Android WebView
 */
public class SAWebPlayer extends WebView {

    // private interface objects used for the web player callback mechanism
    private SAWebPlayerEventInterface eventListener;
    private SAWebPlayerClickInterface clickListener;

    // current web view scale factor
    private float scaleFactor = 0;

    /**
     * Main constructor
     *
     * @param context current context (activity or fragment)
     */
    public SAWebPlayer(Context context) {
        super(context);
    }

    /**
     * Constructor with context and attribute sets
     *
     * @param context current context (activity or fragment)
     * @param attrs   attributes for the web player
     */
    public SAWebPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Main constructor with context, attribute sets and default style
     *
     * @param context   current context (activity or fragment)
     * @param attrs     attributes for the web player
     * @param defStyle  default style (usually 0)
     */
    public SAWebPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //setup the default event listener, so as it's never null
        eventListener = new SAWebPlayerEventInterface() {@Override public void SAWebPlayerEventHandled(SAWebPlayerEvent event) {}};
        // setup the default click listener, so as it's never null
        clickListener = new SAWebPlayerClickInterface() {@Override public void SAWebPlayerClickHandled(String url) {}};

        // enable javascript
        this.getSettings().setJavaScriptEnabled(true);

        // calculate the current scale factor the web view should use, based on the current
        // device metrics (density, etc)
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        scaleFactor =  (float)metrics.densityDpi / 160.0F;

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
                    clickListener.SAWebPlayerClickHandled(url);
                    return true;
                }
            }

        });

        // and finally a web chrome client to get log messages from the web view
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("SuperAwesome", cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
                return true;
            }
        });

    }

    /**
     * This method loads HTML into the web view for ad use. It takes into account the source
     * html string, the needed ad with and height as well as the bounding frame width and height
     *
     * @param html        the html to load
     * @param adWidth     the current ad Width (e.g. 320px)
     * @param adHeight    the current ad Height (e.g. 50px)
     * @param frameWidth  the actual web player Width (e.g. 1255px)
     * @param frameHeight the actual web player Height (e.g. 800px)
     */
    public void loadHTML(String html, float adWidth, float adHeight, float frameWidth, float frameHeight) {
        // replace HTML template parameters with their corresponding actual values,
        // before loading the final HTML into the web player
        String _html = html;
        _html = _html.replace("_WIDTH_", "" + (int) (adWidth));
        _html = _html.replace("_HEIGHT_", "" + (int) (adHeight));
        _html = _html.replace("_SCALE_", "" + ((frameWidth / scaleFactor) / adWidth));

        // get the context
        Context context = this.getContext();

        // start creating a temporary file
        File path = context.getFilesDir();
        File file = new File(path, "tmpHTML.html");

        // write to the tmp file
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(_html.getBytes());
            stream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            eventListener.SAWebPlayerEventHandled(SAWebPlayerEvent.Web_Error);
        }

        // load HTML data from a file
        this.loadUrl("file://" + file.getAbsolutePath());

        // call success listener
        eventListener.SAWebPlayerEventHandled(SAWebPlayerEvent.Web_Start);
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
