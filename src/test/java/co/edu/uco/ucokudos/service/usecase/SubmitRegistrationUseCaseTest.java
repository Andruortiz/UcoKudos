package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.RegistrationRequest;
import co.edu.uco.ucokudos.domain.model.RegistrationStatus;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.request.SubmitRegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RegistrationResponse;
import co.edu.uco.ucokudos.service.output.port.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitRegistrationUseCaseTest {

    private final List<RegistrationRequest> saved = new ArrayList<>();
    private final User existing = new User();
    private final SubmitRegistrationUseCase useCase;

    SubmitRegistrationUseCaseTest() {
        existing.setId(UuidHelper.generate());
        existing.setEmail("ana@uco.net.co");
        final RegistrationRepository registrations = new RegistrationRepository() {
            public RegistrationRequest save(final RegistrationRequest request) { saved.add(request); return request; }
            public boolean existsPendingByEmail(final String email) {
                return saved.stream().anyMatch(r -> r.getEmail().equals(email));
            }
        };
        final UsersStub users = new UsersStub(id -> Optional.empty()) {
            @Override
            public Optional<User> findByEmail(final String email) {
                return email.equals(existing.getEmail()) ? Optional.of(existing) : Optional.empty();
            }
        };
        useCase = new UseCaseFactory().build(registrations, users);
    }

    /** Keeps the constructor call in one place. */
    private static final class UseCaseFactory {
        SubmitRegistrationUseCase build(final RegistrationRepository registrations, final UsersStub users) {
            return new SubmitRegistrationUseCase(registrations, users, new UserDomain(), List.of("uco.net.co"),
                    Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC));
        }
    }

    private static SubmitRegistrationRequest request(final String email, final Role role) {
        final SubmitRegistrationRequest request = new SubmitRegistrationRequest();
        request.setName("  Pedro Pérez ");
        request.setEmail(email);
        request.setPhone("+573001234567");
        request.setRole(role);
        return request;
    }

    private String codeOf(final SubmitRegistrationRequest request) {
        return assertThrows(DomainException.class, () -> useCase.execute(request)).getCode();
    }

    @Test
    void aValidRequestIsSavedAsPendingWithCleanData() {
        final RegistrationResponse response = useCase.execute(request(" Pedro@UCO.net.co ", Role.STUDENT));

        assertEquals(RegistrationStatus.PENDING, response.status());
        assertEquals(1, saved.size());
        assertEquals("pedro@uco.net.co", saved.get(0).getEmail());
        assertEquals("Pedro Pérez", saved.get(0).getName());
    }

    @Test
    void anEmailOutsideTheInstitutionIsRejected() {
        assertEquals(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL, codeOf(request("pedro@gmail.com", Role.STUDENT)));
        assertTrue(saved.isEmpty());
    }

    @Test
    void onlyStudentsAndTeachersCanRegisterThemselves() {
        assertEquals(MessageCodes.REGISTRATION_ROLE_INVALID, codeOf(request("pedro@uco.net.co", Role.RECTOR)));
        assertEquals(MessageCodes.REGISTRATION_ROLE_INVALID, codeOf(request("pedro@uco.net.co", null)));
        useCase.execute(request("pedro@uco.net.co", Role.TEACHER));
        assertEquals(1, saved.size());
    }

    @Test
    void anInvalidPhoneIsRejected() {
        final SubmitRegistrationRequest request = request("pedro@uco.net.co", Role.STUDENT);
        request.setPhone("3001234567");

        assertEquals(MessageCodes.USER_PHONE_INVALID, codeOf(request));
    }

    @Test
    void aBlankNameIsRejected() {
        final SubmitRegistrationRequest request = request("pedro@uco.net.co", Role.STUDENT);
        request.setName("   ");

        assertEquals(MessageCodes.USER_NAME_REQUIRED, codeOf(request));
    }

    @Test
    void anExistingAccountOrAPendingRequestBlocksTheSameEmail() {
        assertEquals(MessageCodes.REGISTRATION_ALREADY_EXISTS, codeOf(request("ana@uco.net.co", Role.STUDENT)));

        useCase.execute(request("pedro@uco.net.co", Role.STUDENT));
        assertEquals(MessageCodes.REGISTRATION_ALREADY_EXISTS, codeOf(request("PEDRO@uco.net.co", Role.STUDENT)));
        assertEquals(1, saved.size());
    }
}
