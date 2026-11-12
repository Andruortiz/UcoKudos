package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.AuditAction;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.IdempotencyRecord;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.domain.model.NotificationStatus;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.CategoryEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.CategoryJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JpaAdaptersTest {

    private static final Instant NOW = Instant.parse("2026-11-12T15:00:00Z");

    @Autowired UserRepositoryAdapter users;
    @Autowired UserJpaRepository userJpa;
    @Autowired UserEntityMapper userMapper;
    @Autowired CategoryRepositoryAdapter categories;
    @Autowired CategoryJpaRepository categoryJpa;
    @Autowired CategoryEntityMapper categoryMapper;
    @Autowired KudoRepositoryAdapter kudos;
    @Autowired NotificationRepositoryAdapter notifications;
    @Autowired AuditRepositoryAdapter audits;
    @Autowired IdempotencyRepositoryAdapter idempotency;

    private Kudo kudo(final UUID authorId, final Instant createdAt, final List<Attachment> attachments) {
        final Kudo kudo = new Kudo();
        kudo.setId(UuidHelper.generate());
        kudo.setAuthorId(authorId);
        kudo.setRecipientId(UuidHelper.generate());
        kudo.setCategoryId(UuidHelper.generate());
        kudo.setContent("Gracias por la ayuda");
        kudo.setCreatedAt(createdAt);
        kudo.setAttachments(attachments);
        return kudo;
    }

    @Test
    void userRoundTrip() {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId("dir-" + user.getId());
        user.setName("Ana");
        user.setEmail(user.getId() + "@uco.net.co");
        user.setPhone("+573001234567");
        user.setRole(Role.TEACHER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setPhoneVerified(true);
        user.setCreatedAt(NOW);
        userJpa.save(userMapper.toJpa(user));

        final User found = users.findById(user.getId()).orElseThrow();
        assertEquals(Role.TEACHER, found.getRole());
        assertEquals(user.getEmail(), found.getEmail());
        assertTrue(found.isActive() && found.isEmailVerified() && found.isPhoneVerified());
    }

    @Test
    void categoryRoundTrip() {
        final Category category = new Category();
        category.setId(UuidHelper.generate());
        category.setName("Colaboración " + category.getId());
        category.setCreatedAt(NOW);
        categoryJpa.save(categoryMapper.toJpa(category));

        final Category found = categories.findById(category.getId()).orElseThrow();
        assertEquals(category.getName(), found.getName());
        assertTrue(found.isActive());
    }

    @Test
    void kudoIsSavedWithItsAttachmentsAndCountedByDay() {
        final UUID author = UuidHelper.generate();
        final Kudo kudo = kudo(author, NOW, List.of(
                new Attachment("a.pdf", "application/pdf", new byte[]{1, 2, 3}),
                new Attachment("b.png", "image/png", new byte[]{4, 5})));
        kudos.save(kudo);
        kudos.save(kudo(author, NOW.minusSeconds(86_400), List.of()));

        final Kudo found = kudos.findById(kudo.getId()).orElseThrow();
        assertEquals("Gracias por la ayuda", found.getContent());
        assertEquals(2, found.getAttachments().size());
        assertTrue(found.getAttachments().stream().anyMatch(a -> a.name().equals("a.pdf")
                && a.contentType().equals("application/pdf")));
        final Attachment pdf = found.getAttachments().stream().filter(a -> a.name().equals("a.pdf")).findFirst().orElseThrow();
        assertArrayEquals(new byte[]{1, 2, 3}, pdf.content());
        assertEquals(1, kudos.countByAuthorBetween(author, NOW.minusSeconds(3600), NOW.plusSeconds(3600)));
    }

    @Test
    void notificationsAreSavedAndFoundByKudo() {
        final UUID kudoId = UuidHelper.generate();
        final List<Notification> list = java.util.Arrays.stream(NotificationChannel.values()).map(channel -> {
            final Notification notification = new Notification();
            notification.setId(UuidHelper.generate());
            notification.setKudoId(kudoId);
            notification.setChannel(channel);
            notification.setCreatedAt(NOW);
            return notification;
        }).toList();
        notifications.saveAll(list);

        final List<Notification> found = notifications.findByKudoId(kudoId);
        assertEquals(3, found.size());
        assertTrue(found.stream().allMatch(n -> n.getStatus() == NotificationStatus.PENDING));
    }

    @Test
    void findDueReturnsOnlyPendingDueNotificationsOfTheGivenChannels() {
        final UUID kudoId = UuidHelper.generate();
        final Notification due = notification(kudoId, NotificationChannel.EMAIL, NOW);
        final Notification later = notification(kudoId, NotificationChannel.WHATSAPP, NOW.plusSeconds(600));
        final Notification sent = notification(kudoId, NotificationChannel.PUSH, NOW);
        sent.setStatus(NotificationStatus.SENT);
        notifications.saveAll(List.of(due, later, sent));

        final List<UUID> found = notifications.findDue(List.of(NotificationChannel.values()), NOW, 1000).stream()
                .filter(n -> n.getKudoId().equals(kudoId)).map(Notification::getId).toList();
        assertEquals(List.of(due.getId()), found);
        assertTrue(notifications.findDue(List.of(NotificationChannel.WHATSAPP), NOW, 1000).stream()
                .noneMatch(n -> n.getKudoId().equals(kudoId)));

        due.setStatus(NotificationStatus.SENT);
        due.setAttempts(1);
        notifications.save(due);
        assertTrue(notifications.findByKudoId(kudoId).stream()
                .anyMatch(n -> n.getId().equals(due.getId()) && n.getStatus() == NotificationStatus.SENT));
    }

    private Notification notification(final UUID kudoId, final NotificationChannel channel, final Instant next) {
        final Notification notification = new Notification();
        notification.setId(UuidHelper.generate());
        notification.setKudoId(kudoId);
        notification.setChannel(channel);
        notification.setCreatedAt(NOW);
        notification.setNextAttemptAt(next);
        return notification;
    }

    @Test
    void auditRecordIsSaved() {
        audits.save(new AuditRecord(UuidHelper.generate(), NOW, UuidHelper.generate(), AuditAction.KUDO_PUBLISHED,
                "KUDO", UuidHelper.generate(), "key-1"));
    }

    @Test
    void idempotencyKeyIsOverwrittenAndHonorsTheWindow() {
        final UUID author = UuidHelper.generate();
        final String key = "key-" + author;
        final UUID first = UuidHelper.generate();
        final UUID second = UuidHelper.generate();
        idempotency.save(new IdempotencyRecord(key, author, "hash1", first, NOW));
        idempotency.save(new IdempotencyRecord(key, author, "hash2", second, NOW.plusSeconds(7200)));

        assertEquals(second, idempotency.find(key, author, NOW.plusSeconds(3600)).orElseThrow().kudoId());
        assertTrue(idempotency.find(key, author, NOW.plusSeconds(8000)).isEmpty());
    }
}
