package tv.superawesome.lib.sawebplayer;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

class SAWebChromeClient extends WebChromeClient {

    private Listener listener;

    SAWebChromeClient(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return listener.onConsoleMessage(consoleMessage.message());
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        public boolean onConsoleMessage (String message);
    }
}
