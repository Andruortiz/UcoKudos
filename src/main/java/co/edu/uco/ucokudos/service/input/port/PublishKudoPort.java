package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.request.PublishKudoRequest;
import co.edu.uco.ucokudos.service.input.dto.response.PublishKudoResponse;

public interface PublishKudoPort {

    PublishKudoResponse execute(PublishKudoRequest request);
}
