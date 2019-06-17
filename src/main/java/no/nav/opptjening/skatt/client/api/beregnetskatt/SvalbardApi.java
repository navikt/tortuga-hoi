package no.nav.opptjening.skatt.client.api.beregnetskatt;

import no.nav.opptjening.skatt.client.api.JsonApi;


import java.util.Optional;

import static no.nav.opptjening.skatt.client.api.SvalbardLonnExtractor.finnLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen;

public class SvalbardApi {

    private final String endepunkt;
    private final JsonApi jsonApi;

    public SvalbardApi(String endepunkt, JsonApi jsonApi) {
        this.endepunkt = endepunkt;
        this.jsonApi = jsonApi;
    }


    Optional<Long> fetchSvalbardLoennsInntekt(String rettighetspakke, String inntektsaar, String personidentifikator){
        return finnLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen(jsonApi.fetch(String.format("%s%s/%s/%s", endepunkt, rettighetspakke, inntektsaar, personidentifikator)));
    }
}
