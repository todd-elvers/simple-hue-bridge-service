# simple-philips-hue-connector

<br/>

## Goal of this app:

Simplify the process of connecting to a Hue bridge and storing the credentials for future use.

<br/>

## What this app does:

- Looks for a Hue bridge on the LAN
- Tries to connect to it (handling authentication if necessary)
- Stores the Hue bridge credentials in a temp. file
- Reads the temp. file on subsequent executions of this library so startup is fast

<br/>

## Adding this to your project:

Make the following changes to your `build.gradle` file:

- Add ```maven { url "https://jitpack.io" }``` to the ```repositories``` closure 
- Add ```compile 'com.github.todd-elvers:simple-hue-bridge-service:1.0.1'``` to the ```dependencies``` 

## Using this in your project:

#### Simple Case:

To initialize the ```HueBridgeService``` in the case, simply provide the name of your application
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

<br/>

#### Complex Case:

To initialize the ```HueBridgeService``` with more fine-grained control, simply provide your implementation
of ```PHSDKListener```.  <b>Note:</b> This method requires you to handle basically everything.

```groovy
HueBridgeService hueBridgeService = HueBridgeService.createWithCustomSDKListener("<your-app-name>", yourSdkListenerImpl)

hueBridgeService.findAndConnectToBridge()
```
