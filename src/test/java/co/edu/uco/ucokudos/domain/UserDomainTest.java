package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDomainTest {

    private static final List<String> DOMAINS = List.of("uco.net.co");

    private final UserDomain domain = new UserDomain();

    private String codeOf(final Runnable action) {
        return assertThrows(DomainException.class, action::run).getCode();
    }

    @Test
    void activeUserPassesValidateActive() {
        final User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        assertDoesNotThrow(() -> domain.validateActive(user));
    }

    @Test
    void pendingOrInactiveUserFailsValidateActive() {
        final User user = new User();
        assertEquals(MessageCodes.USER_NOT_ACTIVE, codeOf(() -> domain.validateActive(user)));
        user.setStatus(UserStatus.INACTIVE);
        assertEquals(MessageCodes.USER_NOT_ACTIVE, codeOf(() -> domain.validateActive(user)));
    }

    @Test
    void activationNeedsEmailAndPhoneVerified() {
        final User user = new User();
        assertEquals(MessageCodes.USER_NOT_VERIFIED, codeOf(() -> domain.validateCanActivate(user)));
        user.setEmailVerified(true);
        assertEquals(MessageCodes.USER_NOT_VERIFIED, codeOf(() -> domain.validateCanActivate(user)));
        user.setPhoneVerified(true);
        assertDoesNotThrow(() -> domain.validateCanActivate(user));
    }

    @Test
    void blankNameIsRejected() {
        assertEquals(MessageCodes.USER_NAME_REQUIRED, codeOf(() -> domain.validateName(null)));
        assertEquals(MessageCodes.USER_NAME_REQUIRED, codeOf(() -> domain.validateName("  ")));
        assertDoesNotThrow(() -> domain.validateName("Ana Perez"));
    }

    @Test
    void institutionalEmailIsAcceptedIgnoringCase() {
        assertDoesNotThrow(() -> domain.validateEmail("ana.perez@uco.net.co", DOMAINS));
        assertDoesNotThrow(() -> domain.validateEmail("Ana.Perez@UCO.NET.CO", DOMAINS));
    }

    @Test
    void notInstitutionalEmailIsRejectedAndReportsTheAllowedDomains() {
        final DomainException error = assertThrows(DomainException.class, () -> domain.validateEmail("ana@gmail.com", DOMAINS));
        assertEquals(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL, error.getCode());
        assertArrayEquals(new String[]{"uco.net.co"}, error.getArgs());
    }

    @Test
    void malformedEmailIsRejected() {
        assertEquals(MessageCodes.USER_EMAIL_INVALID, codeOf(() -> domain.validateEmail(null, DOMAINS)));
        assertEquals(MessageCodes.USER_EMAIL_INVALID, codeOf(() -> domain.validateEmail("ana", DOMAINS)));
        assertEquals(MessageCodes.USER_EMAIL_INVALID, codeOf(() -> domain.validateEmail("@uco.net.co", DOMAINS)));
        assertEquals(MessageCodes.USER_EMAIL_INVALID, codeOf(() -> domain.validateEmail("ana@", DOMAINS)));
    }

    @Test
    void phoneMustUseInternationalFormat() {
        assertDoesNotThrow(() -> domain.validatePhone("+573001234567"));
        assertEquals(MessageCodes.USER_PHONE_INVALID, codeOf(() -> domain.validatePhone("3001234567")));
        assertEquals(MessageCodes.USER_PHONE_INVALID, codeOf(() -> domain.validatePhone("+57abc")));
        assertEquals(MessageCodes.USER_PHONE_INVALID, codeOf(() -> domain.validatePhone(null)));
    }
}
