package tv.superawesome.lib.sawebplayer;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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