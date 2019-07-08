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

import java.util.function.Consumer

import static com.philips.lighting.hue.sdk.PHMessageType.BRIDGE_NOT_FOUND

/**
 * Listens for bridge connections, handles authentication when they occur,
 * and, when authentication is successful, executes payloads against them.
 */
@Slf4j
@CompileStatic
@EqualsAndHashCode
class BridgeConnectionListener implements PHSDKListener {

    private final PHHueSDK sdk
    private final Consumer<PHBridge> connectionCallback
    private final PushLinkAuthHandler pushlinkAuthHandler

    BridgeConnectionListener(Consumer<PHBridge> callback) {
        this(PHHueSDK.getInstance(), callback)
    }

    BridgeConnectionListener(PHHueSDK sdk, Consumer<PHBridge> callback) {
        this.sdk = sdk
        this.connectionCallback = callback
        this.pushlinkAuthHandler = new PushLinkAuthHandler(sdk)
    }

    @Override
    void onAccessPointsFound(List<PHAccessPoint> accessPoints) {
        log.info("Bridge found, connecting to {}...", accessPoints[0].ipAddress)
        sdk.connect(accessPoints[0])
    }

    @Override
    void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
        pushlinkAuthHandler.startAuthProcess(phAccessPoint)
    }

    @Override
    void onBridgeConnected(PHBridge bridge, String username) {
        log.info("Connected to bridge on ${bridge.resourceCache.bridgeConfiguration.ipAddress}")

        pushlinkAuthHandler.finishAuthProcess(bridge, username)
        sdk.setSelectedBridge(bridge)
        PHHeartbeatManager.getInstance().enableLightsHeartbeat(bridge, PHHueSDK.HB_INTERVAL)

        connectionCallback?.accept(bridge)
    }

    @Override
    void onError(int code, String message) {
        switch (code) {
            case pushlinkAuthHandler.&isAuthError:
                break

            case BRIDGE_NOT_FOUND:
                log.info("No bridge was found on your network.")
                break

            default:
                log.error("Error #$code - $message")
        }
    }

    @Override
    void onParsingErrors(List<PHHueParsingError> parsingErrors) {
        log.error("Parsing errors: ${parsingErrors.collect({ "$it.code - $it.resourceId - $it.message" })}")
    }

    @Override
    void onConnectionLost(PHAccessPoint accessPoint) {
        log.info("Connection to bridge lost.")
    }

    @Override
    void onCacheUpdated(List<Integer> cacheNotifications, PHBridge bridge) {
        log.info("Cache updated.")
    }

    /**
     * Triggers when reconnecting to a bridge that has already been connected
     * to before.  This is always triggered when connecting to a bridge via
     * cached credentials.
     */
    @Override
    void onConnectionResumed(PHBridge bridge) { }
}
