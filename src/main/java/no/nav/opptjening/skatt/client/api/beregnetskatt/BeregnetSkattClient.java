package no.nav.opptjening.skatt.client.api.beregnetskatt;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.BeregnetSkattMapper;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.exceptions.ClientException;

import java.util.Optional;

public class BeregnetSkattClient {

    private final SvalbardApi svalbardApi;
    private final BeregnetSkattApi beregnetSkattApi;

    public BeregnetSkattClient(SvalbardApi svalbardApi, String endepunkt, JsonApi jsonApi) {
        this.svalbardApi = svalbardApi;
        beregnetSkattApi = new BeregnetSkattApi(jsonApi, endepunkt);
    }

    public BeregnetSkatt getBeregnetSkatt(String rettighetspakke, String inntektsaar, String personidentifikator) {
        try {
            var beregnetSkatt = new BeregnetSkattMapper().mapToBeregnetSkatt(beregnetSkattApi.getBeregnetSkatt(rettighetspakke, inntektsaar, personidentifikator));
            if (isSvalbardLoennInSkattegrunnlag(inntektsaar)) {
                return beregnetSkatt.withSvalbardLoenn(svalbardApi.fetchSvalbardLoennsInntekt(inntektsaar, personidentifikator).orElse(null));
            }
            return beregnetSkatt;
        } catch (ClientException exception) {
            if (isSvalbardLoennInSkattegrunnlag(inntektsaar) && exception.getHttpStatus() == 404 && isKodeBSA006(exception.getMessage())) {
                var svalbardLoenn = fetchSvalbardLoennsInntektOrRethrowException(exception, inntektsaar, personidentifikator);
                return new BeregnetSkatt(personidentifikator, inntektsaar, svalbardLoenn.orElseThrow(() -> exception));
            }
            throw exception;
        }
    }

    private boolean isSvalbardLoennInSkattegrunnlag(String inntektsaar) {
        return Integer.parseInt(inntektsaar) > 2017;
    }

    private boolean isKodeBSA006(String errorMessage) {
        var configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
        var documentContext = JsonPath.using(configuration).parse(errorMessage);
        String errorKode = documentContext.read("$.kode");
        return errorKode != null && errorKode.equals("BSA-006");
    }

    private Optional<Long> fetchSvalbardLoennsInntektOrRethrowException(ClientException exception, String inntektsaar, String personidentifikator) {
        try {
            return svalbardApi.fetchSvalbardLoennsInntekt(inntektsaar, personidentifikator);
        } catch (Exception e) {
            throw exception;
        }
    }
}
