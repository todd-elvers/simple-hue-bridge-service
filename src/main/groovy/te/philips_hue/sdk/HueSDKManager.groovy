package te.philips_hue.sdk

import com.philips.lighting.hue.sdk.*
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager
import com.philips.lighting.hue.sdk.notification.impl.PHNotificationManagerImpl
import groovy.util.logging.Slf4j
import te.philips_hue.config_file.ConfigFileHandler

import static java.lang.System.getProperty

/**
 * This is a wrapper around the PHHueSDK class, and other Philips Hue classes, abstracting away the
 * necessary startup/shutdown logic (along with other SDK features).
 */
@Slf4j
class HueSDKManager {

    private static final PHHueSDK hueSDK = PHHueSDK.getInstance()

    /**
     * Configures the Hue SDK's appName & deviceName and adds a shutdown hook to destroy the SDK
     * reference when the JVM is terminated.
     */
    static void initSDKIfNecessary(String appName) {
        PHHueSDK.create().with {
            setAppName(getAppName() ?: appName)
            setDeviceName(getDeviceName() ?: "${getProperty('user.name')}@${getProperty('os.name')}")
        }

        addShutdownHook { shutdown() }
    }

    /**
     * Registers a an implementation of PHSDKListener with the Hue SDK.  Does not allow registering
     * the same listener twice.
     */
    static void registerSDKListener(PHSDKListener listener) {
        PHNotificationManager notificationManager = hueSDK.notificationManager as PHNotificationManagerImpl
        boolean thisListenerIsNotAlreadyRegistered = !notificationManager.localSDKListeners.contains(listener)
        if(thisListenerIsNotAlreadyRegistered){
            hueSDK.notificationManager.registerSDKListener(listener)
        }
    }

    static boolean configFileHasValidCredentials() {
        ConfigFileHandler.getInstance().configFileIsStillValid()
    }

    static void connectToBridgeUsingConfigFileCredentials() {
        log.debug("Using Hue credentials from config file.")
        hueSDK.connect(ConfigFileHandler.getInstance().readAccessPointFromConfigFile())
    }

    /**
     * This triggers a UPNP/Portal search on the LAN for all Hue bridges.
     * <p>The search can take up to 10 seconds to complete.
     * <p>Upon completion of the search, the PHSDKListener.onBridgeConnected() method will be called.
     */
    static void triggerBridgeSearch() {
        log.info("Searching for Hue bridges...")
        def bridgeSearchManager = hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE) as PHBridgeSearchManager
        bridgeSearchManager.search(true, true)  // void search(isUpnpSearch, isPortalSearch)
    }

    /**
     * This calls the necessary methods to gracefully terminate the SDK and its connection to the Hue bridge.
     * <p>If this method is called after the SDK has already been shutdown nothing happens.
     */
    static void shutdown() {
        if (hasNotBeenShutdown()) {
            log.info("Shutting down the Hue SDK.")
            hueSDK.with {
                if(selectedBridge) {
                    PHHeartbeatManager.getInstance().disableAllHeartbeats(selectedBridge)
                    disconnect(selectedBridge)
                }

                destroySDK()
            }
        }
    }

    private static final boolean hasNotBeenShutdown() {
        return PHHueSDK.@instance as Boolean
    }
}
