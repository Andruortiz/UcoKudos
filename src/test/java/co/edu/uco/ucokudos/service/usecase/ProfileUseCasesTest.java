package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.service.input.dto.request.UpdatePhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileUseCasesTest {

    private final User user = new User();
    private final List<AuditRecord> audits = new ArrayList<>();
    private final UsersStub users = new UsersStub(id -> id.equals(user.getId()) ? Optional.of(user) : Optional.empty());
    private final UpdatePhoneUseCase update = new UpdatePhoneUseCase(users, audits::add, new UserDomain(),
            Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC));

    ProfileUseCasesTest() {
        user.setId(UuidHelper.generate());
        user.setName("Ana");
        user.setEmail("ana@uco.net.co");
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setPhone("+573001234567");
        user.setPhoneVerified(true);
    }

    private UpdatePhoneRequest request(final String phone) {
        final UpdatePhoneRequest request = new UpdatePhoneRequest();
        request.setUserId(user.getId());
        request.setPhone(phone);
        return request;
    }

    @Test
    void profileShowsMyData() {
        final ProfileResponse profile = new GetProfileUseCase(users).execute(user.getId());

        assertEquals("Ana", profile.getName());
        assertEquals("+573001234567", profile.getPhone());
        assertTrue(profile.isPhoneVerified());
    }

    @Test
    void unknownUserIsNotFound() {
        assertEquals(MessageCodes.USER_NOT_FOUND, assertThrows(DomainException.class,
                () -> new GetProfileUseCase(users).execute(UuidHelper.generate())).getCode());
    }

    @Test
    void aNewPhoneIsSavedUnverifiedAndAudited() {
        final ProfileResponse profile = update.execute(request(" +573009998877 "));

        assertEquals("+573009998877", profile.getPhone());
        assertFalse(profile.isPhoneVerified());
        assertEquals("+573009998877", users.saved.get(user.getId()).getPhone());
        assertEquals(1, audits.size());
    }

    @Test
    void theSamePhoneChangesNothing() {
        final ProfileResponse profile = update.execute(request("+573001234567"));

        assertTrue(profile.isPhoneVerified());
        assertTrue(users.saved.isEmpty());
        assertTrue(audits.isEmpty());
    }

    @Test
    void anInvalidPhoneIsRejected() {
        assertEquals(MessageCodes.USER_PHONE_INVALID,
                assertThrows(DomainException.class, () -> update.execute(request("3001234567"))).getCode());
        assertEquals(MessageCodes.USER_PHONE_INVALID,
                assertThrows(DomainException.class, () -> update.execute(request(null))).getCode());
        assertTrue(users.saved.isEmpty());
    }
}
