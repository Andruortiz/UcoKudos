package co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.mapper;

import co.edu.uco.ucokudos.domain.model.PhoneVerification;
import co.edu.uco.ucokudos.infrastructure.output.repository.structured.jpa.entity.PhoneVerificationJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PhoneVerificationEntityMapper {

    PhoneVerificationJpaEntity toJpa(PhoneVerification domain);

    PhoneVerification toDomain(PhoneVerificationJpaEntity jpaEntity);
}
