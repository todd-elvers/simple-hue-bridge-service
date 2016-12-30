package te.philips_hue

class RunLocally {
    static void main(String... args) {
        HueBridgeService hueBridgeService = null

        def callback = {
            println "\nConnected to bridge - everything functioning correctly.\n"
            hueBridgeService.shutdown()
            hueBridgeService.shutdown()
        }

        hueBridgeService = HueBridgeService.createWithBridgeConnectionCallback("Test", callback)
        hueBridgeService.findAndConnectToBridge()
    }
}
