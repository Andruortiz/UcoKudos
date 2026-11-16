package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.PhoneVerificationDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.AuditAction;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.PhoneVerification;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.port.RequestPhoneVerificationPort;
import co.edu.uco.ucokudos.service.output.port.notification.SmsSender;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.PhoneVerificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Transactional
public class RequestPhoneVerificationUseCase implements RequestPhoneVerificationPort {

    private static final String RESOURCE_TYPE = "USER";
    private static final Duration WINDOW = Duration.ofHours(1);

    private final UserRepository users;
    private final PhoneVerificationRepository verifications;
    private final AuditRepository audits;
    private final UserDomain userDomain;
    private final PhoneVerificationDomain verificationDomain;
    private final SmsSender sms;
    private final Clock clock;
    private final Duration codeTtl;
    private final Duration cooldown;
    private final int maxSendsPerHour;

    public RequestPhoneVerificationUseCase(final UserRepository users,
                                           final PhoneVerificationRepository verifications,
                                           final AuditRepository audits, final UserDomain userDomain,
                                           final PhoneVerificationDomain verificationDomain, final SmsSender sms,
                                           final Clock clock, final Duration codeTtl, final Duration cooldown,
                                           final int maxSendsPerHour) {
        this.users = users;
        this.verifications = verifications;
        this.audits = audits;
        this.userDomain = userDomain;
        this.verificationDomain = verificationDomain;
        this.sms = sms;
        this.clock = clock;
        this.codeTtl = codeTtl;
        this.cooldown = cooldown;
        this.maxSendsPerHour = maxSendsPerHour;
    }

    @Override
    public void execute(final UUID userId) {
        final User user = users.findById(userId).orElseThrow(() -> new DomainException(MessageCodes.USER_NOT_FOUND));
        userDomain.validateActive(user);
        userDomain.validatePhone(user.getPhone());
        if (user.isPhoneVerified()) {
            return;
        }
        final Instant now = clock.instant();
        final PhoneVerification verification = verifications.find(userId).orElseGet(() -> newVerification(userId, now));
        if (!now.isBefore(verification.getWindowStart().plus(WINDOW))) {
            verification.setWindowStart(now);
            verification.setSentCount(0);
        }
        if (now.isBefore(verification.getLastSentAt().plus(cooldown))) {
            throw new DomainException(MessageCodes.PHONE_CODE_TOO_SOON);
        }
        if (verification.getSentCount() >= maxSendsPerHour) {
            throw new DomainException(MessageCodes.PHONE_CODE_TOO_MANY);
        }

        final String code = verificationDomain.newCode();
        sms.sendVerificationCode(user.getPhone(), code);

        verification.setCodeHash(verificationDomain.hash(userId, user.getPhone(), code));
        verification.setExpiresAt(now.plus(codeTtl));
        verification.setAttempts(0);
        verification.setSentCount(verification.getSentCount() + 1);
        verification.setLastSentAt(now);
        verifications.save(verification);
        audits.save(new AuditRecord(UuidHelper.generate(), now, userId, AuditAction.PHONE_VERIFICATION_SENT,
                RESOURCE_TYPE, userId, UuidHelper.generate().toString()));
    }

    private static PhoneVerification newVerification(final UUID userId, final Instant now) {
        final PhoneVerification verification = new PhoneVerification();
        verification.setUserId(userId);
        verification.setWindowStart(now);
        verification.setLastSentAt(Instant.EPOCH);
        return verification;
    }
}
