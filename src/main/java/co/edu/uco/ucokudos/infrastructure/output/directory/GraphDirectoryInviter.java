package co.edu.uco.ucokudos.infrastructure.output.directory;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.domain.model.AccessStatus;
import co.edu.uco.ucokudos.service.output.port.directory.DirectoryInviter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class GraphDirectoryInviter implements DirectoryInviter {

    private static final String COMPLETED = "Completed";

    private final RestClient login;
    private final RestClient graph;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUrl;

    private String token;
    private Instant tokenExpiresAt = Instant.MIN;

    public GraphDirectoryInviter(
            @Value("${kudos.graph.login-url}") final String loginUrl,
            @Value("${kudos.graph.base-url}") final String baseUrl,
            @Value("${kudos.graph.tenant-id}") final String tenantId,
            @Value("${kudos.graph.client-id}") final String clientId,
            @Value("${kudos.graph.client-secret}") final String clientSecret,
            @Value("${kudos.graph.invite-redirect-url}") final String redirectUrl) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        final JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.login = RestClient.builder().baseUrl(loginUrl).requestFactory(factory).build();
        this.graph = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public AccessStatus invite(final String email) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new DomainException(MessageCodes.ACCESS_UNAVAILABLE);
        }
        try {
            final Map<?, ?> answer = graph.post().uri("/invitations")
                    .headers(headers -> headers.setBearerAuth(accessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("invitedUserEmailAddress", email, "inviteRedirectUrl", redirectUrl,
                            "sendInvitationMessage", true))
                    .retrieve().body(Map.class);
            return answer != null && COMPLETED.equals(answer.get("status"))
                    ? AccessStatus.ALREADY_HAS_ACCESS : AccessStatus.INVITED;
        } catch (final RestClientException e) {
            throw new DomainException(MessageCodes.ACCESS_UNAVAILABLE);
        }
    }

    private synchronized String accessToken() {
        if (token == null || Instant.now().isAfter(tokenExpiresAt)) {
            final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("scope", "https://graph.microsoft.com/.default");
            form.add("grant_type", "client_credentials");
            final Map<?, ?> answer = login.post().uri("/{tenant}/oauth2/v2.0/token", tenantId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Map.class);
            if (answer == null || !(answer.get("access_token") instanceof String value)) {
                throw new IllegalStateException("Microsoft no entregó un token");
            }
            token = value;
            final long seconds = answer.get("expires_in") instanceof Number number ? number.longValue() : 300;
            tokenExpiresAt = Instant.now().plusSeconds(Math.max(seconds - 60, 0));
        }
        return token;
    }
}
