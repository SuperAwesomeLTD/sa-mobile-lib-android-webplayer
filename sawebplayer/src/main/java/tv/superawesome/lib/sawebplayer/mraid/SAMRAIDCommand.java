package tv.superawesome.lib.sawebplayer.mraid;

import java.util.HashMap;
import java.util.Map;

public class SAMRAIDCommand {

    public enum Command {
        None {
            @Override
            public String toString() {
                return "None";
            }
        },
        Close {
            @Override
            public String toString() {
                return "close";
            }
        },
        CreateCalendarEvent {
            @Override
            public String toString() {
                return "createCalendarEvent";
            }
        },
        Expand {
            @Override
            public String toString() {
                return "expand";
            }
        },
        Open {
            @Override
            public String toString() {
                return "open";
            }
        },
        PlayVideo {
            @Override
            public String toString() {
                return "playVideo";
            }
        },
        Resize {
            @Override
            public String toString() {
                return "resize";
            }
        },
        SetOrientationProperties {
            @Override
            public String toString() {
                return "setOrientationProperties";
            }
        },
        SetResizeProperties {
            @Override
            public String toString() {
                return "setResizeProperties";
            }
        },
        StorePicture {
            @Override
            public String toString() {
                return "storePicture";
            }
        },
        UseCustomClose {
            @Override
            public String toString() {
                return "useCustomClose";
            }
        };

        public static Command fromString (String command) {
            switch (command) {
                case "close":                    return Close;
                case "createCalendarEvent":      return CreateCalendarEvent;
                case "expand":                   return Expand;
                case "open":                     return Open;
                case "playVideo":                return PlayVideo;
                case "resize":                   return Resize;
                case "setOrientationProperties": return SetOrientationProperties;
                case "setResizeProperties":      return SetResizeProperties;
                case "storePicture":             return StorePicture;
                case "useCustomClose":           return UseCustomClose;
            }
            return None;
        }
    }

    private Command command;
    private Map<String, String> params = new HashMap<>();
    private boolean valid = false;

    public SAMRAIDCommand (String query) {

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
                    valid = true;
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
                        params.containsKey("customClosePosition") &&
                        params.containsKey("allowOffscreen");
        }

        return false;
    }

    public Command getCommand() {
        return command;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public boolean isValid() {
        return valid;
    }
}
