package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import org.junit.Assert;
import org.junit.Test;

public class PensjonsgivendeInntektMapperTest {

    @Test
    public void toPensjonsgivendeInntekt() throws Exception {
        BeregnetSkatt beregnetSkatt = new BeregnetSkatt("12345678911", "2018", 5678, 7890,
                8901, 9012, 89012, 123456, false);

        PensjonsgivendeInntekt pensjonsgivendeInntekt = new PensjonsgivendeInntektMapper().apply(beregnetSkatt);

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
