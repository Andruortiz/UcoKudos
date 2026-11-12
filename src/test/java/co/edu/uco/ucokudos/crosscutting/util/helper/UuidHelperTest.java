package co.edu.uco.ucokudos.crosscutting.util.helper;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UuidHelperTest {

    @Test
    void generatesVersion7WithRfcVariant() {
        final UUID uuid = UuidHelper.generate();
        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void embedsTheCurrentTimeInMilliseconds() {
        final long before = System.currentTimeMillis();
        final UUID uuid = UuidHelper.generate();
        final long timestamp = uuid.getMostSignificantBits() >>> 16;
        assertTrue(timestamp >= before && timestamp <= System.currentTimeMillis());
    }

    @Test
    void generatesDifferentValues() {
        assertNotEquals(UuidHelper.generate(), UuidHelper.generate());
    }

    @Test
    void defaultIsUsedForNullAndBlank() {
        assertEquals(UuidHelper.DEFAULT_UUID, UuidHelper.getDefault(null));
        assertEquals(UuidHelper.DEFAULT_UUID, UuidHelper.getFromString(null));
        assertEquals(UuidHelper.DEFAULT_UUID, UuidHelper.getFromString("  "));
        assertTrue(UuidHelper.isDefault(null));
        assertFalse(UuidHelper.isDefault(UuidHelper.generate()));
    }

    @Test
    void parsesAValidString() {
        final UUID uuid = UuidHelper.generate();
        assertEquals(uuid, UuidHelper.getFromString(uuid.toString()));
    }
}
