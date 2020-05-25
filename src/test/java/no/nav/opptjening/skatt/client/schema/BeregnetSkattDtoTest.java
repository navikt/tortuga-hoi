package no.nav.opptjening.skatt.client.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class BeregnetSkattDtoTest {

    @Test
    void that_Mapping_Works_With_Default_Values() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\"\n" +
                "}\n";

        BeregnetSkattDto beregnetSkatt = mapper.readValue(jsonString, BeregnetSkattDto.class);
        assertEquals("12345678901", beregnetSkatt.getPersonidentifikator());
        assertEquals("2016", beregnetSkatt.getInntektsaar());
        assertNull(beregnetSkatt.getPersoninntektLoenn().orElse(null));
        assertNull(beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertNull(beregnetSkatt.getPersoninntektNaering().orElse(null));
        assertNull(beregnetSkatt.getPersoninntektBarePensjonsdel().orElse(null));
        assertNull(beregnetSkatt.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertNull(beregnetSkatt.getSvalbardPersoninntektNaering().orElse(null));
        assertNull(beregnetSkatt.getKildeskattPaaLoennPersoninntektLoenn().orElse(null));
        assertNull(beregnetSkatt.getKildeskattPaaLoennPersoninntektBarePensjonsdel().orElse(null));
    }

    @Test
    void that_Mapping_Works() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"personinntektLoenn\": 490000,\n" +
                "    \"personinntektFiskeFangstFamiliebarnehage\": 90000,\n" +
                "    \"personinntektNaering\": 70000,\n" +
                "    \"personinntektBarePensjonsdel\": 40000,\n" +
                "    \"svalbardLoennLoennstrekkordningen\": 123456,\n" +
                "    \"svalbardPersoninntektNaering\": 654321," +
                "    \"kildeskattPaaLoennPersoninntektLoenn\": 10000," +
                "    \"kildeskattPaaLoennPersoninntektBarePensjonsdel\": 10000," +
                "    \"skjermet\": false\n" +
                "}\n";

        BeregnetSkattDto beregnetSkatt = mapper.readValue(jsonString, BeregnetSkattDto.class);
        assertEquals("12345678901", beregnetSkatt.getPersonidentifikator());
        assertEquals("2016", beregnetSkatt.getInntektsaar());
        assertEquals((Long) 490000L, beregnetSkatt.getPersoninntektLoenn().orElse(null));
        assertEquals((Long) 90000L, beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals((Long) 70000L, beregnetSkatt.getPersoninntektNaering().orElse(null));
        assertEquals((Long) 40000L, beregnetSkatt.getPersoninntektBarePensjonsdel().orElse(null));
        assertEquals((Long) 123456L, beregnetSkatt.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals((Long) 654321L, beregnetSkatt.getSvalbardPersoninntektNaering().orElse(null));
        assertEquals((Long) 10000L, beregnetSkatt.getKildeskattPaaLoennPersoninntektLoenn().orElse(null));
        assertEquals((Long) 10000L, beregnetSkatt.getKildeskattPaaLoennPersoninntektBarePensjonsdel().orElse(null));
    }

    @Test
    void that_Mapping_Works_With_Some_Null_And_Some_Long_Values() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"personinntektLoenn\": 490000,\n" +
                "    \"personinntektFiskeFangstFamiliebarnehage\": 90000,\n" +
                "    \"skjermet\": false\n" +
                "}\n";

        BeregnetSkattDto beregnetSkatt = mapper.readValue(jsonString, BeregnetSkattDto.class);
        assertEquals("12345678901", beregnetSkatt.getPersonidentifikator());
        assertEquals("2016", beregnetSkatt.getInntektsaar());
        assertEquals((Long) 490000L, beregnetSkatt.getPersoninntektLoenn().orElse(null));
        assertEquals((Long) 90000L, beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertNull(beregnetSkatt.getPersoninntektNaering().orElse(null));
        assertNull(beregnetSkatt.getPersoninntektBarePensjonsdel().orElse(null));
        assertNull(beregnetSkatt.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertNull(beregnetSkatt.getSvalbardPersoninntektNaering().orElse(null));
        assertNull(beregnetSkatt.getKildeskattPaaLoennPersoninntektLoenn().orElse(null));
        assertNull(beregnetSkatt.getKildeskattPaaLoennPersoninntektBarePensjonsdel().orElse(null));
    }
}
