package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.ProgramJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgramJpaRepository extends JpaRepository<ProgramJpaEntity, UUID> {
}
