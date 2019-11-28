package no.nav.opptjening.skatt.client.api;

import no.nav.opptjening.skatt.client.api.beregnetskatt.FantIkkeBeregnetSkattException;
import no.nav.opptjening.skatt.client.exceptions.BadGateway;
import no.nav.opptjening.skatt.client.exceptions.BadRequestException;
import no.nav.opptjening.skatt.client.exceptions.ClientException;
import no.nav.opptjening.skatt.client.exceptions.ServerException;
import no.nav.opptjening.skatt.client.schema.FeilmeldingDto;

import java.net.http.HttpResponse;

class HttpErrorHandler {

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
            if (feil != null && "BSA-006".equals(feil.getKode()))
                throw new FantIkkeBeregnetSkattException(response.body());
        } catch (ResponseUnmappableException ignored) {

        }
    }
}
