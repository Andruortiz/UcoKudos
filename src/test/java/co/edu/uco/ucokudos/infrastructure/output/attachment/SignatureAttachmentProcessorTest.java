package co.edu.uco.ucokudos.infrastructure.output.attachment;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Attachment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignatureAttachmentProcessorTest {

    private final SignatureAttachmentProcessor processor = new SignatureAttachmentProcessor();

    private static Attachment file(final String type, final byte[] content) {
        return new Attachment("f", type, content);
    }

    @Test
    void filesWhoseBytesMatchTheirTypeArePassedThrough() {
        final List<Attachment> files = List.of(
                file("application/pdf", "%PDF-1.7".getBytes()),
                file("image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 1}),
                file("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0}),
                file("image/gif", "GIF89a".getBytes()),
                file("image/webp", "RIFF1234WEBPVP8 ".getBytes()));

        assertEquals(files, processor.process(files));
    }

    @Test
    void aFileThatLiesAboutItsTypeIsRejected() {
        for (final Attachment liar : List.of(
                file("application/pdf", "MZ-executable".getBytes()),
                file("image/png", new byte[]{1, 2, 3}),
                file("image/webp", "RIFF1234WAVEfmt ".getBytes()),
                file("image/svg+xml", "<svg onload=alert(1)>".getBytes()),
                file("image/png", new byte[0]))) {
            assertEquals(MessageCodes.KUDO_ATTACHMENT_TYPE,
                    assertThrows(DomainException.class, () -> processor.process(List.of(liar))).getCode());
        }
    }
}
