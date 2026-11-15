package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.input.dto.request.UpdatePhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;
import co.edu.uco.ucokudos.service.input.port.GetProfilePort;
import co.edu.uco.ucokudos.service.input.port.UpdatePhonePort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
public class ProfileController {

    private final GetProfilePort getProfilePort;
    private final UpdatePhonePort updatePhonePort;

    public ProfileController(final GetProfilePort getProfilePort, final UpdatePhonePort updatePhonePort) {
        this.getProfilePort = getProfilePort;
        this.updatePhonePort = updatePhonePort;
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

    private UUID userId(final Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (final IllegalArgumentException e) {
            throw new DomainException(MessageCodes.USER_NOT_FOUND);
        }
    }
}
