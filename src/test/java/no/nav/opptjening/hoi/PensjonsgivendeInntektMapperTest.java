package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PensjonsgivendeInntektMapperTest {

    @Test
    void toPensjonsgivendeInntektOk() {
        BeregnetSkatt beregnetSkatt = new BeregnetSkatt("12345678911", "2018", 5678L,
                7890L, 8901L, 9012L,
                89012L, 123456L, null);

        PensjonsgivendeInntekt pensjonsgivendeInntekt = new PensjonsgivendeInntektMapper().apply(HendelseKey.newBuilder()
                .setGjelderPeriode(beregnetSkatt.getInntektsaar())
                .setIdentifikator(beregnetSkatt.getPersonidentifikator())
                .build(), beregnetSkatt);

        assertEquals(beregnetSkatt.getPersonidentifikator(), pensjonsgivendeInntekt.getPersonidentifikator());
        assertEquals(beregnetSkatt.getInntektsaar(), pensjonsgivendeInntekt.getInntektsaar());

        assertEquals(beregnetSkatt.getPersoninntektLoenn().orElse(null), pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektLoenn());
        assertEquals(beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null), pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektFiskeFangstFamiliebarnehage());
        assertEquals(beregnetSkatt.getPersoninntektNaering().orElse(null), pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektNaering());
        assertEquals(beregnetSkatt.getPersoninntektBarePensjonsdel().orElse(null), pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektBarePensjonsdel());

        assertEquals(beregnetSkatt.getSvalbardLoennLoennstrekkordningen().orElse(null), pensjonsgivendeInntekt.getSvalbardinntekt().getSvalbardLoennLoennstrekkordningen());
        assertEquals(beregnetSkatt.getSvalbardPersoninntektNaering().orElse(null), pensjonsgivendeInntekt.getSvalbardinntekt().getSvalbardPersoninntektNaering());

    }

    @Test
    void toPensjonsGivendeInntektWithNullOk() {
        BeregnetSkatt beregnetSkatt = null;
        PensjonsgivendeInntekt pensjonsgivendeInntekt = new PensjonsgivendeInntektMapper().apply(null, beregnetSkatt);
        assertNull(pensjonsgivendeInntekt);
    }
}
