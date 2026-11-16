package co.edu.uco.ucokudos.infrastructure.input.controller;

import co.edu.uco.ucokudos.crosscutting.util.helper.UuidHelper;
import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.domain.model.Role;
import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.domain.model.UserStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.CategoryEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.UserEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.CategoryJpaRepository;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.UserJpaRepository;
import co.edu.uco.ucokudos.service.input.dto.response.DispatchNotificationsResponse;
import co.edu.uco.ucokudos.service.input.port.DispatchNotificationsPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"kudos.dev-auth.enabled=true",
        "kudos.dev.user-id=00000000-0000-0000-0000-000000000001"})
class PushStreamIntegrationTest {

    private static final String BOUNDARY = "kudosboundary";

    @LocalServerPort int port;
    @Autowired UserJpaRepository userJpa;
    @Autowired UserEntityMapper userMapper;
    @Autowired CategoryJpaRepository categoryJpa;
    @Autowired CategoryEntityMapper categoryMapper;
    @Autowired DispatchNotificationsPort dispatch;

    private final HttpClient http = HttpClient.newHttpClient();

    private User saveUser(final String name, final Role role) {
        final User user = new User();
        user.setId(UuidHelper.generate());
        user.setDirectoryId("dir-" + user.getId());
        user.setName(name);
        user.setEmail(user.getId() + "@uco.net.co");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setPhoneVerified(true);
        user.setCreatedAt(Instant.now());
        userJpa.save(userMapper.toJpa(user));
        return user;
    }

    private Category saveCategory() {
        final Category category = new Category();
        category.setId(UuidHelper.generate());
        category.setName("Trabajo en equipo " + category.getId());
        category.setCreatedAt(Instant.now());
        categoryJpa.save(categoryMapper.toJpa(category));
        return category;
    }

    private CompletableFuture<String> openStream(final User user, final CompletableFuture<Void> connected) {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/notifications/stream"))
                .header("X-Dev-User", user.getId().toString())
                .header("Accept", "text/event-stream")
                .build();
        final CompletableFuture<String> kudoEvent = new CompletableFuture<>();
        http.sendAsync(request, HttpResponse.BodyHandlers.ofLines()).thenAccept(response -> {
            connected.complete(null);
            try (var lines = response.body()) {
                lines.filter(line -> line.startsWith("data:")).findFirst().ifPresent(kudoEvent::complete);
            }
        });
        return kudoEvent;
    }

    private String publish(final User author, final User recipient, final Category category) throws Exception {
        final String body = "--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"kudo\"\r\n"
                + "Content-Type: application/json\r\n\r\n"
                + "{\"recipientId\":\"" + recipient.getId() + "\",\"categoryId\":\"" + category.getId()
                + "\",\"content\":\"Gracias por la ayuda\"}\r\n--" + BOUNDARY + "--\r\n";
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/kudos"))
                .header("X-Dev-User", author.getId().toString())
                .header("Idempotency-Key", UuidHelper.generate().toString())
                .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    @Test
    void theRecipientWithAnOpenStreamReceivesTheKudoAndAnOfflineOneDoesNot() throws Exception {
        final User ana = saveUser("Ana Estudiante", Role.STUDENT);
        final User luis = saveUser("Luis Docente", Role.TEACHER);
        final User carlos = saveUser("Carlos Estudiante", Role.STUDENT);
        final Category category = saveCategory();

        final CompletableFuture<Void> connected = new CompletableFuture<>();
        final CompletableFuture<String> luisEvent = openStream(luis, connected);
        connected.get(10, TimeUnit.SECONDS);

        final String first = publish(ana, luis, category);
        assertTrue(first.contains("\"id\""), first);
        final String second = publish(ana, carlos, category);
        assertTrue(second.contains("\"id\""), second);
        final DispatchNotificationsResponse result = dispatch.execute();

        final String data = luisEvent.get(10, TimeUnit.SECONDS);
        assertTrue(data.contains("Ana Estudiante") && data.contains(category.getName()), data);
        assertEquals(1, result.getSent());
        assertTrue(result.getSkipped() >= 1);
    }
}
