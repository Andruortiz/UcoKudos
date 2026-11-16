package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PushNotificationSenderTest {

    private final PushNotificationSender sender = new PushNotificationSender();

    private static KudoNotice noticeFor(final UUID recipientId) {
        return new KudoNotice(UUID.randomUUID(), recipientId, "Luis", "luis@uco.net.co", null,
                "Ana", "Trabajo en equipo", "https://kudos.example/kudos/1");
    }

    @Test
    void itIsThePushChannel() {
        assertEquals(NotificationChannel.PUSH, sender.channel());
    }

    @Test
    void aRecipientWithoutAnOpenStreamIsSkipped() {
        assertEquals(DeliveryResult.SKIPPED, sender.send(noticeFor(UUID.randomUUID())));
    }

    @Test
    void aRecipientWithAnOpenStreamGetsItSent() {
        final UUID luis = UUID.randomUUID();
        sender.subscribe(luis);

        assertEquals(DeliveryResult.SENT, sender.send(noticeFor(luis)));
    }

    @Test
    void aStreamThatWasCompletedIsNoLongerUsed() {
        final UUID luis = UUID.randomUUID();
        sender.subscribe(luis).complete();

        assertEquals(DeliveryResult.SKIPPED, sender.send(noticeFor(luis)));
    }

    @Test
    void theHeartbeatKeepsWorkingWithNoStreams() {
        sender.heartbeat();
        sender.subscribe(UUID.randomUUID());
        sender.heartbeat();
    }
}
