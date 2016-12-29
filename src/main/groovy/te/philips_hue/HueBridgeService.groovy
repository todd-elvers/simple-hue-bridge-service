package te.philips_hue

import te.philips_hue.sdk.BridgeConnectedCallback
import te.philips_hue.sdk.HueSDKEventListener
import te.philips_hue.sdk.HueSDKManager

/**
 * <p>This class simplifies utilizing the Hue SDK by abstracting some boilerplate/configuration and
 * providing a simple temp-file storage feature to store bridge credentials for faster subsequent
 * connections.
 *
 * After a successful connection to a Hue bridge, the credentials are stored in a temporary file on
 * machine. The next time the app using this class starts up, the credentials from the config file
 * will be tried first, usually resulting in a sub-second connection time.
 */
class HueBridgeService {

    static HueBridgeService createWithBridgeConnectionCallback(String appName, BridgeConnectedCallback callback) {
        new HueBridgeService(appName, callback)
    }

    private HueBridgeService(String appName, BridgeConnectedCallback callback) {
        HueSDKManager.initSDKIfNecessary(appName)
        HueSDKManager.registerSDKListener(new HueSDKEventListener(callback))
    }

    /**
     * Connects to a Hue bridge on the network by either:
     * <ul>
     *     <li>Reading a bridge's credentials from a properties file in the temp. directory</li>
     *     <li>Searching over the network for a bridge and starting the authentication process</li>
     * </ul>
     *
     * The search process is done on a separate thread so that this method does not block.
     */
    void findAndConnectToBridge() {
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
