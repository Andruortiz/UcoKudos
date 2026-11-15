package co.edu.uco.ucokudos.domain;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationStatus;

import java.time.Duration;
import java.time.Instant;

public class NotificationDomain {

    public static final int MAX_ERROR_LENGTH = 1000;
    private static final int MAX_BACKOFF_SHIFT = 20;

    public void markSent(final Notification notification, final Instant now) {
        validatePending(notification);
        notification.setAttempts(notification.getAttempts() + 1);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(now);
        notification.setNextAttemptAt(null);
        notification.setLastError(null);
    }

    public void markSkipped(final Notification notification) {
        validatePending(notification);
        notification.setStatus(NotificationStatus.SKIPPED);
        notification.setNextAttemptAt(null);
    }

    public void markFailed(final Notification notification, final String error, final Instant now,
                           final int maxAttempts, final Duration baseDelay) {
        validatePending(notification);
        final int attempts = notification.getAttempts() + 1;
        notification.setAttempts(attempts);
        notification.setLastError(error == null || error.length() <= MAX_ERROR_LENGTH
                ? error : error.substring(0, MAX_ERROR_LENGTH));
        if (attempts >= maxAttempts) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setNextAttemptAt(null);
        } else {
            notification.setNextAttemptAt(now.plus(baseDelay.multipliedBy(1L << Math.min(attempts - 1, MAX_BACKOFF_SHIFT))));
        }
    }

    private void validatePending(final Notification notification) {
        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new DomainException(MessageCodes.NOTIFICATION_INVALID_TRANSITION);
        }
    }
}
