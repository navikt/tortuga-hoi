package no.nav.opptjening.hoi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.api.JsonApiBuilder;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BeregnetSkattMapperTest {

    private static final String FNR_FANT_IKKE_BEREGNET_SKATT = "11987654321";
    private static final String FNR_FORESPURT_INNTEKTSAAR_IKKE_STOTTET = "12345678923";

    private static final WireMockServer wireMockServer = new WireMockServer(8080);

    private static BeregnetSkattMapper beregnetSkattMapper;

    @BeforeAll
    static void setUp() {
        wireMockServer.start();
        createMockApi();
        JsonApi jsonApi = JsonApiBuilder.createJsonApi(() -> "foobar");
        var beregnetSkattClient = new BeregnetSkattClient(null, "http://localhost:" + wireMockServer.port() + "/", jsonApi);
        beregnetSkattMapper = new BeregnetSkattMapper(beregnetSkattClient);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void transformReturnsValidBeregnetskatt() {
        createMockApi();
        BeregnetSkatt expectedBeregnetSkatt = new BeregnetSkatt("12345678911", "2018", 350371L,
                null, null, null,
                null, null, false);
        Hendelse hendelse = new Hendelse(0L, "12345678911", "2018");
        BeregnetSkatt transformedHendelse = beregnetSkattMapper.apply(HendelseKey.newBuilder()
                .setGjelderPeriode("2017")
                .setIdentifikator("12345678911").build(), hendelse);
        assertEquals(expectedBeregnetSkatt, transformedHendelse);
    }

    @Test
    void transformReturnsNullWhenFantIkkeBeregnetSkattExceptionIsThrown() {
        createMockApi();
        Hendelse hendelse = new Hendelse(0L, FNR_FANT_IKKE_BEREGNET_SKATT, "2014");
        BeregnetSkatt transformedHendelse = beregnetSkattMapper.apply(HendelseKey.newBuilder()
                .setGjelderPeriode("2014")
                .setIdentifikator(FNR_FANT_IKKE_BEREGNET_SKATT).build(), hendelse);
        assertNull(transformedHendelse);
    }

    @Test
    void transformReturnsNullWhenForespurtInntektsaarIkkeErStottet() {
        createMockApi();
        Hendelse hendelse = new Hendelse(0L, FNR_FORESPURT_INNTEKTSAAR_IKKE_STOTTET, "2014");
        BeregnetSkatt transformedHendelse = beregnetSkattMapper.apply(HendelseKey.newBuilder()
                .setGjelderPeriode("2014")
                .setIdentifikator(FNR_FORESPURT_INNTEKTSAAR_IKKE_STOTTET).build(), hendelse);
        assertNull(transformedHendelse);
    }

    private static void createMockApi() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2017/12345678911"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"12345678911\",\n" +
                        "  \"inntektsaar\": \"2018\",\n" +
                        "  \"personinntektLoenn\": 350371,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2014/" + FNR_FANT_IKKE_BEREGNET_SKATT))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.notFound().withBody("{\n" +
                        "  \"kode\": \"BSA-006\",\n" +
                        "  \"melding\": \"Fant ikke Beregnet Skatt for gitt inntektsår og identifikator\",\n" +
                        "  \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                        "}")));
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2014/" + FNR_FORESPURT_INNTEKTSAAR_IKKE_STOTTET))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.badRequest().withBody("{\n" +
                        "  \"kode\": \"BSA-005\",\n" +
                        "  \"melding\": \"Det forespurte inntektsåret er ikke støttet\",\n" +
                        "  \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                        "}")));
    }
}
