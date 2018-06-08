package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.Fastlandsinntekt;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.Svalbardinntekt;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import org.apache.kafka.streams.kstream.ValueMapper;

public class PensjonsgivendeInntektMapper implements ValueMapper<BeregnetSkatt, PensjonsgivendeInntekt> {

    @Override
    public PensjonsgivendeInntekt apply(BeregnetSkatt beregnetSkatt) {
        return PensjonsgivendeInntekt.newBuilder()
                .setPersonidentifikator(beregnetSkatt.getPersonidentifikator())
                .setInntektsaar(beregnetSkatt.getInntektsaar())
                .setFastlandsinntekt(Fastlandsinntekt.newBuilder()
                        .setPersoninntektBarePensjonsdel(beregnetSkatt.getPersoninntektBarePensjonsdel())
                        .setPersoninntektLoenn(beregnetSkatt.getPersoninntektLoenn())
                        .setPersoninntektNaering(beregnetSkatt.getPersoninntektNaering())
                        .setPersoninntektFiskeFangstFamiliebarnehage(beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage())
                        .build())
                .setSvalbardinntekt(Svalbardinntekt.newBuilder()
                        .setSvalbardLoennLoennstrekkordningen(beregnetSkatt.getSvalbardLoennLoennstrekkordningen())
                        .setSvalbardPersoninntektNaering(beregnetSkatt.getSvalbardPersoninntektNaering())
                        .build())
                .build();
    }
}
