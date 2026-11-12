package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Kudo {

    private UUID id;
    private UUID authorId;
    private UUID recipientId;
    private UUID categoryId;
    private String content;
    private Instant createdAt;
    private KudoStatus status = KudoStatus.PUBLISHED;
    private List<Attachment> attachments = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(final UUID authorId) { this.authorId = authorId; }
    public UUID getRecipientId() { return recipientId; }
    public void setRecipientId(final UUID recipientId) { this.recipientId = recipientId; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(final UUID categoryId) { this.categoryId = categoryId; }
    public String getContent() { return content; }
    public void setContent(final String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }
    public KudoStatus getStatus() { return status; }
    public void setStatus(final KudoStatus status) { this.status = status; }
    public List<Attachment> getAttachments() { return attachments; }
    public void setAttachments(final List<Attachment> attachments) { this.attachments = attachments; }
}
