package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Optional<Category> findById(UUID id);
    List<Category> findActive();

}
