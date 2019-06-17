package no.nav.opptjening.skatt.client.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static no.nav.opptjening.skatt.client.api.HttpLogger.logReceivingResponse;
import static no.nav.opptjening.skatt.client.api.HttpLogger.logSendingRequest;

public class JsonApi {

    private final HttpClient client = HttpClient.newHttpClient();
    private final JsonDeserializer jsonDeserializer = new JsonDeserializer();
    private final HttpErrorHandler errorHandler;
    private final AuthenticationHeader authenticationHeader;

    public JsonApi(AuthenticationHeader authenticationHeader) {
        this.errorHandler = new HttpErrorHandler(jsonDeserializer);
        this.authenticationHeader = authenticationHeader;
    }

    public String fetch(String endepunkt) {
        HttpRequest request = buildRequest(endepunkt);
        try {
            var response = fetch(request);
            if (response.statusCode() != 200) errorHandler.handleError(response);
            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fetchObject(String endepunkt, Class<T> expectedType) {
        return jsonDeserializer.toObject(fetch(endepunkt), expectedType);
    }

    private HttpResponse<String> fetch(HttpRequest request) throws IOException, InterruptedException {
        logSendingRequest(request);
        long requestSentStartTime = now();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logReceivingResponse(response, millisecondsSince(requestSentStartTime));
        return response;
    }

    private long now() {
        return System.nanoTime();
    }

    private long millisecondsSince(long pointInTime) {
        return (now() - pointInTime) / 1000000;
    }

    private HttpRequest buildRequest(String endepunkt) {
        return HttpRequest.newBuilder()
                .GET()
                .header(AuthenticationHeader.APIKEY_HEADER_NAME, authenticationHeader.value())
                .header("Accept", "application/json")
                .uri(URI.create(endepunkt))
                .build();
    }



}
