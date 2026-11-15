package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.RegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.request.SubmitRegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RegistrationResponse;
import co.edu.uco.ucokudos.service.input.port.SubmitRegistrationPort;
import co.edu.uco.ucokudos.service.output.port.repository.RegistrationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

/**
 * shortcut: it only records the request as pending. There is no approval, no account creation in Entra and no
 * confirmation email yet (ADR-0003, ADR-0004); a person above the applicant has to approve it in a later step.
 */
@Transactional
public class SubmitRegistrationUseCase implements SubmitRegistrationPort {

    private final RegistrationRepository registrations;
    private final UserRepository users;
    private final UserDomain userDomain;
    private final List<String> allowedDomains;
    private final Clock clock;

    public SubmitRegistrationUseCase(final RegistrationRepository registrations, final UserRepository users,
                                     final UserDomain userDomain, final List<String> allowedDomains,
                                     final Clock clock) {
        this.registrations = registrations;
        this.users = users;
        this.userDomain = userDomain;
        this.allowedDomains = allowedDomains;
        this.clock = clock;
    }

    @Override
    public RegistrationResponse execute(final SubmitRegistrationRequest request) {
        final String name = request.getName() == null ? null : request.getName().trim();
        final String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase(Locale.ROOT);
        final String phone = request.getPhone() == null ? null : request.getPhone().trim();

        userDomain.validateName(name);
        userDomain.validateEmail(email, allowedDomains);
        userDomain.validatePhone(phone);
        userDomain.validateSelfRegistrationRole(request.getRole());
        if (users.findByEmail(email).isPresent() || registrations.existsPendingByEmail(email)) {
            throw new DomainException(MessageCodes.REGISTRATION_ALREADY_EXISTS);
        }

        final RegistrationRequest registration = new RegistrationRequest();
        registration.setId(UuidHelper.generate());
        registration.setName(name);
        registration.setEmail(email);
        registration.setPhone(phone);
        registration.setRole(request.getRole());
        registration.setCreatedAt(clock.instant());
        final RegistrationRequest saved = registrations.save(registration);
        return new RegistrationResponse(saved.getId(), saved.getStatus());
    }
}
