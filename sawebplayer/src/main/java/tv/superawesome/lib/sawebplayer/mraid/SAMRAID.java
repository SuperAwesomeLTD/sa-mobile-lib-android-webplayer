package tv.superawesome.lib.sawebplayer.mraid;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.webkit.WebView;

import static tv.superawesome.lib.sawebplayer.mraid.SAMRAIDCommand.CustomClosePosition;

public class SAMRAID {

    private boolean hasMRAID = false;

    private WebView webView = null;

    private int expandedWidth = 0;
    private int expandedHeight = 0;
    private int expandedOffsetX = 0;
    private int expandedOffestY = 0;

    private CustomClosePosition expandedCustomClosePosition = CustomClosePosition.Top_Right;
    private boolean expandedAllowsOffscreen = false;

    private String mraidJs() {
        return "!function(){function A(a){var b=document.createElement(\"IFRAME\");b.setAttribute(\"src\",\"mraid://\"+a),document.documentElement.appendChild(b),b.parentNode.removeChild(b),b=null}function B(a){var b=Array.prototype.slice.call(arguments);b.shift(),c.i(\"fireEvent \"+a+\" [\"+b.toString()+\"]\");var d=z[a];if(d){var e=d.length;c.i(e+\" listener(s) found\");for(var f=0;f<e;f++)d[f].apply(null,b)}else c.i(\"no listeners found\")}function C(a,b){for(var c in b)if(b[c]===a)return!0;return!1}function D(a,b){var c=!0,e=E[b];for(var f in a){var g=e[f],h=a[f];g&&!g(h)&&(d.fireErrorEvent(\"Value of property \"+f+\" (\"+h+\") is invalid\",\"mraid.\"+b),c=!1)}return c}function F(a){c.d(\"isCloseRegionOnScreen\"),c.d(\"defaultPosition \"+v.x+\" \"+v.y),c.d(\"offset \"+a.offsetX+\" \"+a.offsetY);var b={};b.x=v.x+a.offsetX,b.y=v.y+a.offsetY,b.width=a.width,b.height=a.height,I(\"resizeRect\",b);var d=a.hasOwnProperty(\"expandedCustomClosePosition\")?a.expandedCustomClosePosition:t.expandedCustomClosePosition;c.d(\"expandedCustomClosePosition \"+d);var e={width:50,height:50};d.search(\"left\")!==-1?e.x=b.x:d.search(\"center\")!==-1?e.x=b.x+b.width/2-25:d.search(\"right\")!==-1&&(e.x=b.x+b.width-50),d.search(\"top\")!==-1?e.y=b.y:\"center\"===d?e.y=b.y+b.height/2-25:d.search(\"bottom\")!==-1&&(e.y=b.y+b.height-50);var f={x:0,y:0};return f.width=w.width,f.height=w.height,H(f,e)}function G(a){c.d(\"fitResizeViewOnScreen\"),c.d(\"defaultPosition \"+v.x+\" \"+v.y),c.d(\"offset \"+a.offsetX+\" \"+a.offsetY);var b={};b.x=v.x+a.offsetX,b.y=v.y+a.offsetY,b.width=a.width,b.height=a.height,I(\"resizeRect\",b);var d={x:0,y:0};d.width=w.width,d.height=w.height;var e={x:0,y:0};return H(d,b)?(c.d(\"no adjustment necessary\"),e):(b.x<d.x?e.x=d.x-b.x:b.x+b.width>d.x+d.width&&(e.x=d.x+d.width-(b.x+b.width)),c.d(\"adjustments.x \"+e.x),b.y<d.y?e.y=d.y-b.y:b.y+b.height>d.y+d.height&&(e.y=d.y+d.height-(b.y+b.height)),c.d(\"adjustments.y \"+e.y),b.x=v.x+a.offsetX+e.x,b.y=v.y+a.offsetY+e.y,I(\"adjusted resizeRect\",b),e)}function H(a,b){return c.d(\"isRectContained\"),I(\"containingRect\",a),I(\"containedRect\",b),b.x>=a.x&&b.x+b.width<=a.x+a.width&&b.y>=a.y&&b.y+b.height<=a.y+a.height}function I(a,b){c.d(a+\" [\"+b.x+\",\"+b.y+\"],[\"+(b.x+b.width)+\",\"+(b.y+b.height)+\"] (\"+b.width+\"x\"+b.height+\")\")}console.log(\"MRAID object loading...\");var a={DEBUG:0,INFO:1,WARNING:2,ERROR:3,NONE:4},b=a.NONE,c={};c.d=function(c){b<=a.DEBUG&&console.log(\"(D-mraid.js) \"+c)},c.i=function(c){b<=a.INFO&&console.log(\"(I-mraid.js) \"+c)},c.w=function(c){b<=a.WARNING&&console.log(\"(W-mraid.js) \"+c)},c.e=function(c){b<=a.ERROR&&console.log(\"(E-mraid.js) \"+c)};var d=window.mraid={},e=\"2.0\",f=d.STATES={LOADING:\"loading\",DEFAULT:\"default\",EXPANDED:\"expanded\",RESIZED:\"resized\",HIDDEN:\"hidden\"},g=d.PLACEMENT_TYPES={INLINE:\"inline\",INTERSTITIAL:\"interstitial\"},h=d.RESIZE_PROPERTIES_CUSTOM_CLOSE_POSITION={Top_Left:\"top-left\",Top_Center:\"top-center\",Top_Right:\"top-right\",Center:\"center\",Bottom_Left:\"bottom-left\",Bottom_Center:\"bottom-center\",Bottom_Right:\"bottom-right\"},i=d.ORIENTATION_PROPERTIES_FORCE_ORIENTATION={PORTRAIT:\"portrait\",LANDSCAPE:\"landscape\",NONE:\"none\"},j=d.EVENTS={ERROR:\"error\",READY:\"ready\",SIZECHANGE:\"sizeChange\",STATECHANGE:\"stateChange\",VIEWABLECHANGE:\"viewableChange\"},l=(d.SUPPORTED_FEATURES={SMS:\"sms\",TEL:\"tel\",CALENDAR:\"calendar\",STOREPICTURE:\"storePicture\",INLINEVIDEO:\"inlineVideo\"},f.LOADING),m=g.INLINE,n={},o=!1,p=!1,q=!1,r={width:0,height:0,useCustomClose:!1,isModal:!0},s={allowOrientationChange:!0,forceOrientation:i.NONE},t={width:0,height:0,expandedCustomClosePosition:h.Top_Right,offsetX:0,offsetY:0,allowOffscreen:!0},u={x:0,y:0,width:0,height:0},v={x:0,y:0,width:0,height:0},w={width:0,height:0},x={width:0,height:0},z={};d.addEventListener=function(a,b){if(c.i(\"mraid.addEventListener \"+a+\": \"+String(b)),!a||!b)return void d.fireErrorEvent(\"Both event and listener are required.\",\"addEventListener\");if(!C(a,j))return void d.fireErrorEvent(\"Unknown MRAID event: \"+a,\"addEventListener\");for(var e=z[a]=z[a]||[],f=0;f<e.length;f++){var g=String(b),h=String(e[f]);if(b===e[f]||g===h)return void c.i(\"listener \"+g+\" is already registered for event \"+a)}e.push(b)},d.createCalendarEvent=function(a){c.i(\"mraid.createCalendarEvent with \"+a),n[d.SUPPORTED_FEATURES.CALENDAR]?A(\"createCalendarEvent?eventJSON=\"+JSON.stringify(a)):c.e(\"createCalendarEvent is not supported\")},d.close=function(){c.i(\"mraid.close\"),l===f.LOADING||l===f.DEFAULT&&m===g.INLINE||l===f.HIDDEN||A(\"close\")},d.expand=function(a){void 0===a?c.i(\"mraid.expand (1-part)\"):c.i(\"mraid.expand \"+a),m!==g.INLINE||l!==f.DEFAULT&&l!==f.RESIZED||A(void 0===a?\"expand\":\"expand?url=\"+encodeURIComponent(a))},d.getCurrentPosition=function(){return c.i(\"mraid.getCurrentPosition\"),u},d.getDefaultPosition=function(){return c.i(\"mraid.getDefaultPosition\"),v},d.getExpandProperties=function(){return c.i(\"mraid.getExpandProperties\"),r},d.getMaxSize=function(){return c.i(\"mraid.getMaxSize\"),w},d.getOrientationProperties=function(){return c.i(\"mraid.getOrientationProperties\"),s},d.getPlacementType=function(){return c.i(\"mraid.getPlacementType\"),m},d.getResizeProperties=function(){return c.i(\"mraid.getResizeProperties\"),t},d.getScreenSize=function(){return c.i(\"mraid.getScreenSize\"),x},d.getState=function(){return c.i(\"mraid.getState\"),l},d.getVersion=function(){return c.i(\"mraid.getVersion\"),e},d.isViewable=function(){return c.i(\"mraid.isViewable\"),o},d.open=function(a){c.i(\"mraid.open \"+a),A(\"open?url=\"+encodeURIComponent(a))},d.playVideo=function(a){c.i(\"mraid.playVideo \"+a),A(\"playVideo?url=\"+encodeURIComponent(a))},d.removeEventListener=function(a,b){if(c.i(\"mraid.removeEventListener \"+a+\" : \"+String(b)),!a)return void d.fireErrorEvent(\"Event is required.\",\"removeEventListener\");if(!C(a,j))return void d.fireErrorEvent(\"Unknown MRAID event: \"+a,\"removeEventListener\");if(z.hasOwnProperty(a))if(b){for(var e=z[a],f=e.length,g=0;g<f;g++){var h=e[g],i=String(b),k=String(h);if(b===h||i===k){e.splice(g,1);break}}g===f&&c.i(\"listener \"+i+\" not found for event \"+a),0===e.length&&delete z[a]}else delete z[a];else c.i(\"no listeners registered for event \"+a)},d.resize=function(){if(c.i(\"mraid.resize\"),m!==g.INTERSTITIAL&&l!==f.LOADING&&l!==f.HIDDEN)return l===f.EXPANDED?void d.fireErrorEvent(\"mraid.resize called when ad is in expanded state\",\"mraid.resize\"):q?void A(\"resize\"):void d.fireErrorEvent(\"mraid.resize is not ready to be called\",\"mraid.resize\")},d.setExpandProperties=function(a){if(c.i(\"mraid.setExpandProperties\"),!D(a,\"setExpandProperties\"))return void c.e(\"failed validation\");for(var b=r.useCustomClose,d=[\"width\",\"height\",\"useCustomClose\"],e=0;e<d.length;e++){var f=d[e];a.hasOwnProperty(f)&&(r[f]=a[f])}r.useCustomClose!==b&&A(\"useCustomClose?useCustomClose=\"+r.useCustomClose),p=!0},d.setOrientationProperties=function(a){if(c.i(\"mraid.setOrientationProperties\"),!D(a,\"setOrientationProperties\"))return void c.e(\"failed validation\");var b={};b.allowOrientationChange=s.allowOrientationChange,b.forceOrientation=s.forceOrientation;for(var e=[\"allowOrientationChange\",\"forceOrientation\"],f=0;f<e.length;f++){var g=e[f];a.hasOwnProperty(g)&&(b[g]=a[g])}if(b.allowOrientationChange&&b.forceOrientation!==d.ORIENTATION_PROPERTIES_FORCE_ORIENTATION.NONE)return void d.fireErrorEvent(\"allowOrientationChange is true but forceOrientation is \"+b.forceOrientation,\"setOrientationProperties\");s.allowOrientationChange=b.allowOrientationChange,s.forceOrientation=b.forceOrientation;var h=\"allowOrientationChange=\"+s.allowOrientationChange+\"&forceOrientation=\"+s.forceOrientation;A(\"setOrientationProperties?\"+h)},d.setResizeProperties=function(a){c.i(\"mraid.setResizeProperties\"),q=!1;for(var b=[\"width\",\"height\",\"offsetX\",\"offsetY\"],e=0;e<b.length;e++){var f=b[e];if(!a.hasOwnProperty(f))return void d.fireErrorEvent(\"required property \"+f+\" is missing\",\"mraid.setResizeProperties\")}if(!D(a,\"setResizeProperties\"))return void d.fireErrorEvent(\"failed validation\",\"mraid.setResizeProperties\");var g={x:0,y:0},h=a.hasOwnProperty(\"allowOffscreen\")?a.allowOffscreen:t.allowOffscreen;if(h){if(!F(a))return void d.fireErrorEvent(\"close event region will not appear entirely onscreen\",\"mraid.setResizeProperties\")}else{if(a.width>w.width||a.height>w.height)return void d.fireErrorEvent(\"resize width or height is greater than the maxSize width or height\",\"mraid.setResizeProperties\");g=G(a)}for(var i=[\"width\",\"height\",\"offsetX\",\"offsetY\",\"expandedCustomClosePosition\",\"allowOffscreen\"],e=0;e<i.length;e++){var f=i[e];a.hasOwnProperty(f)&&(t[f]=a[f])}var j=\"width=\"+t.width+\"&height=\"+t.height+\"&offsetX=\"+(t.offsetX+g.x)+\"&offsetY=\"+(t.offsetY+g.y)+\"&expandedCustomClosePosition=\"+t.expandedCustomClosePosition+\"&allowOffscreen=\"+t.allowOffscreen;A(\"setResizeProperties?\"+j),q=!0},d.storePicture=function(a){c.i(\"mraid.storePicture \"+a),n[d.SUPPORTED_FEATURES.STOREPICTURE]?A(\"storePicture?url=\"+encodeURIComponent(a)):c.e(\"storePicture is not supported\")},d.supports=function(a){c.i(\"mraid.supports \"+a+\" \"+n[a]);var b=n[a];return\"undefined\"==typeof b&&(b=!1),b},d.useCustomClose=function(a){c.i(\"mraid.useCustomClose \"+a),r.useCustomClose!==a&&(r.useCustomClose=a,A(\"useCustomClose?useCustomClose=\"+r.useCustomClose))},d.setCurrentPosition=function(a,b,e,f){c.i(\"mraid.setCurrentPosition \"+a+\",\"+b+\",\"+e+\",\"+f);var g={};g.width=u.width,g.height=u.height,c.i(\"previousSize \"+g.width+\",\"+g.height),u.x=a,u.y=b,u.width=e,u.height=f,e===g.width&&f===g.height||d.fireSizeChangeEvent(e,f)},d.setDefaultPosition=function(a,b,d,e){c.i(\"mraid.setDefaultPosition \"+a+\",\"+b+\",\"+d+\",\"+e),v.x=a,v.y=b,v.width=d,v.height=e},d.setExpandSize=function(a,b){c.i(\"mraid.setExpandSize \"+a+\"x\"+b),r.width=a,r.height=b},d.setMaxSize=function(a,b){c.i(\"mraid.setMaxSize \"+a+\"x\"+b),w.width=a,w.height=b},d.setPlacementType=function(a){c.i(\"mraid.setPlacementType \"+a),m=a},d.setScreenSize=function(a,b){c.i(\"mraid.setScreenSize \"+a+\"x\"+b),x.width=a,x.height=b,p||(r.width=a,r.height=b)},d.setSupports=function(a,b){c.i(\"mraid.setSupports \"+a+\" \"+b),n[a]=b},d.fireErrorEvent=function(a,b){c.i(\"mraid.fireErrorEvent \"+a+\" \"+b),B(d.EVENTS.ERROR,a,b)},d.fireReadyEvent=function(){c.i(\"mraid.fireReadyEvent\"),B(d.EVENTS.READY)},d.fireSizeChangeEvent=function(a,b){c.i(\"mraid.fireSizeChangeEvent \"+a+\"x\"+b),l!==d.STATES.LOADING&&B(d.EVENTS.SIZECHANGE,a,b)},d.fireStateChangeEvent=function(a){c.i(\"mraid.fireStateChangeEvent \"+a),l!==a&&(l=a,B(d.EVENTS.STATECHANGE,l))},d.fireViewableChangeEvent=function(a){c.i(\"mraid.fireViewableChangeEvent \"+a),o!==a&&(o=a,B(d.EVENTS.VIEWABLECHANGE,o))};var E={setExpandProperties:{width:function(a){return!isNaN(a)},height:function(a){return!isNaN(a)},useCustomClose:function(a){return\"boolean\"==typeof a}},setOrientationProperties:{allowOrientationChange:function(a){return\"boolean\"==typeof a},forceOrientation:function(a){var b=[\"portrait\",\"landscape\",\"none\"];return\"string\"==typeof a&&b.indexOf(a)!==-1}},setResizeProperties:{width:function(a){return!isNaN(a)&&50<=a},height:function(a){return!isNaN(a)&&50<=a},offsetX:function(a){return!isNaN(a)},offsetY:function(a){return!isNaN(a)},expandedCustomClosePosition:function(a){var b=[\"top-left\",\"top-center\",\"top-right\",\"center\",\"bottom-left\",\"bottom-center\",\"bottom-right\"];return\"string\"==typeof a&&b.indexOf(a)!==-1},allowOffscreen:function(a){return\"boolean\"==typeof a}}};d.dumpListeners=function(){var a=Object.keys(z).length;c.i(\"dumping listeners (\"+a+\" events)\");for(var b in z){var d=z[b];c.i(\"  \"+b+\" contains \"+d.length+\" listeners\");for(var e=0;e<d.length;e++)c.i(\"    \"+d[e])}},console.log(\"MRAID object loaded\")}();";
    }

