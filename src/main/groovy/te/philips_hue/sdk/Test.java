package te.philips_hue.sdk;

import te.philips_hue.sdk.remote.RemoteHueAPI;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Integer> lightNubmers = new RemoteHueAPI("aFhzQ21MSk9Dc21nTndDNTVEeG9mMGpTa2xodnk2TlhzQzBvbFlEY2lZdz0=", "001788fffe219c38").getLightNumbers();
        lightNubmers.forEach(System.out::println);
    }
}
