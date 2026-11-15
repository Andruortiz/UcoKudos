package co.edu.uco.ucokudos.service.input.dto.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KudoDetailResponse {

    private UUID id;
    private UUID authorId;
    private String authorName;
    private UUID recipientId;
    private String recipientName;
    private UUID categoryId;
    private String categoryName;
    private String content;
    private Instant createdAt;
    private List<AttachmentResponse> attachments = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(final UUID authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(final String authorName) { this.authorName = authorName; }
    public UUID getRecipientId() { return recipientId; }
    public void setRecipientId(final UUID recipientId) { this.recipientId = recipientId; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(final String recipientName) { this.recipientName = recipientName; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(final UUID categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(final String categoryName) { this.categoryName = categoryName; }
    public String getContent() { return content; }
    public void setContent(final String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }
    public List<AttachmentResponse> getAttachments() { return attachments; }
    public void setAttachments(final List<AttachmentResponse> attachments) { this.attachments = attachments; }
}
