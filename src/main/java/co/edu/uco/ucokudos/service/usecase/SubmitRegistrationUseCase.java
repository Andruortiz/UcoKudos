package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.RegistrationStatus;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.service.input.dto.request.SubmitRegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RegistrationResponse;
import co.edu.uco.ucokudos.service.input.port.SubmitRegistrationPort;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

@Transactional
public class SubmitRegistrationUseCase implements SubmitRegistrationPort {

    private final UserRepository users;
    private final UserDomain userDomain;
    private final List<String> allowedDomains;
    private final Clock clock;

    public SubmitRegistrationUseCase(final UserRepository users, final UserDomain userDomain,
                                     final List<String> allowedDomains, final Clock clock) {
        this.users = users;
        this.userDomain = userDomain;
        this.allowedDomains = allowedDomains;
        this.clock = clock;
    }

    @Override
    public RegistrationResponse execute(final SubmitRegistrationRequest request) {
        final String name = request.getName() == null ? null : request.getName().trim();
        final String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase(Locale.ROOT);

        userDomain.validateName(name);
        userDomain.validateEmail(email, allowedDomains);
        if (users.findByEmail(email).isPresent() || users.findByDirectoryId(request.getDirectoryId()).isPresent()) {
            throw new DomainException(MessageCodes.REGISTRATION_ALREADY_EXISTS);
        }

        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId(request.getDirectoryId());
        user.setName(name);
        user.setEmail(email);
        user.setRole(userDomain.roleForNewAccount(email));
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);
        user.setCreatedAt(clock.instant());
        return new RegistrationResponse(users.save(user).getId(), RegistrationStatus.APPROVED);
    }
}
