package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VonageSmsSenderTest {

    private HttpServer server;
    private String url;
    private String answer = "{\"messages\":[{\"status\":\"0\"}]}";
    private final List<String> bodies = new ArrayList<>();
    private final StaticMessageSource messages = new StaticMessageSource();

    @BeforeEach
    void start() throws IOException {
        messages.addMessage(MessageCodes.PHONE_VERIFICATION_SMS, Locale.of("es", "CO"), "Tu código es {0}");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/sms/json", exchange -> {
            bodies.add(URLDecoder.decode(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8));
            reply(exchange);
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private void reply(final HttpExchange exchange) throws IOException {
        final byte[] bytes = answer.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private VonageSmsSender sender(final String key, final String secret, final String from) {
        return new VonageSmsSender(url, key, secret, from, messages);
    }

    @Test
    void itPostsTheMessageToVonageWithoutThePlus() {
        sender("key-1", "secret-1", "UCOKudos").sendVerificationCode("+573001234567", "123456");

        final String body = bodies.get(0);
        assertTrue(body.contains("api_key=key-1"), body);
        assertTrue(body.contains("api_secret=secret-1"));
        assertTrue(body.contains("from=UCOKudos"));
        assertTrue(body.contains("to=573001234567"));
        assertTrue(body.contains("text=Tu código es 123456"));
    }

    @Test
    void aVonageRejectionIsReportedAsUnavailable() {
        answer = "{\"messages\":[{\"status\":\"4\"}]}";

        final DomainException error = assertThrows(DomainException.class,
                () -> sender("key-1", "secret-1", "UCOKudos").sendVerificationCode("+573001234567", "123456"));

        assertEquals(MessageCodes.PHONE_VERIFICATION_UNAVAILABLE, error.getCode());
    }

    @Test
    void withoutCredentialsNothingIsCalled() {
        final DomainException error = assertThrows(DomainException.class,
                () -> sender("", "", "").sendVerificationCode("+573001234567", "123456"));

        assertEquals(MessageCodes.PHONE_VERIFICATION_UNAVAILABLE, error.getCode());
        assertTrue(bodies.isEmpty());
    }
}
