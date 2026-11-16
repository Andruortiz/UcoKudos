package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.infrastructure.output.notification.PushNotificationSender;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class PushController {

    private final PushNotificationSender push;

    public PushController(final PushNotificationSender push) {
        this.push = push;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(final Authentication authentication) {
        try {
            return ResponseEntity.ok().header("X-Accel-Buffering", "no")
                    .body(push.subscribe(UUID.fromString(authentication.getName())));
        } catch (final IllegalArgumentException e) {
            throw new DomainException(MessageCodes.USER_NOT_FOUND);
        }
    }
}
