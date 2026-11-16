package co.edu.uco.ucokudos.service.input.port;

import java.util.UUID;

public interface RequestPhoneVerificationPort {

    void execute(UUID userId);
}
