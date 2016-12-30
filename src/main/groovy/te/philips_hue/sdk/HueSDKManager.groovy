package te.philips_hue.sdk

import com.philips.lighting.hue.sdk.*
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager
import com.philips.lighting.hue.sdk.notification.impl.PHNotificationManagerImpl
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import te.philips_hue.config_file.ConfigFileHandler

import static java.lang.System.getProperty

/**
 * This is a wrapper around the PHHueSDK class, and other Philips Hue classes, abstracting away the
 * necessary startup/shutdown logic (along with other SDK features).
 */
@Slf4j
class HueSDKManager {

    /**
     * Configures the Hue SDK's appName & deviceName and adds a shutdown hook to destroy the SDK
     * reference when the JVM is terminated.
     */
    static void initSDK(String appName) {
        PHHueSDK.create().with {
            setAppName(getAppName() ?: appName)
            setDeviceName(getDeviceName() ?: "${getProperty('user.name')}@${getProperty('os.name')}")
        }

        addShutdownHook { shutdown() }
    }

    /**
     * Registers a an implementation of PHSDKListener with the Hue SDK.
     * <p>Guards against registering the same listener twice.
     */
    @CompileStatic
    static void registerSDKListener(PHSDKListener listener) {
        PHHueSDK.getInstance().getNotificationManager().with {
            unregisterSDKListener(listener)
            registerSDKListener(listener)
        }
    }

    @CompileStatic
    static boolean configFileHasValidCredentials() {
        ConfigFileHandler.getInstance().configFileIsStillValid()
    }

    @CompileStatic
    static void connectToBridgeUsingConfigFileCredentials() {
        log.info("Connecting to Hue bridge using credentials from config file.")
        PHHueSDK.getInstance().connect(ConfigFileHandler.getInstance().readAccessPointFromConfigFile())
    }

    /**
     * This triggers a UPNP/Portal search over the network for all Hue bridges.
     * <p>The search can take up to 10 seconds to complete.
     * <p>Upon completion of the search, the PHSDKListener.onBridgeConnected() method will be called.
     *
     * @see HueSDKConnectionListener#onBridgeConnected(com.philips.lighting.model.PHBridge, java.lang.String)
     */
    @CompileStatic
    static void triggerBridgeSearch() {
        log.info("Searching for Hue bridges...")
        def bridgeSearchManager = PHHueSDK.getInstance().getSDKService(PHHueSDK.SEARCH_BRIDGE) as PHBridgeSearchManager
        bridgeSearchManager.search(true, true)  // void search(isUpnpSearch, isPortalSearch)
    }

    /**
     * This calls the necessary methods to gracefully terminate the SDK.
     * <p>If this method is called after the SDK has already been shutdown nothing happens.
     */
    @CompileStatic
    static void shutdown() {
        if (hasNotBeenShutdown()) {
            log.info("Shutting down the Hue SDK.")
            PHHueSDK.getInstance().destroySDK()
        }
    }

    private static final boolean hasNotBeenShutdown() {
        return PHHueSDK.@instance as Boolean
    }
}
