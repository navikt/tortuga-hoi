package no.nav.opptjening.skatt.client.api.summertskattegrunnlag;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import no.nav.opptjening.skatt.client.api.beregnetskatt.SvalbardApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class SummertSkattegrunnlagClientTest {

    private static final WireMockServer WIREMOCKSERVER = new WireMockServer(8080);
    private static final String API_KEY = "my-api-key";
    private static BeregnetSkattClient beregnetSkattClient;

    @BeforeAll
    static void setUp(){
        WIREMOCKSERVER.start();
        var jsonApi = JsonApiBuilder.createJsonApi(()->API_KEY);
        var svalbardApi = new SvalbardApi("http://localhost:" + WIREMOCKSERVER.port() + "/summertskatt/nav/", jsonApi );
        beregnetSkattClient = new BeregnetSkattClient(svalbardApi, "http://localhost:" + WIREMOCKSERVER.port() + "/beregnetskatt/", jsonApi);
    }

    private static void stubJsonResponseFromEndpoint(String endpoint, String jsonResponse) {
        WIREMOCKSERVER.stubFor(WireMock.get(WireMock.urlPathEqualTo(endpoint))
                .withHeader("X-Nav-Apikey", WireMock.equalTo(API_KEY))
                .willReturn(WireMock.okJson(jsonResponse)));
    }

    private static void stubErrorResponse(String endpoint) {
        WIREMOCKSERVER.stubFor(WireMock.get(WireMock.urlPathEqualTo(endpoint))
                .withHeader("X-Nav-Apikey", WireMock.equalTo(API_KEY))
                .willReturn(WireMock.badRequest()));
    }

    @AfterAll
    static  void tearDown(){
        WIREMOCKSERVER.stop();
    }

    @Test
    void skaHenteSvalbardLoennFraSummertSkatt() {
        stubJsonResponseFromEndpoint("/summertskatt/nav/2018/12345678901", summertSkattJsonBody());
        stubJsonResponseFromEndpoint("/beregnetskatt/nav/2018/12345678901", beregnetSkattJsonBody());

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav","2018", "12345678901");

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
    void svalbardLoennSkalVaerNullOmDenikkeFinnesISummertSkatt() {
        stubJsonResponseFromEndpoint("/summertskatt/nav/2018/12345678901", summertSkattUtenSvalbardLoennJsonBody());
        stubJsonResponseFromEndpoint("/beregnetskatt/nav/2018/12345678901", beregnetSkattJsonBody());

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav","2018", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals((Long) 490000L, result.getPersoninntektLoenn().orElse(null));
        assertEquals((Long) 90000L, result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals((Long) 70000L, result.getPersoninntektNaering().orElse(null));
        assertEquals((Long) 40000L, result.getPersoninntektBarePensjonsdel().orElse(null));
        assertTrue(result.getSvalbardLoennLoennstrekkordningen().isEmpty());
        assertEquals((Long) 123456L, result.getSvalbardPersoninntektNaering().orElse(null));
    }

    @Test
    void svalbardFeiler() {
        stubErrorResponse("/summertskatt/nav/2018/12345678901");
        stubJsonResponseFromEndpoint("/beregnetskatt/nav/2018/12345678901", beregnetSkattJsonBody());

       assertThrows(RuntimeException.class, ()->beregnetSkattClient.getBeregnetSkatt("nav","2018", "12345678901"));

    }

    @Test
    void skalBareHenteSvalbardInntektFraSummertSkattOmInntektsAarErFoer2018() {
        stubJsonResponseFromEndpoint("/beregnetskatt/nav/2017/12345678901", beregnetSkattJsonBody());

        BeregnetSkatt result = beregnetSkattClient.getBeregnetSkatt("nav","2017", "12345678901");

        assertEquals("12345678901", result.getPersonidentifikator());
        assertEquals("2016", result.getInntektsaar());
        assertEquals((Long) 490000L, result.getPersoninntektLoenn().orElse(null));
        assertEquals((Long) 90000L, result.getPersoninntektFiskeFangstFamiliebarnehage().orElse(null));
        assertEquals((Long) 70000L, result.getPersoninntektNaering().orElse(null));
        assertEquals((Long) 40000L, result.getPersoninntektBarePensjonsdel().orElse(null));
        assertTrue(result.getSvalbardLoennLoennstrekkordningen().isEmpty());
        assertEquals((Long) 123456L, result.getSvalbardPersoninntektNaering().orElse(null));
    }

    private static String beregnetSkattJsonBody() {
        return "{\n" +
                "    \"personidentifikator\": \"12345678901\",\n" +
                "    \"inntektsaar\": \"2016\",\n" +
                "    \"personinntektLoenn\": 490000,\n" +
                "    \"personinntektFiskeFangstFamiliebarnehage\": 90000,\n" +
                "    \"personinntektNaering\": 70000,\n" +
                "    \"personinntektBarePensjonsdel\": 40000,\n" +
                "    \"svalbardPersoninntektNaering\": 123456\n" +
                "}\n";
    }

    private static String summertSkattJsonBody(){
        return "{\n" +
                "  \"personidentifikator\": \"12345678910\",\n" +
                "  \"inntektsaar\": \"2017\",\n" +
                "  \"skjermet\": false,\n" +
                "  \"grunnlag\": [\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samledePaaloepteRenter\",\n" +
                "      \"beloep\": 779981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntektsfradrag\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"andreFradragsberettigedeKostnader\",\n" +
                "      \"beloep\": 59981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntektsfradrag\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samletSkattepliktigOverskuddAvUtleieAvFritidseiendom\",\n" +
                "      \"beloep\": 1609981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"skattepliktigAvkastningEllerKundeutbytte\",\n" +
                "      \"beloep\": 1749981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"skatteoppgjoersdato\": \"2018-10-04\",\n" +
                "  \"svalbardGrunnlag\": [\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samledePaaloepteRenter\",\n" +
                "      \"beloep\": 779981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntektsfradrag\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samletAndelAvInntektIBoligselskapEllerBoligsameie\",\n" +
                "      \"beloep\": 849981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"skattepliktigUtbytteFraAksjerRegistrertIVerdipapirsentralen\",\n" +
                "      \"beloep\": 1779981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen\",\n" +
                "      \"beloep\": 123456,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"skattepliktigAvkastningEllerKundeutbytte\",\n" +
                "      \"beloep\": 1749981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private static String summertSkattUtenSvalbardLoennJsonBody(){
        return "{\n" +
                "  \"personidentifikator\": \"12345678910\",\n" +
                "  \"inntektsaar\": \"2017\",\n" +
                "  \"skjermet\": false,\n" +
                "  \"grunnlag\": [\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samledePaaloepteRenter\",\n" +
                "      \"beloep\": 779981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntektsfradrag\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"andreFradragsberettigedeKostnader\",\n" +
                "      \"beloep\": 59981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntektsfradrag\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samletSkattepliktigOverskuddAvUtleieAvFritidseiendom\",\n" +
                "      \"beloep\": 1609981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"skattepliktigAvkastningEllerKundeutbytte\",\n" +
                "      \"beloep\": 1749981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"skatteoppgjoersdato\": \"2018-10-04\",\n" +
                "  \"svalbardGrunnlag\": [\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samledePaaloepteRenter\",\n" +
                "      \"beloep\": 779981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntektsfradrag\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"samletAndelAvInntektIBoligselskapEllerBoligsameie\",\n" +
                "      \"beloep\": 849981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"skattepliktigUtbytteFraAksjerRegistrertIVerdipapirsentralen\",\n" +
                "      \"beloep\": 1779981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"tekniskNavn\": \"skattepliktigAvkastningEllerKundeutbytte\",\n" +
                "      \"beloep\": 1749981,\n" +
                "      \"kategori\": [\n" +
                "        \"inntekt\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

}
