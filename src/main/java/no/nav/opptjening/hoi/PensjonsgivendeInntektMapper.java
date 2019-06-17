package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.Fastlandsinntekt;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.Svalbardinntekt;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import org.apache.kafka.streams.kstream.ValueMapperWithKey;

class PensjonsgivendeInntektMapper implements ValueMapperWithKey<HendelseKey, BeregnetSkatt, PensjonsgivendeInntekt> {

    @Override
    public PensjonsgivendeInntekt apply(HendelseKey key, BeregnetSkatt beregnetSkatt) {
        if (beregnetSkatt == null) {
            return null;
        }

        return PensjonsgivendeInntekt.newBuilder()
                .setPersonidentifikator(key.getIdentifikator())
                .setInntektsaar(key.getGjelderPeriode())
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
