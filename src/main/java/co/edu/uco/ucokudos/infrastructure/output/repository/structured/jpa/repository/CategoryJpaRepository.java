package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository;

import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
    List<CategoryJpaEntity> findByActiveTrueOrderByNameAsc();
}
