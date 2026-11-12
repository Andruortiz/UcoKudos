package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.AuditRecordJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditRecordEntityMapper {

    AuditRecordJpaEntity toJpa(AuditRecord domain);

    AuditRecord toDomain(AuditRecordJpaEntity jpaEntity);
}
