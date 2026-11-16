package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class PushNotificationSender implements NotificationSender {

    private final Map<UUID, List<SseEmitter>> streams = new ConcurrentHashMap<>();

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    public SseEmitter subscribe(final UUID userId) {
        final SseEmitter emitter = new SseEmitter(0L);
        streams.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(error -> remove(userId, emitter));
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (final IOException | IllegalStateException e) {
            remove(userId, emitter);
        }
        return emitter;
    }

    @Override
    public DeliveryResult send(final KudoNotice notice) {
        final List<SseEmitter> open = streams.get(notice.recipientId());
        if (open == null || open.isEmpty()) {
            return DeliveryResult.SKIPPED;
        }
        final Map<String, Object> payload = Map.of("kudoId", notice.kudoId(), "authorName", notice.authorName(),
                "categoryName", notice.categoryName(), "link", notice.link());
        boolean delivered = false;
        for (final SseEmitter emitter : open) {
            try {
                emitter.send(SseEmitter.event().name("kudo").data(payload, MediaType.APPLICATION_JSON));
                delivered = true;
            } catch (final IOException | IllegalStateException e) {
                remove(notice.recipientId(), emitter);
            }
        }
        return delivered ? DeliveryResult.SENT : DeliveryResult.SKIPPED;
    }

    @Scheduled(fixedDelay = 25_000)
    void heartbeat() {
        streams.forEach((userId, emitters) -> emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("keep-alive"));
            } catch (final IOException | IllegalStateException e) {
                remove(userId, emitter);
            }
        }));
    }

    private void remove(final UUID userId, final SseEmitter emitter) {
        streams.computeIfPresent(userId, (id, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
