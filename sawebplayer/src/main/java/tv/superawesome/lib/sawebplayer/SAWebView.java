package tv.superawesome.lib.sawebplayer;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

public class SAWebView extends WebView {

    public SAWebView(Context context) {
        this(context, null, 0);
    }

    public SAWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SAWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(Color.TRANSPARENT);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setFocusableInTouchMode(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true);
        }
        getSettings().setJavaScriptEnabled(true);
    }

    public void resize (int toWidth, int toHeight) {

        setPivotX(0);
        setPivotY(0);

        try {
            setScaleX(toWidth / (float) getMeasuredWidth());
        } catch (ArithmeticException e) {
            //
        }
        try {
            setScaleY(toHeight / (float) getMeasuredHeight());
        } catch (ArithmeticException e) {
            //
        }
    }

    public void loadHTML (String base, String html) {

        String baseHtml = "<html><header><meta name='viewport' content='width=device-width'/><style>html, body, div { margin: 0px; padding: 0px; } html, body { width: 100%; height: 100%; }</style></header><body>_CONTENT_</body></html>";
        String fullHtml = baseHtml.replace("_CONTENT_", html);
        loadDataWithBaseURL(base, fullHtml, "text/html", "UTF-8", null);
    }
}
