package no.nav.opptjening.skatt.client.api.beregnetskatt;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
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
        JsonApi jsonApiSkatt = JsonApiBuilder.createJsonApi(() -> "my-api-key");
        JsonApi jsonApiSvalbard = JsonApiBuilder.createJsonApi(() -> "my-api-key");

        SvalbardApi svalbardApi = new SvalbardApi("http://localhost:" + wireMockRule.port() + "/", jsonApiSvalbard);
        this.beregnetSkattClient = new BeregnetSkattClient(svalbardApi, "http://localhost:" + wireMockRule.port() + "/", jsonApiSkatt);
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

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals(490000L, result.getPersoninntektLoenn().orElse(null));
        assertEquals(90000L, result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals(70000L, result.getPersoninntektNaering().orElse(null));
        assertEquals(40000L, result.getPersoninntektBarePensjonsdel().orElse(null));
        assertEquals(123456L, result.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals(123456L, result.getSvalbardPersoninntektNaering().orElse(null));
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

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals(490000L, result.getPersoninntektLoenn().orElse(null));
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

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals(490000L, result.getPersoninntektLoenn().orElse(null));
        assertEquals(90000L, result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals(70000L, result.getPersoninntektNaering().orElse(null));
        assertEquals(40000L, result.getPersoninntektBarePensjonsdel().orElse(null));
        assertEquals(123456L, result.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals(123456L, result.getSvalbardPersoninntektNaering().orElse(null));
    }

    @Test
    void when_ResponseIsOkAndOfkildeskattVariant_Then_CorrectValuesAreMappedOk() {
        Long kildeskattPaaLoennPersoninntektLoenn = 1000L;
        Long kildeskattPaaLoennPersoninntektBarePensjonsdel = 1000L;
        String jsonBody = "{\n" +
                "  \"personidentifikator\": \"12345678910\",\n" +
                "  \"inntektsaar\": \"2016\",\n" +
                "  \"skjermet\": false,\n" +
                "  \"kildeskattPaaLoennNettoinntekt\": 12345,\n" +
                "  \"kildeskattPaaLoennNettoformue\": 0,\n" +
                "  \"kildeskattPaaLoennBetaltSkattOgAvgift\": 11973,\n" +
                "  \"kildeskattPaaLoennPersoninntektLoenn\": " + kildeskattPaaLoennPersoninntektLoenn + ",\n" +
                "  \"kildeskattPaaLoennPersoninntektBarePensjonsdel\": " + kildeskattPaaLoennPersoninntektBarePensjonsdel + "\n" +
                "}";

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345678910"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.okJson(jsonBody)));

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345678910");

        assertEquals("12345678910", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals(kildeskattPaaLoennPersoninntektLoenn + kildeskattPaaLoennPersoninntektBarePensjonsdel, result.getPersoninntektLoenn().orElse(null));
    }

    @Test
    void should_sum_PersoninntektLoenn_KildeskattPaaLoennPersoninntektLoenn_and_KildeskattPaaLoennPersoninntektBarePensjonsdel_into_personinntektLoenn() {
        Long personinntektLoenn = 2000L;
        Long kildeskattPaaLoennPersoninntektLoenn = 1000L;
        Long kildeskattPaaLoennPersoninntektBarePensjonsdel = 1000L;
        String jsonBody = "{\n" +
                "  \"personidentifikator\": \"12345678910\",\n" +
                "  \"inntektsaar\": \"2016\",\n" +
                "  \"skjermet\": false,\n" +
                "  \"personinntektLoenn\": " + personinntektLoenn + ",\n" +
                "  \"kildeskattPaaLoennPersoninntektLoenn\": " + kildeskattPaaLoennPersoninntektLoenn + ",\n" +
                "  \"kildeskattPaaLoennPersoninntektBarePensjonsdel\": " + kildeskattPaaLoennPersoninntektBarePensjonsdel + "\n" +
                "}";

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345678910"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.okJson(jsonBody)));

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345678910");

        Long sumPersoninntektLoenn = personinntektLoenn + kildeskattPaaLoennPersoninntektLoenn + kildeskattPaaLoennPersoninntektBarePensjonsdel;
        assertEquals(sumPersoninntektLoenn, result.getPersoninntektLoenn().orElse(null));
    }

    @Test
    void when_ResponseFailedWithFeilmelding_Then_ThrowMappedException() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(
                        WireMock.badRequest().withBody("{\"kode\": \"BSA-005\", \"melding\": \"Det forespurte inntektsåret er ikke støttet\", \"korrelasjonsId\": \"foobar\"}")));

        try {
            beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345");
            fail("Expected an BadRequestException to be thrown");
        } catch (BadRequestException e) {
            // ok
        }
    }

    @Test
    void when_ResponseFailedWithBadGatawayThenRetry() {
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
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .inScenario("retryscenario")
                .willReturn(WireMock.serverError().withStatus(502).withBody("Bad Gateway"))
                .willSetStateTo("Retry1"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .inScenario("retryscenario")
                .willReturn(WireMock.serverError().withStatus(502).withBody("Bad Gateway"))
                .whenScenarioStateIs("Retry1")
                .willSetStateTo("Retry2"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .inScenario("retryscenario")
                .willReturn(WireMock.serverError().withStatus(502).withBody("Bad Gateway"))
                .whenScenarioStateIs("Retry2")
                .willSetStateTo("CauseSuccess"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .inScenario("retryscenario")
                .whenScenarioStateIs("CauseSuccess")
                .willReturn(WireMock.okJson(jsonBody)));

        var result = beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals(490000L, result.getPersoninntektLoenn().orElse(null));
        assertEquals(90000L, result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals(70000L, result.getPersoninntektNaering().orElse(null));
        assertEquals(40000L, result.getPersoninntektBarePensjonsdel().orElse(null));
        assertEquals(123456L, result.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals(123456L, result.getSvalbardPersoninntektNaering().orElse(null));
    }

    @Test
    void when_ResponseFailedWithGeneric4xxError_Then_ThrowClientException() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2016/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.badRequest().withBody("bad request")));

        try {
            beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345");
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
            beregnetSkattClient.getBeregnetSkatt("nav", "2016", "12345");
            fail("Expected an ServerException to be thrown");
        } catch (ServerException e) {
            // ok
        }
    }


    @Test
    void when_InntektsaarIs2018_Then_CallSvalbardApi() {
        Long svalbardBelop = 1000L;
        String svalbardApiResponse = getSvalbardApiResponse(svalbardBelop);

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2018/12345"))
                .willReturn(WireMock.okJson(getSkattResponse())));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/2018/12345"))
                .willReturn(WireMock.okJson(svalbardApiResponse)));

        var result = beregnetSkattClient.getBeregnetSkatt("nav", "2018", "12345");

        assertEquals(svalbardBelop, result.getSvalbardLoennLoennstrekkordningen().orElse(null));
    }

    @Test
    void when_ResponseFromBeregnetSkattFailedWithClientExceptionAndCodeBSA006_Then_ReturnBeregnetSkattWithSvalbardInntektInntektsaarAndPersonidentifikator() {
        Long svalbardBelop = 1000L;
        String inntekstsaar = "2018";
        String personidentifikator = "12345";


        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2018/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.notFound().withBody(getBSA006ErrorMessage())));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/2018/12345"))
                .willReturn(WireMock.okJson(getSvalbardApiResponse(svalbardBelop))));

        var result = beregnetSkattClient.getBeregnetSkatt("nav", inntekstsaar, personidentifikator);

        assertEquals(svalbardBelop, result.getSvalbardLoennLoennstrekkordningen().orElse(null));
        assertEquals(inntekstsaar, result.getInntektsaar());
        assertEquals(personidentifikator, result.getPersonidentifikator());
    }

    @Test
    void when_ResponseFromBeregnetSkattFailedWithClientExceptionAndNotCodeBSA006_Then_RethrowException() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2018/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.notFound().withBody("some error")));

        assertThrows(ClientException.class, () -> beregnetSkattClient.getBeregnetSkatt("nav", "2018", "12345"));
    }

    @Test
    void when_ResponseFromBeregnetSkattFailedsWithClientCodeBSA005_Then_InntektsarIkkeStottetExceptionShouldBeThrown() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2018/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.badRequest().withBody(getBSA005ErrorMessage())));

        assertThrows(InntektsarIkkeStottetException.class, () -> beregnetSkattClient.getBeregnetSkatt("nav", "2018", "12345"));
    }

    @Test
    void when_ResponseFromBeregnetSkattFailedWithClientExceptionAndCodeBSA006AndsvalbardApiFailsWithException_Then_RethrowExceptionFromBeregnetSkatt() {
        String beregnetSkattErrorMessage = getBSA006ErrorMessage();

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2018/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.notFound().withBody(getBSA006ErrorMessage())));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/2018/12345"))
                .willReturn(WireMock.notFound().withBody("Det finnes ikke skattegrunnlag for gitt personidentifikator og år")));

        Throwable exception = assertThrows(ClientException.class, () -> beregnetSkattClient.getBeregnetSkatt("nav", "2018", "12345"));

        assertEquals(getJsonString(beregnetSkattErrorMessage, "$.kode"), getJsonString(exception.getMessage(), "$.kode"));
    }

    @Test
    void when_ResponseFromBeregnetSkattFailedWithClientExceptionAndCodeBSA006AndNoSvalbardLoennIsFound_Then_RethrowException() {
        String responseWithoutSvalbardLoenn = "{ \"skjermet\": false}";

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2018/12345"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("my-api-key"))
                .willReturn(WireMock.notFound().withBody(getBSA006ErrorMessage())));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/2018/12345"))
                .willReturn(WireMock.okJson(responseWithoutSvalbardLoenn)));

        assertThrows(ClientException.class, () -> beregnetSkattClient.getBeregnetSkatt("nav", "2018", "12345"));
    }

    private String getSkattResponse() {
        return "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"personinntektLoenn\": 490000,\n" +
                "    \"personinntektFiskeFangstFamiliebarnehage\": 90000,\n" +
                "    \"personinntektNaering\": 70000,\n" +
                "    \"personinntektBarePensjonsdel\": 40000,\n" +
                "    \"svalbardLoennLoennstrekkordningen\": 123456,\n" +
                "    \"svalbardPersoninntektNaering\": 123456\n" +
                "}\n";
    }

    private String getSvalbardApiResponse(Long belop) {
        return "{\n" +
                "\"personidentifikator\": \"12345678910\",\n" +
                "\"inntektsaar\": \"2018\",\n" +
                "\"skjermet\": false,\n" +
                "\t\"svalbardGrunnlag\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"tekniskNavn\": \"loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen\",\n" +
                "\t\t\t\"beloep\":" + belop + "\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
    }

    private String getBSA006ErrorMessage() {
        return "{\n" +
                " \"kode\": \"BSA-006\",\n" +
                " \"melding\": \"Fant ikke Beregnet Skatt for gitt inntektsår og identifikator\",\n" +
                " \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                "}";
    }

    private String getBSA005ErrorMessage() {
        return "{\n" +
                " \"kode\": \"BSA-005\",\n" +
                " \"melding\": \"Det forespurte inntektsåret er ikke støttet\",\n" +
                " \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                "}";
    }

    private String getJsonString(String message, String path) {
        var configuration = Configuration.builder().build();
        var documentContext = JsonPath.using(configuration).parse(message);
        return documentContext.read(path);
    }

}
