package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.input.dto.request.RequestAccessRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RequestAccessResponse;
import co.edu.uco.ucokudos.service.input.port.RequestAccessPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access-requests")
public class AccessRequestController {

    private final RequestAccessPort requestAccessPort;
    private final AccessRequestThrottle throttle;

    public AccessRequestController(final RequestAccessPort requestAccessPort, final AccessRequestThrottle throttle) {
        this.requestAccessPort = requestAccessPort;
        this.throttle = throttle;
    }

    @PostMapping
    public RequestAccessResponse request(@RequestBody final RequestAccessRequest request,
                                         final HttpServletRequest http) {
        if (!throttle.allow(clientOf(http))) {
            throw new DomainException(MessageCodes.ACCESS_TOO_MANY);
        }
        return requestAccessPort.execute(request);
    }

    private static String clientOf(final HttpServletRequest http) {
        final String forwarded = http.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? http.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
