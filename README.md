# simple-hue-bridge-service

A library to simplify the process of connecting to a Hue bridge and storing the credentials for future use.

<br/>

## What this app does:

- Initializes the Philips Hue SDK
- Searches for a Hue bridge over the network 
- Handles PushLink authentication
- Stores the credentials returned by the Hue bridge in a temp. file 
- Uses the stored credentials on subsequent executions of the library, so connecting is fast 

<br/>

## Adding this to your project:

Make the following changes to your `build.gradle` file:

- Add ```maven { url "https://jitpack.io" }``` to the ```repositories``` closure
    -   *NOTE:* It's important that this is declared last in the the ```repositories``` closure
- Add ```compile 'com.github.todd-elvers:simple-hue-bridge-service:1.2.0'``` to the ```dependencies``` closure

## Using this in your project:

The only class you need to call from this library is `HueBridgeService`.  This class will take care of
initializing the SDK, adding a hook to shutdown the SDK, authenticating with the bridge, and storing 
the bridge credentials. 

To initialize the `HueBridgeService` simply provide the name of your application
and a callback that will be executed once the service has successfully connected to a bridge.

In Groovy:
```groovy
HueBridgeService hueBridgeService = HueBridgeService.createWithBridgeConnectionCallback("<your-app-name>") {
    // Callback that will execute when the bridge has connected
}

hueBridgeService.findAndConnectToBridge()
```


In Java:
```java
HueBridgeService hueBridgeService = HueBridgeService.createWithBridgeConnectionCallback("<your-app-name>", () -> {
    // Callback that will execute when the bridge has connected
});

hueBridgeService.findAndConnectToBridge();
```


When the `HueBridgeService` instance is no longer of use, call `hueBridgeService.shutdown()` to terminate the connection to the bridge.
<br/>

