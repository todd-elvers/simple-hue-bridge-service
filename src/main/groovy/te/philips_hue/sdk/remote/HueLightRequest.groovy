package te.philips_hue.sdk.remote

class HueLightRequest {
    static HueLightRequest forLight(int lightNumber) {
        return new HueLightRequest(url: "lights/$lightNumber/state")
    }

    Map body
    String url
    String method = "PUT"

    HueLightRequest withUrl(String url) {
        this.url = url?.startsWith("/") ? url[1..-1] : url
        this
    }

    HueLightRequest withMethod(String method) {
        this.method = method
        this
    }

    HueLightRequest withBody(Map body) {
        this.body = body
        this
    }

    HueLightRequest withColor(HueColor hueColor) {
        this.body = hueColor.mapValues
        this
    }
}