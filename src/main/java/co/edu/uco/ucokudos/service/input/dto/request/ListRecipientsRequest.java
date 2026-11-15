package co.edu.uco.ucokudos.service.input.dto.request;

import java.util.UUID;

public class ListRecipientsRequest {

    private UUID requesterId;
    private String text;

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(final UUID requesterId) {
        this.requesterId = requesterId;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }
}
