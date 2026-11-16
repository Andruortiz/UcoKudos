package co.edu.uco.ucokudos.infrastructure.input.configuration;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class ApiAuthErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Locale LOCALE = Locale.of("es", "CO");

    private final MessageSource messageSource;

    public ApiAuthErrorHandler(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response,
                         final AuthenticationException exception) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        write(response, HttpStatus.UNAUTHORIZED, codeOf(exception));
    }

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response,
                       final AccessDeniedException exception) throws IOException {
        final Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (current != null && current.getAuthorities().stream()
                .anyMatch(authority -> EntraUserConverter.UNREGISTERED.equals(authority.getAuthority()))) {
            write(response, HttpStatus.UNAUTHORIZED, MessageCodes.AUTH_USER_NOT_REGISTERED);
            return;
        }
        write(response, HttpStatus.FORBIDDEN, MessageCodes.AUTH_FORBIDDEN);
    }


    private static String codeOf(final AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth) {
            final String code = oauth.getError().getErrorCode();
            if (code.startsWith("auth.")) {
                return code;
            }
        }
        return MessageCodes.AUTH_TOKEN_INVALID;
    }

    private void write(final HttpServletResponse response, final HttpStatus status, final String code)
            throws IOException {
        final String message = messageSource.getMessage(code, null, LOCALE);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":\"" + escape(code) + "\",\"message\":\"" + escape(message) + "\"}");
    }

    private static String escape(final String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
