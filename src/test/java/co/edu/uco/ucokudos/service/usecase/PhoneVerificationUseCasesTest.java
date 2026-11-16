package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.PhoneVerificationDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.AuditAction;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.PhoneVerification;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.service.input.dto.request.ConfirmPhoneRequest;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.PhoneVerificationRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneVerificationUseCasesTest {

    private static final String PHONE = "+573001234567";

    private static final class MutableClock extends Clock {
        Instant now = Instant.parse("2026-11-16T10:00:00Z");

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(final ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
        void advance(final Duration duration) { now = now.plus(duration); }
    }

    private final MutableClock clock = new MutableClock();
    private final Map<UUID, User> store = new HashMap<>();
    private final Map<UUID, PhoneVerification> verifications = new HashMap<>();
    private final List<AuditRecord> audits = new ArrayList<>();
    private final List<String> sentCodes = new ArrayList<>();
    private final List<String> sentPhones = new ArrayList<>();
    private final User user = new User();
    private final RequestPhoneVerificationUseCase request;
    private final ConfirmPhoneVerificationUseCase confirm;

    PhoneVerificationUseCasesTest() {
        user.setId(UuidHelper.generate());
        user.setName("Ana");
        user.setPhone(PHONE);
        user.setRole(Role.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        store.put(user.getId(), user);
        final UsersStub users = new UsersStub(id -> Optional.ofNullable(store.get(id)));
        final PhoneVerificationRepository repository = new PhoneVerificationRepository() {
            public Optional<PhoneVerification> find(final UUID userId) { return Optional.ofNullable(verifications.get(userId)); }
            public void save(final PhoneVerification verification) { verifications.put(verification.getUserId(), verification); }
            public void delete(final UUID userId) { verifications.remove(userId); }
        };
        final AuditRepository auditRepository = audits::add;
        final PhoneVerificationDomain domain = new PhoneVerificationDomain();
        request = new RequestPhoneVerificationUseCase(users, repository, auditRepository, new UserDomain(), domain,
                (phone, code) -> { sentPhones.add(phone); sentCodes.add(code); }, clock,
                Duration.ofMinutes(10), Duration.ofMinutes(1), 3);
        confirm = new ConfirmPhoneVerificationUseCase(users, repository, auditRepository, new UserDomain(), domain,
                clock, 3);
    }

    private ConfirmPhoneRequest confirmation(final String code) {
        final ConfirmPhoneRequest confirmation = new ConfirmPhoneRequest();
        confirmation.setUserId(user.getId());
        confirmation.setCode(code);
        return confirmation;
    }

    private String codeOf(final Runnable action) {
        return assertThrows(DomainException.class, action::run).getCode();
    }

    private String lastCode() {
        return sentCodes.get(sentCodes.size() - 1);
    }

    @Test
    void theCodeIsSixDigitsSentToThePhoneAndStoredOnlyAsAHash() {
        request.execute(user.getId());

        assertEquals(List.of(PHONE), sentPhones);
        assertTrue(lastCode().matches("\\d{6}"), lastCode());
        assertFalse(verifications.get(user.getId()).getCodeHash().contains(lastCode()));
        assertEquals(AuditAction.PHONE_VERIFICATION_SENT, audits.get(0).action());
    }

    @Test
    void theRightCodeVerifiesThePhoneAndClearsTheCode() {
        request.execute(user.getId());

        assertTrue(confirm.execute(confirmation(" " + lastCode() + " ")).isPhoneVerified());
        assertTrue(user.isPhoneVerified());
        assertTrue(verifications.isEmpty());
        assertEquals(AuditAction.PHONE_VERIFIED, audits.get(1).action());
    }

    @Test
    void aWrongCodeCountsAnAttemptAndTooManyBlockThemEvenWithTheRightCode() {
        request.execute(user.getId());
        final String right = lastCode();
        final String wrong = right.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 3; i++) {
            assertEquals(MessageCodes.PHONE_CODE_INVALID, codeOf(() -> confirm.execute(confirmation(wrong))));
        }

        assertEquals(MessageCodes.PHONE_CODE_TOO_MANY, codeOf(() -> confirm.execute(confirmation(right))));
        assertFalse(user.isPhoneVerified());
    }

    @Test
    void anExpiredCodeIsRejected() {
        request.execute(user.getId());
        clock.advance(Duration.ofMinutes(11));

        assertEquals(MessageCodes.PHONE_CODE_EXPIRED, codeOf(() -> confirm.execute(confirmation(lastCode()))));
    }

    @Test
    void confirmingWithoutHavingAskedForACodeIsInvalid() {
        assertEquals(MessageCodes.PHONE_CODE_INVALID, codeOf(() -> confirm.execute(confirmation("123456"))));
    }

    @Test
    void aCodeSentToTheOldNumberDoesNotVerifyTheNewOne() {
        request.execute(user.getId());
        user.setPhone("+573009998877");

        assertEquals(MessageCodes.PHONE_CODE_INVALID, codeOf(() -> confirm.execute(confirmation(lastCode()))));
    }

    @Test
    void askingAgainTooSoonIsRefusedAndAfterTheCooldownItWorks() {
        request.execute(user.getId());

        assertEquals(MessageCodes.PHONE_CODE_TOO_SOON, codeOf(() -> request.execute(user.getId())));
        clock.advance(Duration.ofSeconds(61));
        request.execute(user.getId());

        assertEquals(2, sentCodes.size());
    }

    @Test
    void thereIsALimitOfSendsPerHourThatResetsAfterwards() {
        for (int i = 0; i < 3; i++) {
            request.execute(user.getId());
            clock.advance(Duration.ofMinutes(2));
        }

        assertEquals(MessageCodes.PHONE_CODE_TOO_MANY, codeOf(() -> request.execute(user.getId())));
        clock.advance(Duration.ofHours(1));
        request.execute(user.getId());

        assertEquals(4, sentCodes.size());
    }

    @Test
    void aFailedSendDoesNotConsumeTheCooldown() {
        final RequestPhoneVerificationUseCase failing = new RequestPhoneVerificationUseCase(
                new UsersStub(id -> Optional.ofNullable(store.get(id))), new PhoneVerificationRepository() {
                    public Optional<PhoneVerification> find(final UUID userId) { return Optional.ofNullable(verifications.get(userId)); }
                    public void save(final PhoneVerification verification) { verifications.put(verification.getUserId(), verification); }
                    public void delete(final UUID userId) { verifications.remove(userId); }
                }, audits::add, new UserDomain(), new PhoneVerificationDomain(), (phone, code) -> {
                    throw new DomainException(MessageCodes.PHONE_VERIFICATION_UNAVAILABLE);
                }, clock, Duration.ofMinutes(10), Duration.ofMinutes(1), 3);

        assertEquals(MessageCodes.PHONE_VERIFICATION_UNAVAILABLE, codeOf(() -> failing.execute(user.getId())));
        assertTrue(verifications.isEmpty());
    }

    @Test
    void aUserWithoutPhoneOrAlreadyVerifiedOrInactiveGetsNothing() {
        user.setPhone(null);
        assertEquals(MessageCodes.USER_PHONE_INVALID, codeOf(() -> request.execute(user.getId())));

        user.setPhone(PHONE);
        user.setPhoneVerified(true);
        request.execute(user.getId());
        assertTrue(sentCodes.isEmpty());

        user.setPhoneVerified(false);
        user.setStatus(UserStatus.INACTIVE);
        assertEquals(MessageCodes.USER_NOT_ACTIVE, codeOf(() -> request.execute(user.getId())));
        assertEquals(MessageCodes.USER_NOT_FOUND, codeOf(() -> request.execute(UuidHelper.generate())));
    }
}
