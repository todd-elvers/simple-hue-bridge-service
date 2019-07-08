package te.philips_hue.sdk

import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.model.PHBridge
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import te.philips_hue.config_file.ConfigFileHandler

import static com.philips.lighting.hue.sdk.PHMessageType.PUSHLINK_AUTHENTICATION_FAILED
import static com.philips.lighting.hue.sdk.PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED

@Slf4j
@CompileStatic
class PushLinkAuthHandler {

    private final PHHueSDK sdk
    private int authTimeoutCounter
    private boolean isAuthenticating

    PushLinkAuthHandler(PHHueSDK sdk) {
        this.sdk = sdk
        this.authTimeoutCounter = 0
        this.isAuthenticating = false
    }

    /**
     * This callback is triggered when a bridge requires authentication.
     * <p>Authentication just means pressing the pushlink button on top of the bridge within 30 seconds.
     *
     * @param accessPoint the access point requesting authentication via the push-link button
     */
    void startAuthProcess(PHAccessPoint accessPoint) {
        log.info("Authentication required!")
        log.info("You have 30 seconds to press the blue button on your Hue bridge.")
        sdk.startPushlinkAuthentication(accessPoint)

        isAuthenticating = true
        authTimeoutCounter = 30
    }

    /**
     * Finishes the authentication chain by potentially updating the stored credentials.
     */
    void finishAuthProcess(PHBridge bridge, String username) {
        if (isAuthenticating) {
            ConfigFileHandler.getInstance().updateConfigFile(bridge, username)
            isAuthenticating = false
        }
    }

    /**
     * @return if the provided error code is related to authentication then this
     * method returns true after logging the corresponding error message. Otherwise
     * this method returns false and nothing is logged.
     */
    boolean isAuthError(int code) {
        switch (code) {
            case PUSHLINK_BUTTON_NOT_PRESSED:
                if (authTimeoutCounter-- % 5 == 0) {
                    log.info("${authTimeoutCounter + 1} seconds remaining...")
                }
                return true

            case PUSHLINK_AUTHENTICATION_FAILED:
                log.info("Hue bridge push-link button was not pressed in time, authentication failed.")
                HueSDKManager.shutdown()
                return true

            default:
                return false
        }
    }
}
