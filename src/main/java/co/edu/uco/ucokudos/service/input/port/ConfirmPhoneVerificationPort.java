package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.request.ConfirmPhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;

public interface ConfirmPhoneVerificationPort {

    ProfileResponse execute(ConfirmPhoneRequest request);
}
