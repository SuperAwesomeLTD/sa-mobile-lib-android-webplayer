package tv.superawesome.lib.sawebplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.sawebplayer.utilities.SAWebUtils;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAID;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAIDCommand;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAIDVideoActivity;

import static android.os.Build.VERSION_CODES.KITKAT;

public class SAWebPlayer extends RelativeLayout implements
        SAWebClient.Listener,
        SAWebChromeClient.Listener,
        ViewTreeObserver.OnGlobalLayoutListener,
        SAMRAIDCommand.Listener {

    public enum Event {
        Web_Prepared,
        Web_Loaded,
        Web_Error,
        Web_Click,
        Web_Started,
        Web_Layout
    }

    // boolean holding whether the web view has finished loading or not
    private boolean             finishedLoading = false;

    // private variables for the web player
    protected FrameLayout       holder = null;
    protected SAWebView         webView = null;
    private SAExpandedWebPlayer expandedWebPlayer = null;
    private SAResizedWebPlayer  resizedWebPlayer = null;

    protected int               origContentWidth = 0;
    protected int               origContentHeight = 0;
    protected int               contentWidth = 0;
    protected int               contentHeight = 0;

    // interface objects used for the web player callback mechanism
    protected Listener          eventListener;

    // mraid instance
    protected SAMRAID           mraid;
    private String              html;
    protected int               holderWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    protected int               holderHeight = ViewGroup.LayoutParams.MATCH_PARENT;
    protected int               holderBgColor = Color.TRANSPARENT;

    public SAWebPlayer(Context context) {
        this(context, null, 0);
    }

    public SAWebPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SAWebPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        eventListener = new Listener() {@Override public void saWebPlayerDidReceiveEvent(Event event, String destination) {}};
        mraid = new SAMRAID();

        holder = new FrameLayout(context);
        holder.setClipChildren(false);
        holder.setBackgroundColor(holderBgColor);
        holder.setClipToPadding(false);

        webView = new SAWebView(context);
        webView.setWebViewClient(new SAWebClient(this));
        webView.setWebChromeClient(new SAWebChromeClient(this));

        this.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void setContentSize (int width, int height) {
        origContentWidth = width;
        origContentHeight = height;
        contentWidth = width;
        contentHeight = height;
    }

    public void setup () {
        webView.setLayoutParams(new ViewGroup.LayoutParams(contentWidth, contentHeight));
        holder.setLayoutParams(new FrameLayout.LayoutParams(holderWidth, holderHeight));

        holder.addView(webView);
        this.addView(holder);

        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Prepared, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SAWebClient implementation
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPageStarted(WebView view, String url) {
        if (shouldOverrideUrlLoading(view, url)) {
            view.stopLoading();
        }
    }

    @Override
    public void onPageFinished(WebView view) {
        view.loadUrl("javascript:console.log('SAMRAID_EXT'+document.getElementsByTagName('html')[0].innerHTML);");
        finishedLoading = true;
        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Started, null);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {

        SAMRAIDCommand command = new SAMRAIDCommand();
        boolean isMraid = command.isMRAIDCommand(url);

        if (isMraid) {

            command.setListener(this);
            command.getQuery(url);

            return false;
        } else {

            if (finishedLoading) {
                eventListener.saWebPlayerDidReceiveEvent(Event.Web_Click, url);
                return true;
            }
            else {
                return false;
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MRAID Commands
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void closeCommand() {
        // do nothing in here
    }

    @Override
    public void expandCommand(final String url) {

        SAUtils.SASize size = SAUtils.getRealScreenSize((Activity)getContext(), false);
        float scale = SAUtils.getScaleFactor((Activity)getContext());
        boolean oldV = Build.VERSION.SDK_INT < KITKAT;
        final int width = oldV ? size.width : (int) (size.width / scale),
                height = oldV ? size.height : (int) (size.height / scale);

        expandedWebPlayer = new SAExpandedWebPlayer(getContext());
        expandedWebPlayer.setContentSize(width, height);
        expandedWebPlayer.setEventListener(eventListener);
        expandedWebPlayer.holderBgColor = Color.BLACK;
        expandedWebPlayer.mraid.setExpandedCustomClosePosition(mraid.getExpandedCustomClosePosition());
        ((FrameLayout)((Activity)getContext()).findViewById(android.R.id.content)).addView(expandedWebPlayer);
        expandedWebPlayer.setup();

        if (url != null) {
            SAWebUtils.loadContentsOfURL(getContext(), url, new SAWebUtils.Listener() {
                @Override
                public void didLoadContent(String content) {
                    if (content != null) {
                        expandedWebPlayer.loadHTML(null, content);
                    } else {
                        expandedWebPlayer.loadHTML(null, html);
                    }
                }
            });
        } else {
            expandedWebPlayer.loadHTML(null, html);
        }
    }

    @Override
    public void resizeCommand() {

        float postScaleX = (webView.getMeasuredWidth() * webView.getScaleX()) / (float) origContentWidth;
        float postScaleY = (webView.getMeasuredHeight() * webView.getScaleY()) / (float) origContentHeight;

        int resizedWidth = (int) (mraid.getExpandedWidth() * postScaleX);
        int resizedHeight = (int) (mraid.getExpandedHeight() * postScaleY);

        resizedWebPlayer = new SAResizedWebPlayer(getContext());
        resizedWebPlayer.setContentSize(mraid.getExpandedWidth(), mraid.getExpandedHeight());
        resizedWebPlayer.setEventListener(eventListener);
        resizedWebPlayer.holderWidth = resizedWidth;
        resizedWebPlayer.holderHeight = resizedHeight;
        resizedWebPlayer.mraid.setExpandedCustomClosePosition(mraid.getExpandedCustomClosePosition());
        resizedWebPlayer.parentWebView = webView;
        ((FrameLayout)((Activity)getContext()).findViewById(android.R.id.content)).addView(resizedWebPlayer);
        resizedWebPlayer.setup();
        resizedWebPlayer.loadHTML(null, html);
    }

    @Override
    public void useCustomCloseCommand(boolean useCustomClose) {
        mraid.setExpandedCustomClosePosition(SAMRAIDCommand.CustomClosePosition.Unavailable);
    }

    @Override
    public void createCalendarEventCommand(String eventJSON) {
        // do nothing
    }

    @Override
    public void openCommand(String url) {
        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Click, url);
    }

    @Override
    public void playVideoCommand(String url) {
        if (url != null) {
            Intent intent = new Intent(getContext(), SAMRAIDVideoActivity.class);
            intent.putExtra("link_url", url);
            getContext().startActivity(intent);
        }
    }

    @Override
    public void storePictureCommand(String url) {
        // do nothing
    }

    @Override
    public void setOrientationPropertiesCommand(boolean allowOrientationChange, boolean forceOrientation) {
        // do nothing
    }

    @Override
    public void setResizePropertiesCommand(int width, int height, int offsetX, int offestY, SAMRAIDCommand.CustomClosePosition customClosePosition, boolean allowOffscreen) {
        mraid.setResizeProperties(width, height, offsetX, offestY, customClosePosition, allowOffscreen);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Web Chrome client methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onConsoleMessage(String message) {
        if (message.startsWith("SAMRAID_EXT"))  {

            String msg = message.substring(5); // strip off prefix

            mraid.setHasMRAID(msg.contains("mraid.js"));

            if (mraid.hasMRAID()) {

                Log.d("SuperAwesome", "MRAID SHOULD BE PRESENT");

                SAUtils.SASize screen = SAUtils.getRealScreenSize((Activity)getContext(), false);

                mraid.setPlacementInline();
                mraid.setReady();
                mraid.setViewableTrue();
                mraid.setScreenSize(screen.width, screen.height);
                mraid.setMaxSize(screen.width, screen.height);
                mraid.setCurrentPosition(contentWidth, contentHeight);
                mraid.setDefaultPosition(contentWidth, contentHeight);
                mraid.setStateToDefault();
                mraid.setReady();
                mraid.setViewableTrue();

                if (Build.VERSION.SDK_INT < KITKAT) {

                    float scale = SAUtils.getScaleFactor((Activity)getContext());
                    contentWidth = (int) (scale * contentWidth);
                    contentHeight = (int) (scale * contentHeight);

                    ViewGroup.LayoutParams params = webView.getLayoutParams();
                    params.width = contentWidth;
                    params.height = contentHeight;
                    webView.setLayoutParams(params);
                }

            } else {
                Log.d("SuperAwesome", "MRAID SHOULD NOT BE PRESENT");
            }

            return true;
        } else {
            Log.d("SuperAwesome", message);
        }

        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Global Layout of the Container
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onGlobalLayout() {

        webView.scale(contentWidth, contentHeight, holder.getMeasuredWidth(), holder.getMeasuredHeight());
        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Layout, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Useful Web Player methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void loadHTML (String base, String html) {
        if (webView != null) {

            // if the HTML is null, just return by default and don't do anything
            if (html == null) return;

            this.html = html;

            // inject MRAID
            mraid.setWebView(webView);
            mraid.injectMRAID();

            // load data directly, not from file as before
            webView.loadHTML(base, html);

            // call success listener
            eventListener.saWebPlayerDidReceiveEvent(Event.Web_Loaded, null);
        }
    }

    public FrameLayout getHolder () {
        return holder;
    }

    public WebView getWebView () {
        return webView;
    }

    public void setEventListener(Listener l) {
        eventListener = l != null ? l : eventListener;
    }

    public interface Listener {

        void saWebPlayerDidReceiveEvent (Event event, String destination);
    }
}
