package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.UserProgramJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProgramJpaRepository extends JpaRepository<UserProgramJpaEntity, UUID> {
}
