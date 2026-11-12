package co.edu.uco.ucokudos.service.usecase;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.KudoDomain;
import co.edu.uco.ucokudos.domain.UserDomain;
import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.domain.model.AuditAction;
import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.IdempotencyRecord;
import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.service.input.dto.request.PublishKudoRequest;
import co.edu.uco.ucokudos.service.input.dto.response.NotificationResponse;
import co.edu.uco.ucokudos.service.input.dto.response.PublishKudoResponse;
import co.edu.uco.ucokudos.service.input.port.PublishKudoPort;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentProcessor;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;
import co.edu.uco.ucokudos.service.output.port.repository.IdempotencyRepository;
import co.edu.uco.ucokudos.service.output.port.repository.KudoRepository;
import co.edu.uco.ucokudos.service.output.port.repository.NotificationRepository;
import co.edu.uco.ucokudos.service.output.port.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Transactional
public class PublishKudoUseCase implements PublishKudoPort {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final String RESOURCE_TYPE = "KUDO";

    private final UserRepository users;
    private final CategoryRepository categories;
    private final KudoRepository kudos;
    private final NotificationRepository notifications;
    private final AuditRepository audits;
    private final IdempotencyRepository idempotency;
    private final AttachmentProcessor attachmentProcessor;
    private final UserDomain userDomain;
    private final KudoDomain kudoDomain;
    private final Clock clock;
    private final int dailyLimit;
    private final Duration idempotencyWindow;

    public PublishKudoUseCase(final UserRepository users, final CategoryRepository categories,
                              final KudoRepository kudos, final NotificationRepository notifications,
                              final AuditRepository audits, final IdempotencyRepository idempotency,
                              final AttachmentProcessor attachmentProcessor, final UserDomain userDomain,
                              final KudoDomain kudoDomain, final Clock clock,
                              final int dailyLimit, final Duration idempotencyWindow) {
        this.users = users;
        this.categories = categories;
        this.kudos = kudos;
        this.notifications = notifications;
        this.audits = audits;
        this.idempotency = idempotency;
        this.attachmentProcessor = attachmentProcessor;
        this.userDomain = userDomain;
        this.kudoDomain = kudoDomain;
        this.clock = clock;
        this.dailyLimit = dailyLimit;
        this.idempotencyWindow = idempotencyWindow;
    }

    @Override
    public PublishKudoResponse execute(final PublishKudoRequest request) {
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new DomainException(MessageCodes.KUDO_IDEMPOTENCY_KEY_REQUIRED);
        }
        final Instant now = clock.instant();
        final String hash = fingerprint(request);

        final Optional<IdempotencyRecord> previous =
                idempotency.find(request.getIdempotencyKey(), request.getAuthorId(), now.minus(idempotencyWindow));
        if (previous.isPresent()) {
            if (!previous.get().requestHash().equals(hash)) {
                throw new DomainException(MessageCodes.KUDO_IDEMPOTENCY_CONFLICT);
            }
            final Kudo original = kudos.findById(previous.get().kudoId()).orElseThrow(IllegalStateException::new);
            return toResponse(original, notifications.findByKudoId(original.getId()));
        }

        final User author = users.findById(request.getAuthorId())
                .orElseThrow(() -> new DomainException(MessageCodes.USER_NOT_FOUND));
        final User recipient = users.findById(request.getRecipientId())
                .orElseThrow(() -> new DomainException(MessageCodes.USER_NOT_FOUND));
        userDomain.validateActive(author);
        userDomain.validateActive(recipient);
        kudoDomain.validateContent(request.getContent());
        kudoDomain.validateNotSelf(author, recipient);
        kudoDomain.validateRoleCanSend(author.getRole(), recipient.getRole());

        final Category category = categories.findById(request.getCategoryId())
                .orElseThrow(() -> new DomainException(MessageCodes.CATEGORY_NOT_FOUND));
        kudoDomain.validateCategoryActive(category);

        final LocalDate today = LocalDate.ofInstant(now, ZONE);
        kudoDomain.validateDailyLimit(kudos.countByAuthorBetween(author.getId(),
                today.atStartOfDay(ZONE).toInstant(), today.plusDays(1).atStartOfDay(ZONE).toInstant()), dailyLimit);

        kudoDomain.validateAttachments(request.getAttachments());
        final List<Attachment> processed = attachmentProcessor.process(request.getAttachments());

        final Kudo kudo = new Kudo();
        kudo.setId(UuidHelper.generate());
        kudo.setAuthorId(author.getId());
        kudo.setRecipientId(recipient.getId());
        kudo.setCategoryId(category.getId());
        kudo.setContent(request.getContent());
        kudo.setCreatedAt(now);
        kudo.setAttachments(processed);

        final List<Notification> pending = new ArrayList<>();
        for (final NotificationChannel channel : NotificationChannel.values()) {
            final Notification notification = new Notification();
            notification.setId(UuidHelper.generate());
            notification.setKudoId(kudo.getId());
            notification.setChannel(channel);
            notification.setCreatedAt(now);
            notification.setNextAttemptAt(now);
            pending.add(notification);
        }

        kudos.save(kudo);
        notifications.saveAll(pending);
        audits.save(new AuditRecord(UuidHelper.generate(), now, author.getId(), AuditAction.KUDO_PUBLISHED,
                RESOURCE_TYPE, kudo.getId(), request.getIdempotencyKey()));
        idempotency.save(new IdempotencyRecord(request.getIdempotencyKey(), author.getId(), hash, kudo.getId(), now));
        return toResponse(kudo, pending);
    }

    private static PublishKudoResponse toResponse(final Kudo kudo, final List<Notification> list) {
        final PublishKudoResponse response = new PublishKudoResponse();
        response.setId(kudo.getId());
        response.setStatus(kudo.getStatus());
        response.setCreatedAt(kudo.getCreatedAt());
        response.setNotifications(list.stream().map(notification -> {
            final NotificationResponse item = new NotificationResponse();
            item.setChannel(notification.getChannel());
            item.setStatus(notification.getStatus());
            return item;
        }).toList());
        return response;
    }

    private static String fingerprint(final PublishKudoRequest request) {
        final StringBuilder text = new StringBuilder()
                .append(request.getRecipientId()).append('|').append(request.getCategoryId()).append('|')
                .append(request.getContent());
        for (final Attachment attachment : request.getAttachments()) {
            text.append('|').append(attachment.name()).append(':').append(attachment.size());
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
