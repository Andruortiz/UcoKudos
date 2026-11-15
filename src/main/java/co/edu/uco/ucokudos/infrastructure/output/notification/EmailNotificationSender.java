package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Locale;


@Component
@ConditionalOnProperty("spring.mail.host")
public class EmailNotificationSender implements NotificationSender {

    private static final Locale LOCALE = Locale.of("es", "CO");

    private final JavaMailSender mailSender;
    private final MessageSource messages;
    private final String from;

    public EmailNotificationSender(final JavaMailSender mailSender, final MessageSource messages,
                                   @Value("${kudos.mail.from}") final String from) {
        this.mailSender = mailSender;
        this.messages = messages;
        this.from = from;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public DeliveryResult send(final KudoNotice notice) {
        if (notice.recipientEmail() == null || notice.recipientEmail().isBlank()) {
            return DeliveryResult.SKIPPED;
        }
        final SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(notice.recipientEmail());
        mail.setSubject(messages.getMessage(MessageCodes.NOTIFICATION_EMAIL_SUBJECT,
                new Object[]{notice.authorName()}, LOCALE));
        mail.setText(messages.getMessage(MessageCodes.NOTIFICATION_EMAIL_BODY,
                new Object[]{notice.recipientName(), notice.authorName(), notice.categoryName(), notice.link()},
                LOCALE));
        mailSender.send(mail);
        return DeliveryResult.SENT;
    }
}
