package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.NotificationDomain;
import co.edu.uco.ucokudos.domain.model.AuditAction;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.domain.model.NotificationStatus;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.response.DispatchNotificationsResponse;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.NotificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatchNotificationsUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-11-12T15:00:00Z");

    static class Memory implements NotificationRepository, KudoRepository, AuditRepository {
        final List<Notification> notifications = new ArrayList<>();
        final List<AuditRecord> audits = new ArrayList<>();
        Kudo kudo;

        public void saveAll(final List<Notification> list) { notifications.addAll(list); }
        public void save(final Notification notification) { }
        public List<Notification> findByKudoId(final UUID kudoId) { return notifications; }
        public List<Notification> findDue(final Collection<NotificationChannel> channels, final Instant now, final int limit) {
            return notifications.stream()
                    .filter(n -> n.getStatus() == NotificationStatus.PENDING && channels.contains(n.getChannel())
                            && n.getNextAttemptAt() != null && !n.getNextAttemptAt().isAfter(now))
                    .limit(limit).toList();
        }
        public Kudo save(final Kudo value) { return value; }
        public Optional<Kudo> findById(final UUID id) { return Optional.ofNullable(kudo); }
        public long countByAuthorBetween(final UUID authorId, final Instant from, final Instant to) { return 0; }
        public void save(final AuditRecord record) { audits.add(record); }
    }

    static class FakeSender implements NotificationSender {
        final NotificationChannel channel;
        final Function<KudoNotice, DeliveryResult> behavior;
        final List<KudoNotice> sent = new ArrayList<>();

        FakeSender(final NotificationChannel channel, final Function<KudoNotice, DeliveryResult> behavior) {
            this.channel = channel;
            this.behavior = behavior;
        }

        public NotificationChannel channel() { return channel; }

        public DeliveryResult send(final KudoNotice notice) {
            sent.add(notice);
            return behavior.apply(notice);
        }
    }

    private final Memory memory = new Memory();
    private User author;
    private User recipient;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(UuidHelper.generate());
        author.setName("Ana");
        recipient = new User();
        recipient.setId(UuidHelper.generate());
        recipient.setName("Luis");
        recipient.setEmail("luis@uco.net.co");
        memory.kudo = new Kudo();
        memory.kudo.setId(UuidHelper.generate());
        memory.kudo.setAuthorId(author.getId());
        memory.kudo.setRecipientId(recipient.getId());
        memory.kudo.setCategoryId(UuidHelper.generate());
    }

    private Notification pending(final NotificationChannel channel, final Instant next) {
        final Notification notification = new Notification();
        notification.setId(UuidHelper.generate());
        notification.setKudoId(memory.kudo.getId());
        notification.setChannel(channel);
        notification.setCreatedAt(NOW.minusSeconds(60));
        notification.setNextAttemptAt(next);
        memory.notifications.add(notification);
        return notification;
    }

    private DispatchNotificationsResponse run(final int maxAttempts, final NotificationSender... senders) {
        final UserRepository users = id -> Optional.of(id.equals(author.getId()) ? author : recipient);
        final Category category = new Category();
        category.setName("Empatía");
        final CategoryRepository categories = id -> Optional.of(category);
        return new DispatchNotificationsUseCase(memory, memory, users, categories, memory, new NotificationDomain(),
                List.of(senders), Clock.fixed(NOW, ZoneOffset.UTC), 50, maxAttempts, Duration.ofMinutes(1),
                "http://front/kudos/").execute();
    }

    @Test
    void sendsTheDueNotificationAndAuditsIt() {
        final Notification notification = pending(NotificationChannel.EMAIL, NOW);
        final FakeSender email = new FakeSender(NotificationChannel.EMAIL, notice -> DeliveryResult.SENT);

        final DispatchNotificationsResponse response = run(5, email);

        assertEquals(1, response.getSent());
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertEquals(1, memory.audits.size());
        assertEquals(AuditAction.NOTIFICATION_SENT, memory.audits.get(0).action());
        assertEquals("http://front/kudos/" + memory.kudo.getId(), email.sent.get(0).link());
        assertEquals("Ana", email.sent.get(0).authorName());
        assertEquals("Empatía", email.sent.get(0).categoryName());
    }

    @Test
    void failureKeepsItPendingWithANewDateAndDoesNotAudit() {
        final Notification notification = pending(NotificationChannel.WHATSAPP, NOW);
        final FakeSender whatsapp = new FakeSender(NotificationChannel.WHATSAPP, notice -> {
            throw new IllegalStateException("provider down");
        });

        final DispatchNotificationsResponse response = run(5, whatsapp);

        assertEquals(1, response.getRetrying());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertEquals(NOW.plus(Duration.ofMinutes(1)), notification.getNextAttemptAt());
        assertTrue(notification.getLastError().contains("provider down"));
        assertEquals(0, memory.audits.size());
    }

    @Test
    void lastFailedAttemptEndsInFailedAndIsAudited() {
        final Notification notification = pending(NotificationChannel.WHATSAPP, NOW);
        notification.setAttempts(4);
        final FakeSender whatsapp = new FakeSender(NotificationChannel.WHATSAPP, notice -> {
            throw new IllegalStateException("provider down");
        });

        final DispatchNotificationsResponse response = run(5, whatsapp);

        assertEquals(1, response.getFailed());
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertEquals(AuditAction.NOTIFICATION_FAILED, memory.audits.get(0).action());
    }

    @Test
    void pushWithoutConnectionIsSkipped() {
        final Notification notification = pending(NotificationChannel.PUSH, NOW);
        final FakeSender push = new FakeSender(NotificationChannel.PUSH, notice -> DeliveryResult.SKIPPED);

        final DispatchNotificationsResponse response = run(5, push);

        assertEquals(1, response.getSkipped());
        assertEquals(NotificationStatus.SKIPPED, notification.getStatus());
    }

    @Test
    void channelsWithoutSenderAndFutureNotificationsAreLeftUntouched() {
        final Notification noSender = pending(NotificationChannel.WHATSAPP, NOW);
        final Notification notDue = pending(NotificationChannel.EMAIL, NOW.plusSeconds(600));
        final FakeSender email = new FakeSender(NotificationChannel.EMAIL, notice -> DeliveryResult.SENT);

        run(5, email);

        assertEquals(0, email.sent.size());
        assertEquals(NotificationStatus.PENDING, noSender.getStatus());
        assertEquals(0, noSender.getAttempts());
        assertEquals(NotificationStatus.PENDING, notDue.getStatus());
    }

    @Test
    void withoutAnySenderNothingHappens() {
        final Notification notification = pending(NotificationChannel.EMAIL, NOW);
        final DispatchNotificationsResponse response = run(5);
        assertEquals(0, response.getSent() + response.getRetrying() + response.getFailed() + response.getSkipped());
        assertEquals(0, notification.getAttempts());
    }
}
