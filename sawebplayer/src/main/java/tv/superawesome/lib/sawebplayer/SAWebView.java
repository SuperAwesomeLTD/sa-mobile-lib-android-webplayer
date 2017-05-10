package tv.superawesome.lib.sawebplayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
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
        setInitialScale(100);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setFocusableInTouchMode(false);
        getSettings().setJavaScriptEnabled(true);
    }

    public void scale (int iW, int iH, int bW, int bH) {

        // create sizes for the scaled view
        Rect sizes = mapSourceSizeIntoBoundingSize(iW, iH, bW, bH);

        setPivotX(0);
        setPivotY(0);
        setScaleX((sizes.right / (float) (iW)));
        setScaleY((sizes.bottom / (float) (iH)));
        setTranslationX(sizes.left);
        setTranslationY(sizes.top);
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

        String baseHtml = "<html><header><style>html, body, div { margin: 0px; padding: 0px; } html, body { width: 100%; height: 100%; }</style></header><body>_CONTENT_</body></html>";
        String fullHtml = baseHtml.replace("_CONTENT_", html);
        loadDataWithBaseURL(base, fullHtml, "text/html", "UTF-8", null);

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
}
