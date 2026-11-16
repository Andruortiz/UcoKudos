package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.PhoneVerification;

import java.util.Optional;
import java.util.UUID;

public interface PhoneVerificationRepository {

    Optional<PhoneVerification> find(UUID userId);

    void save(PhoneVerification verification);

    void delete(UUID userId);
}
