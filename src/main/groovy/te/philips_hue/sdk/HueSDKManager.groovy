package te.philips_hue.sdk

import com.philips.lighting.hue.sdk.PHBridgeSearchManager
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.hue.sdk.PHSDKListener
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import org.jetbrains.annotations.Nullable
import te.philips_hue.config_file.ConfigFileHandler

/**
 * This is a wrapper around the PHHueSDK class, and other Philips Hue classes, abstracting away the
 * necessary startup/shutdown logic (along with other SDK features).
 */
@Slf4j
@CompileStatic
class HueSDKManager {

    private HueSDKManager() {}

    /**
     * Configures the Hue SDK's appName & deviceName and adds a shutdown hook to destroy the SDK
     * reference when the JVM is terminated.
     */
    //TODO: Is there a clear advantage to keeping this static?  It is abnormal and that comes with a cost
    static void initSDK(String appName, @Nullable File configFile) {
        if (!isSdkConnected()) {
            PHHueSDK.create().with {
                setAppName(appName)
                setDeviceName("${System.getProperty('user.name')}@${System.getProperty('os.name')}")
            }

            addShutdownHook { shutdown() }

            if (configFile) {
                ConfigFileHandler.initWithSpecificConfigFile(configFile)
            } else {
                ConfigFileHandler.initWithTempDirConfigFile()
            }
        }
    }

    /**
     * Registers a an implementation of PHSDKListener with the Hue SDK.
     * <p>Guards against registering the same listener twice.
     */
    static void registerConnectionListener(PHSDKListener listener) {
        PHHueSDK.getInstance().getNotificationManager().with {
            unregisterSDKListener(listener)
            registerSDKListener(listener)
        }
    }

    static boolean configFileHasValidCredentials() {
        ConfigFileHandler.getInstance().configFileIsStillValid()
    }

    static void connectToBridgeUsingConfigFileCredentials() {
        log.info("Connecting to Hue bridge using credentials from config file.")
        PHHueSDK.getInstance().connect(
                ConfigFileHandler.getInstance().readAccessPointFromConfigFile()
        )
    }

    /**
     * This triggers a UPNP/Portal search over the network for all Hue bridges.
     * <p>The search can take up to 10 seconds to complete.
     * <p>Upon completion of the search, the PHSDKListener.onBridgeConnected() method will be called.
     *
     * @see BridgeConnectionListener#onBridgeConnected(com.philips.lighting.model.PHBridge, java.lang.String)
     */
    static void triggerBridgeSearch() {
        log.info("Searching for Hue bridges...")

        PHHueSDK.getInstance()
                .getSDKService(PHHueSDK.SEARCH_BRIDGE)
                .asType(PHBridgeSearchManager)
                .search(true, true)             // void search(isUpnpSearch, isPortalSearch)
    }

    /**
     * This calls the necessary methods to gracefully terminate the SDK.
     * <p>If this method is called after the SDK has already been shutdown nothing happens.
     */
    static void shutdown() {
        if (isSdkConnected()) {
            log.info("Shutting down the Hue SDK.")
            PHHueSDK.getInstance().with {
                stopPushlinkAuthentication()
                disableAllHeartbeat()
                destroySDK()
            }
            ConfigFileHandler.getInstance().shutdown()
        }
    }

    //TODO: Test this method
    @TypeChecked(TypeCheckingMode.SKIP)
    private static final boolean isSdkConnected() {
        return PHHueSDK.@instance as Boolean
    }
}
