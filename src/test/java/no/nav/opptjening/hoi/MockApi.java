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

         WireMock.stubFor(buildMappingBuilder("/nav/2017/04057849687")
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"04057849687\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 350371,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

         WireMock.stubFor(buildMappingBuilder("/nav/2017/09038800237")
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"09038800237\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 192483,\n" +
                        "  \"personinntektNaering\": 23090,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

         WireMock.stubFor(buildMappingBuilder("/nav/2017/01029413157")
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"01029413157\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 195604,\n" +
                        "  \"personinntektFiskeFangstFamiliebarnehage\": 7860,\n" +
                        "  \"personinntektNaering\": 29540,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

         WireMock.stubFor(buildMappingBuilder("/nav/2017/10026300407")
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"10026300407\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 160000,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

         WireMock.stubFor(buildMappingBuilder("/nav/2017/10016000383")
                .willReturn(WireMock.okJson("{\n" +
                        "  \"personidentifikator\": \"10016000383\",\n" +
                        "  \"inntektsaar\": \"2017\",\n" +
                        "  \"personinntektLoenn\": 444800,\n" +
                        "  \"personinntektNaering\": 24600,\n" +
                        "  \"skjermet\": false,\n" +
                        "  \"skatteoppgjoersdato\": \"2018-06-06\"\n" +
                        "}")));

         WireMock.stubFor(buildMappingBuilder("/nav/2017/11987654321")
                .willReturn(WireMock.notFound().withBody("{\n" +
                        "  \"kode\": \"BSA-006\",\n" +
                        "  \"melding\": \"Fant ikke Beregnet Skatt for gitt inntektsår og identifikator\",\n" +
                        "  \"korrelasjonsid\": \"13a865f5-28f9-47db-9abd-ab78977c79fe\"\n" +
                        "}")));
    }
}
