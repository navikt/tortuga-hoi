package no.nav.opptjening.skatt.client.api;

class ResponseUnmappableException extends RuntimeException {
    ResponseUnmappableException(String message, Throwable cause) {
        super(message, cause);
    }
}
