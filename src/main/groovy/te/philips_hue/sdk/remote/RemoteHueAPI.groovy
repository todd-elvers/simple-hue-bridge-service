package te.philips_hue.sdk.remote

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import wslite.rest.RESTClient

class RemoteHueAPI {

    private final RESTClient hueAPI = new RESTClient("https://www.meethue.com")
    String authToken, bridgeId

    RemoteHueAPI(String authToken, String bridgeId) {
        hueAPI.setDefaultContentTypeHeader("application/x-www-form-urlencoded")
        this.authToken = authToken
        this.bridgeId = bridgeId
    }

    List<Integer> getLightNumbers() {
        def response = hueAPI.get(path: '/api/getbridge', query: [token: authToken, bridgeId: bridgeId])
        def json = new JsonSlurper().parseText(response.contentAsString)
        return json?.lights?.keySet()?.toList()
    }

    void execute(HueLightRequest hueRequest) {
        hueAPI.post(path: '/api/sendmessage', query: [token: authToken]) {
            def data = [
                    bridgeId   : bridgeId,
                    clipCommand: [
                            url   : "/api/0/" + hueRequest.url,
                            method: hueRequest.method,
                            body  : hueRequest.body
                    ]
            ]

            urlenc(clipmessage: new JsonBuilder(data).toString())
        }
    }

}



