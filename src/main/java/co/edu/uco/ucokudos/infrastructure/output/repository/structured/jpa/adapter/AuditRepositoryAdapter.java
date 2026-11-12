package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.AuditRecord;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.AuditRecordEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.AuditRecordJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.AuditRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditRepositoryAdapter implements AuditRepository {

    private final AuditRecordJpaRepository jpaRepository;
    private final AuditRecordEntityMapper mapper;

    public AuditRepositoryAdapter(
            final AuditRecordJpaRepository jpaRepository,
            final AuditRecordEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(final AuditRecord record) {
        jpaRepository.save(mapper.toJpa(record));
    }
}
