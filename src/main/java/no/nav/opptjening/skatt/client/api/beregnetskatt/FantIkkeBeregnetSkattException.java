package no.nav.opptjening.skatt.client.api.beregnetskatt;

import no.nav.opptjening.skatt.client.exceptions.NotFoundException;

public class FantIkkeBeregnetSkattException extends NotFoundException {
    public FantIkkeBeregnetSkattException(String message) {
        this(message, null);
    }

    private FantIkkeBeregnetSkattException(String s, Throwable cause) {
        super(s, cause);
    }
}
