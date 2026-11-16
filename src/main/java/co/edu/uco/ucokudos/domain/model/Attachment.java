package co.edu.uco.ucokudos.domain.model;

import java.util.Arrays;
import java.util.Objects;

public record Attachment(String name, String contentType, byte[] content) {

    public long size() {
        return content.length;
    }

    // A record compares arrays by reference, so two attachments with the same bytes would not be equal.
    @Override
    public boolean equals(final Object other) {
        return other instanceof Attachment that
                && Objects.equals(name, that.name)
                && Objects.equals(contentType, that.contentType)
                && Arrays.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(name, contentType) + Arrays.hashCode(content);
    }

    // Never print the bytes: an attachment can be several megabytes of an untrusted file.
    @Override
    public String toString() {
        return "Attachment[name=" + name + ", contentType=" + contentType + ", size=" + size() + "]";
    }
}
