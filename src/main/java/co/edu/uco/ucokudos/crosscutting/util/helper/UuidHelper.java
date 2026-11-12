package co.edu.uco.ucokudos.crosscutting.util.helper;

import java.security.SecureRandom;
import java.util.UUID;

public class UuidHelper {




    public static final UUID DEFAULT_UUID = new UUID(0L, 0L);
    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidHelper() {
    }

    public static UUID generate() {
        long milliseconds = System.currentTimeMillis();
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        long mostBits = (milliseconds << 16) | 0x7000L | ((randomBytes[0] & 0x0FL) << 8) | (randomBytes[1] & 0xFFL);
        long leastBits = ((randomBytes[2] & 0x3FL) | 0x80L) << 56;
        for (int i = 3; i < 10; i++) {
            leastBits |= (randomBytes[i] & 0xFFL) << (8 * (9 - i));
        }
        return new UUID(mostBits, leastBits);
    }

    public static UUID getDefault(final UUID value) {
        return value == null ? DEFAULT_UUID : value;
    }

    public static UUID getFromString(final String uuidAsString) {
        if (uuidAsString == null || uuidAsString.isBlank())  {
            return DEFAULT_UUID;
        }
        return UUID.fromString(uuidAsString);
    }

    public static boolean isDefault(final UUID value) {
        return DEFAULT_UUID.equals(getDefault(value));
    }
}
