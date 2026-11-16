package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.infrastructure.input.configuration.EntraUserConverter;
import co.edu.uco.ucokudos.service.input.dto.request.SubmitRegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RegistrationResponse;
import co.edu.uco.ucokudos.service.input.port.SubmitRegistrationPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    private final SubmitRegistrationPort submitRegistrationPort;

    public RegistrationController(final SubmitRegistrationPort submitRegistrationPort) {
        this.submitRegistrationPort = submitRegistrationPort;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> submit(@AuthenticationPrincipal final Jwt jwt) {
        if (jwt == null) {
            throw new DomainException(MessageCodes.AUTH_TOKEN_INVALID);
        }
        final SubmitRegistrationRequest request = new SubmitRegistrationRequest();
        request.setDirectoryId(jwt.getClaimAsString("oid"));
        request.setName(jwt.getClaimAsString("name"));
        request.setEmail(EntraUserConverter.emailOf(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(submitRegistrationPort.execute(request));
    }
}
