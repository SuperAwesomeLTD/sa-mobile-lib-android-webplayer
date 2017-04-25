package tv.superawesome.lib.sawebplayer.mraid;

import java.util.HashMap;
import java.util.Map;

public class SAMRAIDCommand {

    public enum Command {
        None,
        Close,
        CreateCalendarEvent,
        Expand,
        Open,
        PlayVideo,
        Resize,
        SetOrientationProperties,
        SetResizeProperties,
        StorePicture,
        UseCustomClose;

        public static Command fromString (String command) {
            if (command == null) {
                return None;
            } else {
                switch (command) {
                    case "close":
                        return Close;
                    case "createCalendarEvent":
                        return CreateCalendarEvent;
                    case "expand":
                        return Expand;
                    case "open":
                        return Open;
                    case "playVideo":
                        return PlayVideo;
                    case "resize":
                        return Resize;
                    case "setOrientationProperties":
                        return SetOrientationProperties;
                    case "setResizeProperties":
                        return SetResizeProperties;
                    case "storePicture":
                        return StorePicture;
                    case "useCustomClose":
                        return UseCustomClose;
                    default:
                        return None;
                }
            }
        }
    }

    public enum CustomClosePosition {
        Unavailable,
        Top_Left,
        Top_Right,
        Center,
        Bottom_Left,
        Bottom_Right,
        Top_Center,
        Bottom_Center;

        public static CustomClosePosition fromString (String btnPos) {
            if (btnPos == null) {
                return Top_Right;
            } else {
                switch (btnPos) {
                    case "top-left":
                        return Top_Left;
                    case "top-right":
                        return Top_Right;
                    case "center":
                        return Center;
                    case "bottom-left":
                        return Bottom_Left;
                    case "bottom-right":
                        return Bottom_Right;
                    case "top-center":
                        return Top_Center;
                    case "bottom-center":
                        return Bottom_Center;
                    default:
                        return Top_Right;
                }
            }
        }
    }

    private Listener listener;
    private Command command;
    private Map<String, String> params = new HashMap<>();

    public SAMRAIDCommand () {
        // do nothing
    }

    public boolean isMRAIDCommand (String query) {
        return query.startsWith("mraid://");
    }

    public void getQuery (String query) {

        String[] parts = query.replace("mraid://", "").split("\\?");

        if (parts.length >= 1) {
            command = Command.fromString(parts[0]);

            if (parts.length >= 2) {
                String paramStr = parts[1];
                String[] pairs = paramStr.split("&");
                Map<String, String> lparams = new HashMap<>();
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        String key = kv[0];
                        String value = kv[1];
                        lparams.put(key, value);
                    }
                }
                if (checkParamsForCommand(command, lparams)) {
                    params = lparams;
                }
            }

            switch (command) {
                case None:
                    break;
                case Close: {
                    listener.closeCommand();
                    break;
                }
                case CreateCalendarEvent: {
                    listener.createCalendarEventCommand(null);
                    break;
                }
                case Expand: {
                    String url = parseUrl(params.get("url"));
                    listener.expandCommand(url);
                    break;
                }
                case Open: {
                    String url = parseUrl(params.get("url"));
                    listener.openCommand(url);
                    break;
                }
                case PlayVideo: {
                    String url = parseUrl(params.get("url"));
                    listener.playVideoCommand(url);
                    break;
                }
                case StorePicture: {
                    String url = parseUrl(params.get("url"));
                    listener.storePictureCommand(url);
                    break;
                }
                case Resize: {
                    listener.resizeCommand();
                    break;
                }
                case SetOrientationProperties: {
                    listener.setOrientationPropertiesCommand(false, false);
                    break;
                }
                case SetResizeProperties: {

                    String width = params.get("width");
                    int iWidth = 0;
                    try {
                        iWidth = Integer.parseInt(width);
                    } catch (Exception e) {
                        //
                    }

                    String height = params.get("height");
                    int iHeight = 0;
                    try {
                        iHeight = Integer.parseInt(height);
                    } catch (Exception e) {
                        //
                    }

                    String offsetX = params.get("offsetX");
                    int iOffsetX = 0;
                    try {
                        iOffsetX = Integer.parseInt(offsetX);
                    } catch (Exception e) {
                        //
                    }

                    String offsetY = params.get("offsetY");
                    int iOffsetY = 0;
                    try {
                        iOffsetY = Integer.parseInt(offsetY);
                    } catch (Exception e) {
                        //
                    }

                    String allowOffscreen = params.get("allowOffscreen");
                    boolean bAllowOffscreen = false;
                    try {
                        bAllowOffscreen = Boolean.parseBoolean(allowOffscreen);
                    } catch (Exception e) {
                        //
                    }

                    String customClosePosition = params.get("customClosePosition");
                    if (customClosePosition == null) {
                        customClosePosition = params.get("expandedCustomClosePosition");
                    }
                    CustomClosePosition eCustomClosePosition = CustomClosePosition.fromString(customClosePosition);

                    listener.setResizePropertiesCommand(iWidth, iHeight, iOffsetX, iOffsetY, eCustomClosePosition, bAllowOffscreen);

                    break;
                }
                case UseCustomClose: {
                    listener.useCustomCloseCommand(true);
                    break;
                }
            }

        }
    }

    private boolean checkParamsForCommand (Command command, Map<String, String> params) {

        switch (command) {
            case None:
                return false;
            case Close:
            case Expand:
            case Resize:
                return true;
            case UseCustomClose:
                return params.containsKey("useCustomClose");
            case CreateCalendarEvent:
                return params.containsKey("eventJSON");
            case Open:
            case PlayVideo:
            case StorePicture:
                return params.containsKey("url");
            case SetOrientationProperties:
                return params.containsKey("allowOrientationChange") &&
                        params.containsKey("forceOrientation");
            case SetResizeProperties:
                return params.containsKey("width") &&
                        params.containsKey("height") &&
                        params.containsKey("offsetX") &&
                        params.containsKey("offsetY") &&
                        (params.containsKey("customClosePosition") ||
                        params.containsKey("expandedCustomClosePosition")) &&
                        params.containsKey("allowOffscreen");
        }

        return false;
    }

    private String parseUrl (String url) {
        return url == null ? null :
                url.replace("%3A", ":")
                .replace("%2F", "/")
                .replace("%2B", "+")
                .replace("%3F", "?")
                .replace("%26", "&")
                .replace("%3D", "=")
                .replace("%2C", ",");
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void closeCommand ();
        void expandCommand (String url);
        void resizeCommand ();
        void useCustomCloseCommand (boolean useCustomClose);
        void createCalendarEventCommand (String eventJSON);
        void openCommand (String url);
        void playVideoCommand (String url);
        void storePictureCommand (String url);
        void setOrientationPropertiesCommand (boolean allowOrientationChange, boolean forceOrientation);
        void setResizePropertiesCommand (int width, int height, int offsetX, int offestY, CustomClosePosition customClosePosition, boolean allowOffscreen);

    }
}
