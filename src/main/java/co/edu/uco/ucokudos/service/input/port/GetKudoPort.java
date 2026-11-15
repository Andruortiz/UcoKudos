package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.response.KudoDetailResponse;

import java.util.UUID;

public interface GetKudoPort {

    KudoDetailResponse execute(UUID requesterId, UUID kudoId);
}
