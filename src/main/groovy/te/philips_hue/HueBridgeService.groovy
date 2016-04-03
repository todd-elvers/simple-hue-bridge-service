package te.philips_hue

import com.philips.lighting.hue.sdk.PHSDKListener
import te.philips_hue.callbacks.BridgeConnectedCallback

/**
 * <p>This class simplifies utilizing the Hue SDK by abstracting some boilerplate/configuration and
 * providing a simple temp-file storage feature to store bridge credentials for faster subsequent
 * connections.
 *
 * <p>If your only concern is getting connected to a Hue bridge, use <pre>HueBridgeService.createWithBridgeConnectionCallback()</pre>
 * and provide whatever code you wish to have executed in your implementation of BridgeConnectedCallback.
 *
 * <p>If you want finer control over connecting to the bridge and storing the credentials, use <pre>HueBridgeService.createWithCustomSDKListener()</pre>
 * and provide whatever PHSDKListener implementation you wish.
 *
 * After a successful connection to a Hue bridge, the credentials are stored in a temporary file on
 * machine. The next time the app using this class starts up, the credentials from the config file
 * will be tried first, usually resulting in a sub-second connection time.
 */
class HueBridgeService {

    static HueBridgeService createWithBridgeConnectionCallback(String appName, BridgeConnectedCallback callback) {
        new HueBridgeService(appName, callback)
    }

    static HueBridgeService createWithCustomSDKListener(String appName, PHSDKListener customSDKListener) {
        new HueBridgeService(appName, customSDKListener)
    }

    private HueBridgeService(String appName, BridgeConnectedCallback callback) {
        HueSDKManager.initSDKIfNecessary(appName)
        HueSDKManager.registerSDKListener(new HueSDKEventListener(callback))
    }

    private HueBridgeService(String appName, PHSDKListener customSDKListener) {
        HueSDKManager.initSDKIfNecessary(appName)
        HueSDKManager.registerSDKListener(customSDKListener)
    }

    void findAndConnectToBridge() {
        if(HueSDKManager.configFileHasValidCredentials()) {
            HueSDKManager.connectToBridgeUsingConfigFileCredentials()
        } else {
            HueSDKManager.triggerBridgeSearch()
        }
    }

}
