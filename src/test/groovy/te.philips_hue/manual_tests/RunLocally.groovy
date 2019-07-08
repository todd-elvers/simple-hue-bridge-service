package te.philips_hue.manual_tests


import com.philips.lighting.model.PHBridge
import te.philips_hue.HueBridgeService

class RunLocally {

    static void main(String... args) {
        HueBridgeService hueBridgeService = null

        def callback = { PHBridge bridge ->
            println "\nConnected to bridge - everything functioning correctly.\n"
            hueBridgeService.shutdown()
        }

        hueBridgeService = new HueBridgeService("Test", callback)
        hueBridgeService.connectToBridgeOnLAN()
    }

}
