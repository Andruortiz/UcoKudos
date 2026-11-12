package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.Kudo;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.KudoJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KudoEntityMapper {

    KudoJpaEntity toJpa(Kudo domain);

    @Mapping(target = "attachments", ignore = true)
    Kudo toDomain(KudoJpaEntity jpaEntity);
}
