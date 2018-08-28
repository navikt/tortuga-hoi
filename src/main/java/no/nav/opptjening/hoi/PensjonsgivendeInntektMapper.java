package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.Fastlandsinntekt;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.Svalbardinntekt;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import org.apache.kafka.streams.kstream.ValueMapper;

public class PensjonsgivendeInntektMapper implements ValueMapper<BeregnetSkatt, PensjonsgivendeInntekt> {

    @Override
    public PensjonsgivendeInntekt apply(BeregnetSkatt beregnetSkatt) {

        if(beregnetSkatt == null) return null;

        return PensjonsgivendeInntekt.newBuilder()
                .setPersonidentifikator(beregnetSkatt.getPersonidentifikator())
                .setInntektsaar(beregnetSkatt.getInntektsaar())
                .setFastlandsinntekt(Fastlandsinntekt.newBuilder()
                        .setPersoninntektBarePensjonsdel(beregnetSkatt.getPersoninntektBarePensjonsdel().orElse(null))
                        .setPersoninntektLoenn(beregnetSkatt.getPersoninntektLoenn().orElse(null))
                        .setPersoninntektNaering(beregnetSkatt.getPersoninntektNaering().orElse(null))
                        .setPersoninntektFiskeFangstFamiliebarnehage(beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null))
                        .build())
                .setSvalbardinntekt(Svalbardinntekt.newBuilder()
                        .setSvalbardLoennLoennstrekkordningen(beregnetSkatt.getSvalbardLoennLoennstrekkordningen().orElse(null))
                        .setSvalbardPersoninntektNaering(beregnetSkatt.getSvalbardPersoninntektNaering().orElse(null))
                        .build())
                .build();
    }
}
