package co.edu.uco.ucokudos.infrastructure.output.attachment;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import co.edu.uco.ucokudos.service.output.port.attachment.AttachmentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
@Primary
@ConditionalOnProperty(name = "kudos.attachments.storage", havingValue = "azure")
public class AzureBlobAttachmentStorage implements AttachmentStorage {

    private static final Logger LOG = LoggerFactory.getLogger(AzureBlobAttachmentStorage.class);
    private static final String API_VERSION = "2023-11-03";

    private final RestClient client;
    private final String containerUrl;
    private final String sas;

    public AzureBlobAttachmentStorage(
            @Value("${kudos.attachments.container-url}") final String containerUrl,
            @Value("${kudos.attachments.sas}") final String sas) {
        if (containerUrl.isBlank() || sas.isBlank()) {
            throw new IllegalStateException("kudos.attachments.container-url and kudos.attachments.sas are required");
        }
        this.containerUrl = containerUrl.endsWith("/") ? containerUrl.substring(0, containerUrl.length() - 1) : containerUrl;
        this.sas = sas.startsWith("?") ? sas.substring(1) : sas;
        final JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public void put(final String key, final String contentType, final byte[] content) {
        try {
            client.put().uri(blob(key))
                    .header("x-ms-blob-type", "BlockBlob")
                    .header("x-ms-version", API_VERSION)
                    .header("Content-Type", contentType)
                    .body(content).retrieve().toBodilessEntity();
        } catch (final RestClientException e) {
            LOG.warn("No se pudo guardar el adjunto en Blob Storage: {}", e.getClass().getSimpleName());
            throw new DomainException(MessageCodes.ATTACHMENT_STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public byte[] get(final String key) {
        try {
            return client.get().uri(blob(key)).header("x-ms-version", API_VERSION).retrieve().body(byte[].class);
        } catch (final RestClientException e) {
            LOG.warn("No se pudo leer el adjunto de Blob Storage: {}", e.getClass().getSimpleName());
            throw new DomainException(MessageCodes.ATTACHMENT_STORAGE_UNAVAILABLE);
        }
    }

    private URI blob(final String key) {
        return URI.create(containerUrl + "/" + key + "?" + sas);
    }
}
