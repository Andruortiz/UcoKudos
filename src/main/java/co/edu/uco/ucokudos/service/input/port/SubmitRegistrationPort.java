package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.request.SubmitRegistrationRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RegistrationResponse;

public interface SubmitRegistrationPort {

    RegistrationResponse execute(SubmitRegistrationRequest request);
}
