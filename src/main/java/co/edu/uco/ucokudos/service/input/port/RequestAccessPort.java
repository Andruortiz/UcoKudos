package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.request.RequestAccessRequest;
import co.edu.uco.ucokudos.service.input.dto.response.RequestAccessResponse;

public interface RequestAccessPort {

    RequestAccessResponse execute(RequestAccessRequest request);
}
