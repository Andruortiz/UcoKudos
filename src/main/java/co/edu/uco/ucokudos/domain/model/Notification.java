package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Notification {

    private UUID id;
    private UUID kudoId;
    private NotificationChannel channel;
    private NotificationStatus status = NotificationStatus.PENDING;
    private int attempts;
    private Instant nextAttemptAt;
    private String lastError;
    private Instant createdAt;
    private Instant sentAt;

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = id; }
    public UUID getKudoId() { return kudoId; }
    public void setKudoId(final UUID kudoId) { this.kudoId = kudoId; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(final NotificationChannel channel) { this.channel = channel; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(final NotificationStatus status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(final int attempts) { this.attempts = attempts; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(final Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public String getLastError() { return lastError; }
    public void setLastError(final String lastError) { this.lastError = lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Instant createdAt) { this.createdAt = createdAt; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(final Instant sentAt) { this.sentAt = sentAt; }
}
