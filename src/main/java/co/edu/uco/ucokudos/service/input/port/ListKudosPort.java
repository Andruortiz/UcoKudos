package co.edu.uco.ucokudos.service.input.port;

import co.edu.uco.ucokudos.service.input.dto.request.ListKudosRequest;
import co.edu.uco.ucokudos.service.input.dto.response.ListKudosResponse;

public interface ListKudosPort {

    ListKudosResponse execute(ListKudosRequest request);
}
