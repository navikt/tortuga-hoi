package no.nav.opptjening.hoi;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.schema.skatt.hendelsesliste.HendelseKey;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BeregnetSkattMapperTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private BeregnetSkattClient beregnetSkattClient;
    private BeregnetSkattMapper beregnetSkattMapper;

    @Before
    public void setUp() {
        beregnetSkattClient = new BeregnetSkattClient("http://localhost:" + wireMockRule.port() + "/", "foobar");
        beregnetSkattMapper = new BeregnetSkattMapper(beregnetSkattClient);
    }

    @Test
    public void transformReturnsValidBeregnetskatt() throws Exception{
        createMockApi();
        BeregnetSkatt expectedBeregnetSkatt = new BeregnetSkatt("12345678911", "2018", 350371L,
                null, null, null,
                null, null, false);
        Hendelse hendelse = new Hendelse(0L, "12345678911", "2018");
        BeregnetSkatt transformedHendelse = beregnetSkattMapper.apply(HendelseKey.newBuilder()
                .setGjelderPeriode("2018")
                .setIdentifikator("12345678911").build(), hendelse);
        assertEquals(expectedBeregnetSkatt, transformedHendelse);
    }

    @Test
    public void transformReturnsNullWhenFantIkkeBeregnetSkattExceptionIsThrown() {
        createMockApi();
        Hendelse hendelse = new Hendelse(0L, "11987654321", "2014");
        BeregnetSkatt transformedHendelse = beregnetSkattMapper.apply(HendelseKey.newBuilder()
                .setGjelderPeriode("2014")
                .setIdentifikator("11987654321").build(), hendelse);
        assertNull(transformedHendelse);
    }

    private void createMockApi() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2018/12345678911"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"12345678911\",\n" +
                        "  \"inntektsaar\": \"2018\",\n" +
                        "  \"personinntektLoenn\": 350371,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/nav/2014/11987654321"))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"))
                .willReturn(WireMock.notFound().withBody("{\n" +
                        "  \"kode\": \"BSA-006\",\n" +
                        "  \"melding\": \"Fant ikke Beregnet Skatt for gitt inntektsår og identifikator\",\n" +
                        "  \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                "}")));
    }
}
