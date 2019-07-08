package te.philips_hue


import com.philips.lighting.model.PHBridge
import groovy.transform.CompileStatic
import org.jetbrains.annotations.Nullable
import te.philips_hue.sdk.BridgeConnectionListener
import te.philips_hue.sdk.HueSDKManager

import java.util.function.Consumer

/**
 * <p>This class simplifies utilizing the Hue SDK by abstracting some boilerplate/configuration and
 * providing a simple temp-file storage feature to store bridge credentials for faster subsequent
 * connections.
 *
 * After a successful connection to a Hue bridge, the credentials are stored in a temporary file on
 * machine. The next time the app using this class starts up, the credentials from the config file
 * will be tried first, usually resulting in a sub-second connection time.
 */
@CompileStatic
@SuppressWarnings("GrMethodMayBeStatic")
class HueBridgeService {

    HueBridgeService(String appName, Consumer<PHBridge> connectionCallback) {
        this(appName, null, connectionCallback)
    }

    //TODO: This `configFile` should probably be named `credentialsFile`
    HueBridgeService(String appName, @Nullable File configFile, Consumer<PHBridge> connectionCallback) {
        try {
            HueSDKManager.initSDK(appName, configFile)
            HueSDKManager.registerConnectionListener(new BridgeConnectionListener(connectionCallback))
        } catch(Exception ex) {
            HueSDKManager.shutdown()
            throw ex
        }
    }

    /**
     * Starts the process to connect a Philips Hue bridge located on the same LAN.
     *
     * The process involves trying credentials, if any exist, or spinning up a
     * separate thread to scan the network for a bridge and beginning the PushLink
     * authentication process with it.
     */
    void connectToBridgeOnLAN() {
        if(HueSDKManager.configFileHasValidCredentials()) {
            HueSDKManager.connectToBridgeUsingConfigFileCredentials()
        } else {
            HueSDKManager.triggerBridgeSearch()
        }
    }

    /**
     * Terminates the underlying connection to the bridge and the SDK.  Must be called or the
     * JVM will hang on exit.  The only exception is if <pre>System.exit()</pre> is called somewhere.
     */
    void shutdown() {
        HueSDKManager.shutdown()
    }

}
