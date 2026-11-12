package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity;

import co.edu.uco.ucokudos.domain.model.KudoStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kudo",
        indexes = {@Index(name = "idx_kudo_author_created", columnList = "author_id, created_at"),
                @Index(name = "idx_kudo_recipient_created", columnList = "recipient_id, created_at")})
public class KudoJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private KudoStatus status;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(final UUID authorId) {
        this.authorId = authorId;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(final UUID recipientId) {
        this.recipientId = recipientId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public KudoStatus getStatus() {
        return status;
    }

    public void setStatus(final KudoStatus status) {
        this.status = status;
    }
}
