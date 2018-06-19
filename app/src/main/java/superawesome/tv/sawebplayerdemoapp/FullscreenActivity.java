package superawesome.tv.sawebplayerdemoapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.sawebplayer.SAWebPlayer;

/**
 * Created by gabriel.coman on 18/06/2018.
 */

public class FullscreenActivity extends Activity {

    private String ad2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ad2 = MainActivity.readFromFile(this, R.raw.ad2);

        RelativeLayout parent = new RelativeLayout(this);
        parent.setId(SAUtils.randomNumberBetween(1000000, 1500000));
        parent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        FrameLayout banner = new FrameLayout(this);
        banner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        SAWebPlayer webPlayer;

        webPlayer = new SAWebPlayer(this);
        final SAWebPlayer finalWebPlayer = webPlayer;
        webPlayer.setEventListener(new SAWebPlayer.Listener() {
            @Override
            public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                switch (event) {
                    case Web_Prepared: {
                        finalWebPlayer.loadHTML("https://s3-eu-west-1.amazonaws.com", ad2);
                        break;
                    }
                    case Web_Layout: {
                        Log.d("SuperAwesome", "Player scale is " + finalWebPlayer.getScaleX() + " | " + finalWebPlayer.getScaleY());
                        break;
                    }
                }
                Log.d("SuperAwesome/WebView", "Event is " + event + " | " + destination);
            }
        });
        webPlayer.setBackgroundColor(Color.BLUE);
        webPlayer.setContentSize(320, 480);
        webPlayer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        parent.addView(banner);
        banner.addView(webPlayer);
        webPlayer.setup();

        setContentView(parent);
    }
}
