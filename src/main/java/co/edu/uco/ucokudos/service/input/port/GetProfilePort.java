package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.response.ProfileResponse;

import java.util.UUID;

public interface GetProfilePort {

    ProfileResponse execute(UUID userId);
}
