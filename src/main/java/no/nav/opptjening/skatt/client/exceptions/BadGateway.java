package no.nav.opptjening.skatt.client.exceptions;

public class BadGateway extends ClientException {
    public BadGateway(String message) {
        super(502, message);
    }
}
