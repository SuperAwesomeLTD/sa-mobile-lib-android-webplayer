package tv.superawesome.lib.sawebplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tv.superawesome.lib.sautils.SAUtils;

/**
 * Created by gabriel.coman on 30/12/15.
 */
public class SAWebPlayer extends WebView {

    /** the internal listener */
    private SAWebPlayerInterface listener;
    private float scaleFactor = 0;

    /** Constructors */
    public SAWebPlayer(Context context) {
        super(context);
    }

    public SAWebPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SAWebPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /** enable JS */
        this.getSettings().setJavaScriptEnabled(true);

        /** get current scale factor */
        scaleFactor = SAUtils.getScaleFactor((Activity)context);

        /** setup a custom WebView client */
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
                    if (listener != null) {
                        listener.saWebViewWillNavigate(url);
                    }

                    return true;
                }
            }

        });

        /** and finally a web chrome client */
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("SuperAwesome", cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
                return true;
            }
        });

    }

    /**
     * Public function that specially loads HTML
     * @param html - the html to load
     * @param adWidth - the current ad Width (e.g. 320px)
     * @param adHeight - the current ad Height (e.g. 50px)
     * @param frameWidth - the actual web player Width (e.g. 1255px)
     * @param frameHeight - the actual web player Height (e.g. 800px)
     */
    public void loadHTML(String html, float adWidth, float adHeight, float frameWidth, float frameHeight){
        /** calc params */
        String _html = html;
        _html = _html.replace("_WIDTH_", "" + (int)(adWidth));
        _html = _html.replace("_HEIGHT_", "" + (int)(adHeight));
        _html = _html.replace("_SCALE_", "" + ((frameWidth/scaleFactor)/adWidth));

        /** get the context */
        Context context = this.getContext();

        /** start creating a temporary file */
        File path = context.getFilesDir();
        File file = new File(path, "tmpHTML.html");

        /** write to the tmp file */
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(_html.getBytes());
            stream.close();
        } catch (IOException e1){
            e1.printStackTrace();

            if (listener != null) {
                listener.saWebViewDidFail();
            }
        }

        /** load HTML data */
        this.loadUrl("file://" + file.getAbsolutePath());

        /** call success listener */
        if (listener != null){
            listener.saWebViewDidLoad();
        }
    }

    /**
     * Assign the listener
     * @param listener an object of SAWebViewListener type
     */
    public void setListener(SAWebPlayerInterface listener) {
        this.listener = listener;
    }
}
