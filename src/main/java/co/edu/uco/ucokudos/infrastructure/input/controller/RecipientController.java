package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.input.dto.request.ListRecipientsRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ListRecipientsResponse;
import co.edu.uco.ucokudos.service.input.port.ListRecipientsPort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recipients")
public class RecipientController {

    private final ListRecipientsPort listRecipientsPort;

    public RecipientController(final ListRecipientsPort listRecipientsPort) {
        this.listRecipientsPort = listRecipientsPort;
    }

    @GetMapping
    public ListRecipientsResponse list(
            @RequestParam(name = "q", required = false) final String text,
            final Authentication authentication) {
        final ListRecipientsRequest request = new ListRecipientsRequest();
        request.setRequesterId(requesterId(authentication));
        request.setText(text);
        return listRecipientsPort.execute(request);
    }

    private UUID requesterId(final Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (final IllegalArgumentException e) {
            throw new DomainException(MessageCodes.USER_NOT_FOUND);
        }
    }
}
