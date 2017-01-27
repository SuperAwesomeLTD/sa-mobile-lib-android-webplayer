/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sawebplayer;

/**
 * Interface that is used by the SAWebPlayer to send back web view events to the library users
 */
public interface SAWebPlayerEventInterface {

    /**
     * Main method of the interface
     *
     * @param event         the event that just happened
     * @param destination   the destination URL
     */
    void saWebPlayerDidReceiveEvent (SAWebPlayerEvent event, String destination);
}
