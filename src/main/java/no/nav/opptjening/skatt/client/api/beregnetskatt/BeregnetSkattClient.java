package no.nav.opptjening.skatt.client.api.beregnetskatt;

import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.BeregnetSkattMapper;
import no.nav.opptjening.skatt.client.api.JsonApi;

import java.util.Optional;

public class BeregnetSkattClient {


    private final SvalbardApi svalbardApi;
    private final BeregnetSkattApi beregnetSkattApi;

    public BeregnetSkattClient(SvalbardApi svalbardApi, String endepunkt, JsonApi jsonApi) {
        this.svalbardApi = svalbardApi;
        beregnetSkattApi = new BeregnetSkattApi(jsonApi, endepunkt);
    }

    public BeregnetSkatt getBeregnetSkatt(String rettighetspakke, String inntektsaar, String personidentifikator) {
        var beregnetSkattUtenSvalbardLoennLoennstrekkordningen = new BeregnetSkattMapper().mapToBeregnetSkatt(beregnetSkattApi.getBeregnetSkatt(rettighetspakke, inntektsaar, personidentifikator));


        if(svalbardLoennIkkeErFlyttetTilSummertSkattegrunnlag(inntektsaar))
            return beregnetSkattUtenSvalbardLoennLoennstrekkordningen;

        Optional<Long> svalbardLoenn = svalbardApi.fetchSvalbardLoennsInntekt(rettighetspakke, inntektsaar, personidentifikator);
        return beregnetSkattUtenSvalbardLoennLoennstrekkordningen.withSvalbardLoenn(svalbardLoenn.orElse(null));
    }

    private boolean svalbardLoennIkkeErFlyttetTilSummertSkattegrunnlag(String inntektsaar) {
        return Integer.parseInt(inntektsaar) < 2018;
    }
}
