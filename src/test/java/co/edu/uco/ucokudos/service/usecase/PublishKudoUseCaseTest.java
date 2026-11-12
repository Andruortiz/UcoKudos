package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.KudoDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.IdempotencyRecord;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.service.input.dto.request.PublishKudoRequest;
import co.edu.uco.ucokudos.service.input.dto.response.NotificationResponse;
import co.edu.uco.ucokudos.service.input.dto.response.PublishKudoResponse;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.IdempotencyRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublishKudoUseCaseTest {

    static class Memory implements KudoRepository, NotificationRepository, AuditRepository, IdempotencyRepository {
        final Map<UUID, Kudo> kudos = new HashMap<>();
        final List<Notification> notifications = new ArrayList<>();
        final List<AuditRecord> audits = new ArrayList<>();
        final Map<String, IdempotencyRecord> keys = new HashMap<>();

        public Kudo save(final Kudo kudo) { kudos.put(kudo.getId(), kudo); return kudo; }
        public Optional<Kudo> findById(final UUID id) { return Optional.ofNullable(kudos.get(id)); }
        public long countByAuthorBetween(final UUID authorId, final Instant from, final Instant to) {
            return kudos.values().stream().filter(k -> k.getAuthorId().equals(authorId)
                    && !k.getCreatedAt().isBefore(from) && k.getCreatedAt().isBefore(to)).count();
        }
        public void saveAll(final List<Notification> list) { notifications.addAll(list); }
        public void save(final Notification notification) { notifications.add(notification); }
        public List<Notification> findDue(final java.util.Collection<NotificationChannel> channels,
                                          final Instant now, final int limit) { return List.of(); }
        public List<Notification> findByKudoId(final UUID kudoId) {
            return notifications.stream().filter(n -> n.getKudoId().equals(kudoId)).toList();
        }
        public void save(final AuditRecord record) { audits.add(record); }
        public Optional<IdempotencyRecord> find(final String key, final UUID authorId, final Instant since) {
            return Optional.ofNullable(keys.get(key + authorId)).filter(r -> !r.createdAt().isBefore(since));
        }
        public void save(final IdempotencyRecord record) { keys.put(record.key() + record.authorId(), record); }
    }

    private final Map<UUID, User> users = new HashMap<>();
    private final Category category = new Category();
    private final Memory memory = new Memory();
    private User author;
    private User recipient;
    private PublishKudoUseCase useCase;
    private boolean scannerFails;

    private User user(final Role role) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        users.put(user.getId(), user);
        return user;
    }

    @BeforeEach
    void setUp() {
        author = user(Role.STUDENT);
        recipient = user(Role.TEACHER);
        category.setId(UuidHelper.generate());
        final UserRepository userRepository = id -> Optional.ofNullable(users.get(id));
        final CategoryRepository categoryRepository = id -> Optional.of(category);
        useCase = new PublishKudoUseCase(userRepository, categoryRepository, memory, memory, memory, memory,
                attachments -> {
                    if (scannerFails) {
                        throw new DomainException(MessageCodes.KUDO_ATTACHMENT_INFECTED);
                    }
                    return attachments;
                }, new UserDomain(), new KudoDomain(),
                Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneOffset.UTC), 2, Duration.ofHours(1));
    }

    private PublishKudoRequest request(final UUID authorId, final UUID recipientId, final String content,
                                       final String key) {
        final PublishKudoRequest request = new PublishKudoRequest();
        request.setAuthorId(authorId);
        request.setRecipientId(recipientId);
        request.setCategoryId(category.getId());
        request.setContent(content);
        request.setIdempotencyKey(key);
        return request;
    }

    private PublishKudoRequest command(final String key, final String content) {
        return request(author.getId(), recipient.getId(), content, key);
    }

    private String codeOf(final PublishKudoRequest request) {
        return assertThrows(DomainException.class, () -> useCase.execute(request)).getCode();
    }

    @Test
    void publishSavesKudoThreePendingNotificationsAndAudit() {
        final PublishKudoResponse result = useCase.execute(command("k1", "Gracias"));
        assertEquals(1, memory.kudos.size());
        assertEquals(3, result.getNotifications().size());
        assertEquals(List.of(NotificationChannel.values()),
                result.getNotifications().stream().map(NotificationResponse::getChannel).toList());
        assertEquals(1, memory.audits.size());
        assertEquals(result.getId(), memory.audits.get(0).resourceId());
    }

    @Test
    void sameKeyAndSameRequestReturnsTheOriginalWithoutSavingAgain() {
        final PublishKudoResponse first = useCase.execute(command("k1", "Gracias"));
        final PublishKudoResponse second = useCase.execute(command("k1", "Gracias"));
        assertEquals(first.getId(), second.getId());
        assertEquals(1, memory.kudos.size());
        assertEquals(1, memory.audits.size());
    }

    @Test
    void sameKeyWithOtherContentIsAConflict() {
        useCase.execute(command("k1", "Gracias"));
        assertEquals(MessageCodes.KUDO_IDEMPOTENCY_CONFLICT, codeOf(command("k1", "Otro texto")));
    }

    @Test
    void missingKeyIsRejected() {
        assertEquals(MessageCodes.KUDO_IDEMPOTENCY_KEY_REQUIRED, codeOf(command(" ", "Gracias")));
    }

    @Test
    void rejectedByRulesSavesNothing() {
        final PublishKudoRequest toSelf = request(author.getId(), author.getId(), "Hola", "k2");
        assertEquals(MessageCodes.KUDO_RECIPIENT_SELF, codeOf(toSelf));
        recipient.setStatus(UserStatus.INACTIVE);
        assertEquals(MessageCodes.USER_NOT_ACTIVE, codeOf(command("k3", "Hola")));
        assertEquals(0, memory.kudos.size());
        assertEquals(0, memory.audits.size());
    }

    @Test
    void dailyLimitCountsOnlyPublishedKudos() {
        useCase.execute(command("k1", "Uno"));
        useCase.execute(command("k2", "Dos"));
        assertEquals(MessageCodes.KUDO_DAILY_LIMIT, codeOf(command("k3", "Tres")));
        assertEquals(2, memory.kudos.size());
    }

    @Test
    void infectedAttachmentRejectsTheWholeKudo() {
        scannerFails = true;
        assertEquals(MessageCodes.KUDO_ATTACHMENT_INFECTED, codeOf(command("k1", "Gracias")));
        assertEquals(0, memory.kudos.size());
        assertEquals(0, memory.notifications.size());
        assertEquals(0, memory.keys.size());
    }
}
