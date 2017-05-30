package superawesome.tv.sawebplayerdemoapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

import tv.superawesome.lib.sawebplayer.SAWebPlayer;

public class MainActivity extends Activity {

    RelativeLayout banner1Holder = null;
    SAWebPlayer webPlayer = null;

    private String ad1;
    private String ad2;
    private String ad3;
    private String ad4;
    private String ad5;
    private String mraid1;
    private String mraid2;
    private String mraid3;
    private String mraid4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ad1 = readFromFile(this, R.raw.ad1);
        ad2 = readFromFile(this, R.raw.ad2);
        ad3 = readFromFile(this, R.raw.ad3);
        ad4 = readFromFile(this, R.raw.ad4);
        ad5 = readFromFile(this, R.raw.ad5);
        mraid1 = readFromFile(this, R.raw.mraid1);
        mraid2 = readFromFile(this, R.raw.mraid2);
        mraid3 = readFromFile(this, R.raw.mraid3);
        mraid4 = readFromFile(this, R.raw.mraid4);

        banner1Holder = (RelativeLayout) findViewById(R.id.Banner1Holder);
    }

    public void playAd1 (View v) {

        if (webPlayer != null) {
            banner1Holder.removeView(webPlayer);
            webPlayer = null;
        }
        webPlayer = new SAWebPlayer(this);
        webPlayer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webPlayer.setContentSize(320, 50);
        banner1Holder.addView(webPlayer);
        webPlayer.setup();
        webPlayer.loadHTML("https://s3-eu-west-1.amazonaws.com", ad1);
    }

    public void playAd2 (View v) {

        if (webPlayer != null) {
            banner1Holder.removeView(webPlayer);
            webPlayer = null;
        }
        webPlayer = new SAWebPlayer(this);
        webPlayer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webPlayer.setContentSize(320, 250);
        banner1Holder.addView(webPlayer);
        webPlayer.setup();
        webPlayer.loadHTML("https://s3-eu-west-1.amazonaws.com", ad2);
    }

    public void playAd3 (View v) {

        if (webPlayer != null) {
            banner1Holder.removeView(webPlayer);
            webPlayer = null;
        }
        webPlayer = new SAWebPlayer(this);
        webPlayer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webPlayer.setContentSize(320, 50);
        banner1Holder.addView(webPlayer);
        webPlayer.setup();
        webPlayer.loadHTML(null, mraid1);
    }

    public void playAd4 (View v) {

        if (webPlayer != null) {
            banner1Holder.removeView(webPlayer);
            webPlayer = null;
        }
        webPlayer = new SAWebPlayer(this);
        webPlayer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webPlayer.setContentSize(300, 50);
        banner1Holder.addView(webPlayer);
        webPlayer.setup();
        webPlayer.loadHTML(null, mraid2);
    }

    public void playAd5 (View v) {

        if (webPlayer != null) {
            banner1Holder.removeView(webPlayer);
            webPlayer = null;
        }
        webPlayer = new SAWebPlayer(this);
        webPlayer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webPlayer.setContentSize(320, 50);
        banner1Holder.addView(webPlayer);
        webPlayer.setup();
        webPlayer.loadHTML(null, mraid3);
    }

    private static String readFromFile (Context context, int ID) {
        InputStream is = context.getResources().openRawResource(ID);
        try {
            return new String(ByteStreams.toByteArray(is));
        } catch (IOException e) {
            return "";
        }
    }
}
