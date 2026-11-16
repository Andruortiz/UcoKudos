package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Talks to a fake Meta server on localhost, so it checks the exact request without sending a real message. */
class WhatsAppNotificationSenderTest {

    private record Received(String method, String path, String authorization, String contentType, String body) {
    }

    private HttpServer server;
    private final List<Received> received = new ArrayList<>();
    private int status = 200;
    private String answer = "{\"messages\":[{\"id\":\"wamid.1\"}]}";
    private WhatsAppNotificationSender sender;

    @BeforeEach
    void startFakeMeta() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            received.add(new Received(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            final byte[] bytes = answer.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        sender = new WhatsAppNotificationSender("http://localhost:" + server.getAddress().getPort(), "v22.0",
                "PHONE-ID-1", "SECRET-TOKEN", "kudos_envios_url", "es_CO");
    }

    @AfterEach
    void stopFakeMeta() {
        server.stop(0);
    }

    private static final UUID KUDO_ID = UUID.fromString("0a1b2c3d-0000-0000-0000-000000000000");

    private static KudoNotice notice(final String phone) {
        return new KudoNotice(KUDO_ID, UUID.randomUUID(), "Luis Docente", "luis@uco.net.co", phone,
                "Ana Estudiante", "Empatía", "http://localhost:5173/kudos/abc");
    }

    @Test
    void itHandlesTheWhatsAppChannel() {
        assertEquals(NotificationChannel.WHATSAPP, sender.channel());
    }

    @Test
    void itPostsTheApprovedTemplateWithTheThreeVariablesInOrderAndTheKudoIdInTheButton() {
        final DeliveryResult result = sender.send(notice("+57 300 123 4567"));

        assertEquals(DeliveryResult.SENT, result);
        assertEquals(1, received.size());
        final Received request = received.get(0);
        assertEquals("POST", request.method());
        assertEquals("/v22.0/PHONE-ID-1/messages", request.path());
        assertEquals("Bearer SECRET-TOKEN", request.authorization());
        assertTrue(request.contentType().startsWith("application/json"));
        final String body = request.body();
        assertTrue(body.contains("\"messaging_product\":\"whatsapp\""), body);
        assertTrue(body.contains("\"to\":\"573001234567\""), body);
        assertTrue(body.contains("\"name\":\"kudos_envios_url\""), body);
        assertTrue(body.contains("\"code\":\"es_CO\""), body);
        // recipient, author, category: the order of the variables in the template body
        final int recipient = body.indexOf("Luis Docente");
        final int author = body.indexOf("Ana Estudiante");
        final int category = body.indexOf("Empatía");
        assertTrue(recipient > 0 && recipient < author && author < category, body);
        // the dynamic URL button receives only the id that completes https://kudos.ucokudos.qd.je/kudos/{{1}}
        assertTrue(body.contains("\"sub_type\":\"url\""), body);
        assertTrue(body.contains("\"index\":\"0\""), body);
        assertTrue(body.indexOf(KUDO_ID.toString()) > category, body);
        assertFalse(body.contains("http://localhost:5173"), body);
    }

    @Test
    void aRecipientWithoutPhoneIsSkippedAndNothingIsSent() {
        assertEquals(DeliveryResult.SKIPPED, sender.send(notice(null)));
        assertEquals(DeliveryResult.SKIPPED, sender.send(notice("  ")));
        assertTrue(received.isEmpty());
    }

    @Test
    void lineBreaksInTheVariablesAreFlattenedBecauseTheTemplateRejectsThem() {
        final KudoNotice notice = new KudoNotice(UUID.randomUUID(), UUID.randomUUID(), "Luis", null, "+573001234567",
                "Ana\nPérez\t  Díaz", null, "http://x");

        sender.send(notice);

        final String body = received.get(0).body();
        assertTrue(body.contains("Ana Pérez Díaz"), body);
        assertFalse(body.contains("\\n") || body.contains("\\t"), body);
        assertTrue(body.contains("\"text\":\"-\""), body);
    }

    @Test
    void aProviderErrorIsReportedWithItsReasonAndWithoutTheToken() {
        status = 400;
        answer = "{\"error\":{\"message\":\"Recipient phone number not in allowed list\",\"code\":131030}}";

        final IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> sender.send(notice("+573001234567")));

        assertTrue(error.getMessage().contains("400"));
        assertTrue(error.getMessage().contains("131030"));
        assertFalse(error.getMessage().contains("SECRET-TOKEN"));
    }
}
