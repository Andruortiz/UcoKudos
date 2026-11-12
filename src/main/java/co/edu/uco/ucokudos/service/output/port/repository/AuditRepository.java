package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.AuditRecord;

public interface AuditRepository {

    void save(AuditRecord record);
}
