package no.nav.opptjening.skatt.client;

import java.util.Optional;

public class SvalbardLoennsInntekt {
    private Long inntekt;
    private Boolean skjermet;

    public SvalbardLoennsInntekt(Long inntekt, Boolean skjermet) {
        this.inntekt = inntekt;
        this.skjermet = skjermet;
    }

    public Optional<Long> getSvalbardLoennsInntekt() {
        return Optional.ofNullable(inntekt);
    }

    public Optional<Boolean> isSkjermet() {
        return Optional.ofNullable(skjermet);
    }
}
