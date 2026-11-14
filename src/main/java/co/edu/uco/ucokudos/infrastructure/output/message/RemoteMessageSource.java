package co.edu.uco.ucokudos.infrastructure.output.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.support.AbstractMessageSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RemoteMessageSource extends AbstractMessageSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteMessageSource.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_CHARACTERS = 262_144;

    private final String url;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private volatile Map<String, MessageFormat> messages = Map.of();

    public RemoteMessageSource(final String url) {
        this.url = url;
    }

    public boolean refresh() {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body().length() > MAX_CHARACTERS) {
                LOGGER.warn("Message catalog not updated: status {}", response.statusCode());
                return false;
            }
            messages = parse(response.body());
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final IOException | RuntimeException e) {
            LOGGER.warn("Message catalog not updated: {}", e.getMessage());
            return false;
        }
    }

    private Map<String, MessageFormat> parse(final String body) {
        final Map<String, MessageFormat> parsed = new HashMap<>();
        JsonParserFactory.getJsonParser().parseMap(body).forEach((code, value) -> {
            if (value instanceof String text && !text.isBlank()) {
                try {
                    parsed.put(code, new MessageFormat(text));
                } catch (final IllegalArgumentException e) {
                    LOGGER.warn("Message {} ignored: invalid pattern", code);
                }
            }
        });
        return Map.copyOf(parsed);
    }

    @Override
    protected MessageFormat resolveCode(final String code, final Locale locale) {
        return messages.get(code);
    }
}
