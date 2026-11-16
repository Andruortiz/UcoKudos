package co.edu.uco.ucokudos.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

public class PhoneVerificationDomain {

    private static final int CODE_BOUND = 1_000_000;

    private final SecureRandom random = new SecureRandom();

    public String newCode() {
        return String.format("%06d", random.nextInt(CODE_BOUND));
    }

    public String hash(final UUID userId, final String phone, final String code) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((userId + "|" + phone + "|" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean matches(final String expectedHash, final String actualHash) {
        return MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8),
                actualHash.getBytes(StandardCharsets.UTF_8));
    }
}
