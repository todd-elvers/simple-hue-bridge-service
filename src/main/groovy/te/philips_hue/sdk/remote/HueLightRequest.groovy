package te.philips_hue.sdk.remote

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

@CompileStatic
class HueLightRequest {
    static HueLightRequest forLight(int lightNumber) {
        return new HueLightRequest().withUrl("lights/$lightNumber/state")
    }

    static HueLightRequest forGroup(int groupNumber = 0) {
        return new HueLightRequest().withUrl("groups/$groupNumber/action")
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

    String toJSON(String bridgeId) {
        Map requestAsMap = [
                bridgeId   : bridgeId,
                clipCommand: [
                        url   : "/api/0/" + url,
                        method: method,
                        body  : body
                ]
        ]

        return new JsonBuilder(requestAsMap).toString()
    }

}