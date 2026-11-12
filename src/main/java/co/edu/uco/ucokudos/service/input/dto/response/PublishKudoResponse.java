package co.edu.uco.ucokudos.service.input.dto.response;

import co.edu.uco.ucokudos.domain.model.KudoStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PublishKudoResponse {

    private UUID id;

    private KudoStatus status;

    private Instant createdAt;

    private List<NotificationResponse> notifications;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public KudoStatus getStatus() {
        return status;
    }

    public void setStatus(final KudoStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<NotificationResponse> getNotifications() {
        return notifications;
    }

    public void setNotifications(final List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }
}
