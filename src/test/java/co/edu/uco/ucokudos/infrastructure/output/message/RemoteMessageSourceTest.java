package co.edu.uco.ucokudos.infrastructure.output.message;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoteMessageSourceTest {

    private static final Locale LOCALE = Locale.of("es", "CO");

    private HttpServer server;
    private final AtomicReference<String> body = new AtomicReference<>("{}");
    private final AtomicInteger status = new AtomicInteger(200);
    private RemoteMessageSource source;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/messages.json", exchange -> {
            final byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        final ResourceBundleMessageSource bundle = new ResourceBundleMessageSource();
        bundle.setBasename("messages");
        bundle.setDefaultEncoding("UTF-8");
        bundle.setFallbackToSystemLocale(false);
        source = new RemoteMessageSource("http://127.0.0.1:" + server.getAddress().getPort() + "/messages.json");
        source.setParentMessageSource(bundle);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void beforeTheFirstRefreshTheBundleAnswers() {
        assertEquals("El nombre es obligatorio", source.getMessage("user.name.required", null, LOCALE));
    }

    @Test
    void aRemoteTextOverridesTheBundleAndTheRestKeepsFallingBack() {
        body.set("{\"user.name.required\": \"Falta el nombre\"}");
        assertTrue(source.refresh());
        assertEquals("Falta el nombre", source.getMessage("user.name.required", null, LOCALE));
        assertEquals("El correo no es válido", source.getMessage("user.email.invalid", null, LOCALE));
    }

    @Test
    void argumentsAreFormatted() {
        body.set("{\"kudo.daily.limit\": \"Máximo {0} por día\"}");
        source.refresh();
        assertEquals("Máximo 50 por día", source.getMessage("kudo.daily.limit", new Object[]{"50"}, LOCALE));
    }

    @Test
    void aChangeInTheFileIsPickedUpOnTheNextRefresh() {
        body.set("{\"user.name.required\": \"Uno\"}");
        source.refresh();
        body.set("{\"user.name.required\": \"Dos\"}");
        source.refresh();
        assertEquals("Dos", source.getMessage("user.name.required", null, LOCALE));
    }

    @Test
    void invalidEntriesAreIgnored() {
        body.set("{\"user.name.required\": \"  \", \"user.email.invalid\": \"mal {0\", \"user.phone.invalid\": 5}");
        assertTrue(source.refresh());
        assertEquals("El nombre es obligatorio", source.getMessage("user.name.required", null, LOCALE));
        assertEquals("El correo no es válido", source.getMessage("user.email.invalid", null, LOCALE));
    }

    @Test
    void aFailedRefreshKeepsTheLastGoodCatalog() {
        body.set("{\"user.name.required\": \"Bueno\"}");
        source.refresh();
        status.set(500);
        assertFalse(source.refresh());
        body.set("esto no es json");
        status.set(200);
        assertFalse(source.refresh());
        assertEquals("Bueno", source.getMessage("user.name.required", null, LOCALE));
    }

    @Test
    void withoutUrlNothingIsFetched() {
        assertFalse(new RemoteMessageSource("").refresh());
    }
}
