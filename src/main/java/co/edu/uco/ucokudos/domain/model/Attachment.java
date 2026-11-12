package co.edu.uco.ucokudos.domain.model;

public record Attachment(String name, String contentType, byte[] content) {

    public long size() {
        return content.length;
    }
}
