package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.FacultyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacultyJpaRepository   extends JpaRepository<FacultyJpaEntity, UUID> {
}
