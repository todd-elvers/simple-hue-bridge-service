package te.philips_hue

class RunLocallyWithSpecificConfigFile {
    static void main(String... args) {
        HueBridgeService hueBridgeService = null

        def callback = {
            println "\nConnected to bridge - everything functioning correctly.\n"
            hueBridgeService.shutdown()
        }

        def configFile = new File(Thread.currentThread().contextClassLoader.getResource("custom_config_file.txt").toURI())
        println configFile

        hueBridgeService = HueBridgeService.createWithBridgeConnectionCallback("Test", configFile, callback)
        hueBridgeService.findAndConnectToBridge()
    }
}
