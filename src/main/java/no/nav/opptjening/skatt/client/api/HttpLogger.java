package no.nav.opptjening.skatt.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static no.nav.opptjening.skatt.client.api.AuthenticationHeader.APIKEY_HEADER_NAME;

class HttpLogger {
    private static final Logger LOG = LoggerFactory.getLogger(HttpLogger.class);

    static void logReceivingResponse(HttpResponse<String> response, long elapsedTime) {
        debugIfEnabbled(()->String.format("Received %s for %s in %s ms\n%s", response.statusCode(), response.request().uri(), elapsedTime, headersAsString(response.headers())));
    }

    static void logSendingRequest(HttpRequest request) {
        debugIfEnabbled(()->String.format("%s %s\n%s", request.method(), request.uri(), headersAsString(request.headers())));
    }

    static void logBadGateway(int attempts) {
        LOG.warn("Received 502 Bad Gateway, assuming temporary hiccup and trying again. Attempts:{}", attempts);
    }

    static void debugIfEnabbled(Supplier<String> logMessage){
        if(LOG.isDebugEnabled()) LOG.debug(logMessage.get());
    }

    private static String headersAsString(HttpHeaders headers) {
        return headers
                .map()
                .entrySet()
                .stream()
                .flatMap(entry -> entry
                        .getValue()
                        .stream()
                        .map(value -> headerAsString(entry.getKey(), value))
                ).collect(Collectors.joining("\n"));
    }

    private static String headerAsString(String key, String value) {
        return key + ": " + (key.equals(APIKEY_HEADER_NAME) ? "********" : value);
    }

}
