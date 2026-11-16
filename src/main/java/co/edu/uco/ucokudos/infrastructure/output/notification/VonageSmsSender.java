package co.edu.uco.ucokudos.infrastructure.output.notification;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.output.port.notification.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class VonageSmsSender implements SmsSender {

    private static final Logger LOG = LoggerFactory.getLogger(VonageSmsSender.class);
    private static final Locale LOCALE = Locale.of("es", "CO");

    private final RestClient client;
    private final MessageSource messages;
    private final String apiKey;
    private final String apiSecret;
    private final String from;

    public VonageSmsSender(
            @Value("${kudos.vonage.base-url}") final String baseUrl,
            @Value("${kudos.vonage.api-key}") final String apiKey,
            @Value("${kudos.vonage.api-secret}") final String apiSecret,
            @Value("${kudos.vonage.from}") final String from,
            final MessageSource messages) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.from = from;
        this.messages = messages;
        final JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public void sendVerificationCode(final String phone, final String code) {
        if (apiKey.isBlank() || apiSecret.isBlank() || from.isBlank()) {
            throw new DomainException(MessageCodes.PHONE_VERIFICATION_UNAVAILABLE);
        }
        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("api_key", apiKey);
        form.add("api_secret", apiSecret);
        form.add("from", from);
        form.add("to", phone.startsWith("+") ? phone.substring(1) : phone);
        form.add("text", messages.getMessage(MessageCodes.PHONE_VERIFICATION_SMS, new Object[]{code}, LOCALE));
        final Map<?, ?> answer;
        try {
            answer = client.post().uri("/sms/json").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form).retrieve().body(Map.class);
        } catch (final RestClientException e) {
            LOG.warn("No se pudo contactar a Vonage: {}", e.getClass().getSimpleName());
            throw new DomainException(MessageCodes.PHONE_VERIFICATION_UNAVAILABLE);
        }
        final String status = firstStatus(answer);
        if (!"0".equals(status)) {
            LOG.warn("Vonage rechazó el SMS de verificación: status {}", status);
            throw new DomainException(MessageCodes.PHONE_VERIFICATION_UNAVAILABLE);
        }
    }

    private static String firstStatus(final Map<?, ?> answer) {
        if (answer != null && answer.get("messages") instanceof List<?> list && !list.isEmpty()
                && list.get(0) instanceof Map<?, ?> first) {
            return String.valueOf(first.get("status"));
        }
        return "invalid-response";
    }
}
