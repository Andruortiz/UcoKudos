package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.service.input.port.GetAttachmentPort;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentStorage;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;

import java.util.UUID;

public class GetAttachmentUseCase implements GetAttachmentPort {

    private final KudoRepository kudos;
    private final AttachmentStorage storage;

    public GetAttachmentUseCase(final KudoRepository kudos, final AttachmentStorage storage) {
        this.kudos = kudos;
        this.storage = storage;
    }

    @Override
    public Attachment execute(final UUID requesterId, final UUID kudoId, final UUID attachmentId) {
        final Kudo kudo = kudos.findById(kudoId).orElseThrow(() -> new DomainException(MessageCodes.KUDO_NOT_FOUND));
        if (!requesterId.equals(kudo.getAuthorId()) && !requesterId.equals(kudo.getRecipientId())) {
            throw new DomainException(MessageCodes.KUDO_NOT_FOUND);
        }
        final Attachment found = kudo.getAttachments().stream()
                .filter(a -> attachmentId.equals(a.id()))
                .findFirst()
                .orElseThrow(() -> new DomainException(MessageCodes.KUDO_NOT_FOUND));
        final byte[] content = storage.get(AttachmentStorage.keyOf(kudoId, attachmentId));
        return new Attachment(found.id(), found.name(), found.contentType(), found.size(), content);
    }
}
