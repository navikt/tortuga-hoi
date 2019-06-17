package no.nav.opptjening.skatt.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

import static no.nav.opptjening.skatt.client.api.AuthenticationHeader.APIKEY_HEADER_NAME;

class HttpLogger {
    private static final Logger LOG = LoggerFactory.getLogger(HttpLogger.class);

    static void logReceivingResponse(HttpResponse<String> response, long elapsedTime) {
        LOG.debug("Received {} for {} in {} ms\n{}", response.statusCode(), response.request().uri(), elapsedTime, ToStringAdapter.of(response.headers(), HttpLogger::headersAsString));
    }

    static void logSendingRequest(HttpRequest request) {
        LOG.debug("{} {}\n{}", request.method(), request.uri(), ToStringAdapter.of(request.headers(), HttpLogger::headersAsString));
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
