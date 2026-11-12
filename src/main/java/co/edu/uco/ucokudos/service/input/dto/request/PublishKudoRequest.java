package co.edu.uco.ucokudos.service.input.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import co.edu.uco.ucokudos.domain.model.Attachment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PublishKudoRequest {

    @NotNull
    private UUID recipientId;

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String content;

    @JsonIgnore
    private UUID authorId;

    @JsonIgnore
    private String idempotencyKey;

    @JsonIgnore
    private List<Attachment> attachments = new ArrayList<>();

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

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(final UUID authorId) {
        this.authorId = authorId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(final String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
