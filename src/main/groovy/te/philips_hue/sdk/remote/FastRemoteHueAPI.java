package te.philips_hue.sdk.remote;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import groovy.json.JsonBuilder;
import groovy.transform.CompileStatic;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FastRemoteHueAPI {

    private final String authToken;
    private final String bridgeId;

    public FastRemoteHueAPI(String authToken, String bridgeId) {
        this.authToken = authToken;
        this.bridgeId = bridgeId;
    }

    public List<Integer> getColorLightNumbers() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get("https://www.meethue.com/api/getbridge")
                .header("content-type", "application/x-www-form-urlencoded")
                .queryString("token", authToken)
                .queryString("bridgeId", bridgeId)
                .asJson();

        return response.getBody()
                .getObject()
                .getJSONObject("lights")
                .keySet()
                .stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public void execute(HueLightRequest lightRequest) throws UnirestException {
        Unirest.post("https://www.meethue.com/api/sendmessage")
                .header("content-type", "application/x-www-form-urlencoded")
                .queryString("token", authToken)
                .field("clipmessage", lightRequest.toJSON(bridgeId))
                .asJson();
    }

    public void shutdown() throws IOException {
        try {
            Unirest.shutdown();
        } catch(IOException exception) { }
    }

}
