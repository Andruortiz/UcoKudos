package co.edu.uco.ucokudos.crosscutting.exception;

public class DomainException extends RuntimeException {

    private final String code;
    private final String[] args;

    public DomainException(final String code, final String... args) {
        super(code);
        this.code = code;
        this.args = args.clone();
    }

    public String getCode() {
        return code;
    }

    public String[] getArgs() {
        return args.clone();
    }
}
