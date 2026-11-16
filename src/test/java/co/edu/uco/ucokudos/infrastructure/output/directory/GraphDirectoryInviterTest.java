package co.edu.uco.ucokudos.infrastructure.output.directory;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.AccessStatus;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphDirectoryInviterTest {

    private HttpServer server;
    private String url;
    private final List<String> graphBodies = new ArrayList<>();
    private final List<String> graphAuthorization = new ArrayList<>();
    private final List<String> tokenBodies = new ArrayList<>();
    private final AtomicInteger tokenCalls = new AtomicInteger();
    private String invitationStatus = "PendingAcceptance";
    private int invitationCode = 201;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tenant-1/oauth2/v2.0/token", exchange -> {
            tokenCalls.incrementAndGet();
            tokenBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            reply(exchange, 200, "{\"access_token\":\"graph-token\",\"expires_in\":3600}");
        });
        server.createContext("/invitations", exchange -> {
            graphAuthorization.add(exchange.getRequestHeaders().getFirst("Authorization"));
            graphBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            reply(exchange, invitationCode, "{\"status\":\"" + invitationStatus + "\"}");
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private static void reply(final com.sun.net.httpserver.HttpExchange exchange, final int code, final String body)
            throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private GraphDirectoryInviter inviter(final String clientId, final String secret) {
        return new GraphDirectoryInviter(url, url, "tenant-1", clientId, secret, "https://kudos.example");
    }

    @Test
    void itAsksForATokenAndSendsTheInvitationWithIt() {
        final AccessStatus status = inviter("client-1", "secret-1").invite("pedro@uco.net.co");

        assertEquals(AccessStatus.INVITED, status);
        assertTrue(tokenBodies.get(0).contains("client_id=client-1") && tokenBodies.get(0).contains("grant_type=client_credentials"));
        assertEquals("Bearer graph-token", graphAuthorization.get(0));
        assertTrue(graphBodies.get(0).contains("\"invitedUserEmailAddress\":\"pedro@uco.net.co\""), graphBodies.get(0));
        assertTrue(graphBodies.get(0).contains("\"inviteRedirectUrl\":\"https://kudos.example\""));
        assertTrue(graphBodies.get(0).contains("\"sendInvitationMessage\":true"));
    }

    @Test
    void theTokenIsReusedBetweenInvitations() {
        final GraphDirectoryInviter inviter = inviter("client-1", "secret-1");

        inviter.invite("a@uco.net.co");
        inviter.invite("b@uco.net.co");

        assertEquals(1, tokenCalls.get());
        assertEquals(2, graphBodies.size());
    }

    @Test
    void aCompletedInvitationMeansTheyAlreadyHaveAccess() {
        invitationStatus = "Completed";

        assertEquals(AccessStatus.ALREADY_HAS_ACCESS, inviter("client-1", "secret-1").invite("a@uco.net.co"));
    }

    @Test
    void aGraphErrorIsReportedAsUnavailable() {
        invitationCode = 403;

        final DomainException error = assertThrows(DomainException.class,
                () -> inviter("client-1", "secret-1").invite("a@uco.net.co"));

        assertEquals(MessageCodes.ACCESS_UNAVAILABLE, error.getCode());
    }

    @Test
    void withoutCredentialsNothingIsCalled() {
        final DomainException error = assertThrows(DomainException.class, () -> inviter("", "").invite("a@uco.net.co"));

        assertEquals(MessageCodes.ACCESS_UNAVAILABLE, error.getCode());
        assertEquals(0, tokenCalls.get());
    }
}
