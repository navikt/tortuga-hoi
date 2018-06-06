package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.skatt.BeregnetSkatt;

public class BeregnetSkattMapper {
    public BeregnetSkatt mapToBeregnetSkatt(no.nav.opptjening.skatt.client.BeregnetSkatt beregnetSkatt) {

        return BeregnetSkatt.newBuilder()
                .setPersonidentifikator(beregnetSkatt.getPersonidentifikator())
                .setInntektsaar(beregnetSkatt.getInntektsaar())
                .setPersoninntektBarePensjonsdel(beregnetSkatt.getPersoninntektBarePensjonsdel())
                .setPersoninntektFiskeFangstFamiliebarnehage(beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage())
                .setPersoninntektLoenn(beregnetSkatt.getPersoninntektLoenn())
                .setPersoninntektNaering(beregnetSkatt.getPersoninntektNaering())
                .setSvalbardLoennLoennstrekkordningen(beregnetSkatt.getSvalbardLoennLoennstrekkordningen())
                .setSvalbardPersoninntektNaering(beregnetSkatt.getSvalbardPersoninntektNaering())
                .build();
    }
}
