package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.CategoryEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.CategoryJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;
    private final CategoryEntityMapper mapper;

    public CategoryRepositoryAdapter(
            final CategoryJpaRepository jpaRepository,
            final CategoryEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Category> findById(final UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Category> findActive() {
        return jpaRepository.findByActiveTrueOrderByNameAsc().stream().map(mapper::toDomain).toList();
    }
}
