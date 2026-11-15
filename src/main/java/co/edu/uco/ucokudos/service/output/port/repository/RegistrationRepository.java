package co.edu.uco.ucokudos.service.output.port.repository;

import co.edu.uco.ucokudos.domain.model.RegistrationRequest;

public interface RegistrationRepository {

    RegistrationRequest save(RegistrationRequest request);

    boolean existsPendingByEmail(String email);
}
