/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sawebplayer;

/**
 * WebPlayer event enum, containing two main events:
 *  - Web_Start: happens when the web view content is fully loaded
 *  - Web_Error: happens when something prevents the web view from properly loading the content
 */
public enum SAWebPlayerEvent {
    Web_Prepared,
    Web_Start,
    Web_Error,
    Web_Click
}
