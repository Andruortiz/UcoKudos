package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.input.dto.request.ConfirmPhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.request.UpdatePhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;
import co.edu.uco.ucokudos.service.input.port.ConfirmPhoneVerificationPort;
import co.edu.uco.ucokudos.service.input.port.GetProfilePort;
import co.edu.uco.ucokudos.service.input.port.RequestPhoneVerificationPort;
import co.edu.uco.ucokudos.service.input.port.UpdatePhonePort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
public class ProfileController {

    private final GetProfilePort getProfilePort;
    private final UpdatePhonePort updatePhonePort;
    private final RequestPhoneVerificationPort requestPhoneVerificationPort;
    private final ConfirmPhoneVerificationPort confirmPhoneVerificationPort;

    public ProfileController(final GetProfilePort getProfilePort, final UpdatePhonePort updatePhonePort,
                             final RequestPhoneVerificationPort requestPhoneVerificationPort,
                             final ConfirmPhoneVerificationPort confirmPhoneVerificationPort) {
        this.getProfilePort = getProfilePort;
        this.updatePhonePort = updatePhonePort;
        this.requestPhoneVerificationPort = requestPhoneVerificationPort;
        this.confirmPhoneVerificationPort = confirmPhoneVerificationPort;
    }

    @GetMapping
    public ProfileResponse profile(final Authentication authentication) {
        return getProfilePort.execute(userId(authentication));
    }

    @PutMapping("/phone")
    public ProfileResponse updatePhone(@RequestBody final UpdatePhoneRequest request,
                                       final Authentication authentication) {
        request.setUserId(userId(authentication));
        return updatePhonePort.execute(request);
    }

    @PostMapping("/phone/verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestVerification(final Authentication authentication) {
        requestPhoneVerificationPort.execute(userId(authentication));
    }

    @PostMapping("/phone/verification/confirm")
    public ProfileResponse confirmVerification(@RequestBody final ConfirmPhoneRequest request,
                                               final Authentication authentication) {
        request.setUserId(userId(authentication));
        return confirmPhoneVerificationPort.execute(request);
    }

    private UUID userId(final Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (final IllegalArgumentException e) {
            throw new DomainException(MessageCodes.USER_NOT_FOUND);
        }
    }
}
