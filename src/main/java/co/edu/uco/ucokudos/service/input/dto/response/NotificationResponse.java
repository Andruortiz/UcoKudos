package co.edu.uco.ucokudos.service.input.dto.response;

import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.domain.model.NotificationStatus;

public class NotificationResponse {

    private NotificationChannel channel;

    private NotificationStatus status;

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(final NotificationChannel channel) {
        this.channel = channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(final NotificationStatus status) {
        this.status = status;
    }
}
