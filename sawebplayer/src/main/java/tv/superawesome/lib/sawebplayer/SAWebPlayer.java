package tv.superawesome.lib.sawebplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.FrameLayout;

import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.sawebplayer.aux.SAWebAux;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAID;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAIDCommand;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAIDVideoActivity;

public class SAWebPlayer extends Fragment implements
        SAWebClient.Listener,
        SAWebChromeClient.Listener,
        ViewTreeObserver.OnGlobalLayoutListener,
        SAMRAIDCommand.Listener
{

    private static final String kEXPANDED_PLAYER = "expandedPlayer";

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
    protected boolean           retainsIntance = true;

    /**
     * Constructor
     */
    public SAWebPlayer() {
        eventListener = new Listener() {@Override public void saWebPlayerDidReceiveEvent(Event event, String destination) {}};
        mraid = new SAMRAID();
    }

    public void disableRetainInstance () {
        retainsIntance = false;
    }

    public void setContentSize (int width, int height) {
        origContentWidth = width;
        origContentHeight = height;
        contentWidth = width;
        contentHeight = height;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden fragment methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(retainsIntance);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Started, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentManager manager = getFragmentManager();
        if (expandedWebPlayer != null) {
            try {
                manager.beginTransaction().remove(expandedWebPlayer).commit();
            } catch (Exception e) {
                // do nothing
            }
        }
        if (resizedWebPlayer != null) {
            try {
                manager.beginTransaction().remove(resizedWebPlayer).commit();
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        if (holder == null) {

            holder = new FrameLayout(getActivity());
            holder.setLayoutParams(new FrameLayout.LayoutParams(holderWidth, holderHeight));
            holder.setClipChildren(false);
            holder.setBackgroundColor(holderBgColor);
            holder.setClipToPadding(false);

            webView = new SAWebView(getActivity());
            webView.setWebViewClient(new SAWebClient(this));
            webView.setWebChromeClient(new SAWebChromeClient(this));

            eventListener.saWebPlayerDidReceiveEvent(Event.Web_Prepared, null);
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
            container.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        return holder;
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
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {

        Log.d("SuperAwesome", "URL is " + url);

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

        FragmentManager manager = getFragmentManager();
        if (manager != null) {
            SAWebPlayer player = (SAWebPlayer) manager.findFragmentByTag(kEXPANDED_PLAYER);
            if (player != null) {
                manager.beginTransaction().remove(player).commit();
            }
        }

    }

    @Override
    public void expandCommand(final String url) {

        SAUtils.SASize size = SAUtils.getRealScreenSize(getActivity(), false);
        float scale = SAUtils.getScaleFactor(getActivity());

        int width, height;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            width = size.width;
            height = size.height;
        } else {
            width = (int) (size.width / scale);
            height = (int) (size.height / scale);
        }

        FragmentManager manager = getFragmentManager();

        expandedWebPlayer = new SAExpandedWebPlayer();
        expandedWebPlayer.retainsIntance = retainsIntance;
        expandedWebPlayer.holderBgColor = Color.BLACK;
        expandedWebPlayer.setContentSize(width, height);
        expandedWebPlayer.mraid.setExpandedCustomClosePosition(mraid.getExpandedCustomClosePosition());
        expandedWebPlayer.setEventListener(new Listener() {
            @Override
            public void saWebPlayerDidReceiveEvent(Event event, String destination) {

                if (event == SAWebPlayer.Event.Web_Prepared) {

                    expandedWebPlayer.setEventListener(eventListener);

                    if (url != null) {

                        (new Thread(new Runnable() {
                            @Override
                            public void run() {

                                final String contents = SAWebAux.readContentsOfURL(url);
                                if (!TextUtils.isEmpty(contents)) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            expandedWebPlayer.loadHTML(null, contents);
                                        }
                                    });
                                }

                            }
                        })).start();

                    } else {
                        expandedWebPlayer.loadHTML(null, html);
                    }
                }
            }
        });
        manager.beginTransaction().add(android.R.id.content, expandedWebPlayer, kEXPANDED_PLAYER).commit();
    }

    @Override
    public void resizeCommand() {

        float postScaleX = (webView.getMeasuredWidth() * webView.getScaleX()) / (float) origContentWidth;
        float postScaleY = (webView.getMeasuredHeight() * webView.getScaleY()) / (float) origContentHeight;

        int resizedWidth = (int) (mraid.getExpandedWidth() * postScaleX);
        int resizedHeight = (int) (mraid.getExpandedHeight() * postScaleY);

        FragmentManager manager = getFragmentManager();

        resizedWebPlayer = new SAResizedWebPlayer();
        resizedWebPlayer.retainsIntance = retainsIntance;
        resizedWebPlayer.holderWidth = resizedWidth;
        resizedWebPlayer.holderHeight = resizedHeight;
        resizedWebPlayer.mraid.setExpandedCustomClosePosition(mraid.getExpandedCustomClosePosition());
        resizedWebPlayer.setContentSize(mraid.getExpandedWidth(), mraid.getExpandedHeight());
        resizedWebPlayer.parentWebView = webView;
        resizedWebPlayer.parent = this;
        resizedWebPlayer.setEventListener(new Listener() {
            @Override
            public void saWebPlayerDidReceiveEvent(Event event, String destination) {
                if (event == SAWebPlayer.Event.Web_Prepared) {
                    resizedWebPlayer.setEventListener(eventListener);
                    resizedWebPlayer.loadHTML(null, html);
                }
            }
        });
        manager.beginTransaction().add(android.R.id.content, resizedWebPlayer, kEXPANDED_PLAYER).commit();
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
            Intent intent = new Intent(getActivity(), SAMRAIDVideoActivity.class);
            intent.putExtra("link_url", url);
            getActivity().startActivity(intent);
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

                SAUtils.SASize screen = SAUtils.getRealScreenSize(getActivity(), false);

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

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

                    float scale = SAUtils.getScaleFactor(getActivity());
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
