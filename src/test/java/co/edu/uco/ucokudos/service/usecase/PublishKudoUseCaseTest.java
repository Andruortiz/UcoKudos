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
import static org.junit.jupiter.api.Assertions.assertSame;
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

    private PublishKudoCommand command(final String key, final String content) {
        return new PublishKudoCommand(author.getId(), recipient.getId(), category.getId(), content, List.of(), key);
    }

    private String codeOf(final PublishKudoCommand command) {
        return assertThrows(DomainException.class, () -> useCase.execute(command)).getCode();
    }

    @Test
    void publishSavesKudoThreePendingNotificationsAndAudit() {
        final PublishKudoResult result = useCase.execute(command("k1", "Gracias"));
        assertEquals(1, memory.kudos.size());
        assertEquals(3, result.notifications().size());
        assertEquals(List.of(NotificationChannel.values()),
                result.notifications().stream().map(Notification::getChannel).toList());
        assertEquals(1, memory.audits.size());
        assertEquals(result.kudo().getId(), memory.audits.get(0).resourceId());
    }

    @Test
    void sameKeyAndSameRequestReturnsTheOriginalWithoutSavingAgain() {
        final PublishKudoResult first = useCase.execute(command("k1", "Gracias"));
        final PublishKudoResult second = useCase.execute(command("k1", "Gracias"));
        assertSame(first.kudo(), second.kudo());
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
        final PublishKudoCommand toSelf = new PublishKudoCommand(author.getId(), author.getId(),
                category.getId(), "Hola", List.of(), "k2");
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
