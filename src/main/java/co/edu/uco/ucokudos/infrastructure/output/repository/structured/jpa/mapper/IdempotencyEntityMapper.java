package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.IdempotencyRecord;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.IdempotencyJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IdempotencyEntityMapper {

    @Mapping(target = "id", ignore = true)
    IdempotencyJpaEntity toJpa(IdempotencyRecord domain);

    IdempotencyRecord toDomain(IdempotencyJpaEntity jpaEntity);
}
