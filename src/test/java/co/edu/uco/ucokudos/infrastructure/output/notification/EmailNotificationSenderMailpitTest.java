package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sends a real email to a running Mailpit and reads it back from its API, which also proves that Spring builds the
 * sender from spring.mail.* alone. Start Mailpit and run:
 * <pre>
 * docker run -d --name mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
 * MAILPIT_API=http://localhost:8025 ./mvnw test -Dtest=EmailNotificationSenderMailpitTest
 * </pre>
 * Without MAILPIT_API it is skipped, which is what happens in the CI.
 */
@EnabledIfEnvironmentVariable(named = "MAILPIT_API", matches = ".+")
@SpringBootTest(properties = {"spring.mail.host=localhost", "spring.mail.port=${MAILPIT_SMTP_PORT:1025}"})
class EmailNotificationSenderMailpitTest {

    @Autowired EmailNotificationSender sender;

    private static String api(final String method, final String path) throws Exception {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(System.getenv("MAILPIT_API") + path))
                .method(method, HttpRequest.BodyPublishers.noBody()).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .body();
    }

    @Test
    void theEmailReachesMailpitWithTheAccentsIntact() throws Exception {
        api("DELETE", "/api/v1/messages");
        final KudoNotice notice = new KudoNotice(UUID.randomUUID(), UUID.randomUUID(), "Luis Docente",
                "luis@ucokudosgmail.onmicrosoft.com", null, "Ana Estudiante", "Empatía",
                "http://localhost:5173/kudos/abc");

        assertEquals(DeliveryResult.SENT, sender.send(notice));

        final String inbox = api("GET", "/api/v1/messages");
        assertTrue(inbox.contains("Recibiste un Kudo de Ana Estudiante"), inbox);
        assertTrue(inbox.contains("luis@ucokudosgmail.onmicrosoft.com"), inbox);
    }
}
