package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.Notification;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.NotificationJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationEntityMapper {

    NotificationJpaEntity toJpa(Notification domain);

    Notification toDomain(NotificationJpaEntity jpaEntity);
}
