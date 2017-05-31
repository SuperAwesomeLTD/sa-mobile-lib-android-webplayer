package tv.superawesome.lib.sawebplayer.utilities;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SAWebUtils {

    private static String readContentsOfURL(String url) {

        String content = null;
        InputStream is = null;
        try {
            HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                byte[] buf = new byte[1500];
                int count;
                StringBuilder sb = new StringBuilder();
                while ((count = is.read(buf)) != -1) {
                    String data = new String(buf, 0, count);
                    sb.append(data);
                }
                content = sb.toString();

            }
            conn.disconnect();
        } catch (IOException e) {
            // do nothing
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
        return content;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static void loadContentsOfURL(final Context context, final String url, final Listener listener) {
        (new Thread(new Runnable() {
            @Override
            public void run() {

                final String contents = SAWebUtils.readContentsOfURL(url);
                if (!TextUtils.isEmpty(contents)) {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.didLoadContent(contents);
                            }
                        }
                    });
                } else {
                    if (listener != null) {
                        listener.didLoadContent(null);
                    }
                }

            }
        })).start();
    }

    public interface Listener {
        void didLoadContent(String content);
    }

}
