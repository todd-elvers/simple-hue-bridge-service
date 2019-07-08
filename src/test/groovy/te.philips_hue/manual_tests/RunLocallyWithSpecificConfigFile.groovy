package te.philips_hue.manual_tests

import com.philips.lighting.model.PHBridge
import te.philips_hue.HueBridgeService

//BUG: When user starts push-link auth and then gets timeout the library won't shutdown gracefully
// Pretty sure it has something to do with the heartbeat that is started up in the background.
// Checking by commenting it in/out.  If it is, then find an alternate way to shutdown the
// heartbeat threads.
class RunLocallyWithSpecificConfigFile {

    // Note: this test should always fail
    static void main(String... args) {
        HueBridgeService hueBridgeService = null

        def callback = { PHBridge bridge ->
            println "\nConnected to bridge - everything functioning correctly.\n"
            hueBridgeService.shutdown()
        }

        def configFile = new File(
                Thread.currentThread()
                        .contextClassLoader
                        .getResource("custom_config_file.txt")
                        .toURI()
        )

        hueBridgeService = new HueBridgeService("Test", configFile, callback)
        hueBridgeService.connectToBridgeOnLAN()
    }

}
