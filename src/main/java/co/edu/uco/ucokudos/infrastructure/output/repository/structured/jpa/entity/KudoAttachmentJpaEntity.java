package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kudo_attachment",
        indexes = @Index(name = "idx_kudo_attachment_kudo", columnList = "kudo_id"))
public class KudoAttachmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "kudo_id", nullable = false)
    private UUID kudoId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "content", nullable = false, length = 10485760)
    private byte[] content;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getKudoId() {
        return kudoId;
    }

    public void setKudoId(final UUID kudoId) {
        this.kudoId = kudoId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(final byte[] content) {
        this.content = content;
    }
}
