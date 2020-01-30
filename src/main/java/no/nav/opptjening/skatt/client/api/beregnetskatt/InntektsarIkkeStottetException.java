package no.nav.opptjening.skatt.client.api.beregnetskatt;

import no.nav.opptjening.skatt.client.exceptions.BadRequestException;

public class InntektsarIkkeStottetException extends BadRequestException {
    public InntektsarIkkeStottetException(String message) {
        super(message);
    }
}
