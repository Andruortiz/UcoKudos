package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.RegistrationStatus;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.service.input.dto.request.SubmitRegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RegistrationResponse;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitRegistrationUseCaseTest {

    private final User existing = new User();
    private final UsersStub users;
    private final SubmitRegistrationUseCase useCase;

    SubmitRegistrationUseCaseTest() {
        existing.setId(UuidHelper.generate());
        existing.setEmail("ana@uco.net.co");
        users = new UsersStub(id -> Optional.empty()) {
            @Override
            public Optional<User> findByEmail(final String email) {
                return email.equals(existing.getEmail()) ? Optional.of(existing) : super.findByEmail(email);
            }
        };
        useCase = new SubmitRegistrationUseCase(users, new UserDomain(),
                List.of("uco.net.co", "uco.edu.co", "ucokudosgmail.onmicrosoft.com"),
                Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC));
    }

    private static SubmitRegistrationRequest request(final String email) {
        final SubmitRegistrationRequest request = new SubmitRegistrationRequest();
        request.setName("  Pedro Pérez ");
        request.setEmail(email);
        request.setDirectoryId("oid-" + email.trim());
        return request;
    }

    private String codeOf(final SubmitRegistrationRequest request) {
        return assertThrows(DomainException.class, () -> useCase.execute(request)).getCode();
    }

    private User createdUser() {
        return users.saved.values().iterator().next();
    }

    @Test
    void aValidRequestCreatesAnActiveStudentWithoutPhoneLinkedToTheDirectory() {
        final RegistrationResponse response = useCase.execute(request(" Pedro@UCO.net.co "));

        assertEquals(RegistrationStatus.APPROVED, response.status());
        assertEquals(1, users.saved.size());
        final User user = createdUser();
        assertEquals(user.getId(), response.id());
        assertEquals("pedro@uco.net.co", user.getEmail());
        assertEquals("Pedro Pérez", user.getName());
        assertEquals("oid-Pedro@UCO.net.co", user.getDirectoryId());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(Role.STUDENT, user.getRole());
        assertNull(user.getPhone());
        assertTrue(user.isEmailVerified());
        assertFalse(user.isPhoneVerified());
    }

    @Test
    void anInstitutionalTeacherAddressGetsTheTeacherRole() {
        useCase.execute(request("profe@uco.edu.co"));

        assertEquals(Role.TEACHER, createdUser().getRole());
    }

    @Test
    void theDemoDirectoryDomainIsAccepted() {
        useCase.execute(request("ana2@ucokudosgmail.onmicrosoft.com"));

        assertEquals(1, users.saved.size());
    }

    @Test
    void anEmailOutsideTheAllowedDomainsIsRejected() {
        assertEquals(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL, codeOf(request("pedro@gmail.com")));
        assertTrue(users.saved.isEmpty());
    }

    @Test
    void aBlankNameIsRejected() {
        final SubmitRegistrationRequest request = request("pedro@uco.net.co");
        request.setName("   ");

        assertEquals(MessageCodes.USER_NAME_REQUIRED, codeOf(request));
    }

    @Test
    void anExistingAccountBlocksTheSameEmailOrTheSameDirectoryUser() {
        assertEquals(MessageCodes.REGISTRATION_ALREADY_EXISTS, codeOf(request("ana@uco.net.co")));

        useCase.execute(request("pedro@uco.net.co"));
        assertEquals(MessageCodes.REGISTRATION_ALREADY_EXISTS, codeOf(request("PEDRO@uco.net.co")));
        final SubmitRegistrationRequest sameDirectoryUser = request("otro@uco.net.co");
        sameDirectoryUser.setDirectoryId("oid-pedro@uco.net.co");
        assertEquals(MessageCodes.REGISTRATION_ALREADY_EXISTS, codeOf(sameDirectoryUser));
        assertEquals(1, users.saved.size());
    }
}
