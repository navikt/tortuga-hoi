package no.nav.opptjening.skatt.client.api;

import static no.nav.opptjening.skatt.client.api.HttpLogger.logBadGateway;
import static no.nav.opptjening.skatt.client.api.HttpLogger.logReceivingResponse;
import static no.nav.opptjening.skatt.client.api.HttpLogger.logSendingRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import no.nav.opptjening.skatt.client.exceptions.BadGateway;

public class JsonApi {

    private final HttpClient client;
    private final JsonDeserializer jsonDeserializer;
    private final HttpErrorHandler errorHandler;
    private final AuthenticationHeader authenticationHeader;

    JsonApi(HttpClient client, JsonDeserializer jsonDeserializer, HttpErrorHandler errorHandler, AuthenticationHeader authenticationHeader) {
        this.client = client;
        this.jsonDeserializer = jsonDeserializer;
        this.errorHandler = errorHandler;
        this.authenticationHeader = authenticationHeader;
    }

    public String fetch(String endepunkt) {
        HttpRequest request = buildRequest(endepunkt);
        HttpResponse<String> response;
        try {
            int MAX_ATTEMPTS = 4;
            int attempts = 1;
            do {
                response = fetch(request);
                try {
                    if (response.statusCode() != 200) {
                        errorHandler.handleError(response);
                    }
                } catch (BadGateway ex) {
                    logBadGateway(attempts);
                    TimeUnit.MILLISECONDS.sleep(attempts * 200 * 2);
                    attempts++;
                }
            } while (response.statusCode() != 200 && attempts <= MAX_ATTEMPTS);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body();
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
