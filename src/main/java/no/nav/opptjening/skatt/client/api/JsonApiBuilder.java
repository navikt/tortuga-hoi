package no.nav.opptjening.skatt.client.api;

import java.net.http.HttpClient;

public final class JsonApiBuilder {

    private static final JsonDeserializer JSON_DESERIALIZER = new JsonDeserializer();
    private static final HttpErrorHandler HTTP_ERROR_HANDLER = new HttpErrorHandler(JSON_DESERIALIZER);
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private JsonApiBuilder() {
    }

    public static JsonApi createJsonApi(AuthenticationHeader authenticationHeader) {
        return new JsonApi(HTTP_CLIENT, JSON_DESERIALIZER, HTTP_ERROR_HANDLER, authenticationHeader);
    }
}
