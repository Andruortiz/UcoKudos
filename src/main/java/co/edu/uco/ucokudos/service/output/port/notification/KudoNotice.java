package co.edu.uco.ucokudos.service.output.port.notification;

import java.util.UUID;

public record KudoNotice(UUID kudoId, UUID recipientId, String recipientName, String recipientEmail,
                         String recipientPhone, String authorName, String categoryName, String link) {
}
