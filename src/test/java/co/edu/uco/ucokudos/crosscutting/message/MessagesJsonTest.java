package co.edu.uco.ucokudos.crosscutting.message;

import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JsonParserFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesJsonTest {

    private Map<String, Object> remote() throws IOException {
        return JsonParserFactory.getJsonParser().parseMap(Files.readString(Path.of("config/messages.json")));
    }

    @Test
    void everyKeyBelongsToACodeAndEveryTextIsAValidPattern() throws Exception {
        final Set<String> codes = new HashSet<>();
        for (final Field field : MessageCodes.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && field.getType() == String.class) {
                codes.add((String) field.get(null));
            }
        }
        final Map<String, Object> messages = remote();
        assertFalse(messages.isEmpty());
        messages.forEach((code, text) -> {
            assertTrue(codes.contains(code), "Unknown code in config/messages.json: " + code);
            assertTrue(text instanceof String value && !value.isBlank(), "Bad text for " + code);
            assertDoesNotThrow(() -> new MessageFormat((String) text), "Bad pattern for " + code);
        });
    }
}
