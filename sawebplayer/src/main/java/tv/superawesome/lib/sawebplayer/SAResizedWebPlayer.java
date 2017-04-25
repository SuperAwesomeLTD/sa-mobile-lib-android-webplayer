package tv.superawesome.lib.sawebplayer;

import android.graphics.Rect;
import android.os.Build;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

import tv.superawesome.lib.sautils.SAUtils;

public class SAResizedWebPlayer extends SAExpandedWebPlayer {

    public WebView parentWebView;
    public SAWebPlayer parent;

    @Override
    public boolean onConsoleMessage(String message) {
        if (message.startsWith("SAMRAID_EXT")) {

            String msg = message.substring(5); // strip off prefix

            mraid.setHasMRAID(msg.contains("mraid.js"));

            if (mraid.hasMRAID()) {

                SAUtils.SASize screen = SAUtils.getRealScreenSize(getActivity(), false);

                mraid.setPlacementInline();
                mraid.setReady();
                mraid.setViewableTrue();
                mraid.setScreenSize(screen.width, screen.height);
                mraid.setMaxSize(screen.width, screen.height);
                mraid.setCurrentPosition(contentWidth, contentHeight);
                mraid.setDefaultPosition(contentWidth, contentHeight);
                mraid.setReady();
                mraid.setStateToResized();
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
            }
        }

        return true;
    }

    @Override
    public void onGlobalLayout() {

        if (getActivity() != null) {

            SAUtils.SASize screen = SAUtils.getRealScreenSize(getActivity(), false);
            Rect rectangle = new Rect();
            Window window = getActivity().getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int bottom = screen.height - rectangle.bottom;

            webView.resize(holder.getMeasuredWidth(), holder.getMeasuredHeight());

            float wwWidth = webView.getMeasuredWidth() * webView.getScaleX(), wwHeight = webView.getMeasuredHeight() * webView.getScaleY();

            int ploc[] = {0, 0};
            parentWebView.getLocationInWindow(ploc);

            int pX = ploc[0], pY = ploc[1];
            float pWidth = parentWebView.getMeasuredWidth() * parentWebView.getScaleX();
            float pHeight = parentWebView.getMeasuredHeight() * parentWebView.getScaleY();

            float pCenterX = pX + pWidth/2, pCenterY = pY + pHeight/2;

            float tX = pCenterX - wwWidth/2;
            tX = tX < 0 ? 0 : tX > screen.width - wwHeight ? screen.width - wwHeight : tX;

            float tY = pCenterY - wwHeight/2;
            tY = tY < 0 ? 0 : tY > (screen.height - bottom) - wwHeight ? (screen.height - bottom) - wwHeight : tY;

            holder.setTranslationX(tX);
            holder.setTranslationY(tY);

        }

        eventListener.saWebPlayerDidReceiveEvent(Event.Web_Layout, null);
    }
}
