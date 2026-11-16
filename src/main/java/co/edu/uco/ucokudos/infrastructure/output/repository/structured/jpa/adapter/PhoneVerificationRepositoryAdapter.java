package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.adapter;

import co.edu.uco.ucokudos.domain.model.PhoneVerification;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper.PhoneVerificationEntityMapper;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.repository.PhoneVerificationJpaRepository;
import co.edu.uco.ucokudos.service.output.port.repository.PhoneVerificationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PhoneVerificationRepositoryAdapter implements PhoneVerificationRepository {

    private final PhoneVerificationJpaRepository jpaRepository;
    private final PhoneVerificationEntityMapper mapper;

    public PhoneVerificationRepositoryAdapter(final PhoneVerificationJpaRepository jpaRepository,
                                              final PhoneVerificationEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PhoneVerification> find(final UUID userId) {
        return jpaRepository.findById(userId).map(mapper::toDomain);
    }

    @Override
    public void save(final PhoneVerification verification) {
        jpaRepository.save(mapper.toJpa(verification));
    }

    @Override
    public void delete(final UUID userId) {
        jpaRepository.deleteById(userId);
    }
}
