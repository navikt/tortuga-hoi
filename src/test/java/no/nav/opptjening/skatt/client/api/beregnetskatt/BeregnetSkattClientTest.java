package no.nav.opptjening.skatt.client.api.beregnetskatt;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.exceptions.BadRequestException;
import no.nav.opptjening.skatt.client.exceptions.ClientException;
import no.nav.opptjening.skatt.client.exceptions.ServerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class BeregnetSkattClientTest {

    private final WireMockServer wireMockRule = new WireMockServer();

    private BeregnetSkattClient beregnetSkattClient;

    @BeforeEach
    void setUp() {
        wireMockRule.start();
        JsonApi jsonApi = new JsonApi(()->"my-api-key");
        this.beregnetSkattClient = new BeregnetSkattClient(null, "http://localhost:" + wireMockRule.port() + "/", jsonApi);
    }

    @AfterEach
    void tearDown() {
        wireMockRule.stop();
    }

    @Test
    void when_ResponseIsOk_Then_CorrectValuesAreMappedOk() {
        String jsonBody = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"personinntektLoenn\": 490000,\n" +
                "    \"personinntektFiskeFangstFamiliebarnehage\": 90000,\n" +
                "    \"personinntektNaering\": 70000,\n" +
                "    \"personinntektBarePensjonsdel\": 40000,\n" +
                "    \"svalbardLoennLoennstrekkordningen\": 123456,\n" +
                "    \"svalbardPersoninntektNaering\": 123456\n" +
                "}\n";

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345678901"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.okJson(jsonBody)));

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav","2016", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals((Long) 490000L, result.getPersoninntektLoenn().orElse(null));
        assertEquals((Long) 90000L, result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals((Long) 70000L, result.getPersoninntektNaering().orElse(null));
        assertEquals((Long) 40000L, result.getPersoninntektBarePensjonsdel().orElse(null));
        assertEquals((Long) 123456L, result.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals((Long) 123456L, result.getSvalbardPersoninntektNaering().orElse(null));
    }

    @Test
    void when_ResponseIsOkAndContainsSubsetOfFields_Then_CorrectValuesAreMappedOk() {
        String jsonBody = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"personinntektLoenn\": 490000\n" +
                "}\n";

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345678901"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.okJson(jsonBody)));

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav","2016", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals((Long) 490000L, result.getPersoninntektLoenn().orElse(null));
        assertNull(result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertNull(result.getPersoninntektNaering().orElse(null));
        assertNull(result.getPersoninntektBarePensjonsdel().orElse(null));
        assertNull(result.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertNull(result.getSvalbardPersoninntektNaering().orElse(null));
    }

    @Test
    void when_ResponseIsOkAndContainsMoreThanWhatWeNeed_Then_CorrectValuesAreMappedOk() {
        String jsonBody = "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"sumSaerfradrag\": 80000,\n" +
                "    \"skjermingsfradrag\": 1000,\n" +
                "    \"formuePrimaerbolig\": 2000000,\n" +
                "    \"samletGjeld\": 50000,\n" +
                "    \"personinntektLoenn\": 490000,\n" +
                "    \"personinntektPensjon\": 100000,\n" +
                "    \"personinntektFiskeFangstFamiliebarnehage\": 90000,\n" +
                "    \"personinntektNaering\": 70000,\n" +
                "    \"personinntektBarePensjonsdel\": 40000,\n" +
                "    \"personinntektBareSykedel\": 30000,\n" +
                "    \"samletPensjon\": 10700,\n" +
                "    \"pensjonsgrad\": 100.1,\n" +
                "    \"antallMaanederPensjon\": 12,\n" +
                "    \"tolvdeler\": 12,\n" +
                "    \"skatteklasse\": \"1E\",\n" +
                "    \"nettoformue\": 300000,\n" +
                "    \"nettoinntekt\": 400000,\n" +
                "    \"utlignetSkatt\": 100000,\n" +
                "    \"grunnlagTrinnskatt\": 500000,\n" +
                "    \"svalbardGjeld\": 234567,\n" +
                "    \"svalbardLoennLoennstrekkordningen\": 123456,\n" +
                "    \"svalbardPensjonLoennstrekkordningen\": 123456,\n" +
                "    \"svalbardPersoninntektNaering\": 123456,\n" +
                "    \"svalbardLoennUtenTrygdeavgiftLoennstrekkordningen\": 123456,\n" +
                "    \"svalbardSumAllePersoninntekter\": 1234567,\n" +
                "    \"svalbardNettoformue\": 123456,\n" +
                "    \"svalbardNettoinntekt\": 123456,\n" +
                "    \"svalbardUtlignetSkatt\": 34567,\n" +
                "    \"svalbardUfoeretrygdLoennstrekkordningen\": 4564,\n" +
                "    \"grunnlagTrinnskattUtenomPersoninntekt\": 324231,\n" +
                "    \"personinntektUfoeretrygd\": 32232\n" +
                "}\n";

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345678901"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.okJson(jsonBody)));

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav","2016", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals((Long) 490000L, result.getPersoninntektLoenn().orElse(null));
        assertEquals((Long) 90000L, result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals((Long) 70000L, result.getPersoninntektNaering().orElse(null));
        assertEquals((Long) 40000L, result.getPersoninntektBarePensjonsdel().orElse(null));
        assertEquals((Long) 123456L, result.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals((Long) 123456L, result.getSvalbardPersoninntektNaering().orElse(null));
    }

    @Test
    void when_ResponseFailedWithFeilmelding_Then_ThrowMappedException() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.badRequest().withBody("{\"kode\": \"BSA-005\", \"melding\": \"Det forespurte inntektsåret er ikke støttet\", \"korrelasjonsId\": \"foobar\"}")));

        try {
            beregnetSkattClient.getBeregnetSkatt("nav","2016", "12345");
            fail("Expected an BadRequestException to be thrown");
        } catch (BadRequestException e) {
            // ok
        }
    }

    @Test
    void when_ResponseFailedWithGeneric4xxError_Then_ThrowClientException() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.badRequest().withBody("bad request")));

        try {
            beregnetSkattClient.getBeregnetSkatt("nav","2016", "12345");
            fail("Expected an ClientException to be thrown");
        } catch (ClientException e) {
            // ok
        }
    }

    @Test
    void when_ResponseFailedWithGeneric5xxError_Then_ThrowServerException() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.serverError().withBody("internal server error")));

        try {
            beregnetSkattClient.getBeregnetSkatt("nav","2016", "12345");
            fail("Expected an ServerException to be thrown");
        } catch (ServerException e) {
            // ok
        }
    }
}
