package no.nav.opptjening.skatt.client.exceptions;

public class NotFoundException extends ClientException {

    public NotFoundException(String message) {
        this(message, null);
    }

    protected NotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
}
