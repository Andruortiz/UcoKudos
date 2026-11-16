package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.AccessStatus;
import co.edu.uco.ucokudos.service.input.dto.request.RequestAccessRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RequestAccessResponse;
import co.edu.uco.ucokudos.service.input.port.RequestAccessPort;
import co.edu.uco.ucokudos.service.output.port.directory.DirectoryInviter;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;

import java.util.List;
import java.util.Locale;

public class RequestAccessUseCase implements RequestAccessPort {

    private final UserRepository users;
    private final UserDomain userDomain;
    private final List<String> allowedDomains;
    private final DirectoryInviter inviter;

    public RequestAccessUseCase(final UserRepository users, final UserDomain userDomain,
                                final List<String> allowedDomains, final DirectoryInviter inviter) {
        this.users = users;
        this.userDomain = userDomain;
        this.allowedDomains = allowedDomains;
        this.inviter = inviter;
    }

    @Override
    public RequestAccessResponse execute(final RequestAccessRequest request) {
        final String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase(Locale.ROOT);
        userDomain.validateEmail(email, allowedDomains);
        if (users.findByEmail(email).isPresent()) {
            return new RequestAccessResponse(AccessStatus.ALREADY_HAS_ACCESS);
        }
        return new RequestAccessResponse(inviter.invite(email));
    }
}
