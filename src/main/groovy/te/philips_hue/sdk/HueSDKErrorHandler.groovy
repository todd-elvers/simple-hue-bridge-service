package te.philips_hue.sdk

import groovy.util.logging.Slf4j

import static com.philips.lighting.hue.sdk.PHMessageType.*
import static com.philips.lighting.hue.sdk.PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED

/**
 * This class handles errors reported back from the Hue SDK.
 */
@Slf4j
class HueSDKErrorHandler {

    private int pushlinkButtonTimeoutCounter = 0

    /**
     * Handles errors that come back from the Hue SDK.
     *
     * The Hue uses errors as control flow at times, so not every time this method is called has
     * something actually gone wrong.
     */
    void handleError(int code, String message) {
        switch (code) {
            case PUSHLINK_BUTTON_NOT_PRESSED:
                print("\r${pushlinkButtonTimeoutCounter--} seconds remaining...")
                break

            case PUSHLINK_AUTHENTICATION_FAILED:
                log.info("The blue button on the Hue bridge was not pressed in time.  Looking for bridge again...")
                break

            case BRIDGE_NOT_FOUND:
                log.info("No bridge found, trying again.")
                break

            default:
                log.error("Error #$code - $message")
        }
    }

    void resetPushlinkButtonTimer() {
        pushlinkButtonTimeoutCounter = 30
    }

}
