package no.nav.opptjening.hoi;

import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import org.junit.Assert;
import org.junit.Test;

public class PensjonsgivendeInntektMapperTest {

    @Test
    public void toPensjonsgivendeInntekt() throws Exception {
        BeregnetSkatt beregnetSkatt = new BeregnetSkatt("12345678911", "2018", 5678,
                7890, 8901, 9012,
                89012, 123456, false);

        PensjonsgivendeInntekt pensjonsgivendeInntekt = new PensjonsgivendeInntektMapper().apply(beregnetSkatt);

        Assert.assertEquals(beregnetSkatt.getPersonidentifikator(), pensjonsgivendeInntekt.getPersonidentifikator());
        Assert.assertEquals(beregnetSkatt.getInntektsaar(), pensjonsgivendeInntekt.getInntektsaar());

        Assert.assertEquals(beregnetSkatt.getPersoninntektLoenn(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektLoenn());
        Assert.assertEquals(beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektFiskeFangstFamiliebarnehage());
        Assert.assertEquals(beregnetSkatt.getPersoninntektNaering(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektNaering());
        Assert.assertEquals(beregnetSkatt.getPersoninntektBarePensjonsdel(), (long)pensjonsgivendeInntekt.getFastlandsinntekt().getPersoninntektBarePensjonsdel());

        Assert.assertEquals(beregnetSkatt.getSvalbardLoennLoennstrekkordningen(), (long)pensjonsgivendeInntekt.getSvalbardinntekt().getSvalbardLoennLoennstrekkordningen());
        Assert.assertEquals(beregnetSkatt.getSvalbardPersoninntektNaering(), (long)pensjonsgivendeInntekt.getSvalbardinntekt().getSvalbardPersoninntektNaering());

    }

}
