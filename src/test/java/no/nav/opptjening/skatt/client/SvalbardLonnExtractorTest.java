package no.nav.opptjening.skatt.client;

import org.junit.jupiter.api.Test;

import static no.nav.opptjening.skatt.client.api.SvalbardLonnExtractor.fetchLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvalbardLonnExtractorTest {

    @Test
    void jsonPathFinnerSvalbardInntekten() {
        var lennsinntekt = fetchLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen(jsonBody());
        assertTrue(lennsinntekt.getSvalbardLoennsInntekt().isPresent());
        assertEquals(123456L, lennsinntekt.getSvalbardLoennsInntekt().get());
        assertTrue(lennsinntekt.isSkjermet().isPresent());
        assertEquals(false, lennsinntekt.isSkjermet().get());
    }


    String jsonBody() {
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
}
