package co.edu.uco.ucokudos.infrastructure.output.attachment;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentProcessor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Rejects a file whose bytes do not match the type it declares; it is not an antivirus. */
@Component
public class SignatureAttachmentProcessor implements AttachmentProcessor {

    private static final Map<String, byte[]> SIGNATURES = Map.of(
            "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G'},
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/gif", "GIF8".getBytes(StandardCharsets.US_ASCII),
            "application/pdf", "%PDF".getBytes(StandardCharsets.US_ASCII));

    @Override
    public List<Attachment> process(final List<Attachment> attachments) {
        for (final Attachment attachment : attachments) {
            if (!matches(attachment)) {
                throw new DomainException(MessageCodes.KUDO_ATTACHMENT_TYPE);
            }
        }
        return attachments;
    }

    private static boolean matches(final Attachment attachment) {
        final byte[] content = attachment.content();
        if ("image/webp".equals(attachment.contentType())) {
            return startsWith(content, "RIFF".getBytes(StandardCharsets.US_ASCII), 0)
                    && startsWith(content, "WEBP".getBytes(StandardCharsets.US_ASCII), 8);
        }
        final byte[] signature = SIGNATURES.get(attachment.contentType());
        return signature != null && startsWith(content, signature, 0);
    }

    private static boolean startsWith(final byte[] content, final byte[] prefix, final int offset) {
        if (content.length < offset + prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (content[offset + i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
