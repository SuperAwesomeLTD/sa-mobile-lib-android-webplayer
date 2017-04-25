package tv.superawesome.lib.sawebplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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
    private boolean finishedLoading = false;

    // private variables for the web player
    private FrameLayout holder = null;
    private SAWebView   webView = null;
    private SAWebPlayer expandedPlayer = null;
    private Button      closeButton = null;

    private int         origContentWidth = 0;
    private int         origContentHeight = 0;
    private int         contentWidth = 0;
    private int         contentHeight = 0;

    // interface objects used for the web player callback mechanism
    private Listener    eventListener;

    // mraid instance
    private SAMRAID     mraid;
    private String      html;
    private boolean     isExpanded = false;
    private boolean     isResized = false;
    private int         holderWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int         holderHeight = ViewGroup.LayoutParams.MATCH_PARENT;
    private int         holderBgColor = Color.TRANSPARENT;

    /**
     * Constructor
     */
    public SAWebPlayer() {
        eventListener = new Listener() {@Override public void saWebPlayerDidReceiveEvent(Event event, String destination) {}};
        mraid = new SAMRAID();
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
        setRetainInstance(true);
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
        if (expandedPlayer != null) {
            try {
                manager.beginTransaction().remove(expandedPlayer).commit();
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

        // do this only for the Resized / Expanded web player
        if ((isExpanded || isResized) && mraid.getExpandedCustomClosePosition() != SAMRAIDCommand.CustomClosePosition.Unavailable) {
            RelativeLayout closeButtonHolder = new RelativeLayout(getActivity());
            closeButtonHolder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            holder.addView(closeButtonHolder);

            closeButton = new Button(getActivity());
            int btnWidth = (int) SAWebAux.dipToPixels(getActivity(), 50);
            int btnHeight = (int) SAWebAux.dipToPixels(getActivity(), 50);
            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(btnWidth, btnHeight);

            switch (mraid.getExpandedCustomClosePosition()) {
                case Unavailable:
                    break;
                case Top_Left:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    break;
                case Top_Right:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    break;
                case Center:
                    btnParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    break;
                case Bottom_Left:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    break;
                case Bottom_Right:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    break;
                case Top_Center:
                    btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                    break;
                case Bottom_Center:
                    btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    break;
            }

            closeButton.setBackgroundColor(isResized ? Color.TRANSPARENT : Color.RED);
            closeButton.setText(isResized ? "" : "X");
            closeButton.setTextColor(Color.WHITE);
            closeButton.setLayoutParams(btnParams);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FragmentManager manager = getFragmentManager();
                    if (manager != null) {
                        try {
                            manager.beginTransaction().remove(SAWebPlayer.this).commit();
                        } catch (Exception e) {
                            // do nothing
                        }
                    }
                }
            });
            closeButtonHolder.addView(closeButton);
        }

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
        expandedPlayer = new SAWebPlayer();
        expandedPlayer.setContentSize(width, height);
        expandedPlayer.isExpanded = true;
        expandedPlayer.holderBgColor = Color.BLACK;
        expandedPlayer.mraid.setExpandedCustomClosePosition(mraid.getExpandedCustomClosePosition());
        expandedPlayer.setEventListener(new Listener() {
            @Override
            public void saWebPlayerDidReceiveEvent(Event event, String destination) {

                if (event == SAWebPlayer.Event.Web_Prepared) {
                    if (url != null) {

                        (new Thread(new Runnable() {
                            @Override
                            public void run() {

                                final String contents = SAWebAux.readContentsOfURL(url);
                                if (!TextUtils.isEmpty(contents)) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            expandedPlayer.loadHTML(contents);
                                        }
                                    });
                                }

                            }
                        })).start();

                    } else {
                        expandedPlayer.loadHTML(html);
                    }
                } else {
                    eventListener.saWebPlayerDidReceiveEvent(event, destination);
                }
            }
        });
        manager.beginTransaction().add(android.R.id.content, expandedPlayer, kEXPANDED_PLAYER).commit();

    }

    @Override
    public void resizeCommand() {

        float postScaleX = (webView.getMeasuredWidth() * webView.getScaleX()) / (float) origContentWidth;
        float postScaleY = (webView.getMeasuredHeight() * webView.getScaleY()) / (float) origContentHeight;

        int resizedWidth = (int) (mraid.getExpandedWidth() * postScaleX);
        int resizedHeight = (int) (mraid.getExpandedHeight() * postScaleY);

        FragmentManager manager = getFragmentManager();
        expandedPlayer = new SAWebPlayer();
        expandedPlayer.isResized = true;
        expandedPlayer.holderWidth = resizedWidth;
        expandedPlayer.holderHeight = resizedHeight;
        expandedPlayer.mraid.setExpandedCustomClosePosition(mraid.getExpandedCustomClosePosition());
        expandedPlayer.setContentSize(mraid.getExpandedWidth(), mraid.getExpandedHeight());
        expandedPlayer.setEventListener(new Listener() {
            @Override
            public void saWebPlayerDidReceiveEvent(Event event, String destination) {
                if (event == SAWebPlayer.Event.Web_Prepared) {
                    expandedPlayer.loadHTML(html);
                } else {
                    eventListener.saWebPlayerDidReceiveEvent(event, destination);
                }
            }
        });
        manager.beginTransaction().add(android.R.id.content, expandedPlayer, kEXPANDED_PLAYER).commit();

    }

    @Override
    public void useCustomCloseCommand(boolean useCustomClose) {

        mraid.setExpandedCustomClosePosition(SAMRAIDCommand.CustomClosePosition.Unavailable);
        if (expandedPlayer != null && expandedPlayer.closeButton != null) {
            expandedPlayer.closeButton.setVisibility(View.GONE);
        }

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
                if (isResized) {
                    mraid.setStateToResized();
                } else if (isExpanded) {
                    float scale = SAUtils.getScaleFactor(getActivity());
                    int width = (int) (screen.width / scale);
                    int height = (int) (screen.height / scale);
                    mraid.setStateToExpanded();
                    mraid.setCurrentPosition(width, height - 1);
                } else {
                    mraid.setStateToDefault();
                }
                mraid.setReady();
                mraid.setViewableTrue();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && !isExpanded) {

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

        if (isResized) {
            webView.resize(holder.getMeasuredWidth(), holder.getMeasuredHeight());
        } else {
            webView.scale(contentWidth, contentHeight, holder.getMeasuredWidth(), holder.getMeasuredHeight());
        }

        if (getActivity() != null && expandedPlayer != null && expandedPlayer.isResized) {

            //
            // get screen & other measures that stay the same
            SAUtils.SASize size = SAUtils.getRealScreenSize(getActivity(), false);

            float postScaleX = (webView.getMeasuredWidth() * webView.getScaleX()) / (float) origContentWidth;
            float postScaleY = (webView.getMeasuredHeight() * webView.getScaleY()) / (float) origContentHeight;

            int loc[] = {0, 0};
            webView.getLocationInWindow(loc);

            //
            // start calculating the actual Y position of the expanded player
            int resizedHeight = (int) (mraid.getExpandedHeight() * postScaleY);
            int resizedHalfHeight = (int) (resizedHeight / 2.0F);

            Rect rectangle = new Rect();
            Window window = getActivity().getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int statusBarHeight = rectangle.top;

            int wwYMiddle = loc[1] + (int) ((webView.getMeasuredHeight() * webView.getScaleY()) / 2.0F);

            int bottomDif = size.height - wwYMiddle;
            int topDiff = wwYMiddle;

            int downMax = Math.min(bottomDif, resizedHalfHeight);
            int upMax = Math.min(topDiff, resizedHalfHeight);
            upMax += downMax < resizedHalfHeight ? (resizedHalfHeight - bottomDif) : 0;

            int resizedY = wwYMiddle - upMax;
            resizedY -= wwYMiddle > size.height / 2.0F ? statusBarHeight : 0;

            //
            // start calculating the actual X position of the expanded player
            int resizedWidth = (int) (mraid.getExpandedWidth() * postScaleX);
            int resizedHalfWidth = (int) (resizedWidth / 2.0F);

            int wwXMiddle = loc[0] + (int) ((webView.getMeasuredWidth() * webView.getScaleX()) / 2.0F);

            int rightDiff = size.width - wwXMiddle;
            int leftDiff = wwXMiddle;

            int rightMax = Math.min(rightDiff, resizedHalfWidth);
            int leftMax = Math.min(leftDiff, resizedHalfWidth);
            leftMax += rightMax < resizedHalfWidth ? (resizedHalfWidth - rightDiff) : 0;

            int resizedX = wwXMiddle - leftMax;

            expandedPlayer.holder.setTranslationX(resizedX);
            expandedPlayer.holder.setTranslationY(resizedY);
        }

        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Layout, null);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Useful Web Player methods
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void loadHTML (String html) {
        if (webView != null) {

            // if the HTML is null, just return by default and don't do anything
            if (html == null) return;

            this.html = html;

            // inject MRAID
            mraid.setWebView(webView);
            mraid.injectMRAID();

            // load data directly, not from file as before
            webView.loadHTML(html);

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