package no.nav.opptjening.skatt.client.api.beregnetskatt;

import no.nav.opptjening.skatt.client.SvalbardLoennsInntekt;
import no.nav.opptjening.skatt.client.api.JsonApi;

import static no.nav.opptjening.skatt.client.api.SvalbardLonnExtractor.fetchLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen;

public class SvalbardApi {
    private final String endepunkt;
    private final JsonApi jsonApi;

    public SvalbardApi(String endepunkt, JsonApi jsonApi) {
        this.endepunkt = endepunkt;
        this.jsonApi = jsonApi;
    }

    SvalbardLoennsInntekt fetchSvalbardLoennsInntekt(String inntektsaar, String personidentifikator) {
        //api-gw legger for summertskattegrunnlag på "rettigspakke" selv
        return fetchLoennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen(jsonApi.fetch(String.format("%s%s/%s", endepunkt, inntektsaar, personidentifikator)));
    }

}
