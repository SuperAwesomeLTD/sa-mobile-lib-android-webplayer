/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sawebplayer;

/**
 * Interface that is used by SAWebPlayer to send back click events to a library user
 */
public interface SAWebPlayerClickInterface {

    /**
     * Main interface method, that gets sent when a click actually occurs in the web view
     *
     * @param url the destination URL the click is bound to
     */
    void SAWebPlayerClickHandled(String url);
}
