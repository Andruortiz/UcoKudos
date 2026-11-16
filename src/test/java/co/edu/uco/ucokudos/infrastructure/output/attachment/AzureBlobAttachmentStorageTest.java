package co.edu.uco.ucokudos.infrastructure.output.attachment;

import co.edu.uco.ucokudos.crosscutting.exception.DomainException;
import co.edu.uco.ucokudos.crosscutting.message.MessageCodes;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureBlobAttachmentStorageTest {

    private HttpServer server;
    private String containerUrl;
    private int failWith = 0;
    private final Map<String, byte[]> blobs = new ConcurrentHashMap<>();
    private final List<String> queries = new ArrayList<>();
    private final List<String> blobTypes = new ArrayList<>();

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/adjuntos", exchange -> {
            final String path = exchange.getRequestURI().getRawPath();
            queries.add(exchange.getRequestURI().getRawQuery());
            if (failWith != 0) {
                exchange.sendResponseHeaders(failWith, -1);
            } else if ("PUT".equals(exchange.getRequestMethod())) {
                blobTypes.add(exchange.getRequestHeaders().getFirst("x-ms-blob-type"));
                blobs.put(path, exchange.getRequestBody().readAllBytes());
                exchange.sendResponseHeaders(201, -1);
            } else if (blobs.containsKey(path)) {
                final byte[] body = blobs.get(path);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
            exchange.close();
        });
        server.start();
        containerUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/adjuntos";
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private AzureBlobAttachmentStorage storage() {
        return new AzureBlobAttachmentStorage(containerUrl + "/", "?sv=2023&sig=a%2Bb%3D");
    }

    @Test
    void aBlobIsWrittenAsBlockBlobAndReadBackKeepingTheSasUntouched() {
        storage().put("k1/a1", "application/pdf", new byte[]{1, 2, 3});

        assertArrayEquals(new byte[]{1, 2, 3}, storage().get("k1/a1"));
        assertEquals("BlockBlob", blobTypes.get(0));
        assertEquals("sv=2023&sig=a%2Bb%3D", queries.get(0));
    }

    @Test
    void aMissingBlobOrAServerErrorIsReportedAsUnavailable() {
        assertEquals(MessageCodes.ATTACHMENT_STORAGE_UNAVAILABLE,
                assertThrows(DomainException.class, () -> storage().get("no/existe")).getCode());
        failWith = 403;
        assertEquals(MessageCodes.ATTACHMENT_STORAGE_UNAVAILABLE,
                assertThrows(DomainException.class, () -> storage().put("k/a", "image/png", new byte[]{1})).getCode());
    }

    @Test
    void withoutContainerOrSasItRefusesToStart() {
        assertTrue(assertThrows(IllegalStateException.class,
                () -> new AzureBlobAttachmentStorage("", "sas")).getMessage().contains("required"));
        assertThrows(IllegalStateException.class, () -> new AzureBlobAttachmentStorage(containerUrl, " "));
    }
}
