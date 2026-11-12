package co.edu.uco.ucokudos.service.input.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class PublishKudoRequest {

    @NotNull
    private UUID recipientId;

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String content;

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
}
