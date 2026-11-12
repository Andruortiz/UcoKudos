package co.edu.uco.ucokudos.crosscutting.message;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageCatalogTest {

    private final ResourceBundle catalog = ResourceBundle.getBundle("messages", Locale.ROOT);

    private static Set<String> declaredCodes() throws IllegalAccessException {
        final Set<String> codes = new HashSet<>();
        for (final Field field : MessageCodes.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && field.getType() == String.class) {
                codes.add((String) field.get(null));
            }
        }
        return codes;
    }

    @Test
    void everyCodeHasATextInTheCatalog() throws IllegalAccessException {
        for (final String code : declaredCodes()) {
            assertTrue(catalog.containsKey(code), "Missing text for code " + code);
            assertFalse(catalog.getString(code).isBlank(), "Blank text for code " + code);
        }
    }

    @Test
    void everyTextInTheCatalogBelongsToACode() throws IllegalAccessException {
        final Set<String> codes = declaredCodes();
        for (final String key : catalog.keySet()) {
            assertTrue(codes.contains(key), "The catalog has a text without a code: " + key);
        }
    }

    @Test
    void thereAreAsManyTextsAsCodes() throws IllegalAccessException {
        assertEquals(declaredCodes().size(), catalog.keySet().size());
    }
}
