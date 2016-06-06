package tv.superawesome.lib.sawebplayer;

/**
 * Public interface
 */
public interface SAWebPlayerInterface {
    /**
     * Callback called when the user clicks on a <a></a> element or is otherwise
     * redirected from the WebView
     * @param url - the URL the user is going to
     */
    void saWebViewWillNavigate(String url);

    /**
     * Called when the web view loads
     */
    void saWebViewDidLoad();

    /**
     * Called when the web view fails to load
     */
    void saWebViewDidFail();
}
