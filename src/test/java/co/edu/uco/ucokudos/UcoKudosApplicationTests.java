package co.edu.uco.ucokudos;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UcoKudosApplicationTests {

    @Autowired
    private MessageSource messageSource;

    @Test
    void contextLoads() {
    }

    @Test
    void catalogResolvesMessagesWithAndWithoutParameters() {
        final Locale colombia = Locale.forLanguageTag("es-CO");
        assertEquals("El usuario no está activo",
                messageSource.getMessage(MessageCodes.USER_NOT_ACTIVE, null, colombia));
        assertEquals("El correo debe ser institucional (uco.net.co)",
                messageSource.getMessage(MessageCodes.USER_EMAIL_NOT_INSTITUTIONAL, new Object[]{"uco.net.co"}, colombia));
    }
}
