package no.nav.opptjening.skatt.client.api;

import no.nav.opptjening.skatt.client.api.beregnetskatt.FantIkkeBeregnetSkattException;
import no.nav.opptjening.skatt.client.api.beregnetskatt.InntektsarIkkeStottetException;
import no.nav.opptjening.skatt.client.exceptions.BadGateway;
import no.nav.opptjening.skatt.client.exceptions.BadRequestException;
import no.nav.opptjening.skatt.client.exceptions.ClientException;
import no.nav.opptjening.skatt.client.exceptions.ServerException;
import no.nav.opptjening.skatt.client.schema.FeilmeldingDto;

import java.net.http.HttpResponse;

class HttpErrorHandler {

    private static final String FANT_IKKE_BEREGNET_SKATT = "BSA-006";
    private static final String DET_FORESPURTE_INNTEKTSAARET_ER_IKKE_STOTTET = "BSA-005";
    private final JsonDeserializer jsonDeserializer;

    HttpErrorHandler(JsonDeserializer jsonDeserializer) {
        this.jsonDeserializer = jsonDeserializer;
    }

    void handleError(HttpResponse<String> response) {
        handleSpecialCaseByApiErrorCode(response);
        handleSpecificStatusCodes(response);
        handleClassOfStatusCodes(response);
    }

    private void handleClassOfStatusCodes(HttpResponse<String> response) {
        switch (response.statusCode() / 100) {
            case 4:
                throw new ClientException(response.statusCode(), response.body());
            case 5:
                throw new ServerException(response.statusCode(), response.body());
            default:
                throw new RuntimeException(response.body());
        }
    }

    private void handleSpecificStatusCodes(HttpResponse<String> response) {
        if (response.statusCode() == 400) throw new BadRequestException(response.body());
        else if (response.statusCode() == 502) throw new BadGateway(response.body());
    }

    private void handleSpecialCaseByApiErrorCode(HttpResponse<String> response) {
        try {
            var feil = jsonDeserializer.toObject(response.body(), FeilmeldingDto.class);
            if (feil != null && FANT_IKKE_BEREGNET_SKATT.equals(feil.getKode())) {
                throw new FantIkkeBeregnetSkattException(response.body());
            } else if (feil != null && DET_FORESPURTE_INNTEKTSAARET_ER_IKKE_STOTTET.equals(feil.getKode())) {
                throw new InntektsarIkkeStottetException(response.body());
            }
        } catch (ResponseUnmappableException ignored) {

        }
    }
    /*
        TODO
        400	BSA-005	Det forespurte inntektsåret er ikke støttet
        404	BSA-006	Fant ikke Beregnet Skatt for gitt inntektsår og identifikator
        400	BSA-007	Inntektsår har ikke gyldig format
        400	BSA-008	Personidentifikator har ikke gyldig format
        404	BSA-009	Fant ingen person for gitt identifikator
     */
}
