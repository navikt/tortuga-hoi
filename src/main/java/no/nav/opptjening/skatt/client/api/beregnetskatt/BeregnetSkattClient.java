package no.nav.opptjening.skatt.client.api.beregnetskatt;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import no.nav.opptjening.skatt.client.BeregnetSkatt;
import no.nav.opptjening.skatt.client.BeregnetSkattMapper;
import no.nav.opptjening.skatt.client.SvalbardLoennsInntekt;
import no.nav.opptjening.skatt.client.api.JsonApi;
import no.nav.opptjening.skatt.client.exceptions.ClientException;

public class BeregnetSkattClient {
    private static final String ERROR_CODE_PATH = "$.kode";
    private static final String BSA_006 = "BSA-006";

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
                return beregnetSkatt.withSvalbardLoenn(svalbardApi.fetchSvalbardLoennsInntekt(inntektsaar, personidentifikator).getSvalbardLoennsInntekt().orElse(null));
            }
            return beregnetSkatt;
        } catch (ClientException exception) {
            if (isSvalbardLoennInSkattegrunnlag(inntektsaar) && exception.getHttpStatus() == 404 && isErrorCode(exception, BSA_006)) {
                var svalbardLoenn = fetchSvalbardLoennsInntektOrRethrowException(exception, inntektsaar, personidentifikator);
                return new BeregnetSkatt(personidentifikator, inntektsaar, svalbardLoenn.getSvalbardLoennsInntekt().orElseThrow(() -> exception), svalbardLoenn.isSkjermet().orElseThrow(() -> exception));
            }
            throw exception;
        }
    }

    private boolean isSvalbardLoennInSkattegrunnlag(String inntektsaar) {
        return Integer.parseInt(inntektsaar) > 2017;
    }

    private boolean isErrorCode(ClientException exception, String code) {
        var configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
        var documentContext = JsonPath.using(configuration).parse(exception.getMessage());
        String errorCode = documentContext.read(ERROR_CODE_PATH);
        return errorCode != null && errorCode.equals(code);
    }

    private SvalbardLoennsInntekt fetchSvalbardLoennsInntektOrRethrowException(ClientException exception, String inntektsaar, String personidentifikator) {
        try {
            return svalbardApi.fetchSvalbardLoennsInntekt(inntektsaar, personidentifikator);
        } catch (Exception e) {
            throw exception;
        }
    }
}
