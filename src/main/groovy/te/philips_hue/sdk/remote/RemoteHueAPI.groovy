package te.philips_hue.sdk.remote

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import wslite.rest.ContentBuilder
import wslite.rest.RESTClient

@Slf4j
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
        return json?.lights?.keySet()?.collect { it as Integer }
    }

    List<Integer> getColorLightNumbers() {
        def response = hueAPI.get(path: '/api/getbridge', query: [token: authToken, bridgeId: bridgeId])
        def json = new JsonSlurper().parseText(response.contentAsString)
        def colorLights = json.lights.findAll { it.value.state.colormode }
        return colorLights?.keySet()?.collect { it as Integer }
    }

    void execute(HueLightRequest hueRequest) {
        hueAPI.post(path: '/api/sendmessage', query: [token: authToken]) {
            urlenc(clipmessage: hueRequest.toJSON(bridgeId))
        }
    }

}



