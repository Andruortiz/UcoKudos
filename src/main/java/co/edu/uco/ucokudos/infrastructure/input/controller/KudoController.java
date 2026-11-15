package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.KudoDirection;
import co.edu.uco.ucokudos.service.input.dto.request.ListKudosRequest;
import co.edu.uco.ucokudos.service.input.dto.request.PublishKudoRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ListKudosResponse;
import co.edu.uco.ucokudos.service.input.dto.response.PublishKudoResponse;
import co.edu.uco.ucokudos.service.input.port.ListKudosPort;
import co.edu.uco.ucokudos.service.input.port.PublishKudoPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kudos")
public class KudoController {

    private final PublishKudoPort publishKudoPort;
    private final ListKudosPort listKudosPort;

    public KudoController(final PublishKudoPort publishKudoPort, final ListKudosPort listKudosPort) {
        this.publishKudoPort = publishKudoPort;
        this.listKudosPort = listKudosPort;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublishKudoResponse> publish(
            @Valid @RequestPart("kudo") final PublishKudoRequest request,
            @RequestPart(value = "files", required = false) final List<MultipartFile> files,
            @RequestHeader(value = "Idempotency-Key", required = false) final String idempotencyKey,
            final Authentication authentication) {
        request.setAuthorId(authorId(authentication));
        request.setIdempotencyKey(idempotencyKey);
        request.setAttachments(files == null ? List.of() : files.stream().map(this::toAttachment).toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(publishKudoPort.execute(request));
    }

    private UUID authorId(final Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (final IllegalArgumentException e) {
            throw new DomainException(MessageCodes.USER_NOT_FOUND);
        }
    }

    private Attachment toAttachment(final MultipartFile file) {
        try {
            return new Attachment(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @GetMapping
    public ListKudosResponse listKudos(
            @RequestParam(name = "categoryId", required = false) final UUID categoryId,
            @RequestParam(name = "type", defaultValue = "ALL") final KudoDirection direction,
            final Authentication authentication) {
        final ListKudosRequest request = new ListKudosRequest();
        request.setUserId(authorId(authentication));
        request.setDirection(direction);
        request.setCategoryId(categoryId);
        return listKudosPort.execute(request);
    }
}
