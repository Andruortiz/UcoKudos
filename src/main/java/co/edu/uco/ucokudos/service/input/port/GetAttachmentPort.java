package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.domain.model.Attachment;

import java.util.UUID;

public interface GetAttachmentPort {

    Attachment execute(UUID requesterId, UUID kudoId, UUID attachmentId);
}
