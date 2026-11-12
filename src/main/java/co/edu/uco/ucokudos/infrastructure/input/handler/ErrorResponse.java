package co.edu.uco.ucokudos.infrastructure.input.handler;

public class ErrorResponse {

    private final String code;
    private final String message;

    public ErrorResponse(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
