package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.AccessStatus;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.request.RequestAccessRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestAccessUseCaseTest {

    private final List<String> invited = new ArrayList<>();
    private final User existing = new User();
    private final RequestAccessUseCase useCase;

    RequestAccessUseCaseTest() {
        existing.setId(UuidHelper.generate());
        existing.setEmail("ana@uco.net.co");
        final UsersStub users = new UsersStub(id -> Optional.empty()) {
            @Override
            public Optional<User> findByEmail(final String email) {
                return email.equals(existing.getEmail()) ? Optional.of(existing) : Optional.empty();
            }
        };
        useCase = new RequestAccessUseCase(users, new UserDomain(), List.of("uco.net.co", "uco.edu.co"), email -> {
            invited.add(email);
            return AccessStatus.INVITED;
        });
    }

    private static RequestAccessRequest request(final String email) {
        final RequestAccessRequest request = new RequestAccessRequest();
        request.setEmail(email);
        return request;
    }

    @Test
    void anInstitutionalEmailIsInvitedWithCleanData() {
        assertEquals(AccessStatus.INVITED, useCase.execute(request("  Pedro@UCO.net.co ")).status());
        assertEquals(List.of("pedro@uco.net.co"), invited);
    }

    @Test
    void anEmailOutsideTheInstitutionIsRejectedWithoutInviting() {
        final DomainException error = assertThrows(DomainException.class, () -> useCase.execute(request("x@gmail.com")));

        assertEquals(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL, error.getCode());
        assertTrue(invited.isEmpty());
    }

    @Test
    void aBlankEmailIsRejected() {
        assertEquals(MessageCodes.USER_EMAIL_INVALID,
                assertThrows(DomainException.class, () -> useCase.execute(request(null))).getCode());
    }

    @Test
    void someoneWhoAlreadyHasAnAccountIsNotInvitedAgain() {
        assertEquals(AccessStatus.ALREADY_HAS_ACCESS, useCase.execute(request("ANA@uco.net.co")).status());
        assertTrue(invited.isEmpty());
    }
}
