package no.nav.opptjening.hoi;

import no.nav.opptjening.hoi.PensjonsgivendeInntektMapper;
import no.nav.opptjening.schema.PensjonsgivendeInntekt;
import no.nav.opptjening.skatt.schema.BeregnetSkatt;
import org.junit.Assert;
import org.junit.Test;

public class PensjonsgivendeInntektMapperTest {
    @Test
    public void toPensjonsgivendeInntekt() throws Exception {
        BeregnetSkatt beregnetSkatt = BeregnetSkatt.newBuilder()
                .withPersonidentifikator("12345678911")
                .withInntektsaar("2018")
                .withPersoninntektLoenn(5678)
                .withPersoninntektFiskeFangstFamiliebarnehage(7890)
                .withPersoninntektNaering(8901)
                .withPersoninntektBarePensjonsdel(9012)
                .withSvalbardLoennLoennstrekkordningen(89012)
                .withSvalbardPersoninntektNaering(123456)
                .withSkjermet(false)
                .build();

        PensjonsgivendeInntekt pensjonsgivendeInntekt = new PensjonsgivendeInntektMapper().toPensjonsgivendeInntekt(beregnetSkatt);

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
