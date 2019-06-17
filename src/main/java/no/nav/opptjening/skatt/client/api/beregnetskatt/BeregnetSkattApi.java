package no.nav.opptjening.skatt.client.api.beregnetskatt;

import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.schema.BeregnetSkattDto;

class BeregnetSkattApi {
    private final JsonApi jsonApi;
    private final String endepunkt;

    BeregnetSkattApi(JsonApi jsonApi, String endepunkt) {
        this.jsonApi = jsonApi;
        this.endepunkt = endepunkt;

    }

    BeregnetSkattDto getBeregnetSkatt(String rettighetspakke, String inntektsaar, String pid){
        return jsonApi.fetchObject(String.format("%s%s/%s/%s", endepunkt, rettighetspakke, inntektsaar, pid), BeregnetSkattDto.class);
    }
}
