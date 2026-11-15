package co.edu.uco.ucokudos.infrastructure.input.configuration;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAuthErrorHandlerTest {

    private final ApiAuthErrorHandler handler = new ApiAuthErrorHandler(messages());

    private static StaticMessageSource messages() {
        final StaticMessageSource source = new StaticMessageSource();
        final Locale locale = Locale.of("es", "CO");
        source.addMessage(MessageCodes.AUTH_USER_NOT_REGISTERED, locale, "No registrada");
        source.addMessage(MessageCodes.AUTH_TOKEN_INVALID, locale, "Sesión \"inválida\"");
        source.addMessage(MessageCodes.AUTH_FORBIDDEN, locale, "Sin permiso");
        return source;
    }

    @Test
    void anUnregisteredUserGets401WithTheirOwnCode() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(new MockHttpServletRequest(), response,
                new OAuth2AuthenticationException(new OAuth2Error(MessageCodes.AUTH_USER_NOT_REGISTERED)));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"auth.user.not.registered\""));
        assertTrue(response.getContentAsString().contains("No registrada"));
    }

    @Test
    void anyOtherAuthenticationFailureIsAnInvalidSessionWithAnEscapedMessage() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(new MockHttpServletRequest(), response, new BadCredentialsException("x"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"auth.token.invalid\""));
        assertTrue(response.getContentAsString().contains("Sesión \\\"inválida\\\""));
    }

    @Test
    void accessDeniedIs403() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("x"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"auth.forbidden\""));
    }
}
