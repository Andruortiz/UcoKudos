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
import co.edu.uco.ucokudos.service.input.dto.request.ConfirmPhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;
import co.edu.uco.ucokudos.service.input.port.ConfirmPhoneVerificationPort;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.PhoneVerificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Transactional(noRollbackFor = DomainException.class)
public class ConfirmPhoneVerificationUseCase implements ConfirmPhoneVerificationPort {

    private static final String RESOURCE_TYPE = "USER";

    private final UserRepository users;
    private final PhoneVerificationRepository verifications;
    private final AuditRepository audits;
    private final UserDomain userDomain;
    private final PhoneVerificationDomain verificationDomain;
    private final Clock clock;
    private final int maxAttempts;

    public ConfirmPhoneVerificationUseCase(final UserRepository users,
                                           final PhoneVerificationRepository verifications,
                                           final AuditRepository audits, final UserDomain userDomain,
                                           final PhoneVerificationDomain verificationDomain, final Clock clock,
                                           final int maxAttempts) {
        this.users = users;
        this.verifications = verifications;
        this.audits = audits;
        this.userDomain = userDomain;
        this.verificationDomain = verificationDomain;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ProfileResponse execute(final ConfirmPhoneRequest request) {
        final User user = users.findById(request.getUserId())
                .orElseThrow(() -> new DomainException(MessageCodes.USER_NOT_FOUND));
        userDomain.validateActive(user);
        if (user.isPhoneVerified()) {
            return ProfileMapper.toResponse(user);
        }
        final String code = request.getCode() == null ? "" : request.getCode().trim();
        final PhoneVerification verification = verifications.find(user.getId())
                .orElseThrow(() -> new DomainException(MessageCodes.PHONE_CODE_INVALID));
        final Instant now = clock.instant();
        if (now.isAfter(verification.getExpiresAt())) {
            throw new DomainException(MessageCodes.PHONE_CODE_EXPIRED);
        }
        if (verification.getAttempts() >= maxAttempts) {
            throw new DomainException(MessageCodes.PHONE_CODE_TOO_MANY);
        }
        final String hash = verificationDomain.hash(user.getId(), user.getPhone(), code);
        if (!verificationDomain.matches(verification.getCodeHash(), hash)) {
            verification.setAttempts(verification.getAttempts() + 1);
            verifications.save(verification);
            throw new DomainException(MessageCodes.PHONE_CODE_INVALID);
        }

        user.setPhoneVerified(true);
        users.save(user);
        verifications.delete(user.getId());
        audits.save(new AuditRecord(UuidHelper.generate(), now, user.getId(), AuditAction.PHONE_VERIFIED,
                RESOURCE_TYPE, user.getId(), UuidHelper.generate().toString()));
        return ProfileMapper.toResponse(user);
    }
}
