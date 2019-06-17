package no.nav.opptjening.skatt.client;

import no.nav.opptjening.skatt.client.schema.BeregnetSkattDto;

public class BeregnetSkattMapper {

    public BeregnetSkatt mapToBeregnetSkatt(BeregnetSkattDto beregnetSkattDto) {
        String personidentifikator = beregnetSkattDto.getPersonidentifikator();
        String inntektsaar = beregnetSkattDto.getInntektsaar();
        if (personidentifikator == null) {
            throw new NullPointerException("Personidentifikator is null");
        }
        if (inntektsaar == null) {
            throw new NullPointerException("Inntektsaar is null");
        }
        return new BeregnetSkatt(personidentifikator, inntektsaar,
                beregnetSkattDto.getPersoninntektLoenn().orElse(null),
                beregnetSkattDto.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null),
                beregnetSkattDto.getPersoninntektNaering().orElse(null),
                beregnetSkattDto.getPersoninntektBarePensjonsdel().orElse(null),
                beregnetSkattDto.getSvalbardLoennLoennstrekkordningen().orElse(null),
                beregnetSkattDto.getSvalbardPersoninntektNaering().orElse(null),
                beregnetSkattDto.isSkjermet());
    }
}
