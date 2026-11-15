package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.domain.model.RegistrationStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.RegistrationRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistrationRequestJpaRepository extends JpaRepository<RegistrationRequestJpaEntity, UUID> {

    boolean existsByEmailAndStatus(String email, RegistrationStatus status);
}
