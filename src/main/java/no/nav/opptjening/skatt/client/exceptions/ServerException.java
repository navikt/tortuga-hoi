package no.nav.opptjening.skatt.client.exceptions;

public class ServerException extends HttpException {

    public ServerException(int httpStatus, String message) {
        this(httpStatus, message, null);
    }

    private ServerException(int httpStatus, String message, Throwable cause) {
        super(httpStatus, message, cause);
    }
}