    private void injectJS (String js) {
        webView.loadUrl("javascript: " + js);
    }

    public void injectMRAID () {
        String mraid = mraidJs();
        injectJS(mraid);
    }

    private void fireStateChangedEvent (String event) {
        String method = "mraid.fireStateChangeEvent('" + event + "');";
        injectJS(method);
    }

    public void setStateToLoading () {
        fireStateChangedEvent("loading");
    }

    public void setStateToDefault () {
        fireStateChangedEvent("default");
    }

    public void setStateToExpanded () {
        fireStateChangedEvent("expanded");
    }

    public void setStateToResized () {
        fireStateChangedEvent("resized");
    }

    public void setStateToHidden () {
        fireStateChangedEvent("hidden");
    }

    private void fireViewableChangeEvent (boolean isViewable) {
        String method = "mraid.fireViewableChangeEvent(" + isViewable + ");";
        injectJS(method);
    }

    public void setViewableTrue () {
        fireViewableChangeEvent(true);
    }

    public void setViewableFalse () {
        fireViewableChangeEvent(false);
    }

    private void setPlacementType (boolean isInterstitial) {
        String method = "mraid.setPlacementType('" + (isInterstitial ? "interstitial" : "inline") + "');";
        injectJS(method);
    }

    public void setPlacementInline () {
        setPlacementType(false);
    }

