package te.philips_hue

import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.hue.sdk.PHSDKListener
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueParsingError
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import te.philips_hue.callbacks.BridgeConnectedCallback
import te.philips_hue.config_file.ConfigHandler


/**
 * This is the class that listens for changes from the Hue SDK.
 */
@Slf4j
@EqualsAndHashCode(excludes = ['hueSDK', 'configHandler'])
class HueSDKEventListener implements PHSDKListener {

    private PHHueSDK hueSDK = PHHueSDK.getInstance()
    private ConfigHandler configHandler = ConfigHandler.getInstance()
    private HueSDKErrorHandler errorHandler = new HueSDKErrorHandler()
    private boolean pushlinkAuthWasRequired = false

    BridgeConnectedCallback bridgeConnectedCallback

    HueSDKEventListener(BridgeConnectedCallback callback) {
        this.bridgeConnectedCallback = callback
    }

    //TODO: Handle multiple access points being returned
    @Override
    void onAccessPointsFound(List<PHAccessPoint> accessPoints) {
        def accessPoint = accessPoints.first()
        log.info("Access point found! (IP=$accessPoint.ipAddress)")
        errorHandler.resetPushlinkButtonTimer()
        hueSDK.connect(accessPoint)
    }

    @Override
    void onCacheUpdated(List<Integer> cacheNotifications, PHBridge bridge) {
        // NO-OP
    }

    /**
     * This callback is triggered once bridge authentication completes and is passed the username that the bridge generated for us.
     * <p>This finishes the bridge-connection process and sets up a heartbeat to the bridge every 10 seconds.
     * 
     * @param bridge the bridge we just connected to
     * @param username the username to use when making API requests to this bridge
     */
    @Override
    void onBridgeConnected(PHBridge bridge, String username) {
        hueSDK.setSelectedBridge(bridge)
        PHHeartbeatManager.getInstance().enableLightsHeartbeat(bridge, PHHueSDK.HB_INTERVAL)
        log.info("Bridge Connected!  [Username: $username | Lights: ${bridge.resourceCache.allLights*.identifier.join(',')}]")
        if(pushlinkAuthWasRequired) {
            pushlinkAuthWasRequired = false
            configHandler.updateConfigFile([
                    (ConfigHandler.USERNAME_PROP)  : username,
                    (ConfigHandler.IP_ADDRESS_PROP): bridge.resourceCache.bridgeConfiguration.ipAddress
            ])
        }

        // Everything's done and we're connected - execute the callback
        bridgeConnectedCallback.execute()
    }

    /**
     * This callback is triggered when a bridge requires authentication.
     * <p>Authentication just means pressing the pushlink button on top of the bridge within 30 seconds.
     *
     * @param accessPoint the access point requesting authentication via the pushlink button
     */
    @Override
    void onAuthenticationRequired(PHAccessPoint accessPoint) {
        log.info("Authentication required!  Please press the blue button on the Hue Bridge.")
        hueSDK.startPushlinkAuthentication(accessPoint)
        pushlinkAuthWasRequired = true
    }

    @Override
    void onConnectionResumed(PHBridge bridge) {
        // NO-OP
    }

    @Override
    void onConnectionLost(PHAccessPoint accessPoint) {
        log.info("Connection to bridge lost!")
        if(HueSDKManager.configFileHasValidCredentials()) {
            HueSDKManager.connectToBridgeUsingConfigFileCredentials()
        }
    }

    @Override
    void onError(int code, String message) {
        errorHandler.handleError(code, message)
        if(errorHandler.shouldSearchForBridgeAgain(code)) {
            HueSDKManager.triggerBridgeSearch()
        }
    }

    /**
     * Any JSON parsing errors that occurred will be passed to this method.
     */
    @Override
    void onParsingErrors(List<PHHueParsingError> parsingErrors) {
        log.error("Parsing errors:")
        parsingErrors.each {
            log.error("$it.code - $it.resourceId - $it.message")
        }
    }
}
