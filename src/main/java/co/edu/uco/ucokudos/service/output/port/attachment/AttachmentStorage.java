package co.edu.uco.ucokudos.service.output.port.attachment;

import java.util.UUID;

public interface AttachmentStorage {

    static String keyOf(final UUID kudoId, final UUID attachmentId) {
        return kudoId + "/" + attachmentId;
    }

    void put(String key, String contentType, byte[] content);

    byte[] get(String key);
}
