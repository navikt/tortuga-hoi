package no.nav.opptjening.hoi;

import no.nav.opptjening.hoi.PensjonsgivendeInntektMapper;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;

import no.nav.opptjening.schema.skatt.BeregnetSkatt;
import org.junit.Assert;
import org.junit.Test;

public class PensjonsgivendeInntektMapperTest {
    @Test
    public void toPensjonsgivendeInntekt() throws Exception {
        BeregnetSkatt beregnetSkatt = BeregnetSkatt.newBuilder()
                .setPersonidentifikator("12345678911")
                .setInntektsaar("2018")
                .setPersoninntektLoenn(5678)
                .setPersoninntektFiskeFangstFamiliebarnehage(7890)
                .setPersoninntektNaering(8901)
                .setPersoninntektBarePensjonsdel(9012)
                .setSvalbardLoennLoennstrekkordningen(89012)
                .setSvalbardPersoninntektNaering(123456)
                .build();

        PensjonsgivendeInntekt pensjonsgivendeInntekt = new PensjonsgivendeInntektMapper().toPensjonsgivendeInntekt(beregnetSkatt);

        Assert.assertEquals(beregnetSkatt.getPersonidentifikator(), pensjonsgivendeInntekt.getPersonidentifikator());
        Assert.assertEquals(beregnetSkatt.getInntektsaar(), pensjonsgivendeInntekt.getInntektsaar());

        Assert.assertEquals((long)beregnetSkatt.getPersoninntektLoenn(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektLoenn());
        Assert.assertEquals((long)beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektFiskeFangstFamiliebarnehage());
        Assert.assertEquals((long)beregnetSkatt.getPersoninntektNaering(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektNaering());
        Assert.assertEquals((long)beregnetSkatt.getPersoninntektBarePensjonsdel(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektBarePensjonsdel());

        Assert.assertEquals((long)beregnetSkatt.getSvalbardLoennLoennstrekkordningen(), (long)pensjonsgivendeInntekt.getSvalbardinntekt().getSvalbardLoennLoennstrekkordningen());
        Assert.assertEquals((long)beregnetSkatt.getSvalbardPersoninntektNaering(), (long)pensjonsgivendeInntekt.getSvalbardinntekt().getSvalbardPersoninntektNaering());

    }

}
