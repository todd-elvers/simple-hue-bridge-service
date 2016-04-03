package te.philips_hue

class RunLocally {
    static void main(String... args) {
        def hueBridgeService = HueBridgeService.createWithBridgeConnectionCallback("Test") {
            println "Connected to bridge - everything functioning correctly."
            System.exit(0)
        }

        hueBridgeService.findAndConnectToBridge()
    }
}
