package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.domain.model.NotificationChannel;
import co.edu.uco.ucokudos.service.output.port.notification.DeliveryResult;
import co.edu.uco.ucokudos.service.output.port.notification.KudoNotice;
import co.edu.uco.ucokudos.service.output.port.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sends the Kudo notice by WhatsApp with the Meta Cloud API, using the approved template (ADR-0009). It exists only
 * when the access token and the phone number id are defined, so without them the dispatcher leaves WhatsApp
 * notifications pending. The token never goes into the repository: set KUDOS_WHATSAPP_TOKEN and
 * KUDOS_WHATSAPP_PHONE_NUMBER_ID in the environment.
 */
@Component
@ConditionalOnProperty({"kudos.whatsapp.token", "kudos.whatsapp.phone-number-id"})
public class WhatsAppNotificationSender implements NotificationSender {

    private static final int MAX_PARAMETER_LENGTH = 1000;
    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppNotificationSender.class);
    private static final int MAX_ERROR_BODY = 300;

    private final RestClient client;
    private final String phoneNumberId;
    private final String template;
    private final String language;
    private final String apiVersion;

    public WhatsAppNotificationSender(
            @Value("${kudos.whatsapp.base-url}") final String baseUrl,
            @Value("${kudos.whatsapp.api-version}") final String apiVersion,
            @Value("${kudos.whatsapp.phone-number-id}") final String phoneNumberId,
            @Value("${kudos.whatsapp.token}") final String token,
            @Value("${kudos.whatsapp.template}") final String template,
            @Value("${kudos.whatsapp.language}") final String language) {
        this.apiVersion = apiVersion;
        this.phoneNumberId = phoneNumberId;
        this.template = template;
        this.language = language;
        // timeouts so that a slow provider cannot block the scheduler that runs every few seconds
        final JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public DeliveryResult send(final KudoNotice notice) {
        final String to = digitsOf(notice.recipientPhone());
        if (to.isEmpty()) {
            return DeliveryResult.SKIPPED;
        }
        final Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", template,
                        "language", Map.of("code", language),
                        "components", List.of(Map.of(
                                "type", "body",
                                // same order as the template: recipient, author, category, link
                                "parameters", List.of(
                                        text(notice.recipientName()),
                                        text(notice.authorName()),
                                        text(notice.categoryName()),
                                        text(notice.link()))))));
        try {
            final String answer = client.post()
                    .uri("/{version}/{phoneNumberId}/messages", apiVersion, phoneNumberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            // Meta accepting the request is not delivery: keep the message id to follow it in Meta's panel or webhooks
            LOG.info("WhatsApp aceptado por Meta: mensaje {}, estado {}, destino ...{}", find(answer, "\"id\"\\s*:\\s*\"([^\"]+)\""),
                    find(answer, "\"message_status\"\\s*:\\s*\"([^\"]+)\""), to.substring(Math.max(0, to.length() - 4)));
        } catch (final RestClientResponseException e) {
            // a short, readable cause for the outbox; never the token, which only travels in the header
            final String answer = e.getResponseBodyAsString();
            throw new IllegalStateException("WhatsApp respondió " + e.getStatusCode().value() + ": "
                    + (answer.length() > MAX_ERROR_BODY ? answer.substring(0, MAX_ERROR_BODY) : answer));
        }
        return DeliveryResult.SENT;
    }

    private static String find(final String text, final String regex) {
        if (text == null) {
            return "?";
        }
        final Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : "?";
    }

    /** Meta wants the number in international format, digits only, with no plus sign. */
    private static String digitsOf(final String phone) {
        return phone == null ? "" : phone.replaceAll("\\D", "");
    }

    /** Template parameters cannot hold line breaks, tabs or long runs of spaces. */
    private static Map<String, String> text(final String value) {
        final String clean = value == null ? "-" : value.replaceAll("\\s+", " ").trim();
        return Map.of("type", "text",
                "text", clean.length() > MAX_PARAMETER_LENGTH ? clean.substring(0, MAX_PARAMETER_LENGTH) : clean);
    }
}
