package co.edu.uco.ucokudos.service.input.dto.response;

/** What the detail shows about an attachment; the file content never travels in the JSON. */
public record AttachmentResponse(java.util.UUID id, String name, String contentType, long size) {
}
