package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.AuditAction;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.request.UpdatePhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;
import co.edu.uco.ucokudos.service.input.port.UpdatePhonePort;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Transactional
public class UpdatePhoneUseCase implements UpdatePhonePort {

    private static final String RESOURCE_TYPE = "USER";

    private final UserRepository users;
    private final AuditRepository audits;
    private final UserDomain userDomain;
    private final Clock clock;

    public UpdatePhoneUseCase(final UserRepository users, final AuditRepository audits,
                              final UserDomain userDomain, final Clock clock) {
        this.users = users;
        this.audits = audits;
        this.userDomain = userDomain;
        this.clock = clock;
    }

    @Override
    public ProfileResponse execute(final UpdatePhoneRequest request) {
        final User user = users.findById(request.getUserId())
                .orElseThrow(() -> new DomainException(MessageCodes.USER_NOT_FOUND));
        userDomain.validateActive(user);
        final String phone = request.getPhone() == null ? null : request.getPhone().trim();
        userDomain.validatePhone(phone);

        if (!phone.equals(user.getPhone())) {
            user.setPhone(phone);
            user.setPhoneVerified(false);
            users.save(user);
            audits.save(new AuditRecord(UuidHelper.generate(), clock.instant(), user.getId(),
                    AuditAction.PHONE_UPDATED, RESOURCE_TYPE, user.getId(), UuidHelper.generate().toString()));
        }
        return ProfileMapper.toResponse(user);
    }
}
