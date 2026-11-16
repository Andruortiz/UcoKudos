package co.edu.uco.ucokudos.infrastructure.output.attachment;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "kudos.attachments.storage", havingValue = "memory")
public class InMemoryAttachmentStorage implements AttachmentStorage {

    private final Map<String, byte[]> blobs = new ConcurrentHashMap<>();

    @Override
    public void put(final String key, final String contentType, final byte[] content) {
        blobs.put(key, content.clone());
    }

    @Override
    public byte[] get(final String key) {
        final byte[] content = blobs.get(key);
        if (content == null) {
            throw new DomainException(MessageCodes.ATTACHMENT_STORAGE_UNAVAILABLE);
        }
        return content.clone();
    }
}
