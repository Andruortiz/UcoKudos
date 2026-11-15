package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationDomainTest {

    private static final Instant NOW = Instant.parse("2026-11-12T15:00:00Z");
    private static final Duration BASE = Duration.ofMinutes(1);

    private final NotificationDomain domain = new NotificationDomain();

    @Test
    void markSentIsFinalAndCountsTheAttempt() {
        final Notification notification = new Notification();
        domain.markSent(notification, NOW);
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertEquals(1, notification.getAttempts());
        assertEquals(NOW, notification.getSentAt());
        assertEquals(MessageCodes.NOTIFICATION_INVALID_TRANSITION,
                assertThrows(DomainException.class, () -> domain.markSent(notification, NOW)).getCode());
    }

    @Test
    void failureRetriesWithGrowingDelayUntilTheLastAttempt() {
        final Notification notification = new Notification();
        domain.markFailed(notification, "boom", NOW, 3, BASE);
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertEquals(NOW.plus(BASE), notification.getNextAttemptAt());
        domain.markFailed(notification, "boom", NOW, 3, BASE);
        assertEquals(NOW.plus(BASE.multipliedBy(2)), notification.getNextAttemptAt());
        domain.markFailed(notification, "boom", NOW, 3, BASE);
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertNull(notification.getNextAttemptAt());
        assertEquals(3, notification.getAttempts());
    }

    @Test
    void longErrorsAreTruncated() {
        final Notification notification = new Notification();
        domain.markFailed(notification, "x".repeat(5000), NOW, 3, BASE);
        assertEquals(NotificationDomain.MAX_ERROR_LENGTH, notification.getLastError().length());
    }

    @Test
    void skippedAndFailedAreFinal() {
        final Notification skipped = new Notification();
        domain.markSkipped(skipped);
        assertEquals(NotificationStatus.SKIPPED, skipped.getStatus());
        assertEquals(MessageCodes.NOTIFICATION_INVALID_TRANSITION,
                assertThrows(DomainException.class, () -> domain.markFailed(skipped, "e", NOW, 3, BASE)).getCode());
    }
}
