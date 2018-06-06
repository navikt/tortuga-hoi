package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.Fastlandsinntekt;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.Svalbardinntekt;
import no.nav.opptjening.schema.skatt.BeregnetSkatt;
import org.jetbrains.annotations.NotNull;


public class PensjonsgivendeInntektMapper {
    @NotNull
    public PensjonsgivendeInntekt toPensjonsgivendeInntekt(@NotNull BeregnetSkatt beregnetSkatt) {
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
