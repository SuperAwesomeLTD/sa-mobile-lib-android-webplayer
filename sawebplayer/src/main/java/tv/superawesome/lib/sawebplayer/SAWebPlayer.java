package tv.superawesome.lib.sawebplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
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
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.sawebplayer.aux.SAWebAux;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAID;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAIDCommand;

public class SAWebPlayer extends Fragment {

    private static final String kEXPANDED_PLAYER = "expandedPlayer";

    /**
     * WebPlayer event enum, containing two main events:
     *  - Web_Start: happens when the web view content is fully loaded
     *  - Web_Error: happens when something prevents the web view from properly loading the content
     */
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
        //setup the default event listener, so as it's never null
        eventListener = new Listener() {@Override public void saWebPlayerDidReceiveEvent(Event event, String destination) {}};
        mraid = new SAMRAID();
    }

    /**
     * Main method that sets the desired content size to be displayed
     *
     * @param width  width of the content size
     * @param height height of the content size
     */
    public void setContentSize (int width, int height) {
        origContentWidth = width;
        origContentHeight = height;
        contentWidth = width;
        contentHeight = height;
    }

    /**
     * Overridden fragment "onCreate" method. This just specifies the fragment to be retain.
     * This will be called only once.
     *
     * @param savedInstanceState a saved instance state; can be null.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Overridden fragment "onCreateView" method in which all the subviews needed by the fragment
     * are created or laid out. This will be called every time the configuration of the app
     * changes (e.g. on orientation).
     *
     * @param inflater           current inflater
     * @param container          view that contains the fragment
     * @param savedInstanceState previous saved state
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        if (holder == null) {

            holder = new FrameLayout(getActivity());
            holder.setLayoutParams(new FrameLayout.LayoutParams(holderWidth, holderHeight));
            holder.setClipChildren(false);
            holder.setBackgroundColor(holderBgColor);
            holder.setClipToPadding(false);

            webView = new SAWebView(getActivity());
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    view.loadUrl("javascript:console.log('MAGIC'+document.getElementsByTagName('html')[0].innerHTML);");

                    finishedLoading = true;
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    if (url.startsWith("mraid://")) {

                        Log.d("SuperAwesome", url);

                        SAMRAIDCommand command = new SAMRAIDCommand(url);
                        switch (command.getCommand()) {
                            case None:
                                break;
                            case Close: {

                                FragmentManager manager = getFragmentManager();
                                if (manager != null) {
                                    SAWebPlayer player = (SAWebPlayer) manager.findFragmentByTag(kEXPANDED_PLAYER);
                                    if (player != null) {
                                        manager.beginTransaction().remove(player).commit();
                                    }
                                }

                                break;
                            }
                            case CreateCalendarEvent:
                                break;
                            case Expand: {

                                String extUrl = command.getParams().get("url");
                                if (extUrl != null) {
                                    extUrl = extUrl.replace("%3A", ":").replace("%2F", "/");
                                }

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
                                expandedPlayer.mraid.setCloseButtonPosition(mraid.getCloseButtonPosition());
                                final String finalExtUrl = extUrl;
                                expandedPlayer.setEventListener(new Listener() {
                                    @Override
                                    public void saWebPlayerDidReceiveEvent(Event event, String destination) {
                                        if (event == SAWebPlayer.Event.Web_Prepared) {
                                            if (finalExtUrl != null) {

                                                (new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        final String contents = SAWebAux.readContentsOfURL(finalExtUrl);
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
                                        }
                                    }
                                });
                                manager.beginTransaction().add(android.R.id.content, expandedPlayer, kEXPANDED_PLAYER).commit();

                                break;
                            }
                            case Open:
                                break;
                            case PlayVideo:
                                break;
                            case Resize: {

                                float postScaleX = (webView.getMeasuredWidth() * webView.getScaleX()) / (float) origContentWidth;
                                float postScaleY = (webView.getMeasuredHeight() * webView.getScaleY()) / (float) origContentHeight;

                                int resizedWidth = (int) (mraid.getExpandedWidth() * postScaleX);
                                int resizedHeight = (int) (mraid.getExpandedHeight() * postScaleY);

                                Log.d("SuperAwesome-X", "Resized " + resizedWidth + ", " + resizedHeight);

                                FragmentManager manager = getFragmentManager();
                                expandedPlayer = new SAWebPlayer();
                                expandedPlayer.isResized = true;
                                expandedPlayer.holderWidth = resizedWidth;
                                expandedPlayer.holderHeight = resizedHeight;
                                expandedPlayer.mraid.setCloseButtonPosition(mraid.getCloseButtonPosition());
                                expandedPlayer.setContentSize(mraid.getExpandedWidth(), mraid.getExpandedHeight());
                                expandedPlayer.setEventListener(new Listener() {
                                    @Override
                                    public void saWebPlayerDidReceiveEvent(Event event, String destination) {
                                        if (event == SAWebPlayer.Event.Web_Prepared) {
                                            expandedPlayer.loadHTML(html);
                                        }
                                    }
                                });
                                manager.beginTransaction().add(android.R.id.content, expandedPlayer, kEXPANDED_PLAYER).commit();

                                break;
                            }
                            case SetOrientationProperties:
                                break;
                            case SetResizeProperties: {
                                mraid.setResizeProperties(command.getParams());
                                break;
                            }
                            case StorePicture:
                                break;
                            case UseCustomClose: {
                                mraid.setCloseButtonPosition(SAMRAID.CloseButtonPosition.NONE);
                                if (expandedPlayer != null && expandedPlayer.closeButton != null) {
                                    expandedPlayer.closeButton.setVisibility(View.GONE);
                                }
                                break;
                            }
                        }
                    }

                    return true;
                }
            });
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    if (consoleMessage.message().startsWith("MAGIC"))  {

                        String msg = consoleMessage.message().substring(5); // strip off prefix

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
                        Log.d("SuperAwesome", consoleMessage.message());
                    }

                    return false;
                }
            });

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
        if ((isExpanded || isResized) && mraid.getCloseButtonPosition() != SAMRAID.CloseButtonPosition.NONE ) {
            RelativeLayout closeButtonHolder = new RelativeLayout(getActivity());
            closeButtonHolder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            webView.addView(closeButtonHolder);

            closeButton = new Button(getActivity());
            float factor = SAUtils.getScaleFactor(getActivity());
            int btnWidth = (int) SAWebAux.dipToPixels(getActivity(), 40);
            int btnHeight = (int) SAWebAux.dipToPixels(getActivity(), 40);
            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(btnWidth, btnHeight);

            switch (mraid.getCloseButtonPosition()) {
                case NONE:
                    break;
                case TOP_LEFT:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    break;
                case TOP_RIGHT:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    break;
                case CENTER:
                    btnParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    break;
                case BOTTOM_LEFT:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                    break;
                case BOTTOM_RIGHT:
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    break;
                case TOP_CENTER:
                    btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                    break;
                case BOTTOM_CENTER:
                    btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    break;
            }

            closeButton.setBackgroundColor(Color.RED);
            closeButton.setText("X");
//            closeButton.setBackgroundColor(isResized ? Color.TRANSPARENT : Color.RED);
//            closeButton.setText(isResized ? "" : "X");
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
            ViewTreeObserver observer = container.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    webView.scale(contentWidth, contentHeight, holder.getMeasuredWidth(), holder.getMeasuredHeight());

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

                        // get up & down difference
                        int downDiff = size.height - wwYMiddle;
                        int upDiff = wwYMiddle;

                        int downMax = Math.min(downDiff, resizedHalfHeight);
                        int upMax = Math.min(upDiff, resizedHalfHeight);
                        upMax += downMax < resizedHalfHeight ? (resizedHalfHeight - downDiff) : 0;

                        int resizedY = wwYMiddle - upMax;
                        resizedY -= wwYMiddle > size.height / 2.0F ? statusBarHeight : 0;

                        //
                        // start calculating the actual X position of the expanded player
                        int resizedWidth = (int) (mraid.getExpandedWidth() * postScaleX);
                        int resizedHalfWidth = (int) (resizedWidth / 2.0F);

                        int wwXMiddle = loc[0] + (int) ((webView.getMeasuredWidth() * webView.getScaleX()) / 2.0F);

                        // get left & right difference
                        int rightDiff = size.width - wwXMiddle;
                        int leftDiff = wwXMiddle;

                        Log.d("SuperAwesome-X", "Screen: " + size.width + "; Middle: " + wwXMiddle + " RDif: " + rightDiff + " , " + leftDiff);

                        int rightMax = Math.min(rightDiff, resizedHalfWidth);
                        int leftMax = Math.min(leftDiff, resizedHalfWidth);
                        leftMax += rightMax < resizedHalfWidth ? (resizedHalfWidth - rightDiff) : 0;

                        int resizedX = wwXMiddle - leftMax;

                        Log.d("SuperAwesome-X", "LMax: " + leftMax + ", RMax: " + rightMax + " FinalX: " + resizedX + " vs " + loc[0]);

                        expandedPlayer.holder.setTranslationX(resizedX);
                        expandedPlayer.holder.setTranslationY(resizedY);
                    }

                    eventListener.saWebPlayerDidReceiveEvent(Event.Web_Layout, null);

                }
            });
        }

        return holder;
    }

    /**
     * Method that loads the HTML into the underlining web view
     *
     * @param html the html to load
     */
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

    public void loadURL (String url) {
        if (webView != null) {
            mraid.injectMRAID();
            webView.loadUrl(url);
            eventListener.saWebPlayerDidReceiveEvent(Event.Web_Loaded, null);
        }
    }

    /**
     * Getter method for the view holder (frame layout) that acts as first child to the web player
     *
     * @return holder instance
     */
    public FrameLayout getHolder () {
        return holder;
    }

    /**
     * Getter method for the web view
     *
     * @return web view instance
     */
    public WebView getWebView () {
        return webView;
    }

    /**
     * Overridden fragment "onStart" method that sends out an event
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Started, null);
    }

    /**
     * Setter method that adds an actual event listener to the web player
     *
     * @param listener library user listener implementation
     */
    public void setEventListener(Listener listener) {
        eventListener = listener != null ? listener : eventListener;
    }

    /**
     * Interface that is used by the SAWebPlayer to send back web view events to the library users
     */
    public interface Listener {

        /**
         * Main method of the interface
         *
         * @param event         the event that just happened
         * @param destination   the destination URL
         */
        void saWebPlayerDidReceiveEvent (Event event, String destination);
    }
}