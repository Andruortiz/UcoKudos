package co.edu.uco.ucokudos.infrastructure.output.attachment;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnavailableAttachmentProcessor implements AttachmentProcessor {

    @Override
    public List<Attachment> process(final List<Attachment> attachments) {
        if (!attachments.isEmpty()) {
            throw new DomainException(MessageCodes.KUDO_SCANNER_UNAVAILABLE);
        }
        return attachments;
    }
}
