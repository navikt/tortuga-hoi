package no.nav.opptjening.skatt.client.exceptions;

public abstract class HttpException extends IllegalArgumentException {

    private final int httpStatus;

    protected HttpException(int httpStatus, String message) {
        this(httpStatus, message, null);
    }

    HttpException(int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
