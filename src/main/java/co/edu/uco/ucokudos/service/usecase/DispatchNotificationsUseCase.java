package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.NotificationDomain;
import co.edu.uco.ucokudos.domain.model.AuditAction;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.domain.model.NotificationStatus;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.response.DispatchNotificationsResponse;
import co.edu.uco.ucokudos.service.input.port.DispatchNotificationsPort;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.NotificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DispatchNotificationsUseCase implements DispatchNotificationsPort {

    private static final String RESOURCE_TYPE = "NOTIFICATION";

    private final NotificationRepository notifications;
    private final KudoRepository kudos;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final AuditRepository audits;
    private final NotificationDomain notificationDomain;
    private final Map<NotificationChannel, NotificationSender> senders = new EnumMap<>(NotificationChannel.class);
    private final Clock clock;
    private final int batchSize;
    private final int maxAttempts;
    private final Duration baseDelay;
    private final String linkBaseUrl;

    public DispatchNotificationsUseCase(final NotificationRepository notifications, final KudoRepository kudos,
                                        final UserRepository users, final CategoryRepository categories,
                                        final AuditRepository audits, final NotificationDomain notificationDomain,
                                        final List<NotificationSender> senderList, final Clock clock,
                                        final int batchSize, final int maxAttempts, final Duration baseDelay,
                                        final String linkBaseUrl) {
        this.notifications = notifications;
        this.kudos = kudos;
        this.users = users;
        this.categories = categories;
        this.audits = audits;
        this.notificationDomain = notificationDomain;
        senderList.forEach(sender -> senders.put(sender.channel(), sender));
        this.clock = clock;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.baseDelay = baseDelay;
        this.linkBaseUrl = linkBaseUrl;
    }

    @Override
    public DispatchNotificationsResponse execute() {
        final DispatchNotificationsResponse response = new DispatchNotificationsResponse();
        if (senders.isEmpty()) {
            return response;
        }
        final Instant now = clock.instant();
        for (final Notification notification : notifications.findDue(senders.keySet(), now, batchSize)) {
            process(notification, now, response);
        }
        return response;
    }

    private void process(final Notification notification, final Instant now, final DispatchNotificationsResponse response) {
        DeliveryResult result = null;
        String error = null;
        try {
            result = senders.get(notification.getChannel()).send(buildNotice(notification));
        } catch (final RuntimeException e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
        }

        AuditAction action = null;
        if (error != null) {
            notificationDomain.markFailed(notification, error, now, maxAttempts, baseDelay);
            if (notification.getStatus() == NotificationStatus.FAILED) {
                action = AuditAction.NOTIFICATION_FAILED;
                response.setFailed(response.getFailed() + 1);
            } else {
                response.setRetrying(response.getRetrying() + 1);
            }
        } else if (result == DeliveryResult.SKIPPED) {
            notificationDomain.markSkipped(notification);
            response.setSkipped(response.getSkipped() + 1);
        } else {
            notificationDomain.markSent(notification, now);
            action = AuditAction.NOTIFICATION_SENT;
            response.setSent(response.getSent() + 1);
        }
        notifications.save(notification);
        if (action != null) {
            audits.save(new AuditRecord(UuidHelper.generate(), now, null, action, RESOURCE_TYPE,
                    notification.getId(), notification.getKudoId().toString()));
        }
    }

    private KudoNotice buildNotice(final Notification notification) {
        final Kudo kudo = kudos.findById(notification.getKudoId()).orElseThrow(IllegalStateException::new);
        final User recipient = users.findById(kudo.getRecipientId()).orElseThrow(IllegalStateException::new);
        final User author = users.findById(kudo.getAuthorId()).orElseThrow(IllegalStateException::new);
        final String category = categories.findById(kudo.getCategoryId()).orElseThrow(IllegalStateException::new).getName();
        return new KudoNotice(kudo.getId(), recipient.getId(), recipient.getName(), recipient.getEmail(),
                recipient.getPhone(), author.getName(), category, linkBaseUrl + kudo.getId());
    }
}
