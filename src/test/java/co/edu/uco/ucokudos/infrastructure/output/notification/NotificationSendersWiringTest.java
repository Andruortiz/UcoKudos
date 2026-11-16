package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.UcoKudosApplication;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Email and WhatsApp exist only when their provider is configured; push needs none, so it is always there. */
class NotificationSendersWiringTest {

    private static Set<NotificationChannel> channelsWith(final String... properties) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(UcoKudosApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .properties(properties)
                .run()) {
            return context.getBeansOfType(NotificationSender.class).values().stream()
                    .map(NotificationSender::channel)
                    .collect(Collectors.toSet());
        }
    }

    @Test
    void withoutConfigurationOnlyPushExists() {
        assertEquals(Set.of(NotificationChannel.PUSH), channelsWith());
    }

    @Test
    void whatsAppExistsOnlyWithTheTokenAndThePhoneNumberId() {
        assertEquals(Set.of(NotificationChannel.PUSH), channelsWith("kudos.whatsapp.token=t"));
        assertEquals(Set.of(NotificationChannel.PUSH, NotificationChannel.WHATSAPP),
                channelsWith("kudos.whatsapp.token=t", "kudos.whatsapp.phone-number-id=1"));
    }

    @Test
    void emailExistsOnlyWithTheSmtpHost() {
        assertEquals(Set.of(NotificationChannel.PUSH, NotificationChannel.EMAIL),
                channelsWith("spring.mail.host=localhost"));
    }

    @Test
    void allChannelsCanRunTogether() {
        assertEquals(Set.of(NotificationChannel.PUSH, NotificationChannel.EMAIL, NotificationChannel.WHATSAPP),
                channelsWith("spring.mail.host=localhost", "kudos.whatsapp.token=t", "kudos.whatsapp.phone-number-id=1"));
    }
}
