package co.edu.uco.ucokudos.service.output.port.attachment;

import co.edu.uco.ucokudos.domain.model.Attachment;

import java.util.List;

public interface AttachmentProcessor {

    List<Attachment> process(List<Attachment> attachments);
}
