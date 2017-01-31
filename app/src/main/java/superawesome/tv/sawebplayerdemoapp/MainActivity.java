package superawesome.tv.sawebplayerdemoapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import tv.superawesome.lib.sawebplayer.SAWebPlayer;
import tv.superawesome.lib.sawebplayer.SAWebPlayerEvent;
import tv.superawesome.lib.sawebplayer.SAWebPlayerEventInterface;

public class MainActivity extends Activity {

    SAWebPlayer webPlayer = null;
    String webPlayerTag = "WebPlayerTag";
    SAWebPlayer webPlayer2 = null;
    String webPlayer2Tag = "WebPlayer2Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getFragmentManager();

        final String img = "<a href='https://superawesome.tv'><img src='https://ads.superawesome.tv/v2/demo_images/320x50.jpg'/></a>";

        if (manager.findFragmentByTag(webPlayerTag) == null) {
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(320, 50);
            webPlayer.setEventListener(new SAWebPlayerEventInterface() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayerEvent.Web_Prepared) {
                        webPlayer.loadHTML(img);
                    }
                    if (event == SAWebPlayerEvent.Web_Layout) {
                        Log.d("SuperAwesome", "Pos " + webPlayer.getWebView().getTranslationX() + ", " + webPlayer.getWebView().getTranslationY());
                    }

                }
            });
            getFragmentManager().beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        } else {
            webPlayer = (SAWebPlayer) manager.findFragmentByTag(webPlayerTag);
        }

        if (manager.findFragmentByTag(webPlayer2Tag) == null) {
            webPlayer2 = new SAWebPlayer();
            getFragmentManager().beginTransaction().add(R.id.MyBanner2, webPlayer2, webPlayer2Tag).commit();
        } else {
            webPlayer2 = (SAWebPlayer) manager.findFragmentByTag(webPlayer2Tag);
        }
    }

    public void playAd1 (View v) {
        final String img = "<a href='https://superawesome.tv'><img src='https://ads.superawesome.tv/v2/demo_images/320x50.jpg'/></a>";

        if (webPlayer != null) {
            getFragmentManager().beginTransaction().remove(webPlayer).commit();
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(320, 50);
            webPlayer.setEventListener(new SAWebPlayerEventInterface() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayerEvent.Web_Prepared) {
                        webPlayer.loadHTML(img);
                    }
                    if (event == SAWebPlayerEvent.Web_Layout) {
                        Log.d("SuperAwesome", "Pos " + webPlayer.getWebView().getTranslationX() + ", " + webPlayer.getWebView().getTranslationY());
                    }

                }
            });
            getFragmentManager().beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        }
    }

    public void playAd2 (View v) {
        final String tag = "<div width='100%' height='100%' style='border:0;padding:0'><A HREF=\"[click]https://ad.doubleclick.net/ddm/jump/N304202.1915243SUPERAWESOME.TV/B10773905.144625054;sz=300x250;ord=[timestamp]?\"><IMG SRC=\"https://ad.doubleclick.net/ddm/ad/N304202.1915243SUPERAWESOME.TV/B10773905.144625054;sz=300x250;ord=[timestamp];dc_lat=;dc_rdid=;tag_for_child_directed_treatment=?\" BORDER=0 WIDTH=300 HEIGHT=250 ALT=\"Advertisement\"></A></div>";

        if (webPlayer != null) {
            getFragmentManager().beginTransaction().remove(webPlayer).commit();
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(300, 250);
            webPlayer.setEventListener(new SAWebPlayerEventInterface() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayerEvent.Web_Prepared) {
                        webPlayer.loadHTML(tag);
                    }
                    if (event == SAWebPlayerEvent.Web_Layout) {
                        Log.d("SuperAwesome", "Pos " + webPlayer.getWebView().getTranslationX() + ", " + webPlayer.getWebView().getTranslationY());
                    }

                }
            });
            getFragmentManager().beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        }
    }

    public void playAd3 (View v) {
        final String rich2 = "<iframe style='padding:0;border:0;' width='100%' height='100%' src='https://s3-eu-west-1.amazonaws.com/sb-ads-uploads/rich-media/npgSkbFMPznR1uqJhOGjkqzuWFiekbfO/normal/index.html'/>";

        if (webPlayer2 != null) {
            getFragmentManager().beginTransaction().remove(webPlayer2).commit();
            webPlayer2 = new SAWebPlayer();
            webPlayer2.setContentSize(320, 480);
            webPlayer2.setEventListener(new SAWebPlayerEventInterface() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayerEvent event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayerEvent.Web_Prepared) {
                        webPlayer2.loadHTML(rich2);
                    }

                }
            });
            getFragmentManager().beginTransaction().add(R.id.MyBanner2, webPlayer2, webPlayer2Tag).commit();
        }
    }
}

