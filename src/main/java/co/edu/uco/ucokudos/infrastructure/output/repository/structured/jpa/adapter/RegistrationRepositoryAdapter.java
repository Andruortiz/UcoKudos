package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.RegistrationRequest;
import co.edu.uco.ucokudos.domain.model.RegistrationStatus;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.RegistrationRequestJpaEntity;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.RegistrationRequestJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.RegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class RegistrationRepositoryAdapter implements RegistrationRepository {

    private final RegistrationRequestJpaRepository jpaRepository;

    public RegistrationRepositoryAdapter(final RegistrationRequestJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RegistrationRequest save(final RegistrationRequest request) {
        final RegistrationRequestJpaEntity entity = new RegistrationRequestJpaEntity();
        entity.setId(request.getId());
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setRole(request.getRole());
        entity.setStatus(request.getStatus());
        entity.setCreatedAt(request.getCreatedAt());
        jpaRepository.save(entity);
        return request;
    }

    @Override
    public boolean existsPendingByEmail(final String email) {
        return jpaRepository.existsByEmailAndStatus(email, RegistrationStatus.PENDING);
    }
}
