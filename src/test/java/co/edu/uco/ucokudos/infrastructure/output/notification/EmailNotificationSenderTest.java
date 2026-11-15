package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailNotificationSenderTest {

    private final List<SimpleMailMessage> sent = new ArrayList<>();
    private boolean fail;

    private final JavaMailSenderImpl mailSender = new JavaMailSenderImpl() {
        @Override
        public void send(final SimpleMailMessage message) {
            if (fail) {
                throw new MailSendException("SMTP caido");
            }
            sent.add(message);
        }
    };

    private final EmailNotificationSender sender = new EmailNotificationSender(mailSender, messages(),
            "no-reply@ucokudos.local");

    private static StaticMessageSource messages() {
        final StaticMessageSource source = new StaticMessageSource();
        final Locale locale = Locale.of("es", "CO");
        source.addMessage(MessageCodes.NOTIFICATION_EMAIL_SUBJECT, locale, "Recibiste un Kudo de {0}");
        source.addMessage(MessageCodes.NOTIFICATION_EMAIL_BODY, locale, "Hola {0}, {1} te envio un Kudo de {2}: {3}");
        return source;
    }

    private static KudoNotice notice(final String email) {
        return new KudoNotice(UUID.randomUUID(), UUID.randomUUID(), "Luis Docente", email, null,
                "Ana Estudiante", "Empatía", "http://localhost:5173/kudos/abc");
    }

    @Test
    void itHandlesTheEmailChannel() {
        assertEquals(NotificationChannel.EMAIL, sender.channel());
    }

    @Test
    void itSendsTheNoticeWithTheTextsFromTheCatalog() {
        final DeliveryResult result = sender.send(notice("luis@uco.net.co"));

        assertEquals(DeliveryResult.SENT, result);
        assertEquals(1, sent.size());
        final SimpleMailMessage mail = sent.get(0);
        assertEquals("no-reply@ucokudos.local", mail.getFrom());
        assertEquals("luis@uco.net.co", mail.getTo()[0]);
        assertEquals("Recibiste un Kudo de Ana Estudiante", mail.getSubject());
        assertTrue(mail.getText().contains("Luis Docente"));
        assertTrue(mail.getText().contains("Empatía"));
        assertTrue(mail.getText().contains("http://localhost:5173/kudos/abc"));
    }

    @Test
    void aRecipientWithoutEmailIsSkippedNotSent() {
        assertEquals(DeliveryResult.SKIPPED, sender.send(notice(null)));
        assertEquals(DeliveryResult.SKIPPED, sender.send(notice("  ")));
        assertTrue(sent.isEmpty());
    }

    @Test
    void aSmtpFailureIsNotHiddenSoTheDispatcherCanRetry() {
        fail = true;

        assertThrows(MailSendException.class, () -> sender.send(notice("luis@uco.net.co")));
    }
}
