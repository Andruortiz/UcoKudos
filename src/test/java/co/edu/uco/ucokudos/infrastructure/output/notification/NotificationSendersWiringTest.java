package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.UcoKudosApplication;
import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Each channel exists only when its provider is configured; without configuration the notice stays pending. */
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
    void withoutConfigurationThereIsNoSender() {
        assertEquals(Set.of(), channelsWith());
    }

    @Test
    void whatsAppExistsOnlyWithTheTokenAndThePhoneNumberId() {
        assertEquals(Set.of(), channelsWith("kudos.whatsapp.token=t"));
        assertEquals(Set.of(NotificationChannel.WHATSAPP),
                channelsWith("kudos.whatsapp.token=t", "kudos.whatsapp.phone-number-id=1"));
    }

    @Test
    void emailExistsOnlyWithTheSmtpHost() {
        assertEquals(Set.of(NotificationChannel.EMAIL), channelsWith("spring.mail.host=localhost"));
    }

    @Test
    void bothChannelsCanRunTogether() {
        assertEquals(Set.of(NotificationChannel.EMAIL, NotificationChannel.WHATSAPP),
                channelsWith("spring.mail.host=localhost", "kudos.whatsapp.token=t", "kudos.whatsapp.phone-number-id=1"));
    }
}
