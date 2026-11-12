package co.edu.uco.ucokudos.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Kudo {

    UUID id;
    UUID authorId;
    UUID recipientId;
    UUID categoryId;
    String content;
    Instant createdAt;
    KudoStatus status;

}
