package tv.superawesome.lib.sawebplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.sawebplayer.utilities.SAWebUtils;
import tv.superawesome.lib.sawebplayer.mraid.SAMRAIDCommand;

public class SAExpandedWebPlayer extends SAWebPlayer {

    protected Button closeButton = null;

    public SAExpandedWebPlayer(Context context) {
        this(context, null, 0);
    }

    public SAExpandedWebPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SAExpandedWebPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        super.holder.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void expandCommand(final String url) {
        // can't expand
    }

    @Override
    public void resizeCommand() {
        // can't resize
    }

    @Override
    public void setResizePropertiesCommand(int width, int height, int offsetX, int offestY, SAMRAIDCommand.CustomClosePosition customClosePosition, boolean allowOffscreen) {
        // can't resize
    }

    @Override
    public void closeCommand() {
        ViewParent parent = getParent();
        if (parent != null && parent instanceof FrameLayout) {
            ((FrameLayout)parent).removeView(this);
        }
    }

    @Override
    public void setup() {
        super.setup();

        // do this only for the Resized / Expanded web player
        if (mraid.getExpandedCustomClosePosition() != SAMRAIDCommand.CustomClosePosition.Unavailable) {
            RelativeLayout closeButtonHolder = new RelativeLayout(getContext());
            closeButtonHolder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            holder.addView(closeButtonHolder);

            closeButton = new Button(getContext());
            int btnWidth = (int) SAWebUtils.dipToPixels(getContext(), 50);
            int btnHeight = (int) SAWebUtils.dipToPixels(getContext(), 50);
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

            closeButton.setBackgroundColor(Color.RED);
            closeButton.setText("X");
            closeButton.setTextColor(Color.WHITE);
            closeButton.setLayoutParams(btnParams);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeCommand();
                }
            });
            closeButtonHolder.addView(closeButton);
        }
    }

    @Override
    public boolean onConsoleMessage(String message) {
        if (message.startsWith("SAMRAID_EXT")) {

            String msg = message.substring(5); // strip off prefix

            mraid.setHasMRAID(msg.contains("mraid.js"));

            if (mraid.hasMRAID()) {

                SAUtils.SASize screen = SAUtils.getRealScreenSize((Activity)getContext(), false);

                mraid.setPlacementInline();
                mraid.setReady();
                mraid.setViewableTrue();
                mraid.setScreenSize(screen.width, screen.height);
                mraid.setMaxSize(screen.width, screen.height);
                mraid.setCurrentPosition(contentWidth, contentHeight);
                mraid.setDefaultPosition(contentWidth, contentHeight);

                float scale = SAUtils.getScaleFactor((Activity)getContext());
                int width = (int) (screen.width / scale);
                int height = (int) (screen.height / scale);
                mraid.setStateToExpanded();
                mraid.setCurrentPosition(width, height - 1);

                mraid.setReady();
                mraid.setViewableTrue();
            }
        }

        return true;
    }
}
