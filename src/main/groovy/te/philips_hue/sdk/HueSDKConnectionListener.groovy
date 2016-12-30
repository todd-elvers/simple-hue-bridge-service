package te.philips_hue.sdk

import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.hue.sdk.PHSDKListener
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueParsingError
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import te.philips_hue.config_file.ConfigFileHandler

import static com.philips.lighting.hue.sdk.PHMessageType.BRIDGE_NOT_FOUND
import static com.philips.lighting.hue.sdk.PHMessageType.PUSHLINK_AUTHENTICATION_FAILED
import static com.philips.lighting.hue.sdk.PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED


/**
 * This is the class that listens for changes from the Hue SDK.
 */
@Slf4j
@EqualsAndHashCode(excludes = ['hueSDK'])
@CompileStatic
class HueSDKConnectionListener implements PHSDKListener {

    private PHHueSDK hueSDK = PHHueSDK.getInstance()
    private BridgeConnectedCallback bridgeConnectedCallback
    private boolean pushlinkAuthWasRequired = false
    private int pushlinkButtonTimeoutCounter = 0

    HueSDKConnectionListener(BridgeConnectedCallback callback) {
        this.bridgeConnectedCallback = callback
    }

    //TODO: Handle multiple access points being returned
    @Override
    void onAccessPointsFound(List<PHAccessPoint> accessPoints) {
        PHAccessPoint hueBridge = accessPoints.first()
        if(accessPoints.size() == 1) {
            log.info("Hue bridge found! Connecting to {}.", hueBridge.ipAddress)
        } else {
            log.info("{} Hue bridges found! Connecting to {}.", accessPoints.size(), hueBridge.ipAddress)
        }
        
        // Reset pushlink logic
        pushlinkButtonTimeoutCounter = 30
        pushlinkAuthWasRequired = false

        hueSDK.connect(hueBridge)
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
        log.info("Bridge Connected!")

        finalizeConnection(bridge)

        if(pushlinkAuthWasRequired) {
            ConfigFileHandler.getInstance().updateConfigFile(bridge, username)
        }

        bridgeConnectedCallback?.execute()
    }

    private void finalizeConnection(PHBridge bridge) {
        hueSDK.setSelectedBridge(bridge)
        PHHeartbeatManager.getInstance().enableLightsHeartbeat(bridge, PHHueSDK.HB_INTERVAL)
    }

    /**
     * This callback is triggered when a bridge requires authentication.
     * <p>Authentication just means pressing the pushlink button on top of the bridge within 30 seconds.
     *
     * @param accessPoint the access point requesting authentication via the pushlink button
     */
    @Override
    void onAuthenticationRequired(PHAccessPoint accessPoint) {
        log.info("Authentication required!  You have 30 seconds to press the blue button on your Hue Bridge.")
        hueSDK.startPushlinkAuthentication(accessPoint)
        pushlinkAuthWasRequired = true
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
        switch (code) {
            case PUSHLINK_BUTTON_NOT_PRESSED:
                print("\r${pushlinkButtonTimeoutCounter--} seconds remaining...")
                break

            case PUSHLINK_AUTHENTICATION_FAILED:
                println("\r0 seconds remaining.")
                log.info("Hue bridge button not pressed in time, authentication failed.")
                HueSDKManager.shutdown()
                break

            case BRIDGE_NOT_FOUND:
                log.info("No bridge was found on your network.")
                break

            default:
                log.error("Error #$code - $message")
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

    @Override
    void onCacheUpdated(List<Integer> cacheNotifications, PHBridge bridge) {
        // NO-OP
    }

    @Override
    void onConnectionResumed(PHBridge bridge) {
        // NO-OP
    }
}
