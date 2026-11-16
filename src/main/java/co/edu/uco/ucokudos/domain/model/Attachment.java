package co.edu.uco.ucokudos.domain.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record Attachment(UUID id, String name, String contentType, long size, byte[] content) {

    public Attachment(final String name, final String contentType, final byte[] content) {
        this(null, name, contentType, content.length, content);
    }

    // A record compares arrays by reference, so two attachments with the same bytes would not be equal.
    @Override
    public boolean equals(final Object other) {
        return other instanceof Attachment that
                && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(contentType, that.contentType)
                && size == that.size
                && Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(id, name, contentType, size) + Arrays.hashCode(content);
    }

    // Never print the bytes: an attachment can be several megabytes of an untrusted file.
    @Override
    public String toString() {
        return "Attachment[name=" + name + ", contentType=" + contentType + ", size=" + size + "]";
    }
}
