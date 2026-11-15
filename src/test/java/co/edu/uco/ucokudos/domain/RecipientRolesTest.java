package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipientRolesTest {

    private final KudoDomain kudoDomain = new KudoDomain();
    private final UserDomain userDomain = new UserDomain();

    @Test
    void recipientRolesFollowTheSendingMatrix() {
        assertEquals(EnumSet.of(Role.STUDENT, Role.TEACHER), kudoDomain.allowedRecipientRoles(Role.STUDENT));
        assertEquals(EnumSet.of(Role.TEACHER, Role.PROGRAM_COORDINATOR, Role.FACULTY_COORDINATOR,
                Role.ACADEMIC_COORDINATOR, Role.RECTOR), kudoDomain.allowedRecipientRoles(Role.TEACHER));
        final Set<Role> everyoneButAdmin = EnumSet.complementOf(EnumSet.of(Role.ADMINISTRATOR));
        assertEquals(everyoneButAdmin, kudoDomain.allowedRecipientRoles(Role.PROGRAM_COORDINATOR));
        assertEquals(everyoneButAdmin, kudoDomain.allowedRecipientRoles(Role.RECTOR));
        assertEquals(EnumSet.noneOf(Role.class), kudoDomain.allowedRecipientRoles(Role.ADMINISTRATOR));
    }

    @Test
    void theListAndTheValidationNeverDisagree() {
        for (final Role from : Role.values()) {
            for (final Role to : Role.values()) {
                final boolean listed = kudoDomain.allowedRecipientRoles(from).contains(to);
                boolean validated = true;
                try {
                    kudoDomain.validateRoleCanSend(from, to);
                } catch (final DomainException e) {
                    validated = false;
                }
                assertEquals(listed, validated, from + " to " + to);
            }
        }
    }

    @Test
    void eachRoleListsOnlyTheRolesItAdministers() {
        assertDoesNotThrow(() -> userDomain.validateCanList(Role.PROGRAM_COORDINATOR, Role.STUDENT));
        assertDoesNotThrow(() -> userDomain.validateCanList(Role.RECTOR, Role.ACADEMIC_COORDINATOR));
        assertEquals(MessageCodes.USER_LIST_NOT_ALLOWED,
                assertThrows(DomainException.class, () -> userDomain.validateCanList(Role.STUDENT, Role.STUDENT)).getCode());
        assertEquals(MessageCodes.USER_LIST_NOT_ALLOWED,
                assertThrows(DomainException.class, () -> userDomain.validateCanList(Role.PROGRAM_COORDINATOR, Role.RECTOR)).getCode());
    }
}
