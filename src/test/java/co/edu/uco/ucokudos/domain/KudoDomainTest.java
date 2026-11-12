package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KudoDomainTest {

    private final KudoDomain domain = new KudoDomain();

    private String codeOf(final Runnable action) {
        return assertThrows(DomainException.class, action::run).getCode();
    }

    private boolean allowed(final Role from, final Role to) {
        try {
            domain.validateRoleCanSend(from, to);
            return true;
        } catch (final DomainException e) {
            assertEquals(MessageCodes.KUDO_ROLE_NOT_ALLOWED, e.getCode());
            return false;
        }
    }

    @Test
    void blankContentIsRejected() {
        assertEquals(MessageCodes.KUDO_CONTENT_REQUIRED, codeOf(() -> domain.validateContent("  ")));
        assertEquals(MessageCodes.KUDO_CONTENT_REQUIRED, codeOf(() -> domain.validateContent(null)));
        assertDoesNotThrow(() -> domain.validateContent("x".repeat(100_000)));
    }

    @Test
    void studentSendsOnlyToStudentAndTeacher() {
        assertTrue(allowed(Role.STUDENT, Role.STUDENT));
        assertTrue(allowed(Role.STUDENT, Role.TEACHER));
        assertFalse(allowed(Role.STUDENT, Role.PROGRAM_COORDINATOR));
        assertFalse(allowed(Role.STUDENT, Role.FACULTY_COORDINATOR));
        assertFalse(allowed(Role.STUDENT, Role.RECTOR));
    }

    @Test
    void teacherSendsToTeacherAndAbove() {
        assertFalse(allowed(Role.TEACHER, Role.STUDENT));
        assertTrue(allowed(Role.TEACHER, Role.TEACHER));
        assertTrue(allowed(Role.TEACHER, Role.RECTOR));
    }

    @Test
    void coordinatorsAndAboveSendToEveryoneExceptAdmin() {
        for (final Role from : List.of(Role.PROGRAM_COORDINATOR, Role.FACULTY_COORDINATOR,
                Role.ACADEMIC_COORDINATOR, Role.RECTOR)) {
            for (final Role to : Role.values()) {
                assertEquals(to != Role.ADMINISTRATOR, allowed(from, to), from + " to " + to);
            }
        }
    }

    @Test
    void administratorDoesNotTakePart() {
        for (final Role other : Role.values()) {
            assertFalse(allowed(Role.ADMINISTRATOR, other));
            assertFalse(allowed(other, Role.ADMINISTRATOR));
        }
    }

    @Test
    void dailyLimitRejectsTheFiftyFirst() {
        assertDoesNotThrow(() -> domain.validateDailyLimit(49, 50));
        assertEquals(MessageCodes.KUDO_DAILY_LIMIT, codeOf(() -> domain.validateDailyLimit(50, 50)));
    }

    @Test
    void attachmentLimits() {
        final Attachment pdf = new Attachment("a.pdf", "application/pdf", new byte[10]);
        assertDoesNotThrow(() -> domain.validateAttachments(Collections.nCopies(5, pdf)));
        assertEquals(MessageCodes.KUDO_ATTACHMENT_TOO_MANY,
                codeOf(() -> domain.validateAttachments(Collections.nCopies(6, pdf))));
        assertEquals(MessageCodes.KUDO_ATTACHMENT_TYPE,
                codeOf(() -> domain.validateAttachments(List.of(new Attachment("a.exe", "application/x-msdownload", new byte[1])))));
        final Attachment big = new Attachment("b.png", "image/png", new byte[6 * 1024 * 1024]);
        assertEquals(MessageCodes.KUDO_ATTACHMENT_TOO_LARGE, codeOf(() -> domain.validateAttachments(List.of(big, big))));
    }

    private static void assertTrue(final boolean value) {
        org.junit.jupiter.api.Assertions.assertTrue(value);
    }

    private static void assertFalse(final boolean value) {
        org.junit.jupiter.api.Assertions.assertFalse(value);
    }
}
