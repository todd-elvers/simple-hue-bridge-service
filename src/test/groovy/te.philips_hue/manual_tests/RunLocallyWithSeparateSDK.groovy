package te.philips_hue.manual_tests

import io.github.zeroone3010.yahueapi.Hue

import java.util.concurrent.CompletableFuture

class RunLocallyWithSeparateSDK {

    static void main(String[] args) {
        final String bridgeIp = "192.168.1.99" // Fill in the IP address of your Bridge
        final String appName = "MyFirstHueApp" // Fill in the name of your application
        final CompletableFuture<String> apiKey = Hue.hueBridgeConnectionBuilder(bridgeIp).initializeApiConnection(appName)

        // Push the button on your Hue Bridge to resolve the apiKey future:
        final Hue hue = new Hue(bridgeIp, apiKey.get())
    }

}
