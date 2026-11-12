package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.Category;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.CategoryJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryEntityMapper {

    CategoryJpaEntity toJpa(Category domain);

    Category toDomain(CategoryJpaEntity jpaEntity);
}
