package no.nav.opptjening.skatt.client.exceptions;

public class ClientException extends HttpException {

    public ClientException(int httpStatus, String message) {
        this(httpStatus, message, null);
    }

    ClientException(int httpStatus, String message, Throwable cause) {
        super(httpStatus, message, cause);
    }
}
