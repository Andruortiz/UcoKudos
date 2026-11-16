package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PhoneVerification {

    private UUID userId;
    private String codeHash;
    private Instant expiresAt;
    private Instant lastSentAt;
    private Instant windowStart;
    private int attempts;
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