    public void setPlacementInterstitial () {
        setPlacementType(true);
    }

    public void setReady () {
        String method = "mraid.fireReadyEvent()";
        injectJS(method);
    }

    public void setCurrentPosition (int width, int height) {
        int x = 0;
        int y = 0;
        String method = "mraid.setCurrentPosition(" + x + ", " + y + ", " + width + ", " + height + ");";
        injectJS(method);
    }

    public void setDefaultPosition (int width, int height) {
        int x = 0;
        int y = 0;
        String method = "mraid.setDefaultPosition(" + x + ", " + y + ", " + width + ", " + height + ");";
        injectJS(method);
    }

    public void setScreenSize (int width, int height) {
        String method = "mraid.setScreenSize(" + width + ", " + height + ");";
        injectJS(method);
    }

    public void setMaxSize (int width, int height) {
        String method = "mraid.setMaxSize(" + width + ", " + height + ");";
        injectJS(method);
    }

    public void setResizeProperties (int width, int height, int offsetX, int offsetY, CustomClosePosition customClosePosition, boolean allowsOffscreen) {
        expandedWidth = width;
        expandedHeight = height;
        expandedOffsetX = offsetX;
        expandedOffestY = offsetY;
        expandedCustomClosePosition = customClosePosition;
        expandedAllowsOffscreen = allowsOffscreen;

    }

    private int px2dip(Activity context, int pixels) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return pixels * DisplayMetrics.DENSITY_DEFAULT / displayMetrics.densityDpi;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public boolean hasMRAID() {
        return hasMRAID;
    }

    public void setHasMRAID(boolean hasMRAID) {
        this.hasMRAID = hasMRAID;
    }

    public int getExpandedWidth() {
        return expandedWidth;
    }

    public int getExpandedHeight() {
        return expandedHeight;
    }

    public int getExpandedOffsetX() {
        return expandedOffsetX;
    }

    public int getExpandedOffestY() {
        return expandedOffestY;
    }

    public void setExpandedCustomClosePosition(CustomClosePosition e) {
        expandedCustomClosePosition = e;
    }

    public CustomClosePosition getExpandedCustomClosePosition() {
        return expandedCustomClosePosition;
    }

    public boolean isExpandedAllowsOffscreen() {
        return expandedAllowsOffscreen;
    }
}
