package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_key",
        uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key_author", columnNames = {"idempotency_key", "author_id"}))
public class IdempotencyJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String key;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "kudo_id", nullable = false)
    private UUID kudoId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(final UUID authorId) {
        this.authorId = authorId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(final String requestHash) {
        this.requestHash = requestHash;
    }

    public UUID getKudoId() {
        return kudoId;
    }

    public void setKudoId(final UUID kudoId) {
        this.kudoId = kudoId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }
}
