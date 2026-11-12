package co.edu.uco.ucokudos.infrastructure.input.handler;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Locale LOCALE = Locale.of("es", "CO");

    private static final Map<String, HttpStatus> STATUS_BY_CODE = Map.of(
            MessageCodes.USER_NOT_FOUND, HttpStatus.NOT_FOUND,
            MessageCodes.CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND,
            MessageCodes.KUDO_IDEMPOTENCY_KEY_REQUIRED, HttpStatus.BAD_REQUEST,
            MessageCodes.KUDO_IDEMPOTENCY_CONFLICT, HttpStatus.CONFLICT,
            MessageCodes.KUDO_SCANNER_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE,
            MessageCodes.KUDO_ROLE_NOT_ALLOWED, HttpStatus.FORBIDDEN);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(final DomainException exception) {
        final HttpStatus status = STATUS_BY_CODE.getOrDefault(exception.getCode(), HttpStatus.UNPROCESSABLE_ENTITY);
        return build(status, exception.getCode(), exception.getArgs());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final MethodArgumentNotValidException exception) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, MessageCodes.REQUEST_INVALID);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ErrorResponse> handleUnreadable(final Exception exception) {
        return build(HttpStatus.BAD_REQUEST, MessageCodes.REQUEST_INVALID);
    }

    private ResponseEntity<ErrorResponse> build(final HttpStatus status, final String code, final Object... args) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, messageSource.getMessage(code, args, LOCALE)));
    }
}
