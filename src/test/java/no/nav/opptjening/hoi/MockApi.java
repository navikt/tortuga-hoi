package no.nav.opptjening.hoi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

class MockApi {
    private static final WireMockServer wireMockServer = new WireMockServer();

    static void start() {
        wireMockServer.start();
    }

    static void stop() {
        wireMockServer.stop();
    }

    static int port() {
        return wireMockServer.port();
    }

    private static MappingBuilder buildMappingBuilder(String testUrl) {
        return WireMock.get(WireMock.urlPathEqualTo(testUrl))
                .withHeader("X-Nav-Apikey", WireMock.equalTo("foobar"));
    }

    static void initBeregnetSkatt() {
        WireMock.stubFor(buildMappingBuilder("/nav/2017/01029804032")
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"01029804032\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 350371,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

         WireMock.stubFor(buildMappingBuilder("/nav/2018/11987654321")
                .willReturn(WireMock.notFound().withBody("{\n" +
                        "  \"kode\": \"BSA-006\",\n" +
                        "  \"melding\": \"Fant ikke Beregnet Skatt for gitt inntektsår og identifikator\",\n" +
                        "  \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                        "}")));
    }
}
