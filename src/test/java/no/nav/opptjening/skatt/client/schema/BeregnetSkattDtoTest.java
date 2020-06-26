package no.nav.opptjening.skatt.client.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class BeregnetSkattDtoTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void that_Mapping_Works_With_Default_Values() throws Exception {
        String json = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\"\n" +
                "}\n";

        BeregnetSkattDto beregnetSkatt = mapper.readValue(json, BeregnetSkattDto.class);
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
        String json = "{\n" +
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

        BeregnetSkattDto beregnetSkatt = mapper.readValue(json, BeregnetSkattDto.class);
        assertEquals("12345678901", beregnetSkatt.getPersonidentifikator());
        assertEquals("2016", beregnetSkatt.getInntektsaar());
        assertEquals(490000L, beregnetSkatt.getPersoninntektLoenn().orElse(null));
        assertEquals(90000L, beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals(70000L, beregnetSkatt.getPersoninntektNaering().orElse(null));
        assertEquals(40000L, beregnetSkatt.getPersoninntektBarePensjonsdel().orElse(null));
        assertEquals(123456L, beregnetSkatt.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals(654321L, beregnetSkatt.getSvalbardPersoninntektNaering().orElse(null));
        assertEquals(10000L, beregnetSkatt.getKildeskattPaaLoennPersoninntektLoenn().orElse(null));
        assertEquals(10000L, beregnetSkatt.getKildeskattPaaLoennPersoninntektBarePensjonsdel().orElse(null));
    }

    @Test
    void that_Mapping_Works_With_Some_Null_And_Some_Long_Values() throws Exception {
        String json = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"personinntektLoenn\": 490000,\n" +
                "    \"personinntektFiskeFangstFamiliebarnehage\": 90000,\n" +
                "    \"skjermet\": false\n" +
                "}\n";

        BeregnetSkattDto beregnetSkatt = mapper.readValue(json, BeregnetSkattDto.class);
        assertEquals("12345678901", beregnetSkatt.getPersonidentifikator());
        assertEquals("2016", beregnetSkatt.getInntektsaar());
        assertEquals(490000L, beregnetSkatt.getPersoninntektLoenn().orElse(null));
        assertEquals(90000L, beregnetSkatt.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertNull(beregnetSkatt.getPersoninntektNaering().orElse(null));
        assertNull(beregnetSkatt.getPersoninntektBarePensjonsdel().orElse(null));
        assertNull(beregnetSkatt.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertNull(beregnetSkatt.getSvalbardPersoninntektNaering().orElse(null));
        assertNull(beregnetSkatt.getKildeskattPaaLoennPersoninntektLoenn().orElse(null));
        assertNull(beregnetSkatt.getKildeskattPaaLoennPersoninntektBarePensjonsdel().orElse(null));
    }

    @Test
    void mapping_works_for_response_of_the_kildeskatt_variant() throws Exception {
        String json = "{\n" +
                "  \"personidentifikator\": \"12345678910\",\n" +
                "  \"inntektsaar\": \"2019\",\n" +
                "  \"skjermet\": false,\n" +
                "  \"kildeskattPaaLoennPersoninntektLoenn\": 24587,\n" +
                "  \"kildeskattPaaLoennPersoninntektBarePensjonsdel\": 123\n" +
                "}";
        BeregnetSkattDto beregnetSkatt = mapper.readValue(json, BeregnetSkattDto.class);
        assertEquals(24587L, beregnetSkatt.getKildeskattPaaLoennPersoninntektLoenn().orElse(null));
        assertEquals(123L, beregnetSkatt.getKildeskattPaaLoennPersoninntektBarePensjonsdel().orElse(null));
    }
}
