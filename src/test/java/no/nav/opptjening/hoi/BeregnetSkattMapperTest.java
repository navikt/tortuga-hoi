package no.nav.opptjening.hoi;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.opptjening.schema.skatt.hendelsesliste.Hendelse;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.api.beregnetskatt.BeregnetSkattClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

        BeregnetSkatt expectedBeregnetSkatt = new BeregnetSkatt("12345678911", "2018", 350371,
                0, 0, 0,
                0, 0, false);

        Hendelse hendelse = new Hendelse(0L, "12345678911", "2018");

        BeregnetSkatt transformedHendelse = beregnetSkattMapper.transform(hendelse);

        assertEquals(expectedBeregnetSkatt, transformedHendelse);
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
    }
}
