package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.Attachment;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.KudoAttachmentJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface KudoAttachmentEntityMapper {

    @Mapping(target = "id", ignore = true)
    KudoAttachmentJpaEntity toJpa(Attachment domain, UUID kudoId);

    Attachment toDomain(KudoAttachmentJpaEntity jpaEntity);
}
