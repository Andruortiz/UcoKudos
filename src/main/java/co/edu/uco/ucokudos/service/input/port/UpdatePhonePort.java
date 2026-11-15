package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.request.UpdatePhoneRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;

public interface UpdatePhonePort {

    ProfileResponse execute(UpdatePhoneRequest request);
}
