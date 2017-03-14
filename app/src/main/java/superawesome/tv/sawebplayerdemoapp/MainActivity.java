package superawesome.tv.sawebplayerdemoapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

import tv.superawesome.lib.sawebplayer.SAWebPlayer;

public class MainActivity extends Activity {

    FragmentManager manager = null;

    SAWebPlayer webPlayer = null;
    String webPlayerTag = "WebPlayerTag";
    SAWebPlayer webPlayer2 = null;
    String webPlayer2Tag = "WebPlayer2Tag";

    private String ad1;
    private String ad2;
    private String ad3;
    private String mraid1;
    private String mraid2;
    private String mraid3;
    private String mraid4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = getFragmentManager();

        ad1 = readFromFile(this, R.raw.ad1);
        ad2 = readFromFile(this, R.raw.ad2);
        ad3 = readFromFile(this, R.raw.ad3);
        mraid1 = readFromFile(this, R.raw.mraid1);
        mraid2 = readFromFile(this, R.raw.mraid2);
        mraid3 = readFromFile(this, R.raw.mraid3);
        mraid4 = readFromFile(this, R.raw.mraid4);

        if (manager.findFragmentByTag(webPlayerTag) == null) {
            webPlayer = new SAWebPlayer();
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
        if (webPlayer != null) {
            manager.beginTransaction().remove(webPlayer).commit();
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(320, 50);
            webPlayer.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer.loadHTML(ad1);
                    }

                }
            });
            manager.beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        }
    }

    public void playAd2 (View v) {
        if (webPlayer != null) {
            manager.beginTransaction().remove(webPlayer).commit();
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(300, 250);
            webPlayer.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer.loadHTML(ad2);
                    }

                }
            });
            manager.beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        }
    }

    public void playAd3 (View v) {
        if (webPlayer2 != null) {
            manager.beginTransaction().remove(webPlayer2).commit();
            webPlayer2 = new SAWebPlayer();
            webPlayer2.setContentSize(320, 480);
            webPlayer2.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer2.loadHTML(ad3);
                    }

                }
            });
            manager.beginTransaction().add(R.id.MyBanner2, webPlayer2, webPlayer2Tag).commit();
        }
    }

    public void playMRAID1 (View view) {
        if (webPlayer != null) {
            manager.beginTransaction().remove(webPlayer).commit();
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(320, 50);
            webPlayer.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer.loadHTML(mraid1);
                    }
                }
            });
            manager.beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        }
    }

    public void playMRAID2 (View view) {
        if (webPlayer != null) {
            manager.beginTransaction().remove(webPlayer).commit();
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(300, 50);
            webPlayer.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer.loadHTML(mraid2);
                    }
                }
            });
            manager.beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        }
    }

    public void playMRAID3 (View view) {
        if (webPlayer2 != null) {
            manager.beginTransaction().remove(webPlayer2).commit();
            webPlayer2 = new SAWebPlayer();
            webPlayer2.setContentSize(320, 50);
            webPlayer2.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer2.loadHTML(mraid3);
                    }
                }
            });
            manager.beginTransaction().add(R.id.MyBanner2, webPlayer2, webPlayer2Tag).commit();
        }
    }

    public void playMRAID4 (View view) {
        if (webPlayer != null) {
            manager.beginTransaction().remove(webPlayer).commit();
            webPlayer = new SAWebPlayer();
            webPlayer.setContentSize(320, 50);
            webPlayer.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer.loadHTML(mraid3);
                    }
                }
            });
            manager.beginTransaction().add(R.id.MyBanner, webPlayer, webPlayerTag).commit();
        }
    }

    public void playMRAID5 (View view) {
        if (webPlayer2 != null) {
            manager.beginTransaction().remove(webPlayer2).commit();
            webPlayer2 = new SAWebPlayer();
            webPlayer2.setContentSize(320, 100);
            webPlayer2.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(SAWebPlayer.Event event, String destination) {
                    Log.d("SuperAwesome", "Event is " + event +  " Dest " + destination);

                    if (event == SAWebPlayer.Event.Web_Prepared) {
                        webPlayer2.loadHTML(mraid4);
                    }
                }
            });
            manager.beginTransaction().add(R.id.MyBanner2, webPlayer2, webPlayer2Tag).commit();
        }
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

