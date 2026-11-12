package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.domain.model.Attachment;

import java.util.List;
import java.util.UUID;

public record PublishKudoCommand(UUID authorId, UUID recipientId, UUID categoryId, String content,
                                 List<Attachment> attachments, String idempotencyKey) {
}
