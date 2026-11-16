package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "phone_verification")
public class PhoneVerificationJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_sent_at", nullable = false)
    private Instant lastSentAt;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "sent_count", nullable = false)
    private int sentCount;

    public UUID getUserId() { return userId; }
    public void setUserId(final UUID userId) { this.userId = userId; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(final String codeHash) { this.codeHash = codeHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(final Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(final Instant lastSentAt) { this.lastSentAt = lastSentAt; }
    public Instant getWindowStart() { return windowStart; }
    public void setWindowStart(final Instant windowStart) { this.windowStart = windowStart; }
    public int getAttempts() { return attempts; }
    public void setAttempts(final int attempts) { this.attempts = attempts; }
    public int getSentCount() { return sentCount; }
    public void setSentCount(final int sentCount) { this.sentCount = sentCount; }
}
