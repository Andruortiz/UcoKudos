package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.User;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserJpaEntity toJpa(User domain);

    User toDomain(UserJpaEntity jpaEntity);
}
